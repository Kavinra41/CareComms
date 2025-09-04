package com.carecomms.data.validation

import com.carecomms.data.models.CarerRegistrationData

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList()
)

sealed class ValidationError {
    object InvalidEmail : ValidationError()
    object WeakPassword : ValidationError()
    object InvalidAge : ValidationError()
    object InvalidPhoneNumber : ValidationError()
    object EmptyLocation : ValidationError()
    object NoDocuments : ValidationError()
    data class CustomError(val message: String) : ValidationError()
}

class CarerRegistrationValidator {
    
    fun validate(data: CarerRegistrationData): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Email validation
        if (!isValidEmail(data.email)) {
            errors.add(ValidationError.InvalidEmail)
        }
        
        // Password validation
        if (!isValidPassword(data.password)) {
            errors.add(ValidationError.WeakPassword)
        }
        
        // Age validation
        if (!isValidAge(data.age)) {
            errors.add(ValidationError.InvalidAge)
        }
        
        // Phone number validation
        if (!isValidPhoneNumber(data.phoneNumber)) {
            errors.add(ValidationError.InvalidPhoneNumber)
        }
        
        // Location validation
        if (data.location.isBlank()) {
            errors.add(ValidationError.EmptyLocation)
        }
        
        // Documents validation
        if (data.documents.isEmpty()) {
            errors.add(ValidationError.NoDocuments)
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.isNotBlank() && emailRegex.matches(email)
    }
    
    private fun isValidPassword(password: String): Boolean {
        // Password must be at least 8 characters long and contain at least one letter and one number
        return password.length >= 8 && 
               password.any { it.isLetter() } && 
               password.any { it.isDigit() }
    }
    
    private fun isValidAge(age: Int): Boolean {
        // Age must be between 18 and 100 for professional carers
        return age in 18..100
    }
    
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Basic phone number validation - at least 10 digits
        val digitsOnly = phoneNumber.filter { it.isDigit() }
        return digitsOnly.length >= 10
    }
}