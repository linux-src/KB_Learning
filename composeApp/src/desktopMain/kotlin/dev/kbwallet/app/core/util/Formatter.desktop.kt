package dev.kbwallet.app.core.util

import java.text.DecimalFormat

actual fun formatFiat(amount: Double, showDecimal: Boolean): String {
    val absAmount = kotlin.math.abs(amount)
    val formatter = when {
        showDecimal.not() -> DecimalFormat("#,###")
        absAmount == 0.0 || absAmount >= 0.01 -> DecimalFormat("#,##0.00")
        else -> DecimalFormat("0.00######")
    }
    val sign = if (amount < 0 && formatter.format(absAmount) != formatter.format(0.0)) "-" else ""
    return "$sign$ " + formatter.format(absAmount)
}

actual fun formatCoinUnit(amount: Double, symbol: String): String {
    return DecimalFormat("0.00000000").format(amount) + " $symbol"
}

actual fun formatPercentage(amount: Double): String {
    val prefix = if (amount >= 0) "+" else ""
    return prefix + DecimalFormat("0.00").format(amount) + " %"
}
