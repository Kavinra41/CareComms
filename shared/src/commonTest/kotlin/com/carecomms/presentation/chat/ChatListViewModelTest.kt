package com.carecomms.presentation.chat

import com.carecomms.data.models.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ChatListViewModelTest {

    private lateinit var mockChatUseCase: MockChatUseCase
    private lateinit var chatListViewModel: ChatListViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val carerId = "carer123"

    @BeforeTest
    fun setup() {
        mockChatUseCase = MockChatUseCase()
        chatListViewModel = ChatListViewModel(mockChatUseCase, carerId)
    }

    @Test
    fun `initial state is correct`() {
        val state = chatListViewModel.state.value
        assertEquals(ChatListState(), state)
    }

    @Test
    fun `loadChats updates state with chat previews`() = runTest(testDispatcher) {
        // Given
        val chatPreviews = listOf(
            ChatPreview("chat1", "John Doe", "Hello", System.currentTimeMillis(), 2, true),
            ChatPreview("chat2", "Jane Smith", "Hi there", System.currentTimeMillis(), 0, false)
        )
        mockChatUseCase.chatPreviews = chatPreviews

        // When
        chatListViewModel.handleAction(ChatListAction.LoadChats)
        advanceUntilIdle()

        // Then
        val state = chatListViewModel.state.value
        assertEquals(chatPreviews, state.chatPreviews)
        assertEquals(chatPreviews, state.filteredChats) // Should be same when no search query
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadChats sets loading state initially`() = runTest(testDispatcher) {
        // When
        chatListViewModel.handleAction(ChatListAction.LoadChats)

        // Then
        val state = chatListViewModel.state.value
        assertTrue(state.isLoading)
    }

    @Test
    fun `searchChats filters chat previews by name`() = runTest(testDispatcher) {
        // Given
        val chatPreviews = listOf(
            ChatPreview("chat1", "John Doe", "Hello", System.currentTimeMillis(), 2, true),
            ChatPreview("chat2", "Jane Smith", "Hi there", System.currentTimeMillis(), 0, false),
            ChatPreview("chat3", "Bob Johnson", "Hey", System.currentTimeMillis(), 1, true)
        )
        mockChatUseCase.chatPreviews = chatPreviews
        
        // Load chats first
        chatListViewModel.handleAction(ChatListAction.LoadChats)
        advanceUntilIdle()

        // When
        chatListViewModel.handleAction(ChatListAction.SearchChats("john"))

        // Then
        val state = chatListViewModel.state.value
        assertEquals("john", state.searchQuery)
        assertEquals(2, state.filteredChats.size) // John Doe and Bob Johnson
        assertTrue(state.filteredChats.any { it.careeName.contains("John", ignoreCase = true) })
        assertTrue(state.filteredChats.any { it.careeName.contains("Johnson", ignoreCase = true) })
    }

    @Test
    fun `searchChats filters chat previews by message content`() = runTest(testDispatcher) {
        // Given
        val chatPreviews = listOf(
            ChatPreview("chat1", "John Doe", "Hello world", System.currentTimeMillis(), 2, true),
            ChatPreview("chat2", "Jane Smith", "Hi there", System.currentTimeMillis(), 0, false),
            ChatPreview("chat3", "Bob Johnson", "Hello everyone", System.currentTimeMillis(), 1, true)
        )
        mockChatUseCase.chatPreviews = chatPreviews
        
        // Load chats first
        chatListViewModel.handleAction(ChatListAction.LoadChats)
        advanceUntilIdle()

        // When
        chatListViewModel.handleAction(ChatListAction.SearchChats("hello"))

        // Then
        val state = chatListViewModel.state.value
        assertEquals("hello", state.searchQuery)
        assertEquals(2, state.filteredChats.size) // John Doe and Bob Johnson
        assertTrue(state.filteredChats.all { it.lastMessage.contains("Hello", ignoreCase = true) })
    }

    @Test
    fun `searchChats shows all chats when query is blank`() = runTest(testDispatcher) {
        // Given
        val chatPreviews = listOf(
            ChatPreview("chat1", "John Doe", "Hello", System.currentTimeMillis(), 2, true),
            ChatPreview("chat2", "Jane Smith", "Hi there", System.currentTimeMillis(), 0, false)
        )
        mockChatUseCase.chatPreviews = chatPreviews
        
        // Load chats and set a search query first
        chatListViewModel.handleAction(ChatListAction.LoadChats)
        chatListViewModel.handleAction(ChatListAction.SearchChats("john"))
        advanceUntilIdle()

        // When - clear search
        chatListViewModel.handleAction(ChatListAction.SearchChats(""))

        // Then
        val state = chatListViewModel.state.value
        assertEquals("", state.searchQuery)
        assertEquals(chatPreviews, state.filteredChats) // Should show all chats
    }

    @Test
    fun `selectChat emits navigation effect`() = runTest(testDispatcher) {
        // Given
        val chatId = "chat123"
        var emittedEffect: ChatEffect? = null
        
        // Collect effects
        val job = backgroundScope.launch {
            chatListViewModel.effects.collect { effect ->
                emittedEffect = effect
            }
        }

        // When
        chatListViewModel.handleAction(ChatListAction.SelectChat(chatId))
        advanceUntilIdle()

        // Then
        assertTrue(emittedEffect is ChatEffect.NavigateToChat)
        assertEquals(chatId, (emittedEffect as ChatEffect.NavigateToChat).chatId)
        
        job.cancel()
    }

    @Test
    fun `refreshChats reloads chat list`() = runTest(testDispatcher) {
        // Given
        val initialChats = listOf(
            ChatPreview("chat1", "John Doe", "Hello", System.currentTimeMillis(), 2, true)
        )
        val refreshedChats = listOf(
            ChatPreview("chat1", "John Doe", "Hello", System.currentTimeMillis(), 2, true),
            ChatPreview("chat2", "Jane Smith", "New chat", System.currentTimeMillis(), 1, false)
        )
        
        mockChatUseCase.chatPreviews = initialChats
        chatListViewModel.handleAction(ChatListAction.LoadChats)
        advanceUntilIdle()
        
        // Update mock data
        mockChatUseCase.chatPreviews = refreshedChats

        // When
        chatListViewModel.handleAction(ChatListAction.RefreshChats)
        advanceUntilIdle()

        // Then
        val state = chatListViewModel.state.value
        assertEquals(refreshedChats, state.chatPreviews)
        assertEquals(refreshedChats, state.filteredChats)
    }

    @Test
    fun `clearError removes error from state`() = runTest(testDispatcher) {
        // Given - simulate an error state
        mockChatUseCase.shouldThrowError = true
        chatListViewModel.handleAction(ChatListAction.LoadChats)
        advanceUntilIdle()
        
        // Verify error is set
        assertNotNull(chatListViewModel.state.value.error)

        // When
        chatListViewModel.handleAction(ChatListAction.ClearError)

        // Then
        assertNull(chatListViewModel.state.value.error)
    }

    @Test
    fun `unread count is updated from chat previews`() = runTest(testDispatcher) {
        // Given
        val chatPreviews = listOf(
            ChatPreview("chat1", "John Doe", "Hello", System.currentTimeMillis(), 3, true),
            ChatPreview("chat2", "Jane Smith", "Hi there", System.currentTimeMillis(), 2, false),
            ChatPreview("chat3", "Bob Johnson", "Hey", System.currentTimeMillis(), 0, true)
        )
        mockChatUseCase.chatPreviews = chatPreviews
        mockChatUseCase.unreadCount = 5 // 3 + 2 + 0

        // When
        chatListViewModel.handleAction(ChatListAction.LoadChats)
        advanceUntilIdle()

        // Then
        val state = chatListViewModel.state.value
        assertEquals(5, state.unreadCount)
    }

    @Test
    fun `error handling works correctly`() = runTest(testDispatcher) {
        // Given
        mockChatUseCase.shouldThrowError = true

        // When
        chatListViewModel.handleAction(ChatListAction.LoadChats)
        advanceUntilIdle()

        // Then
        val state = chatListViewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error?.contains("Failed to load chats") == true)
    }

    @Test
    fun `search preserves existing filter when new chats are loaded`() = runTest(testDispatcher) {
        // Given
        val initialChats = listOf(
            ChatPreview("chat1", "John Doe", "Hello", System.currentTimeMillis(), 2, true),
            ChatPreview("chat2", "Jane Smith", "Hi there", System.currentTimeMillis(), 0, false)
        )
        mockChatUseCase.chatPreviews = initialChats
        
        // Load chats and set search
        chatListViewModel.handleAction(ChatListAction.LoadChats)
        chatListViewModel.handleAction(ChatListAction.SearchChats("john"))
        advanceUntilIdle()
        
        // Verify search is applied
        assertEquals(1, chatListViewModel.state.value.filteredChats.size)
        
        // Update chats with new data that includes more John matches
        val updatedChats = listOf(
            ChatPreview("chat1", "John Doe", "Hello", System.currentTimeMillis(), 2, true),
            ChatPreview("chat2", "Jane Smith", "Hi there", System.currentTimeMillis(), 0, false),
            ChatPreview("chat3", "Johnny Cash", "Music", System.currentTimeMillis(), 1, true)
        )
        mockChatUseCase.chatPreviews = updatedChats

        // When - new chats are loaded (simulating real-time updates)
        chatListViewModel.handleAction(ChatListAction.LoadChats)
        advanceUntilIdle()

        // Then - search filter should still be applied to new data
        val state = chatListViewModel.state.value
        assertEquals("john", state.searchQuery)
        assertEquals(2, state.filteredChats.size) // John Doe and Johnny Cash
        assertTrue(state.filteredChats.all { it.careeName.contains("john", ignoreCase = true) })
    }
}

// Extended MockChatUseCase for ChatListViewModel testing
class MockChatUseCaseForList : MockChatUseCase() {
    var chatPreviews = emptyList<ChatPreview>()
    var unreadCount = 0
    var shouldThrowError = false

    override suspend fun getChatList(carerId: String) = flowOf(
        if (shouldThrowError) throw Exception("Test error") else chatPreviews
    )

    override suspend fun getUnreadMessageCount(carerId: String) = flowOf(unreadCount)
}