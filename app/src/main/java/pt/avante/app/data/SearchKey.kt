package pt.avante.app.data

import java.text.Normalizer
import java.util.Locale

private val COMBINING_MARKS = Regex("""\p{Mn}+""")

/**
 * Chave de comparação da Pesquisa: minúsculas e sem diacríticos ("Educação" → "educacao").
 * Só serve para comparar; o texto mostrado mantém os acentos.
 */
fun String.toSearchKey(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(COMBINING_MARKS, "")
        .lowercase(Locale.ROOT)
