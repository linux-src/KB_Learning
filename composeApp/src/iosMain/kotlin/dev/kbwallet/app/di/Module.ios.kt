package dev.kbwallet.app.di

import androidx.room.RoomDatabase
import dev.kbwallet.app.core.database.AppDatabase
import dev.kbwallet.app.core.database.getAppDatabaseBuilder
import dev.kbwallet.app.core.i18n.IosLanguageStorage
import dev.kbwallet.app.core.i18n.LanguageStorage
import dev.kbwallet.app.notifications.IosSystemNotifier
import dev.kbwallet.app.notifications.domain.SystemNotifier
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {
    single<HttpClientEngine> { Darwin.create() }
    singleOf(::getAppDatabaseBuilder).bind<RoomDatabase.Builder<AppDatabase>>()
    single<LanguageStorage> { IosLanguageStorage() }
    single<SystemNotifier> { IosSystemNotifier() }
}