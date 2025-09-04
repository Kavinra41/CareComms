package com.carecomms.presentation.invitation

import com.carecomms.data.utils.DeepLinkHandler
import com.carecomms.domain.usecase.InvitationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InvitationViewModel(
    private val invitationUseCase: InvitationUseCase,
    private val deepLinkHandler: DeepLinkHandler,
    private val coroutineScope: CoroutineScope
) {
    
    private val _state = MutableStateFlow(InvitationState())
    val state: StateFlow<InvitationState> = _state.asStateFlow()
    
    private val _effects = MutableSharedFlow<InvitationEffect>()
    val effects: SharedFlow<InvitationEffect> = _effects.asSharedFlow()
    
    fun handleIntent(intent: InvitationIntent) {
        when (intent) {
            is InvitationIntent.GenerateInvitationLink -> generateInvitationLink(intent.carerId)
            is InvitationIntent.ValidateInvitationFromToken -> validateInvitationFromToken(intent.token)
            is InvitationIntent.ValidateInvitationFromUrl -> validateInvitationFromUrl(intent.invitationUrl)
            is InvitationIntent.AcceptInvitation -> acceptInvitation(intent.token, intent.careeId)
            is InvitationIntent.LoadActiveInvitations -> loadActiveInvitations(intent.carerId)
            is InvitationIntent.RevokeInvitation -> revokeInvitation(intent.token)
            is InvitationIntent.ClearError -> clearError()
            is InvitationIntent.ClearGeneratedUrl -> clearGeneratedUrl()
            is InvitationIntent.ResetAcceptedState -> resetAcceptedState()
        }
    }
    
    private fun generateInvitationLink(carerId: String) {
        coroutineScope.launch {
            _state.value = _state.value.copy(isGeneratingLink = true, error = null)
            
            invitationUseCase.generateInvitationLink(carerId)
                .onSuccess { url ->
                    _state.value = _state.value.copy(
                        isGeneratingLink = false,
                        generatedInvitationUrl = url
                    )
                    _effects.emit(InvitationEffect.ShareInvitationUrl(url))
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isGeneratingLink = false,
                        error = error.message ?: "Failed to generate invitation link"
                    )
                    _effects.emit(InvitationEffect.ShowError(error.message ?: "Failed to generate invitation link"))
                }
        }
    }
    
    private fun validateInvitationFromToken(token: String) {
        coroutineScope.launch {
            _state.value = _state.value.copy(isValidatingInvitation = true, error = null)
            
            invitationUseCase.validateInvitationFromToken(token)
                .onSuccess { invitationData ->
                    _state.value = _state.value.copy(
                        isValidatingInvitation = false,
                        invitationData = invitationData
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isValidatingInvitation = false,
                        error = error.message ?: "Invalid or expired invitation"
                    )
                    _effects.emit(InvitationEffect.ShowError(error.message ?: "Invalid or expired invitation"))
                }
        }
    }
    
    private fun validateInvitationFromUrl(invitationUrl: String) {
        coroutineScope.launch {
            _state.value = _state.value.copy(isValidatingInvitation = true, error = null)
            
            invitationUseCase.validateInvitationFromUrl(invitationUrl)
                .onSuccess { invitationData ->
                    _state.value = _state.value.copy(
                        isValidatingInvitation = false,
                        invitationData = invitationData
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isValidatingInvitation = false,
                        error = error.message ?: "Invalid or expired invitation"
                    )
                    _effects.emit(InvitationEffect.ShowError(error.message ?: "Invalid or expired invitation"))
                }
        }
    }
    
    private fun acceptInvitation(token: String, careeId: String) {
        coroutineScope.launch {
            _state.value = _state.value.copy(isAcceptingInvitation = true, error = null)
            
            invitationUseCase.acceptInvitation(token, careeId)
                .onSuccess {
                    _state.value = _state.value.copy(
                        isAcceptingInvitation = false,
                        invitationAccepted = true
                    )
                    _effects.emit(InvitationEffect.InvitationAcceptedSuccessfully)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isAcceptingInvitation = false,
                        error = error.message ?: "Failed to accept invitation"
                    )
                    _effects.emit(InvitationEffect.ShowError(error.message ?: "Failed to accept invitation"))
                }
        }
    }
    
    private fun loadActiveInvitations(carerId: String) {
        coroutineScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            invitationUseCase.getActiveInvitations(carerId)
                .onSuccess { invitations ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        activeInvitations = invitations
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load invitations"
                    )
                    _effects.emit(InvitationEffect.ShowError(error.message ?: "Failed to load invitations"))
                }
        }
    }
    
    private fun revokeInvitation(token: String) {
        coroutineScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            invitationUseCase.revokeInvitation(token)
                .onSuccess {
                    // Reload active invitations to reflect the change
                    val currentState = _state.value
                    val updatedInvitations = currentState.activeInvitations.filter { it.token != token }
                    _state.value = currentState.copy(
                        isLoading = false,
                        activeInvitations = updatedInvitations
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to revoke invitation"
                    )
                    _effects.emit(InvitationEffect.ShowError(error.message ?: "Failed to revoke invitation"))
                }
        }
    }
    
    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    private fun clearGeneratedUrl() {
        _state.value = _state.value.copy(generatedInvitationUrl = null)
    }
    
    private fun resetAcceptedState() {
        _state.value = _state.value.copy(invitationAccepted = false)
    }
    
    /**
     * Handles deep link URLs and validates invitation
     */
    fun handleDeepLink(url: String) {
        if (invitationUseCase.isValidInvitationUrl(url)) {
            handleIntent(InvitationIntent.ValidateInvitationFromUrl(url))
        }
    }
    
    /**
     * Extracts token from invitation URL
     */
    fun extractTokenFromUrl(url: String): String? {
        return invitationUseCase.extractTokenFromUrl(url)
    }
    
    /**
     * Checks if URL is a valid invitation deep link
     */
    fun isValidInvitationUrl(url: String): Boolean {
        return invitationUseCase.isValidInvitationUrl(url)
    }
    
    /**
     * Generates share text for invitation
     */
    fun generateShareText(carerName: String, invitationUrl: String): String {
        return "Hi! $carerName has invited you to join CareComms for better care coordination. Click the link to get started: $invitationUrl"
    }
    
    /**
     * Generates share subject for invitation
     */
    fun generateShareSubject(): String {
        return "CareComms Invitation"
    }
}