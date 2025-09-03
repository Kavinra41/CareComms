# Design Document

## Overview

CareComms is a Kotlin Multiplatform mobile application that facilitates care coordination between professional carers and their care recipients. The application employs a role-based architecture with distinct user experiences optimized for accessibility and ease of use, particularly for elderly users who may be unfamiliar with mobile technology.

The design emphasizes clean architecture principles, real-time communication capabilities, and a minimal UI approach using a deep purple, light purple, and white color scheme.

## Architecture

### High-Level Architecture

The application follows a layered architecture pattern with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│           Presentation Layer            │
│  (Platform-specific UI - iOS/Android)   │
├─────────────────────────────────────────┤
│           Business Logic Layer          │
│     (Shared Kotlin Multiplatform)      │
├─────────────────────────────────────────┤
│             Data Layer                  │
│  (Repository Pattern + Data Sources)    │
├─────────────────────────────────────────┤
│          External Services              │
│   (Firebase, Push Notifications)       │
└─────────────────────────────────────────┘
```

### Technology Stack (Free Tier Only)

- **Shared Business Logic**: Kotlin Multiplatform Mobile (KMM) - Free
- **iOS UI**: SwiftUI with Kotlin/Native integration - Free
- **Android UI**: Jetpack Compose - Free
- **Authentication**: Firebase Authentication (Free tier: 10K verifications/month)
- **Real-time Communication**: Firebase Realtime Database (Free tier: 1GB storage, 10GB/month transfer)
- **Push Notifications**: Firebase Cloud Messaging (FCM) - Free
- **Navigation**: Platform-specific navigation solutions - Free
- **State Management**: MVI (Model-View-Intent) pattern - Free
- **Dependency Injection**: Koin (multiplatform) - Free, open source
- **Networking**: Ktor client (multiplatform) - Free, open source
- **Database**: SQLite with SQLDelight for local storage - Free
- **Image Loading**: Coil (Android) / AsyncImage (iOS) - Free

## Components and Interfaces

### Core Modules

#### 1. Authentication Module
```kotlin
interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signUpCarer(carerData: CarerRegistrationData): Result<User>
    suspend fun signUpCaree(careeData: CareeRegistrationData, invitationToken: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun validateInvitationToken(token: String): Result<CarerInfo>
}

data class CarerRegistrationData(
    val email: String,
    val password: String,
    val documents: List<String>,
    val age: Int,
    val phoneNumber: String,
    val location: String
)

data class CareeRegistrationData(
    val email: String,
    val password: String,
    val healthInfo: String,
    val basicDetails: PersonalDetails
)
```

#### 2. Chat Module
```kotlin
interface ChatRepository {
    suspend fun getChatList(carerId: String): Flow<List<ChatPreview>>
    suspend fun getMessages(chatId: String): Flow<List<Message>>
    suspend fun sendMessage(chatId: String, message: Message): Result<Unit>
    suspend fun markAsRead(chatId: String, messageId: String): Result<Unit>
    suspend fun getTypingStatus(chatId: String): Flow<TypingStatus>
    suspend fun setTypingStatus(chatId: String, isTyping: Boolean): Result<Unit>
}

data class Message(
    val id: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val status: MessageStatus,
    val type: MessageType = MessageType.TEXT
)

enum class MessageStatus { SENT, DELIVERED, READ }
```

#### 3. Invitation Module
```kotlin
interface InvitationRepository {
    suspend fun generateInvitationLink(carerId: String): Result<String>
    suspend fun validateInvitation(token: String): Result<InvitationData>
    suspend fun acceptInvitation(token: String, careeId: String): Result<Unit>
}

data class InvitationData(
    val carerId: String,
    val carerName: String,
    val expirationTime: Long
)
```

#### 4. Analytics Module
```kotlin
interface AnalyticsRepository {
    suspend fun getCareeAnalytics(careeId: String, period: AnalyticsPeriod): Result<AnalyticsData>
    suspend fun getMultiCareeAnalytics(careeIds: List<String>, period: AnalyticsPeriod): Result<AnalyticsData>
}

data class AnalyticsData(
    val dailyData: List<DailyMetric>,
    val weeklyData: List<WeeklyMetric>,
    val biweeklyData: List<BiweeklyMetric>,
    val notes: List<AnalyticsNote>
)
```

### User Interface Components

#### 1. Navigation Structure

**Carer Navigation Flow:**
```
Splash → Terms → Landing → Login/Signup → Chat List
                                            ├── Chat Page
                                            ├── Profile
                                            ├── Data Dashboard
                                            └── Details Tree
```

**Caree Navigation Flow:**
```
Splash → Terms → Landing → Login → Chat Page
                        ↑
                   Invitation Link → Hidden Signup
