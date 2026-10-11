# CivicNotebook — Leitor do Avante! com caderno de notas

Aplicação Android (Kotlin + Jetpack Compose) para ler o jornal **Avante!** — Órgão
Central do Partido Comunista Português — e trabalhar sobre ele: destacar passagens,
anotar e organizar notas de leitura.

## Funcionalidades

- **Edição semanal** — feed da edição corrente do avante.pt, com secções, indicação
  de artigos livres/exclusivos e layouts adaptativos (telemóvel, tablet, landscape).
- **Arquivo completo com a palavra-passe semanal** — a palavra-passe publicada na
  edição impressa (ou na compra do PDF) desbloqueia a edição digital e o arquivo
  recente dentro da app, com re-login silencioso.
- **Caderno** — notas próprias (livres, de artigo, de excerto, reflexões) e
  destaques de passagens, com modo de anotação dedicado.
- **Definições de leitura** — tipo de letra (incl. Atkinson Hyperlegible), tamanho,
  cor de fundo (Papel, Branco, Sépia, Escuro) e toggle de imagens.
- **Pesquisa** — sobre notas e excertos do utilizador.

## Arquitetura em resumo

```
Fetching (HTTP) → Parsing (adaptador de fonte Avante) → Modelo de publicação → UI
```

- A camada de parsing (`data/avante/`) é um adaptador de fonte substituível: preenche
  um modelo de publicação estável (edição, secção, artigo, estado de acesso).
- O caderno (notas/destaques) é o objeto durável do utilizador; artigos são fontes
  externas. Persistência local com Room.
- Autenticação: `data/auth/` — POST a `avante.pt/auth` com a credencial semanal,
  cookies de sessão em `EncryptedSharedPreferences`, fetching centralizado em
  `AvanteHttp` com re-login silencioso.

## Build

```bash
./gradlew installDebug
```

Requer Android SDK, JDK 17, minSdk 24. Sem Google Play Services, sem Firebase —
compatível com F-Droid (Jsoup, Room, Compose: Apache 2.0; fontes sob SIL OFL).
