package com.carecomms.security

import com.carecomms.data.models.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class SecurityIntegrationTest {
    
    private lateinit var encryptionManager: MockEncryptionManager
    private lateinit var secureStorage: MockSecureStorage
    private lateinit var sessionManager: SessionManager
    private lateinit var secureRepository: SecureRepository
    
    @BeforeTest
    fun setup() {
        encryptionManager = MockEncryptionManager()
        secureStorage = MockSecureStorage()
        sessionManager = SessionManager(secureStorage, encryptionManager)
        secureRepository = SecureRepository(encryptionManager, secureStorage)
    }
    
    @Test
    fun `complete authentication flow with security`() = runTest {
        // 1. User registration with validation
        val carerData = CarerRegistrationData(
            email = "carer@example.com",
            password = "SecurePass123!",
            documents = listOf("license.pdf"),
            age = 30,
            phoneNumber = "+1234567890",
            location = "New York, NY"
        )
        
        // Validate registration data
        val validationErrors = DataValidator.validateCarerRegistration(carerData)
        assertTrue(validationErrors.isEmpty(), "Registration data should be valid")
        
        // 2. Create session after successful authentication
        val userId = "carer_123"
        val authToken = "auth_token_xyz"
        val refreshToken = "refresh_token_abc"
        
        val sessionResult = sessionManager.createSession(userId, authToken, refreshToken)
        assertTrue(sessionResult.isSuccess, "Session creation should succeed")
        
        // 3. Validate session
        val validationResult = sessionManager.validateSession()
        assertTrue(validationResult.isSuccess)
        assertEquals(SessionValidation.Valid, validationResult.getOrThrow())
        
        // 4. Store encrypted health data for a caree
        val healthData = "Patient has diabetes, requires insulin monitoring"
        val careeId = "caree_456"
        
        val storeResult = secureRepository.storeEncryptedHealthData(careeId, healthData)
        assertTrue(storeResult.isSuccess, "Health data storage should succeed")
        
        // 5. Retrieve and verify encrypted health data
        val retrieveResult = secureRepository.retrieveEncryptedHealthData(careeId)
        assertTrue(retrieveResult.isSuccess)
        assertEquals(healthData, retrieveResult.getOrThrow())
        
        // 6. Test message encryption
        val message = Message(
            id = "msg_789",
            senderId = userId,
            content = "How are you feeling today? Any changes in your condition?",
            timestamp = 1234567890L,
            status = MessageStatus.SENT
        )
        
        val encryptResult = secureRepository.encryptMessage(message)
        assertTrue(encryptResult.isSuccess)
        
        val encryptedMessage = encryptResult.getOrThrow()
        assertNotEquals(message.content, encryptedMessage.content)
        
        val decryptResult = secureRepository.decryptMessage(encryptedMessage)
        assertTrue(decryptResult.isSuccess)
        assertEquals(message.content, decryptResult.getOrThrow().content)
        
        // 7. Session refresh
        val newAuthToken = "new_auth_token_xyz"
        val newRefreshToken = "new_refresh_token_abc"
        
        val refreshResult = sessionManager.refreshSession(newAuthToken, newRefreshToken)
        assertTrue(refreshResult.isSuccess, "Session refresh should succeed")
        
        // Verify updated tokens
        assertEquals(newAuthToken, sessionManager.getCurrentAuthToken())
        assertEquals(newRefreshToken, sessionManager.getCurrentRefreshToken())
        
        // 8. Clear session and encrypted data on logout
        val clearDataResult = secureRepository.clearEncryptedData(careeId)
        assertTrue(clearDataResult.isSuccess)
        
        val clearSessionResult = sessionManager.clearSession()
        assertTrue(clearSessionResult.isSuccess)
        
        // Verify everything is cleared
        assertNull(sessionManager.getCurrentUserId())
        assertNull(sessionManager.getCurrentAuthToken())
        
        val retrieveAfterClear = secureRepository.retrieveEncryptedHealthData(careeId)
        assertNull(retrieveAfterClear.getOrThrow())
    }
    
    @Test
    fun `caree registration with invitation validation and encryption`() = runTest {
        // 1. Validate invitation token (mock validation)
        val invitationToken = "valid_invitation_token_123"
        
        // 2. Caree registration data
        val careeData = CareeRegistrationData(
            email = "caree@example.com",
            password = "SecurePass456!",
            healthInfo = "No known allergies, takes blood pressure medication",
            basicDetails = PersonalDetails(
                firstName = "Jane",
                lastName = "Doe",
                dateOfBirth = "1950-01-01"
            )
        )
        
        // 3. Validate registration data
        val validationErrors = DataValidator.validateCareeRegistration(careeData)
        assertTrue(validationErrors.isEmpty(), "Caree registration data should be valid")
        
        // 4. Create caree user object
        val caree = Caree(
            id = "caree_789",
            email = careeData.email,
            createdAt = 1234567890L,
            healthInfo = careeData.healthInfo,
            personalDetails = careeData.basicDetails,
            carerId = "carer_123"
        )
        
        // 5. Encrypt sensitive user data
        val encryptResult = secureRepository.encryptSensitiveUserData(caree)
        assertTrue(encryptResult.isSuccess)
        
        val encryptedCaree = encryptResult.getOrThrow() as Caree
        assertNotEquals(caree.healthInfo, encryptedCaree.healthInfo)
        
        // 6. Decrypt for verification
        val decryptResult = secureRepository.decryptSensitiveUserData(encryptedCaree)
        assertTrue(decryptResult.isSuccess)
        
        val decryptedCaree = decryptResult.getOrThrow() as Caree
        assertEquals(caree.healthInfo, decryptedCaree.healthInfo)
        
        // 7. Create session for caree
        val sessionResult = sessionManager.createSession(
            caree.id,
            "caree_auth_token",
            "caree_refresh_token"
        )
        assertTrue(sessionResult.isSuccess)
    }
    
    @Test
    fun `data sanitization prevents injection attacks`() = runTest {
        // Test SQL injection prevention
        val maliciousInputs = listOf(
            "'; DROP TABLE users; --",
            "admin' OR '1'='1",
            "<script>alert('xss')</script>",
            "UNION SELECT * FROM passwords"
        )
        
        maliciousInputs.forEach { maliciousInput ->
            // Test email validation
            val emailResult = DataValidator.validateEmail("test@example.com$maliciousInput")
            assertTrue(emailResult is ValidationResult.Invalid, "Should reject malicious email input")
            
            // Test location validation
            val locationResult = DataValidator.validateLocation("New York$maliciousInput")
            assertTrue(locationResult is ValidationResult.Invalid, "Should reject malicious location input")
            
            // Test health info validation
            val healthResult = DataValidator.validateHealthInfo("Health info$maliciousInput")
            assertTrue(healthResult is ValidationResult.Invalid, "Should reject malicious health info input")
        }
    }
    
    @Test
    fun `password security requirements are enforced`() = runTest {
        val weakPasswords = listOf(
            "password",           // No uppercase, numbers, or special chars
            "PASSWORD123",        // No lowercase or special chars
            "Password",           // No numbers or special chars
            "Pass123!",          // Too short
            "simple123",         // No uppercase or special chars
            ""                   // Empty
        )
        
        weakPasswords.forEach { password ->
            val result = DataValidator.validatePassword(password)
            assertTrue(result is ValidationResult.Invalid, "Weak password '$password' should be rejected")
        }
        
        // Test strong password
        val strongPassword = "MyStr0ng@P@ssw0rd!"
        val result = DataValidator.validatePassword(strongPassword)
        assertTrue(result is ValidationResult.Valid, "Strong password should be accepted")
    }
    
    @Test
    fun `encryption keys are properly managed`() = runTest {
        // Test key generation
        val key1 = encryptionManager.generateKey()
        val key2 = encryptionManager.generateKey()
        
        assertNotEquals(key1, key2, "Generated keys should be unique")
        assertTrue(EncryptionUtils.validateEncryptionKey(key1), "Generated key should be valid")
        assertTrue(EncryptionUtils.validateEncryptionKey(key2), "Generated key should be valid")
        
        // Test salt generation
        val salt1 = encryptionManager.generateSalt()
        val salt2 = encryptionManager.generateSalt()
        
        assertNotEquals(salt1, salt2, "Generated salts should be unique")
        
        // Test password hashing with different salts
        val password = "TestPassword123!"
        val hash1 = encryptionManager.hashPassword(password, salt1)
        val hash2 = encryptionManager.hashPassword(password, salt2)
        
        assertNotEquals(hash1, hash2, "Same password with different salts should produce different hashes")
        
        // Test password hashing consistency
        val hash1Again = encryptionManager.hashPassword(password, salt1)
        assertEquals(hash1, hash1Again, "Same password and salt should produce same hash")
    }
}