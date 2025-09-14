package com.carecomms.di

import com.carecomms.security.EncryptionManager
import com.carecomms.security.IOSEncryptionManager
import com.carecomms.security.IOSSecureStorage
import com.carecomms.security.SecureStorage
import org.koin.dsl.module

val iosSecurityModule = module {
    single<EncryptionManager> { IOSEncryptionManager() }
    single<SecureStorage> { IOSSecureStorage() }
}