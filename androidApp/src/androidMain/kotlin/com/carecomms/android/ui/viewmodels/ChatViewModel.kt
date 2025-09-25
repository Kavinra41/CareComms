package com.carecomms.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carecomms.data.models.Message
import com.carecomms.data.models.Chat
import com.carecomms.data.models.MessageStatus
import com.carecomms.data.repository.ChatRepository
import com.carecomms.data.repository.UserRepository
import com.carecomms.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _chat = MutableStateFlow<Chat?>(null)
    val chat: StateFlow<Chat?> = _chat.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _newMessage = MutableStateFlow("")
    val newMessage: StateFlow<String> = _newMessage.asStateFlow()

    private val _otherUserName = MutableStateFlow("Loading...")
    val otherUserName: StateFlow<String> = _otherUserName.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private var currentUserId: String? = null
    private var otherUserId: String? = null
    private var isInitialized = false

    fun initializeChat(otherUserId: String) {
        // Prevent multiple initializations
        if (isInitialized && this.otherUserId == otherUserId) {
            return
        }
        
        this.otherUserId = otherUserId
        isInitialized = true
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Get current user
                val currentUser = authRepository.getCurrentUser()
                currentUserId = currentUser?.uid

                // Get other user's name first
                userRepository.getUser(otherUserId)
                    .onSuccess { user ->
                        _otherUserName.value = user?.name ?: "Unknown User"
                    }
                    .onFailure {
                        _otherUserName.value = "Unknown User"
                    }

                if (currentUserId != null) {
                    println("ChatViewModel: Initializing chat between $currentUserId and $otherUserId")
                    
                    // Create or get existing chat
                    chatRepository.createOrGetChat(currentUserId!!, otherUserId)
                        .onSuccess { chat ->
                            println("ChatViewModel: Chat created/retrieved with ID: ${chat.id}")
                            _chat.value = chat
                            // Start listening to messages
                            listenToMessages(chat.id)
                            // Mark messages as read
                            markMessagesAsRead(chat.id)
                        }
                        .onFailure { exception ->
                            println("ChatViewModel: Failed to create/get chat: ${exception.message}")
                            _error.value = exception.message ?: "Failed to initialize chat"
                        }
                } else {
                    _error.value = "User not authenticated"
                }
            } catch (e: Exception) {
                println("ChatViewModel: Exception in initializeChat: ${e.message}")
                _error.value = e.message ?: "Failed to initialize chat"
            }

            _isLoading.value = false
        }
    }

    private fun listenToMessages(chatId: String) {
        viewModelScope.launch {
            println("ChatViewModel: Starting to listen to messages for chat: $chatId")
            chatRepository.getMessagesFlow(chatId).collect { messageList ->
                println("ChatViewModel: Received ${messageList.size} messages")
                _messages.value = messageList
            }
        }
    }

    fun updateNewMessage(message: String) {
        _newMessage.value = message
    }

    fun sendMessage() {
        val messageText = _newMessage.value.trim()
        if (messageText.isBlank() || currentUserId == null || _chat.value == null || _isSending.value) {
            return
        }

        viewModelScope.launch {
            _isSending.value = true
            
            try {
                val currentUser = authRepository.getCurrentUser()
                val message = Message(
                    chatId = _chat.value!!.id,
                    senderId = currentUserId!!,
                    senderName = currentUser?.name ?: "Unknown",
                    content = messageText,
                    timestamp = System.currentTimeMillis(),
                    status = MessageStatus.SENT
                )

                println("ChatViewModel: Sending message: $messageText")
                
                // Clear the message immediately to prevent duplicate sends
                _newMessage.value = ""
                
                chatRepository.sendMessage(message)
                    .onSuccess {
                        println("ChatViewModel: Message sent successfully")
                    }
                    .onFailure { exception ->
                        println("ChatViewModel: Failed to send message: ${exception.message}")
                        _error.value = exception.message ?: "Failed to send message"
                        // Restore the message text if sending failed
                        _newMessage.value = messageText
                    }
            } catch (e: Exception) {
                println("ChatViewModel: Exception in sendMessage: ${e.message}")
                _error.value = e.message ?: "Failed to send message"
                // Restore the message text if sending failed
                _newMessage.value = messageText
            } finally {
                _isSending.value = false
            }
        }
    }

    private fun markMessagesAsRead(chatId: String) {
        if (currentUserId == null) return
        
        viewModelScope.launch {
            chatRepository.markMessagesAsRead(chatId, currentUserId!!)
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun getOtherUserName(): String {
        return _otherUserName.value
    }

    fun isMessageFromCurrentUser(message: Message): Boolean {
        return message.senderId == currentUserId
    }
}