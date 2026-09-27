package dev.kbwallet.app.notifications.domain

/**
 * Posts a notification to the operating system's own notification surface —
 * the Android status bar, iOS notification centre, or the desktop tray.
 *
 * Separate from the in-app centre on purpose: the in-app list is the app's own
 * record and always gets written, while this may silently do nothing when the
 * OS hasn't granted permission. Implementations must never throw — a platform
 * that can't deliver is a no-op, not a failed purchase.
 */
interface SystemNotifier {

    /** True when the OS currently permits posting. Cheap; safe to call often. */
    fun isPermitted(): Boolean

    /**
     * Ask the OS for permission if it hasn't been decided yet. No-op where the
     * platform needs no runtime grant.
     */
    fun requestPermission()

    fun notify(title: String, body: String)
}
