package dev.kbwallet.app.theme.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kbwallet.app.theme.KBTheme
import dev.kbwallet.app.theme.tabular

/**
 * Shared "stat tile" — the small metric cards used on Dashboard, History,
 * Profile, P&L Analytics, and the Simulator (balance, win rate, etc.).
 *
 * These used to be six near-identical, hand-copied composables (one per
 * screen) that had each drifted to slightly different padding/corner-radius/
 * font-size values, which is why the cards didn't line up visually between
 * screens (and sometimes even within the same row, since nothing forced a
 * shared height).
 *
 * The height itself is intentionally NOT a fixed dp constant — an earlier
 * version of this component pinned a guessed height, which clipped the
 * bottom of the value text once real font metrics (line height, not just
 * the nominal sp size) were taken into account. Instead this fills the
 * height of its parent, and callers put their Row in
 * `Modifier.height(IntrinsicSize.Max)` so every card in the row stretches to
 * match whichever one actually needs the most room — correct regardless of
 * font/locale/DPI instead of a magic number that happens to fit today.
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onBackground,
    size: StatCardSize = StatCardSize.Regular,
    // Deprecated: values always use tabular figures now.
    @Suppress("UNUSED_PARAMETER") monospaceValue: Boolean = false,
) {
    val shape = RoundedCornerShape(size.cornerRadius)
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, KBTheme.colors.hairline, shape)
            .padding(size.padding)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(size.titleValueGap)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                fontSize = size.titleFontSize,
                maxLines = 2,
                lineHeight = size.titleFontSize * 1.25f, // Ensure readable line height if it wraps
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.tabular(),
                fontSize = size.valueFontSize,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

enum class StatCardSize(
    val padding: Dp,
    val cornerRadius: Dp,
    val titleValueGap: Dp,
    val titleFontSize: TextUnit,
    val valueFontSize: TextUnit,
) {
    /** Dashboard, History, Profile, P&L — three cards per row. */
    Regular(padding = 16.dp, cornerRadius = 18.dp, titleValueGap = 6.dp, titleFontSize = 12.sp, valueFontSize = 19.sp),

    /** Simulator's denser grids (up to six cards across two rows). */
    Compact(padding = 12.dp, cornerRadius = 14.dp, titleValueGap = 3.dp, titleFontSize = 11.sp, valueFontSize = 15.sp),
}
