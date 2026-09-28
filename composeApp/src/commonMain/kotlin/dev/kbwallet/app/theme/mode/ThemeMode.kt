package dev.kbwallet.app.theme.mode

enum class ThemeMode(val code: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromCode(code: String?): ThemeMode? = entries.firstOrNull { it.code == code }
    }
}

interface ThemeStorage {
    fun get(): ThemeMode?
    fun save(mode: ThemeMode)
}
