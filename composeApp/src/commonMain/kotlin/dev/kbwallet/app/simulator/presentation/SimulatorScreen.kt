package dev.kbwallet.app.simulator.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbwallet.app.chart.presentation.component.CandlestickChart
import dev.kbwallet.app.chart.presentation.component.ChartGrid
import dev.kbwallet.app.chart.presentation.util.ChartTransform
import dev.kbwallet.app.core.domain.coin.Coin
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.simulator.domain.ClosedTrade
import dev.kbwallet.app.simulator.domain.PositionSide
import dev.kbwallet.app.simulator.domain.SimPosition
import dev.kbwallet.app.theme.Dimens
import dev.kbwallet.app.theme.KBTheme
import dev.kbwallet.app.theme.component.BackHeader
import dev.kbwallet.app.theme.component.ChangePill
import dev.kbwallet.app.theme.component.CoinAvatar
import dev.kbwallet.app.theme.component.ErrorRetryCard
import dev.kbwallet.app.theme.component.IconBadge
import dev.kbwallet.app.theme.component.KBCard
import dev.kbwallet.app.theme.component.SectionHeader
import dev.kbwallet.app.theme.component.SkeletonList
import dev.kbwallet.app.theme.component.StatCard
import dev.kbwallet.app.theme.component.StatCardSize
import dev.kbwallet.app.theme.component.Tag
import dev.kbwallet.app.theme.component.screenContentPadding
import dev.kbwallet.app.theme.tabular
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.round

/** Format a Double to N decimal places (KMP-safe, no String.format). */
private fun fmtDec(value: Double, decimals: Int): String {
    if (decimals <= 0) return round(value).toLong().toString()
    val factor = listOf(1.0, 10.0, 100.0, 1000.0).getOrElse(decimals) { 1.0 }
    val rounded = round(value * factor) / factor
    val parts = rounded.toString().split('.')
    val intPart = parts[0]
    val fracPart = (parts.getOrElse(1) { "0" }).take(decimals).padEnd(decimals, '0')
    return "$intPart.$fracPart"
}

@Composable
fun SimulatorScreen(
    onBack: () -> Unit,
    viewModel: SimulatorViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = appStrings()

    LaunchedEffect(Unit) {
        viewModel.loadCoins()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BackHeader(
            title = strings.simulatorTitle,
            subtitle = state.selectedCoin?.let { "${it.name} · ${it.symbol.uppercase()}" },
            onBack = onBack,
            actions = {
                if (state.selectedCoin != null) {
                    TextButton(onClick = viewModel::changeCoin) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(strings.simulatorChangeCoin, style = MaterialTheme.typography.labelLarge)
                    }
                }
            },
        )

        when {
            state.isLoading && state.availableCoins.isEmpty() -> SkeletonList(rows = 5, header = false)

            state.error != null && state.availableCoins.isEmpty() && state.selectedCoin == null -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(Dimens.xl),
            ) {
                ErrorRetryCard(message = state.error!!.label(strings), onRetry = viewModel::loadCoins)
            }

            state.selectedCoin == null -> CoinPicker(
                coins = state.availableCoins,
                onSelect = viewModel::selectCoin,
            )

            state.candles.isEmpty() && state.error != null && !state.isLoading -> Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(Dimens.xl),
            ) {
                ErrorRetryCard(
                    message = state.error!!.label(strings),
                    onRetry = { viewModel.selectCoin(state.selectedCoin!!) },
                )
            }

            state.candles.isEmpty() -> SkeletonList(rows = 3)

            else -> SimulationContent(state = state, viewModel = viewModel)
        }
    }
}

