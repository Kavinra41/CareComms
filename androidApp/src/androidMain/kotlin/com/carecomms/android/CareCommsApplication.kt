package com.carecomms.android

import android.app.Application
import com.carecomms.android.di.androidModule
import com.carecomms.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CareCommsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@CareCommsApplication)
            modules(sharedModule, androidModule)
        }
    }
}