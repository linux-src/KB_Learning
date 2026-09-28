package dev.kbwallet.app.theme.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.theme.Dimens

/**
 * Shared "something went wrong" state with a retry button.
 *
 * Before this, every screen either didn't render its ViewModel's `error`
 * field at all (dead state — [dev.kbwallet.app.coins.presentation.CoinsState],
 * [dev.kbwallet.app.portfolio.presentation.PortfolioState],
 * [dev.kbwallet.app.simulator.presentation.SimulatorState] all populated one
 * but no Screen read it), or hand-rolled a bare `Text` with no way to retry
 * ([dev.kbwallet.app.trade.presentation.common.TradeScreen],
 * [dev.kbwallet.app.chart.presentation.CryptoChartScreen]). A screen that
 * fails silently is indistinguishable from one that's still loading or
 * genuinely empty — this is why bugs like a dead API key or a deserialization
 * exception went unnoticed for a long time (see project notes).
 *
 * Deliberately NOT `fillMaxSize()` itself — it's a self-contained card, so
 * callers decide placement: wrap it in a centered `Box(Modifier.fillMaxSize())`
 * for a full-screen replacement (coin list, portfolio, simulator), or drop it
 * straight into a `LazyColumn` `item {}` for an inline banner above content
 * that's still partially available (Dashboard).
 */
@Composable
fun ErrorRetryCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorColor = MaterialTheme.colorScheme.error
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.cardRadius))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, errorColor.copy(alpha = 0.35f), RoundedCornerShape(Dimens.cardRadius))
            .padding(Dimens.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconBadge(
            icon = Icons.Default.CloudOff,
            tint = errorColor,
            size = 52.dp,
            iconSize = 26.dp,
            shape = CircleShape,
        )
        Spacer(Modifier.height(Dimens.sm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Dimens.md))
        OutlinedButton(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Dimens.xs))
            Text(appStrings().actionRetry)
        }
    }
}
