package com.carecomms.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.carecomms.data.database.DatabaseManager
import com.carecomms.database.CareCommsDatabase
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.*

@Serializable
data class TestData(val name: String, val value: Int)

class LocalCacheRepositoryTest {
    
    private lateinit var driver: SqlDriver
    private lateinit var database: CareCommsDatabase
    private lateinit var databaseManager: DatabaseManager
    private lateinit var cacheRepository: LocalCacheRepositoryImpl
    private val json = Json { ignoreUnknownKeys = true }
    
    @BeforeTest
    fun setup() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        CareCommsDatabase.Schema.create(driver)
        database = CareCommsDatabase(driver)
        databaseManager = DatabaseManager(database)
        cacheRepository = LocalCacheRepositoryImpl(databaseManager, json)
    }
    
    @AfterTest
    fun teardown() {
        driver.close()
    }
    
    @Test
    fun testPutAndGet() = runTest {
        // Given
        val key = "test_key"
        val value = "test_value"
        
        // When
        val putResult = cacheRepository.put(key, value)
        val retrievedValue = cacheRepository.get(key)
        
        // Then
        assertTrue(putResult.isSuccess)
        assertEquals(value, retrievedValue)
    }
    
    @Test
    fun testPutWithExpiration() = runTest {
        // Given
        val key = "test_key"
        val value = "test_value"
        val expirationTime = System.currentTimeMillis() + 86400000 // 24 hours
        
        // When
        val putResult = cacheRepository.put(key, value, expirationTime)
        val retrievedValue = cacheRepository.get(key)
        
        // Then
        assertTrue(putResult.isSuccess)
        assertEquals(value, retrievedValue)
    }
    
    @Test
    fun testExpiredCacheNotRetrieved() = runTest {
        // Given
        val key = "test_key"
        val value = "test_value"
        val expirationTime = System.currentTimeMillis() - 1000 // Already expired
        
        // When
        val putResult = cacheRepository.put(key, value, expirationTime)
        val retrievedValue = cacheRepository.get(key)
        
        // Then
        assertTrue(putResult.isSuccess)
        assertNull(retrievedValue) // Should be null because it's expired
    }
    
    @Test
    fun testRemove() = runTest {
        // Given
        val key = "test_key"
        val value = "test_value"
        
        // When
        cacheRepository.put(key, value)
        val beforeRemove = cacheRepository.get(key)
        val removeResult = cacheRepository.remove(key)
        val afterRemove = cacheRepository.get(key)
        
        // Then
        assertEquals(value, beforeRemove)
        assertTrue(removeResult.isSuccess)
        assertNull(afterRemove)
    }
    
    @Test
    fun testPutAndGetObject() = runTest {
        // Given
        val key = "test_object"
        val testData = TestData("test", 42)
        
        // When
        val putResult = cacheRepository.putObject(key, testData)
        val retrievedData = cacheRepository.getObject<TestData>(key)
        
        // Then
        assertTrue(putResult.isSuccess)
        assertNotNull(retrievedData)
        assertEquals(testData.name, retrievedData.name)
        assertEquals(testData.value, retrievedData.value)
    }
    
    @Test
    fun testClearAll() = runTest {
        // Given
        val key1 = "key1"
        val key2 = "key2"
        val value1 = "value1"
        val value2 = "value2"
        
        // When
        cacheRepository.put(key1, value1)
        cacheRepository.put(key2, value2)
        val beforeClear1 = cacheRepository.get(key1)
        val beforeClear2 = cacheRepository.get(key2)
        val clearResult = cacheRepository.clearAll()
        val afterClear1 = cacheRepository.get(key1)
        val afterClear2 = cacheRepository.get(key2)
        
        // Then
        assertEquals(value1, beforeClear1)
        assertEquals(value2, beforeClear2)
        assertTrue(clearResult.isSuccess)
        assertNull(afterClear1)
        assertNull(afterClear2)
    }
}