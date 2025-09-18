plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqlDelight)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            
            // Serialization
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            
            // UUID
            implementation("com.benasher44:uuid:0.8.2")
            
            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            
            // Koin
            implementation(libs.koin.core)
            
            // SQLDelight
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines.extensions)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
        }
        
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
            implementation(libs.sqldelight.android.driver)
            
            // Koin Android
            implementation(libs.koin.android)
            
            // Firebase - temporarily disabled
            // implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
            // implementation("com.google.firebase:firebase-auth")
            // implementation("com.google.firebase:firebase-database")
            // implementation("com.google.firebase:firebase-messaging")
            
            // Security
            implementation("androidx.security:security-crypto:1.1.0-alpha06")
        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
    }
}

android {
    namespace = "com.carecomms.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

sqldelight {
    databases {
        create("CareCommsDatabase") {
            packageName.set("com.carecomms.database")
        }
    }
}