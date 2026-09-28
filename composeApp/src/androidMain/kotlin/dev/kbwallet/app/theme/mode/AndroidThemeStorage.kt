package dev.kbwallet.app.theme.mode

import android.content.Context

private const val PREFS_NAME = "kb_learning_prefs"
private const val KEY_THEME = "app_theme"

class AndroidThemeStorage(private val context: Context) : ThemeStorage {
    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun get(): ThemeMode? = ThemeMode.fromCode(prefs.getString(KEY_THEME, null))

    override fun save(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.code).apply()
    }
}
