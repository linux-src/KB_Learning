package dev.kbwallet.app.notifications.domain.model

/**
 * Category a notification belongs to. Each maps onto one of the switches on the
 * notification settings screen, which is what decides whether a post is
 * delivered or dropped — see [dev.kbwallet.app.notifications.domain.NotificationPreferences].
 */
enum class NotificationType {
    TRADE_CONFIRMATION,
    PRICE_ALERT,
    NEWS,
    /** App-level messages that aren't user-configurable (kept always-on). */
    SYSTEM,
}
