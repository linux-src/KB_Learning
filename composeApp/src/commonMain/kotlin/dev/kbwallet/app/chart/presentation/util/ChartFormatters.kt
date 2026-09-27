package dev.kbwallet.app.chart.presentation.util

import dev.kbwallet.app.chart.domain.model.TimeRange
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object ChartFormatters {

    fun formatPrice(price: Double): String {
        val absPrice = kotlin.math.abs(price)
        return when {
            absPrice >= 10_000 -> formatWithCommas(price, 0)
            absPrice >= 1_000 -> formatWithCommas(price, 0)
            absPrice >= 1.0   -> roundToString(price, 2)
            absPrice >= 0.01  -> roundToString(price, 4)
            absPrice >= 0.0001 -> roundToString(price, 6)
            else -> roundToString(price, 8)
        }
    }

    private fun formatWithCommas(value: Double, decimals: Int): String {
        val factor = pow10(decimals)
        val rounded = kotlin.math.round(value * factor) / factor
        val intPart = rounded.toLong()
        val sign = if (value < 0) "-" else ""
        val absInt = kotlin.math.abs(intPart)
        val str = absInt.toString().reversed().chunked(3).joinToString(",").reversed()
        val frac = if (decimals > 0) {
            val fracVal = kotlin.math.abs(rounded - intPart)
            "." + (fracVal * factor).toLong().toString().padStart(decimals, '0')
        } else ""
        return "$sign$str$frac"
    }

    private fun roundToString(value: Double, decimals: Int): String {
        val factor = pow10(decimals)
        val rounded = kotlin.math.round(value * factor) / factor
        val intPart = rounded.toLong()
        val fracPart = kotlin.math.abs(rounded - intPart)
        val fracStr = if (decimals > 0) {
            "." + (fracPart * factor).toLong().toString().padStart(decimals, '0')
        } else ""
        return "$intPart$fracStr"
    }

    fun formatVolume(volume: Double): String = when {
        volume >= 1_000_000_000 -> roundToString(volume / 1_000_000_000, 1) + "B"
        volume >= 1_000_000     -> roundToString(volume / 1_000_000, 1) + "M"
        volume >= 1_000         -> roundToString(volume / 1_000, 1) + "K"
        else -> roundToString(volume, 0)
    }

    // ── Time axis ──
    //
    // These used to treat the epoch-millis timestamp as a *duration* and print
    // things like "20700d" on every candle. Candle timestamps are absolute, so
    // they're rendered as local wall-clock dates instead, at the granularity
    // that suits the selected range.

    /** "14:30" — for intraday ranges. */
    fun formatTimeOfDay(timestampMs: Long): String {
        val t = localDateTime(timestampMs)
        return "${two(t.hour)}:${two(t.minute)}"
    }

    /** "07.03" — for day/week ranges. */
    fun formatDayMonth(timestampMs: Long): String {
        val t = localDateTime(timestampMs)
        return "${two(t.dayOfMonth)}.${two(t.monthNumber)}"
    }

    /** "03.25" (month.year) — for month/year ranges. */
    fun formatMonthYear(timestampMs: Long): String {
        val t = localDateTime(timestampMs)
        return "${two(t.monthNumber)}.${two(t.year % 100)}"
    }

    /** "07.03 14:30" — used by the crosshair readout, where the full stamp fits. */
    fun formatDateTime(timestampMs: Long): String {
        val t = localDateTime(timestampMs)
        return "${two(t.dayOfMonth)}.${two(t.monthNumber)} ${two(t.hour)}:${two(t.minute)}"
    }

    /**
     * X-axis label formatter matching a range's candle size: intraday ranges get
     * a clock, mid ranges a date, long ranges a month.
     */
    fun axisLabelFor(range: TimeRange): (Long) -> String = when (range) {
        TimeRange.ONE_HOUR, TimeRange.FOUR_HOURS -> ChartFormatters::formatTimeOfDay
        TimeRange.ONE_DAY, TimeRange.ONE_WEEK -> ChartFormatters::formatDayMonth
        TimeRange.ONE_MONTH, TimeRange.ONE_YEAR -> ChartFormatters::formatMonthYear
    }

    private fun localDateTime(timestampMs: Long): LocalDateTime =
        Instant.fromEpochMilliseconds(timestampMs).toLocalDateTime(TimeZone.currentSystemDefault())

    private fun two(v: Int): String = v.toString().padStart(2, '0')
}

private fun pow10(n: Int): Double {
    var r = 1.0; repeat(n) { r *= 10.0 }; return r
}

fun Double.formatPriceString(): String = ChartFormatters.formatPrice(this)
fun Double.formatVolumeString(): String = ChartFormatters.formatVolume(this)
