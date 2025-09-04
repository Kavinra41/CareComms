package com.carecomms.data.storage

import kotlinx.coroutines.test.runTest
import kotlin.test.*

class MockSecureStorageImpl : SecureStorage {
    private val storage = mutableMapOf<String, String>()
    private var shouldFail = false
    
    fun setShouldFail(shouldFail: Boolean) {
        this.shouldFail = shouldFail
    }
    
    override suspend fun storeToken(key: String, token: String): Result<Unit> {
        return if (shouldFail) {
            Result.failure(Exception("Storage failed"))
        } else {
            storage[key] = token
            Result.success(Unit)
        }
    }
    
    override suspend fun getToken(key: String): Result<String?> {
        return if (shouldFail) {
            Result.failure(Exception("Retrieval failed"))
        } else {
            Result.success(storage[key])
        }
    }
    
    override suspend fun removeToken(key: String): Result<Unit> {
        return if (shouldFail) {
            Result.failure(Exception("Removal failed"))
        } else {
            storage.remove(key)
            Result.success(Unit)
        }
    }
    
    override suspend fun clearAll(): Result<Unit> {
        return if (shouldFail) {
            Result.failure(Exception("Clear failed"))
        } else {
            storage.clear()
            Result.success(Unit)
        }
    }
}

class SecureStorageTest {
    
    private lateinit var secureStorage: MockSecureStorageImpl
    
    @BeforeTest
    fun setup() {
        secureStorage = MockSecureStorageImpl()
    }
    
    @Test
    fun testStoreAndRetrieveToken() = runTest {
        val key = "test_key"
        val token = "test_token_123"
        
        val storeResult = secureStorage.storeToken(key, token)
        assertTrue(storeResult.isSuccess)
        
        val retrieveResult = secureStorage.getToken(key)
        assertTrue(retrieveResult.isSuccess)
        assertEquals(token, retrieveResult.getOrNull())
    }
    
    @Test
    fun testRetrieveNonExistentToken() = runTest {
        val result = secureStorage.getToken("non_existent_key")
        
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }
    
    @Test
    fun testRemoveToken() = runTest {
        val key = "test_key"
        val token = "test_token_123"
        
        // Store token
        secureStorage.storeToken(key, token)
        assertEquals(token, secureStorage.getToken(key).getOrNull())
        
        // Remove token
        val removeResult = secureStorage.removeToken(key)
        assertTrue(removeResult.isSuccess)
        
        // Verify token is removed
        assertNull(secureStorage.getToken(key).getOrNull())
    }
    
    @Test
    fun testClearAll() = runTest {
        // Store multiple tokens
        secureStorage.storeToken("key1", "token1")
        secureStorage.storeToken("key2", "token2")
        secureStorage.storeToken("key3", "token3")
        
        // Verify tokens are stored
        assertEquals("token1", secureStorage.getToken("key1").getOrNull())
        assertEquals("token2", secureStorage.getToken("key2").getOrNull())
        assertEquals("token3", secureStorage.getToken("key3").getOrNull())
        
        // Clear all
        val clearResult = secureStorage.clearAll()
        assertTrue(clearResult.isSuccess)
        
        // Verify all tokens are cleared
        assertNull(secureStorage.getToken("key1").getOrNull())
        assertNull(secureStorage.getToken("key2").getOrNull())
        assertNull(secureStorage.getToken("key3").getOrNull())
    }
    
    @Test
    fun testOverwriteExistingToken() = runTest {
        val key = "test_key"
        val originalToken = "original_token"
        val newToken = "new_token"
        
        // Store original token
        secureStorage.storeToken(key, originalToken)
        assertEquals(originalToken, secureStorage.getToken(key).getOrNull())
        
        // Overwrite with new token
        secureStorage.storeToken(key, newToken)
        assertEquals(newToken, secureStorage.getToken(key).getOrNull())
    }
    
    @Test
    fun testStorageFailure() = runTest {
        secureStorage.setShouldFail(true)
        
        val storeResult = secureStorage.storeToken("key", "token")
        assertTrue(storeResult.isFailure)
        
        val retrieveResult = secureStorage.getToken("key")
        assertTrue(retrieveResult.isFailure)
        
        val removeResult = secureStorage.removeToken("key")
        assertTrue(removeResult.isFailure)
        
        val clearResult = secureStorage.clearAll()
        assertTrue(clearResult.isFailure)
    }
    
    @Test
    fun testSecureStorageKeys() {
        assertEquals("auth_token", SecureStorageKeys.AUTH_TOKEN)
        assertEquals("refresh_token", SecureStorageKeys.REFRESH_TOKEN)
        assertEquals("user_id", SecureStorageKeys.USER_ID)
        assertEquals("user_email", SecureStorageKeys.USER_EMAIL)
    }
    
    @Test
    fun testMultipleTokensStorage() = runTest {
        val tokens = mapOf(
            SecureStorageKeys.AUTH_TOKEN to "auth_token_123",
            SecureStorageKeys.REFRESH_TOKEN to "refresh_token_456",
            SecureStorageKeys.USER_ID to "user_id_789",
            SecureStorageKeys.USER_EMAIL to "user@example.com"
        )
        
        // Store all tokens
        tokens.forEach { (key, token) ->
            val result = secureStorage.storeToken(key, token)
            assertTrue(result.isSuccess)
        }
        
        // Verify all tokens are stored correctly
        tokens.forEach { (key, expectedToken) ->
            val result = secureStorage.getToken(key)
            assertTrue(result.isSuccess)
            assertEquals(expectedToken, result.getOrNull())
        }
    }
}