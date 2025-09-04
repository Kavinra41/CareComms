package com.carecomms.data.validation

import com.carecomms.data.models.CarerRegistrationData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CarerRegistrationValidatorTest {
    
    private val validator = CarerRegistrationValidator()
    
    @Test
    fun `validate should return success for valid carer registration data`() {
        val validData = CarerRegistrationData(
            email = "carer@example.com",
            password = "SecurePass123",
            documents = listOf("certificate.pdf", "id.jpg"),
            age = 30,
            phoneNumber = "1234567890",
            location = "New York, NY"
        )
        
        val result = validator.validate(validData)
        
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }
    
    @Test
    fun `validate should fail for invalid email`() {
        val invalidEmailData = CarerRegistrationData(
            email = "invalid-email",
            password = "SecurePass123",
            documents = listOf("certificate.pdf"),
            age = 30,
            phoneNumber = "1234567890",
            location = "New York, NY"
        )
        
        val result = validator.validate(invalidEmailData)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.InvalidEmail))
    }
    
    @Test
    fun `validate should fail for empty email`() {
        val emptyEmailData = CarerRegistrationData(
            email = "",
            password = "SecurePass123",
            documents = listOf("certificate.pdf"),
            age = 30,
            phoneNumber = "1234567890",
            location = "New York, NY"
        )
        
        val result = validator.validate(emptyEmailData)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.InvalidEmail))
    }
    
    @Test
    fun `validate should fail for weak password`() {
        val weakPasswordData = CarerRegistrationData(
            email = "carer@example.com",
            password = "weak",
            documents = listOf("certificate.pdf"),
            age = 30,
            phoneNumber = "1234567890",
            location = "New York, NY"
        )
        
        val result = validator.validate(weakPasswordData)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.WeakPassword))
    }
    
    @Test
    fun `validate should fail for password without numbers`() {
        val passwordWithoutNumbers = CarerRegistrationData(
            email = "carer@example.com",
            password = "OnlyLetters",
            documents = listOf("certificate.pdf"),
            age = 30,
            phoneNumber = "1234567890",
            location = "New York, NY"
        )
        
        val result = validator.validate(passwordWithoutNumbers)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.WeakPassword))
    }
    
    @Test
    fun `validate should fail for password without letters`() {
        val passwordWithoutLetters = CarerRegistrationData(
            email = "carer@example.com",
            password = "12345678",
            documents = listOf("certificate.pdf"),
            age = 30,
            phoneNumber = "1234567890",
            location = "New York, NY"
        )
        
        val result = validator.validate(passwordWithoutLetters)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.WeakPassword))
    }
    
    @Test
    fun `validate should fail for invalid age - too young`() {
        val tooYoungData = CarerRegistrationData(
            email = "carer@example.com",
            password = "SecurePass123",
            documents = listOf("certificate.pdf"),
            age = 17,
            phoneNumber = "1234567890",
            location = "New York, NY"
        )
        
        val result = validator.validate(tooYoungData)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.InvalidAge))
    }
    
    @Test
    fun `validate should fail for invalid age - too old`() {
        val tooOldData = CarerRegistrationData(
            email = "carer@example.com",
            password = "SecurePass123",
            documents = listOf("certificate.pdf"),
            age = 101,
            phoneNumber = "1234567890",
            location = "New York, NY"
        )
        
        val result = validator.validate(tooOldData)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.InvalidAge))
    }
    
    @Test
    fun `validate should pass for boundary ages`() {
        val age18Data = CarerRegistrationData(
            email = "carer@example.com",
            password = "SecurePass123",
            documents = listOf("certificate.pdf"),
            age = 18,
            phoneNumber = "1234567890",
            location = "New York, NY"
        )
        
        val age100Data = CarerRegistrationData(
            email = "carer@example.com",
            password = "SecurePass123",
            documents = listOf("certificate.pdf"),
            age = 100,
            phoneNumber = "1234567890",
            location = "New York, NY"
        )
        
        assertTrue(validator.validate(age18Data).isValid)
        assertTrue(validator.validate(age100Data).isValid)
    }
    
    @Test
    fun `validate should fail for invalid phone number - too short`() {
        val shortPhoneData = CarerRegistrationData(
            email = "carer@example.com",
            password = "SecurePass123",
            documents = listOf("certificate.pdf"),
            age = 30,
            phoneNumber = "123456789", // 9 digits
            location = "New York, NY"
        )
        
        val result = validator.validate(shortPhoneData)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.InvalidPhoneNumber))
    }
    
    @Test
    fun `validate should pass for phone number with formatting`() {
        val formattedPhoneData = CarerRegistrationData(
            email = "carer@example.com",
            password = "SecurePass123",
            documents = listOf("certificate.pdf"),
            age = 30,
            phoneNumber = "+1 (555) 123-4567",
            location = "New York, NY"
        )
        
        val result = validator.validate(formattedPhoneData)
        
        assertTrue(result.isValid)
    }
    
    @Test
    fun `validate should fail for empty location`() {
        val emptyLocationData = CarerRegistrationData(
            email = "carer@example.com",
            password = "SecurePass123",
            documents = listOf("certificate.pdf"),
            age = 30,
            phoneNumber = "1234567890",
            location = ""
        )
        
        val result = validator.validate(emptyLocationData)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.EmptyLocation))
    }
    
    @Test
    fun `validate should fail for blank location`() {
        val blankLocationData = CarerRegistrationData(
            email = "carer@example.com",
            password = "SecurePass123",
            documents = listOf("certificate.pdf"),
            age = 30,
            phoneNumber = "1234567890",
            location = "   "
        )
        
        val result = validator.validate(blankLocationData)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.EmptyLocation))
    }
    
    @Test
    fun `validate should fail for no documents`() {
        val noDocumentsData = CarerRegistrationData(
            email = "carer@example.com",
            password = "SecurePass123",
            documents = emptyList(),
            age = 30,
            phoneNumber = "1234567890",
            location = "New York, NY"
        )
        
        val result = validator.validate(noDocumentsData)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.NoDocuments))
    }
    
    @Test
    fun `validate should return multiple errors for multiple invalid fields`() {
        val multipleErrorsData = CarerRegistrationData(
            email = "invalid-email",
            password = "weak",
            documents = emptyList(),
            age = 17,
            phoneNumber = "123",
            location = ""
        )
        
        val result = validator.validate(multipleErrorsData)
        
        assertFalse(result.isValid)
        assertEquals(6, result.errors.size)
        assertTrue(result.errors.contains(ValidationError.InvalidEmail))
        assertTrue(result.errors.contains(ValidationError.WeakPassword))
        assertTrue(result.errors.contains(ValidationError.InvalidAge))
        assertTrue(result.errors.contains(ValidationError.InvalidPhoneNumber))
        assertTrue(result.errors.contains(ValidationError.EmptyLocation))
        assertTrue(result.errors.contains(ValidationError.NoDocuments))
    }
}