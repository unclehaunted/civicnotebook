package pt.avante.app.data.avante

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

/**
 * Seam: a imagem que a fase 2 do feed tira da página de um artigo.
 * A página de uma Opinião traz, depois do próprio artigo, os outros da secção em `article`
 * irmãos; a imagem de um deles não pode passar a ser a de todos (docs/avante-source.md).
 */
class ImagemDoArtigoTest {

    private fun pagina(vararg artigos: String) = Jsoup.parse(
        "<html><body><section id='grid-articles'>${artigos.joinToString("")}</section></body></html>",
        AvanteSite.BASE_URL
    )

    @Test
    fun `uma Opiniao sem imagem nao fica com a do vizinho`() {
        val doc = pagina(
            "<article><h2 class='balance-text'>O lado oculto</h2><span class='author'>G. C.</span></article>",
            "<article><img src='https://avante.pt/images/cms-image-000050697.jpg'><h3>Outro</h3></article>"
        )

        assertNull(AvanteFeedAssembler.ownArticleImage(doc))
    }

    @Test
    fun `um artigo com imagem fica com a sua`() {
        val doc = pagina(
            "<article><img src='/get_img?ImageWidth=355&ImageId=50708'><h2>Este</h2></article>",
            "<article><img src='https://avante.pt/images/cms-image-000050697.jpg'><h3>Outro</h3></article>"
        )

        assertEquals(
            "${AvanteSite.BASE_URL}/get_img?ImageWidth=355&ImageId=50708",
            AvanteFeedAssembler.ownArticleImage(doc)
        )
    }

    @Test
    fun `numa pagina real a imagem e a do artigo e nao o icone de imprimir`() {
        val fixture = File("src/test/resources/fixtures/avante/subtitulos-adere-ao-pcp.html")
        val doc = Jsoup.parse(fixture, "UTF-8", AvanteSite.BASE_URL)

        assertEquals(
            "${AvanteSite.BASE_URL}/get_img?ImageWidth=355&ImageHeight=247&ImageId=50729",
            AvanteFeedAssembler.ownArticleImage(doc)
        )
    }
}
