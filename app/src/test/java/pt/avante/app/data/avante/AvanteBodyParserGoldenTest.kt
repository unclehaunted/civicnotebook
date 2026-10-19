package pt.avante.app.data.avante

import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import pt.avante.app.data.ArticleBlock

/**
 * Fixa a saída atual do parser sobre páginas reais guardadas em
 * src/test/resources/fixtures/avante/<slug>.html.
 *
 * Para (re)gerar os snapshots: ./gradlew testDebugUnitTest -DupdateGoldens=true
 * e rever o diff dos .expected.json antes de fazer commit.
 */
class AvanteBodyParserGoldenTest {

    private val json = Json {
        prettyPrint = true
        classDiscriminator = "type"
    }

    private val fixturesDir = File("src/test/resources/fixtures/avante")
    private val updateGoldens = System.getProperty("updateGoldens") == "true"

    @Test
    fun bodyBlocksMatchGoldenSnapshots() {
        val fixtures = fixturesDir.listFiles { f -> f.extension == "html" }.orEmpty().sortedBy { it.name }
        assertTrue("Sem fixtures em ${fixturesDir.absolutePath}", fixtures.isNotEmpty())

        val missing = mutableListOf<String>()
        for (html in fixtures) {
            val doc = Jsoup.parse(html, "UTF-8")
            val blocks = AvanteBodyParser.parseBodyContainer(AvanteArticleParser.selectArticleContainer(doc))
            val actual = json.encodeToString<List<ArticleBlock>>(blocks) + "\n"
            val expected = File(fixturesDir, "${html.nameWithoutExtension}.expected.json")

            if (updateGoldens || !expected.exists()) {
                expected.writeText(actual)
                missing += expected.name
                continue
            }
            assertEquals("Saída diferente para ${html.name}", expected.readText(), actual)
        }
        if (missing.isNotEmpty()) {
            fail("Snapshots escritos (rever antes de commit): ${missing.joinToString()}")
        }
    }
}
