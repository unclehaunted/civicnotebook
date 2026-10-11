# CivicNotebook — Leitor do Avante! com caderno de notas

Aplicação Android para ler o jornal **Avante!** — Órgão Central do Partido
Comunista Português — e trabalhar sobre ele: destacar passagens, anotar e
organizar notas de leitura.

## O que a app faz

O avante.pt não expõe uma API pública para aplicações. A app faz uma **leitura
educada dos templates online do jornal** — o índice estruturado da edição, a
página de destaque e as páginas de artigo — pedidos HTTP pontuais e
espaçados, como os de um leitor no browser, sem sobrecarregar o site.

Dessa leitura **deduz as categorias de informação** próprias do jornal
(edição, secção editorial, título, autoria, introdução, corpo, estado de
acesso livre/exclusivo) e traduz-las para um modelo de publicação estável,
independente dos detalhes do HTML. A app apresenta depois essa informação
**de forma utilizável em ecrãs pequenos**: feed da edição com secções,
leitura de artigo confortável e arquivo.

O acesso a conteúdo exclusivo usa a **palavra-passe semanal publicada na
edição impressa** (ou na compra do PDF) — exatamente o mesmo acesso que o
site concede, sem contornos.

## Funcionalidades atuais

- **Edição semanal** — feed da edição corrente, com secções, indicação de
  artigos livres/exclusivos e layouts adaptativos (telemóvel, tablet,
  landscape).
- **Arquivo com a palavra-passe semanal** — desbloqueia a edição digital e o
  arquivo recente dentro da app, com re-login silencioso.
- **Caderno** — notas próprias (livres, de artigo, de excerto, reflexões) e
  destaques de passagens, com modo de anotação dedicado.
- **Definições de leitura** — tipo de letra (incl. Atkinson Hyperlegible),
  tamanho, cor de fundo (Papel, Branco, Sépia, Escuro) e toggle de imagens.
- **Pesquisa** — sobre notas e excertos do utilizador.

## Autoria e método

O comportamento da app, o layout e o design de interface e de experiência
foram **concebidos por Nuno Castro** ([@unclehaunted](https://github.com/unclehaunted)).
O código é implementado por **interfaces LLM (código generativo)**,
trabalhando sobre esse desenho — ver o histórico de commits.

## Arquitetura em resumo

```
Fetching (HTTP educado) → Parsing (templates online) → Categorias deduzidas → Modelo de publicação → UI
```

- A camada de parsing (`data/avante/`) é um **adaptador de fonte
  substituível**: preenche um modelo de publicação estável (edição, secção,
  artigo, estado de acesso). Uma futura API oficial do jornal pode preencher
  o mesmo modelo sem tocar na app.
- O caderno (notas/destaques) é o objeto durável do utilizador; artigos são
  fontes externas. Persistência local com Room.
- Autenticação: `data/auth/` — credencial semanal, cookies de sessão em
  `EncryptedSharedPreferences`, fetching centralizado com re-login silencioso.

## Build

```bash
./gradlew installDebug
```

Requer Android SDK, JDK 17, minSdk 24. Sem Google Play Services, sem
Firebase — compatível com F-Droid (Jsoup MIT; Room/Compose Apache 2.0;
fontes sob SIL OFL).

## Licença

[GNU GPLv3](LICENSE) — ver o ficheiro `LICENSE`.
