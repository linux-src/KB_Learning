package dev.kbwallet.app.theme.mode

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeController(private val storage: ThemeStorage) {

    // Dark stays the default look for a fresh install.
    private val _mode = MutableStateFlow(storage.get() ?: ThemeMode.DARK)
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun setMode(mode: ThemeMode) {
        _mode.value = mode
        storage.save(mode)
    }
}
