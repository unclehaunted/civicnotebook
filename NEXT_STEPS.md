# CivicNotebook — Próximos Passos da Auditoria

## Doutrina de Arquitetura Parser/Source-Layer

### Princípio Fundamental

**O parser não é o produto. O caderno é o produto.**

Esta aplicação é um caderno de leitura cívica/política. Artigos são fontes externas; destaques, comentários e notas são objetos do utilizador.

O parser é atualmente importante porque o Avante.pt não expõe uma API limpa para aplicações. No entanto, o parser deve ser tratado como um **adaptador de fonte temporário**, não como o núcleo do produto.

### Separação de Responsabilidades

```text
Fetching (HTTP/XML)
    ↓
Parsing/Source Adaptation (Avante HTML → modelo estável)
    ↓
Publication Model (Issue, Section, Article, Access Status)
    ↓
App Domain (UI, Caderno, Notes, Highlights, Search, Archive)
```

**Regra de ouro:**
```text
O parser preenche o modelo de publicação.
O modelo de publicação alimenta a app.
O parser é substituível.
O caderno é durável.
```

### Modelo de Publicação Estável

O parser deve produzir objetos que representam a organização jornalística/editorial do jornal:

- **Issue** (edição/número)
- **Issue Index** (inventário canónico de artigos)
- **Section/Category** (secção editorial)
- **Article Summary** (título, autor, intro, imagem, URL, estado de acesso)
- **Article Full Content** (corpo completo, blocos estruturados)
- **Access Status** (FREE, SUBSCRIBER_OR_PRINT, UNKNOWN)
- **Author** (autoria)
- **Intro/Lead** (parágrafo de abertura)
- **Body Blocks** (texto, imagem, citação, subtítulo)
- **Canonical URL** (URL fonte)
- **Images** (com atribuição)
- **Source Provenance** (de onde veio o conteúdo)
- **Parser Warnings** (diagnósticos, problemas detetados)

### Princípio de Substituição Futura

O parser deve ser modular o suficiente para que uma futura integração API/backoffice possa preencher o mesmo modelo sem reescrever a app.

**Fontes futuras possíveis:**
```text
AvanteHtmlSource        parser HTML atual
AvanteApiSource         futura API oficial/backoffice
OMilitanteHtmlSource    futuro parser para O Militante
StaticFixtureSource     fixtures de teste/dev
```

Todas devem produzir o mesmo tipo de dados de publicação.

### Inventário Canónico: Índice tpl=183

O Índice estruturado é especialmente importante porque expressa a estrutura editorial real do jornal:

```text
https://www.avante.pt/pt/{issue}/?tpl=183
```

**O que o Índice contém:**
- Inventário completo de artigos da edição
- Secções claramente identificadas (`div.seccao`)
- Marcadores de acesso livre ("Artigo de acesso livre")
- Títulos, intros, imagens locais
- Estrutura em ordem de documento

**Estado atual:**
O parser já usa o Índice tpl=183 para detetar artigos livres (`fetchFreeArticlesFromIndex()`). A próxima tarefa é validar, modularizar e testar esta funcionalidade dentro de uma estrutura de adaptador de fonte substituível.

A página visual de destaque (`/pt/{issue}/`) é importante para destaques, mas **não é o inventário completo**.

## Estado Atual (16 Jul 2026)

### ✅ Completado Recentemente

6. **Parser usa índice para detectar artigos livres** (commit [index-parser])
   - Parser consulta `https://avante.pt/pt/{editionNumber}/?tpl=183`
   - Deteta artigos marcados como "Artigo de acesso livre"
   - Indicador vermelho (bullet) para artigos pagos no canto inferior direito
   - Cards compactos só para secções "Opinião" e "Actual"
   - Pill de autor reduzida e mais próxima do botão "Ler artigo"
   - **Nota:** Esta funcionalidade precisa de ser validada, modularizada e testada na próxima auditoria

5. **Masthead redesenhado** (commit f2c3581, 61bbc29, 0386a9d, 7f34b50, 6408ae7)
   - Subtítulo agora mostra "Órgão Central do Partido Comunista Português"
   - Número da edição movido para badge com ícone de calendário
   - Ponto de exclamação do logo corrigido para vermelho
   - Header compactado com badge e ícones na mesma linha

### ✅ Completado Anteriormente
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

### 🎯 Descoberta Importante: Índice da Edição

**URL do Índice:** `https://avante.pt/pt/{editionNumber}/?tpl=183`