```

#### 2. Screen Components

**Splash Screen:**
- Logo display with 5-second maximum duration
- Smooth transition to terms page
- Loading indicator if needed

**Terms and Conditions:**
- Scrollable content with accept/decline buttons
- Clear typography optimized for elderly users
- Progress to landing screen on acceptance

**Landing Screen:**
- Login and signup options for carers only
- Clean, minimal design with large touch targets
- Deep purple branding elements

**Chat List Screen (Carer Only):**
- List of caree conversations with previews
- Search functionality with real-time filtering
- Invite button with prominent placement
- Bottom navigation bar
- Pull-to-refresh capability

**Chat Screen (Both Roles):**
- Real-time messaging interface
- Typing indicators
- Message status indicators
- Large, readable text
- Smooth scrolling with proper keyboard handling

## Data Models

### User Models
```kotlin
sealed class User {
    abstract val id: String
    abstract val email: String
    abstract val createdAt: Long
}

data class Carer(
    override val id: String,
    override val email: String,
    override val createdAt: Long,
    val documents: List<String>,
    val age: Int,
    val phoneNumber: String,
    val location: String,
    val careeIds: List<String>
) : User()

data class Caree(
    override val id: String,
    override val email: String,
    override val createdAt: Long,
    val healthInfo: String,
    val personalDetails: PersonalDetails,
    val carerId: String
) : User()
```

### Chat Models
```kotlin
data class ChatPreview(
    val chatId: String,
    val careeName: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int,
    val isOnline: Boolean
)

data class Chat(
    val id: String,
    val carerId: String,
    val careeId: String,
    val createdAt: Long,
    val lastActivity: Long
)
```

### Analytics Models
```kotlin
data class DailyMetric(
    val date: String,
    val activityLevel: Int,
    val communicationCount: Int,
    val notes: String
)

data class DetailsTreeNode(
    val id: String,
    val title: String,
    val type: NodeType,
    val children: List<DetailsTreeNode>,
    val data: Any?
)

enum class NodeType { CAREE, CATEGORY, DETAIL, ITEM }
```

## Error Handling

### Error Types
```kotlin
sealed class AppError : Exception() {
    object NetworkError : AppError()
    object AuthenticationError : AppError()
    object ValidationError : AppError()
    data class ServerError(val code: Int, val message: String) : AppError()
    data class UnknownError(val throwable: Throwable) : AppError()
}
```

### Error Handling Strategy
- Global error handler for unhandled exceptions
- User-friendly error messages with clear actions
- Retry mechanisms for network operations
- Offline capability with local data caching
- Graceful degradation for non-critical features

## Testing Strategy

### Unit Testing
- Business logic testing in shared module
- Repository pattern testing with mock data sources
- Use cases and view models testing
- Data model validation testing

### Integration Testing
- Firebase integration testing
- Real-time communication testing
- Authentication flow testing
- Cross-platform compatibility testing

### UI Testing
- Platform-specific UI testing (Espresso for Android, XCUITest for iOS)
- Accessibility testing for elderly users
- Navigation flow testing
- Error state testing

### Performance Testing
- Memory usage optimization
- Battery consumption monitoring
- Network efficiency testing
- Startup time optimization

## Security Considerations

### Authentication Security
- Firebase Authentication with email verification
- Secure token storage using platform keychain/keystore
- Session management with automatic refresh
- Invitation token expiration and validation

### Data Security
- End-to-end encryption for sensitive health data
- HIPAA compliance considerations for health information
- Secure data transmission using HTTPS/TLS
- Local data encryption for cached information

### Privacy Protection
- Minimal data collection principle
- Clear privacy policy and consent management
- Data retention policies
- User data deletion capabilities

## Accessibility Features

### Visual Accessibility
- Large text support with dynamic type scaling
- High contrast color scheme options
- Clear visual hierarchy with proper spacing
- Consistent iconography with text labels

### Motor Accessibility
- Large touch targets (minimum 44pt/dp)
- Gesture alternatives for complex interactions
- Voice input support where applicable
- Simplified navigation patterns

### Cognitive Accessibility
- Simple, consistent interface patterns
- Clear error messages and guidance
- Minimal cognitive load design
- Progressive disclosure of complex features

## Performance Optimization

### App Performance
- Lazy loading for chat history
- Image optimization and caching
- Background task optimization
- Memory management for long-running sessions

### Network Optimization
- Efficient data synchronization
- Offline-first architecture where possible
- Compression for large data transfers
- Smart retry policies for failed requests

### Battery Optimization
- Efficient push notification handling
- Background processing optimization
- Location services optimization
- Screen wake lock management