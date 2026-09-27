package dev.kbwallet.app.notifications

import dev.kbwallet.app.notifications.domain.SystemNotifier
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/**
 * Desktop notifications go through the AWT system tray, the only notification
 * surface available to a plain JVM app without a native dependency.
 *
 * Tray support is genuinely optional: headless JVMs and several Linux desktops
 * either report it unsupported or throw on add(). Every step is therefore
 * best-effort — a desktop without a tray simply gets the in-app centre.
 */
class DesktopSystemNotifier : SystemNotifier {

    private val trayIcon: TrayIcon? by lazy { installTrayIcon() }

    override fun isPermitted(): Boolean = trayIcon != null

    /** The desktop tray has no permission model. */
    override fun requestPermission() = Unit

    override fun notify(title: String, body: String) {
        val icon = trayIcon ?: return
        runCatching { icon.displayMessage(title, body, TrayIcon.MessageType.INFO) }
    }

    private fun installTrayIcon(): TrayIcon? = runCatching {
        if (!SystemTray.isSupported()) return@runCatching null
        val icon = TrayIcon(loadIconImage(), "KB Wallet").apply {
            isImageAutoSize = true
        }
        SystemTray.getSystemTray().add(icon)
        icon
    }.getOrNull()

    private fun loadIconImage() = runCatching {
        javaClass.getResourceAsStream("/icon.png")?.use { ImageIO.read(it) }
    }.getOrNull() ?: BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
}
