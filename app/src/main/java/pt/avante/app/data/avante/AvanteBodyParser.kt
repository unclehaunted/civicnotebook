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
            
            val onlyStrong =
                el.children().size == 1 &&
                el.children().firstOrNull()?.tagName()?.equals("strong", ignoreCase = true) == true &&
                el.ownText().trim().isBlank() &&
                text.length <= 100
                
            if (onlyStrong || el.tagName().matches(Regex("h[1-6]", RegexOption.IGNORE_CASE))) {
                blocks.add(ArticleBlock.Subheading(text))
                continue
            }
            
            blocks.add(ArticleBlock.Text(text))
        }
        
        return blocks
    }
}
