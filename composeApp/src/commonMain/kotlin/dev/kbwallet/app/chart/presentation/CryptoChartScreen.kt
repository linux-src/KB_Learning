package dev.kbwallet.app.chart.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.IconButtonDefaults
import dev.kbwallet.app.theme.Dimens
import dev.kbwallet.app.theme.KBTheme
import dev.kbwallet.app.theme.tabular
import dev.kbwallet.app.theme.component.ChangePill
import dev.kbwallet.app.theme.component.SkeletonBlock
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kbwallet.app.core.i18n.appStrings
import dev.kbwallet.app.chart.domain.model.CandleModel
import dev.kbwallet.app.chart.domain.model.isBullish
import dev.kbwallet.app.chart.presentation.component.CandlestickChart
import dev.kbwallet.app.chart.presentation.component.ChartGrid
import dev.kbwallet.app.chart.presentation.component.LineChart
import dev.kbwallet.app.chart.presentation.component.SMAOverlay
import dev.kbwallet.app.chart.presentation.component.TimeRangeSelector
import dev.kbwallet.app.chart.presentation.util.ChartFormatters
import dev.kbwallet.app.chart.presentation.util.ChartTransform
import dev.kbwallet.app.theme.component.ErrorRetryCard
import dev.kbwallet.app.trade.presentation.buy.BuyScreen
import dev.kbwallet.app.trade.presentation.common.TradeType
import dev.kbwallet.app.trade.presentation.sell.SellScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoChartScreen(
    coinId: String,
    coinName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChartViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = appStrings()

    var showTradeSheet by remember { mutableStateOf(false) }
    var sheetTradeType by remember { mutableStateOf(TradeType.BUY) }

    LaunchedEffect(coinId) {
        viewModel.init(coinId, coinName)
    }

    val trendColor = KBTheme.trend(state.priceChange >= 0)
    val bullColor = KBTheme.colors.profitGreen
    val bearColor = KBTheme.colors.lossRed
    val smaColor = KBTheme.colors.chartAccent

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.md)
                .padding(bottom = 88.dp) // padding for bottom buttons
        ) {
            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = Dimens.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, strings.actionBack)
                }
                Spacer(Modifier.width(Dimens.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.coinName.ifEmpty { coinName },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (state.currentPrice > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ChartFormatters.formatPrice(state.currentPrice),
                                style = MaterialTheme.typography.headlineMedium.tabular(),
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Spacer(Modifier.width(Dimens.sm))
                            ChangePill(
                                text = "${twoDec(state.priceChangePercent)}%",
                                isPositive = state.priceChangePercent >= 0,
                            )
                        }
                    } else {
                        SkeletonBlock(Modifier.padding(top = 4.dp).width(160.dp).height(28.dp))
                    }
                }
            }

            Spacer(Modifier.padding(vertical = 4.dp))

            // ── Time range selector ──
            TimeRangeSelector(
                selected = state.selectedRange,
                onSelect = { viewModel.selectTimeRange(it) },
            )

            Spacer(Modifier.padding(vertical = 4.dp))

            // ── Chart mode + indicators ──
            // Candles lead: they're the primary view, the line chart is the
            // simplified alternative.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ChartChip(
                    label = strings.chartModeCandles,
                    selected = state.isCandlestickMode,
                    accent = trendColor,
                    onClick = { viewModel.setChartMode(candlestick = true) },
                )
                ChartChip(
                    label = strings.chartModeLine,
                    selected = !state.isCandlestickMode,
                    accent = trendColor,
                    onClick = { viewModel.setChartMode(candlestick = false) },
                )
                Spacer(Modifier.weight(1f))
                ChartChip(
                    label = "SMA ${state.smaPeriod}",
                    selected = state.showSma,
                    accent = smaColor,
                    onClick = { viewModel.toggleSma() },
                )
            }

            Spacer(Modifier.padding(vertical = 2.dp))

            // ── OHLC readout for the tapped (or latest) candle ──
            OhlcReadout(
                candle = state.focusedCandle,
                bullColor = bullColor,
                bearColor = bearColor,
            )

            Spacer(Modifier.padding(vertical = 4.dp))

            // ── Chart ──
            val transform = remember(state.candles, state.selectedRange) {
                ChartTransform(state.candles)
            }
            val xAxisLabel = remember(state.selectedRange) {
                ChartFormatters.axisLabelFor(state.selectedRange)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.cardRadius))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, KBTheme.colors.hairline, RoundedCornerShape(Dimens.cardRadius))
                    .padding(Dimens.xs),
            ) {
                if (state.isLoading) {
                    SkeletonBlock(Modifier.fillMaxSize(), RoundedCornerShape(Dimens.tileRadius))
                } else if (state.candles.isNotEmpty()) {
                    ChartGrid(transform = transform)

                    if (state.isCandlestickMode) {
                        CandlestickChart(
                            transform = transform,
                            bullColor = bullColor,
                            bearColor = bearColor,
                            crosshairIndex = state.crosshairIndex,
                            onCrosshair = { viewModel.onCrosshair(it) },
                            xAxisLabel = xAxisLabel,
                        )
                    } else {
                        LineChart(transform = transform, lineColor = trendColor)

                        // The candlestick view draws its own price axis; the line
                        // view only gets the high/low markers.
                        PriceLabels(transform = transform)
                    }

                    if (state.showSma) {
                        SMAOverlay(
                            transform = transform,
                            smaValues = state.smaValues,
                            color = smaColor,
                        )
                    }
                } else if (state.error != null) {
                    ErrorRetryCard(
                        message = stringResource(state.error!!),
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp),
                    )
                }
            }
        }

        // ── Sticky Bottom Buttons ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(KBTheme.colors.hairline))
            Row(
                modifier = Modifier.fillMaxWidth().padding(Dimens.md),
                horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
            ) {
                Button(
                    onClick = {
                        sheetTradeType = TradeType.BUY
                        showTradeSheet = true
                    },
                    modifier = Modifier.weight(1f).height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = bullColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.tradeBuyButton, style = MaterialTheme.typography.titleSmall)
                }

                Button(
                    onClick = {
                        sheetTradeType = TradeType.SELL
                        showTradeSheet = true
                    },
                    modifier = Modifier.weight(1f).height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = bearColor,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.tradeSellButton, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }

    // ── Trade Bottom Sheet ──
    if (showTradeSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showTradeSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            if (sheetTradeType == TradeType.BUY) {
                BuyScreen(
                    coinId = coinId,
                    onSuccess = { showTradeSheet = false },
                    onBack = { showTradeSheet = false }
                )
            } else {
                SellScreen(
                    coinId = coinId,
                    onSuccess = { showTradeSheet = false },
                    onBack = { showTradeSheet = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartChip(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = accent.copy(alpha = 0.16f),
            selectedLabelColor = accent,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = KBTheme.colors.hairline,
            selectedBorderColor = accent.copy(alpha = 0.4f),
        ),
    )
}

/**
 * Open / High / Low / Close of the candle under the crosshair — the reason to
 * look at candles rather than a line in the first place.
 */
@Composable
private fun OhlcReadout(
    candle: CandleModel?,
    bullColor: Color,
    bearColor: Color,
) {
    if (candle == null) return
    val closeColor = if (candle.isBullish) bullColor else bearColor
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        // Five monospace columns don't fit a narrow phone at every price
        // magnitude — let the row scroll rather than clipping the close.
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = ChartFormatters.formatDateTime(candle.openTime),
            fontSize = 10.sp,
            style = MaterialTheme.typography.labelSmall.tabular(),
            color = mutedColor.copy(alpha = 0.7f),
        )
        OhlcValue("O", candle.open, mutedColor, mutedColor)
        OhlcValue("H", candle.high, mutedColor, bullColor)
        OhlcValue("L", candle.low, mutedColor, bearColor)
        OhlcValue("C", candle.close, mutedColor, closeColor)
    }
}

@Composable
private fun OhlcValue(
    label: String,
    value: Double,
    labelColor: Color,
    valueColor: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 10.sp,
            style = MaterialTheme.typography.labelSmall.tabular(),
            color = labelColor.copy(alpha = 0.6f),
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text = ChartFormatters.formatPrice(value),
            fontSize = 10.sp,
            style = MaterialTheme.typography.labelSmall.tabular(),
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
        )
    }
}

@Composable
private fun PriceLabels(transform: ChartTransform) {
    val vis = transform.visibleCandles
    if (vis.isEmpty()) return

    val high = vis.maxOf { it.high }
    val low = vis.minOf { it.low }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                ChartFormatters.formatPrice(high),
                fontSize = 10.sp,
                style = MaterialTheme.typography.labelSmall.tabular(),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
            Spacer(Modifier.weight(1f))
            Text(
                ChartFormatters.formatPrice(low),
                fontSize = 10.sp,
                style = MaterialTheme.typography.labelSmall.tabular(),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}

private fun twoDec(v: Double): String {
    val r = (v * 100).toInt()
    val abs = if (r < 0) -r else r
    val sgn = if (r < 0) "-" else ""
    return "$sgn${abs / 100}.${(abs % 100).toString().padStart(2, '0')}"
}
