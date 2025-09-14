package com.carecomms.data.repository

import com.carecomms.data.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class OfflineFirstRepositoryTest {
    
    private lateinit var mockLocalCacheRepository: MockLocalCacheRepository
    private lateinit var mockNetworkMonitor: MockNetworkMonitor
    private lateinit var offlineFirstRepository: OfflineFirstRepository
    
    @BeforeTest
    fun setup() {
        mockLocalCacheRepository = MockLocalCacheRepository()
        mockNetworkMonitor = MockNetworkMonitor()
        offlineFirstRepository = OfflineFirstRepository(mockLocalCacheRepository, mockNetworkMonitor)
    }
    
    @Test
    fun `getDataOfflineFirst should return cached data first when available`() = runTest {
        // Given
        val cachedData = "cached_data"
        val cacheKey = "test_key"
        
        // When
        val results = mutableListOf<Result<String>>()
        offlineFirstRepository.getDataOfflineFirst(
            cacheKey = cacheKey,
            networkFetch = { Result.success("network_data") },
            cacheStore = { },
            cacheRetrieve = { cachedData }
        ).take(1).collect { results.add(it) }
        
        // Then
        assertEquals(1, results.size)
        assertTrue(results[0].isSuccess)
        assertEquals(cachedData, results[0].getOrNull())
    }
    
    @Test
    fun `getDataOfflineFirst should fetch network data when online`() = runTest {
        // Given
        val networkData = "network_data"
        val cacheKey = "test_key"
        var storedData: String? = null
        
        mockNetworkMonitor.setOnline(true)
        
        // When
        val results = mutableListOf<Result<String>>()
        offlineFirstRepository.getDataOfflineFirst(
            cacheKey = cacheKey,
            networkFetch = { Result.success(networkData) },
            cacheStore = { storedData = it },
            cacheRetrieve = { null }
        ).take(1).collect { results.add(it) }
        
        // Then
        assertEquals(1, results.size)
        assertTrue(results[0].isSuccess)
        assertEquals(networkData, results[0].getOrNull())
        assertEquals(networkData, storedData)
    }
    
    @Test
    fun `getDataOfflineFirst should return offline error when no cache and offline`() = runTest {
        // Given
        val cacheKey = "test_key"
        mockNetworkMonitor.setOnline(false)
        
        // When
        val results = mutableListOf<Result<String>>()
        offlineFirstRepository.getDataOfflineFirst(
            cacheKey = cacheKey,
            networkFetch = { Result.success("network_data") },
            cacheStore = { },
            cacheRetrieve = { null }
        ).take(1).collect { results.add(it) }
        
        // Then
        assertEquals(1, results.size)
        assertTrue(results[0].isFailure)
        assertTrue(results[0].exceptionOrNull() is OfflineException)
    }
    
    @Test
    fun `performWriteOperation should execute local update first`() = runTest {
        // Given
        var localUpdateCalled = false
        val operation = SyncOperation("op1", SyncOperationType.SEND_MESSAGE, emptyMap())
        
        mockNetworkMonitor.setOnline(true)
        
        // When
        val result = offlineFirstRepository.performWriteOperation(
            operation = operation,
            localUpdate = { localUpdateCalled = true },
            networkUpdate = { Result.success(Unit) }
        )
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(localUpdateCalled)
    }
    
    @Test
    fun `performWriteOperation should queue operation when offline`() = runTest {
        // Given
        var localUpdateCalled = false
        val operation = SyncOperation("op1", SyncOperationType.SEND_MESSAGE, emptyMap())
        
        mockNetworkMonitor.setOnline(false)
        
        // When
        val result = offlineFirstRepository.performWriteOperation(
            operation = operation,
            localUpdate = { localUpdateCalled = true },
            networkUpdate = { Result.failure(Exception("Should not be called")) }
        )
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(localUpdateCalled)
    }
    
    @Test
    fun `getCachedMessages should return messages from cache`() = runTest {
        // Given
        val chatId = "chat1"
        val messages = listOf(
            Message("msg1", "user1", "Hello", System.currentTimeMillis(), MessageStatus.SENT)
        )
        mockLocalCacheRepository.cachedMessages[chatId] = messages
        
        // When
        val result = offlineFirstRepository.getCachedMessages(chatId)
        
        // Then
        assertEquals(messages, result)
    }
    
    @Test
    fun `cacheMessages should store messages in cache`() = runTest {
        // Given
        val chatId = "chat1"
        val messages = listOf(
            Message("msg1", "user1", "Hello", System.currentTimeMillis(), MessageStatus.SENT)
        )
        
        // When
        offlineFirstRepository.cacheMessages(chatId, messages)
        
        // Then
        assertEquals(messages, mockLocalCacheRepository.cachedMessages[chatId])
    }
    
    @Test
    fun `getCachedChatPreviews should return previews from cache`() = runTest {
        // Given
        val carerId = "carer1"
        val previews = listOf(
            ChatPreview("chat1", "John Doe", "Hello", System.currentTimeMillis(), 0, true)
        )
        mockLocalCacheRepository.cachedChatPreviews[carerId] = previews
        
        // When
        val result = offlineFirstRepository.getCachedChatPreviews(carerId)
        
        // Then
        assertEquals(previews, result)
    }
    
    @Test
    fun `getNetworkStatus should return network monitor status`() = runTest {
        // Given
        mockNetworkMonitor.setOnline(true)
        
        // When
        val isOnline = offlineFirstRepository.getNetworkStatus().first()
        
        // Then
        assertTrue(isOnline)
    }
}

