package dev.kbwallet.app.notifications.domain

import dev.kbwallet.app.notifications.domain.model.NotificationType
import dev.kbwallet.app.profile.presentation.ProfileState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationPreferencesTest {

    @Test
    fun `each type is gated by its own switch`() {
        val preferences = NotificationPreferences(
            priceAlerts = true,
            tradeConfirmations = false,
            newsUpdates = true,
        )

        assertFalse(preferences.allows(NotificationType.TRADE_CONFIRMATION))
        assertTrue(preferences.allows(NotificationType.PRICE_ALERT))
        assertTrue(preferences.allows(NotificationType.NEWS))
    }

    @Test
    fun `SYSTEM is not user-configurable and always passes`() {
        val allOff = NotificationPreferences(
            pushNotifications = false,
            emailNotifications = false,
            priceAlerts = false,
            tradeConfirmations = false,
            newsUpdates = false,
        )

        assertTrue(allOff.allows(NotificationType.SYSTEM))
    }

    @Test
    fun `the profile state maps across field for field`() {
        val state = ProfileState(
            pushNotifications = false,
            emailNotifications = true,
            priceAlerts = false,
            tradeConfirmations = true,
            newsUpdates = true,
        )

        assertEquals(
            NotificationPreferences(
                pushNotifications = false,
                emailNotifications = true,
                priceAlerts = false,
                tradeConfirmations = true,
                newsUpdates = true,
            ),
            state.toNotificationPreferences(),
        )
    }
}
