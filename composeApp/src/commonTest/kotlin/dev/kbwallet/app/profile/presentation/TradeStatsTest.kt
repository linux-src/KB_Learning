package dev.kbwallet.app.profile.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TradeStatsTest {
    private val day = 86_400_000L

    @Test
    fun noTrades_givesZeroTradesAndNoWinRate() {
        val stats = computeTradeStats(emptyList(), nowMillis = 10 * day)
        assertEquals(0, stats.totalTrades)
        assertNull(stats.winRatePercent)
        assertEquals(1, stats.daysActive)
    }

    @Test
    fun sellAboveAverageCost_isAWin_belowIsALoss() {
        val trades = listOf(
            TradeRecord("btc", isBuy = true, units = 1.0, price = 100.0, timestamp = 0),
            TradeRecord("btc", isBuy = true, units = 1.0, price = 200.0, timestamp = 1), // avg 150
            TradeRecord("btc", isBuy = false, units = 0.5, price = 160.0, timestamp = 2), // win
            TradeRecord("btc", isBuy = false, units = 0.5, price = 140.0, timestamp = 3), // loss
        )
        val stats = computeTradeStats(trades, nowMillis = 2 * day + 5)
        assertEquals(4, stats.totalTrades)
        assertEquals(50, stats.winRatePercent)
        assertEquals(3, stats.daysActive)
    }

    @Test
    fun averageCostIsTrackedPerCoin() {
        val trades = listOf(
            TradeRecord("btc", true, 1.0, 100.0, 0),
            TradeRecord("eth", true, 1.0, 10.0, 1),
            TradeRecord("eth", false, 1.0, 50.0, 2), // win vs eth avg 10, would be loss vs btc
        )
        assertEquals(100, computeTradeStats(trades, 3).winRatePercent)
    }
}
