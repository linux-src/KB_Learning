package dev.kbwallet.app.chart.presentation.util

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import dev.kbwallet.app.chart.domain.model.CandleModel
import kotlin.math.max

/**
 * Maps between data space (candle index / price) and screen fractions (0..1).
 * Supports a visible sub-range for pan & zoom.
 *
 * The visible range is backed by snapshot state: the charts read it from inside
 * a Canvas draw lambda, so a plain `var` would be mutated by a pan/zoom gesture
 * without ever invalidating the draw — the gesture would be silently ignored.
 */
@Stable
class ChartTransform(
    val candles: List<CandleModel>,
    visibleStart: Float = 0f,
    visibleEnd: Float = 1f,
) {
    private var _visibleStart by mutableFloatStateOf(visibleStart)
    private var _visibleEnd by mutableFloatStateOf(visibleEnd)

    val visibleStartIdx: Int
        get() = if (candles.isEmpty()) 0
        else (candles.size * _visibleStart).toInt().coerceIn(0, candles.lastIndex)

    val visibleEndIdx: Int
        get() = if (candles.isEmpty()) 0
        else (candles.size * _visibleEnd).toInt().coerceIn(visibleStartIdx + 1, candles.size)

    val visibleCount: Int get() = max(1, visibleEndIdx - visibleStartIdx)

    val visibleCandles: List<CandleModel>
        get() = if (candles.isEmpty()) emptyList() else candles.subList(visibleStartIdx, visibleEndIdx)

    val priceRange: ClosedFloatingPointRange<Double>
        get() {
            val vs = visibleCandles
            if (vs.isEmpty()) return 0.0..1.0
            val high = vs.maxOf { it.high }
            val low = vs.minOf { it.low }
            // A perfectly flat window (high == low) would collapse the range to a
            // single point and make priceToFraction() constant; fall back to a
            // relative pad so the candles still land mid-chart.
            val pad = if (high > low) (high - low) * 0.05 else max(high * 0.005, 0.5)
            return (low - pad)..(high + pad)
        }

    val volumeRange: ClosedFloatingPointRange<Double>
        get() {
            val maxVol = visibleCandles.maxOfOrNull { it.volume } ?: 1.0
            return 0.0..(maxVol * 1.05)
        }

    val span: Float get() = _visibleEnd - _visibleStart

    fun pan(deltaFraction: Float) {
        val d = deltaFraction.coerceIn(-_visibleStart, 1f - _visibleEnd)
        _visibleStart += d
        _visibleEnd += d
    }

    fun zoom(scaleFactor: Float, anchorFraction: Float) {
        if (scaleFactor <= 0f) return
        val newSpan = (span / scaleFactor).coerceIn(MIN_SPAN, 1f)
        val anchor = _visibleStart + anchorFraction * span
        _visibleStart = (anchor - anchorFraction * newSpan).coerceIn(0f, 1f - newSpan)
        _visibleEnd = _visibleStart + newSpan
    }

    fun reset() {
        _visibleStart = 0f
        _visibleEnd = 1f
    }

    fun indexToFraction(globalIndex: Int): Float {
        val s = visibleEndIdx - visibleStartIdx
        if (s == 0) return 0f
        return ((globalIndex - visibleStartIdx).toFloat() / s).coerceIn(0f, 1f)
    }

    fun priceToFraction(price: Double): Float {
        val r = priceRange
        val s = r.endInclusive - r.start
        if (s == 0.0) return 0.5f
        return (1.0 - (price - r.start) / s).toFloat().coerceIn(0f, 1f)
    }

    fun volumeToFraction(volume: Double): Float {
        val maxV = volumeRange.endInclusive
        if (maxV == 0.0) return 0f
        return (volume / maxV).toFloat().coerceIn(0f, 1f)
    }

    private companion object {
        /** Never zoom in past ~5 candles' worth of the series. */
        const val MIN_SPAN = 0.02f
    }
}
