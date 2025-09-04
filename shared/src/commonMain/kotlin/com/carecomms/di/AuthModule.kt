package com.carecomms.di

import com.carecomms.data.repository.AuthRepository
import org.koin.dsl.module

val authModule = module {
    // Platform-specific implementations will be provided in androidMain and iosMain
    // This is just the common module definition
}