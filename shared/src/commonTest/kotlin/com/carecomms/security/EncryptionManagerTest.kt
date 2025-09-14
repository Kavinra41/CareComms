package com.carecomms.security

import kotlinx.coroutines.test.runTest
import kotlin.test.*

class EncryptionManagerTest {
    
    private lateinit var encryptionManager: MockEncryptionManager
    
    @BeforeTest
    fun setup() {
        encryptionManager = MockEncryptionManager()
    }
    
    @Test
    fun `encrypt and decrypt should work correctly`() = runTest {
        val originalData = "Sensitive health information"
        val key = encryptionManager.generateKey()
        
        val encryptResult = encryptionManager.encrypt(originalData, key)
        assertTrue(encryptResult.isSuccess)
        
        val encryptedData = encryptResult.getOrThrow()
        assertNotEquals(originalData, encryptedData)
        
        val decryptResult = encryptionManager.decrypt(encryptedData, key)
        assertTrue(decryptResult.isSuccess)
        
        val decryptedData = decryptResult.getOrThrow()
        assertEquals(originalData, decryptedData)
    }
    
    @Test
    fun `encrypt with invalid key should fail`() = runTest {
        val data = "Test data"
        val invalidKey = ""
        
        val result = encryptionManager.encrypt(data, invalidKey)
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `decrypt with wrong key should fail`() = runTest {
        val data = "Test data"
        val correctKey = encryptionManager.generateKey()
        val wrongKey = encryptionManager.generateKey()
        
        val encryptResult = encryptionManager.encrypt(data, correctKey)
        assertTrue(encryptResult.isSuccess)
        
        val encryptedData = encryptResult.getOrThrow()
        val decryptResult = encryptionManager.decrypt(encryptedData, wrongKey)
        assertTrue(decryptResult.isFailure)
    }
    
    @Test
    fun `password hashing should be consistent`() = runTest {
        val password = "TestPassword123!"
        val salt = encryptionManager.generateSalt()
        
        val hash1 = encryptionManager.hashPassword(password, salt)
        val hash2 = encryptionManager.hashPassword(password, salt)
        
        assertEquals(hash1, hash2)
    }
    
    @Test
    fun `password hashing with different salts should produce different hashes`() = runTest {
        val password = "TestPassword123!"
        val salt1 = encryptionManager.generateSalt()
        val salt2 = encryptionManager.generateSalt()
        
        val hash1 = encryptionManager.hashPassword(password, salt1)
        val hash2 = encryptionManager.hashPassword(password, salt2)
        
        assertNotEquals(hash1, hash2)
    }
    
    @Test
    fun `generateKey should produce valid keys`() = runTest {
        val key = encryptionManager.generateKey()
        
        assertNotNull(key)
        assertTrue(key.isNotEmpty())
        assertTrue(EncryptionUtils.validateEncryptionKey(key))
    }
    
    @Test
    fun `generateSalt should produce unique salts`() = runTest {
        val salt1 = encryptionManager.generateSalt()
        val salt2 = encryptionManager.generateSalt()
        
        assertNotNull(salt1)
        assertNotNull(salt2)
        assertNotEquals(salt1, salt2)
    }
}

// Mock implementation for testing
class MockEncryptionManager : EncryptionManager {
    override suspend fun encrypt(data: String, key: String): Result<String> {
        return if (EncryptionUtils.validateEncryptionKey(key)) {
            // Simple mock encryption (just base64 encode)
            val encoded = data.encodeToByteArray().let { 
                it.joinToString("") { byte -> "%02x".format(byte) }
            }
            Result.success("encrypted_$encoded")
        } else {
            Result.failure(SecurityException("Invalid key"))
        }
    }
    
    override suspend fun decrypt(encryptedData: String, key: String): Result<String> {
        return if (EncryptionUtils.validateEncryptionKey(key) && encryptedData.startsWith("encrypted_")) {
            try {
                val hexString = encryptedData.removePrefix("encrypted_")
                val bytes = hexString.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                val decoded = bytes.decodeToString()
                Result.success(decoded)
            } catch (e: Exception) {
                Result.failure(SecurityException("Decryption failed"))
            }
        } else {
            Result.failure(SecurityException("Invalid encrypted data or key"))
        }
    }
    
    override suspend fun generateKey(): String {
        return "mock_key_" + (1..32).map { ('a'..'z').random() }.joinToString("")
    }
    
    override suspend fun hashPassword(password: String, salt: String): String {
        return "hashed_${password}_with_${salt}"
    }
    
    override suspend fun generateSalt(): String {
        return "salt_" + (1..16).map { ('a'..'z').random() }.joinToString("")
    }
}