package dev.kbwallet.app.chart.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.kbwallet.app.chart.presentation.util.ChartTransform

/**
 * Overlays an SMA line on the chart.
 *
 * @param transform   Chart coordinate transform
 * @param smaValues   SMA value for each candle index (null where no value)
 * @param color       Line color
 * @param lineWidth   Stroke width in dp
 */
@Composable
fun SMAOverlay(
    transform: ChartTransform,
    smaValues: List<Double?>,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFA500),
    lineWidth: Float = 1.5f,
    chartHeightFraction: Float = ChartPlotHeightFraction,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val chartHeight = size.height * chartHeightFraction
        // Same plot area as CandlestickChart, so the overlay tracks the candle
        // centers instead of drifting right by the width of the price axis.
        val chartWidth = size.width - ChartPriceAxisWidth.toPx()
        if (chartWidth <= 0f) return@Canvas
        val candles = transform.candles
        if (candles.isEmpty() || smaValues.isEmpty()) return@Canvas

        val visStart = transform.visibleStartIdx
        val visEnd = transform.visibleEndIdx
        val count = visEnd - visStart
        if (count == 0) return@Canvas
        val spacing = chartWidth / count

        val path = Path()
        var started = false

        for (i in visStart until visEnd) {
            val sma = smaValues.getOrNull(i) ?: continue
            val localIdx = i - visStart
            val x = (localIdx.toFloat() + 0.5f) * spacing
            val y = transform.priceToFraction(sma) * chartHeight

            if (!started) {
                path.moveTo(x, y)
                started = true
            } else {
                path.lineTo(x, y)
            }
        }

        if (started) {
            drawPath(
                path,
                color = color,
                style = Stroke(width = lineWidth.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}
