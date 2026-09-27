package dev.kbwallet.app.notifications.domain.model

/**
 * One entry in the in-app notification centre.
 *
 * [title] and [body] are stored already localized, in the language that was
 * active when the notification was posted — the same thing the OS notification
 * showed at that moment. Switching languages afterwards deliberately does not
 * rewrite history, so what the centre lists keeps matching what was actually
 * delivered.
 */
data class AppNotification(
    val id: Long = 0,
    val type: NotificationType,
    val title: String,
    val body: String,
    val timestamp: Long,
    val isRead: Boolean = false,
)
