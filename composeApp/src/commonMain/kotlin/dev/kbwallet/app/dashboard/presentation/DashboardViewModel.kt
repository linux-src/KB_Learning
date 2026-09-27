package dev.kbwallet.app.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbwallet.app.coins.domain.GetCoinsListUseCase
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.util.RetryTrigger
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.core.util.formatPercentage
import dev.kbwallet.app.core.util.toUiText
import dev.kbwallet.app.portfolio.domain.PortfolioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

class DashboardViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val getCoinsListUseCase: GetCoinsListUseCase,
) : ViewModel() {

    // isLoading starts true (below) and the collectors in init{} below flip it to
    // false once, permanently, when their first real value arrives — there used
    // to be an onStart { isLoading = true } here too, resetting it back to true
    // on every resubscribe (i.e. every time you left this tab for 5+ seconds and
    // came back, per WhileSubscribed(5000)). Nothing in that resubscribe path
    // ever set it back to false again — the data collectors below only run once,
    // for the ViewModel's whole lifetime — so the spinner got stuck forever the
    // moment you revisited Dashboard after being away. That's what "namertvo
    // zavis" actually was.
    private val _state = MutableStateFlow(DashboardState(isLoading = true))
    val state: StateFlow<DashboardState> = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardState(isLoading = true)
        )

    private val retryTrigger = RetryTrigger()

    // Three independent async sources (portfolio coins, total balance, top coins)
    // all feed the single DashboardState.error shown on screen. Naively writing
    // `error = null` on any one source's success can silently hide another
    // source's still-active error (or resurrect a stale one), depending on
    // update order between the three concurrent collectors. Tracking each
    // source's last error separately and folding them into every _state.update
    // via combinedError() avoids that race.
    private var portfolioCoinsError: StringResource? = null
    private var totalBalanceError: StringResource? = null
    private var topCoinsError: StringResource? = null

    private var topCoinsJob: Job? = null

    private fun combinedError(): StringResource? = portfolioCoinsError ?: totalBalanceError ?: topCoinsError

    init {
        // ── Portfolio coins (reactive) ──
        viewModelScope.launch {
            retryTrigger.retryable { portfolioRepository.allPortfolioCoinsFlow() }.collect { result ->
                when (result) {
                    is Result.Success -> {
                        portfolioCoinsError = null
                        val coins = result.data
                        val totalFiat = coins.sumOf { it.ownedAmountInFiat }
                        val weightedPerf = if (totalFiat > 0) {
                            coins.sumOf { it.performancePercent * it.ownedAmountInFiat } / totalFiat
                        } else 0.0
                        val summaryItems = coins.take(3).map { coin ->
                            DashboardCoinItem(
                                id = coin.coin.id,
                                name = coin.coin.name,
                                symbol = coin.coin.symbol,
                                iconUrl = coin.coin.iconUrl,
                                formattedPrice = formatFiat(coin.ownedAmountInFiat),
                                formattedChange = formatPercentage(coin.performancePercent),
                                isPositive = coin.performancePercent >= 0,
                            )
                        }
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = combinedError(),
                                coinCount = coins.size,
                                portfolioSummaryCoins = summaryItems,
                                recentPerformance = formatPercentage(weightedPerf),
                                isPerformancePositive = weightedPerf >= 0,
                            )
                        }
                    }
                    is Result.Error -> {
                        portfolioCoinsError = result.error.toUiText()
                        _state.update { it.copy(isLoading = false, error = combinedError()) }
                    }
                }
            }
        }

        // ── Account balance (reactive) ──
        viewModelScope.launch {
            retryTrigger.retryable { portfolioRepository.accountBalanceFlow() }.collect { result ->
                when (result) {
                    is Result.Success -> {
                        totalBalanceError = null
                        val balance = result.data
                        _state.update {
                            it.copy(
                                totalValue = formatFiat(balance.total),
                                cashBalance = formatFiat(balance.cash),
                                holdingsValue = formatFiat(balance.holdings),
                                error = combinedError(),
                            )
                        }
                    }
                    is Result.Error -> {
                        totalBalanceError = result.error.toUiText()
                        _state.update { it.copy(error = combinedError()) }
                    }
                }
            }
        }

        loadTopCoins()
    }

    // ── Top coins (one-shot) ──
    private fun loadTopCoins() {
        topCoinsJob?.cancel()
        topCoinsJob = viewModelScope.launch {
            when (val coinsResult = getCoinsListUseCase.execute()) {
                is Result.Success -> {
                    topCoinsError = null
                    val topCoins = coinsResult.data.take(5).map { coin ->
                        DashboardCoinItem(
                            id = coin.coin.id,
                            name = coin.coin.name,
                            symbol = coin.coin.symbol,
                            iconUrl = coin.coin.iconUrl,
                            formattedPrice = formatFiat(coin.price),
                            formattedChange = formatPercentage(coin.change),
                            isPositive = coin.change >= 0,
                        )
                    }
                    _state.update { it.copy(topCoins = topCoins, error = combinedError()) }
                }
                is Result.Error -> {
                    topCoinsError = coinsResult.error.toUiText()
                    _state.update { it.copy(error = combinedError()) }
                }
            }
        }
    }

    /** Re-triggers the portfolio/balance flows and the top-coins fetch after an error. */
    fun retry() {
        retryTrigger.retry()
        loadTopCoins()
    }
}
