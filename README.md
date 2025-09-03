# CareComms Mobile App

A Kotlin Multiplatform mobile application for care coordination between professional carers and care recipients.

## Project Structure

```
CareComms/
├── androidApp/                 # Android-specific code
│   ├── src/androidMain/        # Android UI with Jetpack Compose
│   ├── build.gradle.kts        # Android app configuration
│   └── google-services.json    # Firebase configuration (placeholder)
├── iosApp/                     # iOS-specific code (to be implemented)
│   └── Configuration/
│       └── GoogleService-Info.plist  # Firebase configuration (placeholder)
├── shared/                     # Shared Kotlin Multiplatform code
│   ├── src/commonMain/         # Common business logic
│   │   ├── kotlin/com/carecomms/
│   │   │   ├── data/           # Data models and repositories
│   │   │   ├── domain/         # Business logic and use cases
│   │   │   ├── presentation/   # UI state management
│   │   │   └── di/             # Dependency injection
│   │   └── sqldelight/         # Database schema
│   ├── src/androidMain/        # Android-specific implementations
│   ├── src/iosMain/            # iOS-specific implementations
│   └── build.gradle.kts        # Shared module configuration
├── gradle/
│   └── libs.versions.toml      # Dependency versions
├── build.gradle.kts            # Root project configuration
└── settings.gradle.kts         # Project settings
```

## Technology Stack

- **Kotlin Multiplatform Mobile (KMM)** - Shared business logic
- **Jetpack Compose** - Android UI
- **SwiftUI** - iOS UI (to be implemented)
- **Firebase Authentication** - User authentication
- **Firebase Realtime Database** - Real-time chat
- **Firebase Cloud Messaging** - Push notifications
- **SQLDelight** - Local database
- **Ktor** - HTTP client
- **Koin** - Dependency injection
- **Kotlinx Serialization** - JSON serialization

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Xcode 13+ (for iOS development)
- JDK 11 or later

### Firebase Configuration
1. Create a new Firebase project at https://console.firebase.google.com
2. Add Android app with package name `com.carecomms.android`
3. Add iOS app with bundle ID `com.carecomms.ios`
4. Replace placeholder `google-services.json` and `GoogleService-Info.plist` files with your actual Firebase configuration files
5. Enable Authentication, Realtime Database, and Cloud Messaging in Firebase console

### Building the Project
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Replace Firebase configuration files with your actual ones
5. Build and run on Android or iOS

## Architecture

The app follows Clean Architecture principles with three main layers:

1. **Data Layer** - Models, repositories, and data sources
2. **Domain Layer** - Business logic and use cases
3. **Presentation Layer** - UI state management and platform-specific UI

## Features

- Role-based authentication (Carer/Caree)
- Invitation-based caree registration
- Real-time chat communication
- Data analytics dashboard
- Details tree navigation
- Cross-platform compatibility

## Development Status

✅ Project structure and core configuration
⏳ Authentication implementation (next task)
⏳ Chat functionality
⏳ UI implementation
⏳ Analytics and dashboard
⏳ Testing and deployment

## License

[Add your license here]