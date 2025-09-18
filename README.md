# CareComms - Kotlin Multiplatform Mobile App

A cross-platform mobile application for care communication built with Kotlin Multiplatform Mobile (KMM).

## 🚀 Quick Start

### Prerequisites
- Android Studio Arctic Fox or later
- Xcode 13+ (for iOS development)
- JDK 11 or later
- Kotlin 1.9+

### Build & Run

**Android:**
```bash
./gradlew androidApp:assembleDebug
./gradlew androidApp:installDebug
```

**iOS:**
```bash
cd iosApp
xcodebuild -scheme iosApp -configuration Debug
```

## 📱 Features

- **Cross-Platform**: Shared business logic between Android and iOS
- **Authentication**: User login and registration flows
- **Real-time Chat**: Messaging system for care communication
- **Dashboard**: Analytics and care overview
- **Profile Management**: User settings and preferences
- **Material Design**: Modern UI following platform guidelines

## 🏗️ Architecture

### Project Structure
```
├── androidApp/          # Android-specific code
├── iosApp/             # iOS-specific code
├── shared/             # Shared Kotlin code
│   ├── commonMain/     # Platform-agnostic code
│   ├── androidMain/    # Android-specific shared code
│   └── iosMain/        # iOS-specific shared code
└── gradle/             # Build configuration
```

### Key Components
- **UI Layer**: Jetpack Compose (Android) + SwiftUI (iOS)
- **Business Logic**: Shared Kotlin Multiplatform code
- **Dependency Injection**: Koin
- **Database**: SQLDelight
- **Networking**: Ktor (when Firebase is re-enabled)

## 🔧 Current Status

- ✅ **Android App**: Fully functional with complete UI
- ✅ **Shared Module**: Business logic implemented
- ✅ **iOS App**: UI implemented and ready
- ⏳ **Firebase Integration**: Temporarily disabled (can be re-enabled)

## 🛠️ Development

### Essential Files Only
This repository contains only the essential files needed for building and running the app:
- Source code files (.kt, .swift)
- Build configuration (gradle files, Xcode project)
- App manifests and configuration
- Core assets and resources

### Excluded Files
The following are excluded via .gitignore to keep the repository clean:
- Build outputs and temporary files
- IDE-specific files
- Test files (can be regenerated)
- Documentation files (can be regenerated)
- Performance/Security/Accessibility implementations (can be added back)

## 📦 Dependencies

### Shared
- Kotlin Multiplatform
- Koin (Dependency Injection)
- SQLDelight (Database)
- Kotlinx Serialization

### Android
- Jetpack Compose
- Material Design Components
- Navigation Component

### iOS
- SwiftUI
- Combine Framework

## 🚀 Next Steps

1. **Re-enable Firebase**: Add back authentication and real-time features
2. **Add Tests**: Implement unit and integration tests
3. **Performance Optimization**: Add monitoring and optimization
4. **Security**: Implement encryption and secure storage
5. **Accessibility**: Add accessibility features
6. **App Store Deployment**: Prepare for production release

## 📄 License

This project is licensed under the MIT License.