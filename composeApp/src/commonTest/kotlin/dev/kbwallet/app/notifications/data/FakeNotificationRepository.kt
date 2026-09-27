package dev.kbwallet.app.notifications.data

import dev.kbwallet.app.notifications.domain.NotificationRepository
import dev.kbwallet.app.notifications.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Hand-written fake — see FakeCoinsRemoteDataSource for the project's testing convention. */
class FakeNotificationRepository : NotificationRepository {

    private val _items = MutableStateFlow<List<AppNotification>>(emptyList())

    /** Everything recorded so far, newest last — convenient for assertions. */
    val stored: List<AppNotification> get() = _items.value

    private var nextId = 1L

    override fun observeAll(): Flow<List<AppNotification>> =
        _items.map { list -> list.sortedByDescending { it.timestamp } }

    override fun observeUnreadCount(): Flow<Int> =
        _items.map { list -> list.count { !it.isRead } }

    override suspend fun add(notification: AppNotification) {
        _items.update { it + notification.copy(id = nextId++) }
    }

    override suspend fun markRead(id: Long) {
        _items.update { list -> list.map { if (it.id == id) it.copy(isRead = true) else it } }
    }

    override suspend fun markAllRead() {
        _items.update { list -> list.map { it.copy(isRead = true) } }
    }

    override suspend fun clearAll() {
        _items.value = emptyList()
    }
}
