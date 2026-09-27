package dev.kbwallet.app.notifications

import dev.kbwallet.app.notifications.domain.SystemNotifier
import platform.Foundation.NSUUID
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

/**
 * iOS notifications via UNUserNotificationCenter.
 *
 * The system's own authorization query is asynchronous, but [isPermitted] has
 * to answer synchronously — so the last known status is cached and refreshed
 * whenever we ask or are granted. It starts out `false`, which is the safe
 * direction: the in-app centre still records everything either way.
 */
class IosSystemNotifier : SystemNotifier {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    private var permitted: Boolean = false

    init {
        refreshPermission()
    }

    override fun isPermitted(): Boolean = permitted

    override fun requestPermission() {
        val options = UNAuthorizationOptionAlert or
            UNAuthorizationOptionSound or
            UNAuthorizationOptionBadge
        center.requestAuthorizationWithOptions(options) { granted, _ ->
            permitted = granted
        }
    }

    override fun notify(title: String, body: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
        }
        // A null trigger delivers immediately.
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = NSUUID().UUIDString,
            content = content,
            trigger = null,
        )
        center.addNotificationRequest(request, null)
        refreshPermission()
    }

    private fun refreshPermission() {
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val status = settings?.authorizationStatus
            permitted = status == UNAuthorizationStatusAuthorized ||
                status == UNAuthorizationStatusProvisional
        }
    }
}
