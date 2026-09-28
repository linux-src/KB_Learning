package dev.kbwallet.app.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/** App-specific colours that Material's scheme has no slot for. */
@Immutable
data class KBLearningColorsPalette(
    val profitGreen: Color = Color.Unspecified,
    val lossRed: Color = Color.Unspecified,
    /** Low-emphasis fills behind profit/loss icons and pills. */
    val profitContainer: Color = Color.Unspecified,
    val lossContainer: Color = Color.Unspecified,
    val warning: Color = Color.Unspecified,
    val warningContainer: Color = Color.Unspecified,
    /** Overlay line on charts (SMA etc.). */
    val chartAccent: Color = Color.Unspecified,
    /** Categorical series, ordered so neighbours stay distinguishable. */
    val chartSeries: List<Color> = emptyList(),
    /** Top-left → bottom-right stops of the balance hero card. */
    val heroGradient: List<Color> = emptyList(),
    /** Hairline used for card borders and dividers. */
    val hairline: Color = Color.Unspecified,
)

val ProfitGreenColor = Color(0xFF0E9F6E)
val LossRedColor = Color(0xFFD63A40)

val DarkProfitGreenColor = Color(0xFF34D399)
val DarkLossRedColor = Color(0xFFFF6B70)

val LightKBLearningColorsPalette = KBLearningColorsPalette(
    profitGreen = ProfitGreenColor,
    lossRed = LossRedColor,
    profitContainer = Color(0xFFDDF7EC),
    lossContainer = Color(0xFFFFE4E5),
    warning = Color(0xFFB7791F),
    warningContainer = Color(0xFFFFF1D6),
    chartAccent = Color(0xFFE08A00),
    chartSeries = listOf(
        Color(0xFF0E9F6E), Color(0xFF2F6FE4), Color(0xFF8B5CF6), Color(0xFFE08A00),
        Color(0xFFDB2777), Color(0xFF0891B2), Color(0xFF65A30D), Color(0xFF64748B),
    ),
    heroGradient = listOf(Color(0xFF0B8F61), Color(0xFF0A6E78)),
    hairline = Color(0xFFE3E8EE),
)

val DarkKBLearningColorsPalette = KBLearningColorsPalette(
    profitGreen = DarkProfitGreenColor,
    lossRed = DarkLossRedColor,
    profitContainer = Color(0xFF113A2C),
    lossContainer = Color(0xFF3E1A1D),
    warning = Color(0xFFFBBF24),
    warningContainer = Color(0xFF3A2E12),
    chartAccent = Color(0xFFFFB547),
    chartSeries = listOf(
        Color(0xFF3EE6A0), Color(0xFF6EA8FF), Color(0xFFB794F6), Color(0xFFFFB547),
        Color(0xFFF472B6), Color(0xFF22D3EE), Color(0xFFA3E635), Color(0xFF94A3B8),
    ),
    heroGradient = listOf(Color(0xFF123F33), Color(0xFF0F2A3A)),
    hairline = Color(0xFF1F2833),
)

val LocalKBLearningColorsPalette = compositionLocalOf { KBLearningColorsPalette() }

/** Short accessor: `KBTheme.colors.profitGreen`. */
object KBTheme {
    val colors: KBLearningColorsPalette
        @Composable @ReadOnlyComposable get() = LocalKBLearningColorsPalette.current

    /** Profit colour for >= 0, loss colour otherwise. */
    @Composable
    @ReadOnlyComposable
    fun trend(isPositive: Boolean): Color =
        if (isPositive) colors.profitGreen else colors.lossRed
}