@Composable
private fun CoinPicker(coins: List<Coin>, onSelect: (Coin) -> Unit) {
    val strings = appStrings()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenContentPadding(top = 0.dp),
        verticalArrangement = Arrangement.spacedBy(Dimens.itemGap),
    ) {
        item {
            KBCard(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                border = null,
                contentPadding = PaddingValues(Dimens.lg),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    IconBadge(Icons.AutoMirrored.Filled.TrendingUp, size = 44.dp, iconSize = 22.dp)
                    Spacer(Modifier.width(Dimens.md))
                    Text(
                        strings.simulatorIntro,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        item { SectionHeader(title = strings.simulatorSelectCoinPrompt.trimEnd(':')) }
        items(coins, key = { it.id }) { coin ->
            KBCard(
                modifier = Modifier.fillMaxWidth().animateItem(),
                onClick = { onSelect(coin) },
                contentPadding = PaddingValues(horizontal = Dimens.md, vertical = Dimens.sm),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoinAvatar(iconUrl = coin.iconUrl, symbol = coin.symbol, size = 38.dp)
                    Spacer(Modifier.width(Dimens.sm))
                    Column(Modifier.weight(1f)) {
                        Text(coin.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            coin.symbol.uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulationContent(state: SimulatorState, viewModel: SimulatorViewModel) {
    val strings = appStrings()
    val candle = state.candles.getOrNull(state.currentCandleIndex)
    val firstClose = state.candles.firstOrNull()?.close ?: 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = screenContentPadding(top = 0.dp),
        verticalArrangement = Arrangement.spacedBy(Dimens.md),
    ) {
        // ── Price + chart + playback ──
        item {
            KBCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(Dimens.md)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            candle?.let { formatFiat(it.close) } ?: "—",
                            style = MaterialTheme.typography.headlineMedium.tabular(),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            strings.simulatorCandleCounter(state.currentCandleIndex + 1, state.candles.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (candle != null && firstClose > 0) {
                        val change = (candle.close - firstClose) / firstClose * 100
                        ChangePill("${fmtDec(change, 2)}%", isPositive = change >= 0)
                    }
                }

                Spacer(Modifier.height(Dimens.sm))
                val transform = remember(state.candles, state.currentCandleIndex) {
                    val window = 60.coerceAtMost(state.candles.size)
                    val start = (state.currentCandleIndex - window / 2).coerceIn(0, state.candles.size - window)
                    val end = start + window
                    ChartTransform(state.candles, start.toFloat() / state.candles.size, end.toFloat() / state.candles.size)
                }
                Box(modifier = Modifier.fillMaxWidth().height(230.dp)) {
                    ChartGrid(transform = transform)
                    CandlestickChart(
                        transform = transform,
                        bullColor = KBTheme.colors.profitGreen,
                        bearColor = KBTheme.colors.lossRed,
                        chartHeightFraction = 0.86f,
                    )
                }

                val progress by animateFloatAsState(
                    (state.currentCandleIndex + 1).toFloat() / state.candles.size.coerceAtLeast(1),
                    label = "replayProgress",
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )

                Spacer(Modifier.height(Dimens.sm))
                PlaybackControls(state = state, viewModel = viewModel)
            }
        }

        // ── Account ──
        item {
            // Round to cents to hide floating point noise.
            val pnl = round((state.equity - state.initialBalance) * 100) / 100
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap),
            ) {
                StatCard(strings.simulatorStatBalance, formatFiat(state.cashBalance), Modifier.weight(1f), size = StatCardSize.Compact)
                StatCard(strings.simulatorStatEquity, formatFiat(state.equity), Modifier.weight(1f), size = StatCardSize.Compact)
                StatCard(strings.simulatorStatPnl, formatFiat(pnl), Modifier.weight(1f), KBTheme.trend(pnl >= 0), size = StatCardSize.Compact)
            }
        }

        // ── Hint ──
        if (state.activeHint != null) {
            item {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.tileRadius))
                        .background(KBTheme.colors.warningContainer)
                        .clickable(onClick = viewModel::nextHint)
                        .padding(Dimens.md),
                ) {
                    Icon(Icons.Default.Lightbulb, strings.simulatorHintContentDesc, tint = KBTheme.colors.warning, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(Dimens.sm))
                    Text(state.activeHint.removePrefix("💡").trim(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // ── Order form ──
        item { OrderForm(state = state, viewModel = viewModel) }

        // ── Open positions ──
        if (state.positions.isNotEmpty()) {
            item { SectionHeader(title = strings.simulatorOpenPositions(state.positions.size)) }
            items(state.positions, key = { it.id }) { pos ->
                PositionCard(pos = pos, onClose = { viewModel.closePosition(pos.id) }, modifier = Modifier.animateItem())
            }
        }

        // ── Metrics ──
        if (state.closedTrades.isNotEmpty()) {
            val m = state.metrics
            item { SectionHeader(title = strings.simulatorMetricsTitle) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap), modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
                        StatCard(strings.simulatorMetricWinRate, "${fmtDec(m.winRate * 100, 0)}%", Modifier.weight(1f), MaterialTheme.colorScheme.primary, size = StatCardSize.Compact)
                        StatCard(strings.simulatorMetricProfitFactor, fmtDec(m.profitFactor, 2), Modifier.weight(1f), size = StatCardSize.Compact)
                        StatCard(strings.simulatorMetricTrades, "${m.totalTrades}", Modifier.weight(1f), size = StatCardSize.Compact)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap), modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
                        StatCard(strings.simulatorMetricMaxDD, formatFiat(m.maxDrawdown), Modifier.weight(1f), KBTheme.colors.lossRed, size = StatCardSize.Compact)
                        StatCard(strings.simulatorMetricBest, formatFiat(m.bestTrade), Modifier.weight(1f), KBTheme.colors.profitGreen, size = StatCardSize.Compact)
                        StatCard(strings.simulatorMetricSharpe, fmtDec(m.sharpeRatio, 2), Modifier.weight(1f), size = StatCardSize.Compact)
                    }
                }
            }
            item { SectionHeader(title = strings.simulatorTradeHistoryTitle) }
            items(state.closedTrades.reversed(), key = { "closed-${it.id}" }) { trade ->
                ClosedTradeCard(trade, modifier = Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun PlaybackControls(state: SimulatorState, viewModel: SimulatorViewModel) {
    val strings = appStrings()
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = viewModel::stepBackward) {
            Icon(Icons.Default.SkipPrevious, strings.simulatorPrevContentDesc)
        }
        FilledIconButton(
            onClick = viewModel::togglePlay,
            modifier = Modifier.size(52.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                if (state.isPlaying) strings.simulatorPauseContentDesc else strings.simulatorPlayContentDesc,
                modifier = Modifier.size(28.dp),
            )
        }
        IconButton(onClick = viewModel::stepForward) {
            Icon(Icons.Default.SkipNext, strings.simulatorNextContentDesc)
        }
        Spacer(Modifier.weight(1f))
        Segmented(
            options = PlaySpeed.entries,
            selected = state.playSpeed,
            label = { it.label },
            onSelect = viewModel::setPlaySpeed,
            accent = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(200.dp),
        )
    }
}

@Composable
private fun <T> Segmented(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    accent: Color,
    modifier: Modifier = Modifier,
    selectedAccent: (T) -> Color = { accent },
    height: androidx.compose.ui.unit.Dp = 34.dp,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val color = selectedAccent(option)
            val bg by animateColorAsState(
                if (isSelected) color.copy(alpha = 0.18f) else Color.Transparent,
                label = "segment",
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .clip(RoundedCornerShape(9.dp))
                    .background(bg)
                    .clickable(role = Role.Tab) { onSelect(option) },
            ) {
                Text(
                    label(option),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun OrderForm(state: SimulatorState, viewModel: SimulatorViewModel) {
    val strings = appStrings()
    val isLong = state.orderSide == OrderSideInput.LONG
    val sideColor = KBTheme.trend(isLong)
    val longColor = KBTheme.colors.profitGreen
    val shortColor = KBTheme.colors.lossRed

    KBCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(Dimens.md)) {
        Text(strings.simulatorNewPositionTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(Dimens.sm))
        Segmented(
            options = OrderSideInput.entries,
            selected = state.orderSide,
            label = { it.label },
            onSelect = viewModel::onOrderSideChanged,
            accent = sideColor,
            selectedAccent = { if (it == OrderSideInput.LONG) longColor else shortColor },
            height = 40.dp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Dimens.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
            OrderField(strings.simulatorAmountLabel, state.orderAmount, viewModel::onOrderAmountChanged, Modifier.weight(1f))
            OrderField(strings.simulatorLeverageLabel, state.orderLeverage, viewModel::onOrderLeverageChanged, Modifier.weight(1f))
        }

        // Leverage shrinks the adverse move that wipes out the margin; show where that is.
        val previewLeverage = state.orderLeverage.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 1.0
        val previewEntry = state.candles.getOrNull(state.currentCandleIndex)?.close
        if (previewLeverage > 1.0 && previewEntry != null) {
            val move = previewEntry / previewLeverage
            val liqPreview = if (isLong) (previewEntry - move).coerceAtLeast(0.0) else previewEntry + move
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = Dimens.xs)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(KBTheme.colors.lossContainer)
                    .padding(horizontal = Dimens.sm, vertical = Dimens.xs),
            ) {
                Icon(Icons.Default.Warning, null, tint = KBTheme.colors.lossRed, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    strings.simulatorLiquidatesAt(formatFiat(liqPreview), fmtDec(100.0 / previewLeverage, 1)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(Modifier.height(Dimens.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.itemGap)) {
            OrderField(strings.simulatorStopLossLabel, state.orderStopLoss, viewModel::onOrderSLChanged, Modifier.weight(1f), accent = KBTheme.colors.lossRed)
            OrderField(strings.simulatorTakeProfitLabel, state.orderTakeProfit, viewModel::onOrderTPChanged, Modifier.weight(1f), accent = KBTheme.colors.profitGreen)
        }
        Spacer(Modifier.height(Dimens.md))
        Button(
            onClick = viewModel::openPosition,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = sideColor,
                contentColor = if (isLong) MaterialTheme.colorScheme.onPrimary else Color.White,
            ),
        ) {
            Icon(
                if (isLong) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(Dimens.xs))
            Text(if (isLong) strings.simulatorLongAtMarket else strings.simulatorShortAtMarket, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun OrderField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    prefix: String? = null,
    suffix: String? = null,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        prefix = prefix?.let { { Text(it) } },
        suffix = suffix?.let { { Text(it) } },
        singleLine = true,
        textStyle = MaterialTheme.typography.titleSmall.tabular(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(Dimens.controlRadius),
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedLabelColor = accent,
            cursorColor = accent,
        ),
    )
}

@Composable
private fun PositionCard(pos: SimPosition, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val strings = appStrings()
    val isLong = pos.side == PositionSide.LONG
    KBCard(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(Dimens.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tag(pos.side.name, color = KBTheme.trend(isLong))
            Spacer(Modifier.width(Dimens.xs))
            Text(pos.coinSymbol.uppercase(), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.width(Dimens.xs))
            Text(
                formatFiat(pos.amountInFiat),
                style = MaterialTheme.typography.bodySmall.tabular(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatFiat(pos.pnl),
                    style = MaterialTheme.typography.titleSmall.tabular(),
                    color = KBTheme.trend(pos.pnl >= 0),
                )
                ChangePill("${fmtDec(pos.pnlPercent, 1)}%", isPositive = pos.pnl >= 0, filled = false)
            }
        }
        Spacer(Modifier.height(Dimens.xs))
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
            MiniFigure(strings.simulatorEntryLabel(formatFiat(pos.entryPrice)))
            MiniFigure(strings.simulatorNowLabel(formatFiat(pos.currentPrice)))
        }
        if (pos.stopLoss != null || pos.takeProfit != null) {
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                if (pos.stopLoss != null) MiniFigure(strings.simulatorSlLabel(formatFiat(pos.stopLoss)), KBTheme.colors.lossRed)
                if (pos.takeProfit != null) MiniFigure(strings.simulatorTpLabel(formatFiat(pos.takeProfit)), KBTheme.colors.profitGreen)
            }
        }
        Spacer(Modifier.height(Dimens.sm))
        OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth().height(40.dp)) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(strings.simulatorCloseButton, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun RowScope.MiniFigure(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(text, style = MaterialTheme.typography.bodySmall.tabular(), color = color)
}

@Composable
private fun ClosedTradeCard(trade: ClosedTrade, modifier: Modifier = Modifier) {
    val strings = appStrings()
    val won = trade.pnl >= 0
    KBCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Dimens.md, vertical = Dimens.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(
                icon = if (trade.side == PositionSide.LONG) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                tint = KBTheme.trend(won),
                shape = CircleShape,
                size = 36.dp,
                iconSize = 18.dp,
            )
            Spacer(Modifier.width(Dimens.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${trade.side.name} ${trade.coinName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    strings.simulatorEntryExitLabel(formatFiat(trade.entryPrice), formatFiat(trade.exitPrice)),
                    style = MaterialTheme.typography.bodySmall.tabular(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatFiat(trade.pnl), style = MaterialTheme.typography.titleSmall.tabular(), color = KBTheme.trend(won))
                Text(
                    strings.simulatorExitReason(trade.exitReason.name),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
