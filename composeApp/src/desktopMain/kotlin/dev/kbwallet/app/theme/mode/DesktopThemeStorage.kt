package dev.kbwallet.app.theme.mode

import java.util.prefs.Preferences

private const val KEY_THEME = "app_theme"

class DesktopThemeStorage : ThemeStorage {
    private val prefs = Preferences.userRoot().node("dev/kbwallet/app")

    override fun get(): ThemeMode? = ThemeMode.fromCode(prefs.get(KEY_THEME, null))

    override fun save(mode: ThemeMode) {
        prefs.put(KEY_THEME, mode.code)
    }
}
