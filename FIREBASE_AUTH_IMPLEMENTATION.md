# Firebase Authentication Module Implementation

## Overview

This document describes the implementation of the Firebase Authentication module for the CareComms mobile application. The module provides secure authentication for both carers and carees with role-based access control and invitation-based caree registration.

## Architecture

The authentication module follows a clean architecture pattern with the following components:

### Core Interfaces

1. **AuthRepository** - Main authentication interface
2. **SecureStorage** - Platform-specific secure token storage

### Platform Implementations

1. **Android**: Firebase Authentication with encrypted SharedPreferences
2. **iOS**: Mock implementation with Keychain storage (ready for Firebase iOS SDK integration)

## Features Implemented

### ✅ Authentication Methods

- **Email/Password Sign In**: Standard authentication for both carers and carees
- **Carer Registration**: Professional carer signup with document validation
- **Caree Registration**: Invitation-based signup for care recipients
- **Sign Out**: Secure logout with token cleanup
- **Account Deletion**: Complete account removal

### ✅ Token Management

- **Secure Token Storage**: Platform-specific encrypted storage
- **Token Refresh**: Automatic token renewal
- **Session Management**: Persistent login state

### ✅ Invitation System

- **Token Generation**: Unique invitation tokens for carers
- **Token Validation**: Secure invitation verification
- **Carer-Caree Linking**: Automatic relationship establishment

### ✅ Error Handling

Comprehensive error types:
- `InvalidCredentials`
- `UserNotFound`
- `EmailAlreadyInUse`
- `WeakPassword`
- `InvalidEmail`
- `InvalidInvitationToken`
- `InvitationExpired`
- `NetworkError`
- `UnknownError`

## File Structure

```
shared/src/
├── commonMain/kotlin/com/carecomms/
│   ├── data/
│   │   ├── models/
│   │   │   ├── AuthResult.kt          # Authentication result models
│   │   │   └── User.kt                # User models (existing)
│   │   ├── repository/
│   │   │   └── AuthRepository.kt      # Main auth interface
│   │   └── storage/
│   │       └── SecureStorage.kt       # Secure storage interface
│   └── di/
│       └── AuthModule.kt              # Common DI module
├── androidMain/kotlin/com/carecomms/
│   ├── data/
│   │   ├── repository/
│   │   │   └── FirebaseAuthRepository.kt  # Firebase implementation
│   │   └── storage/
│   │       └── AndroidSecureStorage.kt    # Android encrypted storage
│   └── di/
│       └── AndroidAuthModule.kt           # Android DI module
├── iosMain/kotlin/com/carecomms/
│   ├── data/
│   │   ├── repository/
│   │   │   └── IOSAuthRepository.kt       # iOS mock implementation
│   │   └── storage/
│   │       └── IOSSecureStorage.kt        # iOS Keychain storage
│   └── di/
│       └── IOSAuthModule.kt               # iOS DI module
└── commonTest/kotlin/com/carecomms/
    ├── data/
    │   ├── repository/
    │   │   ├── AuthRepositoryTest.kt          # Auth repository tests
    │   │   └── AuthModuleVerificationTest.kt  # Module verification
    │   └── storage/
    │       └── SecureStorageTest.kt           # Storage tests
```

## Dependencies Added

### Shared Module (build.gradle.kts)

```kotlin
androidMain.dependencies {
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    
    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
```

### Version Catalog (libs.versions.toml)

```toml
[versions]
firebase-bom = "32.7.0"

[libraries]
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebase-bom" }
firebase-auth = { module = "com.google.firebase:firebase-auth" }
```

## Usage Examples

### Basic Authentication

```kotlin
class AuthViewModel(private val authRepository: AuthRepository) {
    
    suspend fun signIn(email: String, password: String) {
        val result = authRepository.signInWithEmail(email, password)
        result.fold(
            onSuccess = { authResult ->
                // Handle successful login
                val user = authResult.user
                val token = authResult.token
            },
            onFailure = { error ->
                // Handle authentication error
                when (error) {
                    is AuthError.InvalidCredentials -> showError("Invalid credentials")
                    is AuthError.NetworkError -> showError("Network error")
                    else -> showError("Authentication failed")
                }
            }
        )
    }
}
```

### Carer Registration

```kotlin
suspend fun registerCarer(
    email: String,
    password: String,
    documents: List<String>,
    age: Int,
    phoneNumber: String,
    location: String
) {
    val carerData = CarerRegistrationData(
        email = email,
        password = password,
        documents = documents,
        age = age,
        phoneNumber = phoneNumber,
        location = location
    )
    
    val result = authRepository.signUpCarer(carerData)
    // Handle result...
}
```

### Invitation-Based Caree Registration

```kotlin
suspend fun registerCaree(
    email: String,
    password: String,
    healthInfo: String,
    personalDetails: PersonalDetails,
    invitationToken: String
) {
    val careeData = CareeRegistrationData(
        email = email,
        password = password,
        healthInfo = healthInfo,
        basicDetails = personalDetails
    )
    
    val result = authRepository.signUpCaree(careeData, invitationToken)
    // Handle result...
}
```

## Security Features

### Android Security

- **Encrypted SharedPreferences**: Uses Android Security Crypto library
- **Master Key**: AES256_GCM encryption scheme
- **Key Protection**: Hardware-backed keystore when available

### iOS Security

- **Keychain Services**: Native iOS secure storage
- **Access Control**: `kSecAttrAccessibleWhenUnlockedThisDeviceOnly`
- **Service Isolation**: App-specific keychain entries

### Token Security

- **Secure Storage**: All tokens stored in platform-specific secure storage
- **Automatic Cleanup**: Tokens cleared on logout and account deletion
- **Refresh Mechanism**: Automatic token renewal to maintain security

## Testing

### Test Coverage

- ✅ **Unit Tests**: Comprehensive repository and storage testing
- ✅ **Error Handling**: All error scenarios covered
- ✅ **Mock Implementations**: Full mock auth repository for testing
- ✅ **Model Validation**: Data model serialization and validation tests

### Running Tests

```bash
# Run all tests
gradle shared:testDebugUnitTest

# Run specific test class
gradle shared:testDebugUnitTest --tests "AuthRepositoryTest"
```

## Integration with App

### Koin Dependency Injection

```kotlin
// In your Application class
startKoin {
    androidContext(this@Application)
    modules(
        authModule,
        androidAuthModule,
        // other modules...
    )
}
```

### Usage in ViewModels

```kotlin
class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, password)
            // Handle result...
        }
    }
}
```

## Next Steps

1. **Firebase Project Setup**: Configure Firebase project and add configuration files
2. **iOS Firebase Integration**: Replace mock implementation with Firebase iOS SDK
3. **Backend Integration**: Implement invitation token validation with backend service
4. **User Profile Storage**: Add Firestore integration for user profile data
5. **Push Notifications**: Integrate FCM for authentication-related notifications

## Requirements Satisfied

This implementation satisfies the following requirements:

- **Requirement 1.7**: Firebase email authentication ✅
- **Requirement 2.4**: Caree signup through invitation ✅
- **Requirement 2.5**: Invitation token validation ✅

## Notes

- The iOS implementation is currently a mock for development purposes
- Real Firebase iOS SDK integration requires platform-specific code
- Invitation token validation is simplified and should be enhanced with backend integration
- All sensitive data is properly encrypted and stored securely