package dev.kbwallet.app.portfolio.domain

import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.EmptyResult
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.history.data.LimitOrderEntity
import dev.kbwallet.app.history.data.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface PortfolioRepository {

    suspend fun initializeBalance()
    fun allPortfolioCoinsFlow(): Flow<Result<List<PortfolioCoinModel>, DataError.Remote>>
    suspend fun getPortfolioCoin(coinId: String): Result<PortfolioCoinModel?, DataError.Remote>
    suspend fun savePortfolioCoin(portfolioCoin: PortfolioCoinModel): EmptyResult<DataError.Local>
    suspend fun removeCoinFromPortfolio(coinId: String)

    /** Market value of the holdings alone — no cash. */
    fun calculateTotalPortfolioValue(): Flow<Result<Double, DataError.Remote>>

    /** Cash and holdings side by side; see [AccountBalance]. */
    fun accountBalanceFlow(): Flow<Result<AccountBalance, DataError.Remote>>

    /** Spendable fiat only. */
    fun cashBalanceFlow(): Flow<Double>
    suspend fun updateCashBalance(newBalance: Double)

    // ── Transaction history ──
    suspend fun recordTransaction(
        coinId: String,
        coinName: String,
        coinSymbol: String,
        type: String,
        amountInFiat: Double,
        amountInUnit: Double,
        pricePerUnit: Double,
    )
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    suspend fun getTotalTradeCount(): Int
    suspend fun getTotalBuyCount(): Int
    suspend fun getTotalSellCount(): Int
    suspend fun updateTransactionNotes(transactionId: Long, notes: String, tags: String)

    // ── Limit Orders ──
    suspend fun placeLimitOrder(order: LimitOrderEntity)
    fun getActiveLimitOrders(): Flow<List<LimitOrderEntity>>
    fun getAllLimitOrders(): Flow<List<LimitOrderEntity>>
    suspend fun cancelLimitOrder(orderId: Long)
    suspend fun getActiveLimitOrdersList(): List<LimitOrderEntity>
}
