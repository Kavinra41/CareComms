package com.carecomms.domain.usecase

import com.carecomms.data.models.NotificationPreferences
import com.carecomms.data.models.NotificationType
import com.carecomms.data.models.User
import com.carecomms.data.repository.LocalUserRepository
import com.carecomms.data.repository.NotificationRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class NotificationUseCaseTest {
    
    private lateinit var mockNotificationRepository: MockNotificationRepository
    private lateinit var mockLocalUserRepository: MockLocalUserRepository
    private lateinit var notificationUseCase: NotificationUseCase
    
    @BeforeTest
    fun setup() {
        mockNotificationRepository = MockNotificationRepository()
        mockLocalUserRepository = MockLocalUserRepository()
        notificationUseCase = NotificationUseCase(
            mockNotificationRepository,
            mockLocalUserRepository
        )
    }
    
    @Test
    fun `initializeNotifications should initialize repository and subscribe to topics`() = runTest {
        // Given
        val userId = "test-user-id"
        mockLocalUserRepository.setCurrentUser(createTestUser(userId))
        
        // When
        val result = notificationUseCase.initializeNotifications()
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(mockNotificationRepository.initializeCalled)
        assertTrue(mockNotificationRepository.subscribedTopics.contains("user_$userId"))
        assertTrue(mockNotificationRepository.subscribedTopics.contains("system_updates"))
    }
    
    @Test
    fun `requestNotificationPermission should delegate to repository`() = runTest {
        // Given
        mockNotificationRepository.permissionGranted = true
        
        // When
        val result = notificationUseCase.requestNotificationPermission()
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
        assertTrue(mockNotificationRepository.requestPermissionCalled)
    }
    
    @Test
    fun `areNotificationsEnabled should return repository status`() = runTest {
        // Given
        mockNotificationRepository.notificationsEnabled = true
        
        // When
        val result = notificationUseCase.areNotificationsEnabled()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `updateNotificationPreferences should delegate to repository`() = runTest {
        // Given
        val preferences = NotificationPreferences(
            messageNotifications = false,
            soundEnabled = false
        )
        
        // When
        val result = notificationUseCase.updateNotificationPreferences(preferences)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(preferences, mockNotificationRepository.savedPreferences)
    }
    
    @Test
    fun `handleNewMessageNotification should show notification with correct data`() = runTest {
        // Given
        val chatId = "chat-123"
        val senderId = "sender-456"
        val senderName = "John Doe"
        val messageContent = "Hello there!"
        
        // When
        val result = notificationUseCase.handleNewMessageNotification(
            chatId, senderId, senderName, messageContent
        )
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("New message from $senderName", mockNotificationRepository.lastNotificationTitle)
        assertEquals(messageContent, mockNotificationRepository.lastNotificationBody)
        assertTrue(mockNotificationRepository.lastNotificationData.isNotEmpty())
    }
    
    @Test
    fun `handleInvitationNotification should show invitation notification`() = runTest {
        // Given
        val invitationToken = "token-123"
        val carerName = "Dr. Smith"
        
        // When
        val result = notificationUseCase.handleInvitationNotification(invitationToken, carerName)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("Care Invitation", mockNotificationRepository.lastNotificationTitle)
        assertTrue(mockNotificationRepository.lastNotificationBody.contains(carerName))
    }
    
    @Test
    fun `handleSystemNotification should show system notification`() = runTest {
        // Given
        val title = "System Update"
        val message = "App has been updated"
        
        // When
        val result = notificationUseCase.handleSystemNotification(title, message)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(title, mockNotificationRepository.lastNotificationTitle)
        assertEquals(message, mockNotificationRepository.lastNotificationBody)
    }
    
    @Test
    fun `subscribeToUserTopics should subscribe to correct topics`() = runTest {
        // Given
        val userId = "user-789"
        
        // When
        val result = notificationUseCase.subscribeToUserTopics(userId)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(mockNotificationRepository.subscribedTopics.contains("user_$userId"))
        assertTrue(mockNotificationRepository.subscribedTopics.contains("system_updates"))
    }
    
    @Test
    fun `unsubscribeFromUserTopics should unsubscribe from correct topics`() = runTest {
        // Given
        val userId = "user-789"
        mockNotificationRepository.subscribedTopics.addAll(listOf("user_$userId", "system_updates"))
        
        // When
        val result = notificationUseCase.unsubscribeFromUserTopics(userId)
        
        // Then
        assertTrue(result.isSuccess)
        assertFalse(mockNotificationRepository.subscribedTopics.contains("user_$userId"))
        assertFalse(mockNotificationRepository.subscribedTopics.contains("system_updates"))
    }
    
    @Test
    fun `clearAllNotifications should delegate to repository`() = runTest {
        // When
        val result = notificationUseCase.clearAllNotifications()
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(mockNotificationRepository.clearAllCalled)
    }
    
    @Test
    fun `getFCMToken should return token from repository`() = runTest {
        // Given
        val expectedToken = "fcm-token-123"
        mockNotificationRepository.fcmToken = expectedToken
        
        // When
        val result = notificationUseCase.getFCMToken()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedToken, result.getOrNull())
    }
    
    private fun createTestUser(id: String): User.Carer {
        return User.Carer(
            id = id,
            email = "test@example.com",
            createdAt = System.currentTimeMillis(),
            documents = emptyList(),
            age = 30,
            phoneNumber = "1234567890",
            location = "Test City",
            careeIds = emptyList()
        )
    }
}

