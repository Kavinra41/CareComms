package com.carecomms.integration

import com.carecomms.data.models.*
import com.carecomms.data.repository.*
import com.carecomms.domain.usecase.*
import com.carecomms.presentation.analytics.*
import com.carecomms.presentation.auth.*
import com.carecomms.presentation.chat.*
import com.carecomms.presentation.error.ErrorHandler
import com.carecomms.presentation.state.LoadingStateManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Integration test for the complete business logic layer
 * Tests the interaction between use cases, ViewModels, and error handling
 */
class BusinessLogicIntegrationTest {
    
    private lateinit var mockAuthRepository: MockAuthRepository
    private lateinit var mockChatRepository: MockChatRepository
    private lateinit var mockAnalyticsRepository: MockAnalyticsRepository
    private lateinit var mockNetworkMonitor: MockNetworkMonitor
    private lateinit var mockLocalCacheRepository: MockLocalCacheRepository
    
    private lateinit var authUseCase: AuthUseCase
    private lateinit var chatUseCase: ChatUseCase
    private lateinit var analyticsUseCase: AnalyticsUseCase
    
    private lateinit var authViewModel: AuthViewModel
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var analyticsViewModel: AnalyticsViewModel
    
    private lateinit var errorHandler: ErrorHandler
    private lateinit var loadingStateManager: LoadingStateManager
    private lateinit var offlineFirstRepository: OfflineFirstRepository
    
    @BeforeTest
    fun setup() {
        // Setup mock repositories
        mockAuthRepository = MockAuthRepository()
        mockChatRepository = MockChatRepository()
        mockAnalyticsRepository = MockAnalyticsRepository()
        mockNetworkMonitor = MockNetworkMonitor()
        mockLocalCacheRepository = MockLocalCacheRepository()
        
        // Setup utilities
        errorHandler = ErrorHandler()
        loadingStateManager = LoadingStateManager()
        offlineFirstRepository = OfflineFirstRepository(mockLocalCacheRepository, mockNetworkMonitor)
        
        // Setup use cases
        authUseCase = AuthUseCase(mockAuthRepository)
        chatUseCase = ChatUseCase(mockChatRepository)
        analyticsUseCase = AnalyticsUseCase(mockAnalyticsRepository)
        
        // Setup ViewModels
        authViewModel = AuthViewModel(authUseCase)
        chatViewModel = ChatViewModel(chatUseCase, "user1")
        analyticsViewModel = AnalyticsViewModel(analyticsUseCase, "carer1")
    }
    
    @AfterTest
    fun tearDown() {
        authViewModel.onCleared()
        chatViewModel.onCleared()
        analyticsViewModel.onCleared()
    }
    
    @Test
    fun `complete authentication flow should work end to end`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val user = Carer(
            id = "user1",
            email = email,
            createdAt = System.currentTimeMillis(),
            documents = emptyList(),
            age = 30,
            phoneNumber = "1234567890",
            location = "Test City",
            careeIds = emptyList()
        )
        val authResult = AuthResult(user, "token123")
        mockAuthRepository.signInResult = Result.success(authResult)
        
        // When - perform sign in
        authViewModel.handleAction(AuthAction.SignIn(email, password))
        kotlinx.coroutines.delay(100)
        
