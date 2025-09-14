package com.carecomms.security

import com.carecomms.data.models.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class SecureRepositoryTest {
    
    private lateinit var secureRepository: SecureRepository
    private lateinit var mockEncryptionManager: MockEncryptionManager
    private lateinit var mockSecureStorage: MockSecureStorage
    
    @BeforeTest
    fun setup() {
        mockEncryptionManager = MockEncryptionManager()
        mockSecureStorage = MockSecureStorage()
        secureRepository = SecureRepository(mockEncryptionManager, mockSecureStorage)
    }
    
    @Test
    fun `storeEncryptedHealthData should encrypt and store data`() = runTest {
        val userId = "test_user_id"
        val healthData = "Sensitive health information"
        
        val result = secureRepository.storeEncryptedHealthData(userId, healthData)
        assertTrue(result.isSuccess)
        
        // Verify data is stored and encrypted
        val storedData = mockSecureStorage.retrieve("health_data_$userId").getOrThrow()
        assertNotNull(storedData)
        assertNotEquals(healthData, storedData)
        assertTrue(storedData!!.startsWith("encrypted_"))
    }
    
    @Test
    fun `retrieveEncryptedHealthData should decrypt and return data`() = runTest {
        val userId = "test_user_id"
        val healthData = "Sensitive health information"
        
        // Store encrypted data first
        secureRepository.storeEncryptedHealthData(userId, healthData)
        
        // Retrieve and verify decryption
        val result = secureRepository.retrieveEncryptedHealthData(userId)
        assertTrue(result.isSuccess)
        assertEquals(healthData, result.getOrThrow())
    }
    
    @Test
    fun `retrieveEncryptedHealthData should return null for non-existent data`() = runTest {
        val userId = "non_existent_user"
        
        val result = secureRepository.retrieveEncryptedHealthData(userId)
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }
    
    @Test
    fun `encryptSensitiveUserData should encrypt caree health info`() = runTest {
        val caree = Caree(
            id = "caree_id",
            email = "caree@example.com",
            createdAt = 1234567890L,
            healthInfo = "Sensitive health information",
            personalDetails = PersonalDetails(
                firstName = "Jane",
                lastName = "Doe",
                dateOfBirth = "1950-01-01"
            ),
            carerId = "carer_id"
        )
        
        val result = secureRepository.encryptSensitiveUserData(caree)
        assertTrue(result.isSuccess)
        
        val encryptedCaree = result.getOrThrow() as Caree
        assertNotEquals(caree.healthInfo, encryptedCaree.healthInfo)
        assertTrue(encryptedCaree.healthInfo.startsWith("encrypted_"))
        
        // Other fields should remain unchanged
        assertEquals(caree.id, encryptedCaree.id)
        assertEquals(caree.email, encryptedCaree.email)
        assertEquals(caree.personalDetails, encryptedCaree.personalDetails)
    }
    
    @Test
    fun `encryptSensitiveUserData should not modify carer data`() = runTest {
        val carer = Carer(
            id = "carer_id",
            email = "carer@example.com",
            createdAt = 1234567890L,
            documents = listOf("license.pdf"),
            age = 30,
            phoneNumber = "+1234567890",
            location = "New York, NY",
            careeIds = listOf("caree1", "caree2")
        )
        
        val result = secureRepository.encryptSensitiveUserData(carer)
        assertTrue(result.isSuccess)
        
        val resultCarer = result.getOrThrow()
        assertEquals(carer, resultCarer)
    }
    
    @Test
    fun `decryptSensitiveUserData should decrypt caree health info`() = runTest {
        val originalHealthInfo = "Sensitive health information"
        val caree = Caree(
            id = "caree_id",
            email = "caree@example.com",
            createdAt = 1234567890L,
            healthInfo = originalHealthInfo,
            personalDetails = PersonalDetails(
                firstName = "Jane",
                lastName = "Doe",
                dateOfBirth = "1950-01-01"
            ),
            carerId = "carer_id"
        )
        
        // First encrypt the data
        val encryptResult = secureRepository.encryptSensitiveUserData(caree)
        val encryptedCaree = encryptResult.getOrThrow() as Caree
        
        // Then decrypt it back
        val decryptResult = secureRepository.decryptSensitiveUserData(encryptedCaree)
        assertTrue(decryptResult.isSuccess)
        
        val decryptedCaree = decryptResult.getOrThrow() as Caree
        assertEquals(originalHealthInfo, decryptedCaree.healthInfo)
    }
    
    @Test
    fun `encryptMessage should encrypt message content`() = runTest {
        val message = Message(
            id = "msg_id",
            senderId = "sender_id",
            content = "Sensitive message content",
            timestamp = 1234567890L,
            status = MessageStatus.SENT
        )
        
        val result = secureRepository.encryptMessage(message)
        assertTrue(result.isSuccess)
        
        val encryptedMessage = result.getOrThrow()
        assertNotEquals(message.content, encryptedMessage.content)
        assertTrue(encryptedMessage.content.startsWith("encrypted_"))
        
        // Other fields should remain unchanged
        assertEquals(message.id, encryptedMessage.id)
        assertEquals(message.senderId, encryptedMessage.senderId)
        assertEquals(message.timestamp, encryptedMessage.timestamp)
        assertEquals(message.status, encryptedMessage.status)
    }
    
    @Test
    fun `decryptMessage should decrypt message content`() = runTest {
        val originalContent = "Sensitive message content"
        val message = Message(
            id = "msg_id",
            senderId = "sender_id",
            content = originalContent,
            timestamp = 1234567890L,
            status = MessageStatus.SENT
        )
        
        // First encrypt the message
        val encryptResult = secureRepository.encryptMessage(message)
        val encryptedMessage = encryptResult.getOrThrow()
        
        // Then decrypt it back
        val decryptResult = secureRepository.decryptMessage(encryptedMessage)
        assertTrue(decryptResult.isSuccess)
        
        val decryptedMessage = decryptResult.getOrThrow()
        assertEquals(originalContent, decryptedMessage.content)
    }
    
    @Test
    fun `clearEncryptedData should remove user's encrypted data`() = runTest {
        val userId = "test_user_id"
        val healthData = "Sensitive health information"
        
        // Store some encrypted data
        secureRepository.storeEncryptedHealthData(userId, healthData)
        
        // Verify data exists
        val retrieveResult = secureRepository.retrieveEncryptedHealthData(userId)
        assertNotNull(retrieveResult.getOrThrow())
        
        // Clear the data
        val clearResult = secureRepository.clearEncryptedData(userId)
        assertTrue(clearResult.isSuccess)
        
        // Verify data is cleared
        val retrieveAfterClear = secureRepository.retrieveEncryptedHealthData(userId)
        assertNull(retrieveAfterClear.getOrThrow())
    }
}