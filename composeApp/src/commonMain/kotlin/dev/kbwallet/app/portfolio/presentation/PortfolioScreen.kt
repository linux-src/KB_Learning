package dev.kbwallet.app.portfolio.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.portfolio.presentation.component.DonutChart
import dev.kbwallet.app.theme.Dimens
import dev.kbwallet.app.theme.KBTheme
import dev.kbwallet.app.theme.component.BalanceHeroCard
import dev.kbwallet.app.theme.component.ChangePill
import dev.kbwallet.app.theme.component.CoinAvatar
import dev.kbwallet.app.theme.component.EmptyState
import dev.kbwallet.app.theme.component.ErrorRetryCard
import dev.kbwallet.app.theme.component.KBCard
import dev.kbwallet.app.theme.component.ScreenTitle
import dev.kbwallet.app.theme.component.SectionHeader
import dev.kbwallet.app.theme.component.SkeletonList
import dev.kbwallet.app.theme.component.screenContentPadding
import dev.kbwallet.app.theme.tabular
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PortfolioScreen(
    onCoinItemClicked: (id: String, name: String) -> Unit,
    onDiscoverCoinsClicked: () -> Unit,
) {
    val portfolioViewModel = koinViewModel<PortfolioViewModel>()
    val state by portfolioViewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        SkeletonList(rows = 5)
    } else if (state.error != null && state.coins.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            ErrorRetryCard(
                message = stringResource(state.error!!),
                onRetry = { portfolioViewModel.retry() },
                modifier = Modifier.padding(Dimens.xl),
            )
        }
    } else {
        PortfolioContent(
            state = state,
            onRetry = { portfolioViewModel.retry() },
            onCoinItemClicked = onCoinItemClicked,
            onDiscoverCoinsClicked = onDiscoverCoinsClicked
        )
    }
}

@Composable
private fun PortfolioContent(
    state: PortfolioState,
    onRetry: () -> Unit,
    onCoinItemClicked: (id: String, name: String) -> Unit,
    onDiscoverCoinsClicked: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val strings = appStrings()

    val filteredCoins = if (searchQuery.isBlank()) {
        state.coins
    } else {
        state.coins.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.symbol.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenContentPadding(),
        verticalArrangement = Arrangement.spacedBy(Dimens.md),
    ) {
        // ── Header ──
        item {
            ScreenTitle(title = strings.portfolioTitle) {
                FilledTonalButton(onClick = onDiscoverCoinsClicked) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.portfolioDiscoverCoinsButton)
                }
            }
        }

        // ── Partial-error banner (coin list loaded fine, balance fetch failed) ──
        if (state.error != null && state.coins.isNotEmpty()) {
            item {
                ErrorRetryCard(
                    message = stringResource(state.error!!),
                    onRetry = onRetry,
                )
            }
        }

        // ── Balance ──
        item {
            BalanceHeroCard(
                label = strings.portfolioBalanceLabel,
                value = state.totalValue,
                figures = listOf(
                    strings.portfolioCashLabel to state.cashBalance,
                    strings.portfolioHoldingsLabel to state.holdingsValue,
                ),
            )
        }

        if (state.coins.isEmpty()) {
            item {
                KBCard(modifier = Modifier.fillMaxWidth()) {
                    EmptyState(
                        icon = Icons.Default.Wallet,
                        title = strings.portfolioEmptyTitle,
                        subtitle = strings.portfolioEmptySubtitle,
                        actionLabel = strings.portfolioDiscoverCoinsButton,
                        onAction = onDiscoverCoinsClicked,
                    )
                }
            }
            return@LazyColumn
        }

        // ── Distribution ──
        item { SectionHeader(title = strings.portfolioDistributionTitle) }
        item { AllocationCard(state) }

        // ── Your assets ──
        item {
            SectionHeader(
                title = strings.portfolioYourAssets,
                trailingText = strings.portfolioCoinsCount(filteredCoins.size),
            )
        }

        // Search is only useful with more than a handful of coins.
        if (state.coins.size > 4) {
            item {
                SearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = strings.portfolioSearchPlaceholder,
                )
            }
        }

        if (filteredCoins.isEmpty()) {
            item {
                EmptyState(icon = Icons.Default.SearchOff, title = strings.portfolioNoSearchResults)
            }
        } else {
            items(filteredCoins, key = { it.id }) { coin ->
                CoinListItem(
                    coin = coin,
                    onCoinItemClicked = onCoinItemClicked,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

/** Donut + legend: side by side on wide screens, stacked on phones. */
@Composable
private fun AllocationCard(state: PortfolioState) {
    val palette = KBTheme.colors.chartSeries
    val total = state.coins.sumOf { it.amountInFiat }
    val entries = state.coins
        .mapIndexed { index, coin -> Triple(coin, palette[index % palette.size], if (total > 0) coin.amountInFiat / total * 100 else 0.0) }
        .sortedByDescending { it.third }

    KBCard(modifier = Modifier.fillMaxWidth().animateContentSize(), contentPadding = PaddingValues(Dimens.lg)) {
        BoxWithConstraints {
            val wide = maxWidth > 460.dp
            val donut: @Composable () -> Unit = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(if (wide) 180.dp else 200.dp)) {
                    DonutChart(
                        values = entries.map { it.first.amountInFiat.toFloat() },
                        colors = entries.map { it.second },
                        strokeWidth = 44f,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            state.coins.size.toString(),
                            style = MaterialTheme.typography.headlineMedium.tabular(),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            appStrings().dashboardStatAssets,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            val legend: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier, verticalArrangement = Arrangement.spacedBy(Dimens.xs)) {
                    entries.forEach { (coin, color, percent) ->
                        LegendRow(coin.symbol.uppercase(), coin.amountInFiatText, percent, color)
                    }
                }
            }
            if (wide) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    donut()
                    Spacer(Modifier.width(Dimens.xl))
                    legend(Modifier.weight(1f))
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    donut()
                    Spacer(Modifier.height(Dimens.lg))
                    legend(Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun LegendRow(symbol: String, value: String, percent: Double, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(Dimens.xs))
            Text(symbol, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.width(Dimens.xs))
            Text(
                value,
                style = MaterialTheme.typography.bodySmall.tabular(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${(percent * 10).roundToInt() / 10.0}%",
                style = MaterialTheme.typography.labelLarge.tabular(),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                Modifier
                    .fillMaxWidth((percent / 100).toFloat().coerceIn(0f, 1f))
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

@Composable
internal fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = appStrings().actionCancel)
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(Dimens.controlRadius),
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

@Composable
private fun CoinListItem(
    coin: UiPortfolioCoinItem,
    onCoinItemClicked: (id: String, name: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    KBCard(
        modifier = modifier.fillMaxWidth(),
        onClick = { onCoinItemClicked(coin.id, coin.name) },
        contentPadding = PaddingValues(horizontal = Dimens.md, vertical = Dimens.sm + 2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CoinAvatar(iconUrl = coin.iconUrl, symbol = coin.symbol.ifBlank { coin.name })
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
                    text = coin.amountInUnitText,
                    style = MaterialTheme.typography.bodySmall.tabular(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = coin.amountInFiatText,
                    style = MaterialTheme.typography.titleSmall.tabular(),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                ChangePill(text = coin.performancePercentText, isPositive = coin.isPositive)
            }
        }
    }
}
