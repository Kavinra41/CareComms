package com.carecomms.di

import com.carecomms.data.repository.AuthRepository
import com.carecomms.data.repository.FirebaseAuthRepository
import com.carecomms.data.storage.AndroidSecureStorage
import com.carecomms.data.storage.SecureStorage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidAuthModule = module {
    
    // Firebase Auth
    single { FirebaseAuth.getInstance() }
    
    // Secure Storage
    single<SecureStorage> { AndroidSecureStorage(androidContext()) }
    
    // JSON serializer
    single { Json { ignoreUnknownKeys = true } }
    
    // Auth Repository
    single<AuthRepository> { 
        FirebaseAuthRepository(
            firebaseAuth = get(),
            secureStorage = get(),
            json = get()
        )
    }
}