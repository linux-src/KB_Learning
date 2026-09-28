package dev.kbwallet.app.profile.presentation

import androidx.lifecycle.ViewModel
import dev.kbwallet.app.portfolio.domain.PortfolioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

import dev.kbwallet.app.profile.domain.UserRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class ProfileViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadSettings()
        loadStats()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Fetch initial state if empty (or to populate the flow)
            userRepository.getProfileState()
            
            // Collect updates from the flow
            userRepository.profileState.collect { newState ->
                // Stats come from the trade history, not the stored profile.
                _state.update {
                    newState.copy(
                        totalTrades = it.totalTrades,
                        winRate = it.winRate,
                        daysActive = it.daysActive,
                    )
                }
            }
        }
    }

    private fun saveSettings() {
        viewModelScope.launch {
            userRepository.saveProfileState(_state.value)
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            portfolioRepository.getAllTransactions().collect { transactions ->
                val stats = computeTradeStats(
                    transactions.map { TradeRecord(it.coinId, it.type == "BUY", it.amountInUnit, it.pricePerUnit, it.timestamp) },
                    nowMillis = Clock.System.now().toEpochMilliseconds(),
                )
                _state.update {
                    it.copy(
                        totalTrades = stats.totalTrades,
                        winRate = stats.winRatePercent?.let { rate -> "$rate%" } ?: "—",
                        daysActive = stats.daysActive.toString(),
                    )
                }
            }
        }
    }

    fun updateDisplayName(name: String) {
        _state.update {
            it.copy(
                displayName = name,
                avatarInitial = name.firstOrNull()?.uppercase() ?: "C"
            )
        }
        saveSettings()
    }

    fun updateEmail(email: String) {
        _state.update { it.copy(email = email) }
        saveSettings()
    }

    fun togglePushNotifications() {
        _state.update { it.copy(pushNotifications = !it.pushNotifications) }
        saveSettings()
    }

    fun toggleEmailNotifications() {
        _state.update { it.copy(emailNotifications = !it.emailNotifications) }
        saveSettings()
    }

    fun togglePriceAlerts() {
        _state.update { it.copy(priceAlerts = !it.priceAlerts) }
        saveSettings()
    }

    fun toggleTradeConfirmations() {
        _state.update { it.copy(tradeConfirmations = !it.tradeConfirmations) }
        saveSettings()
    }

    fun toggleNewsUpdates() {
        _state.update { it.copy(newsUpdates = !it.newsUpdates) }
        saveSettings()
    }

    fun toggleBiometricAuth() {
        _state.update { it.copy(biometricAuth = !it.biometricAuth) }
        saveSettings()
    }
    fun toggleTwoFactorAuth() {
        _state.update { it.copy(twoFactorAuth = !it.twoFactorAuth) }
        saveSettings()
    }
}

internal data class TradeRecord(
    val coinId: String,
    val isBuy: Boolean,
    val units: Double,
    val price: Double,
    val timestamp: Long,
)

internal data class TradeStats(
    val totalTrades: Int,
    /** Share of sells priced above the running average buy cost; null with no sells. */
    val winRatePercent: Int?,
    val daysActive: Int,
)

/**
 * A sell "wins" when it's executed above the average cost of the units held
 * at that moment (running weighted average per coin).
 */
internal fun computeTradeStats(trades: List<TradeRecord>, nowMillis: Long): TradeStats {
    val held = mutableMapOf<String, Pair<Double, Double>>() // coinId -> (units, avgCost)
    var sells = 0
    var wins = 0
    trades.sortedBy { it.timestamp }.forEach { t ->
        val (units, avg) = held[t.coinId] ?: (0.0 to 0.0)
        if (t.isBuy) {
            val newUnits = units + t.units
            val newAvg = if (newUnits > 0) (units * avg + t.units * t.price) / newUnits else 0.0
            held[t.coinId] = newUnits to newAvg
        } else {
            sells++
            if (avg > 0 && t.price > avg) wins++
            held[t.coinId] = (units - t.units).coerceAtLeast(0.0) to avg
        }
    }
    val first = trades.minOfOrNull { it.timestamp }
    val days = if (first == null) 1 else ((nowMillis - first) / 86_400_000L).toInt() + 1
    return TradeStats(
        totalTrades = trades.size,
        winRatePercent = if (sells == 0) null else (wins * 100 / sells),
        daysActive = days.coerceAtLeast(1),
    )
}
