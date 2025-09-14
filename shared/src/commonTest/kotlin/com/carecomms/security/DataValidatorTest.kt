package com.carecomms.security

import com.carecomms.data.models.CareeRegistrationData
import com.carecomms.data.models.CarerRegistrationData
import com.carecomms.data.models.PersonalDetails
import kotlin.test.*

class DataValidatorTest {
    
    @Test
    fun `validateEmail should accept valid emails`() {
        val validEmails = listOf(
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.org",
            "123@example.com"
        )
        
        validEmails.forEach { email ->
            val result = DataValidator.validateEmail(email)
            assertTrue(result is ValidationResult.Valid, "Email $email should be valid")
        }
    }
    
    @Test
    fun `validateEmail should reject invalid emails`() {
        val invalidEmails = listOf(
            "",
            "invalid",
            "@example.com",
            "test@",
            "test..test@example.com",
            "test@example",
            "test@.com",
            "test@example..com"
        )
        
        invalidEmails.forEach { email ->
            val result = DataValidator.validateEmail(email)
            assertTrue(result is ValidationResult.Invalid, "Email $email should be invalid")
        }
    }
    
    @Test
    fun `validateEmail should sanitize input`() {
        val result = DataValidator.validateEmail("  TEST@EXAMPLE.COM  ")
        assertTrue(result is ValidationResult.Valid)
        assertEquals("test@example.com", result.value)
    }
    
    @Test
    fun `validateEmail should reject SQL injection attempts`() {
        val maliciousEmails = listOf(
            "test@example.com'; DROP TABLE users; --",
            "test@example.com UNION SELECT * FROM users",
            "test@example.com<script>alert('xss')</script>"
        )
        
        maliciousEmails.forEach { email ->
            val result = DataValidator.validateEmail(email)
            assertTrue(result is ValidationResult.Invalid, "Malicious email $email should be rejected")
        }
    }
    
    @Test
    fun `validatePassword should accept strong passwords`() {
        val strongPasswords = listOf(
            "Password123!",
            "MyStr0ng@Pass",
            "C0mpl3x#P@ssw0rd",
            "Secure123$"
        )
        
        strongPasswords.forEach { password ->
            val result = DataValidator.validatePassword(password)
            assertTrue(result is ValidationResult.Valid, "Password $password should be valid")
        }
    }
    
    @Test
    fun `validatePassword should reject weak passwords`() {
        val weakPasswords = listOf(
            "",
            "short",
            "password", // no uppercase, no number, no special char
            "PASSWORD", // no lowercase, no number, no special char
            "Password", // no number, no special char
            "Password123", // no special char
            "password123!", // no uppercase
            "PASSWORD123!" // no lowercase
        )
        
        weakPasswords.forEach { password ->
            val result = DataValidator.validatePassword(password)
            assertTrue(result is ValidationResult.Invalid, "Password $password should be invalid")
        }
    }
    
    @Test
    fun `validatePhoneNumber should accept valid phone numbers`() {
        val validPhones = listOf(
            "+1234567890",
            "1234567890",
            "+44 20 7946 0958",
            "(555) 123-4567",
            "555-123-4567"
        )
        
        validPhones.forEach { phone ->
            val result = DataValidator.validatePhoneNumber(phone)
            assertTrue(result is ValidationResult.Valid, "Phone $phone should be valid")
        }
    }
    
    @Test
    fun `validatePhoneNumber should reject invalid phone numbers`() {
        val invalidPhones = listOf(
            "",
            "abc",
            "123",
            "+",
            "++1234567890",
            "123-abc-4567"
        )
        
        invalidPhones.forEach { phone ->
            val result = DataValidator.validatePhoneNumber(phone)
            assertTrue(result is ValidationResult.Invalid, "Phone $phone should be invalid")
        }
    }
    
    @Test
    fun `validateAge should accept valid ages`() {
        val validAges = listOf(18, 25, 50, 75, 100, 119)
        
        validAges.forEach { age ->
            val result = DataValidator.validateAge(age)
            assertTrue(result is ValidationResult.Valid, "Age $age should be valid")
        }
    }
    
