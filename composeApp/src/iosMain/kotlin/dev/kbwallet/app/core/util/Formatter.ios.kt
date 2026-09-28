package dev.kbwallet.app.core.util

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle

actual fun formatFiat(amount: Double, showDecimal: Boolean): String {
    val numberFormatter = NSNumberFormatter()
    numberFormatter.numberStyle = NSNumberFormatterDecimalStyle
    val absAmount = kotlin.math.abs(amount)
    when {
        showDecimal.not() -> {
            numberFormatter.minimumFractionDigits = 0.toULong()
            numberFormatter.maximumFractionDigits = 0.toULong()
        }
        absAmount == 0.0 || absAmount >= 0.01 -> {
            numberFormatter.minimumFractionDigits = 2.toULong()
            numberFormatter.maximumFractionDigits = 2.toULong()
        }
        else -> {
            numberFormatter.minimumFractionDigits = 2.toULong()
            numberFormatter.maximumFractionDigits = 8.toULong()
        }
    }
    val formatterAmount = numberFormatter.stringFromNumber(NSNumber(absAmount)) ?: return ""
    val sign = if (amount < 0 && formatterAmount != numberFormatter.stringFromNumber(NSNumber(0.0))) "-" else ""
    return "$sign$ $formatterAmount"
}

actual fun formatCoinUnit(amount: Double, symbol: String): String {
    val numberFormatter = NSNumberFormatter()
    numberFormatter.numberStyle = NSNumberFormatterDecimalStyle
    numberFormatter.minimumFractionDigits = 8.toULong()
    numberFormatter.maximumFractionDigits = 8.toULong()
    return numberFormatter.stringFromNumber(NSNumber(amount)) + " $symbol"
}

actual fun formatPercentage(amount: Double): String {
    val numberFormatter = NSNumberFormatter()
    numberFormatter.numberStyle = NSNumberFormatterDecimalStyle
    numberFormatter.minimumFractionDigits = 2.toULong()
    numberFormatter.maximumFractionDigits = 2.toULong()
    val prefix = if (amount >= 0) "+" else ""
    return prefix + numberFormatter.stringFromNumber(NSNumber(amount)) + " %"
}