package com.carecomms.di

import com.carecomms.accessibility.AccessibilityPreferences
import com.carecomms.accessibility.AndroidAccessibilityPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidAccessibilityModule = module {
    single<AccessibilityPreferences> { AndroidAccessibilityPreferences(androidContext()) }
}