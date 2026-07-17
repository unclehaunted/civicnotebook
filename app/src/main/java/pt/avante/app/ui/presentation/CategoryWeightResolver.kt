package pt.avante.app.ui.presentation

object CategoryWeightResolver {
    fun resolveWeight(categoryLabel: String?): Int {
        val cat = categoryLabel?.trim()?.uppercase() ?: return 10
        return when (cat) {
            "EM FOCO" -> 100
            "EM DESTAQUE" -> 90
            "EDITORIAL" -> 85
            "OPINIÃO" -> 80
            "ARGUMENTOS" -> 75
            "PCP" -> 70
            "TRABALHADORES" -> 68
            "ASSEMBLEIA DA REPÚBLICA" -> 66
            "NACIONAL" -> 64
            "INTERNACIONAL" -> 62
            "TEMAS" -> 60
            "MEMÓRIA" -> 50
            "ACONTECEU" -> 40
            "SUGESTÕES DA SEMANA" -> 35
            "AGENDA" -> 30
            "RESUMO DA SEMANA" -> 25
            "FRASES" -> 20
            else -> 10
        }
    }
}
