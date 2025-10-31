package com.carecomms.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carecomms.data.models.EmailInvitation
import com.carecomms.data.repository.AuthRepository
import com.carecomms.data.repository.InvitationRepository
import com.carecomms.utils.CodeGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InvitationViewModel(
    private val invitationRepository: InvitationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvitationUiState())
    val uiState: StateFlow<InvitationUiState> = _uiState.asStateFlow()

    private val _invitationCode = MutableStateFlow<String?>(null)
    val invitationCode: StateFlow<String?> = _invitationCode.asStateFlow()

    init {
        generateInvitationCode()
    }

    fun generateInvitationCode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                invitationRepository.generateCarerCode(currentUser.uid).fold(
                    onSuccess = { code ->
                        _invitationCode.value = code
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to generate invitation code: ${error.message}"
                        )
                    }
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not authenticated"
                )
            }
        }
    }

    fun sendInvitation(recipientEmail: String) {
        if (recipientEmail.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid email address")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(recipientEmail).matches()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true, error = null)
            
            val currentUser = authRepository.getCurrentUser()
            val code = _invitationCode.value
            
            if (currentUser != null && code != null) {
                val emailInvitation = EmailInvitation(
                    recipientEmail = recipientEmail,
                    carerName = currentUser.name,
                    invitationCode = code,
                    deepLink = CodeGenerator.generateDeepLink(code)
                )

                invitationRepository.sendEmailInvitation(emailInvitation).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isSending = false,
                            success = "Invitation sent successfully to $recipientEmail"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isSending = false,
                            error = "Failed to send invitation: ${error.message}"
                        )
                    }
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    error = "Unable to send invitation. Please try again."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(success = null)
    }
}

data class InvitationUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val success: String? = null
)