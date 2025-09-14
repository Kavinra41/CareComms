package com.carecomms.android.ui.viewmodels

import com.carecomms.domain.usecase.ChatUseCase
import com.carecomms.presentation.chat.ChatViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChatScreenViewModel(
    chatId: String,
    currentUserId: String
) : KoinComponent {
    
    private val chatUseCase: ChatUseCase by inject()
    
    private val chatViewModel = ChatViewModel(
        chatUseCase = chatUseCase,
        currentUserId = currentUserId
    )
    
    val state = chatViewModel.state
    val effects = chatViewModel.effects
    
    fun handleAction(action: com.carecomms.presentation.chat.ChatAction) {
        chatViewModel.handleAction(action)
    }
    
    fun onCleared() {
        chatViewModel.onCleared()
    }
}