        // Then - verify authentication state
        val authState = authViewModel.state.value
        assertTrue(authState.authState is com.carecomms.presentation.state.AuthState.Authenticated)
        assertEquals("user1", (authState.authState as com.carecomms.presentation.state.AuthState.Authenticated).userId)
        assertFalse(authState.isLoading)
        assertNull(authState.error)
    }
    
    @Test
    fun `chat flow with offline support should work correctly`() = runTest {
        // Given
        val chatId = "chat1"
        val messages = listOf(
            Message("msg1", "user1", "Hello", System.currentTimeMillis(), MessageStatus.SENT),
            Message("msg2", "user2", "Hi there", System.currentTimeMillis(), MessageStatus.DELIVERED)
        )
        
        // Setup offline scenario
        mockNetworkMonitor.setOnline(false)
        mockLocalCacheRepository.cachedMessages[chatId] = messages
        mockChatRepository.getMessagesResult = flowOf(messages)
        
        // When - load messages while offline
        chatViewModel.handleAction(ChatAction.LoadMessages(chatId))
        kotlinx.coroutines.delay(100)
        
        // Then - should load cached messages
        val chatState = chatViewModel.state.value
        assertEquals(chatId, chatState.chatId)
        assertEquals(messages, chatState.messages)
        assertFalse(chatState.isLoading)
    }
    
    @Test
    fun `analytics flow should handle mock data fallback`() = runTest {
        // Given
        val carees = listOf(
            CareeInfo("caree1", "John Doe", 75, listOf("Diabetes")),
            CareeInfo("caree2", "Jane Smith", 80, listOf("Hypertension"))
        )
        
        // Setup repository to fail and use mock data
        mockAnalyticsRepository.getAvailableCareesResult = Result.success(carees)
        mockAnalyticsRepository.getCareeAnalyticsResult = Result.failure(Exception("Network error"))
        
        // When - load analytics
        analyticsViewModel.handleAction(AnalyticsAction.LoadCarees)
        kotlinx.coroutines.delay(100)
        analyticsViewModel.handleAction(AnalyticsAction.LoadAnalytics)
        kotlinx.coroutines.delay(100)
        
        // Then - should have mock data
        val analyticsState = analyticsViewModel.state.value
        assertEquals(carees, analyticsState.availableCarees)
        assertNotNull(analyticsState.analyticsData)
        assertTrue(analyticsState.error?.contains("mock data") == true)
    }
    
    @Test
    fun `error handling should work across all ViewModels`() = runTest {
        // Given
        val networkError = AppError.NetworkError
        mockAuthRepository.signInResult = Result.failure(networkError)
        
        // When - attempt sign in with network error
        authViewModel.handleAction(AuthAction.SignIn("test@example.com", "password123"))
        kotlinx.coroutines.delay(100)
        
        // Then - should handle error gracefully
        val authState = authViewModel.state.value
        assertNotNull(authState.error)
        assertTrue(authState.error?.contains("Network") == true)
        assertFalse(authState.isLoading)
    }
    
    @Test
    fun `loading state management should work correctly`() = runTest {
        // Given
        val operationId = "test_operation"
        
        // When - set loading state
        loadingStateManager.setLoading(operationId, true, "Loading test data...")
        
        // Then - should be loading
        assertTrue(loadingStateManager.isLoading(operationId))
        assertTrue(loadingStateManager.isAnyLoading())
        assertEquals("Loading test data...", loadingStateManager.getLoadingMessage(operationId))
        
        // When - clear loading state
        loadingStateManager.setLoading(operationId, false)
        
        // Then - should not be loading
        assertFalse(loadingStateManager.isLoading(operationId))
        assertFalse(loadingStateManager.isAnyLoading())
    }
    
    @Test
    fun `offline-first repository should sync when coming online`() = runTest {
        // Given
        val chatId = "chat1"
        val messages = listOf(
            Message("msg1", "user1", "Hello", System.currentTimeMillis(), MessageStatus.SENT)
        )
        
        // Start offline
        mockNetworkMonitor.setOnline(false)
        
        // When - cache data while offline
        offlineFirstRepository.cacheMessages(chatId, messages)
        
        // Then - should have cached data
        val cachedMessages = offlineFirstRepository.getCachedMessages(chatId)
        assertEquals(messages, cachedMessages)
        
        // When - come back online
        mockNetworkMonitor.setOnline(true)
        
        // Then - should be able to sync
        val networkStatus = offlineFirstRepository.getNetworkStatus().first()
        assertTrue(networkStatus)
    }
    
    @Test
    fun `MVI pattern should maintain state consistency`() = runTest {
        // Given
        val chatId = "chat1"
        val initialMessage = "Hello"
        val updatedMessage = "Hello World"
        
        // When - update current message multiple times
        chatViewModel.handleAction(ChatAction.UpdateCurrentMessage(initialMessage))
        chatViewModel.handleAction(ChatAction.UpdateCurrentMessage(updatedMessage))
        
        // Then - state should reflect latest update
        val chatState = chatViewModel.state.value
        assertEquals(updatedMessage, chatState.currentMessage)
        
        // When - clear error
        chatViewModel.handleAction(ChatAction.ClearError)
        
        // Then - error should be cleared
        assertNull(chatViewModel.state.value.error)
    }
    
    @Test
    fun `use case validation should prevent invalid operations`() = runTest {
        // Given
        val emptyEmail = ""
        val validPassword = "password123"
        
        // When - attempt sign in with empty email
        val result = authUseCase.signIn(emptyEmail, validPassword)
        
        // Then - should fail validation
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("required") == true)
    }
    
    @Test
    fun `analytics use case should generate consistent mock data`() = runTest {
        // Given
        val careeIds = listOf("caree1", "caree2")
        val period = AnalyticsPeriod.DAILY
        
        // When - generate mock data multiple times
        val mockData1 = analyticsUseCase.generateMockAnalytics(careeIds, period)
        val mockData2 = analyticsUseCase.generateMockAnalytics(careeIds, period)
        
        // Then - should have consistent structure
        assertEquals(mockData1.dailyData.size, mockData2.dailyData.size)
        assertTrue(mockData1.dailyData.isNotEmpty())
        assertTrue(mockData1.weeklyData.isEmpty())
        assertTrue(mockData1.biweeklyData.isEmpty())
    }
}

// Mock implementations for testing
class MockAuthRepository : com.carecomms.domain.repository.AuthRepository {
    var signInResult: Result<AuthResult> = Result.failure(Exception("Not implemented"))
    var signOutResult: Result<Unit> = Result.success(Unit)
    var currentUser: User? = null
    
