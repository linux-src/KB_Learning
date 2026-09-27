package dev.kbwallet.app.chart.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import dev.kbwallet.app.chart.presentation.util.ChartTransform

private val GridCol = Color.White.copy(alpha = 0.05f)

@Composable
fun ChartGrid(
    transform: ChartTransform,
    modifier: Modifier = Modifier,
    chartArea: Float = ChartPlotHeightFraction,
    lines: Int = 4,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val h = size.height * chartArea
        val w = size.width - ChartPriceAxisWidth.toPx()
        for (i in 0..lines) {
            val y = i.toFloat() / lines * h
            drawLine(GridCol, Offset(0f, y), Offset(w, y), 1f)
        }
    }
}
