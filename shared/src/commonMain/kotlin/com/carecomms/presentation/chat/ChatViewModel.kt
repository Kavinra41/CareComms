package com.carecomms.presentation.chat

import com.carecomms.data.models.MessageType
import com.carecomms.domain.usecase.ChatUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ChatViewModel(
    private val chatUseCase: ChatUseCase,
    private val currentUserId: String
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()
    
    private val _effects = MutableSharedFlow<ChatEffect>()
    val effects: SharedFlow<ChatEffect> = _effects.asSharedFlow()

    private var typingJob: Job? = null

    fun handleAction(action: ChatAction) {
        when (action) {
            is ChatAction.LoadMessages -> loadMessages(action.chatId)
            is ChatAction.SendMessage -> sendMessage(action.content)
            is ChatAction.UpdateCurrentMessage -> updateCurrentMessage(action.message)
            is ChatAction.SetTypingStatus -> setTypingStatus(action.isTyping)
            is ChatAction.MarkMessageAsRead -> markMessageAsRead(action.messageId)
            is ChatAction.MarkAllAsRead -> markAllAsRead()
            is ChatAction.ClearError -> clearError()
            is ChatAction.RefreshMessages -> refreshMessages()
        }
    }

    private fun loadMessages(chatId: String) {
        viewModelScope.launch {
            _state.update { 
                it.copy(
                    isLoading = true, 
                    error = null, 
                    chatId = chatId
                ) 
            }
            
            try {
                // Load messages
                launch {
                    chatUseCase.getMessages(chatId).collect { messages ->
                        _state.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                messages = messages
                            )
                        }
                    }
                }
                
                // Load typing status
                launch {
                    chatUseCase.getTypingStatus(chatId).collect { typingStatus ->
                        _state.update { currentState ->
                            currentState.copy(
                                otherUserTyping = if (typingStatus.userId != currentUserId && typingStatus.isTyping) {
                                    typingStatus
                                } else {
                                    null
                                }
                            )
                        }
                    }
                }
                
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Failed to load messages: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun sendMessage(content: String) {
        val currentState = _state.value
        if (currentState.isSendingMessage || currentState.chatId.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isSendingMessage = true, error = null) }
            
            try {
                val result = chatUseCase.sendMessage(
                    chatId = currentState.chatId,
                    senderId = currentUserId,
                    content = content,
                    type = MessageType.TEXT
                )
                
                if (result.isSuccess) {
                    _state.update { 
                        it.copy(
                            currentMessage = "",
                            isSendingMessage = false
                        ) 
                    }
                    _effects.emit(ChatEffect.MessageSent)
                } else {
                    _state.update { 
                        it.copy(
                            isSendingMessage = false,
                            error = "Failed to send message: ${result.exceptionOrNull()?.message}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isSendingMessage = false,
                        error = "Failed to send message: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun updateCurrentMessage(message: String) {
        _state.update { it.copy(currentMessage = message) }
        
        // Handle typing indicator
        if (message.isNotBlank() && !_state.value.isTyping) {
            setTypingStatus(true)
            
            // Auto-stop typing after 3 seconds of no input
            typingJob?.cancel()
            typingJob = viewModelScope.launch {
                delay(3000)
                setTypingStatus(false)
            }
        } else if (message.isBlank() && _state.value.isTyping) {
            setTypingStatus(false)
        }
    }

    private fun setTypingStatus(isTyping: Boolean) {
        val currentState = _state.value
        if (currentState.chatId.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isTyping = isTyping) }
            
            try {
                chatUseCase.setTypingStatus(currentState.chatId, isTyping)
            } catch (e: Exception) {
                // Typing status is not critical, so we don't show errors for this
            }
        }
    }

    private fun markMessageAsRead(messageId: String) {
        val currentState = _state.value
        if (currentState.chatId.isEmpty()) return

        viewModelScope.launch {
            try {
                val result = chatUseCase.markMessageAsRead(currentState.chatId, messageId)
                if (result.isSuccess) {
                    _effects.emit(ChatEffect.MessagesMarkedAsRead)
                }
            } catch (e: Exception) {
                // Read status is not critical, so we don't show errors for this
            }
        }
    }

    private fun markAllAsRead() {
        val currentState = _state.value
        if (currentState.chatId.isEmpty()) return

        viewModelScope.launch {
            try {
                val result = chatUseCase.markAllMessagesAsRead(currentState.chatId)
                if (result.isSuccess) {
                    _effects.emit(ChatEffect.MessagesMarkedAsRead)
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = "Failed to mark messages as read: ${e.message}") 
                }
            }
        }
    }

    private fun refreshMessages() {
        val currentState = _state.value
        if (currentState.chatId.isNotEmpty()) {
            loadMessages(currentState.chatId)
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun onCleared() {
        typingJob?.cancel()
        viewModelScope.launch {
            // Stop typing indicator when leaving chat
            if (_state.value.isTyping) {
                setTypingStatus(false)
            }
        }
    }
}