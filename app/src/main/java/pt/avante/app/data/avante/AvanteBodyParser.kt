package pt.avante.app.data.avante

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import pt.avante.app.data.ArticleBlock

object AvanteBodyParser {
    fun parseBodyContainer(container: Element): List<ArticleBlock> {
        val blocks = mutableListOf<ArticleBlock>()
        
        // Find the main content elements: texto-intro, and children of texto-body or directly in article
        // Jsoup's select traverses in document order.
        // We select the elements that are structurally meaningful.
        val candidates = container.select("div.texto-intro, p, blockquote, h3, h4")
        
        // Filter out those that are inside other selected candidates to avoid duplicate parsing
        // (e.g. <div class="texto-intro"><p>...</p></div>)
        val validElements = candidates.filter { el ->
            !candidates.any { parent -> parent != el && el.parents().contains(parent) } &&
            !el.parents().any { it.tagName() in listOf("figure", "figcaption") }
        }
        
        for (el in validElements) {
            val text = el.text().trim()
            if (text.isBlank()) continue
            
            if (text.contains("O artigo completo está disponível") || text.contains("Compra o «Avante!»")) {
                blocks.add(ArticleBlock.PaywallCTA)
                break
            }
            if (text.contains("Já é assinante ou comprou")) {
                continue
            }
            
            if (el.tagName().equals("blockquote", ignoreCase = true)) {
                blocks.add(ArticleBlock.Quote(text))
                continue
            }
            
            splitInterviewQuestion(el)?.let { (question, answer) ->
                blocks.add(ArticleBlock.Question(question))
                blocks.add(ArticleBlock.Text(answer))
                continue
            }

            val strongOnly =
                el.children().size == 1 &&
                el.children().firstOrNull()?.tagName()?.equals("strong", ignoreCase = true) == true &&
                el.ownText().trim().isBlank()

            // Pergunta de entrevista sozinha num parágrafo a negrito. Até 100 caracteres mantém-se
            // Subheading (um título de secção pode acabar em "?"); acima disso passaria a Text.
            if (strongOnly && text.length > MAX_SUBHEADING_LENGTH && text.endsWith("?")) {
                blocks.add(ArticleBlock.Question(text))
                continue
            }

            val onlyStrong = strongOnly && text.length <= MAX_SUBHEADING_LENGTH

            if (onlyStrong || el.tagName().matches(Regex("h[1-6]", RegexOption.IGNORE_CASE))) {
                blocks.add(ArticleBlock.Subheading(text))
                continue
            }

            blocks.add(ArticleBlock.Text(text))
        }
        
        return blocks
    }

    private const val MAX_SUBHEADING_LENGTH = 100

    /**
     * Padrão A de entrevista: `<p><strong>Pergunta?<br/></strong>Resposta…</p>`.
     * O primeiro filho é um `strong` terminado em `br` (dentro dele ou logo a seguir) e há texto depois.
     * Devolve (pergunta, resposta) ou null se o parágrafo não tiver esta forma.
     */
    private fun splitInterviewQuestion(p: Element): Pair<String, String>? {
        if (!p.tagName().equals("p", ignoreCase = true)) return null
        val first = p.childNodes().firstOrNull { it !is TextNode || !it.isBlank } as? Element ?: return null
        if (!first.tagName().equals("strong", ignoreCase = true)) return null

        val lastInside = first.childNodes().lastOrNull { it !is TextNode || !it.isBlank }
        val brInside = (lastInside as? Element)?.tagName()?.equals("br", ignoreCase = true) == true
        val nextSibling = first.nextSibling()
            ?.let { n -> generateSequence(n) { it.nextSibling() }.firstOrNull { it !is TextNode || !it.isBlank } }
        val brAfter = (nextSibling as? Element)?.tagName()?.equals("br", ignoreCase = true) == true
        if (!brInside && !brAfter) return null

        val question = first.text().trim()
        if (question.isBlank()) return null

        val rest = p.clone()
        rest.children().first()?.remove() // o strong
        if (brAfter) rest.children().firstOrNull()?.remove() // o br
        val answer = rest.text().trim()
        if (answer.isBlank()) return null

        return question to answer
    }
}
