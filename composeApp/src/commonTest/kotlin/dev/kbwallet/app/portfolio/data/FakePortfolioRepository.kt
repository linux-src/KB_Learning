package dev.kbwallet.app.portfolio.data

import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.EmptyResult
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.domain.coin.Coin
import dev.kbwallet.app.history.data.LimitOrderEntity
import dev.kbwallet.app.history.data.TransactionEntity
import dev.kbwallet.app.portfolio.domain.AccountBalance
import dev.kbwallet.app.portfolio.domain.PortfolioCoinModel
import dev.kbwallet.app.portfolio.domain.PortfolioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakePortfolioRepository : PortfolioRepository {

    private val _data = MutableStateFlow<Result<List<PortfolioCoinModel>, DataError.Remote>>(
        Result.Success(emptyList())
    )

    private val _cashBalance = MutableStateFlow(cashBalance)
    private val _portfolioValue = MutableStateFlow(portfolioValue)
    private val _totalBalanceError = MutableStateFlow<DataError.Remote?>(null)

    private val listOfCoins = mutableListOf<PortfolioCoinModel>()

    override suspend fun initializeBalance() {
        // no-op
    }

    override fun allPortfolioCoinsFlow(): Flow<Result<List<PortfolioCoinModel>, DataError.Remote>> {
        return _data.asStateFlow()
    }

    override suspend fun getPortfolioCoin(coinId: String): Result<PortfolioCoinModel?, DataError.Remote> {
        return Result.Success(listOfCoins.find { it.coin.id == coinId })
    }

    override suspend fun savePortfolioCoin(portfolioCoin: PortfolioCoinModel): EmptyResult<DataError.Local> {
        listOfCoins.add(portfolioCoin)
        _portfolioValue.value = listOfCoins.sumOf { it.ownedAmountInFiat }
        _data.value = Result.Success(listOfCoins)
        return Result.Success(Unit)
    }

    override suspend fun removeCoinFromPortfolio(coinId: String) {
        _data.update { Result.Success(emptyList()) }
    }

    override fun calculateTotalPortfolioValue(): Flow<Result<Double, DataError.Remote>> {
        return _portfolioValue.map { Result.Success(it) }
    }

    override fun accountBalanceFlow(): Flow<Result<AccountBalance, DataError.Remote>> {
        return combine(_cashBalance, _portfolioValue, _totalBalanceError) { cashBalance, portfolioValue, error ->
            if (error != null) {
                Result.Error(error)
            } else {
                Result.Success(AccountBalance(cash = cashBalance, holdings = portfolioValue))
            }
        }
    }

    override fun cashBalanceFlow(): Flow<Double> {
        return _cashBalance.asStateFlow()
    }

    override suspend fun updateCashBalance(newBalance: Double) {
        _cashBalance.value = newBalance
    }

    override suspend fun recordTransaction(
        coinId: String,
        coinName: String,
        coinSymbol: String,
        type: String,
        amountInFiat: Double,
        amountInUnit: Double,
        pricePerUnit: Double,
    ) {
        // no-op
    }

    override fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return flowOf(emptyList())
    }

    override suspend fun getTotalTradeCount(): Int = 0
    override suspend fun getTotalBuyCount(): Int = 0
    override suspend fun getTotalSellCount(): Int = 0

    override suspend fun updateTransactionNotes(transactionId: Long, notes: String, tags: String) {
        // no-op
    }

    // ── Limit Orders ──
    private val _limitOrders = MutableStateFlow<List<LimitOrderEntity>>(emptyList())

    override suspend fun placeLimitOrder(order: LimitOrderEntity) {
        _limitOrders.update { it + order }
    }

    override fun getActiveLimitOrders(): Flow<List<LimitOrderEntity>> {
        return _limitOrders.map { orders -> orders.filter { it.status == "ACTIVE" } }
    }

    override fun getAllLimitOrders(): Flow<List<LimitOrderEntity>> {
        return _limitOrders.asStateFlow()
    }

    override suspend fun cancelLimitOrder(orderId: Long) {
        _limitOrders.update { orders ->
            orders.map { if (it.id == orderId) it.copy(status = "CANCELLED") else it }
        }
    }

    override suspend fun getActiveLimitOrdersList(): List<LimitOrderEntity> {
        return _limitOrders.value.filter { it.status == "ACTIVE" }
    }

    fun simulateError() {
        _data.value = Result.Error(DataError.Remote.SERVER)
    }

    /** Independently controllable from [simulateError] — see DashboardViewModelTest. */
    fun simulateTotalBalanceError(error: DataError.Remote = DataError.Remote.SERVER) {
        _totalBalanceError.value = error
    }

    fun clearTotalBalanceError() {
        _totalBalanceError.value = null
    }

    companion object {
        val fakeCoin = Coin(
            id = "fakeId",
            name = "Fake Coin",
            symbol = "FAKE",
            iconUrl = "https://fake.url/fake.png"
        )
        val portfolioCoin = PortfolioCoinModel(
            coin = fakeCoin,
            ownedAmountInUnit = 1000.0,
            ownedAmountInFiat = 3000.0,
            performancePercent = 10.0,
            averagePurchasePrice = 10.0,
        )
        val cashBalance = 10000.0
        val portfolioValue = 0.0
    }
}