package com.carecomms.domain.usecase

import com.carecomms.data.models.*
import com.carecomms.domain.repository.ChatRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class ChatUseCaseTest {

    private lateinit var mockChatRepository: MockChatRepository
    private lateinit var chatUseCase: ChatUseCase

    @BeforeTest
    fun setup() {
        mockChatRepository = MockChatRepository()
        chatUseCase = ChatUseCase(mockChatRepository)
    }

    @Test
    fun `getChatList returns chat previews from repository`() = runTest {
        // Given
        val carerId = "carer123"
        val expectedChats = listOf(
            ChatPreview(
                chatId = "chat1",
                careeName = "John Doe",
                lastMessage = "Hello",
                lastMessageTime = System.currentTimeMillis(),
                unreadCount = 2,
                isOnline = true
            )
        )
        mockChatRepository.chatPreviews = expectedChats

        // When
        val result = chatUseCase.getChatList(carerId).toList()

        // Then
        assertEquals(expectedChats, result.first())
        assertEquals(carerId, mockChatRepository.lastCarerId)
    }

    @Test
    fun `getMessages returns messages from repository`() = runTest {
        // Given
        val chatId = "chat123"
        val expectedMessages = listOf(
            Message(
                id = "msg1",
                senderId = "user1",
                content = "Hello",
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.SENT
            )
        )
        mockChatRepository.messages = expectedMessages

        // When
        val result = chatUseCase.getMessages(chatId).toList()

        // Then
        assertEquals(expectedMessages, result.first())
        assertEquals(chatId, mockChatRepository.lastChatId)
    }

    @Test
    fun `sendMessage creates message with correct properties`() = runTest {
        // Given
        val chatId = "chat123"
        val senderId = "user123"
        val content = "Test message"
        mockChatRepository.sendMessageResult = Result.success(Unit)

        // When
        val result = chatUseCase.sendMessage(chatId, senderId, content)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(mockChatRepository.lastSentMessage)
        assertEquals(senderId, mockChatRepository.lastSentMessage?.senderId)
        assertEquals(content, mockChatRepository.lastSentMessage?.content)
        assertEquals(MessageStatus.SENT, mockChatRepository.lastSentMessage?.status)
        assertEquals(MessageType.TEXT, mockChatRepository.lastSentMessage?.type)
        assertTrue(mockChatRepository.lastSentMessage?.id?.isNotEmpty() == true)
    }

    @Test
    fun `sendMessage fails with empty content`() = runTest {
        // Given
        val chatId = "chat123"
        val senderId = "user123"
        val content = "   "

        // When
        val result = chatUseCase.sendMessage(chatId, senderId, content)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Message content cannot be empty", result.exceptionOrNull()?.message)
        assertNull(mockChatRepository.lastSentMessage)
    }

    @Test
    fun `markMessageAsRead calls repository with correct parameters`() = runTest {
        // Given
        val chatId = "chat123"
        val messageId = "msg123"
        mockChatRepository.markAsReadResult = Result.success(Unit)

        // When
        val result = chatUseCase.markMessageAsRead(chatId, messageId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(chatId, mockChatRepository.lastMarkReadChatId)
        assertEquals(messageId, mockChatRepository.lastMarkReadMessageId)
    }

    @Test
    fun `markAllMessagesAsRead calls repository with correct chat ID`() = runTest {
        // Given
        val chatId = "chat123"
        mockChatRepository.markAllAsReadResult = Result.success(Unit)

        // When
        val result = chatUseCase.markAllMessagesAsRead(chatId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(chatId, mockChatRepository.lastMarkAllReadChatId)
    }

    @Test
    fun `getTypingStatus returns typing status from repository`() = runTest {
        // Given
        val chatId = "chat123"
        val expectedTypingStatus = TypingStatus("user123", true, System.currentTimeMillis())
        mockChatRepository.typingStatus = expectedTypingStatus

        // When
        val result = chatUseCase.getTypingStatus(chatId).toList()

        // Then
        assertEquals(expectedTypingStatus, result.first())
        assertEquals(chatId, mockChatRepository.lastTypingChatId)
    }

    @Test
    fun `setTypingStatus calls repository with correct parameters`() = runTest {
        // Given
        val chatId = "chat123"
        val isTyping = true
        mockChatRepository.setTypingResult = Result.success(Unit)

        // When
        val result = chatUseCase.setTypingStatus(chatId, isTyping)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(chatId, mockChatRepository.lastSetTypingChatId)
        assertEquals(isTyping, mockChatRepository.lastSetTypingStatus)
    }

    @Test
    fun `createOrGetChat returns existing chat ID when chat exists`() = runTest {
        // Given
        val carerId = "carer123"
        val careeId = "caree123"
        val existingChatId = "existing_chat"
        mockChatRepository.existingChatId = existingChatId

        // When
        val result = chatUseCase.createOrGetChat(carerId, careeId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(existingChatId, result.getOrNull())
        assertEquals(carerId, mockChatRepository.lastGetChatCarerId)
        assertEquals(careeId, mockChatRepository.lastGetChatCareeId)
    }

    @Test
    fun `createOrGetChat creates new chat when none exists`() = runTest {
        // Given
        val carerId = "carer123"
        val careeId = "caree123"
        val newChatId = "new_chat"
        mockChatRepository.existingChatId = null
        mockChatRepository.createChatResult = Result.success(newChatId)

        // When
        val result = chatUseCase.createOrGetChat(carerId, careeId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(newChatId, result.getOrNull())
        assertEquals(carerId, mockChatRepository.lastCreateChatCarerId)
        assertEquals(careeId, mockChatRepository.lastCreateChatCareeId)
    }

    @Test
    fun `searchChats returns all chats when query is blank`() = runTest {
        // Given
        val carerId = "carer123"
        val query = "   "
        val allChats = listOf(
            ChatPreview("chat1", "John", "Hello", 0L, 0, false),
            ChatPreview("chat2", "Jane", "Hi", 0L, 0, false)
        )
        mockChatRepository.chatPreviews = allChats

        // When
        val result = chatUseCase.searchChats(carerId, query).toList()

        // Then
        assertEquals(allChats, result.first())
    }

    @Test
    fun `searchChats filters chats when query is provided`() = runTest {
        // Given
        val carerId = "carer123"
        val query = "john"
        val filteredChats = listOf(
            ChatPreview("chat1", "John", "Hello", 0L, 0, false)
        )
        mockChatRepository.searchResults = filteredChats

        // When
        val result = chatUseCase.searchChats(carerId, query).toList()

        // Then
        assertEquals(filteredChats, result.first())
        assertEquals(carerId, mockChatRepository.lastSearchCarerId)
        assertEquals(query, mockChatRepository.lastSearchQuery)
    }

    @Test
    fun `getUnreadMessageCount sums unread counts from chat previews`() = runTest {
        // Given
        val carerId = "carer123"
        val chats = listOf(
            ChatPreview("chat1", "John", "Hello", 0L, 3, false),
            ChatPreview("chat2", "Jane", "Hi", 0L, 2, false),
            ChatPreview("chat3", "Bob", "Hey", 0L, 0, false)
        )
        mockChatRepository.chatPreviews = chats

        // When
        val result = chatUseCase.getUnreadMessageCount(carerId).toList()

        // Then
        assertEquals(5, result.first())
    }

    @Test
    fun `validateMessageContent returns success for valid content`() {
        // Given
        val validContent = "This is a valid message"

        // When
        val result = chatUseCase.validateMessageContent(validContent)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(validContent, result.getOrNull())
    }

    @Test
    fun `validateMessageContent fails for empty content`() {
        // Given
        val emptyContent = "   "

        // When
        val result = chatUseCase.validateMessageContent(emptyContent)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Message cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `validateMessageContent fails for too long content`() {
        // Given
        val longContent = "a".repeat(1001)

        // When
        val result = chatUseCase.validateMessageContent(longContent)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("too long") == true)
    }
}

// Mock implementation for testing
class MockChatRepository : ChatRepository {
    var chatPreviews = emptyList<ChatPreview>()
    var messages = emptyList<Message>()
    var typingStatus = TypingStatus("", false, 0L)
    var sendMessageResult = Result.success(Unit)
    var markAsReadResult = Result.success(Unit)
    var markAllAsReadResult = Result.success(Unit)
    var setTypingResult = Result.success(Unit)
    var createChatResult = Result.success("new_chat")
    var existingChatId: String? = null
    var searchResults = emptyList<ChatPreview>()

    var lastCarerId: String? = null
    var lastChatId: String? = null
    var lastSentMessage: Message? = null
    var lastMarkReadChatId: String? = null
    var lastMarkReadMessageId: String? = null
    var lastMarkAllReadChatId: String? = null
    var lastTypingChatId: String? = null
    var lastSetTypingChatId: String? = null
    var lastSetTypingStatus: Boolean? = null
    var lastCreateChatCarerId: String? = null
    var lastCreateChatCareeId: String? = null
    var lastGetChatCarerId: String? = null
    var lastGetChatCareeId: String? = null
    var lastSearchCarerId: String? = null
    var lastSearchQuery: String? = null

    override suspend fun getChatList(carerId: String) = flowOf(chatPreviews).also {
        lastCarerId = carerId
    }

    override suspend fun getMessages(chatId: String) = flowOf(messages).also {
        lastChatId = chatId
    }

    override suspend fun sendMessage(chatId: String, message: Message): Result<Unit> {
        lastChatId = chatId
        lastSentMessage = message
        return sendMessageResult
    }

    override suspend fun markAsRead(chatId: String, messageId: String): Result<Unit> {
        lastMarkReadChatId = chatId
        lastMarkReadMessageId = messageId
        return markAsReadResult
    }

    override suspend fun markAllAsRead(chatId: String): Result<Unit> {
        lastMarkAllReadChatId = chatId
        return markAllAsReadResult
    }

    override suspend fun getTypingStatus(chatId: String) = flowOf(typingStatus).also {
        lastTypingChatId = chatId
    }

    override suspend fun setTypingStatus(chatId: String, isTyping: Boolean): Result<Unit> {
        lastSetTypingChatId = chatId
        lastSetTypingStatus = isTyping
        return setTypingResult
    }

    override suspend fun createChat(carerId: String, careeId: String): Result<String> {
        lastCreateChatCarerId = carerId
        lastCreateChatCareeId = careeId
        return createChatResult
    }

    override suspend fun getChatId(carerId: String, careeId: String): String? {
        lastGetChatCarerId = carerId
        lastGetChatCareeId = careeId
        return existingChatId
    }

    override suspend fun searchChats(carerId: String, query: String) = flowOf(searchResults).also {
        lastSearchCarerId = carerId
        lastSearchQuery = query
    }
}