class MockLocalCacheRepository : LocalCacheRepository {
    val cachedMessages = mutableMapOf<String, List<Message>>()
    val cachedChatPreviews = mutableMapOf<String, List<ChatPreview>>()
    val cacheTimestamps = mutableMapOf<String, Long>()
    
    override suspend fun cacheMessages(chatId: String, messages: List<Message>) {
        cachedMessages[chatId] = messages
        cacheTimestamps["messages_$chatId"] = System.currentTimeMillis()
    }
    
    override suspend fun getCachedMessages(chatId: String): List<Message> {
        return cachedMessages[chatId] ?: emptyList()
    }
    
    override suspend fun cacheChatPreviews(carerId: String, previews: List<ChatPreview>) {
        cachedChatPreviews[carerId] = previews
        cacheTimestamps["previews_$carerId"] = System.currentTimeMillis()
    }
    
    override suspend fun getCachedChatPreviews(carerId: String): List<ChatPreview> {
        return cachedChatPreviews[carerId] ?: emptyList()
    }
    
    override suspend fun isDataStale(cacheKey: String, maxAgeMillis: Long): Boolean {
        val timestamp = cacheTimestamps[cacheKey] ?: return true
        return System.currentTimeMillis() - timestamp > maxAgeMillis
    }
    
    override suspend fun clearExpiredCache() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = cacheTimestamps.filter { (_, timestamp) ->
            currentTime - timestamp > 24 * 60 * 60 * 1000L // 24 hours
        }.keys
        
        expiredKeys.forEach { key ->
            cacheTimestamps.remove(key)
            when {
                key.startsWith("messages_") -> {
                    val chatId = key.removePrefix("messages_")
                    cachedMessages.remove(chatId)
                }
                key.startsWith("previews_") -> {
                    val carerId = key.removePrefix("previews_")
                    cachedChatPreviews.remove(carerId)
                }
            }
        }
    }
}

class MockNetworkMonitor : NetworkMonitor {
    private val _isOnline = MutableStateFlow(false)
    override val isOnline: Flow<Boolean> = _isOnline.asStateFlow()
    
    fun setOnline(online: Boolean) {
        _isOnline.value = online
    }
}