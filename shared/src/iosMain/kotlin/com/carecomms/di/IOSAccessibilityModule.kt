package com.carecomms.di

import com.carecomms.accessibility.AccessibilityPreferences
import com.carecomms.accessibility.IOSAccessibilityPreferences
import org.koin.dsl.module

val iosAccessibilityModule = module {
    single<AccessibilityPreferences> { IOSAccessibilityPreferences() }
}