// Mock implementations
private class MockNotificationRepository : NotificationRepository {
    var initializeCalled = false
    var requestPermissionCalled = false
    var notificationsEnabled = false
    var permissionGranted = false
    var savedPreferences: NotificationPreferences? = null
    var lastNotificationTitle = ""
    var lastNotificationBody = ""
    var lastNotificationData = emptyMap<String, String>()
    var clearAllCalled = false
    var fcmToken = "mock-token"
    val subscribedTopics = mutableSetOf<String>()
    
    override suspend fun initialize(): Result<Unit> {
        initializeCalled = true
        return Result.success(Unit)
    }
    
    override suspend fun getToken(): Result<String> {
        return Result.success(fcmToken)
    }
    
    override suspend fun subscribeToTopic(topic: String): Result<Unit> {
        subscribedTopics.add(topic)
        return Result.success(Unit)
    }
    
    override suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        subscribedTopics.remove(topic)
        return Result.success(Unit)
    }
    
    override suspend fun areNotificationsEnabled(): Boolean {
        return notificationsEnabled
    }
    
    override suspend fun requestNotificationPermission(): Result<Boolean> {
        requestPermissionCalled = true
        return Result.success(permissionGranted)
    }
    
    override suspend fun getNotificationPreferences(): kotlinx.coroutines.flow.Flow<NotificationPreferences> {
        return flowOf(savedPreferences ?: NotificationPreferences())
    }
    
    override suspend fun updateNotificationPreferences(preferences: NotificationPreferences): Result<Unit> {
        savedPreferences = preferences
        return Result.success(Unit)
    }
    
    override suspend fun showLocalNotification(
        title: String,
        body: String,
        data: Map<String, String>
    ): Result<Unit> {
        lastNotificationTitle = title
        lastNotificationBody = body
        lastNotificationData = data
        return Result.success(Unit)
    }
    
    override suspend fun clearAllNotifications(): Result<Unit> {
        clearAllCalled = true
        return Result.success(Unit)
    }
    
    override suspend fun handleForegroundNotification(
        title: String,
        body: String,
        data: Map<String, String>
    ): Result<Unit> {
        return showLocalNotification(title, body, data)
    }
    
    override suspend fun handleNotificationClick(data: Map<String, String>): Result<Unit> {
        return Result.success(Unit)
    }
}

private class MockLocalUserRepository : LocalUserRepository {
    private var currentUser: User? = null
    
    fun setCurrentUser(user: User) {
        currentUser = user
    }
    
    override suspend fun getCurrentUser(): User? = currentUser
    override suspend fun saveUser(user: User) {}
    override suspend fun clearUser() {}
    override suspend fun getUserById(id: String): User? = null
    override suspend fun updateUser(user: User) {}
}