package com.carecomms.di

import com.carecomms.data.repository.AuthRepository
import com.carecomms.data.repository.IOSAuthRepository
import com.carecomms.data.storage.IOSSecureStorage
import com.carecomms.data.storage.SecureStorage
import org.koin.dsl.module

val iosAuthModule = module {
    
    // Secure Storage
    single<SecureStorage> { IOSSecureStorage() }
    
    // Auth Repository
    single<AuthRepository> { 
        IOSAuthRepository(
            secureStorage = get()
        )
    }
}