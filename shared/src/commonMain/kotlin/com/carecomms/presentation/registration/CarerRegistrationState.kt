package com.carecomms.presentation.registration

import com.carecomms.data.models.AuthResult
import com.carecomms.data.models.DocumentUpload

data class CarerRegistrationState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val age: String = "",
    val phoneNumber: String = "",
    val location: String = "",
    val uploadedDocuments: List<DocumentUpload> = emptyList(),
    val isLoading: Boolean = false,
    val validationErrors: Map<String, String> = emptyMap(),
    val registrationError: String? = null,
    val isRegistrationSuccessful: Boolean = false,
    val authResult: AuthResult? = null
)

sealed class CarerRegistrationAction {
    data class UpdateEmail(val email: String) : CarerRegistrationAction()
    data class UpdatePassword(val password: String) : CarerRegistrationAction()
    data class UpdateConfirmPassword(val confirmPassword: String) : CarerRegistrationAction()
    data class UpdateAge(val age: String) : CarerRegistrationAction()
    data class UpdatePhoneNumber(val phoneNumber: String) : CarerRegistrationAction()
    data class UpdateLocation(val location: String) : CarerRegistrationAction()
    data class AddDocument(val document: DocumentUpload) : CarerRegistrationAction()
    data class RemoveDocument(val documentId: String) : CarerRegistrationAction()
    object SubmitRegistration : CarerRegistrationAction()
    object ClearErrors : CarerRegistrationAction()
    object ResetState : CarerRegistrationAction()
}

sealed class CarerRegistrationEffect {
    object NavigateToLogin : CarerRegistrationEffect()
    object NavigateToChatList : CarerRegistrationEffect()
    data class ShowError(val message: String) : CarerRegistrationEffect()
    data class ShowSuccess(val message: String) : CarerRegistrationEffect()
}