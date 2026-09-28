package dev.kbwallet.app.watchlist.presentation

import dev.kbwallet.app.theme.component.BackHeader
import dev.kbwallet.app.theme.component.SkeletonList
import dev.kbwallet.app.theme.component.CoinAvatar
import dev.kbwallet.app.theme.Dimens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.theme.LocalKBLearningColorsPalette
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WatchlistScreen(
    onBack: () -> Unit,
    onCoinClicked: (String) -> Unit,
    onChartRequested: (String, String) -> Unit,
) {
    val viewModel = koinViewModel<WatchlistViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = appStrings()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackHeader(title = strings.watchlistTitle, onBack = onBack)

        if (state.isLoading) {
            SkeletonList(rows = 5, header = false)
        } else if (state.items.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(strings.watchlistEmptyTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        strings.watchlistEmptySubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.items, key = { it.id }) { item ->
                    WatchlistRow(
                        item = item,
                        onClick = { onCoinClicked(item.id) },
                        onLongClick = { viewModel.removeItem(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WatchlistRow(
    item: UiWatchlistItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.cardRadius))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        CoinAvatar(iconUrl = item.iconUrl, symbol = item.symbol)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = item.symbol.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.formattedPrice,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "${if (item.isPositive) "↗ " else "↘ "}${item.formattedChange}",
                style = MaterialTheme.typography.bodySmall,
                color = if (item.isPositive) LocalKBLearningColorsPalette.current.profitGreen
                else LocalKBLearningColorsPalette.current.lossRed,
            )
        }
    }
}
