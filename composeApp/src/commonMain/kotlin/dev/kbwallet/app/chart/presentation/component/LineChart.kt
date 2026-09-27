package dev.kbwallet.app.chart.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.kbwallet.app.chart.presentation.util.ChartTransform

@Composable
fun LineChart(
    transform: ChartTransform,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF00FF00),
    chartArea: Float = ChartPlotHeightFraction,
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            // Same pan/zoom as the candlestick view, so toggling between the two
            // modes doesn't silently drop the gestures.
            .pointerInput(transform) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val chartWidth = size.width - ChartPriceAxisWidth.toPx()
                    if (chartWidth <= 0f) return@detectTransformGestures
                    transform.pan(-(pan.x / chartWidth))
                    if (zoom != 1f) {
                        transform.zoom(zoom, (centroid.x / chartWidth).coerceIn(0f, 1f))
                    }
                }
            }
    ) {
        val h = size.height * chartArea
        // Leave the price-axis gutter free, matching the candlestick chart —
        // otherwise switching modes visibly rescales the x-axis.
        val w = size.width - ChartPriceAxisWidth.toPx()
        if (w <= 0f) return@Canvas

        val candles = transform.candles
        if (candles.isEmpty()) return@Canvas

        // ── Build path from all visible points ──
        val path = Path()
        var firstX = 0f
        var started = false

        for (i in transform.visibleStartIdx until transform.visibleEndIdx) {
            val c = candles[i]
            val x = transform.indexToFraction(i) * w
            val y = transform.priceToFraction(c.close) * h
            if (!started) {
                path.moveTo(x, y)
                firstX = x
                started = true
            } else {
                path.lineTo(x, y)
            }
        }

        if (!started) return@Canvas

        val lastIdx = transform.visibleEndIdx - 1
        val lastX = transform.indexToFraction(lastIdx) * w

        // ── Gradient fill under the line ──
        val fillPath = Path().apply {
            addPath(path)
            lineTo(lastX, h)
            lineTo(firstX, h)
            close()
        }

        drawPath(
            fillPath,
            brush = Brush.verticalGradient(
                0.0f to lineColor.copy(alpha = 0.18f),
                0.6f to lineColor.copy(alpha = 0.04f),
                1.0f to Color.Transparent,
                startY = 0f,
                endY = h,
            ),
        )

        // ── Line on top ──
        drawPath(
            path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        // ── Subtle glow ──
        drawPath(
            path,
            color = lineColor.copy(alpha = 0.30f),
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        // ── Last visible price dot ──
        val last = candles[lastIdx]
        val ly = transform.priceToFraction(last.close) * h
        drawCircle(Color.White, 4.dp.toPx(), Offset(lastX, ly))
        drawCircle(lineColor, 5.5.dp.toPx(), Offset(lastX, ly), style = Stroke(1.5.dp.toPx()))

        // ── Max / Min dots ──
        val visStart = transform.visibleStartIdx
        var maxIdx = visStart
        var minIdx = visStart
        for (i in visStart..lastIdx) {
            if (candles[i].high > candles[maxIdx].high) maxIdx = i
            if (candles[i].low < candles[minIdx].low) minIdx = i
        }
        drawCircle(
            lineColor.copy(alpha = 0.3f),
            3.dp.toPx(),
            Offset(transform.indexToFraction(maxIdx) * w, transform.priceToFraction(candles[maxIdx].high) * h),
        )
        drawCircle(
            Color(0xFFFF3B30).copy(alpha = 0.3f),
            3.dp.toPx(),
            Offset(transform.indexToFraction(minIdx) * w, transform.priceToFraction(candles[minIdx].low) * h),
        )
    }
}
