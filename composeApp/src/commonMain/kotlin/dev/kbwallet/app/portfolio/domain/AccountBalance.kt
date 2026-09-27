package dev.kbwallet.app.portfolio.domain

/**
 * The two halves of what the account is worth, kept apart on purpose.
 *
 * These used to be collapsed into a single "total balance" Double before it
 * ever reached the UI, which is why both the Dashboard's "Portfolio Value"
 * card and the Portfolio screen's balance headline were really showing cash
 * plus holdings — and the un-invested cash had no figure of its own anywhere.
 *
 * @property cash     Un-invested fiat: the currency balance, what's spendable.
 * @property holdings Market value of every coin held right now.
 */
data class AccountBalance(
    val cash: Double,
    val holdings: Double,
) {
    /** Everything the account is worth. */
    val total: Double get() = cash + holdings
}
