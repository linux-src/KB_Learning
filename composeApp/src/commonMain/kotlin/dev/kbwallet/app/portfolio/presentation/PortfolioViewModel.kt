package dev.kbwallet.app.portfolio.presentation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.util.RetryTrigger
import dev.kbwallet.app.core.util.formatCoinUnit
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.core.util.formatPercentage
import dev.kbwallet.app.core.util.toUiText
import dev.kbwallet.app.portfolio.domain.AccountBalance
import dev.kbwallet.app.portfolio.domain.PortfolioCoinModel
import dev.kbwallet.app.portfolio.domain.PortfolioRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class PortfolioViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _state = MutableStateFlow(PortfolioState(isLoading = true))

    private val retryTrigger = RetryTrigger()
    private val portfolioCoinsFlow = retryTrigger.retryable { portfolioRepository.allPortfolioCoinsFlow() }
    // Cash used to be collected as a fourth source alongside the total; the
    // account balance now carries both halves, so they can't disagree about
    // which cash figure the total was built from.
    private val accountBalanceFlow = retryTrigger.retryable { portfolioRepository.accountBalanceFlow() }

    val state: StateFlow<PortfolioState> = combine(
        _state,
        portfolioCoinsFlow,
        accountBalanceFlow,
    ) { currentState, portfolioCoinsResponse, accountBalanceResult ->
        when (portfolioCoinsResponse) {
            is Result.Success -> {
                handleSuccessState(
                    currentState = currentState,
                    portfolioCoins = portfolioCoinsResponse.data,
                    accountBalanceResult = accountBalanceResult,
                )
            }
            is Result.Error -> {
                handleErrorState(
                    currentState = currentState,
                    portfolioCoinsResponse.error
                )
            }
        }
    }.onStart {
        portfolioRepository.initializeBalance()
    }.flowOn(coroutineDispatcher).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PortfolioState(isLoading = true)
    )

    /** Re-triggers both the portfolio-coins and total-balance fetches after an error. */
    fun retry() {
        retryTrigger.retry()
    }

    private fun handleSuccessState(
        currentState: PortfolioState,
        portfolioCoins: List<PortfolioCoinModel>,
        accountBalanceResult: Result<AccountBalance, DataError>,
    ): PortfolioState {
        // The coin list itself loaded fine, so it's still shown — only the
        // balance figures and the error banner reflect this failure, rather
        // than silently showing "$0" as if the user's balance really were zero.
        val balance = when (accountBalanceResult) {
            is Result.Success -> accountBalanceResult.data
            is Result.Error -> null
        }

        return currentState.copy(
            coins = portfolioCoins.map { it.toUiPortfolioCoinItem() },
            totalValue = balance?.let { formatFiat(it.total) } ?: currentState.totalValue,
            holdingsValue = balance?.let { formatFiat(it.holdings) } ?: currentState.holdingsValue,
            cashBalance = balance?.let { formatFiat(it.cash) } ?: currentState.cashBalance,
            showBuyButton = portfolioCoins.isNotEmpty(),
            isLoading = false,
            error = (accountBalanceResult as? Result.Error)?.error?.toUiText(),
        )
    }

    private fun handleErrorState(
        currentState: PortfolioState,
        error: DataError,
    ): PortfolioState {
        return currentState.copy(
            isLoading = false,
            error = error.toUiText()
        )
    }

    private fun PortfolioCoinModel.toUiPortfolioCoinItem(): UiPortfolioCoinItem {
        return UiPortfolioCoinItem(
            id = coin.id,
            name = coin.name,
            iconUrl = coin.iconUrl,
            amountInUnitText = formatCoinUnit(ownedAmountInUnit, coin.symbol),
            amountInFiatText = formatFiat(ownedAmountInFiat),
            performancePercentText = formatPercentage(performancePercent),
            isPositive = performancePercent >= 0,
            symbol = coin.symbol,
            amountInFiat = ownedAmountInFiat,
        )
    }
}
