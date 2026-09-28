package dev.kbwallet.app.theme.mode

import platform.Foundation.NSUserDefaults

private const val KEY_THEME = "app_theme"

class IosThemeStorage : ThemeStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun get(): ThemeMode? = ThemeMode.fromCode(defaults.stringForKey(KEY_THEME))

    override fun save(mode: ThemeMode) {
        defaults.setObject(mode.code, forKey = KEY_THEME)
    }
}
