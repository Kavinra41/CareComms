package com.carecomms.data.repository

import com.carecomms.data.models.*
import com.carecomms.domain.repository.ChatRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class RealtimeChatRepositoryTest {

    private lateinit var mockFirebaseRepository: MockChatRepository
    private lateinit var mockLocalRepository: MockChatRepository
    private lateinit var mockNetworkMonitor: MockNetworkMonitor
    private lateinit var realtimeChatRepository: RealtimeChatRepository

    @BeforeTest
    fun setup() {
        mockFirebaseRepository = MockChatRepository()
        mockLocalRepository = MockChatRepository()
        mockNetworkMonitor = MockNetworkMonitor()
        realtimeChatRepository = RealtimeChatRepository(
            firebaseChatRepository = mockFirebaseRepository,
            localChatRepository = mockLocalRepository,
            networkMonitor = mockNetworkMonitor
        )
    }

    @Test
    fun `getChatList returns Firebase data when online`() = runTest {
        // Given
        val carerId = "carer123"
        val firebaseChats = listOf(
            ChatPreview("chat1", "John", "Hello from Firebase", 0L, 1, true)
        )
        val localChats = listOf(
            ChatPreview("chat1", "John", "Hello from Local", 0L, 0, false)
        )
        
        mockNetworkMonitor.setOnline(true)
        mockFirebaseRepository.chatPreviews = firebaseChats
        mockLocalRepository.chatPreviews = localChats

        // When
        val result = realtimeChatRepository.getChatList(carerId).first()

        // Then
        assertEquals(firebaseChats, result)
        assertEquals(carerId, mockFirebaseRepository.lastCarerId)
    }

    @Test
    fun `getChatList returns local data when offline`() = runTest {
        // Given
        val carerId = "carer123"
        val firebaseChats = listOf(
            ChatPreview("chat1", "John", "Hello from Firebase", 0L, 1, true)
        )
        val localChats = listOf(
            ChatPreview("chat1", "John", "Hello from Local", 0L, 0, false)
        )
        
        mockNetworkMonitor.setOnline(false)
        mockFirebaseRepository.chatPreviews = firebaseChats
        mockLocalRepository.chatPreviews = localChats

        // When
        val result = realtimeChatRepository.getChatList(carerId).first()

        // Then
        assertEquals(localChats, result)
        assertEquals(carerId, mockLocalRepository.lastCarerId)
    }

    @Test
    fun `getMessages returns Firebase data when online`() = runTest {
        // Given
        val chatId = "chat123"
        val firebaseMessages = listOf(
            Message("msg1", "user1", "Firebase message", 0L, MessageStatus.SENT)
        )
        val localMessages = listOf(
            Message("msg1", "user1", "Local message", 0L, MessageStatus.SENT)
        )
        
        mockNetworkMonitor.setOnline(true)
        mockFirebaseRepository.messages = firebaseMessages
        mockLocalRepository.messages = localMessages

        // When
        val result = realtimeChatRepository.getMessages(chatId).first()

        // Then
        assertEquals(firebaseMessages, result)
        assertEquals(chatId, mockFirebaseRepository.lastChatId)
    }

    @Test
    fun `getMessages returns local data when offline`() = runTest {
        // Given
        val chatId = "chat123"
        val firebaseMessages = listOf(
            Message("msg1", "user1", "Firebase message", 0L, MessageStatus.SENT)
        )
        val localMessages = listOf(
            Message("msg1", "user1", "Local message", 0L, MessageStatus.SENT)
        )
        
        mockNetworkMonitor.setOnline(false)
        mockFirebaseRepository.messages = firebaseMessages
        mockLocalRepository.messages = localMessages

        // When
        val result = realtimeChatRepository.getMessages(chatId).first()

        // Then
        assertEquals(localMessages, result)
        assertEquals(chatId, mockLocalRepository.lastChatId)
    }

    @Test
    fun `sendMessage saves to local and Firebase when online`() = runTest {
        // Given
        val chatId = "chat123"
        val message = Message("msg1", "user1", "Test message", 0L, MessageStatus.SENT)
        
        mockNetworkMonitor.setOnline(true)
        mockLocalRepository.sendMessageResult = Result.success(Unit)
        mockFirebaseRepository.sendMessageResult = Result.success(Unit)

        // When
        val result = realtimeChatRepository.sendMessage(chatId, message)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(chatId, mockLocalRepository.lastChatId)
        assertEquals(message, mockLocalRepository.lastSentMessage)
        assertEquals(chatId, mockFirebaseRepository.lastChatId)
        assertEquals(message, mockFirebaseRepository.lastSentMessage)
    }

    @Test
    fun `sendMessage saves to local only when offline`() = runTest {
        // Given
        val chatId = "chat123"
        val message = Message("msg1", "user1", "Test message", 0L, MessageStatus.SENT)
        
        mockNetworkMonitor.setOnline(false)
        mockLocalRepository.sendMessageResult = Result.success(Unit)

        // When
        val result = realtimeChatRepository.sendMessage(chatId, message)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(chatId, mockLocalRepository.lastChatId)
        assertEquals(message, mockLocalRepository.lastSentMessage)
        assertNull(mockFirebaseRepository.lastChatId) // Firebase not called when offline
    }

    @Test
    fun `sendMessage handles Firebase failure gracefully when online`() = runTest {
        // Given
        val chatId = "chat123"
        val message = Message("msg1", "user1", "Test message", 0L, MessageStatus.SENT)
        
        mockNetworkMonitor.setOnline(true)
        mockLocalRepository.sendMessageResult = Result.success(Unit)
        mockFirebaseRepository.sendMessageResult = Result.failure(Exception("Firebase error"))

        // When
        val result = realtimeChatRepository.sendMessage(chatId, message)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Firebase error", result.exceptionOrNull()?.message)
        // Both repositories should still be called
        assertEquals(chatId, mockLocalRepository.lastChatId)
        assertEquals(chatId, mockFirebaseRepository.lastChatId)
    }

    @Test
    fun `markAsRead calls Firebase when online`() = runTest {
        // Given
        val chatId = "chat123"
        val messageId = "msg123"
        
        mockNetworkMonitor.setOnline(true)
        mockLocalRepository.markAsReadResult = Result.success(Unit)
        mockFirebaseRepository.markAsReadResult = Result.success(Unit)

        // When
        val result = realtimeChatRepository.markAsRead(chatId, messageId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(chatId, mockLocalRepository.lastMarkReadChatId)
        assertEquals(messageId, mockLocalRepository.lastMarkReadMessageId)
        assertEquals(chatId, mockFirebaseRepository.lastMarkReadChatId)
        assertEquals(messageId, mockFirebaseRepository.lastMarkReadMessageId)
    }

    @Test
    fun `markAsRead calls local only when offline`() = runTest {
        // Given
        val chatId = "chat123"
        val messageId = "msg123"
        
        mockNetworkMonitor.setOnline(false)
        mockLocalRepository.markAsReadResult = Result.success(Unit)

        // When
        val result = realtimeChatRepository.markAsRead(chatId, messageId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(chatId, mockLocalRepository.lastMarkReadChatId)
        assertEquals(messageId, mockLocalRepository.lastMarkReadMessageId)
        assertNull(mockFirebaseRepository.lastMarkReadChatId) // Firebase not called when offline
    }

    @Test
    fun `getTypingStatus returns Firebase data when online`() = runTest {
        // Given
        val chatId = "chat123"
        val typingStatus = TypingStatus("user123", true, System.currentTimeMillis())
        
        mockNetworkMonitor.setOnline(true)
        mockFirebaseRepository.typingStatus = typingStatus

        // When
        val result = realtimeChatRepository.getTypingStatus(chatId).first()

        // Then
        assertEquals(typingStatus, result)
        assertEquals(chatId, mockFirebaseRepository.lastTypingChatId)
    }

    @Test
    fun `getTypingStatus returns empty status when offline`() = runTest {
        // Given
        val chatId = "chat123"
        
        mockNetworkMonitor.setOnline(false)

        // When
        val result = realtimeChatRepository.getTypingStatus(chatId).first()

        // Then
        assertEquals("", result.userId)
        assertEquals(false, result.isTyping)
        assertNull(mockFirebaseRepository.lastTypingChatId) // Firebase not called when offline
    }

    @Test
    fun `setTypingStatus calls Firebase when online`() = runTest {
        // Given
        val chatId = "chat123"
        val isTyping = true
        
        mockNetworkMonitor.setOnline(true)
        mockFirebaseRepository.setTypingResult = Result.success(Unit)

        // When
        val result = realtimeChatRepository.setTypingStatus(chatId, isTyping)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(chatId, mockFirebaseRepository.lastSetTypingChatId)
        assertEquals(isTyping, mockFirebaseRepository.lastSetTypingStatus)
    }

    @Test
    fun `setTypingStatus returns success when offline without calling Firebase`() = runTest {
        // Given
        val chatId = "chat123"
        val isTyping = true
        
        mockNetworkMonitor.setOnline(false)

        // When
        val result = realtimeChatRepository.setTypingStatus(chatId, isTyping)

        // Then
        assertTrue(result.isSuccess)
        assertNull(mockFirebaseRepository.lastSetTypingChatId) // Firebase not called when offline
    }

    @Test
    fun `createChat creates in both local and Firebase when online`() = runTest {
        // Given
        val carerId = "carer123"
        val careeId = "caree123"
        val chatId = "new_chat"
        
        mockNetworkMonitor.setOnline(true)
        mockLocalRepository.createChatResult = Result.success(chatId)
        mockFirebaseRepository.createChatResult = Result.success(chatId)

        // When
        val result = realtimeChatRepository.createChat(carerId, careeId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(chatId, result.getOrNull())
        assertEquals(carerId, mockLocalRepository.lastCreateChatCarerId)
        assertEquals(careeId, mockLocalRepository.lastCreateChatCareeId)
        assertEquals(carerId, mockFirebaseRepository.lastCreateChatCarerId)
        assertEquals(careeId, mockFirebaseRepository.lastCreateChatCareeId)
    }

    @Test
    fun `createChat creates in local only when offline`() = runTest {
        // Given
        val carerId = "carer123"
        val careeId = "caree123"
        val chatId = "new_chat"
        
        mockNetworkMonitor.setOnline(false)
        mockLocalRepository.createChatResult = Result.success(chatId)

        // When
        val result = realtimeChatRepository.createChat(carerId, careeId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(chatId, result.getOrNull())
        assertEquals(carerId, mockLocalRepository.lastCreateChatCarerId)
        assertEquals(careeId, mockLocalRepository.lastCreateChatCareeId)
        assertNull(mockFirebaseRepository.lastCreateChatCarerId) // Firebase not called when offline
    }

    @Test
    fun `getChatId checks Firebase first when online`() = runTest {
        // Given
        val carerId = "carer123"
        val careeId = "caree123"
        val chatId = "existing_chat"
        
        mockNetworkMonitor.setOnline(true)
        mockFirebaseRepository.existingChatId = chatId

        // When
        val result = realtimeChatRepository.getChatId(carerId, careeId)

        // Then
        assertEquals(chatId, result)
        assertEquals(carerId, mockFirebaseRepository.lastGetChatCarerId)
        assertEquals(careeId, mockFirebaseRepository.lastGetChatCareeId)
    }

    @Test
    fun `getChatId checks local only when offline`() = runTest {
        // Given
        val carerId = "carer123"
        val careeId = "caree123"
        val chatId = "existing_chat"
        
        mockNetworkMonitor.setOnline(false)
        mockLocalRepository.existingChatId = chatId

        // When
        val result = realtimeChatRepository.getChatId(carerId, careeId)

        // Then
        assertEquals(chatId, result)
        assertEquals(carerId, mockLocalRepository.lastGetChatCarerId)
        assertEquals(careeId, mockLocalRepository.lastGetChatCareeId)
        assertNull(mockFirebaseRepository.lastGetChatCarerId) // Firebase not called when offline
    }

    @Test
    fun `searchChats uses Firebase when online`() = runTest {
        // Given
        val carerId = "carer123"
        val query = "john"
        val searchResults = listOf(
            ChatPreview("chat1", "John", "Hello", 0L, 0, false)
        )
        
        mockNetworkMonitor.setOnline(true)
        mockFirebaseRepository.searchResults = searchResults

        // When
        val result = realtimeChatRepository.searchChats(carerId, query).first()

        // Then
        assertEquals(searchResults, result)
        assertEquals(carerId, mockFirebaseRepository.lastSearchCarerId)
        assertEquals(query, mockFirebaseRepository.lastSearchQuery)
    }

    @Test
    fun `searchChats uses local when offline`() = runTest {
        // Given
        val carerId = "carer123"
        val query = "john"
        val searchResults = listOf(
            ChatPreview("chat1", "John", "Hello", 0L, 0, false)
        )
        
        mockNetworkMonitor.setOnline(false)
        mockLocalRepository.searchResults = searchResults

        // When
        val result = realtimeChatRepository.searchChats(carerId, query).first()

        // Then
        assertEquals(searchResults, result)
        assertEquals(carerId, mockLocalRepository.lastSearchCarerId)
        assertEquals(query, mockLocalRepository.lastSearchQuery)
        assertNull(mockFirebaseRepository.lastSearchCarerId) // Firebase not called when offline
    }
}

// Mock NetworkMonitor for testing
class MockNetworkMonitor : NetworkMonitor {
    private val _isOnline = MutableStateFlow(true)
    override val isOnline: Flow<Boolean> = _isOnline.asStateFlow()

    fun setOnline(online: Boolean) {
        _isOnline.value = online
    }
}