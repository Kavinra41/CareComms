package com.carecomms.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carecomms.data.models.ChatPreview
import com.carecomms.data.repository.AuthRepository
import com.carecomms.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _chatPreviews = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chatPreviews: StateFlow<List<ChatPreview>> = _chatPreviews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadChatPreviews()
    }

    private fun loadChatPreviews() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    println("ChatListViewModel: Loading chat previews for user: ${currentUser.uid}")
                    chatRepository.getChatPreviews(currentUser.uid).collect { previews ->
                        println("ChatListViewModel: Received ${previews.size} chat previews")
                        previews.forEach { preview ->
                            println("ChatListViewModel: Preview - ${preview.otherUserName} (${preview.otherUserId}): ${preview.lastMessage}")
                        }
                        _chatPreviews.value = previews
                        _isLoading.value = false
                    }
                } else {
                    println("ChatListViewModel: User not authenticated")
                    _error.value = "User not authenticated"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("PERMISSION_DENIED") == true -> 
                        "Unable to access chat data. Please check your connection and try again."
                    e.message?.contains("UNAUTHENTICATED") == true -> 
                        "Please sign in to view your chats."
                    else -> e.message ?: "Failed to load chats"
                }
                _error.value = errorMessage
                _isLoading.value = false
                println("ChatListViewModel: Error loading chats: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun refreshChats() {
        loadChatPreviews()
    }
}