package pt.avante.app.data

/**
 * Tipos de letra (#5): a combinação curada que o Leitor escolhe para o artigo.
 * [display] serve o título, os subtítulos e as Citações; [texto] o corpo e as Perguntas.
 * Para juntar uma opção, as duas faces contrastam e partilham uma qualidade (ex.: a altura-x).
 */
enum class TiposDeLetra(val label: String, val display: ReaderFont, val texto: ReaderFont) {
    CLASSICO("Clássico", ReaderFont.BITTER, ReaderFont.LIBRE_FRANKLIN),
    LIVRO("Livro", ReaderFont.BITTER, ReaderFont.BITTER),
    MODERNO("Moderno", ReaderFont.LIBRE_FRANKLIN, ReaderFont.SOURCE_SANS),
    LEGIVEL("Legível", ReaderFont.ATKINSON_HYPERLEGIBLE, ReaderFont.ATKINSON_HYPERLEGIBLE);

    companion object {
        /**
         * Migra a preferência antiga de fonte única (o nome guardado do enum) para a combinação
         * mais próxima. IBM Plex Sans (retirado em #6) e qualquer valor desconhecido dão Clássico.
         */
        fun daFonteAntiga(nome: String?): TiposDeLetra = when (nome) {
            "BITTER" -> LIVRO
            "ATKINSON_HYPERLEGIBLE" -> LEGIVEL
            "SOURCE_SANS" -> MODERNO
            else -> CLASSICO
        }
    }
}

/** Escrita (#5): a face das palavras do próprio Leitor nas Notas, distinta da do jornal. */
enum class Escrita(val label: String, val face: ReaderFont) {
    IA_WRITER_DUO("iA Writer Duo", ReaderFont.IA_WRITER_DUO),
    SOURCE_SANS("Source Sans 3", ReaderFont.SOURCE_SANS)
}
