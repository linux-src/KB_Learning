package dev.kbwallet.app.portfolio.data

import androidx.sqlite.SQLiteException
import dev.kbwallet.app.coins.domain.api.CoinsRemoteDataSource
import dev.kbwallet.app.core.domain.DataError
import dev.kbwallet.app.core.domain.EmptyResult
import dev.kbwallet.app.core.domain.Result
import dev.kbwallet.app.core.domain.onError
import dev.kbwallet.app.core.domain.onSuccess
import dev.kbwallet.app.history.data.LimitOrderDao
import dev.kbwallet.app.history.data.LimitOrderEntity
import dev.kbwallet.app.history.data.TransactionDao
import dev.kbwallet.app.history.data.TransactionEntity
import dev.kbwallet.app.portfolio.data.local.PortfolioDao
import dev.kbwallet.app.portfolio.data.local.UserBalanceDao
import dev.kbwallet.app.portfolio.data.local.UserBalanceEntity
import dev.kbwallet.app.portfolio.data.mapper.toPortfolioCoinEntity
import dev.kbwallet.app.portfolio.data.mapper.toPortfolioCoinModel
import dev.kbwallet.app.portfolio.domain.AccountBalance
import dev.kbwallet.app.portfolio.domain.PortfolioCoinModel
import dev.kbwallet.app.portfolio.domain.PortfolioRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class PortfolioRepositoryImpl(
    private val portfolioDao: PortfolioDao,
    private val userBalanceDao: UserBalanceDao,
    private val transactionDao: TransactionDao,
    private val limitOrderDao: LimitOrderDao,
    private val coinsRemoteDataSource: CoinsRemoteDataSource,
) : PortfolioRepository {

    override suspend fun initializeBalance() {
        val currentBalance = userBalanceDao.getCashBalance()
        if (currentBalance == null) {
            userBalanceDao.insertBalance(
                UserBalanceEntity(cashBalance = 10000.0)
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun allPortfolioCoinsFlow(): Flow<Result<List<PortfolioCoinModel>, DataError.Remote>> {
        return portfolioDao.getAllOwnedCoins().flatMapLatest { portfolioCoinEntities ->
            if (portfolioCoinEntities.isEmpty()) {
                flow {
                    emit(Result.Success(emptyList<PortfolioCoinModel>()))
                }
            } else {
                flow {
                    coinsRemoteDataSource.getListOfCoins()
                        .onError { error ->
                            emit(Result.Error(error))
                        }
                        .onSuccess { coins ->
                            val portfolioCoins =
                                portfolioCoinEntities.mapNotNull { portfolioCoinEntity ->
                                    val coin = coins.find { it.id == portfolioCoinEntity.coinId }
                                    coin?.let {
                                        portfolioCoinEntity.toPortfolioCoinModel(it.currentPrice)
                                    }
                                }
                            emit(Result.Success(portfolioCoins))
                        }
                }
            }
        }.catch {
            emit(Result.Error(DataError.Remote.UNKNOWN))
        }
    }

    override suspend fun getPortfolioCoin(coinId: String): Result<PortfolioCoinModel?, DataError.Remote> {
        coinsRemoteDataSource.getCoinById(coinId)
            .onError { error ->
                return Result.Error(error)
            }
            .onSuccess { coinDto ->
                val portfolioCoinEntity = portfolioDao.getCoinById(coinId)
                val currentPrice = coinDto.marketData?.currentPrice?.get("usd") ?: 0.0
                return if (portfolioCoinEntity != null) {
                    Result.Success(portfolioCoinEntity.toPortfolioCoinModel(currentPrice))
                } else {
                    Result.Success(null)
                }
            }
        return Result.Error(DataError.Remote.UNKNOWN)
    }

    override suspend fun savePortfolioCoin(portfolioCoin: PortfolioCoinModel): EmptyResult<DataError.Local> {
        try {
            portfolioDao.insert(portfolioCoin.toPortfolioCoinEntity())
            return Result.Success(Unit)
        } catch (e: SQLiteException) {
            return Result.Error(DataError.Local.DISK_FULL)
        }
    }

    override suspend fun removeCoinFromPortfolio(coinId: String) {
        portfolioDao.deletePortfolioItem(coinId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun calculateTotalPortfolioValue(): Flow<Result<Double, DataError.Remote>> {
        return portfolioDao.getAllOwnedCoins().flatMapLatest { portfolioCoinsEntities ->
            if (portfolioCoinsEntities.isEmpty()) {
                flow {
                    emit(Result.Success(0.0))
                }
            } else {
                flow {
                    val apiResult = coinsRemoteDataSource.getListOfCoins()
                    apiResult.onError { error ->
                        emit(Result.Error(error))
                    }.onSuccess { coins ->
                        val totalValue = portfolioCoinsEntities.sumOf { ownedCoin ->
                            val coinPrice = coins.find { it.id == ownedCoin.coinId }?.currentPrice ?: 0.0
                            ownedCoin.amountOwned * coinPrice
                        }
                        emit(Result.Success(totalValue))
                    }
                }
            }
        }.catch {
            emit(Result.Error(DataError.Remote.UNKNOWN))
        }
    }

    override fun cashBalanceFlow(): Flow<Double> {
        return userBalanceDao.getCashBalanceFlow().map { it ?: 10000.0 }
    }

    override fun accountBalanceFlow(): Flow<Result<AccountBalance, DataError.Remote>> {
        return combine(
            cashBalanceFlow(),
            calculateTotalPortfolioValue()
        ) { cashBalance, portfolioResult ->
            when (portfolioResult) {
                is Result.Success -> {
                    Result.Success(AccountBalance(cash = cashBalance, holdings = portfolioResult.data))
                }
                is Result.Error -> {
                    // Only the holdings half can fail (it needs live prices).
                    // Cash is local and always known, but a partial balance
                    // would be misread as the whole account — the caller keeps
                    // showing its last good figures instead.
                    Result.Error(portfolioResult.error)
                }
            }
        }
    }

    override suspend fun updateCashBalance(newBalance: Double) {
        userBalanceDao.updateCashBalance(newBalance)
    }

    // ── Transaction history ──

    override suspend fun recordTransaction(
        coinId: String,
        coinName: String,
        coinSymbol: String,
        type: String,
        amountInFiat: Double,
        amountInUnit: Double,
        pricePerUnit: Double,
    ) {
        transactionDao.insert(
            TransactionEntity(
                coinId = coinId,
                coinName = coinName,
                coinSymbol = coinSymbol,
                type = type,
                amountInFiat = amountInFiat,
                amountInUnit = amountInUnit,
                pricePerUnit = pricePerUnit,
                timestamp = Clock.System.now().toEpochMilliseconds(),
                status = "Completed",
            )
        )
    }

    override fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    override suspend fun getTotalTradeCount(): Int {
        return transactionDao.getTotalTradeCount()
    }

    override suspend fun getTotalBuyCount(): Int {
        return transactionDao.getTotalBuyCount()
    }

    override suspend fun getTotalSellCount(): Int {
        return transactionDao.getTotalSellCount()
    }

    override suspend fun updateTransactionNotes(transactionId: Long, notes: String, tags: String) {
        transactionDao.updateNotes(transactionId, notes, tags)
    }

    // ── Limit Orders ──

    override suspend fun placeLimitOrder(order: LimitOrderEntity) {
        limitOrderDao.insert(order)
    }

    override fun getActiveLimitOrders(): Flow<List<LimitOrderEntity>> {
        return limitOrderDao.getActiveOrders()
    }

    override fun getAllLimitOrders(): Flow<List<LimitOrderEntity>> {
        return limitOrderDao.getAllOrders()
    }

    override suspend fun cancelLimitOrder(orderId: Long) {
        limitOrderDao.updateStatus(orderId, "CANCELLED")
    }

    override suspend fun getActiveLimitOrdersList(): List<LimitOrderEntity> {
        return limitOrderDao.getActiveOrdersList()
    }
}
