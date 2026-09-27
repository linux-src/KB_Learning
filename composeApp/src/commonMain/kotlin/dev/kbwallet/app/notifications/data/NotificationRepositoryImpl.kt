package dev.kbwallet.app.notifications.data

import dev.kbwallet.app.notifications.domain.NotificationRepository
import dev.kbwallet.app.notifications.domain.model.AppNotification
import dev.kbwallet.app.notifications.domain.model.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepositoryImpl(
    private val dao: NotificationDao,
) : NotificationRepository {

    override fun observeAll(): Flow<List<AppNotification>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeUnreadCount(): Flow<Int> = dao.observeUnreadCount()

    override suspend fun add(notification: AppNotification) {
        dao.insert(notification.toEntity())
    }

    override suspend fun markRead(id: Long) = dao.markRead(id)

    override suspend fun markAllRead() = dao.markAllRead()

    override suspend fun clearAll() = dao.clearAll()
}

private fun NotificationEntity.toDomain() = AppNotification(
    id = id,
    // An unknown/renamed type must not take the whole list down with an
    // exception — fall back to the always-visible SYSTEM bucket.
    type = NotificationType.entries.firstOrNull { it.name == type } ?: NotificationType.SYSTEM,
    title = title,
    body = body,
    timestamp = timestamp,
    isRead = isRead,
)

private fun AppNotification.toEntity() = NotificationEntity(
    id = id,
    type = type.name,
    title = title,
    body = body,
    timestamp = timestamp,
    isRead = isRead,
)
