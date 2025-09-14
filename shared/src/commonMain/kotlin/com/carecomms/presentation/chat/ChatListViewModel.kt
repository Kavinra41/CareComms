package com.carecomms.presentation.chat

import com.carecomms.domain.usecase.ChatUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val chatUseCase: ChatUseCase,
    private val carerId: String
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _state = MutableStateFlow(ChatListState())
    val state: StateFlow<ChatListState> = _state.asStateFlow()
    
    private val _effects = MutableSharedFlow<ChatEffect>()
    val effects: SharedFlow<ChatEffect> = _effects.asSharedFlow()

    init {
        loadChats()
        observeUnreadCount()
    }

    fun handleAction(action: ChatListAction) {
        when (action) {
            is ChatListAction.LoadChats -> loadChats()
            is ChatListAction.SearchChats -> searchChats(action.query)
            is ChatListAction.SelectChat -> selectChat(action.chatId)
            is ChatListAction.RefreshChats -> refreshChats()
            is ChatListAction.ClearError -> clearError()
        }
    }

    private fun loadChats() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                chatUseCase.getChatList(carerId).collect { chatPreviews ->
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            chatPreviews = chatPreviews,
                            filteredChats = if (currentState.searchQuery.isBlank()) {
                                chatPreviews
                            } else {
                                chatPreviews.filter { preview ->
                                    preview.careeName.contains(currentState.searchQuery, ignoreCase = true) ||
                                    preview.lastMessage.contains(currentState.searchQuery, ignoreCase = true)
                                }
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Failed to load chats: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun searchChats(query: String) {
        _state.update { currentState ->
            val filteredChats = if (query.isBlank()) {
                currentState.chatPreviews
            } else {
                currentState.chatPreviews.filter { preview ->
                    preview.careeName.contains(query, ignoreCase = true) ||
                    preview.lastMessage.contains(query, ignoreCase = true)
                }
            }
            
            currentState.copy(
                searchQuery = query,
                filteredChats = filteredChats
            )
        }
    }

    private fun selectChat(chatId: String) {
        viewModelScope.launch {
            _effects.emit(ChatEffect.NavigateToChat(chatId))
        }
    }

    private fun refreshChats() {
        loadChats()
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun observeUnreadCount() {
        viewModelScope.launch {
            chatUseCase.getUnreadMessageCount(carerId).collect { count ->
                _state.update { it.copy(unreadCount = count) }
            }
        }
    }

    fun onCleared() {
        // Clean up resources when ViewModel is cleared
        viewModelScope.launch {
            // Cancel any ongoing operations
        }
    }
}