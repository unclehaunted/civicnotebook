# CivicNotebook — Próximos Passos da Auditoria

## Estado Atual (16 Jul 2026)

### ✅ Completado
1. **Parser text duplication fix** (commits 47ef010, 039a812)
   - Removida lógica `startsWithStrong` que duplicava texto
   - Parser agora mais conservador: só trata como subheading se houver exatamente 1 child `<strong>`
   - Texto duplicado eliminado

2. **Botão "Nova Nota" no Caderno** (commits b838b1a, 941701c)
   - FAB vermelho adicionado ao CadernoScreen
   - Método `createNewNote()` implementado no CadernoViewModel
   - Notas standalone criadas com tipo `STANDALONE`

3. **Debounce e auto-save melhorado** (commit 7d9aca9)
   - Debounce de 800ms antes de guardar
   - Status "A guardar..." só aparece quando está ativamente a guardar
   - Pill "Livre" para notas standalone

4. **Modo de anotação visível** (commits e77145e, a760b01)
   - Banner vermelho explicativo quando em modo anotação
   - Instruções claras: "Selecione qualquer texto do artigo para realçar, comentar ou criar uma nota"
   - Botão X vermelho para sair do modo
   - Cantos arredondados no banner alinhados com o card de conteúdo

### ⚠️ Problemas Identificados mas Não Resolvidos

