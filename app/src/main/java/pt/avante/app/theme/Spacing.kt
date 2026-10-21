package pt.avante.app.theme

import androidx.compose.ui.unit.dp

/**
 * Escala de espaçamento da UI (spec #49): 4 · 8 · 12 · 16 · 24 · 32 · 48 dp.
 *
 * Todo o `padding`, `PaddingValues`/`contentPadding`, `Arrangement.spacedBy` e
 * `Spacer` usa estes degraus; `scripts/check-design-tokens.sh` falha com um
 * literal `dp` de espaçamento no código de UI. Um valor fora da escala vai
 * para o degrau mais próximo e, num empate, sobe (6→8, 10→12, 14→16, 20→24).
 * Tamanhos de componentes (ícones, imagens, larguras máximas) não são
 * espaçamento e ficam fora desta escala.
 */
object Spacing {
    /** 4dp: entre ícone e texto, dentro de pílulas. */
    val xxs = 4.dp
    /** 8dp: entre elementos próximos do mesmo grupo. */
    val xs = 8.dp
    /** 12dp: entre cartões de uma lista, padding de blocos pequenos. */
    val s = 12.dp
    /** 16dp: margem lateral do ecrã e padding dos cartões. */
    val m = 16.dp
    /** 24dp: margem do texto do artigo, entre secções. */
    val l = 24.dp
    /** 32dp: separação forte entre grupos, estados vazios. */
    val xl = 32.dp
    /** 48dp: a maior separação vertical. */
    val xxl = 48.dp
}
