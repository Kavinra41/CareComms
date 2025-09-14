package com.carecomms.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carecomms.domain.usecase.ChatUseCase
import com.carecomms.domain.usecase.InvitationUseCase
import com.carecomms.presentation.chat.ChatListAction
import com.carecomms.presentation.chat.ChatListState
import com.carecomms.presentation.chat.ChatListViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatListScreenViewModel(
    private val chatUseCase: ChatUseCase,
    private val invitationUseCase: InvitationUseCase,
    private val carerId: String
) : ViewModel() {
    
    private val chatListViewModel = ChatListViewModel(chatUseCase, carerId)
    
    val state: StateFlow<ChatListState> = chatListViewModel.state
    val effects = chatListViewModel.effects
    
    // Invitation state
    private val _invitationState = MutableStateFlow(InvitationDialogState())
    val invitationState: StateFlow<InvitationDialogState> = _invitationState.asStateFlow()
    
    fun handleAction(action: ChatListAction) {
        chatListViewModel.handleAction(action)
    }
    
    fun generateInvitationLink() {
        viewModelScope.launch {
            _invitationState.update { it.copy(isLoading = true, error = null, invitationUrl = "") }
            
            try {
                val result = invitationUseCase.generateInvitationLink(carerId)
                result.fold(
                    onSuccess = { url ->
                        _invitationState.update { 
                            it.copy(
                                isLoading = false, 
                                invitationUrl = url,
                                error = null
                            ) 
                        }
                    },
                    onFailure = { exception ->
                        _invitationState.update { 
                            it.copy(
                                isLoading = false, 
                                error = exception.message ?: "Failed to generate invitation link"
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                _invitationState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "An unexpected error occurred"
                    ) 
                }
            }
        }
    }
    
    fun dismissInvitationDialog() {
        _invitationState.update { InvitationDialogState() }
    }
    
    fun retryInvitationGeneration() {
        generateInvitationLink()
    }
    
    override fun onCleared() {
        super.onCleared()
        chatListViewModel.onCleared()
    }
}

data class InvitationDialogState(
    val isLoading: Boolean = false,
    val invitationUrl: String = "",
    val error: String? = null,
    val isVisible: Boolean = false
) {
    fun show(): InvitationDialogState = copy(isVisible = true)
    fun hide(): InvitationDialogState = copy(isVisible = false)
}