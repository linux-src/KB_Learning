package dev.kbwallet.app.notifications.presentation

import androidx.compose.runtime.Stable
import dev.kbwallet.app.notifications.domain.model.NotificationType

@Stable
data class UiNotification(
    val id: Long,
    val type: NotificationType,
    val title: String,
    val body: String,
    val formattedTime: String,
    val isRead: Boolean,
)

@Stable
data class NotificationCenterState(
    val notifications: List<UiNotification> = emptyList(),
    val unreadCount: Int = 0,
    /** The user's own push switch, from notification settings. */
    val pushEnabled: Boolean = true,
    /** Whether the OS will actually deliver — independent of [pushEnabled]. */
    val systemPermitted: Boolean = true,
) {
    /**
     * The user asked for push but the OS is blocking it. Worth surfacing: the
     * switch alone looks like everything is fine while nothing is delivered.
     */
    val showSystemBlockedWarning: Boolean get() = pushEnabled && !systemPermitted
}
