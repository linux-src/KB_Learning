package dev.kbwallet.app.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.theme.LocalKBLearningColorsPalette
import dev.kbwallet.app.theme.component.ErrorRetryCard
import dev.kbwallet.app.theme.component.StatCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    onDiscoverCoinsClicked: () -> Unit,
    onCoinItemClicked: (String) -> Unit,
    onSimulatorClicked: () -> Unit = {},
    onLibraryClicked: () -> Unit = {},
) {
    val viewModel = koinViewModel<DashboardViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    } else {
        DashboardContent(
            state = state,
            onRetry = { viewModel.retry() },
            onDiscoverCoinsClicked = onDiscoverCoinsClicked,
            onCoinItemClicked = onCoinItemClicked,
            onSimulatorClicked = onSimulatorClicked,
            onLibraryClicked = onLibraryClicked,
        )
    }
}

@Composable
private fun DashboardContent(
    state: DashboardState,
    onRetry: () -> Unit,
    onDiscoverCoinsClicked: () -> Unit,
    onCoinItemClicked: (String) -> Unit,
    onSimulatorClicked: () -> Unit = {},
    onLibraryClicked: () -> Unit = {},
) {
    val strings = appStrings()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Error banner (data partially failed to load) ──
        if (state.error != null) {
            item {
                ErrorRetryCard(
                    message = stringResource(state.error!!),
                    onRetry = onRetry,
                )
            }
        }

        // ── Header ──
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.dashboardTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = onSimulatorClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(strings.dashboardSimulatorButton, fontSize = 12.sp)
                }
            }
        }

        // ── Balance Row ──
        // The two money figures get a row of their own: the top card used to be
        // labelled "Portfolio Value" while actually showing cash + holdings,
        // and the cash had no card at all.
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
            ) {
                StatCard(
                    title = strings.dashboardStatTotalBalance,
                    value = state.totalValue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = strings.dashboardStatCash,
                    value = state.cashBalance,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Stats Row ──
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
            ) {
                StatCard(
                    title = strings.dashboardStatPortfolioValue,
                    value = state.holdingsValue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = strings.dashboardStatAssets,
                    value = state.coinCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = strings.dashboardStat24hChange,
                    value = state.recentPerformance,
                    modifier = Modifier.weight(1f),
                    valueColor = if (state.isPerformancePositive)
                        LocalKBLearningColorsPalette.current.profitGreen
                    else
                        LocalKBLearningColorsPalette.current.lossRed,
                )
            }
        }

        // ── Market Overview ──
        item {
            Text(
                text = strings.dashboardMarketOverview,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // ── Trading Tip ──
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = strings.dashboardTradingTipTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.dashboardTradingTipBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                    )
                }
            }
        }

        // ── Crypto Library ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable(onClick = onLibraryClicked)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.dashboardLibraryTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.dashboardLibrarySubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "→",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        // ── Portfolio Summary ──
        item {
            Text(
                text = strings.dashboardPortfolioSummary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                if (state.portfolioSummaryCoins.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.dashboardNoAssetsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.dashboardNoAssetsSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Column {
                        Text(
                            text = strings.dashboardAssetsCount(state.portfolioSummaryCoins.size),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        state.portfolioSummaryCoins.forEach { coin ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCoinItemClicked(coin.id) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (coin.isPositive) "↗" else "↘",
                                    color = if (coin.isPositive)
                                        LocalKBLearningColorsPalette.current.profitGreen
                                    else
                                        LocalKBLearningColorsPalette.current.lossRed,
                                    fontSize = 20.sp,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = coin.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = coin.formattedPrice,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
