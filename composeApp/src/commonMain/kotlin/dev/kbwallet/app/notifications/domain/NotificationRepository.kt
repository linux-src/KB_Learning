package dev.kbwallet.app.notifications.domain

import dev.kbwallet.app.notifications.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    /** Newest first. */
    fun observeAll(): Flow<List<AppNotification>>
    fun observeUnreadCount(): Flow<Int>
    suspend fun add(notification: AppNotification)
    suspend fun markRead(id: Long)
    suspend fun markAllRead()
    suspend fun clearAll()
}