O índice contém informação estruturada sobre todos os artigos da edição:
- **Artigos livres** marcados com `<h4>Artigo de acesso livre</h4>`
- **Secções** claramente identificadas (Editorial, Opinião, Em Destaque, etc.)
- **Tags** de subsecção (ex: "A talhe de foice", "PCP", "Crónica Internacional")
- **Estrutura completa** da edição

**Implementação:**
- Parser agora consulta o índice para determinar quais artigos são livres
- `ArticleSummary` tem novo campo `isFree: Boolean`
- UI mostra indicador vermelho (bullet point) para artigos pagos
- Cards compactos só para secções "Opinião" e "Actual"

### ⚠️ Problemas Identificados mas Não Resolvidos

1. **Parser: Blockquotes não formatadas**
   - Alguns artigos possuem `<blockquote>` destacada a cinzento e itálico
   - App deve tratá-las com formatação especial
   - **Severidade:** NICE TO HAVE
   - **Tempo estimado:** 1 hora

2. **UI: Secções especiais em falta**
   - Grelha Sugestões (https://avante.pt/pt/2746/sugestoes/) no final da View Edição
   - Grelha Aconteceu (https://avante.pt/pt/2746/aconteceu/) com card na main view
   - **Severidade:** NICE TO HAVE
   - **Tempo estimado:** 5 horas total

3. **UI: Ícones incorretos**
   - Ícone da view Edição deve ser um jornal
   - Ícone de caderno deve ser um bloco de notas
   - **Severidade:** NICE TO HAVE
   - **Tempo estimado:** 30 minutos

4. **Proveniência da fonte no Caderno**
   - NoteCard não mostra de que artigo veio a nota
   - Requer: `NoteWithSource` data class + query adicional
   - **Severidade:** NICE TO HAVE
   - **Tempo estimado:** 2 horas

## Próxima Tarefa Imediata: Auditoria de Arquitetura Parser/Source-Layer

### Objetivo

Auditar a arquitetura atual de parser/source e propor o menor refactor seguro que mova o parsing HTML específico do Avante para uma estrutura de adaptador de fonte modular e substituível.

### Âmbito

**Parser/source layer apenas.**

**NÃO MUDAR:**
- UI
- Navegação
- Caderno/notas
- Schema de base de dados (a menos que claramente necessário e explicitamente justificado)
- Styling
- Funcionalidades de anotação

### Antes de Editar, Identificar

1. Ficheiros/funções atuais do parser
2. Modelos de dados produzidos pelo parsing
3. Onde acontece o fetching (incluindo `fetchFreeArticlesFromIndex()`)
4. Onde acontece a normalização de URLs
5. Onde são criados resumos de artigos e corpos de artigos
6. Onde o output do parser entra na UI/repositório
7. O que tornaria a substituição futura do parser dolorosa
8. Como a funcionalidade tpl=183 existente se encaixa na arquitetura
9. O menor passo de modularização seguro

### Direção Esperada

- Isolar funções de parsing específicas do Avante
- Separar parsing de índice de edição do parsing de corpo de artigo
- Separar helpers de normalização de URL/imagem/acesso
- Fazer output do parser assemelhar-se a objetos de publicação estáveis
- Evitar vazamento de seletores para código UI/repositório
- Preservar comportamento existente da app enquanto se prepara para melhor substituição do parser

### Política de Reescrita

Uma reescrita completa da camada parser/source é permitida se o refactor incremental produzir piores resultados a longo prazo.

**Se recomendar uma reescrita, deve fornecer:**
1. Porquê o refactor incremental é insuficiente
2. Que partes devem ser reescritas
3. Que partes devem ser preservadas
4. Benefícios esperados
5. Riscos
6. Caminho de migração
7. Estratégia de teste/fixture
8. Plano de rollback
9. Menor passo de prova de conceito de reescrita

**Distinguir entre:**
- Reescrita completa da app
- Reescrita da camada parser
- Reescrita apenas do ArticleParser
- Extração de módulo

### Critérios de Aceitação

Após a auditoria:
1. Explicar o menor passo de implementação seguro
2. Se seguro, implementar apenas esse passo
3. Listar ficheiros alterados
4. Parar

### Prompt Pronto para Aider

```text
Temos um estudo do parser Avante e uma decisão de produto:

A app é um caderno de leitura cívica/política. Artigos são fontes externas; notas/destaques são objetos do utilizador. O parser é atualmente importante porque o Avante.pt não tem uma API limpa para apps, mas o parser deve ser modular, substituível e extraível.

Tarefa:
Auditar a arquitetura atual de parser/source e propor o menor refactor seguro que mova o parsing HTML específico do Avante para uma estrutura de adaptador de fonte modular e substituível.

Importante:
- Não enquadrar isto como meramente corrigir seletores.
- O parser deve preencher um modelo de publicação estável: issue, section/category, article summary, article full content, access status, author, intro, body blocks, canonical URL, images, source provenance, parser warnings.
- A UI, Caderno, notas, destaques, pesquisa e arquivo devem consumir dados de publicação estáveis, não detalhes HTML do Avante.
- Uma futura fonte API/backoffice/RSS/export do Avante, ou uma fonte O Militante, deve poder preencher o mesmo modelo mais tarde.

Antes de editar:
1. identificar ficheiros/funções atuais do parser;
2. identificar modelos de dados produzidos pelo parsing;
3. identificar onde acontece o fetching;
4. identificar onde acontece a normalização de URLs;
5. identificar onde são criados resumos de artigos e corpos de artigos;
6. identificar onde o output do parser entra na UI/repositório;
7. identificar o que tornaria a substituição futura do parser dolorosa;
8. propor o menor passo de modularização seguro.

Âmbito:
Camada parser/source apenas.
Sem mudanças de UI.
Sem mudanças de Caderno/notas/navegação/styling.
Sem reescrita ampla sem justificação extrema.

Política de Reescrita:
Uma reescrita completa da camada parser/source é permitida se o refactor incremental produzir piores resultados a longo prazo.
Se recomendar uma reescrita, deve fornecer:
1. porquê o refactor incremental é insuficiente;
2. que partes devem ser reescritas;
3. que partes devem ser preservadas;
4. benefícios esperados;
5. riscos;
6. caminho de migração;
7. estratégia de teste/fixture;
8. plano de rollback;
9. menor passo de prova de conceito de reescrita.
Distinguir entre reescrita completa da app, reescrita da camada parser, reescrita apenas do ArticleParser, ou extração de módulo.

Sem mudanças de base de dados/schema a menos que claramente necessário e explicitamente justificado.

Direção esperada:
- isolar funções de parsing específicas do Avante;
- separar parsing de índice de edição do parsing de corpo de artigo;
- separar helpers de normalização de URL/imagem/acesso;
- evitar vazamento de seletores para código UI/repositório;
- preservar comportamento existente.

Após a auditoria:
- explicar o menor passo de implementação seguro;
- se seguro, implementar apenas esse passo;
- listar ficheiros alterados;
- parar.
```

## Tarefas Encenadas Posteriores

### Tarefa 2: Validar, Modularizar e Testar Parsing do Índice tpl=183

**Âmbito:** Camada parser/source apenas.

**Contexto:**
O parser já usa `fetchFreeArticlesFromIndex()` para consultar `/pt/{issue}/?tpl=183` e detetar artigos livres. Esta tarefa visa validar, isolar e testar esta funcionalidade dentro de uma estrutura de adaptador de fonte substituível.

**Objetivos:**
1. Validar parsing existente de `/pt/{issue}/?tpl=183`
2. Isolar lógica de parsing do Índice em função/módulo separado
3. Extrair inventário completo de artigos do Índice (não apenas marcadores de acesso)
4. Percorrer conteúdo do índice em ordem e rastrear `div.seccao` como secção atual
5. Extrair links de artigos de âncoras `h2`/`h3` terminando em `.htm`
6. Não depender apenas de wrappers `<article>`
7. Extrair título local, URL, secção, intro, imagem e marcador de acesso onde disponível
8. Desduplicar por URL canónico normalizado
9. Ignorar imagens placeholder/logo
10. Retornar valores de modelo de publicação estáveis, não valores específicos de UI
11. Adicionar testes baseados em fixtures

### Tarefa 3: Melhorar Parsing de Corpo de Artigo Individual

**Âmbito:** Camada parser/source apenas.

**Objetivos:**
1. Extrair corpo apenas de `section.main.entry article`
2. Extrair título, autor, intro, citação e corpo completo
3. Detetar artigos bloqueados/preview usando mensagem de subscrição
4. Evitar parsing de grelhas de artigos relacionados como corpo
5. Preservar proveniência de fonte e avisos do parser

### Tarefa 4: Adicionar Avisos/Diagnósticos de Debug do Parser

**Âmbito:** Diagnósticos parser/modelo apenas.

**Objetivos:**
1. Adicionar avisos para título/corpo/imagem/estado de acesso em falta
2. Registar imagens placeholder ignoradas
3. Registar merges de URLs duplicados
4. Registar tipo de página desconhecido
5. Registar estado preview/paywall

### Tarefa 5: Preservar Metadados do Índice ao Abrir Artigo

**Âmbito:** Comportamento de merge repositório/parser apenas.

**Objetivos:**
1. Não sobrescrever boas imagens do índice com imagens nulas do artigo
2. Fazer merge cuidadoso do estado de acesso
3. Preservar secção/categoria do índice a menos que página do artigo forneça dados melhores
4. Preservar proveniência de fonte

### Tarefa 6: Criar Testes de Parser Baseados em Fixtures

**Âmbito:** Testes apenas.

**Objetivos:**
1. Adicionar fixtures HTML locais se o repo suportar testes
2. Testar contagem de inventário do índice acima de limiar esperado
3. Testar remoção de URLs duplicados
4. Testar deteção de marcador livre
5. Testar deteção de preview de artigo bloqueado
6. Testar rejeição de imagem placeholder
7. Testar exclusão de grelha relacionada

## Não-Objetivos Explícitos

**NÃO fazer sem justificação explícita:**

1. **Reescrita ampla da app** — A app funciona. O parser precisa de modularização, não a app inteira.
2. **Mudanças de UI** — A UI consome dados de publicação. Se o parser produzir dados estáveis, a UI não precisa de mudar.
3. **Mudanças de Caderno/notas** — O Caderno é o produto durável. Não tocar a menos que absolutamente necessário.
4. **Mudanças de navegação** — A navegação funciona.
5. **Mudanças de styling** — O styling funciona.
6. **Mudanças de tema/masthead/branding** — O branding funciona. Modularização multi-publicação é uma tarefa futura separada.
7. **Mudanças de schema de base de dados** — O schema atual suporta o modelo de publicação. Só mudar se claramente necessário e explicitamente justificado.
8. **Corrigir apenas seletores** — Isto não é apenas sobre seletores quebrados. É sobre arquitetura de fonte substituível.

## Trabalho Completado Recentemente

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

### 🟢 FUTURE #1: Sistema de Configuração Multi-Publicação

**Severidade:** NICE TO HAVE (FUTURO - FORA DO ÂMBITO DA AUDITORIA PARSER/SOURCE)  
**Tempo estimado:** 8 horas  
**Ficheiros:** Novos ficheiros de configuração, refactor de componentes UI/tema

**⚠️ IMPORTANTE:** Esta tarefa está FORA DO ÂMBITO da próxima auditoria parser/source-layer. Não deve ser implementada durante a auditoria. É uma visão futura para depois da camada parser estar modularizada.

**Objetivo:**
Tornar a app configurável para servir múltiplas publicações (Avante!, O Militante, outras publicações do PCP ou parceiras).

**Conceito de alto nível:**

A app deve suportar múltiplas publicações através de:
- Interface `PublicationSource` com implementações específicas (AvanteSource, MilitanteSource)
- Configuração de branding por publicação (cores, logos, masthead)
- Parser strategy pattern para diferentes estruturas HTML
- Ecrã de definições para escolher publicação ativa

**Componentes conceptuais:**
- `PublicationConfig` — configuração de branding e URLs base
- `PublicationSource` interface — abstração de fonte de dados
- `AvanteSource`, `MilitanteSource` — implementações específicas
- Theme configuration por publicação
- Settings para escolher publicação

**Benefícios:**
- Suporte para múltiplas publicações sem duplicar código
- Fácil adicionar novas publicações
- Branding consistente por publicação
- Parser específico por publicação (estruturas HTML diferentes)

**Pré-requisitos:**
- ✅ Camada parser/source modularizada e testada
- ✅ Interface `PublicationSource` definida e implementada para Avante
- ✅ Modelo de publicação estável validado

---

### 🟡 NICE TO HAVE #2: Proveniência da Fonte

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

## Problemas Conhecidos do Parser (Para Referência)

### ✅ Resolvidos
1. ✅ Texto duplicado → RESOLVIDO (commits 47ef010, 039a812)
2. ✅ Caption incluída no texto → RESOLVIDO (commit e9b5aa4)
3. ✅ Hero image melhorada → RESOLVIDO (commit 17243a7)
4. ✅ Partilha melhorada → RESOLVIDO (commit 8461bf0)

### 🟡 Pendentes (Após Auditoria)
1. 🟡 Hero image pode não ser a correta em alguns casos
2. 🟡 Blockquotes não formatadas
3. 🟡 Secção "Argumentos" pode ser categorizada incorretamente
4. 🟡 Secção "Memoria" pode não aparecer
5. 🟡 Imagens `/get_img?ImageWidth=...` podem não ser convertidas corretamente

### ⚠️ Futuro
1. ⚠️ Keywords não extraídas
2. ⚠️ Canonical URL não extraído
3. ⚠️ Atribuição de imagem não extraída
4. ⚠️ Avisos do parser não implementados

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

## Referências de Arquitetura

### Mapa de Tipos de Página Avante Atual

```json
[
  {
    "type": "RSS_CURRENT",
    "url_pattern": "/pt/?tpl=181",
    "purpose": "RSS feed/sinal de edição atual e descoberta de secções",
    "confidence": "high"
  },
  {
    "type": "EDITION_FRONT",
    "url_pattern": "/pt/2746/",
    "purpose": "página visual de destaque da edição/destaques, propensa a duplicados",
    "confidence": "high"
  },
  {
    "type": "ISSUE_INDEX",
    "url_pattern": "/pt/2746/?tpl=183",
    "purpose": "candidato a inventário canónico de edição",
    "confidence": "high"
  },
  {
    "type": "ARTICLE_SINGLE",
    "url_pattern": "/pt/2746/{section}/{articleId}/{slug}.htm",
    "purpose": "parsing de corpo/preview de artigo individual",
    "confidence": "high"
  },
  {
    "type": "OPINIAO_FRONT",
    "url_pattern": "/pt/2746/opiniao/",
    "purpose": "secção/destaque especial Opinião, provavelmente template personalizado",
    "confidence": "high"
  },
  {
    "type": "MEMORIA_FRONT",
    "url_pattern": "/pt/2746/memoria/",
    "purpose": "secção/destaque especial Memória, provavelmente template personalizado",
    "confidence": "high"
  }
]
```

### Regras Atuais do Parser (Do Estudo)

```json
{
  "canonical_inventory_source": "https://www.avante.pt/pt/2746/?tpl=183",
  "article_url_rule": {
    "pattern_description": "URLs sob /pt/2746/ terminando em .htm",
    "dedupe_key": "URL canónico normalizado"
  },
  "index_rules": {
    "root_candidates": ["#grid-indice"],
    "section_candidates": ["div.seccao"],
    "entry_link_candidates": [
      "h2 a[href$='.htm']",
      "h3 a[href$='.htm']",
      "qualquer âncora de heading terminando em .htm ao percorrer índice"
    ],
    "warning": "Não depender apenas de <article>; algumas entradas podem ser blocos soltos de heading/intro."
  },
  "article_body_rules": {
    "root": "section.main.entry article",
    "title": "h2.balance-text",
    "author": "span.author",
    "intro": "div.texto-intro",
    "body": "div.texto > p",
    "quote": "blockquote",
    "exclude": [
      "section#grid-articles",
      "grelhas de artigos relacionados",
      "footer/nav/sidebar"
    ]
  },
  "access_rules": {
    "free_marker": "Artigo de acesso livre",
    "locked_marker": "O artigo completo está disponível na edição impressa ou por assinatura on-line",
    "states": ["FREE", "SUBSCRIBER_OR_PRINT", "UNKNOWN"]
  },
  "image_rules": {
    "good_patterns": [
      "/images/cms-image-",
      "/get_img?ImageId=",
      "get_img?ImageId="
    ],
    "ignore_patterns": [
      "/themes/publication_2/theme_2/img/",
      "avante-simbolo_foice_fundo_branco.png",
      "pcp22px.png",
      "omilitante22px.png",
      "festa_avante",
      "avante_clandestino",
      "avante.gif"
    ],
    "principle": "Extrair do contentor local de artigo/card; nunca primeira imagem global."
  },
  "warnings": [
    "UNKNOWN_PAGE_TYPE",
    "NO_MAIN_ARTICLE_ROOT",
    "NO_TITLE",
    "NO_BODY_TEXT",
    "PREVIEW_ONLY",
    "PAYWALL_MARKER_FOUND",
    "DUPLICATE_URL_MERGED",
    "PLACEHOLDER_IMAGE_IGNORED",
    "RELATED_GRID_SKIPPED",
    "OG_IMAGE_MISSING"
  ]
}
```

## Notas Técnicas

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
**Commits relevantes:** 47ef010, 039a812, b838b1a, 941701c, 7d9aca9, e77145e, a760b01, 8461bf0, 99b6b9f, e9b5aa4, 17243a7, f2c3581, 61bbc29, 0386a9d, 7f34b50, 6408ae7, [index-parser]

**Nota sobre Modularidade:** A app está a ser preparada para servir múltiplas publicações. Ver secção "Arquitetura Modular para Múltiplas Publicações" no topo deste documento.

**Nota sobre Índice:** O parser agora usa o índice estruturado (`/?tpl=183`) para obter informação precisa sobre artigos livres vs pagos e estrutura da edição.
