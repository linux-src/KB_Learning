package dev.kbwallet.app.simulator.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbwallet.app.chart.data.remote.impl.CoinGeckoKlineDataSource
import dev.kbwallet.app.chart.domain.model.CandleModel
import dev.kbwallet.app.chart.domain.model.TimeRange
import dev.kbwallet.app.coins.domain.api.CoinsRemoteDataSource
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.domain.coin.Coin
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.simulator.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.sqrt

class SimulatorViewModel(
    private val coinsRemoteDataSource: CoinsRemoteDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(SimulatorState())
    val state: StateFlow<SimulatorState> = _state.asStateFlow()

    private var simulationJob: Job? = null
    private var nextPositionId = 1L
    private var nextTradeId = 1L

    // ── Coin Selection ──

    fun loadCoins() {
        viewModelScope.launch {
            val result = coinsRemoteDataSource.getListOfCoins()
            when (result) {
                is Result.Success -> {
                    val coins = result.data.map { dto ->
                        Coin(id = dto.id, name = dto.name, symbol = dto.symbol.uppercase(), iconUrl = dto.image)
                    }.take(20)
                    _state.update { it.copy(availableCoins = coins, isLoading = false, error = null) }
                }
                is Result.Error -> {
                    _state.update { it.copy(error = SimulatorErrorType.FAILED_TO_LOAD_COINS, isLoading = false) }
                }
            }
        }
    }

    fun selectCoin(coin: Coin) {
        _state.update { it.copy(selectedCoin = coin, isLoading = true) }
        loadHistoryData(coin)
    }

    fun changeCoin() {
        stopPlayback()
        _state.update {
            it.copy(
                selectedCoin = null,
                candles = emptyList(),
                isPlaying = false,
                isLoading = false,
                error = null,
            )
        }
    }

    private fun loadHistoryData(coin: Coin) {
        // Clear the previous candles up front, not just isLoading/error — otherwise a
        // failed fetch (new coin, or a time-range switch) leaves the last-loaded data
        // on screen (SimulatorScreen's error branch only fires when candles is empty),
        // silently showing stale/mismatched data instead of the error+retry state.
        _state.update { it.copy(candles = emptyList(), isLoading = true, error = null) }
        viewModelScope.launch {
            val timeRange = TimeRange.ONE_DAY
            val klineSource = CoinGeckoKlineDataSource(coinsRemoteDataSource)
            when (val result = klineSource.fetchKlines(coin.id, timeRange)) {
                is Result.Success -> {
                    val candles = result.data.sortedBy { it.openTime }
                    if (candles.size < 10) {
                        _state.update { it.copy(error = SimulatorErrorType.NOT_ENOUGH_DATA, isLoading = false) }
                        return@launch
                    }
                    _state.update {
                        it.copy(
                            candles = candles,
                            currentCandleIndex = 0,
                            isLoading = false,
                            error = null,
                            initialBalance = 10000.0,
                            cashBalance = 10000.0,
                            equity = 10000.0,
                            balanceHistory = listOf(10000.0),
                            positions = emptyList(),
                            closedTrades = emptyList(),
                            metrics = SimulatorMetrics(),
                            activeHint = getHint(),
                        )
                    }
                    nextPositionId = 1L
                    nextTradeId = 1L
                }
                is Result.Error -> {
                    _state.update { it.copy(error = SimulatorErrorType.FAILED_TO_LOAD_DATA, isLoading = false) }
                }
            }
        }
    }

    // ── Playback Control ──

    fun togglePlay() {
        val playing = !_state.value.isPlaying
        _state.update { it.copy(isPlaying = playing) }
        if (playing) startPlayback() else stopPlayback()
    }

    fun stepForward() {
        advanceCandle()
    }

    fun stepBackward() {
        _state.update {
            val newIdx = (it.currentCandleIndex - 1).coerceAtLeast(0)
            it.copy(currentCandleIndex = newIdx)
        }
    }

    fun setPlaySpeed(speed: PlaySpeed) {
        _state.update { it.copy(playSpeed = speed) }
        if (_state.value.isPlaying) {
            stopPlayback()
            startPlayback()
        }
    }

    fun changeTimeRange(range: TimeRange) {
        _state.update { it.copy(timeRange = range) }
        _state.value.selectedCoin?.let { loadHistoryData(it) }
    }

    private fun startPlayback() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (isActive && _state.value.isPlaying) {
                delay(_state.value.playSpeed.delayMs)
                advanceCandle()
            }
        }
    }

    private fun stopPlayback() {
        simulationJob?.cancel()
        simulationJob = null
    }

    private fun advanceCandle() {
        val s = _state.value
        if (s.currentCandleIndex >= s.candles.lastIndex) {
            _state.update { it.copy(isPlaying = false) }
            stopPlayback()
            return
        }
        val newIdx = s.currentCandleIndex + 1
        val candle = s.candles[newIdx]

        // Check liquidation/SL/TP for existing positions
        var positions = s.positions.map { pos ->
            val liqPrice = liquidationPrice(pos)
            var isOpen = pos.isOpen
            var exitReason: ExitReason? = null
            var exitPrice = candle.close

            // Adverse-side check: liquidation and stop-loss share the same axis
            // (the direction that hurts the position). Whichever threshold is
            // closer to the entry price is reached first as the candle moves
            // against the trade, so it wins — matching how a real exchange
            // force-closes a position before a farther-away resting SL order
            // would ever get the chance to fill.
            if (pos.side == PositionSide.LONG) {
                val liquidationWins = pos.stopLoss == null || liqPrice >= pos.stopLoss
                val effectiveStop = if (liquidationWins) liqPrice else pos.stopLoss!!
                if (candle.low <= effectiveStop) {
                    isOpen = false
                    exitPrice = effectiveStop
                    exitReason = if (liquidationWins) ExitReason.LIQUIDATION else ExitReason.STOP_LOSS
                }
            } else {
                val liquidationWins = pos.stopLoss == null || liqPrice <= pos.stopLoss
                val effectiveStop = if (liquidationWins) liqPrice else pos.stopLoss!!
                if (candle.high >= effectiveStop) {
                    isOpen = false
                    exitPrice = effectiveStop
                    exitReason = if (liquidationWins) ExitReason.LIQUIDATION else ExitReason.STOP_LOSS
                }
            }
            // Take Profit check
            if (isOpen && pos.takeProfit != null) {
                val tpHit = if (pos.side == PositionSide.LONG) candle.high >= pos.takeProfit
                else candle.low <= pos.takeProfit
                if (tpHit) { isOpen = false; exitPrice = pos.takeProfit; exitReason = ExitReason.TAKE_PROFIT }
            }
            if (!isOpen && exitReason != null) {
                // Close the position at the trigger price
                closePositionInternal(pos, exitPrice, exitReason, newIdx)
                null // will be filtered out
            } else {
                val newPnL = calculatePnL(pos, candle.close)
                pos.copy(currentPrice = candle.close, pnl = newPnL, pnlPercent = newPnL / pos.amountInFiat * 100)
            }
        }.filterNotNull()

        // Equity = free cash + (margin locked in every open position + its unrealized P&L).
        // Margin was already deducted from cashBalance when the position was opened, so it
        // must be added back here — otherwise equity looks like it drops by the margin the
        // instant a trade opens, even at zero P&L.
        val lockedValue = positions.sumOf { it.amountInFiat + it.pnl }
        val equity = s.cashBalance + lockedValue

        _state.update {
            it.copy(
                currentCandleIndex = newIdx,
                positions = positions,
                equity = equity,
                balanceHistory = it.balanceHistory + equity,
            )
        }

        // Recalculate metrics
        recalculateMetrics()
    }

    private fun calculatePnL(pos: SimPosition, price: Double): Double {
        return if (pos.side == PositionSide.LONG) {
            (price - pos.entryPrice) / pos.entryPrice * pos.amountInFiat * pos.leverage
        } else {
            (pos.entryPrice - price) / pos.entryPrice * pos.amountInFiat * pos.leverage
        }
    }

    /**
     * Price at which the position's loss equals 100% of its margin (amountInFiat).
     * Beyond this point a real leveraged exchange force-closes the position rather
     * than letting the loss exceed the margin the trader posted.
     */
    private fun liquidationPrice(pos: SimPosition): Double {
        val move = pos.entryPrice / pos.leverage
        return if (pos.side == PositionSide.LONG) (pos.entryPrice - move).coerceAtLeast(0.0)
        else pos.entryPrice + move
    }

    // ── Open Position ──

    fun onOrderAmountChanged(amount: String) {
        _state.update { it.copy(orderAmount = amount.filter { it.isDigit() || it == '.' }) }
    }

    fun onOrderSideChanged(side: OrderSideInput) {
        _state.update { it.copy(orderSide = side) }
    }

    fun onOrderSLChanged(sl: String) {
        _state.update { it.copy(orderStopLoss = sl.filter { it.isDigit() || it == '.' }) }
    }

    fun onOrderTPChanged(tp: String) {
        _state.update { it.copy(orderTakeProfit = tp.filter { it.isDigit() || it == '.' }) }
    }

    fun onOrderLeverageChanged(lev: String) {
        val digits = lev.filter { it.isDigit() }
        _state.update { it.copy(orderLeverage = digits) }
    }

    fun openPosition() {
        val s = _state.value
        val coin = s.selectedCoin ?: return
        val candle = s.candles.getOrNull(s.currentCandleIndex) ?: return
        val amount = s.orderAmount.toDoubleOrNull() ?: return
        if (amount <= 0 || amount > s.cashBalance) return

        val sl = s.orderStopLoss.toDoubleOrNull()
        val tp = s.orderTakeProfit.toDoubleOrNull()
        val leverage = s.orderLeverage.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 1.0

        val side = if (s.orderSide == OrderSideInput.LONG) PositionSide.LONG else PositionSide.SHORT
        val initialPnl = 0.0

        val position = SimPosition(
            id = nextPositionId++,
            coinId = coin.id,
            coinName = coin.name,
            coinSymbol = coin.symbol,
            side = side,
            entryPrice = candle.close,
            currentPrice = candle.close,
            amountInFiat = amount,
            leverage = leverage,
            stopLoss = sl,
            takeProfit = tp,
            entryTime = s.currentCandleIndex,
            isOpen = true,
            pnl = initialPnl,
            pnlPercent = 0.0,
        )

        _state.update {
            it.copy(
                positions = it.positions + position,
                cashBalance = it.cashBalance - amount,
                orderAmount = "",
                orderStopLoss = "",
                orderTakeProfit = "",
                activeHint = getHint(),
            )
        }
    }

    // ── Close Position ──

    fun closePosition(positionId: Long) {
        val s = _state.value
        val candle = s.candles.getOrNull(s.currentCandleIndex) ?: return
        val pos = s.positions.find { it.id == positionId } ?: return
        closePositionInternal(pos, candle.close, ExitReason.MANUAL, s.currentCandleIndex)
        _state.update {
            it.copy(positions = it.positions.filter { p -> p.id != positionId })
        }
        recalculateMetrics()
    }

    private fun closePositionInternal(pos: SimPosition, exitPrice: Double, reason: ExitReason, exitIdx: Int) {
        val pnl = if (pos.side == PositionSide.LONG) {
            (exitPrice - pos.entryPrice) / pos.entryPrice * pos.amountInFiat * pos.leverage
        } else {
            (pos.entryPrice - exitPrice) / pos.entryPrice * pos.amountInFiat * pos.leverage
        }
        val trade = ClosedTrade(
            id = nextTradeId++,
            coinId = pos.coinId,
            coinName = pos.coinName,
            side = pos.side,
            entryPrice = pos.entryPrice,
            exitPrice = exitPrice,
            amountInFiat = pos.amountInFiat,
            pnl = pnl,
            pnlPercent = pnl / pos.amountInFiat * 100,
            entryTime = pos.entryTime,
            exitTime = exitIdx,
            exitReason = reason,
        )
        _state.update {
            it.copy(
                closedTrades = it.closedTrades + trade,
                cashBalance = it.cashBalance + pos.amountInFiat + pnl,
            )
        }
    }

    // ── Metrics ──

    private fun recalculateMetrics() {
        val trades = _state.value.closedTrades
        if (trades.isEmpty()) return

        val winners = trades.filter { it.pnl > 0 }
        val losers = trades.filter { it.pnl < 0 }
        val totalPnl = trades.sumOf { it.pnl }
        val winRate = winners.size.toDouble() / trades.size
        val grossProfit = winners.sumOf { it.pnl }
        val grossLoss = abs(losers.sumOf { it.pnl })
        val profitFactor = if (grossLoss > 0) grossProfit / grossLoss else if (grossProfit > 0) Double.MAX_VALUE else 1.0

        // Drawdown from balance history
        val history = _state.value.balanceHistory
        var peak = history.firstOrNull() ?: 10000.0
        var maxDd = 0.0
        for (eq in history) {
            if (eq > peak) peak = eq
            val dd = peak - eq
            if (dd > maxDd) maxDd = dd
        }
        val maxDdPercent = if (peak > 0) maxDd / peak * 100 else 0.0

        // Sharpe (simplified)
        val returns = history.zipWithNext { prev, curr -> curr - prev }
        val mean = if (returns.isNotEmpty()) returns.average() else 0.0
        val std = if (returns.isNotEmpty()) {
            val variance = returns.map { (it - mean) * (it - mean) }.average()
            sqrt(variance)
        } else 0.0
        val sharpe = if (std > 0) mean / std * sqrt(252.0) else 0.0

        _state.update {
            it.copy(metrics = SimulatorMetrics(
                totalPnl = totalPnl,
                totalPnlPercent = if (it.initialBalance > 0) totalPnl / it.initialBalance * 100 else 0.0,
                winRate = winRate,
                profitFactor = profitFactor,
                maxDrawdown = maxDd,
                maxDrawdownPercent = maxDdPercent,
                sharpeRatio = sharpe,
                totalTrades = trades.size,
                winningTrades = winners.size,
                losingTrades = losers.size,
                avgWin = if (winners.isNotEmpty()) winners.sumOf { it.pnl } / winners.size else 0.0,
                avgLoss = if (losers.isNotEmpty()) losers.sumOf { it.pnl } / losers.size else 0.0,
                bestTrade = trades.maxOf { it.pnl },
                worstTrade = trades.minOf { it.pnl },
            ))
        }
    }

    // ── Hints / Education ──

    private val hints = listOf(
        "💡 Long позиция: покупаете, ожидая рост цены. Прибыль = цена выросла.",
        "💡 Short позиция: продаете, ожидая падение. Прибыль = цена упала.",
        "💡 Stop-Loss: автоматически закроет позицию при убытке на заданном уровне.",
        "💡 Take-Profit: закроет позицию при достижении целевой прибыли.",
        "📊 Win Rate > 50% и Profit Factor > 1.5 — признаки успешной стратегии.",
        "📉 Максимальная просадка (Max Drawdown) показывает худший сценарий.",
        "🛡 Никогда не рискуйте больше 2% депозита на одну сделку.",
        "🎯 Тренд — ваш друг. Не пытайтесь ловить развороты без подтверждения.",
        "📋 Ведите журнал сделок: анализируйте, почему сделка была успешной или нет.",
        "⏱ Терпение важнее скорости. Ждите хороших точек входа.",
    )

    private var hintIndex = 0
    private fun getHint(): String {
        val h = hints[hintIndex % hints.size]
        hintIndex++
        return h
    }

    fun nextHint() {
        _state.update { it.copy(activeHint = getHint()) }
    }

    override fun onCleared() {
        stopPlayback()
        super.onCleared()
    }
}
