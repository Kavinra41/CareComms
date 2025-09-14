package com.carecomms.security

import kotlinx.coroutines.test.runTest
import kotlin.test.*

class SecureStorageTest {
    
    private lateinit var secureStorage: MockSecureStorage
    
    @BeforeTest
    fun setup() {
        secureStorage = MockSecureStorage()
    }
    
    @Test
    fun `store and retrieve should work correctly`() = runTest {
        val key = "test_key"
        val value = "test_value"
        
        val storeResult = secureStorage.store(key, value)
        assertTrue(storeResult.isSuccess)
        
        val retrieveResult = secureStorage.retrieve(key)
        assertTrue(retrieveResult.isSuccess)
        assertEquals(value, retrieveResult.getOrThrow())
    }
    
    @Test
    fun `retrieve non-existent key should return null`() = runTest {
        val result = secureStorage.retrieve("non_existent_key")
        assertTrue(result.isSuccess)
        assertNull(result.getOrThrow())
    }
    
    @Test
    fun `delete should remove stored value`() = runTest {
        val key = "test_key"
        val value = "test_value"
        
        secureStorage.store(key, value)
        assertTrue(secureStorage.exists(key))
        
        val deleteResult = secureStorage.delete(key)
        assertTrue(deleteResult.isSuccess)
        assertFalse(secureStorage.exists(key))
        
        val retrieveResult = secureStorage.retrieve(key)
        assertNull(retrieveResult.getOrThrow())
    }
    
    @Test
    fun `clear should remove all stored values`() = runTest {
        secureStorage.store("key1", "value1")
        secureStorage.store("key2", "value2")
        
        assertTrue(secureStorage.exists("key1"))
        assertTrue(secureStorage.exists("key2"))
        
        val clearResult = secureStorage.clear()
        assertTrue(clearResult.isSuccess)
        
        assertFalse(secureStorage.exists("key1"))
        assertFalse(secureStorage.exists("key2"))
    }
    
    @Test
    fun `exists should return correct status`() = runTest {
        val key = "test_key"
        
        assertFalse(secureStorage.exists(key))
        
        secureStorage.store(key, "value")
        assertTrue(secureStorage.exists(key))
        
        secureStorage.delete(key)
        assertFalse(secureStorage.exists(key))
    }
}

// Mock implementation for testing
class MockSecureStorage : SecureStorage {
    private val storage = mutableMapOf<String, String>()
    
    override suspend fun store(key: String, value: String): Result<Unit> {
        storage[key] = value
        return Result.success(Unit)
    }
    
    override suspend fun retrieve(key: String): Result<String?> {
        return Result.success(storage[key])
    }
    
    override suspend fun delete(key: String): Result<Unit> {
        storage.remove(key)
        return Result.success(Unit)
    }
    
    override suspend fun clear(): Result<Unit> {
        storage.clear()
        return Result.success(Unit)
    }
    
    override suspend fun exists(key: String): Boolean {
        return storage.containsKey(key)
    }
}