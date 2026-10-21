package pt.avante.app.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Escala de raios da UI (spec #49), com os valores por omissão do Material 3
 * declarados para ficarem documentados. O papel escolhe a forma:
 *
 * - extraSmall (4): pílulas e etiquetas;
 * - small (8): blocos e imagens dentro de cartões, campos;
 * - medium (12): cartões de artigo e de Nota;
 * - large (16): faixas (Modo de Destaque, ações da seleção) e diálogos;
 * - extraLarge (28): topo de folhas, incluindo o corpo do artigo sobre a imagem.
 *
 * Sem cantos (`RectangleShape`) e totalmente redondo (`CircleShape`, ou
 * percentagem 50) são formas, não raios, e ficam fora da escala.
 * `scripts/check-design-tokens.sh` falha com `RoundedCornerShape` com literal
 * `dp` no código de UI.
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
