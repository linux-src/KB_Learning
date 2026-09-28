package dev.kbwallet.app.dashboard.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.ui.graphics.luminance
import dev.kbwallet.app.theme.mode.ThemeController
import dev.kbwallet.app.theme.mode.ThemeMode
import org.koin.compose.koinInject
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.theme.Dimens
import dev.kbwallet.app.theme.KBTheme
import dev.kbwallet.app.theme.component.BalanceHeroCard
import dev.kbwallet.app.theme.component.ChangePill
import dev.kbwallet.app.theme.component.CoinAvatar
import dev.kbwallet.app.theme.component.EmptyState
import dev.kbwallet.app.theme.component.ErrorRetryCard
import dev.kbwallet.app.theme.component.IconBadge
import dev.kbwallet.app.theme.component.KBCard
import dev.kbwallet.app.theme.component.RowDivider
import dev.kbwallet.app.theme.component.ScreenTitle
import dev.kbwallet.app.theme.component.SectionHeader
import dev.kbwallet.app.theme.component.SkeletonList
import dev.kbwallet.app.theme.component.screenContentPadding
import dev.kbwallet.app.theme.tabular
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    onDiscoverCoinsClicked: () -> Unit,
    onCoinItemClicked: (id: String, name: String) -> Unit,
    onSimulatorClicked: () -> Unit = {},
    onLibraryClicked: () -> Unit = {},
    onAnalyticsClicked: () -> Unit = {},
    onSeeAllAssetsClicked: () -> Unit = {},
) {
    val viewModel = koinViewModel<DashboardViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        SkeletonList(rows = 4)
    } else {
        DashboardContent(
            state = state,
            onRetry = { viewModel.retry() },
            onDiscoverCoinsClicked = onDiscoverCoinsClicked,
            onCoinItemClicked = onCoinItemClicked,
            onSimulatorClicked = onSimulatorClicked,
            onLibraryClicked = onLibraryClicked,
            onAnalyticsClicked = onAnalyticsClicked,
            onSeeAllAssetsClicked = onSeeAllAssetsClicked,
        )
    }
}

