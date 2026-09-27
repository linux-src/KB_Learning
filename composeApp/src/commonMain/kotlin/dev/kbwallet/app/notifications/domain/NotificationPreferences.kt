package dev.kbwallet.app.notifications.domain

import dev.kbwallet.app.notifications.domain.model.NotificationType
import dev.kbwallet.app.profile.presentation.ProfileState

/**
 * The notification switches, as the notifications layer sees them.
 *
 * These are not stored here: the settings screen has always written them
 * through UserRepository/SecureTokenStorage, and that stays the single source
 * of truth — [toNotificationPreferences] is just the narrow view of it this
 * layer needs, so nothing has to reach into ProfileState directly.
 */
data class NotificationPreferences(
    val pushNotifications: Boolean = true,
    val emailNotifications: Boolean = false,
    val priceAlerts: Boolean = true,
    val tradeConfirmations: Boolean = true,
    val newsUpdates: Boolean = false,
) {
    /**
     * Whether a notification of [type] may be raised at all. SYSTEM is not
     * user-configurable; every other type follows its switch.
     */
    fun allows(type: NotificationType): Boolean = when (type) {
        NotificationType.TRADE_CONFIRMATION -> tradeConfirmations
        NotificationType.PRICE_ALERT -> priceAlerts
        NotificationType.NEWS -> newsUpdates
        NotificationType.SYSTEM -> true
    }
}

fun ProfileState.toNotificationPreferences() = NotificationPreferences(
    pushNotifications = pushNotifications,
    emailNotifications = emailNotifications,
    priceAlerts = priceAlerts,
    tradeConfirmations = tradeConfirmations,
    newsUpdates = newsUpdates,
)
