package com.carecomms.di

import com.carecomms.data.repository.AndroidNotificationRepository
import com.carecomms.data.repository.NotificationRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidNotificationModule = module {
    single<NotificationRepository> { 
        AndroidNotificationRepository(androidContext(), get()) 
    }
}