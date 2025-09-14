package com.carecomms.data.validation

import com.carecomms.data.models.CareeRegistrationData
import com.carecomms.data.models.PersonalDetails
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CareeRegistrationValidatorTest {
    
    private val validator = CareeRegistrationValidator()
    
    @Test
    fun `validate should return success for valid caree registration data`() {
        val validData = CareeRegistrationData(
            email = "caree@example.com",
            password = "password123",
            healthInfo = "No known allergies. Takes medication for blood pressure.",
            basicDetails = PersonalDetails(
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = "1950-05-15",
                address = "123 Main St",
                emergencyContact = "Jane Doe - 555-0123"
            )
        )
        
        val result = validator.validate(validData)
        
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }
    
    @Test
    fun `validate should fail for invalid email`() {
        val invalidData = CareeRegistrationData(
            email = "invalid-email",
            password = "password123",
            healthInfo = "Health info",
            basicDetails = PersonalDetails(
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = "1950-05-15"
            )
        )
        
        val result = validator.validate(invalidData)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(CareeValidationError.InvalidEmail))
    }
    
    @Test
    fun `validate should fail for weak password`() {
        val invalidData = CareeRegistrationData(
            email = "caree@example.com",
            password = "123", // Too short
            healthInfo = "Health info",
            basicDetails = PersonalDetails(
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = "1950-05-15"
            )
        )
        
        val result = validator.validate(invalidData)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(CareeValidationError.WeakPassword))
    }
    
    @Test
    fun `validate should fail for empty health info`() {
        val invalidData = CareeRegistrationData(
            email = "caree@example.com",
            password = "password123",
            healthInfo = "", // Empty
            basicDetails = PersonalDetails(
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = "1950-05-15"
            )
        )
        
        val result = validator.validate(invalidData)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(CareeValidationError.EmptyHealthInfo))
    }
    
    @Test
    fun `validate should fail for empty first name`() {
        val invalidData = CareeRegistrationData(
            email = "caree@example.com",
            password = "password123",
            healthInfo = "Health info",
            basicDetails = PersonalDetails(
                firstName = "", // Empty
                lastName = "Doe",
                dateOfBirth = "1950-05-15"
            )
        )
        
        val result = validator.validate(invalidData)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(CareeValidationError.EmptyFirstName))
    }
    
    @Test
    fun `validate should fail for empty last name`() {
        val invalidData = CareeRegistrationData(
            email = "caree@example.com",
            password = "password123",
            healthInfo = "Health info",
            basicDetails = PersonalDetails(
                firstName = "John",
                lastName = "", // Empty
                dateOfBirth = "1950-05-15"
            )
        )
        
        val result = validator.validate(invalidData)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(CareeValidationError.EmptyLastName))
    }
    
    @Test
    fun `validate should fail for invalid date of birth format`() {
        val invalidData = CareeRegistrationData(
            email = "caree@example.com",
            password = "password123",
            healthInfo = "Health info",
            basicDetails = PersonalDetails(
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = "invalid-date" // Invalid format
            )
        )
        
        val result = validator.validate(invalidData)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(CareeValidationError.InvalidDateOfBirth))
    }
    
    @Test
    fun `validate should accept valid date of birth format`() {
        val validFormats = listOf("1950-05-15", "2000-12-31", "1980-01-01")
        
        validFormats.forEach { dateOfBirth ->
            val validData = CareeRegistrationData(
                email = "caree@example.com",
                password = "password123",
                healthInfo = "Health info",
                basicDetails = PersonalDetails(
                    firstName = "John",
                    lastName = "Doe",
                    dateOfBirth = dateOfBirth
                )
            )
            
            val result = validator.validate(validData)
            
            assertFalse(result.errors.contains(CareeValidationError.InvalidDateOfBirth), 
                "Date format $dateOfBirth should be valid")
        }
    }
    
    @Test
    fun `validate should collect multiple errors`() {
        val invalidData = CareeRegistrationData(
            email = "invalid-email",
            password = "123", // Too short
            healthInfo = "", // Empty
            basicDetails = PersonalDetails(
                firstName = "", // Empty
                lastName = "", // Empty
                dateOfBirth = "invalid-date" // Invalid format
            )
        )
        
        val result = validator.validate(invalidData)
        
        assertFalse(result.isValid)
        assertEquals(6, result.errors.size)
        assertTrue(result.errors.contains(CareeValidationError.InvalidEmail))
        assertTrue(result.errors.contains(CareeValidationError.WeakPassword))
        assertTrue(result.errors.contains(CareeValidationError.EmptyHealthInfo))
        assertTrue(result.errors.contains(CareeValidationError.EmptyFirstName))
        assertTrue(result.errors.contains(CareeValidationError.EmptyLastName))
        assertTrue(result.errors.contains(CareeValidationError.InvalidDateOfBirth))
    }
    
    @Test
    fun `validate should accept optional fields as empty`() {
        val validData = CareeRegistrationData(
            email = "caree@example.com",
            password = "password123",
            healthInfo = "Health info",
            basicDetails = PersonalDetails(
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = "1950-05-15",
                address = null, // Optional
                emergencyContact = null // Optional
            )
        )
        
        val result = validator.validate(validData)
        
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }
}