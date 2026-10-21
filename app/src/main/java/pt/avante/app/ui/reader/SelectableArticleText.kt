package pt.avante.app.ui.reader

import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import java.util.Locale
import kotlin.math.roundToInt
import pt.avante.app.data.ArticleBlock
import pt.avante.app.data.db.HighlightEntity
import pt.avante.app.theme.LocalPaleta
import pt.avante.app.theme.PapelDoTexto
import pt.avante.app.theme.tipografiaDoArtigo

/** Ids dos itens do menu de seleção de texto ("Realçar" / "Criar Nota"). */
const val SELECTION_MENU_HIGHLIGHT = 1
const val SELECTION_MENU_CREATE_NOTE = 2

/** Língua do texto do artigo: escolhe os padrões de hifenização. */
private val PORTUGUESE: Locale = Locale.forLanguageTag("pt-PT")

/** [corDoDestaque] e [corDoSimbolo] são ARGB da paleta da Aparência (Destaque, texto secundário). */
fun buildHighlightedText(
    text: String,
    highlights: List<HighlightEntity>,
    blockIndex: Int,
    onHighlightClick: (HighlightEntity) -> Unit,
    corDoDestaque: Int,
    corDoSimbolo: Int,
    isFinalBlock: Boolean = false
): SpannableString {
    val baseText = if (isFinalBlock) "$text ☭" else text
    val spannable = SpannableString(baseText)

    if (isFinalBlock) {
        val symbolStart = baseText.length - 1
        spannable.setSpan(
            android.text.style.ForegroundColorSpan(corDoSimbolo),
            symbolStart,
            baseText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            android.text.style.RelativeSizeSpan(0.85f),
            symbolStart,
            baseText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    highlights
        .filter { it.blockIndex == blockIndex && it.startOffset != null && it.endOffset != null }
        .forEach { highlight ->
            val start = highlight.startOffset!!.coerceIn(0, text.length)
            val end = highlight.endOffset!!.coerceIn(start, text.length)

            if (start < end) {
                spannable.setSpan(
                    BackgroundColorSpan(corDoDestaque),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannable.setSpan(
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            onHighlightClick(highlight)
                        }
                        override fun updateDrawState(ds: android.text.TextPaint) {
                            ds.isUnderlineText = false
                        }
                    },
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    return spannable
}

@Composable
fun SelectableArticleText(
    content: String,
    index: Int,
    papel: PapelDoTexto = PapelDoTexto.CORPO,
    isFinalBlock: Boolean = false,
    articleHighlights: List<HighlightEntity>,
    onHighlightClick: (HighlightEntity) -> Unit,
    onHighlightPassage: (String, Int, Int, Int) -> Unit,
    onCreateNote: (String, Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tipografia = tipografiaDoArtigo()
    val paleta = LocalPaleta.current
    val corDoTexto = paleta.cor(papel.cor).toInt()

    val spannableText = androidx.compose.runtime.remember(content, articleHighlights, index, isFinalBlock, paleta) {
        buildHighlightedText(
            text = content,
            highlights = articleHighlights,
            blockIndex = index,
            onHighlightClick = onHighlightClick,
            corDoDestaque = paleta.destaque.toInt(),
            corDoSimbolo = paleta.textoSecundario.toInt(),
            isFinalBlock = isFinalBlock
        )
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            android.widget.TextView(ctx).apply {
                setTextIsSelectable(true)
                movementMethod = LinkMovementMethod.getInstance()
                highlightColor = android.graphics.Color.TRANSPARENT
                // Alinhado à esquerda (o TextView não justifica por omissão), com hifenização
                // portuguesa (#12): a margem direita da coluna estreita do telemóvel fica menos
                // irregular. Desde o Android 13 a frequência por omissão é NONE; fixa-se aqui.
                hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NORMAL
                textLocale = PORTUGUESE


                customSelectionActionModeCallback = object : android.view.ActionMode.Callback {
                    override fun onCreateActionMode(mode: android.view.ActionMode, menu: android.view.Menu): Boolean {
                        menu.add(0, SELECTION_MENU_HIGHLIGHT, 0, "Realçar")
                        menu.add(0, SELECTION_MENU_CREATE_NOTE, 1, "Criar Nota")
                        return true
                    }
                    override fun onPrepareActionMode(mode: android.view.ActionMode, menu: android.view.Menu): Boolean = false
                    override fun onActionItemClicked(mode: android.view.ActionMode, item: android.view.MenuItem): Boolean {
                        val start = selectionStart
                        val end = selectionEnd
                        if (start < 0 || end < 0 || start >= end) return false
                        val selectedStr = text.substring(start, end).removeSuffix(" ☭").removeSuffix("☭")
                        when (item.itemId) {
                            SELECTION_MENU_HIGHLIGHT -> {
                                onHighlightPassage(selectedStr, index, start, end)
                                mode.finish()
                                return true
                            }
                            SELECTION_MENU_CREATE_NOTE -> {
                                onCreateNote(selectedStr, index, start, end)
                                mode.finish()
                                return true
                            }
                        }
                        return false
                    }
                    override fun onDestroyActionMode(mode: android.view.ActionMode) {}
                }
            }
        },
        update = { view ->
            // Aplicar preferências de leitura (Definições): face e tamanho do papel do bloco.
            view.typeface = ResourcesCompat.getFont(view.context, tipografia.faceResId(papel))
            view.textSize = tipografia.tamanhoSp(papel)
            // Entrelinha do Espaçamento, em múltiplos do tamanho do texto (não da altura natural
            // da fonte, que varia de face para face).
            val lineHeight = tipografia.espacamento.lineHeight
            TextViewCompat.setLineHeight(view, (view.textSize * lineHeight).roundToInt())
            view.setTextColor(corDoTexto)

            if (view.tag != spannableText) {
                view.text = spannableText
                view.tag = spannableText
            }
        }
    )
}
