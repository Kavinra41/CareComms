package com.carecomms.domain.usecase

import com.carecomms.data.models.AuthResult
import com.carecomms.data.models.CarerRegistrationData
import com.carecomms.data.repository.AuthRepository
import com.carecomms.data.validation.CarerRegistrationValidator
import com.carecomms.data.validation.ValidationError
import com.carecomms.data.validation.ValidationResult

class CarerRegistrationUseCase(
    private val authRepository: AuthRepository,
    private val validator: CarerRegistrationValidator = CarerRegistrationValidator()
) {
    
    suspend fun execute(registrationData: CarerRegistrationData): Result<AuthResult> {
        // Validate the registration data
        val validationResult = validator.validate(registrationData)
        
        if (!validationResult.isValid) {
            val errorMessage = buildValidationErrorMessage(validationResult.errors)
            return Result.failure(Exception(errorMessage))
        }
        
        // Attempt to register the carer
        return try {
            authRepository.signUpCarer(registrationData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun buildValidationErrorMessage(errors: List<ValidationError>): String {
        return errors.joinToString("; ") { error ->
            when (error) {
                is ValidationError.InvalidEmail -> "Please enter a valid email address"
                is ValidationError.WeakPassword -> "Password must be at least 8 characters with letters and numbers"
                is ValidationError.InvalidAge -> "Age must be between 18 and 100"
                is ValidationError.InvalidPhoneNumber -> "Please enter a valid phone number"
                is ValidationError.EmptyLocation -> "Location is required"
                is ValidationError.NoDocuments -> "At least one professional document is required"
                is ValidationError.CustomError -> error.message
            }
        }
    }
}