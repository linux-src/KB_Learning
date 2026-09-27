package dev.kbwallet.app.notifications.di

import dev.kbwallet.app.core.database.AppDatabase
import dev.kbwallet.app.notifications.data.NotificationRepositoryImpl
import dev.kbwallet.app.notifications.domain.NotificationController
import dev.kbwallet.app.notifications.domain.NotificationRepository
import dev.kbwallet.app.notifications.presentation.NotificationCenterViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * SystemNotifier itself is bound per platform (see each platformModule) — this
 * module holds only what's shared.
 */
val notificationModule = module {
    single { get<AppDatabase>().notificationDao() }
    single { NotificationRepositoryImpl(get()) }.bind<NotificationRepository>()
    single { NotificationController(get(), get(), get(), get()) }
    viewModel { NotificationCenterViewModel(get()) }
}
