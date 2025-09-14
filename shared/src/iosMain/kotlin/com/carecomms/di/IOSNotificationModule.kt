package com.carecomms.di

import com.carecomms.data.repository.IOSNotificationRepository
import com.carecomms.data.repository.NotificationRepository
import org.koin.dsl.module

val iosNotificationModule = module {
    single<NotificationRepository> { 
        IOSNotificationRepository(get()) 
    }
}