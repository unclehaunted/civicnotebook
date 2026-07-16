package pt.avante.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jsoup.nodes.Element

object ArticleParser {

    /**
     * Normaliza uma URL relativa ou absoluta para URL absoluta.
     * 
     * @param url URL a normalizar (pode ser relativa ou absoluta)
     * @param baseUrl URL base a usar para URLs relativas (default: https://avante.pt)
     * @return URL absoluta normalizada
     */
    private fun normalizeUrl(url: String, baseUrl: String = "https://avante.pt"): String {
        return when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.startsWith("/") -> "$baseUrl$url"
            else -> "$baseUrl/$url"
        }
    }

    /**
     * Normaliza uma URL de imagem, convertendo URLs relativas em absolutas.
     * Retorna null se a imagem for placeholder/logo.
     * 
     * @param imageUrl URL da imagem a normalizar (pode ser null)
     * @param baseUrl URL base a usar para URLs relativas (default: https://avante.pt)
     * @return URL absoluta normalizada ou null se for placeholder
     */
    private fun normalizeImageUrl(imageUrl: String?, baseUrl: String = "https://avante.pt"): String? {
        if (imageUrl == null) return null
        if (isPlaceholderImage(imageUrl)) return null
        
        return when {
            imageUrl.startsWith("http://") || imageUrl.startsWith("https://") -> imageUrl
            imageUrl.startsWith("/") -> "$baseUrl$imageUrl"
            else -> "$baseUrl/$imageUrl"
        }
    }

    /**
     * Verifica se uma URL de imagem é um placeholder/logo que deve ser ignorado.
     * 
     * @param imageUrl URL da imagem a verificar
     * @return true se for placeholder, false caso contrário (null retorna false)
     */
    private fun isPlaceholderImage(imageUrl: String?): Boolean {
        if (imageUrl == null || imageUrl.isBlank()) return false
        
        val placeholderPatterns = listOf(
            "simbolo_foice",
            "ImageId=41",
            "pcp22px.png",
            "omilitante22px.png",
            "festa_avante",
            "avante_clandestino",
            "avante.gif",
            "/themes/publication_2/theme_2/img/"
        )
        
        return placeholderPatterns.any { pattern -> imageUrl.contains(pattern) }
    }

    /**
     * Metadados extraídos do Índice tpl=183.
     * Representa a estrutura editorial/semântica da edição.
     * 
     * TODO (Fase 2): Estes metadados serão integrados no modelo público ArticleSummary
     * quando os campos categoryLabel/subcategoryLabel forem adicionados.
     */
    private data class IndexMetadata(
        val url: String,
        val title: String?,
        val categoryLabel: String?,      // Secção editorial do Índice (ex: "Opinião", "Nacional")
        val subcategoryLabel: String?,   // Subsecção/rubrica (ex: "A talhe de foice", "PCP")
        val isFree: Boolean,
        val indexImageUrl: String?,
        val intro: String?
    )

    /**
     * Mapa de categorias válidas conhecidas do Avante!
     * Chave: versão normalizada (lowercase, sem acentos)
     * Valor: label canónico para display
     */
    private val VALID_CATEGORIES = mapOf(
        "editorial" to "Editorial",
        "opiniao" to "Opinião",
        "em destaque" to "Em Destaque",
        "emdestaque" to "Em Destaque",
        "em foco" to "Em Foco",
        "emfoco" to "Em Foco",
        "pcp" to "PCP",
        "trabalhadores" to "Trabalhadores",
        "assembleia da republica" to "Assembleia da República",
        "assembleiadarepublica" to "Assembleia da República",
        "nacional" to "Nacional",
        "internacional" to "Internacional",
        "temas" to "Temas",
        "argumentos" to "Argumentos",
        "memoria" to "Memória",
        "sugestoes da semana" to "Sugestões da semana",
        "sugestoes" to "Sugestões da semana",
        "aconteceu" to "Aconteceu",
        "agenda" to "Agenda",
        "resumo da semana" to "Resumo da Semana",
        "resumosemana" to "Resumo da Semana",
        "frases" to "Frases"
    )

    /**
     * Normaliza texto para comparação: lowercase, remove acentos, colapsa espaços.
     */
    private fun normalizeForComparison(text: String): String {
        return text.trim()
            .lowercase()
            .replace("á", "a")
            .replace("à", "a")
            .replace("â", "a")
            .replace("ã", "a")
            .replace("é", "e")
            .replace("ê", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ô", "o")
            .replace("õ", "o")
            .replace("ú", "u")
            .replace("ç", "c")
            .replace(Regex("\\s+"), " ")
    }

    /**
     * Valida e retorna o label canónico de uma categoria.
     * Retorna null se o texto não for uma categoria válida.
     * 
     * @param rawText Texto candidato a categoria
     * @return Label canónico da categoria ou null
     */
    private fun canonicalCategoryLabel(rawText: String?): String? {
        if (rawText == null) return null
        
        val trimmed = rawText.trim()
        
        // Rejeitar texto muito longo (categorias são curtas)
        if (trimmed.length > 50) return null
        
        // Rejeitar texto com pontuação típica de artigos
        if (trimmed.contains("?") || trimmed.contains("!") || trimmed.contains(":")) return null
        
        // Rejeitar se contém "Índice" (título de página)
        if (trimmed.contains("Índice", ignoreCase = true) || 
            trimmed.contains("Indice", ignoreCase = true)) return null
        
        // Normalizar e procurar no mapa
        val normalized = normalizeForComparison(trimmed)
        return VALID_CATEGORIES[normalized]
    }

    /**
     * Extrai categoria de um div.seccao do índice tpl=183.
     * Procura por link direto de categoria (não .htm), data-section, ou id.
     * 
     * @param section Elemento div.seccao
     * @return Label canónico da categoria ou null
     */
    private fun extractCategoryFromSectionDiv(section: Element): String? {
        // Procurar link direto de categoria (primeiro child <a> que não seja artigo)
        val directCategoryLinkText = section.children()
            .firstOrNull { child ->
                child.tagName().equals("a", ignoreCase = true) &&
                !child.attr("href").contains(".htm")
            }
            ?.text()
            ?.trim()
        
        // Tentar validar o texto do link
        val fromLink = canonicalCategoryLabel(directCategoryLinkText)
        if (fromLink != null) return fromLink
        
        // Fallback para data-section
        val fromDataAttr = canonicalCategoryLabel(section.attr("data-section"))
        if (fromDataAttr != null) return fromDataAttr
        
        // Fallback para id (ex: "opiniao", "emdestaque")
        val sectionId = section.id().takeIf { it.isNotBlank() }
        return canonicalCategoryLabel(sectionId)
    }

    suspend fun fetchAvanteArticle(url: String, section: String? = null, heroImageFromFeed: String? = null): Article = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect(url).get()
        
        // Avante! specific structure
        val title = doc.select("article h2.balance-text").first()?.text() 
            ?: doc.title().substringBefore("|").trim()
            
        val author = doc.select(".author, .autor").first()?.text() ?: "Redacção"
        val publishedAt = doc.select(".date, .data").first()?.text() ?: "Edição Actual"
        
        // Hero image priority:
        // 1. Use hero from feed if provided and valid
        // 2. Look for hero in article page
        // 3. If none found, leave as null (no hero image)
        var heroImageUrl: String? = null
        
        if (heroImageFromFeed != null && heroImageFromFeed != "LOCAL_SYMBOL") {
            // Use hero from feed if it's valid
            heroImageUrl = heroImageFromFeed
        } else {
            // Try to find hero in article page - look in article first, then in section before article
            var articleImg = doc.select("article img").first()?.attr("src")
            
            // If not found in article, look in the section that precedes the article
            if (articleImg == null) {
                articleImg = doc.select("section img, .content img").first()?.attr("src")
            }
            
            heroImageUrl = normalizeImageUrl(articleImg)
        }
        
        val blocks = mutableListOf<ArticleBlock>()
        
        // Extract paragraphs inside the article tag, excluding captions inside <figure> or <figcaption>
        val paragraphs = doc.select("article p, article div.texto-intro")
            .filter { p -> 
                // Exclude paragraphs that are inside a <figure> or <figcaption> element (these are captions)
                p.parents().none { it.tagName() in listOf("figure", "figcaption") }
            }
        for (p in paragraphs) {
            val text = p.text().trim()
            if (text.contains("O artigo completo está disponível") || text.contains("Compra o «Avante!»")) {
                blocks.add(ArticleBlock.PaywallCTA)
                break
            }
            if (text.isNotBlank() && !text.contains("Já é assinante ou comprou")) {
                blocks.addAll(parseParagraphElement(p))
            }
        }
        
        Article(
            id = url.hashCode().toString(),
            title = title,
            author = author,
            publishedAt = publishedAt,
            canonicalUrl = url,
            heroImageUrl = heroImageUrl,
            publication = Publication.AVANTE,
            section = section,
            blocks = blocks
        )
    }

    private fun parseParagraphElement(p: Element): List<ArticleBlock> {
        val blocks = mutableListOf<ArticleBlock>()
        
        val text = p.text().trim()
        if (text.isBlank()) return emptyList()
        
        // Only treat as subheading if there's EXACTLY one <strong> child,
        // no text outside it, and the text is reasonably short (subheadings are brief)
        val onlyStrong =
            p.children().size == 1 &&
            p.children().firstOrNull()?.tagName()?.equals("strong", ignoreCase = true) == true &&
            p.ownText().trim().isBlank() &&
            text.length <= 100
            
        if (onlyStrong) {
            blocks.add(ArticleBlock.Subheading(text))
            return blocks
        }
        
        // For all other cases, treat as regular text to avoid duplication
        blocks.add(ArticleBlock.Text(text))
        return blocks
    }

    suspend fun fetchHomeFeed(): HomeFeed? = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect("https://avante.pt/").get()

            // 1. Fetch Edition Name and Number
            val editionNode = doc.select("#simbolo").first()
            val editionText = editionNode?.text()?.trim() ?: "Nº Desconhecido"
            val editionNumber = Regex("""n\.º\s*(\d+)""").find(editionText)?.groupValues?.get(1) ?: ""
            
            // 2. Fetch index inventory to get semantic metadata and free/premium status
            val indexInventory = fetchIndexInventory(editionNumber)
            
            val articles = mutableListOf<ArticleSummary>()
            
            // 3. Parse sections from the homepage
            val sections = doc.select(".content-front section")
            
            for (section in sections) {
                // Try to find the section title - look for h2 or h3 that's a direct child or close sibling
                var currentCategory = "Destaque"
                
                // Look for section header - could be before the section or inside it
                val sectionHeader = section.selectFirst("h2, h3")
                if (sectionHeader != null && sectionHeader.parents().none { it.tagName() == "article" }) {
                    val headerText = sectionHeader.text().trim()
                    if (headerText.isNotEmpty()) {
                        currentCategory = headerText
                    }
                }
                
                // Parse articles within this section
                val sectionArticles = section.select("article")
                for (el in sectionArticles) {
                    val titleNode = el.select("h2 a, h3 a").first() ?: continue
                    val title = titleNode.text().trim()
                    val url = titleNode.attr("href").trim()
                    
                    if (title.isEmpty() || url.isEmpty()) continue
                    
                    val fullUrl = normalizeUrl(url)
                    val author = "Redacção" 
                    val summary = el.select(".texto-intro p, p").firstOrNull()?.text()?.trim() ?: ""
                    
                    val rawImageUrl = el.select("figure img, .noticia-foto img, img").first()?.attr("src")
                    var heroImageUrl = when {
                        rawImageUrl == null || rawImageUrl.isBlank() -> null
                        isPlaceholderImage(rawImageUrl) -> "LOCAL_SYMBOL"
                        else -> normalizeImageUrl(rawImageUrl)
                    }
                    
                    // Merge com metadados do índice (se disponível)
                    val indexMeta = indexInventory[fullUrl]
                    
                    // Fase 2A: Separar categoria semântica (tpl=183) de placement visual (homepage)
                    val visualPlacement = currentCategory  // Placement da homepage (ex: "Em Destaque")
                    
                    // Fallback seguro: se índice não tem categoria, validar placement visual
                    val semanticCategory = indexMeta?.categoryLabel 
                        ?: canonicalCategoryLabel(visualPlacement)
                    val semanticSubcategory = indexMeta?.subcategoryLabel
                    
                    // Campo section para compatibilidade
                    val compatSection = semanticCategory ?: visualPlacement
                    
                    // Check if article is free based on index inventory
                    // Preserva comportamento original: se não estiver no índice, assume premium
                    val isFree = indexMeta?.isFree ?: false
                    val isPremium = !isFree
                    
                    // Determinar cardTreatment baseado em visualPlacement e categoria semântica
                    val visual = visualPlacement?.trim()
                    val isOpinionPlacement = visual.equals("Opinião", ignoreCase = true) ||
                        visual.equals("Opiniao", ignoreCase = true) ||
                        visual.equals("Actual", ignoreCase = true)
                    val isOpinionFallback = visual.isNullOrBlank() &&
                        semanticCategory.equals("Opinião", ignoreCase = true)
                    
                    val cardTreatment = when {
                        visual.equals("Em Foco", ignoreCase = true) || articles.isEmpty() ->
                            CardTreatment.LEAD_LARGE
                        visual.equals("Em Destaque", ignoreCase = true) ->
                            CardTreatment.FEATURED_MEDIUM
                        isOpinionPlacement || isOpinionFallback ->
                            CardTreatment.OPINION_COMPACT
                        heroImageUrl == null ->
                            CardTreatment.TEXT_ONLY
                        else ->
                            CardTreatment.STANDARD_SMALL
                    }
                    
                    articles.add(ArticleSummary(
                        title = title,
                        author = author,
                        url = fullUrl,
                        heroImageUrl = heroImageUrl,
                        summary = summary,
                        isPremium = isPremium,
                        section = compatSection,
                        isFree = isFree,
                        categoryLabel = semanticCategory,
                        subcategoryLabel = semanticSubcategory,
                        visualPlacement = visualPlacement,
                        cardTreatment = cardTreatment
                    ))
                }
            }

            // Concurrently fetch true images for any LOCAL_SYMBOL fallbacks
            val resolvedArticles = coroutineScope {
                articles.map { summary ->
                    async {
                        var finalImage = summary.heroImageUrl
                        if (finalImage == "LOCAL_SYMBOL" || finalImage == null) {
                            try {
                                val articleDoc = Jsoup.connect(summary.url).get()
                                // Try article first, then section/content
                                var articleImg = articleDoc.select("article img").first()?.attr("src")
                                if (articleImg == null) {
                                    articleImg = articleDoc.select("section img, .content img").first()?.attr("src")
                                }
                                finalImage = normalizeImageUrl(articleImg)
                            } catch (e: Exception) {
                                // Keep LOCAL_SYMBOL on error
                            }
                        }
                        summary.copy(heroImageUrl = finalImage)
                    }
                }.awaitAll()
            }

            HomeFeed(
                editionText = editionText,
                editionNumber = editionNumber,
                articles = resolvedArticles
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Extrai inventário completo do Índice tpl=183 com metadados semânticos.
     * 
     * Implementa DOM-walking algorithm conforme PARSER_STATE_MACHINE_FOR_TPL183.md:
     * 1. Percorre conteúdo em ordem de documento
     * 2. Rastreia currentCategory via div.seccao ou headers de secção
     * 3. Rastreia currentSubcategory via h5 (rubricas)
     * 4. Extrai artigos com categoria/subcategoria atual
     * 5. Deduplica por URL canónico normalizado
     * 
     * @param editionNumber Número da edição (ex: "2746")
     * @return Map de URL normalizado → IndexMetadata
     */
    private suspend fun fetchIndexInventory(editionNumber: String): Map<String, IndexMetadata> = withContext(Dispatchers.IO) {
        try {
            val indexUrl = "https://avante.pt/pt/$editionNumber/?tpl=183"
            val doc = Jsoup.connect(indexUrl).get()
            
            val inventory = mutableMapOf<String, IndexMetadata>()
            
            // State machine: rastrear categoria e subcategoria atual
            var currentCategory: String? = null
            var currentSubcategory: String? = null
            
            // Selecionar área de conteúdo principal do índice (evitar nav/footer)
            val contentArea = doc.select("#grid-indice, .content-indice, main, .main-content").firstOrNull() ?: doc.body()
            
            // Percorrer todos os elementos em ordem de documento
            val elements = contentArea.select("div.seccao, h2, h3, h5, article, a[href\$='.htm']")
            
            for (element in elements) {
                when {
                    // Transição de categoria: div.seccao
                    element.tagName() == "div" && element.hasClass("seccao") -> {
                        val canonical = extractCategoryFromSectionDiv(element)
                        if (canonical != null) {
                            currentCategory = canonical
                            currentSubcategory = null // Reset subcategoria ao mudar de categoria
                        }
                    }
                    
                    // Headers de secção fora de article (podem ser categorias)
                    (element.tagName() == "h2" || element.tagName() == "h3") && 
                    element.parents().none { it.tagName() == "article" } -> {
                        // Usar ownText para evitar capturar texto de descendentes (artigos)
                        val headerText = element.ownText().trim()
                        
                        // Verificar se não é um título de artigo (artigos têm links .htm)
                        val hasArticleLink = element.select("a[href\$='.htm']").isNotEmpty()
                        
                        if (!hasArticleLink) {
                            val canonical = canonicalCategoryLabel(headerText)
                            if (canonical != null) {
                                currentCategory = canonical
                                currentSubcategory = null
                            }
                        }
                    }
                    
                    // Transição de subcategoria: h5 (rubricas como "A talhe de foice")
                    // Só aceitar se já tivermos uma categoria válida
                    element.tagName() == "h5" && 
                    element.parents().none { it.tagName() == "article" } &&
                    currentCategory != null -> {
                        val subcategoryText = element.text().trim()
                        // Subcategorias podem ser mais variadas, mas devem ser curtas
                        if (subcategoryText.isNotBlank() && subcategoryText.length <= 60) {
                            currentSubcategory = subcategoryText
                        }
                    }
                    
                    // Artigo: extrair metadados
                    element.tagName() == "article" -> {
                        extractArticleFromIndexElement(element, currentCategory, currentSubcategory, inventory)
                    }
                    
                    // Link direto .htm fora de article (alguns índices têm estrutura solta)
                    element.tagName() == "a" && element.attr("href").endsWith(".htm") &&
                    element.parents().none { it.tagName() == "article" } -> {
                        extractArticleFromLinkElement(element, currentCategory, currentSubcategory, inventory)
                    }
                }
            }
            
            inventory
        } catch (e: Exception) {
            // Log warning em produção, retornar mapa vazio como fallback
            emptyMap()
        }
    }
    
    /**
     * Extrai metadados de artigo de um elemento <article> do índice.
     */
    private fun extractArticleFromIndexElement(
        article: Element,
        currentCategory: String?,
        currentSubcategory: String?,
        inventory: MutableMap<String, IndexMetadata>
    ) {
        // Procurar link do artigo
        val link = article.select("a[href\$='.htm']").firstOrNull() ?: return
        val url = link.attr("href").trim()
        if (url.isBlank()) return
        
        val normalizedUrl = normalizeUrl(url)
        
        // Evitar duplicados: se já existe, manter o primeiro (ordem de documento)
        if (inventory.containsKey(normalizedUrl)) return
        
        // Extrair título
        val title = link.text().trim().takeIf { it.isNotBlank() }
            ?: article.select("h2, h3").firstOrNull()?.text()?.trim()
        
        // Extrair intro
        val intro = article.select(".texto-intro, p").firstOrNull()?.text()?.trim()
        
        // Extrair imagem (filtrar placeholders)
        val rawImageUrl = article.select("img").firstOrNull()?.attr("src")
        val indexImageUrl = if (rawImageUrl != null && !isPlaceholderImage(rawImageUrl)) {
            normalizeImageUrl(rawImageUrl)
        } else {
            null
        }
        
        // Detetar marcador de acesso livre
        val isFree = article.select("h4:contains(Artigo de acesso livre)").isNotEmpty()
        
        inventory[normalizedUrl] = IndexMetadata(
            url = normalizedUrl,
            title = title,
            categoryLabel = currentCategory,
            subcategoryLabel = currentSubcategory,
            isFree = isFree,
            indexImageUrl = indexImageUrl,
            intro = intro
        )
    }
    
    /**
     * Extrai metadados de artigo de um link solto .htm (estrutura de índice sem <article>).
     */
    private fun extractArticleFromLinkElement(
        link: Element,
        currentCategory: String?,
        currentSubcategory: String?,
        inventory: MutableMap<String, IndexMetadata>
    ) {
        val url = link.attr("href").trim()
        if (url.isBlank()) return
        
        val normalizedUrl = normalizeUrl(url)
        
        // Evitar duplicados
        if (inventory.containsKey(normalizedUrl)) return
        
        // Título vem do texto do link
        val title = link.text().trim().takeIf { it.isNotBlank() }
        
        // Procurar intro em siblings próximos
        val intro = link.parent()?.select("p")?.firstOrNull()?.text()?.trim()
        
        // Procurar marcador de acesso livre em siblings
        val isFree = link.parent()?.select("h4:contains(Artigo de acesso livre)")?.isNotEmpty() ?: false
        
        inventory[normalizedUrl] = IndexMetadata(
            url = normalizedUrl,
            title = title,
            categoryLabel = currentCategory,
            subcategoryLabel = currentSubcategory,
            isFree = isFree,
            indexImageUrl = null,
            intro = intro
        )
    }
}

object ArticleBlockSerializer {

    private val format = Json { 
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    fun serialize(blocks: List<ArticleBlock>): String {
        return format.encodeToString(blocks)
    }

    fun deserialize(jsonString: String): List<ArticleBlock> {
        return try {
            if (jsonString.isBlank()) return emptyList()
            if (jsonString.contains("|||") && !jsonString.trim().startsWith("[")) {
                parseLegacy(jsonString)
            } else {
                format.decodeFromString(jsonString)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseLegacy(jsonString: String): List<ArticleBlock> {
        if (jsonString.isBlank()) return emptyList()
        return jsonString.split("|||").mapNotNull { blockStr ->
            val parts = blockStr.split("||")
            when (parts[0]) {
                "TEXT" -> ArticleBlock.Text(content = parts.getOrNull(1) ?: "")
                "IMAGE" -> ArticleBlock.Image(
                    url = parts.getOrNull(1) ?: "",
                    caption = parts.getOrNull(2) ?: ""
                )
                "SUBHEADING" -> ArticleBlock.Subheading(text = parts.getOrNull(1) ?: "")
                "PAYWALL_CTA" -> ArticleBlock.PaywallCTA
                else -> null
            }
        }
    }
}
