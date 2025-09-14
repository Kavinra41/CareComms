package com.carecomms.presentation.registration

import com.carecomms.data.models.CarerInfo
import com.carecomms.data.models.PersonalDetails
import com.carecomms.data.validation.CareeValidationError

data class CareeRegistrationState(
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val healthInfo: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirth: String = "",
    val address: String = "",
    val emergencyContact: String = "",
    val invitationToken: String = "",
    val carerInfo: CarerInfo? = null,
    val validationErrors: List<CareeValidationError> = emptyList(),
    val isRegistrationSuccessful: Boolean = false,
    val errorMessage: String? = null,
    val isInvitationValid: Boolean = false,
    val isValidatingInvitation: Boolean = false
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && 
                password.isNotBlank() && 
                confirmPassword == password &&
                healthInfo.isNotBlank() &&
                firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                dateOfBirth.isNotBlank() &&
                validationErrors.isEmpty() &&
                isInvitationValid
    
    fun toPersonalDetails(): PersonalDetails {
        return PersonalDetails(
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = dateOfBirth,
            address = address.takeIf { it.isNotBlank() },
            emergencyContact = emergencyContact.takeIf { it.isNotBlank() }
        )
    }
}

sealed class CareeRegistrationIntent {
    data class UpdateEmail(val email: String) : CareeRegistrationIntent()
    data class UpdatePassword(val password: String) : CareeRegistrationIntent()
    data class UpdateConfirmPassword(val confirmPassword: String) : CareeRegistrationIntent()
    data class UpdateHealthInfo(val healthInfo: String) : CareeRegistrationIntent()
    data class UpdateFirstName(val firstName: String) : CareeRegistrationIntent()
    data class UpdateLastName(val lastName: String) : CareeRegistrationIntent()
    data class UpdateDateOfBirth(val dateOfBirth: String) : CareeRegistrationIntent()
    data class UpdateAddress(val address: String) : CareeRegistrationIntent()
    data class UpdateEmergencyContact(val emergencyContact: String) : CareeRegistrationIntent()
    data class ValidateInvitation(val token: String) : CareeRegistrationIntent()
    object RegisterCaree : CareeRegistrationIntent()
    object ClearError : CareeRegistrationIntent()
}