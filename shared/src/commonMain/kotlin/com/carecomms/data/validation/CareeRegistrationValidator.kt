package com.carecomms.data.validation

import com.carecomms.data.models.CareeRegistrationData
import com.carecomms.data.models.PersonalDetails

sealed class CareeValidationError {
    object InvalidEmail : CareeValidationError()
    object WeakPassword : CareeValidationError()
    object EmptyHealthInfo : CareeValidationError()
    object EmptyFirstName : CareeValidationError()
    object EmptyLastName : CareeValidationError()
    object InvalidDateOfBirth : CareeValidationError()
    data class CustomError(val message: String) : CareeValidationError()
}

data class CareeValidationResult(
    val isValid: Boolean,
    val errors: List<CareeValidationError> = emptyList()
)

class CareeRegistrationValidator {
    
    fun validate(data: CareeRegistrationData): CareeValidationResult {
        val errors = mutableListOf<CareeValidationError>()
        
        // Email validation
        if (!isValidEmail(data.email)) {
            errors.add(CareeValidationError.InvalidEmail)
        }
        
        // Password validation
        if (!isValidPassword(data.password)) {
            errors.add(CareeValidationError.WeakPassword)
        }
        
        // Health info validation
        if (data.healthInfo.isBlank()) {
            errors.add(CareeValidationError.EmptyHealthInfo)
        }
        
        // Personal details validation
        errors.addAll(validatePersonalDetails(data.basicDetails))
        
        return CareeValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    private fun validatePersonalDetails(details: PersonalDetails): List<CareeValidationError> {
        val errors = mutableListOf<CareeValidationError>()
        
        if (details.firstName.isBlank()) {
            errors.add(CareeValidationError.EmptyFirstName)
        }
        
        if (details.lastName.isBlank()) {
            errors.add(CareeValidationError.EmptyLastName)
        }
        
        if (!isValidDateOfBirth(details.dateOfBirth)) {
            errors.add(CareeValidationError.InvalidDateOfBirth)
        }
        
        return errors
    }
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.isNotBlank() && emailRegex.matches(email)
    }
    
    private fun isValidPassword(password: String): Boolean {
        // Password must be at least 6 characters for carees (simpler than carers)
        return password.length >= 6
    }
    
    private fun isValidDateOfBirth(dateOfBirth: String): Boolean {
        // Basic date format validation (YYYY-MM-DD)
        val dateRegex = "^\\d{4}-\\d{2}-\\d{2}$".toRegex()
        return dateOfBirth.isNotBlank() && dateRegex.matches(dateOfBirth)
    }
}