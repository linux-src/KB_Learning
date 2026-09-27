package dev.kbwallet.app.notifications.domain

import dev.kbwallet.app.core.i18n.AppLanguage
import dev.kbwallet.app.core.i18n.LanguageStorage

/**
 * Pins the language so tests assert against a known catalog rather than
 * whatever locale the machine running them happens to have.
 */
class FakeLanguageStorage(
    private var language: AppLanguage? = AppLanguage.ENGLISH,
) : LanguageStorage {
    override fun get(): AppLanguage? = language
    override fun save(language: AppLanguage) {
        this.language = language
    }
}
