package dev.kbwallet.app.chart.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.MaterialTheme
import dev.kbwallet.app.chart.presentation.util.ChartTransform

@Composable
fun ChartGrid(
    transform: ChartTransform,
    modifier: Modifier = Modifier,
    chartArea: Float = ChartPlotHeightFraction,
    lines: Int = 4,
) {
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
    Canvas(modifier = modifier.fillMaxSize()) {
        val h = size.height * chartArea
        val w = size.width - ChartPriceAxisWidth.toPx()
        for (i in 0..lines) {
            val y = i.toFloat() / lines * h
            drawLine(gridColor, Offset(0f, y), Offset(w, y), 1f)
        }
    }
}
