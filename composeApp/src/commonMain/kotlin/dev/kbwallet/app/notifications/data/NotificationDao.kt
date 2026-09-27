package dev.kbwallet.app.notifications.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert
    suspend fun insert(notification: NotificationEntity)

    @Query("SELECT * FROM NotificationEntity ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM NotificationEntity WHERE isRead = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("UPDATE NotificationEntity SET isRead = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("UPDATE NotificationEntity SET isRead = 1")
    suspend fun markAllRead()

    @Query("DELETE FROM NotificationEntity")
    suspend fun clearAll()
}
