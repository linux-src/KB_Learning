package dev.kbwallet.app.chart.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kbwallet.app.chart.domain.model.isBullish
import dev.kbwallet.app.chart.presentation.util.ChartFormatters
import dev.kbwallet.app.chart.presentation.util.ChartTransform

/**
 * Candlestick chart — Japanese candles with wicks, the app's primary price view.
 * Supports pan and zoom via gestures, and tap to place a crosshair.
 *
 * @param transform      Data-space ↔ screen-fraction mapping
 * @param bullColor      Color for bullish candles (close >= open)
 * @param bearColor      Color for bearish candles (close < open)
 * @param crosshairIndex Global candle index the crosshair sits on, or null for none
 * @param onCrosshair    Callback with the global candle index under the crosshair
 * @param xAxisLabel     Formats a candle's open time for the time axis
 */
@Composable
fun CandlestickChart(
    transform: ChartTransform,
    modifier: Modifier = Modifier,
    bullColor: Color = Color(0xFF00FF00),
    bearColor: Color = Color(0xFFFF3B30),
    crosshairIndex: Int? = null,
    onCrosshair: ((Int?) -> Unit)? = null,
    xAxisLabel: (Long) -> String = ChartFormatters::formatDayMonth,
    chartHeightFraction: Float = ChartPlotHeightFraction,
) {
    val textMeasurer = rememberTextMeasurer()
    // MaterialTheme is a @Composable reader — has to be resolved here, in the
    // composition, and captured; the Canvas content lambda below runs as a
    // DrawScope during the draw phase and can't call it directly.
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val crosshairColor = MaterialTheme.colorScheme.onSurface
    val tagBackground = MaterialTheme.colorScheme.onSurface
    val tagForeground = MaterialTheme.colorScheme.surface

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(transform) {
                detectTapGestures { offset ->
                    val chartWidth = size.width - ChartPriceAxisWidth.toPx()
                    val idx = if (offset.x <= chartWidth) hitTest(offset.x, chartWidth, transform) else -1
                    onCrosshair?.invoke(idx.takeIf { it >= 0 })
                }
            }
            .pointerInput(transform) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val chartWidth = size.width - ChartPriceAxisWidth.toPx()
                    if (chartWidth <= 0f) return@detectTransformGestures
                    transform.pan(-(pan.x / chartWidth))
                    if (zoom != 1f) {
                        // Anchor on the pinch centroid so the candles under the
                        // fingers stay put, instead of always zooming mid-chart.
                        transform.zoom(zoom, (centroid.x / chartWidth).coerceIn(0f, 1f))
                    }
                    onCrosshair?.invoke(null)
                }
            }
    ) {
        val rightMargin = ChartPriceAxisWidth.toPx()
        val chartHeight = size.height * chartHeightFraction
        val chartWidth = size.width - rightMargin
        if (chartWidth <= 0f) return@Canvas

        val candles = transform.candles
        if (candles.isEmpty()) return@Canvas

        val visStart = transform.visibleStartIdx
        val visEnd = transform.visibleEndIdx
        val count = visEnd - visStart
        if (count == 0) return@Canvas

        // Calculate candle width based on viewport.
        val candleSpacing = chartWidth / count
        val candleBodyWidth = (candleSpacing * 0.7f).coerceAtLeast(2.dp.toPx())

        // Draw visible candles
        for (i in visStart until visEnd) {
            val c = candles[i]
            val localIdx = i - visStart
            val centerX = (localIdx.toFloat() + 0.5f) * candleSpacing

            val openY = transform.priceToFraction(c.open) * chartHeight
            val closeY = transform.priceToFraction(c.close) * chartHeight
            val highY = transform.priceToFraction(c.high) * chartHeight
            val lowY = transform.priceToFraction(c.low) * chartHeight

            val isBull = c.isBullish
            val bodyColor = if (isBull) bullColor else bearColor
            val bodyTop = if (isBull) closeY else openY
            val bodyBottom = if (isBull) openY else closeY

            // Volume bar
            val volFraction = transform.volumeToFraction(c.volume)
            if (volFraction > 0f) {
                val volHeight = (volFraction * chartHeight * 0.25f).coerceAtLeast(1f) // max 25% of chart height
                drawRect(
                    color = bodyColor.copy(alpha = 0.3f),
                    topLeft = Offset(centerX - candleBodyWidth / 2, chartHeight - volHeight),
                    size = Size(candleBodyWidth, volHeight)
                )
            }

            // Wick (high-low line)
            drawLine(
                color = bodyColor,
                start = Offset(centerX, highY),
                end = Offset(centerX, lowY),
                strokeWidth = 1.dp.toPx(),
            )

            // Body
            val bodyHeight = (bodyBottom - bodyTop).coerceAtLeast(0f)
            if (bodyHeight < 1.dp.toPx()) {
                // Flat candle — just draw a horizontal line
                drawLine(
                    color = bodyColor,
                    start = Offset(centerX - candleBodyWidth / 2, bodyTop),
                    end = Offset(centerX + candleBodyWidth / 2, bodyTop),
                    strokeWidth = 1.5.dp.toPx(),
                )
            } else {
                drawRect(
                    color = bodyColor,
                    topLeft = Offset(centerX - candleBodyWidth / 2, bodyTop),
                    size = Size(candleBodyWidth, bodyHeight),
                )
            }
        }

        // ── Y-Axis (Prices) ──
        val priceRange = transform.priceRange
        val minPrice = priceRange.start
        val maxPrice = priceRange.endInclusive
        val steps = 5
        val priceStep = (maxPrice - minPrice) / steps
        val axisTextStyle = TextStyle(color = axisColor, fontSize = 10.sp, fontWeight = FontWeight.Normal)

        for (i in 0..steps) {
            val p = minPrice + priceStep * i
            val y = transform.priceToFraction(p) * chartHeight

            drawText(
                textMeasurer = textMeasurer,
                text = ChartFormatters.formatPrice(p),
                style = axisTextStyle,
                topLeft = Offset(chartWidth + 4.dp.toPx(), y - 6.dp.toPx())
            )
        }

        // ── X-Axis (Time) ──
        val timeSteps = 4
        val timeStepIdx = (count / timeSteps).coerceAtLeast(1)
        for (i in visStart until visEnd step timeStepIdx) {
            val c = candles[i]
            val localIdx = i - visStart
            val cx = (localIdx.toFloat() + 0.5f) * candleSpacing

            drawText(
                textMeasurer = textMeasurer,
                text = xAxisLabel(c.openTime),
                style = axisTextStyle,
                topLeft = Offset(cx - 15.dp.toPx(), chartHeight + 4.dp.toPx())
            )
        }

        // ── Crosshair ──
        val activeIdx = crosshairIndex ?: -1
        if (activeIdx in visStart until visEnd) {
            val localIdx = activeIdx - visStart
            val cx = (localIdx.toFloat() + 0.5f) * candleSpacing
            val c = candles[activeIdx]

            // Vertical line
            drawLine(
                color = crosshairColor.copy(alpha = 0.5f),
                start = Offset(cx, 0f),
                end = Offset(cx, chartHeight),
                strokeWidth = 1.dp.toPx(),
            )

            // Horizontal line at close
            val closeY = transform.priceToFraction(c.close) * chartHeight
            drawLine(
                color = crosshairColor.copy(alpha = 0.5f),
                start = Offset(0f, closeY),
                end = Offset(chartWidth, closeY),
                strokeWidth = 1.dp.toPx(),
            )

            val tagTextStyle = axisTextStyle.copy(color = tagForeground, fontWeight = FontWeight.Bold)

            // Price tag on the price axis
            drawRect(
                color = tagBackground,
                topLeft = Offset(chartWidth, closeY - 8.dp.toPx()),
                size = Size(rightMargin, 16.dp.toPx())
            )
            drawText(
                textMeasurer = textMeasurer,
                text = ChartFormatters.formatPrice(c.close),
                style = tagTextStyle,
                topLeft = Offset(chartWidth + 2.dp.toPx(), closeY - 6.dp.toPx())
            )

            // Time tag on the time axis
            drawRect(
                color = tagBackground,
                topLeft = Offset(cx - 25.dp.toPx(), chartHeight),
                size = Size(50.dp.toPx(), 16.dp.toPx())
            )
            drawText(
                textMeasurer = textMeasurer,
                text = xAxisLabel(c.openTime),
                style = tagTextStyle,
                topLeft = Offset(cx - 20.dp.toPx(), chartHeight + 2.dp.toPx())
            )
        }
    }
}

/**
 * Map a pixel x-coordinate back to a global candle index, or -1 if outside.
 */
private fun hitTest(x: Float, width: Float, transform: ChartTransform): Int {
    val visStart = transform.visibleStartIdx
    val visEnd = transform.visibleEndIdx
    val count = visEnd - visStart
    if (count == 0) return -1
    val spacing = width / count
    val globalIdx = (x / spacing).toInt() + visStart
    return if (globalIdx in visStart until visEnd) globalIdx else -1
}
