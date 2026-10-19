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
        val updated = mutableListOf<String>()
        for (html in fixtures) {
            val doc = Jsoup.parse(html, "UTF-8")
            val blocks = AvanteBodyParser.parseBodyContainer(AvanteArticleParser.selectArticleContainer(doc))
            val actual = json.encodeToString<List<ArticleBlock>>(blocks) + "\n"
            val expected = File(fixturesDir, "${html.nameWithoutExtension}.expected.json")

            if (!expected.exists()) {
                expected.writeText(actual)
                missing += expected.name
                continue
            }
            if (updateGoldens) {
                // Em modo de atualização só se reescreve o que mudou, e o teste passa: o diff fica no git.
                if (expected.readText() != actual) {
                    expected.writeText(actual)
                    updated += expected.name
                }
                continue
            }
            assertEquals("Saída diferente para ${html.name}", expected.readText(), actual)
        }
        if (updateGoldens) {
            println("Snapshots atualizados: ${updated.ifEmpty { listOf("nenhum") }.joinToString()}")
        }
        if (missing.isNotEmpty()) {
            fail("Snapshots novos escritos (rever antes de commit): ${missing.joinToString()}")
        }
    }
}
