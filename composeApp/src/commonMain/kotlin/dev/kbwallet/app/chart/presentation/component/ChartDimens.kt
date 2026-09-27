package dev.kbwallet.app.chart.presentation.component

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Geometry shared by every layer stacked into the chart Box (grid, candles,
 * line, SMA). They all draw into the same Canvas bounds, so the plot area has
 * to be defined once — otherwise the overlays drift out of alignment with the
 * candles they annotate.
 */

/** Right-hand gutter reserved for the price axis labels. */
val ChartPriceAxisWidth: Dp = 55.dp

/** Share of the canvas height used by the plot; the rest holds the time axis. */
const val ChartPlotHeightFraction: Float = 0.85f
