package dev.kbwallet.app.notifications.domain

import dev.kbwallet.app.core.i18n.AppLanguage
import dev.kbwallet.app.core.i18n.LanguageController
import dev.kbwallet.app.core.i18n.stringsFor
import dev.kbwallet.app.notifications.data.FakeNotificationRepository
import dev.kbwallet.app.notifications.data.FakeSystemNotifier
import dev.kbwallet.app.notifications.domain.model.AppNotification
import dev.kbwallet.app.notifications.domain.model.NotificationType
import dev.kbwallet.app.profile.data.FakeUserRepository
import dev.kbwallet.app.profile.presentation.ProfileState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The controller is the one place where the user's switches, the in-app
 * centre, and the OS notification meet — so these cover the combinations that
 * would otherwise drift apart silently (recorded but not pushed, pushed but
 * switched off, and so on).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationControllerTest {

    private lateinit var repository: FakeNotificationRepository
    private lateinit var userRepository: FakeUserRepository
    private lateinit var systemNotifier: FakeSystemNotifier
    private lateinit var controller: NotificationController

    private val english = stringsFor(AppLanguage.ENGLISH)

    @BeforeTest
    fun setup() {
        repository = FakeNotificationRepository()
        userRepository = FakeUserRepository()
        systemNotifier = FakeSystemNotifier()
        controller = NotificationController(
            repository = repository,
            userRepository = userRepository,
            systemNotifier = systemNotifier,
            languageController = LanguageController(FakeLanguageStorage()),
        )
    }

    @Test
    fun `a purchase is recorded and mirrored to the OS`() = runTest {
        val posted = controller.postPurchase("BTC", amountInUnit = 0.5, amountInFiat = 100.0)

        assertTrue(posted)
        assertEquals(1, repository.stored.size)
        assertEquals(NotificationType.TRADE_CONFIRMATION, repository.stored.single().type)
        assertEquals(english.notifPurchaseTitle, repository.stored.single().title)
        assertEquals(1, systemNotifier.posted.size)
    }

    @Test
    fun `trade confirmations switched off suppresses the notification entirely`() = runTest {
        userRepository.set(ProfileState(tradeConfirmations = false))

        val posted = controller.postPurchase("BTC", amountInUnit = 0.5, amountInFiat = 100.0)

        assertFalse(posted)
        assertTrue(repository.stored.isEmpty(), "a suppressed notification must not reach the centre")
        assertTrue(systemNotifier.posted.isEmpty())
    }

    @Test
    fun `push switched off still records in-app but never reaches the OS`() = runTest {
        userRepository.set(ProfileState(pushNotifications = false, tradeConfirmations = true))

        val posted = controller.postPurchase("BTC", amountInUnit = 0.5, amountInFiat = 100.0)

        assertTrue(posted)
        assertEquals(1, repository.stored.size, "the in-app centre is the app's own record")
        assertTrue(systemNotifier.posted.isEmpty(), "push is off — nothing should reach the status bar")
    }

    @Test
    fun `system notifications ignore the user switches`() = runTest {
        userRepository.set(
            ProfileState(
                tradeConfirmations = false,
                priceAlerts = false,
                newsUpdates = false,
            )
        )

        val posted = controller.post(NotificationType.SYSTEM, "Heads up", "Something happened")

        assertTrue(posted)
        assertEquals(1, repository.stored.size)
    }

    @Test
    fun `price alerts and news follow their own switches`() = runTest {
        userRepository.set(ProfileState(priceAlerts = true, newsUpdates = false))

        assertTrue(controller.post(NotificationType.PRICE_ALERT, "Alert", "BTC moved"))
        assertFalse(controller.post(NotificationType.NEWS, "News", "Something happened"))
        assertEquals(1, repository.stored.size)
    }

    @Test
    fun `each recorded notification raises an in-app banner`() = runTest {
        val received = collectBanners()

        controller.postPurchase("ETH", amountInUnit = 2.0, amountInFiat = 50.0)

        assertEquals(1, received.size)
        assertEquals(english.notifPurchaseTitle, received.single().title)
    }

    @Test
    fun `a suppressed notification raises no banner`() = runTest {
        userRepository.set(ProfileState(tradeConfirmations = false))
        val received = collectBanners()

        controller.postPurchase("ETH", amountInUnit = 2.0, amountInFiat = 50.0)

        assertTrue(received.isEmpty())
    }

    @Test
    fun `the body names what was bought and what it cost`() = runTest {
        controller.postPurchase("BTC", amountInUnit = 0.5, amountInFiat = 100.0)

        val body = repository.stored.single().body
        assertTrue(body.contains("BTC"), "expected the symbol in: $body")
        assertTrue(body.contains("100"), "expected the fiat total in: $body")
    }

    @Test
    fun `marking read clears the unread count`() = runTest {
        controller.post(NotificationType.SYSTEM, "One", "body")
        controller.post(NotificationType.SYSTEM, "Two", "body")

        controller.markAllRead()

        assertTrue(repository.stored.all { it.isRead })
    }

    @Test
    fun `preferences emit without anyone priming the profile state first`() = runTest {
        // Opening the notification centre without ever visiting Profile: the
        // repository's profileState is filterNotNull()'d over a null seed, so
        // the controller has to load it itself or every collector hangs.
        assertEquals(NotificationPreferences(), controller.preferences.first())
    }

    /**
     * Banners are a replay-less SharedFlow — a late subscriber misses the
     * emission entirely. Collecting on an unconfined dispatcher makes the
     * subscription land before the test posts, so this can't race.
     */
    private fun TestScope.collectBanners(): List<AppNotification> {
        val received = mutableListOf<AppNotification>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            controller.banners.toList(received)
        }
        return received
    }
}
