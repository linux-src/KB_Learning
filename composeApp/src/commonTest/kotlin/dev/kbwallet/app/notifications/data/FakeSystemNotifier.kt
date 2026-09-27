package dev.kbwallet.app.notifications.data

import dev.kbwallet.app.notifications.domain.SystemNotifier

/** Hand-written fake — records what would have reached the OS. */
class FakeSystemNotifier(
    var permitted: Boolean = true,
) : SystemNotifier {

    val posted = mutableListOf<Pair<String, String>>()
    var permissionRequests = 0
        private set

    override fun isPermitted(): Boolean = permitted

    override fun requestPermission() {
        permissionRequests++
    }

    override fun notify(title: String, body: String) {
        posted += title to body
    }
}
