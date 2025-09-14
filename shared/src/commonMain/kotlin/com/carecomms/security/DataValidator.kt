package com.carecomms.security

import com.carecomms.data.models.CareeRegistrationData
import com.carecomms.data.models.CarerRegistrationData

/**
 * Validates and sanitizes user input data
 */
object DataValidator {
    
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")
    private val phoneRegex = Regex("^[+]?[1-9]\\d{1,14}$")
    private val sqlInjectionPatterns = listOf(
        "('|(\\-\\-)|(;)|(\\|)|(\\*)|(%))",
        "(union|select|insert|delete|update|drop|create|alter|exec|execute)",
        "(script|javascript|vbscript|onload|onerror|onclick)"
    )
    
    fun validateEmail(email: String): ValidationResult {
        val sanitized = EncryptionUtils.sanitizeUserInput(email.lowercase())
        
        return when {
            sanitized.isEmpty() -> ValidationResult.Invalid("Email cannot be empty")
            sanitized.length > 254 -> ValidationResult.Invalid("Email too long")
            !emailRegex.matches(sanitized) -> ValidationResult.Invalid("Invalid email format")
            containsSqlInjection(sanitized) -> ValidationResult.Invalid("Invalid characters in email")
            else -> ValidationResult.Valid(sanitized)
        }
    }
    
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult.Invalid("Password cannot be empty")
            password.length < 8 -> ValidationResult.Invalid("Password must be at least 8 characters")
            password.length > 128 -> ValidationResult.Invalid("Password too long")
            !password.any { it.isUpperCase() } -> ValidationResult.Invalid("Password must contain uppercase letter")
            !password.any { it.isLowerCase() } -> ValidationResult.Invalid("Password must contain lowercase letter")
            !password.any { it.isDigit() } -> ValidationResult.Invalid("Password must contain number")
            !password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) } -> ValidationResult.Invalid("Password must contain special character")
            containsSqlInjection(password) -> ValidationResult.Invalid("Password contains invalid characters")
            else -> ValidationResult.Valid(password)
        }
    }
    
    fun validatePhoneNumber(phone: String): ValidationResult {
        val sanitized = phone.replace(Regex("[\\s\\-\\(\\)]"), "")
        
        return when {
            sanitized.isEmpty() -> ValidationResult.Invalid("Phone number cannot be empty")
            !phoneRegex.matches(sanitized) -> ValidationResult.Invalid("Invalid phone number format")
            containsSqlInjection(sanitized) -> ValidationResult.Invalid("Invalid characters in phone number")
            else -> ValidationResult.Valid(sanitized)
        }
    }
    
    fun validateAge(age: Int): ValidationResult {
        return when {
            age < 18 -> ValidationResult.Invalid("Age must be at least 18")
            age > 120 -> ValidationResult.Invalid("Age must be less than 120")
            else -> ValidationResult.Valid(age.toString())
        }
    }
    
    fun validateLocation(location: String): ValidationResult {
        val sanitized = EncryptionUtils.sanitizeUserInput(location)
        
        return when {
            sanitized.isEmpty() -> ValidationResult.Invalid("Location cannot be empty")
            sanitized.length > 200 -> ValidationResult.Invalid("Location too long")
            containsSqlInjection(sanitized) -> ValidationResult.Invalid("Invalid characters in location")
            else -> ValidationResult.Valid(sanitized)
        }
    }
    
    fun validateHealthInfo(healthInfo: String): ValidationResult {
        val sanitized = EncryptionUtils.sanitizeHealthData(healthInfo)
        
        return when {
            sanitized.length > 1000 -> ValidationResult.Invalid("Health information too long")
            containsSqlInjection(sanitized) -> ValidationResult.Invalid("Invalid characters in health information")
            else -> ValidationResult.Valid(sanitized)
        }
    }
    
    fun validateCarerRegistration(data: CarerRegistrationData): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        
        when (val emailResult = validateEmail(data.email)) {
            is ValidationResult.Invalid -> errors.add(ValidationError("email", emailResult.message))
            else -> {}
        }
        
        when (val passwordResult = validatePassword(data.password)) {
            is ValidationResult.Invalid -> errors.add(ValidationError("password", passwordResult.message))
            else -> {}
        }
        
        when (val phoneResult = validatePhoneNumber(data.phoneNumber)) {
            is ValidationResult.Invalid -> errors.add(ValidationError("phoneNumber", phoneResult.message))
            else -> {}
        }
        
        when (val ageResult = validateAge(data.age)) {
            is ValidationResult.Invalid -> errors.add(ValidationError("age", ageResult.message))
            else -> {}
        }
        
        when (val locationResult = validateLocation(data.location)) {
            is ValidationResult.Invalid -> errors.add(ValidationError("location", locationResult.message))
            else -> {}
        }
        
        return errors
    }
    
    fun validateCareeRegistration(data: CareeRegistrationData): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        
        when (val emailResult = validateEmail(data.email)) {
            is ValidationResult.Invalid -> errors.add(ValidationError("email", emailResult.message))
            else -> {}
        }
        
        when (val passwordResult = validatePassword(data.password)) {
            is ValidationResult.Invalid -> errors.add(ValidationError("password", passwordResult.message))
            else -> {}
        }
        
        when (val healthResult = validateHealthInfo(data.healthInfo)) {
            is ValidationResult.Invalid -> errors.add(ValidationError("healthInfo", healthResult.message))
            else -> {}
        }
        
        return errors
    }
    
    private fun containsSqlInjection(input: String): Boolean {
        val lowercaseInput = input.lowercase()
        return sqlInjectionPatterns.any { pattern ->
            Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(lowercaseInput)
        }
    }
}

sealed class ValidationResult {
    data class Valid(val value: String) : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

data class ValidationError(
    val field: String,
    val message: String
)