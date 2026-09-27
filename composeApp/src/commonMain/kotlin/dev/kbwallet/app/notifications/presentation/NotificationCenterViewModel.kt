package dev.kbwallet.app.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kbwallet.app.notifications.domain.NotificationController
import dev.kbwallet.app.notifications.domain.model.AppNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class NotificationCenterViewModel(
    private val controller: NotificationController,
) : ViewModel() {

    // The OS-level permission can change while the app sits in the background
    // (the user flips it in system settings), and nothing pushes that back to
    // us — so it's re-read on demand rather than assumed.
    private val systemPermitted = MutableStateFlow(controller.isSystemNotificationPermitted())

    val state: StateFlow<NotificationCenterState> = combine(
        controller.notifications,
        controller.unreadCount,
        controller.preferences,
        systemPermitted,
    ) { notifications, unread, preferences, permitted ->
        NotificationCenterState(
            notifications = notifications.map { it.toUi() },
            unreadCount = unread,
            pushEnabled = preferences.pushNotifications,
            systemPermitted = permitted,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotificationCenterState(),
    )

    fun refreshSystemPermission() {
        systemPermitted.value = controller.isSystemNotificationPermitted()
    }

    fun onSystemPermissionRequested() {
        controller.requestSystemPermission()
    }

    fun onNotificationClicked(id: Long) {
        viewModelScope.launch { controller.markRead(id) }
    }

    fun markAllRead() {
        viewModelScope.launch { controller.markAllRead() }
    }

    fun clearAll() {
        viewModelScope.launch { controller.clearAll() }
    }
}

private fun AppNotification.toUi(): UiNotification {
    val local = Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val day = local.dayOfMonth.toString().padStart(2, '0')
    val month = local.monthNumber.toString().padStart(2, '0')
    val hour = local.hour.toString().padStart(2, '0')
    val minute = local.minute.toString().padStart(2, '0')

    return UiNotification(
        id = id,
        type = type,
        title = title,
        body = body,
        formattedTime = "$day.$month $hour:$minute",
        isRead = isRead,
    )
}