1. **Parser: Hero image caption tratada como texto**
   - Exemplo: palavra "Lusa" em https://avante.pt/pt/2745/emdestaque/184314/...
   - Caption da foto está a ser incluída no corpo do artigo
   - **Causa:** Parser extrai `article p` sem filtrar `figure figcaption`
   - **Fix necessário:** Excluir `figcaption` da extração de parágrafos
   - **Fix necessário**: Parser extrai artigos de opinião (https://avante.pt/pt/2746/opiniao/...) e aplica a hero photo de um deles a todos. Se um dado artigo de opinião não tiver hero, não colocar qualquer imagem.
   - Parser não trata a <blockquote> com destaque gráfico. Alguns artigos possuem <blockquote> destacada a cinzento e itálico. App deve tratá-los da mesma forma. Para cada URL, procurar <blockquote> e aplicar formatação.
   - Secção argumentos (https://avante.pt/pt/2746/argumentos/) é tratada como memória (https://avante.pt/pt/2746/memoria/)
   - Colocar grelha Sugestões (https://avante.pt/pt/2746/sugestoes/) no final da View Edição.
   - Ícone da view Edição deve ser um jornal (como, por exemplo https://fontawesome.com/icons/classic/solid/newspaper, mas não necessariamente Font Awesome, principalmente se Font Awesome tiver custos ou precisar de atribuição)
   - Ícone de caderno deve ser um bloco de notas (exemplo: https://fontawesome.com/icons/classic/solid/book)
   - Grelha Aconteceu (https://avante.pt/pt/2746/aconteceu/) deve ter uma card na main view. Clicando, vamos para uma disposição em grelha com os conteúdos parsed (https://avante.pt/pt/2746/aconteceu/184461/Pe%C3%A7a-de-Saramago-estreia-em-ingl%C3%AAs.htm e seguintes)

2. **Loading state sem preview**
   - Spinner branco longo sem contexto
   - Má primeira impressão na demo
   - **Fix necessário:** `ReaderUiState.LoadingWithPreview(articleSummary)`

3. **Partilha não inclui summary**
   - Partilha atual: só título + URL
   - Falta: summary/excerpt do artigo
   - Falta: hero image (opcional, mais complexo)

4. **Note status pills incompletos**
   - Pills existem mas não há distinção visual clara entre tipos
   - Falta: cores diferentes por tipo de nota

5. **Proveniência da fonte no Caderno**
   - NoteCard não mostra de que artigo veio a nota
   - Requer: `NoteWithSource` data class + query adicional

## Próximos Passos Prioritários

### ✅ PITCH BLOCKER #4: Loading State com Preview — COMPLETADO

**Severidade:** PITCH BLOCKER  
**Tempo estimado:** 2 horas  
**Ficheiros:** `ReaderViewModel.kt`, `ArticleReaderScreen.kt`
**Status:** ✅ Implementado

**Implementação:**
1. Adicionar `ReaderUiState.LoadingWithPreview(articleSummary: ArticleSummary)`
2. Quando `loadArticle()` é chamado, emitir `LoadingWithPreview` imediatamente
3. No UI, mostrar hero image + título do summary enquanto carrega conteúdo
4. Spinner pequeno no fundo em vez de tela branca

**Código:**
```kotlin
// ReaderViewModel.kt
sealed class ReaderUiState {
    data object Loading : ReaderUiState()
    data class LoadingWithPreview(val summary: ArticleSummary) : ReaderUiState()
    data class Success(val article: Article) : ReaderUiState()
    // ...
}

fun loadArticle(articleSummary: ArticleSummary, editionNumber: String) {
    viewModelScope.launch {
        _uiState.value = ReaderUiState.LoadingWithPreview(articleSummary)
        // ... resto do código
    }
}
```

```kotlin
// ArticleReaderScreen.kt
when (val state = uiState) {
    is ReaderUiState.LoadingWithPreview -> {
        // Mostrar hero + título do summary
        // Spinner pequeno no canto
    }
    // ...
}
```

### ✅ IMPORTANT #1: Fix Parser Hero Image Caption — COMPLETADO

**Severidade:** IMPORTANT  
**Tempo estimado:** 30 minutos  
**Ficheiro:** `ArticleParser.kt`
**Status:** ✅ Implementado

**Problema:**
Captions de imagens (como "Lusa") apareciam no corpo do artigo porque o parser incluía `<p>` dentro de `<figure>`.

**Solução Implementada:**
```kotlin
val paragraphs = doc.select("article p, article div.texto-intro")
    .filter { p -> 
        // Excluir parágrafos dentro de figure/figcaption
        p.parents().none { it.tagName() in listOf("figure", "figcaption") }
    }
```

**Teste:**
- Abrir https://avante.pt/pt/2745/emdestaque/184314/...
- Verificar que "Lusa" não aparece no corpo do texto
- Verificar que outros artigos com captions também funcionam

### ✅ IMPORTANT #2: Melhorar Hero Image Detection — COMPLETADO

**Severidade:** IMPORTANT  
**Tempo estimado:** 1 hora  
**Ficheiros:** `ArticleParser.kt`, `ReaderViewModel.kt`
**Status:** ✅ Implementado

**Implementação:**
- Hero image agora segue prioridade: feed → artigo → null
- Se hero do feed for válido, usa-o
- Se não, procura na página do artigo
- Se não encontrar em nenhum lado, não mostra hero
- Elimina símbolos PCP como hero images

### ✅ IMPORTANT #3: Melhorar Partilha — COMPLETADO

**Severidade:** IMPORTANT  
**Tempo estimado:** 1 hora  
**Ficheiro:** `ArticleReaderScreen.kt`
**Status:** ✅ Implementado

**Implementação:**
- Partilha agora inclui título + summary (se disponível) + URL
- Usa `buildString` para formatação limpa
- Summary só é incluído se não estiver vazio
- Formato mais profissional e informativo

**Código implementado:**
```kotlin
val shareText = buildString {
    appendLine(article.title)
    if (article.summary.isNotBlank()) {
        appendLine()
        appendLine(article.summary)
    }
    appendLine()
    appendLine("Lê o artigo completo no Avante!:")
    append(articleUrl)
}
putExtra(Intent.EXTRA_TEXT, shareText)
```

### ✅ IMPORTANT #4: Note Status Pills com Cores — COMPLETADO

**Severidade:** IMPORTANT  
**Tempo estimado:** 1 hora  
**Ficheiro:** `CadernoScreen.kt`
**Status:** ✅ Implementado

**Implementação:**
- Cada tipo de nota agora tem uma cor distinta
- STANDALONE → Azul (#2196F3)
- ARTICLE_NOTE → Verde (MutedGreen)
- EXCERPT_NOTE → Amarelo (AnnotationYellow)
- REFLECTION → Roxo (#9C27B0)
- COMMENTARY → Vermelho (OriginalRed)
- Pills usam cor com 10% de opacidade no fundo
- Texto da pill usa a cor sólida

**Código implementado:**
```kotlin
val (pillText, pillColor) = when (note.noteType) {
    NoteType.STANDALONE -> "Livre" to Color(0xFF2196F3)
    NoteType.ARTICLE_NOTE -> "Artigo" to MutedGreen
    NoteType.EXCERPT_NOTE -> "Excerto" to AnnotationYellow
    NoteType.REFLECTION -> "Reflexão" to Color(0xFF9C27B0)
    NoteType.COMMENTARY -> "Comentário" to OriginalRed
}

Surface(
    color = pillColor.copy(alpha = 0.1f),
    shape = RoundedCornerShape(4.dp)
) {
    Text(
        text = pillText,
        style = MaterialTheme.typography.labelSmall,
        color = pillColor,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
```

### 🟡 NICE TO HAVE #1: Proveniência da Fonte

**Severidade:** NICE TO HAVE  
**Tempo estimado:** 2 horas  
**Ficheiros:** `CadernoViewModel.kt`, `CadernoScreen.kt`

**Criar data class:**
```kotlin
data class NoteWithSource(
    val note: NoteEntity,
    val sourceArticle: ArticleEntity?
)
```

**Carregar no ViewModel:**
```kotlin
fun loadNotes() {
    viewModelScope.launch {
        val notes = db.articleDao().getAllNotes()
        val notesWithSources = notes.map { note ->
            val source = db.articleDao().getNoteSource(note.noteId)
            val article = source?.let { db.articleDao().getSource(it.articleId) }
            NoteWithSource(note, article)
        }
        _uiState.value = CadernoUiState.Success(notesWithSources)
    }
}
```

**Mostrar no card:**
```kotlin
if (noteWithSource.sourceArticle != null) {
    Text(
        text = "Fonte: ${noteWithSource.sourceArticle.title}",
        style = MaterialTheme.typography.caption,
        color = TextSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
```

## Checklist para Pitch Demo

### Antes da Demo (CRÍTICO - 1 hora restante)
- [x] ✅ PARSER #1: Fix parser caption (30 min) — COMPLETADO (e9b5aa4)
- [x] ✅ Loading state com preview (2h) — COMPLETADO
- [x] ✅ Melhorar partilha com summary (1h) — COMPLETADO (8461bf0)
- [x] ✅ Pills com cores (1h) — COMPLETADO (99b6b9f)
- [ ] PARSER #2: Fix hero images em opinião (1h) — **PRÓXIMO BLOCKER**
- [ ] Testar fluxo completo 3x com artigos variados

### Artigos de Teste Obrigatórios
1. https://avante.pt/pt/2745/emdestaque/184314/... (caption "Lusa") — ✅ RESOLVIDO
2. https://avante.pt/pt/2746/opiniao/... (vários artigos de opinião) — TESTAR
3. Artigo com blockquote (encontrar exemplo)
4. Artigo normal sem problemas (baseline)

### Fluxo da Demo (4 minutos)
1. ✅ Abrir app → feed carrega
2. ✅ Tocar artigo → abre com parallax
3. ⚠️ Loading deve mostrar preview (FIX NECESSÁRIO)
4. ✅ Artigo carrega → texto legível
5. ⚠️ Caption não deve aparecer no texto (FIX NECESSÁRIO)
6. ✅ Tocar "Anotar" → banner vermelho aparece
7. ✅ Selecionar texto → menu aparece
8. ✅ Criar nota → editor abre
9. ✅ Escrever → auto-save funciona
10. ✅ Voltar → Caderno mostra nota com pill "Livre"
11. ✅ FAB → criar nova nota standalone
12. ✅ Pesquisar → encontra notas
13. ✅ Partilhar → inclui summary quando disponível

### Depois da Demo (Refactors)
- [ ] Unified search (artigos + notas + highlights)
- [ ] Settings screen
- [ ] Arquivo implementation
- [ ] Highlight → Note linking
- [ ] Add highlight to existing note
- [ ] Delete confirmation dialogs
- [ ] Canonical URL extraction
- [ ] Tags/keywords extraction
- [ ] Hero image attribution fix
- [ ] Index creation (through https://avante.pt/pt/2746/aconteceu/?tpl=183 parsing)

## Comandos Úteis

### Build e instalar
```bash
cd CivicNotebook
./gradlew clean
./gradlew installDebug
```

### Ver logs
```bash
adb logcat | grep "Parser"
adb logcat | grep "Reader"
adb logcat | grep "Caderno"
```

### Testar artigos específicos
- Pensões: https://avante.pt/pt/2745/emfoco/184316/...
- População: https://avante.pt/pt/2745/emdestaque/184314/... (tem caption "Lusa")

## Notas Técnicas

### Parser Issues Conhecidos
1. ✅ Texto duplicado → RESOLVIDO (commits 47ef010, 039a812)
2. ✅ Caption incluída no texto → RESOLVIDO (commit e9b5aa4)
3. 🟡 Hero image pode não ser a correta / não existir → PENDENTE (PARSER #2)
4. 🟢 Blockquotes não formatadas → PENDENTE (PARSER #3)
5. 🟢 Secção "Argumentos" categorizada incorretamente → PENDENTE (PARSER #4)
6. ⚠️ Keywords não extraídas → FUTURO
7. ⚠️ Canonical URL não extraído → FUTURO

### Database Schema (v3)
- `article_sources` — metadados
- `cached_article_contents` — conteúdo JSON
- `notes` — notas do utilizador
- `note_sources` — ligação nota↔artigo
- `highlights` — texto selecionado
- `highlight_comments` — comentários

### F-Droid Compatibility
✅ Sem Google Play Services  
✅ Sem Firebase  
✅ Sem SDKs proprietários  
✅ Jsoup Apache 2.0  
✅ Room/Compose Apache 2.0  

## Contacto

Se encontrares bugs ou tiveres dúvidas:
1. Verificar logs: `adb logcat`
2. Testar com artigos conhecidos
3. Verificar estado do ViewModel com breakpoints
4. Consultar auditoria completa nos commits anteriores

---

**Última atualização:** 16 Jul 2026  
**Branch:** aider-audit  
**Commits relevantes:** 47ef010, 039a812, b838b1a, 941701c, 7d9aca9, e77145e, a760b01, 8461bf0, 99b6b9f, [parser-caption-fix]
