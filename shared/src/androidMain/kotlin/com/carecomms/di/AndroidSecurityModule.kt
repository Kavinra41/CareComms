package com.carecomms.di

import android.content.Context
import com.carecomms.security.AndroidEncryptionManager
import com.carecomms.security.AndroidSecureStorage
import com.carecomms.security.EncryptionManager
import com.carecomms.security.SecureStorage
import org.koin.dsl.module

val androidSecurityModule = module {
    single<EncryptionManager> { AndroidEncryptionManager() }
    single<SecureStorage> { AndroidSecureStorage(get<Context>()) }
}