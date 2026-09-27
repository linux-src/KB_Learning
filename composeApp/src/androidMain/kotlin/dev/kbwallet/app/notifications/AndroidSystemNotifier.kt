package dev.kbwallet.app.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import dev.kbwallet.app.notifications.domain.SystemNotifier
import java.util.concurrent.atomic.AtomicInteger

private const val CHANNEL_ID = "kb_wallet_trades"
private const val CHANNEL_NAME = "Trades & alerts"

/**
 * Posts to the Android status bar using framework APIs only — androidx.core is
 * not a declared dependency of androidMain, and leaning on it transitively
 * (via activity-compose / biometric) would break the moment either drops it.
 */
class AndroidSystemNotifier(private val context: Context) : SystemNotifier {

    private val manager: NotificationManager? =
        context.getSystemService(NotificationManager::class.java)

    // Distinct ids so a second purchase adds a notification instead of
    // replacing the first one.
    private val nextId = AtomicInteger(1)

    init {
        ensureChannel()
    }

    override fun isPermitted(): Boolean {
        val mgr = manager ?: return false
        // API 33+ additionally gates posting behind a runtime permission; below
        // that, the per-app notification switch is the only gate.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return mgr.areNotificationsEnabled()
    }

    /**
     * Opens the OS settings page for this app's notifications. The runtime
     * POST_NOTIFICATIONS dialog can only be raised from an Activity (see
     * MainActivity, which asks on launch); once it has been answered, settings
     * is the only remaining way back, so that's what this offers.
     */
    override fun requestPermission() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", context.packageName, null))
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    override fun notify(title: String, body: String) {
        val mgr = manager ?: return
        if (!isPermitted()) return

        @Suppress("DEPRECATION")
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            Notification.Builder(context)
        }

        val notification = builder
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setSmallIcon(context.applicationInfo.icon)
            .setAutoCancel(true)
            .build()

        // A trade must never fail because the status bar refused it.
        runCatching { mgr.notify(nextId.getAndIncrement(), notification) }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = manager ?: return
        runCatching {
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }
}