    @Test
    fun `validateAge should reject invalid ages`() {
        val invalidAges = listOf(0, 10, 17, 121, 150, -5)
        
        invalidAges.forEach { age ->
            val result = DataValidator.validateAge(age)
            assertTrue(result is ValidationResult.Invalid, "Age $age should be invalid")
        }
    }
    
    @Test
    fun `validateLocation should accept valid locations`() {
        val validLocations = listOf(
            "New York, NY",
            "London, UK",
            "123 Main Street, City",
            "Rural Area"
        )
        
        validLocations.forEach { location ->
            val result = DataValidator.validateLocation(location)
            assertTrue(result is ValidationResult.Valid, "Location $location should be valid")
        }
    }
    
    @Test
    fun `validateLocation should reject invalid locations`() {
        val invalidLocations = listOf(
            "",
            "a".repeat(201), // too long
            "Location'; DROP TABLE locations; --"
        )
        
        invalidLocations.forEach { location ->
            val result = DataValidator.validateLocation(location)
            assertTrue(result is ValidationResult.Invalid, "Location $location should be invalid")
        }
    }
    
    @Test
    fun `validateHealthInfo should accept valid health information`() {
        val validHealthInfo = listOf(
            "",
            "No known allergies",
            "Diabetes, takes insulin daily",
            "a".repeat(1000) // max length
        )
        
        validHealthInfo.forEach { info ->
            val result = DataValidator.validateHealthInfo(info)
            assertTrue(result is ValidationResult.Valid, "Health info should be valid")
        }
    }
    
    @Test
    fun `validateHealthInfo should reject invalid health information`() {
        val invalidHealthInfo = listOf(
            "a".repeat(1001), // too long
            "Health info'; DROP TABLE health; --"
        )
        
        invalidHealthInfo.forEach { info ->
            val result = DataValidator.validateHealthInfo(info)
            assertTrue(result is ValidationResult.Invalid, "Health info should be invalid")
        }
    }
    
    @Test
    fun `validateCarerRegistration should return errors for invalid data`() {
        val invalidData = CarerRegistrationData(
            email = "invalid-email",
            password = "weak",
            documents = emptyList(),
            age = 15,
            phoneNumber = "invalid",
            location = ""
        )
        
        val errors = DataValidator.validateCarerRegistration(invalidData)
        
        assertTrue(errors.isNotEmpty())
        assertTrue(errors.any { it.field == "email" })
        assertTrue(errors.any { it.field == "password" })
        assertTrue(errors.any { it.field == "age" })
        assertTrue(errors.any { it.field == "phoneNumber" })
        assertTrue(errors.any { it.field == "location" })
    }
    
    @Test
    fun `validateCarerRegistration should return no errors for valid data`() {
        val validData = CarerRegistrationData(
            email = "carer@example.com",
            password = "StrongPass123!",
            documents = listOf("license.pdf"),
            age = 30,
            phoneNumber = "+1234567890",
            location = "New York, NY"
        )
        
        val errors = DataValidator.validateCarerRegistration(validData)
        assertTrue(errors.isEmpty())
    }
    
    @Test
    fun `validateCareeRegistration should return errors for invalid data`() {
        val invalidData = CareeRegistrationData(
            email = "invalid-email",
            password = "weak",
            healthInfo = "a".repeat(1001),
            basicDetails = PersonalDetails(
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = "1990-01-01"
            )
        )
        
        val errors = DataValidator.validateCareeRegistration(invalidData)
        
        assertTrue(errors.isNotEmpty())
        assertTrue(errors.any { it.field == "email" })
        assertTrue(errors.any { it.field == "password" })
        assertTrue(errors.any { it.field == "healthInfo" })
    }
    
    @Test
    fun `validateCareeRegistration should return no errors for valid data`() {
        val validData = CareeRegistrationData(
            email = "caree@example.com",
            password = "StrongPass123!",
            healthInfo = "No known allergies",
            basicDetails = PersonalDetails(
                firstName = "Jane",
                lastName = "Doe",
                dateOfBirth = "1950-01-01"
            )
        )
        
        val errors = DataValidator.validateCareeRegistration(validData)
        assertTrue(errors.isEmpty())
    }
}