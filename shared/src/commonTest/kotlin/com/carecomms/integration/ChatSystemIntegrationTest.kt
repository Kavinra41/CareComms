package com.carecomms.integration

import com.carecomms.data.database.DatabaseManager
import com.carecomms.data.models.*
import com.carecomms.data.repository.*
import com.carecomms.domain.usecase.ChatUseCase
import com.carecomms.presentation.chat.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ChatSystemIntegrationTest {

    private lateinit var databaseManager: DatabaseManager
    private lateinit var localChatRepository: LocalChatRepository
    private lateinit var mockFirebaseRepository: MockChatRepository
    private lateinit var mockNetworkMonitor: MockNetworkMonitor
    private lateinit var realtimeChatRepository: RealtimeChatRepository
    private lateinit var chatUseCase: ChatUseCase
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatListViewModel: ChatListViewModel
    
    private val testDispatcher = StandardTestDispatcher()
    private val carerId = "carer123"
    private val careeId = "caree123"
    private val currentUserId = "current_user"

    @BeforeTest
    fun setup() {
        // Set up database
        databaseManager = DatabaseManager(":memory:")
        
        // Set up repositories
        localChatRepository = LocalChatRepository(databaseManager)
        mockFirebaseRepository = MockChatRepository()
        mockNetworkMonitor = MockNetworkMonitor()
        
        realtimeChatRepository = RealtimeChatRepository(
            firebaseChatRepository = mockFirebaseRepository,
            localChatRepository = localChatRepository,
            networkMonitor = mockNetworkMonitor
        )
        
        // Set up use case
        chatUseCase = ChatUseCase(realtimeChatRepository)
        
        // Set up view models
        chatViewModel = ChatViewModel(chatUseCase, currentUserId)
        chatListViewModel = ChatListViewModel(chatUseCase, carerId)
    }

    @Test
    fun `complete chat flow works end to end`() = runTest(testDispatcher) {
        // Given - Create a chat and some test data
        val chatResult = chatUseCase.createOrGetChat(carerId, careeId)
        assertTrue(chatResult.isSuccess)
        val chatId = chatResult.getOrNull()!!
        
        // Set up some initial messages in local storage
        val message1 = Message(
            id = "msg1",
            senderId = carerId,
            content = "Hello, how are you?",
            timestamp = System.currentTimeMillis() - 1000,
            status = MessageStatus.SENT
        )
        val message2 = Message(
            id = "msg2", 
            senderId = careeId,
            content = "I'm doing well, thank you!",
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.DELIVERED
        )
        
        localChatRepository.sendMessage(chatId, message1)
        localChatRepository.sendMessage(chatId, message2)
        
        // When - Load messages in chat view model
        chatViewModel.handleAction(ChatAction.LoadMessages(chatId))
        advanceUntilIdle()
        
        // Then - Messages should be loaded
        val chatState = chatViewModel.state.value
        assertEquals(chatId, chatState.chatId)
        assertEquals(2, chatState.messages.size)
        assertEquals("Hello, how are you?", chatState.messages[0].content)
        assertEquals("I'm doing well, thank you!", chatState.messages[1].content)
        
        // When - Send a new message
        val newMessageContent = "That's great to hear!"
        chatViewModel.handleAction(ChatAction.UpdateCurrentMessage(newMessageContent))
        chatViewModel.handleAction(ChatAction.SendMessage(newMessageContent))
        advanceUntilIdle()
        
        // Then - Message should be sent and state updated
        val updatedChatState = chatViewModel.state.value
        assertEquals("", updatedChatState.currentMessage) // Should be cleared after sending
        assertFalse(updatedChatState.isSendingMessage)
        
        // And - Chat list should show the updated conversation
        chatListViewModel.handleAction(ChatListAction.LoadChats)
        advanceUntilIdle()
        
        val chatListState = chatListViewModel.state.value
        assertEquals(1, chatListState.chatPreviews.size)
        val chatPreview = chatListState.chatPreviews[0]
        assertEquals(chatId, chatPreview.chatId)
        assertTrue(chatPreview.lastMessage.isNotEmpty())
    }

    @Test
    fun `offline to online sync works correctly`() = runTest(testDispatcher) {
        // Given - Start offline
        mockNetworkMonitor.setOnline(false)
        
        val chatResult = chatUseCase.createOrGetChat(carerId, careeId)
        assertTrue(chatResult.isSuccess)
        val chatId = chatResult.getOrNull()!!
        
        // When - Send message while offline
        val offlineMessage = "Sent while offline"
        val sendResult = chatUseCase.sendMessage(chatId, currentUserId, offlineMessage)
        assertTrue(sendResult.isSuccess)
        
        // Then - Message should be in local storage only
        val localMessages = localChatRepository.getMessages(chatId).first()
        assertEquals(1, localMessages.size)
        assertEquals(offlineMessage, localMessages[0].content)
        
        // And Firebase should not have been called
        assertNull(mockFirebaseRepository.lastSentMessage)
        
        // When - Go online
        mockNetworkMonitor.setOnline(true)
        
        // Set up Firebase to return the synced data
        mockFirebaseRepository.messages = localMessages
        mockFirebaseRepository.sendMessageResult = Result.success(Unit)
        
        // Load messages again (simulating sync)
        chatViewModel.handleAction(ChatAction.LoadMessages(chatId))
        advanceUntilIdle()
        
        // Then - Should now use Firebase data
        val chatState = chatViewModel.state.value
        assertEquals(1, chatState.messages.size)
        assertEquals(offlineMessage, chatState.messages[0].content)
    }

    @Test
    fun `typing indicators work correctly`() = runTest(testDispatcher) {
        // Given
        val chatResult = chatUseCase.createOrGetChat(carerId, careeId)
        val chatId = chatResult.getOrNull()!!
        
        mockNetworkMonitor.setOnline(true)
        mockFirebaseRepository.setTypingResult = Result.success(Unit)
        
        // Set up typing status from other user
        val otherUserTyping = TypingStatus("other_user", true, System.currentTimeMillis())
        mockFirebaseRepository.typingStatus = otherUserTyping
        
        // When - Load chat
        chatViewModel.handleAction(ChatAction.LoadMessages(chatId))
        advanceUntilIdle()
        
        // Then - Should show other user typing
        val chatState1 = chatViewModel.state.value
        assertEquals(otherUserTyping, chatState1.otherUserTyping)
        
        // When - Start typing
        chatViewModel.handleAction(ChatAction.UpdateCurrentMessage("typing..."))
        advanceUntilIdle()
        
        // Then - Should set typing status
        val chatState2 = chatViewModel.state.value
        assertTrue(chatState2.isTyping)
        assertEquals(chatId, mockFirebaseRepository.lastSetTypingChatId)
        assertEquals(true, mockFirebaseRepository.lastSetTypingStatus)
        
        // When - Stop typing
        chatViewModel.handleAction(ChatAction.UpdateCurrentMessage(""))
        advanceUntilIdle()
        
        // Then - Should clear typing status
        val chatState3 = chatViewModel.state.value
        assertFalse(chatState3.isTyping)
        assertEquals(false, mockFirebaseRepository.lastSetTypingStatus)
    }

    @Test
    fun `search functionality works across the system`() = runTest(testDispatcher) {
        // Given - Create multiple chats with different carees
        val caree1Id = "caree1"
        val caree2Id = "caree2"
        
        val chat1Result = chatUseCase.createOrGetChat(carerId, caree1Id)
        val chat2Result = chatUseCase.createOrGetChat(carerId, caree2Id)
        
        val chat1Id = chat1Result.getOrNull()!!
        val chat2Id = chat2Result.getOrNull()!!
        
        // Add some messages to create chat previews
        localChatRepository.sendMessage(chat1Id, Message(
            "msg1", caree1Id, "Hello from John", System.currentTimeMillis(), MessageStatus.SENT
        ))
        localChatRepository.sendMessage(chat2Id, Message(
            "msg2", caree2Id, "Hi from Jane", System.currentTimeMillis(), MessageStatus.SENT
        ))
        
        // Set up mock data for chat list
        val chatPreviews = listOf(
            ChatPreview(chat1Id, "John Doe", "Hello from John", System.currentTimeMillis(), 1, false),
            ChatPreview(chat2Id, "Jane Smith", "Hi from Jane", System.currentTimeMillis(), 1, false)
        )
        mockFirebaseRepository.chatPreviews = chatPreviews
        mockNetworkMonitor.setOnline(true)
        
        // When - Load chats
        chatListViewModel.handleAction(ChatListAction.LoadChats)
        advanceUntilIdle()
        
        // Then - Should show both chats
        val initialState = chatListViewModel.state.value
        assertEquals(2, initialState.chatPreviews.size)
        assertEquals(2, initialState.filteredChats.size)
        
        // When - Search for "john"
        chatListViewModel.handleAction(ChatListAction.SearchChats("john"))
        
        // Then - Should filter to only John's chat
        val searchState = chatListViewModel.state.value
        assertEquals("john", searchState.searchQuery)
        assertEquals(1, searchState.filteredChats.size)
        assertEquals("John Doe", searchState.filteredChats[0].careeName)
        
        // When - Clear search
        chatListViewModel.handleAction(ChatListAction.SearchChats(""))
        
        // Then - Should show all chats again
        val clearedState = chatListViewModel.state.value
        assertEquals("", clearedState.searchQuery)
        assertEquals(2, clearedState.filteredChats.size)
    }

    @Test
    fun `message status updates work correctly`() = runTest(testDispatcher) {
        // Given
        val chatResult = chatUseCase.createOrGetChat(carerId, careeId)
        val chatId = chatResult.getOrNull()!!
        
        mockNetworkMonitor.setOnline(true)
        mockFirebaseRepository.markAsReadResult = Result.success(Unit)
        
        // Add a message
        val message = Message(
            "msg1", careeId, "Test message", System.currentTimeMillis(), MessageStatus.DELIVERED
        )
        localChatRepository.sendMessage(chatId, message)
        
        // When - Load chat and mark message as read
        chatViewModel.handleAction(ChatAction.LoadMessages(chatId))
        chatViewModel.handleAction(ChatAction.MarkMessageAsRead("msg1"))
        advanceUntilIdle()
        
        // Then - Should call repository to mark as read
        assertEquals(chatId, mockFirebaseRepository.lastMarkReadChatId)
        assertEquals("msg1", mockFirebaseRepository.lastMarkReadMessageId)
        
        // When - Mark all messages as read
        chatViewModel.handleAction(ChatAction.MarkAllAsRead)
        advanceUntilIdle()
        
        // Then - Should call repository to mark all as read
        assertEquals(chatId, mockFirebaseRepository.lastMarkAllReadChatId)
    }

    @Test
    fun `error handling works throughout the system`() = runTest(testDispatcher) {
        // Given
        mockNetworkMonitor.setOnline(true)
        mockFirebaseRepository.sendMessageResult = Result.failure(Exception("Network error"))
        
        val chatResult = chatUseCase.createOrGetChat(carerId, careeId)
        val chatId = chatResult.getOrNull()!!
        
        // When - Try to send message with Firebase error
        chatViewModel.handleAction(ChatAction.LoadMessages(chatId))
        chatViewModel.handleAction(ChatAction.SendMessage("Test message"))
        advanceUntilIdle()
        
        // Then - Should show error in chat view model
        val chatState = chatViewModel.state.value
        assertNotNull(chatState.error)
        assertTrue(chatState.error!!.contains("Network error"))
        
        // When - Clear error
        chatViewModel.handleAction(ChatAction.ClearError)
        
        // Then - Error should be cleared
        val clearedState = chatViewModel.state.value
        assertNull(clearedState.error)
    }

    @Test
    fun `unread count tracking works correctly`() = runTest(testDispatcher) {
        // Given - Create chats with unread messages
        val chat1Result = chatUseCase.createOrGetChat(carerId, "caree1")
        val chat2Result = chatUseCase.createOrGetChat(carerId, "caree2")
        
        val chatPreviews = listOf(
            ChatPreview(chat1Result.getOrNull()!!, "John", "Hello", System.currentTimeMillis(), 3, false),
            ChatPreview(chat2Result.getOrNull()!!, "Jane", "Hi", System.currentTimeMillis(), 2, false)
        )
        mockFirebaseRepository.chatPreviews = chatPreviews
        mockNetworkMonitor.setOnline(true)
        
        // When - Load chats
        chatListViewModel.handleAction(ChatListAction.LoadChats)
        advanceUntilIdle()
        
        // Then - Should calculate total unread count
        val state = chatListViewModel.state.value
        assertEquals(5, state.unreadCount) // 3 + 2
    }
}