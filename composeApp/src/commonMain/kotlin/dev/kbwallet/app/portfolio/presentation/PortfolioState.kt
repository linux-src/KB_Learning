package dev.kbwallet.app.portfolio.presentation

import org.jetbrains.compose.resources.StringResource

data class PortfolioState(
    /** Cash + holdings — what the whole account is worth. */
    val totalValue: String = "",
    /** Market value of the coins held. */
    val holdingsValue: String = "",
    /** Spendable fiat, kept apart from [holdingsValue] rather than folded in. */
    val cashBalance: String = "",
    val showBuyButton: Boolean = false,
    val isLoading: Boolean = false,
    val error: StringResource? = null,
    val coins: List<UiPortfolioCoinItem> = emptyList(),
)
