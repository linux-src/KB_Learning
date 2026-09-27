package dev.kbwallet.app.trade.presentation.buy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbwallet.app.coins.domain.GetCoinDetailsUseCase
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.core.util.toPlainAmountString
import dev.kbwallet.app.core.util.toUiText
import dev.kbwallet.app.notifications.domain.NotificationController
import dev.kbwallet.app.portfolio.domain.PortfolioRepository
import dev.kbwallet.app.trade.domain.BuyCoinUseCase
import dev.kbwallet.app.trade.presentation.common.TradeState
import dev.kbwallet.app.trade.presentation.common.UiTradeCoinItem
import dev.kbwallet.app.trade.presentation.mapper.toCoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BuyViewModel(
    private val getCoinDetailsUseCase: GetCoinDetailsUseCase,
    private val portfolioRepository: PortfolioRepository,
    private val buyCoinUseCase: BuyCoinUseCase,
    private val notificationController: NotificationController,
    private val coinId: String,
) : ViewModel() {

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

    private val _events = Channel<BuyEvents>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private suspend fun getCoinDetails(balance: Double) {
        when (val coinResponse = getCoinDetailsUseCase.execute(coinId)) {
            is Result.Success -> {
                _state.update {
                    it.copy(
                        coin = UiTradeCoinItem(
                            id = coinResponse.data.coin.id,
                            name = coinResponse.data.coin.name,
                            symbol = coinResponse.data.coin.symbol,
                            iconUrl = coinResponse.data.coin.iconUrl,
                            price = coinResponse.data.price,
                        ),
                        availableAmount = formatFiat(balance),
                        availableBalance = balance
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
        val currentAmount = _amount.value.toDoubleOrNull() ?: 0.0
        _state.update { it.copy(isAmountInUnits = !it.isAmountInUnits, fiatEquivalent = "") }
        // Clear amount when switching mode
        _amount.value = ""
        // Update available amount for the new mode
        viewModelScope.launch {
            val balance = portfolioRepository.cashBalanceFlow().first()
            if (_state.value.isAmountInUnits) {
                val maxUnits = if (currentCoin.price > 0) balance / currentCoin.price else 0.0
                _state.update {
                    it.copy(
                        availableAmount = "${formatFiat(maxUnits, showDecimal = false)} ${currentCoin.symbol}",
                        availableBalance = maxUnits
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        availableAmount = formatFiat(balance),
                        availableBalance = balance
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

    fun onBuyClicked() {
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
            val buyCoinResponse = buyCoinUseCase.buyCoin(
                coin = tradeCoin.toCoin(),
                amountInFiat = fiatAmount,
                price = tradeCoin.price,
            )

            when(buyCoinResponse) {
                is Result.Success -> {
                    _amount.value = ""
                    _state.update { it.copy(isLoading = false, error = null) }
                    val amountInUnit = if (tradeCoin.price > 0) fiatAmount / tradeCoin.price else 0.0
                    // Recorded before the event so the notification centre is
                    // already up to date by the time the success screen offers
                    // to jump to it.
                    notificationController.postPurchase(
                        coinSymbol = tradeCoin.symbol,
                        amountInUnit = amountInUnit,
                        amountInFiat = fiatAmount,
                    )
                    _events.send(
                        BuyEvents.BuySuccess(
                            coinSymbol = tradeCoin.symbol,
                            amountInUnit = amountInUnit,
                            amountInFiat = fiatAmount,
                        )
                    )
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = buyCoinResponse.error.toUiText(),
                        )
                    }
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            portfolioRepository.cashBalanceFlow().collect { balance ->
                val currentCoin = _state.value.coin
                if (currentCoin == null) {
                    getCoinDetails(balance)
                } else {
                    if (_state.value.isAmountInUnits) {
                        val maxUnits = if (currentCoin.price > 0) balance / currentCoin.price else 0.0
                        _state.update {
                            it.copy(
                                availableAmount = "${formatFiat(maxUnits, showDecimal = false)} ${currentCoin.symbol}",
                                availableBalance = maxUnits
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                availableAmount = formatFiat(balance),
                                availableBalance = balance
                            )
                        }
                    }
                }
            }
        }
    }
}

sealed interface BuyEvents {
    /**
     * Carries what was actually bought so the success animation can name it —
     * the ViewModel has already cleared the amount field by the time the UI
     * reacts, so the screen can't read it back off the state.
     */
    data class BuySuccess(
        val coinSymbol: String,
        val amountInUnit: Double,
        val amountInFiat: Double,
    ) : BuyEvents
}
