package dev.kbwallet.app.coins.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.portfolio.presentation.SearchField
import dev.kbwallet.app.theme.Dimens
import dev.kbwallet.app.theme.component.BackHeader
import dev.kbwallet.app.theme.component.ChangePill
import dev.kbwallet.app.theme.component.CoinAvatar
import dev.kbwallet.app.theme.component.EmptyState
import dev.kbwallet.app.theme.component.ErrorRetryCard
import dev.kbwallet.app.theme.component.KBCard
import dev.kbwallet.app.theme.component.SkeletonList
import dev.kbwallet.app.theme.component.screenContentPadding
import dev.kbwallet.app.theme.tabular
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CoinListScreen(
    onCoinClicked: (String) -> Unit,
    onChartRequested: (String, String) -> Unit,
    onBack: () -> Unit = {},
) {
    val coinsListViewModel = koinViewModel<CoinsListViewModel>()
    val state by coinsListViewModel.state.collectAsStateWithLifecycle()
    val strings = appStrings()
    var query by rememberSaveable { mutableStateOf("") }

    val filtered = if (query.isBlank()) state.coins else state.coins.filter {
        it.name.contains(query, ignoreCase = true) || it.symbol.contains(query, ignoreCase = true)
    }

    Column(Modifier.fillMaxSize()) {
        BackHeader(title = strings.coinsListTitle, subtitle = strings.coinsHint, onBack = onBack)

        when {
            state.error != null && state.coins.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(Dimens.xl),
                contentAlignment = Alignment.Center,
            ) {
                ErrorRetryCard(
                    message = stringResource(state.error!!),
                    onRetry = { coinsListViewModel.retry() },
                )
            }

            // No loading flag in state: empty list without error means still loading.
            state.coins.isEmpty() -> SkeletonList(rows = 8, header = false)

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = screenContentPadding(top = Dimens.xxs),
                verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
            ) {
                item {
                    SearchField(
                        query = query,
                        onQueryChange = { query = it },
                        placeholder = strings.coinsSearchPlaceholder,
                        modifier = Modifier.padding(bottom = Dimens.xxs),
                    )
                }
                if (filtered.isEmpty()) {
                    item { EmptyState(icon = Icons.Default.SearchOff, title = strings.coinsSearchEmpty) }
                }
                itemsIndexed(filtered, key = { _, coin -> coin.id }) { index, coin ->
                    CoinListItem(
                        rank = state.coins.indexOf(coin) + 1,
                        coin = coin,
                        onClick = { onChartRequested(coin.id, coin.name) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun CoinListItem(
    rank: Int,
    coin: UiCoinListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KBCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = Dimens.md, vertical = Dimens.sm + 2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = rank.toString(),
                style = MaterialTheme.typography.labelMedium.tabular(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(24.dp),
            )
            CoinAvatar(iconUrl = coin.iconUrl, symbol = coin.symbol)
            Spacer(modifier = Modifier.width(Dimens.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = coin.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = coin.symbol.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = coin.formattedPrice,
                    style = MaterialTheme.typography.titleSmall.tabular(),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                ChangePill(text = coin.formattedChange, isPositive = coin.isPositive)
            }
        }
    }
}
