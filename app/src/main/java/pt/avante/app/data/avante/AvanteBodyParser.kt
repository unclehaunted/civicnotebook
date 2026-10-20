package pt.avante.app.data.avante

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import pt.avante.app.data.ArticleBlock

object AvanteBodyParser {
    private const val MAX_SUBHEADING_LENGTH = 100

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
            
            if (text.contains(AvanteSite.PAYWALL_MARKER) || text.contains("Compra o «Avante!»")) {
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
            
            splitInterviewQuestion(el)?.let { exchange ->
                blocks.add(ArticleBlock.Question(exchange.question))
                blocks.add(ArticleBlock.Text(exchange.answer))
                continue
            }

            val isBoldOnly =
                el.children().size == 1 &&
                el.children().firstOrNull()?.tagName()?.equals("strong", ignoreCase = true) == true &&
                el.ownText().trim().isBlank()

            // Pergunta de entrevista sozinha num parágrafo a negrito. Até 100 caracteres mantém-se
            // Subheading (um título de secção pode acabar em "?"); acima disso passaria a Text.
            if (isBoldOnly && text.length > MAX_SUBHEADING_LENGTH && text.endsWith("?")) {
                blocks.add(ArticleBlock.Question(text))
                continue
            }

            val isShortBoldOnly = isBoldOnly && text.length <= MAX_SUBHEADING_LENGTH

            if (isShortBoldOnly || el.tagName().matches(Regex("h[1-6]", RegexOption.IGNORE_CASE))) {
                blocks.add(ArticleBlock.Subheading(text))
                continue
            }

            blocks.add(ArticleBlock.Text(text))
        }
        
        return blocks
    }

    /** Pergunta e resposta separadas de um parágrafo de entrevista. */
    private data class InterviewExchange(val question: String, val answer: String)

    /** O `strong` inicial de um parágrafo e se o `br` que o fecha está fora dele (logo a seguir). */
    private class BoldLineBreakLead(val strong: Element, val lineBreakAfterStrong: Boolean)

    /**
     * Padrão A de entrevista: `<p><strong>Pergunta?<br/></strong>Resposta…</p>`.
     * Devolve a pergunta e a resposta, ou null se o parágrafo não tiver esta forma ou se o negrito
     * não terminar em "?".
     */
    private fun splitInterviewQuestion(p: Element): InterviewExchange? {
        val lead = boldLineBreakLead(p) ?: return null

        val question = lead.strong.text().trim()
        if (!question.endsWith("?")) return null // um negrito sem "?" (ex.: dateline) não é pergunta

        val rest = p.clone()
        rest.children().first()?.remove() // o strong
        if (lead.lineBreakAfterStrong) rest.children().firstOrNull()?.remove() // o br
        val answer = rest.text().trim()
        if (answer.isBlank()) return null

        return InterviewExchange(question, answer)
    }

    /**
     * Deteta um parágrafo que começa por um `strong` terminado em `br` (dentro dele ou logo a seguir).
     * Null se o primeiro filho não for esse `strong` ou se não houver `br`.
     */
    private fun boldLineBreakLead(p: Element): BoldLineBreakLead? {
        if (!p.tagName().equals("p", ignoreCase = true)) return null
        val first = p.childNodes().firstOrNull { it !is TextNode || !it.isBlank } as? Element ?: return null
        if (!first.tagName().equals("strong", ignoreCase = true)) return null

        val lastInside = first.childNodes().lastOrNull { it !is TextNode || !it.isBlank }
        val lineBreakInside = (lastInside as? Element)?.tagName()?.equals("br", ignoreCase = true) == true
        val nextSibling = first.nextSibling()
            ?.let { n -> generateSequence(n) { it.nextSibling() }.firstOrNull { it !is TextNode || !it.isBlank } }
        val lineBreakAfter = (nextSibling as? Element)?.tagName()?.equals("br", ignoreCase = true) == true
        if (!lineBreakInside && !lineBreakAfter) return null

        return BoldLineBreakLead(first, lineBreakAfter)
    }
}