@Composable
private fun DashboardContent(
    state: DashboardState,
    onRetry: () -> Unit,
    onDiscoverCoinsClicked: () -> Unit,
    onCoinItemClicked: (id: String, name: String) -> Unit,
    onSimulatorClicked: () -> Unit,
    onLibraryClicked: () -> Unit,
    onAnalyticsClicked: () -> Unit,
    onSeeAllAssetsClicked: () -> Unit,
) {
    val strings = appStrings()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenContentPadding(),
        verticalArrangement = Arrangement.spacedBy(Dimens.md),
    ) {
        item {
            ScreenTitle(title = strings.dashboardTitle, subtitle = strings.dashboardGreeting) {
                ThemeToggleButton()
            }
        }

        // ── Error banner (data partially failed to load) ──
        if (state.error != null) {
            item {
                ErrorRetryCard(
                    message = stringResource(state.error!!),
                    onRetry = onRetry,
                )
            }
        }

        // ── Balance hero ──
        item {
            BalanceHero(state)
        }

        // ── Quick actions ──
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            ) {
                QuickAction(Icons.Default.AddCircle, strings.dashboardActionBuy, onDiscoverCoinsClicked)
                QuickAction(Icons.AutoMirrored.Filled.ShowChart, strings.dashboardSimulatorButton, onSimulatorClicked)
                QuickAction(Icons.AutoMirrored.Filled.MenuBook, strings.dashboardActionLearn, onLibraryClicked)
                QuickAction(Icons.Default.Insights, strings.dashboardActionHistory, onAnalyticsClicked)
            }
        }

        // ── Your assets ──
        item {
            SectionHeader(
                title = strings.dashboardPortfolioSummary,
                trailingText = if (state.coinCount > 0) state.coinCount.toString() else null,
                actionLabel = if (state.coinCount > 0) strings.actionSeeAll else null,
                onAction = onSeeAllAssetsClicked,
            )
        }
        item {
            KBCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = Dimens.xxs)) {
                if (state.portfolioSummaryCoins.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Wallet,
                        title = strings.dashboardNoAssetsTitle,
                        subtitle = strings.portfolioEmptySubtitle,
                        actionLabel = strings.portfolioDiscoverCoinsButton,
                        onAction = onDiscoverCoinsClicked,
                    )
                } else {
                    state.portfolioSummaryCoins.forEachIndexed { index, coin ->
                        if (index > 0) RowDivider()
                        CoinRow(coin = coin, onClick = { onCoinItemClicked(coin.id, coin.name) })
                    }
                }
            }
        }

        // ── Market ──
        if (state.topCoins.isNotEmpty()) {
            item {
                SectionHeader(
                    title = strings.coinsListTitle,
                    actionLabel = strings.actionSeeAll,
                    onAction = onDiscoverCoinsClicked,
                )
            }
            item {
                KBCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = Dimens.xxs)) {
                    state.topCoins.forEachIndexed { index, coin ->
                        if (index > 0) RowDivider()
                        CoinRow(coin = coin, onClick = { onCoinItemClicked(coin.id, coin.name) })
                    }
                }
            }
        }

        // ── Learn & practice ──
        item {
            SectionHeader(title = strings.dashboardMarketOverview)
        }
        item {
            KBCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onLibraryClicked,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                border = null,
                contentPadding = PaddingValues(Dimens.lg),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.AutoMirrored.Filled.MenuBook, size = 48.dp, iconSize = 24.dp)
                    Spacer(Modifier.width(Dimens.md))
                    Column(Modifier.weight(1f)) {
                        Text(
                            strings.dashboardLibraryTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            strings.dashboardLibrarySubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            KBCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(Dimens.lg)) {
                Row(verticalAlignment = Alignment.Top) {
                    IconBadge(
                        Icons.Default.Lightbulb,
                        tint = KBTheme.colors.warning,
                        size = 48.dp,
                        iconSize = 24.dp,
                    )
                    Spacer(Modifier.width(Dimens.md))
                    Column(Modifier.weight(1f)) {
                        Text(
                            strings.dashboardTradingTipTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            strings.dashboardTradingTipBody,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceHero(state: DashboardState) {
    val strings = appStrings()
    BalanceHeroCard(
        label = strings.dashboardStatTotalBalance,
        value = state.totalValue,
        figures = listOf(
            strings.dashboardStatCash to state.cashBalance,
            strings.dashboardStatPortfolioValue to state.holdingsValue,
        ),
        change = if (state.coinCount > 0) state.recentPerformance else null,
        changePositive = state.isPerformancePositive,
    )
}

@Composable
private fun RowScope.QuickAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(Dimens.tileRadius))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = Dimens.md, horizontal = Dimens.xxs),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CoinRow(coin: DashboardCoinItem, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Dimens.md, vertical = Dimens.sm),
    ) {
        CoinAvatar(iconUrl = coin.iconUrl, symbol = coin.symbol, size = 40.dp)
        Spacer(Modifier.width(Dimens.sm))
        Column(Modifier.weight(1f)) {
            Text(
                coin.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                coin.symbol.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                coin.formattedPrice,
                style = MaterialTheme.typography.titleSmall.tabular(),
                color = MaterialTheme.colorScheme.onSurface,
            )
            ChangePill(text = coin.formattedChange, isPositive = coin.isPositive, filled = false)
        }
    }
}

@Composable
private fun ThemeToggleButton() {
    val controller = koinInject<ThemeController>()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    FilledTonalIconButton(
        onClick = { controller.setMode(if (isDark) ThemeMode.LIGHT else ThemeMode.DARK) },
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        AnimatedContent(
            targetState = isDark,
            transitionSpec = {
                (fadeIn(tween(200)) + scaleIn(initialScale = 0.6f)) togetherWith
                    (fadeOut(tween(150)) + scaleOut(targetScale = 0.6f))
            },
            label = "themeIcon",
        ) { dark ->
            Icon(
                imageVector = if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = appStrings().themeToggle,
            )
        }
    }
}
