package dev.kbwallet.app.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import dev.kbwallet.app.history.data.LimitOrderDao
import dev.kbwallet.app.history.data.LimitOrderEntity
import dev.kbwallet.app.history.data.TransactionDao
import dev.kbwallet.app.history.data.TransactionEntity
import dev.kbwallet.app.notifications.data.NotificationDao
import dev.kbwallet.app.notifications.data.NotificationEntity
import dev.kbwallet.app.portfolio.data.local.PortfolioCoinEntity
import dev.kbwallet.app.portfolio.data.local.PortfolioDao
import dev.kbwallet.app.portfolio.data.local.UserBalanceDao
import dev.kbwallet.app.portfolio.data.local.UserBalanceEntity
import dev.kbwallet.app.watchlist.data.WatchlistDao
import dev.kbwallet.app.watchlist.data.WatchlistEntity

/**
 * The single shared Room database for the whole app — portfolio holdings,
 * watchlist, transaction history, and limit orders all live here together.
 * Was previously named `PortfolioDatabase` (in a `database.portfolio`
 * subpackage), which read as portfolio-only storage even though watchlist/
 * history/limit-orders have always lived here too — renamed for clarity,
 * not behavior. The on-disk file name stays "portfolio.db" (see the
 * per-platform getAppDatabaseBuilder() in this package) — renaming that too
 * would silently orphan every existing install's local data on update.
 */
@ConstructedBy(AppDatabaseCreator::class)
@Database(
    entities = [
        PortfolioCoinEntity::class,
        UserBalanceEntity::class,
        TransactionEntity::class,
        LimitOrderEntity::class,
        WatchlistEntity::class,
        NotificationEntity::class,
    ],
    version = 8
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
    abstract fun userBalanceDao(): UserBalanceDao
    abstract fun transactionDao(): TransactionDao
    abstract fun limitOrderDao(): LimitOrderDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun notificationDao(): NotificationDao
}
