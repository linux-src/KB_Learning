package dev.kbwallet.app.chart.domain.indicator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The SMA overlay ships on the candlestick chart, so the window arithmetic is
 * worth pinning down — it's computed incrementally (running sum), which is the
 * kind of thing that silently drifts by one index.
 */
class IndicatorsTest {

    @Test
    fun `SMA is null until the window is full, then averages it`() {
        val values = listOf(1.0, 2.0, 3.0, 4.0, 5.0)

        val sma = calculateSMA(values, period = 3)

        assertEquals(values.size, sma.size)
        assertNull(sma[0])
        assertNull(sma[1])
        assertEquals(2.0, sma[2])   // (1+2+3)/3
        assertEquals(3.0, sma[3])   // (2+3+4)/3
        assertEquals(4.0, sma[4])   // (3+4+5)/3
    }

    @Test
    fun `SMA of a flat series is the series value`() {
        val sma = calculateSMA(List(10) { 42.0 }, period = 4)

        assertTrue(sma.drop(3).all { it == 42.0 })
    }

    @Test
    fun `SMA yields nothing when there are fewer values than the period`() {
        val sma = calculateSMA(listOf(1.0, 2.0), period = 5)

        assertEquals(2, sma.size)
        assertTrue(sma.all { it == null })
    }

    @Test
    fun `SMA of an empty series is empty`() {
        assertTrue(calculateSMA(emptyList(), period = 3).isEmpty())
    }

    @Test
    fun `EMA is null until the window is full and then tracks the series`() {
        val values = listOf(1.0, 2.0, 3.0, 4.0, 5.0)

        val ema = calculateEMA(values, period = 3)

        assertEquals(values.size, ema.size)
        assertNull(ema[0])
        assertNull(ema[1])
        assertEquals(2.0, ema[2])   // seeded from the SMA of the first window
        assertEquals(3.0, ema[3])   // 2 + (4 - 2) * 2/(3+1)
        assertEquals(4.0, ema[4])   // 3 + (5 - 3) * 2/(3+1)
    }
}
