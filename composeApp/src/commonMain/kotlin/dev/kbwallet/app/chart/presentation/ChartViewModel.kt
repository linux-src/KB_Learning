package dev.kbwallet.app.chart.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbwallet.app.chart.domain.GetChartDataUseCase
import dev.kbwallet.app.chart.domain.indicator.calculateSMA
import dev.kbwallet.app.chart.domain.model.TimeRange
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.util.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChartViewModel(
    private val getChartDataUseCase: GetChartDataUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ChartState())
    val state: StateFlow<ChartState> = _state.asStateFlow()

    private var currentCoinId: String = ""
    private var currentRange: TimeRange = TimeRange.ONE_DAY

    fun retry() {
        loadData(currentRange)
    }

    fun init(coinId: String, coinName: String) {
        currentCoinId = coinId
        _state.update { it.copy(coinName = coinName) }
        loadData(TimeRange.ONE_DAY)
    }

    fun selectTimeRange(range: TimeRange) {
        _state.update { it.copy(selectedRange = range) }
        loadData(range)
    }

    fun onCrosshair(index: Int?) {
        _state.update { it.copy(crosshairIndex = index) }
    }

    // Only the candlestick view draws a crosshair, so switching modes drops it —
    // otherwise the line view keeps an OHLC readout pinned to a candle with
    // nothing on screen pointing at it.
    fun setChartMode(candlestick: Boolean) {
        _state.update { it.copy(isCandlestickMode = candlestick, crosshairIndex = null) }
    }

    fun toggleChartMode() {
        setChartMode(!_state.value.isCandlestickMode)
    }

    fun toggleSma() {
        _state.update { it.copy(showSma = !it.showSma) }
    }

    private fun loadData(range: TimeRange) {
        currentRange = range
        // Clear the previous range's candles too — otherwise a failed fetch (e.g.
        // switching time ranges) leaves the old range's chart on screen instead of
        // the error+retry state, since the "has data" branch below only checks
        // candles.isNotEmpty(), not whether this fetch actually succeeded.
        _state.update {
            it.copy(candles = emptyList(), smaValues = emptyList(), isLoading = true, error = null)
        }
        viewModelScope.launch {
            when (val result = getChartDataUseCase.execute(currentCoinId, range)) {
                is Result.Success -> {
                    val candles = result.data.sortedBy { it.openTime }
                    val first = candles.firstOrNull()
                    val last = candles.lastOrNull()
                    val change = if (first != null && last != null) last.close - first.close else 0.0
                    val changePct = if (first != null && first.close != 0.0) (change / first.close) * 100 else 0.0
                    _state.update {
                        it.copy(
                            candles = candles,
                            smaValues = calculateSMA(candles.map { c -> c.close }, it.smaPeriod),
                            isLoading = false,
                            currentPrice = last?.close ?: 0.0,
                            priceChange = change,
                            priceChangePercent = changePct,
                            crosshairIndex = null,
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.error.toUiText()) }
                }
            }
        }
    }

    companion object {
        /** Window used for the SMA overlay; short enough to have values on every range. */
        const val DEFAULT_SMA_PERIOD = 20
    }
}
