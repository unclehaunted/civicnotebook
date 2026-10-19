package pt.avante.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArticleBlockSelectableTextTest {

    @Test
    fun textQuestionAndQuote_exposeTheirContent() {
        assertEquals("a", ArticleBlock.Text("a").selectableText)
        assertEquals("b", ArticleBlock.Question("b").selectableText)
        assertEquals("c", ArticleBlock.Quote("c").selectableText)
    }

    @Test
    fun otherBlocks_areNotSelectable() {
        assertNull(ArticleBlock.Subheading("s").selectableText)
        assertNull(ArticleBlock.Image("u", "c").selectableText)
        assertNull(ArticleBlock.PaywallCTA.selectableText)
    }
}
