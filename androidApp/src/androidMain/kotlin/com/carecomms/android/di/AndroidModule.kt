package com.carecomms.android.di

import com.carecomms.data.database.DatabaseDriverFactory
import com.carecomms.data.repository.AndroidNetworkMonitor
import com.carecomms.data.repository.FirebaseAuthRepository
import com.carecomms.data.repository.FirebaseChatRepository
import com.carecomms.data.repository.NetworkMonitor
import com.carecomms.domain.repository.AuthRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidModule = module {
    // Database driver
    single { DatabaseDriverFactory(androidContext()) }
    
    // Network monitoring
    single<NetworkMonitor> { AndroidNetworkMonitor(androidContext()) }
    
    // Firebase repositories
    single<AuthRepository> { FirebaseAuthRepository(get()) }
    single { FirebaseChatRepository(get()) }
}