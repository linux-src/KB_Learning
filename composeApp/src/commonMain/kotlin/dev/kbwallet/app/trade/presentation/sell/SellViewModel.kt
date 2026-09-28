package dev.kbwallet.app.trade.presentation.sell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbwallet.app.coins.domain.GetCoinDetailsUseCase
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.core.util.toPlainAmountString
import dev.kbwallet.app.core.util.toUiText
import dev.kbwallet.app.portfolio.domain.PortfolioRepository
import dev.kbwallet.app.trade.domain.SellCoinUseCase
import dev.kbwallet.app.trade.presentation.common.TradeState
import dev.kbwallet.app.trade.presentation.common.UiTradeCoinItem
import dev.kbwallet.app.trade.presentation.mapper.toCoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SellViewModel(
    private val getCoinDetailsUseCase: GetCoinDetailsUseCase,
    private val portfolioRepository: PortfolioRepository,
    private val sellCoinUseCase: SellCoinUseCase,
    private val coinId: String,
): ViewModel() {

    private val _amount = MutableStateFlow("")
    private val _state = MutableStateFlow(TradeState())
    val state = combine(
        _state,
        _amount,
    ) { state, amount ->
        state.copy(
            amount = amount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TradeState(isLoading = true)
    )
    private val _events = Channel<SellEvents>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onAmountChanged(amount: String) {
        _amount.value = amount
        if (_state.value.isAmountInUnits && _state.value.coin != null) {
            val num = amount.toDoubleOrNull()
            _state.update {
                it.copy(
                    fiatEquivalent = if (num != null) "≈ ${formatFiat(num * it.coin!!.price)}"
                    else ""
                )
            }
        }
    }

    fun onToggleMode() {
        val currentCoin = _state.value.coin ?: return
        _state.update { it.copy(isAmountInUnits = !it.isAmountInUnits, fiatEquivalent = "") }
        _amount.value = ""
        viewModelScope.launch {
            when (val portfolioCoinResponse = portfolioRepository.getPortfolioCoin(coinId)) {
                is Result.Success -> {
                    val owned = portfolioCoinResponse.data?.ownedAmountInUnit ?: 0.0
                    val ownedFiat = (owned * currentCoin.price)
                    if (_state.value.isAmountInUnits) {
                        _state.update {
                            it.copy(
                                availableAmount = "${formatFiat(owned, showDecimal = false)} ${currentCoin.symbol}",
                                availableBalance = owned,
                                error = null,
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                availableAmount = formatFiat(ownedFiat),
                                availableBalance = ownedFiat,
                                error = null,
                            )
                        }
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(error = portfolioCoinResponse.error.toUiText()) }
                }
            }
        }
    }

    private suspend fun getCoinDetails(ownedAmountInUnit: Double) {
        when(val coinResponse = getCoinDetailsUseCase.execute(coinId)) {
            is Result.Success -> {
                val availableAmountInFiat = ownedAmountInUnit * coinResponse.data.price
                _state.update {
                    it.copy(
                        coin = UiTradeCoinItem(
                            id = coinResponse.data.coin.id,
                            name = coinResponse.data.coin.name,
                            symbol = coinResponse.data.coin.symbol,
                            iconUrl = coinResponse.data.coin.iconUrl,
                            price = coinResponse.data.price,
                        ),
                        availableAmount = formatFiat(availableAmountInFiat),
                        availableBalance = availableAmountInFiat
                    )
                }
            }
            is Result.Error -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = coinResponse.error.toUiText()
                    )
                }
            }
        }
    }

    fun onPercentageClicked(fraction: Double) {
        val amount = _state.value.availableBalance * fraction
        if (amount > 0) {
            onAmountChanged(amount.toPlainAmountString())
        }
    }

    fun onSellClicked() {
        if (_state.value.isLoading) return
        val tradeCoin = state.value.coin ?: return
        val amount = _amount.value.toDoubleOrNull() ?: return
        if (amount <= 0.0) return
        if (amount > _state.value.availableBalance) {
            _state.update { it.copy(error = dev.kbwallet.app.core.domain.DataError.Local.INSUFFICIENT_FUNDS.toUiText()) }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val fiatAmount = if (_state.value.isAmountInUnits) amount * tradeCoin.price else amount
            val sellCoinResponse = sellCoinUseCase.sellCoin(
                coin = tradeCoin.toCoin(),
                amountInFiat = fiatAmount,
                price = tradeCoin.price
            )
            when (sellCoinResponse) {
                is Result.Success -> {
                    _amount.value = ""
                    _state.update { it.copy(isLoading = false, error = null) }
                    _events.send(
                        SellEvents.SellSuccess(
                            coinSymbol = tradeCoin.symbol,
                            amountInUnit = if (tradeCoin.price > 0) fiatAmount / tradeCoin.price else 0.0,
                            amountInFiat = fiatAmount,
                            price = tradeCoin.price,
                        )
                    )
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = sellCoinResponse.error.toUiText()
                        )
                    }
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            portfolioRepository.allPortfolioCoinsFlow().collect { coinsResult ->
                if (coinsResult is Result.Success) {
                    val ownedUnits = coinsResult.data.find { it.coin.id == coinId }?.ownedAmountInUnit ?: 0.0
                    val currentCoin = _state.value.coin
                    if (currentCoin == null) {
                        getCoinDetails(ownedUnits)
                    } else {
                        val ownedFiat = ownedUnits * currentCoin.price
                        if (_state.value.isAmountInUnits) {
                            _state.update {
                                it.copy(
                                    availableAmount = "${formatFiat(ownedUnits, showDecimal = false)} ${currentCoin.symbol}",
                                    availableBalance = ownedUnits
                                )
                            }
                        } else {
                            _state.update {
                                it.copy(
                                    availableAmount = formatFiat(ownedFiat),
                                    availableBalance = ownedFiat
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
sealed interface SellEvents {
    data class SellSuccess(
        val coinSymbol: String,
        val amountInUnit: Double,
        val amountInFiat: Double,
        val price: Double,
    ) : SellEvents
}
