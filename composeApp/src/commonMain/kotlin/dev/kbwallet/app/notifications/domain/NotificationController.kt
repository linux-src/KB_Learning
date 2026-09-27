package dev.kbwallet.app.notifications.domain

import dev.kbwallet.app.core.i18n.LanguageController
import dev.kbwallet.app.core.i18n.stringsFor
import dev.kbwallet.app.core.util.formatCoinUnit
import dev.kbwallet.app.core.util.formatFiat
import dev.kbwallet.app.notifications.domain.model.AppNotification
import dev.kbwallet.app.notifications.domain.model.NotificationType
import dev.kbwallet.app.profile.domain.UserRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

/**
 * The single entry point for raising a notification.
 *
 * Everything goes through here so the three things that have to stay in step —
 * the user's switches, the in-app centre, and the OS notification — can't drift
 * apart. Singleton (Koin), same shape as [LanguageController].
 */
class NotificationController(
    private val repository: NotificationRepository,
    private val userRepository: UserRepository,
    private val systemNotifier: SystemNotifier,
    private val languageController: LanguageController,
) {

    val notifications: Flow<List<AppNotification>> = repository.observeAll()
    val unreadCount: Flow<Int> = repository.observeUnreadCount()

    // UserRepository.profileState is filterNotNull()'d over a flow that stays
    // null until getProfileState() populates it — collecting it cold (opening
    // the notification centre without having visited Profile first) would
    // otherwise never emit, leaving anything combined with it stuck forever.
    val preferences: Flow<NotificationPreferences> = flow {
        userRepository.getProfileState()
        emitAll(userRepository.profileState.map { it.toNotificationPreferences() })
    }

    // In-app banners are transient: a subscriber that isn't listening at the
    // moment of the post has missed it, and replaying an old one later would
    // pop a banner for a purchase made minutes ago. extraBufferCapacity with
    // DROP_OLDEST keeps tryEmit non-suspending without ever blocking a trade.
    private val _banners = MutableSharedFlow<AppNotification>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val banners: Flow<AppNotification> = _banners.asSharedFlow()

    /** True when the OS would currently accept a push from us. */
    fun isSystemNotificationPermitted(): Boolean = systemNotifier.isPermitted()

    /** Sends the user wherever the platform lets them grant permission. */
    fun requestSystemPermission() = systemNotifier.requestPermission()

    /**
     * Record a notification and, when push is enabled, mirror it to the OS.
     *
     * @return false when the user's switches suppressed it entirely.
     */
    suspend fun post(
        type: NotificationType,
        title: String,
        body: String,
    ): Boolean {
        val preferences = userRepository.getProfileState().toNotificationPreferences()
        if (!preferences.allows(type)) return false

        val notification = AppNotification(
            type = type,
            title = title,
            body = body,
            timestamp = Clock.System.now().toEpochMilliseconds(),
        )
        repository.add(notification)
        _banners.tryEmit(notification)

        if (preferences.pushNotifications) {
            // SystemNotifier implementations swallow their own failures — a
            // rejected OS notification must never surface as a failed trade.
            systemNotifier.notify(title, body)
        }
        return true
    }

    /** "Purchase complete — Bought 0.0021 BTC for $100.00". */
    suspend fun postPurchase(
        coinSymbol: String,
        amountInUnit: Double,
        amountInFiat: Double,
    ): Boolean {
        val strings = stringsFor(languageController.language.value)
        return post(
            type = NotificationType.TRADE_CONFIRMATION,
            title = strings.notifPurchaseTitle,
            body = strings.notifPurchaseBody(
                amount = formatCoinUnit(amountInUnit, coinSymbol),
                total = formatFiat(amountInFiat),
            ),
        )
    }

    suspend fun markRead(id: Long) = repository.markRead(id)

    suspend fun markAllRead() = repository.markAllRead()

    suspend fun clearAll() = repository.clearAll()
}
