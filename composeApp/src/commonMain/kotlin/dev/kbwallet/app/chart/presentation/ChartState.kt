package dev.kbwallet.app.chart.presentation

import androidx.compose.runtime.Stable
import dev.kbwallet.app.chart.domain.model.CandleModel
import dev.kbwallet.app.chart.domain.model.TimeRange
import org.jetbrains.compose.resources.StringResource

@Stable
data class ChartState(
    val coinName: String = "",
    val candles: List<CandleModel> = emptyList(),
    val selectedRange: TimeRange = TimeRange.ONE_DAY,
    val isLoading: Boolean = false,
    val error: StringResource? = null,
    val crosshairIndex: Int? = null,
    val currentPrice: Double = 0.0,
    val priceChange: Double = 0.0,
    val priceChangePercent: Double = 0.0,
    /** Candlesticks are the primary view — the line chart is the alternative. */
    val isCandlestickMode: Boolean = true,
    val showSma: Boolean = true,
    val smaPeriod: Int = ChartViewModel.DEFAULT_SMA_PERIOD,
    /** SMA value per candle index, `null` where the window isn't filled yet. */
    val smaValues: List<Double?> = emptyList(),
) {
    /** Candle the OHLC readout describes: the tapped one, else the most recent. */
    val focusedCandle: CandleModel?
        get() = crosshairIndex?.let { candles.getOrNull(it) } ?: candles.lastOrNull()
}