    override suspend fun signInWithEmail(email: String, password: String): Result<AuthResult> = signInResult
    override suspend fun signUpCarer(carerData: CarerRegistrationData): Result<AuthResult> = Result.failure(Exception("Not implemented"))
    override suspend fun signUpCaree(careeData: CareeRegistrationData, invitationToken: String): Result<AuthResult> = Result.failure(Exception("Not implemented"))
    override suspend fun signOut(): Result<Unit> = signOutResult
    override suspend fun getCurrentUser(): User? = currentUser
    override suspend fun validateInvitationToken(token: String): Result<CarerInfo> = Result.failure(Exception("Not implemented"))
}

class MockChatRepository : com.carecomms.domain.repository.ChatRepository {
    var getChatListResult: Flow<List<ChatPreview>> = flowOf(emptyList())
    var getMessagesResult: Flow<List<Message>> = flowOf(emptyList())
    var sendMessageResult: Result<Unit> = Result.success(Unit)
    
    override suspend fun getChatList(carerId: String): Flow<List<ChatPreview>> = getChatListResult
    override suspend fun getMessages(chatId: String): Flow<List<Message>> = getMessagesResult
    override suspend fun sendMessage(chatId: String, message: Message): Result<Unit> = sendMessageResult
    override suspend fun markAsRead(chatId: String, messageId: String): Result<Unit> = Result.success(Unit)
    override suspend fun markAllAsRead(chatId: String): Result<Unit> = Result.success(Unit)
    override suspend fun getTypingStatus(chatId: String): Flow<TypingStatus> = flowOf(TypingStatus("", false))
    override suspend fun setTypingStatus(chatId: String, isTyping: Boolean): Result<Unit> = Result.success(Unit)
    override suspend fun getChatId(carerId: String, careeId: String): String? = null
    override suspend fun createChat(carerId: String, careeId: String): Result<String> = Result.success("chat1")
    override suspend fun searchChats(carerId: String, query: String): Flow<List<ChatPreview>> = getChatListResult
}

class MockAnalyticsRepository : com.carecomms.domain.repository.AnalyticsRepository {
    var getAvailableCareesResult: Result<List<CareeInfo>> = Result.failure(Exception("Not implemented"))
    var getCareeAnalyticsResult: Result<AnalyticsData> = Result.failure(Exception("Not implemented"))
    var getMultiCareeAnalyticsResult: Result<AnalyticsData> = Result.failure(Exception("Not implemented"))
    
    override suspend fun getCareeAnalytics(careeId: String, period: AnalyticsPeriod): Result<AnalyticsData> = getCareeAnalyticsResult
    override suspend fun getMultiCareeAnalytics(careeIds: List<String>, period: AnalyticsPeriod): Result<AnalyticsData> = getMultiCareeAnalyticsResult
    override suspend fun getAvailableCarees(carerId: String): Result<List<CareeInfo>> = getAvailableCareesResult
    override suspend fun getDetailsTree(careeId: String): Result<List<DetailsTreeNode>> = Result.failure(Exception("Not implemented"))
    override suspend fun getDetailsTreeForMultipleCarees(careeIds: List<String>): Result<List<DetailsTreeNode>> = Result.failure(Exception("Not implemented"))
    override suspend fun getMockAnalyticsData(careeId: String, period: AnalyticsPeriod): AnalyticsData = AnalyticsData(emptyList(), emptyList(), emptyList(), emptyList())
    override suspend fun getMockDetailsTree(careeId: String): List<DetailsTreeNode> = emptyList()
}

class MockLocalCacheRepository : LocalCacheRepository {
    val cachedMessages = mutableMapOf<String, List<Message>>()
    val cachedChatPreviews = mutableMapOf<String, List<ChatPreview>>()
    val cacheTimestamps = mutableMapOf<String, Long>()
    
    override suspend fun cacheMessages(chatId: String, messages: List<Message>) {
        cachedMessages[chatId] = messages
    }
    
    override suspend fun getCachedMessages(chatId: String): List<Message> {
        return cachedMessages[chatId] ?: emptyList()
    }
    
    override suspend fun cacheChatPreviews(carerId: String, previews: List<ChatPreview>) {
        cachedChatPreviews[carerId] = previews
    }
    
    override suspend fun getCachedChatPreviews(carerId: String): List<ChatPreview> {
        return cachedChatPreviews[carerId] ?: emptyList()
    }
    
    override suspend fun isDataStale(cacheKey: String, maxAgeMillis: Long): Boolean = false
    override suspend fun clearExpiredCache() {}
}

class MockNetworkMonitor : NetworkMonitor {
    private val _isOnline = MutableStateFlow(true)
    override val isOnline: Flow<Boolean> = _isOnline.asStateFlow()
    
    fun setOnline(online: Boolean) {
        _isOnline.value = online
    }
}