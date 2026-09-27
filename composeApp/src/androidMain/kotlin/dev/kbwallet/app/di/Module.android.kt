package dev.kbwallet.app.di

import androidx.room.RoomDatabase
import dev.kbwallet.app.core.database.AppDatabase
import dev.kbwallet.app.core.database.getAppDatabaseBuilder
import dev.kbwallet.app.core.i18n.AndroidLanguageStorage
import dev.kbwallet.app.core.i18n.LanguageStorage
import dev.kbwallet.app.notifications.AndroidSystemNotifier
import dev.kbwallet.app.notifications.domain.SystemNotifier
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {

    // core
    single<HttpClientEngine> { Android.create() }
    singleOf(::getAppDatabaseBuilder).bind<RoomDatabase.Builder<AppDatabase>>()
    single<LanguageStorage> { AndroidLanguageStorage(androidContext()) }
    single<SystemNotifier> { AndroidSystemNotifier(androidContext()) }
}