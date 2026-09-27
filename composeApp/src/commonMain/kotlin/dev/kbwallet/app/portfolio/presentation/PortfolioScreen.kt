package dev.kbwallet.app.portfolio.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlin.math.roundToInt
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.portfolio.presentation.component.DonutChart
import dev.kbwallet.app.theme.LocalKBLearningColorsPalette
import dev.kbwallet.app.theme.component.ErrorRetryCard
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val SubtextGray = Color(0xFFAAAAAA)

@Composable
fun PortfolioScreen(
    onCoinItemClicked: (String) -> Unit,
    onDiscoverCoinsClicked: () -> Unit,
) {
    val portfolioViewModel = koinViewModel<PortfolioViewModel>()
    val state by portfolioViewModel.state.collectAsStateWithLifecycle()

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
    } else if (state.error != null && state.coins.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            ErrorRetryCard(
                message = stringResource(state.error!!),
                onRetry = { portfolioViewModel.retry() },
                modifier = Modifier.padding(24.dp),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PortfolioContent(
    state: PortfolioState,
    onRetry: () -> Unit,
    onCoinItemClicked: (String) -> Unit,
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
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Header ──
        item {
            Text(
                text = strings.portfolioTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
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

        // ── Balance Section ──
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.portfolioBalanceLabel,
                        color = SubtextGray,
                        fontSize = 13.sp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.totalValue,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // The headline above is cash + holdings; without this split
                    // there was no way to tell how much of it is still
                    // spendable and how much is tied up in coins.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        BalanceBreakdownItem(
                            label = strings.portfolioCashLabel,
                            value = state.cashBalance,
                        )
                        BalanceBreakdownItem(
                            label = strings.portfolioHoldingsLabel,
                            value = state.holdingsValue,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDiscoverCoinsClicked,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(text = strings.portfolioDiscoverCoinsButton)
                    }
                }
            }
        }

        // ── Search Bar ──
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.portfolioSearchPlaceholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = SubtextGray,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }

        // ── Portfolio Distribution ──
        item {
            Text(
                text = strings.portfolioDistributionTitle,
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
                        RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp)
            ) {
                if (state.coins.isEmpty()) {
                    Text(
                        text = strings.portfolioNothingToShow,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubtextGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Donut Chart
                        val chartValues = state.coins.map { it.amountInFiat.toFloat() }
                        val chartColors = listOf(
                            Color(0xFF00E676), // bright green
                            Color(0xFF69F0AE), // light mint
                            Color(0xFF00C853), // emerald
                            Color(0xFFB2FF59), // lime
                            Color(0xFF1DE9B6), // teal-green
                            Color(0xFF76FF03), // acid green
                            Color(0xFF00BFA5), // turquoise-green
                            Color(0xFF64DD17), // leaf green
                            Color(0xFFAEEA00), // yellow-green
                            Color(0xFF009688), // dark teal
                            Color(0xFF2E7D32), // forest green
                            Color(0xFF81C784), // soft sage
                        )
                        DonutChart(
                            values = chartValues,
                            colors = chartColors,
                            strokeWidth = 40f,
                            // fillMaxWidth(0.7f) alone sized this off the
                            // available container width with no ceiling — on
                            // a phone that's a reasonable ~250dp circle, but
                            // on the desktop target's much wider window it
                            // blew up to take up most of the screen.
                            // NB: chaining `.fillMaxWidth(0.7f).widthIn(max=X)`
                            // does NOT cap it — fillMaxWidth already pins
                            // minWidth == maxWidth on the incoming constraints,
                            // and widthIn's own max gets coerced back up into
                            // that fixed [min,max] range, making it a no-op.
                            // widthIn(max) alone is sufficient: DonutChart's
                            // Canvas uses Modifier.aspectRatio(1f), which by
                            // itself already expands to fill the available
                            // (now-capped) width.
                            modifier = Modifier
                                .widthIn(max = 240.dp)
                                .padding(16.dp),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Legend
                        val totalValue = state.coins.sumOf { it.amountInFiat }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            state.coins.forEachIndexed { index, coin ->
                                val percentage = if (totalValue > 0)
                                    (coin.amountInFiat / totalValue * 100)
                                else 0.0

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                chartColors[index % chartColors.size],
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${coin.symbol}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${(percentage * 10).roundToInt() / 10.0}%",
                                        fontSize = 13.sp,
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

        // ── Your Assets ──
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = strings.portfolioYourAssets,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.portfolioCoinsCount(filteredCoins.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SubtextGray,
                )
            }
        }

        if (filteredCoins.isEmpty() && searchQuery.isNotBlank()) {
            item {
                Text(
                    text = strings.portfolioNoSearchResults,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SubtextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
            }
        } else if (state.coins.isEmpty()) {
            item {
                PortfolioEmptySection(onDiscoverCoinsClicked = onDiscoverCoinsClicked)
            }
        } else {
            items(filteredCoins) { coin ->
                CoinListItem(
                    coin = coin,
                    onCoinItemClicked = onCoinItemClicked,
                )
            }
        }
    }
}

@Composable
private fun CoinListItem(
    coin: UiPortfolioCoinItem,
    onCoinItemClicked: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
            .clickable { onCoinItemClicked(coin.id) }
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFF2A2A2A), CircleShape)
                .padding(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = coin.iconUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.clip(CircleShape).size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = coin.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = coin.symbol.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = SubtextGray,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = coin.amountInFiatText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "${if (coin.isPositive) "↗ " else "↘ "}${coin.performancePercentText}",
                style = MaterialTheme.typography.bodySmall,
                color = if (coin.isPositive)
                    LocalKBLearningColorsPalette.current.profitGreen
                else
                    LocalKBLearningColorsPalette.current.lossRed,
            )
        }
    }
}

@Composable
private fun PortfolioEmptySection(
    onDiscoverCoinsClicked: () -> Unit,
) {
    val strings = appStrings()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(20.dp)
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = strings.portfolioEmptyTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = strings.portfolioEmptySubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = SubtextGray,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDiscoverCoinsClicked,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(text = strings.portfolioDiscoverCoinsButton)
            }
        }
    }
}

/**
 * One half of the balance headline's breakdown — cash on the left, holdings on
 * the right, so the total above is visibly the sum of two separate things.
 */
@Composable
private fun BalanceBreakdownItem(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = SubtextGray,
            fontSize = 11.sp,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
