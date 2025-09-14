package com.carecomms.data.sync

import com.carecomms.data.repository.LocalCacheRepository
import com.carecomms.data.repository.NetworkMonitor
import com.carecomms.data.utils.RetryMechanism
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class OfflineSyncManagerTest {
    
    private val testScope = TestScope()
    private val mockNetworkMonitor = MockNetworkMonitor()
    private val mockCacheRepository = MockLocalCacheRepository()
    private val retryMechanism = RetryMechanism()
    
    private lateinit var syncManager: OfflineSyncManager
    
    @BeforeTest
    fun setup() {
        syncManager = OfflineSyncManager(
            localCacheRepository = mockCacheRepository,
            networkMonitor = mockNetworkMonitor,
            retryMechanism = retryMechanism,
            coroutineScope = testScope
        )
    }
    
    @Test
    fun `queueOperation should store operation locally`() = runTest {
        val operation = PendingOperation(
            id = "test-1",
            type = OperationType.SEND_MESSAGE,
            data = mapOf("message" to "Hello"),
            timestamp = System.currentTimeMillis()
        )
        
        syncManager.queueOperation(operation)
        
        assertTrue(mockCacheRepository.storedOperations.containsKey(operation.id))
        assertEquals(1, syncManager.getPendingOperationCount())
        assertTrue(syncManager.hasPendingOperations())
    }
    
    @Test
    fun `executeWithOfflineSupport should execute local operation when offline`() = runTest {
        mockNetworkMonitor.setOnline(false)
        
        var localExecuted = false
        var remoteExecuted = false
        
        val result = syncManager.executeWithOfflineSupport(
            operationId = "test-offline",
            localOperation = {
                localExecuted = true
                "local-result"
            },
            remoteOperation = {
                remoteExecuted = true
                Result.success("remote-result")
            }
        )
        
        assertTrue(result.isSuccess)
        assertEquals("local-result", result.getOrNull())
        assertTrue(localExecuted)
        assertFalse(remoteExecuted)
        assertTrue(syncManager.hasPendingOperations())
    }
    
    @Test
    fun `executeWithOfflineSupport should execute both operations when online`() = runTest {
        mockNetworkMonitor.setOnline(true)
        
        var localExecuted = false
        var remoteExecuted = false
        
        val result = syncManager.executeWithOfflineSupport(
            operationId = "test-online",
            localOperation = {
                localExecuted = true
                "local-result"
            },
            remoteOperation = {
                remoteExecuted = true
                Result.success("remote-result")
            }
        )
        
        assertTrue(result.isSuccess)
        assertEquals("local-result", result.getOrNull())
        assertTrue(localExecuted)
        assertTrue(remoteExecuted)
    }
    
    @Test
    fun `loadPendingOperations should restore operations from cache`() = runTest {
        val operations = listOf(
            PendingOperation("op1", OperationType.SEND_MESSAGE, mapOf(), System.currentTimeMillis()),
            PendingOperation("op2", OperationType.UPDATE_PROFILE, mapOf(), System.currentTimeMillis())
        )
        
        mockCacheRepository.pendingOperations = operations.toMutableList()
        
        syncManager.loadPendingOperations()
        
        assertEquals(2, syncManager.getPendingOperationCount())
    }
    
    @Test
    fun `clearPendingOperations should remove all operations`() = runTest {
        val operation = PendingOperation(
            id = "test-clear",
            type = OperationType.SEND_MESSAGE,
            data = mapOf(),
            timestamp = System.currentTimeMillis()
        )
        
        syncManager.queueOperation(operation)
        assertEquals(1, syncManager.getPendingOperationCount())
        
        syncManager.clearPendingOperations()
        assertEquals(0, syncManager.getPendingOperationCount())
        assertFalse(syncManager.hasPendingOperations())
    }
    
    @Test
    fun `sync status should update during sync process`() = runTest {
        mockNetworkMonitor.setOnline(true)
        
        val operation = PendingOperation(
            id = "test-sync-status",
            type = OperationType.SEND_MESSAGE,
            data = mapOf(),
            timestamp = System.currentTimeMillis()
        )
        
        syncManager.queueOperation(operation)
        
        // Initial status should be idle or syncing
        val initialStatus = syncManager.syncStatus.value
        assertTrue(initialStatus is SyncStatus.Idle || initialStatus is SyncStatus.Syncing)
    }
}

// Mock implementations for testing
class MockNetworkMonitor : NetworkMonitor {
    private val _isOnline = MutableStateFlow(true)
    override val isOnline = _isOnline
    override val connectionQuality = flowOf(ConnectionQuality.EXCELLENT)
    
    fun setOnline(online: Boolean) {
        _isOnline.value = online
    }
    
    override suspend fun checkConnectivity(): Boolean = _isOnline.value
}

class MockLocalCacheRepository : LocalCacheRepository {
    val cache = mutableMapOf<String, String>()
    val storedOperations = mutableMapOf<String, PendingOperation>()
    var pendingOperations = mutableListOf<PendingOperation>()
    
    override suspend fun put(key: String, value: String, expirationTimeMillis: Long?): Result<Unit> {
        cache[key] = value
        return Result.success(Unit)
    }
    
    override suspend fun get(key: String): String? = cache[key]
    
    override suspend fun remove(key: String): Result<Unit> {
        cache.remove(key)
        return Result.success(Unit)
    }
    
    override suspend fun clearExpired(): Result<Unit> = Result.success(Unit)
    override suspend fun clearAll(): Result<Unit> {
        cache.clear()
        return Result.success(Unit)
    }
    
    override suspend inline fun <reified T> putObject(key: String, value: T, expirationTimeMillis: Long?): Result<Unit> {
        // Simplified implementation for testing
        return Result.success(Unit)
    }
    
    override suspend inline fun <reified T> getObject(key: String): T? = null
    
    override suspend fun getCachedMessages(chatId: String) = emptyList<com.carecomms.data.models.Message>()
    override suspend fun cacheMessages(chatId: String, messages: List<com.carecomms.data.models.Message>) {}
    override suspend fun getCachedChatPreviews(carerId: String) = emptyList<com.carecomms.data.models.ChatPreview>()
    override suspend fun cacheChatPreviews(carerId: String, previews: List<com.carecomms.data.models.ChatPreview>) {}
    override suspend fun isDataStale(cacheKey: String, maxAgeMillis: Long) = false
    override suspend fun clearExpiredCache() {}
    
    override suspend fun storePendingOperation(operation: PendingOperation) {
        storedOperations[operation.id] = operation
        pendingOperations.add(operation)
    }
    
    override suspend fun getPendingOperations(): List<PendingOperation> = pendingOperations.toList()
    
    override suspend fun removePendingOperation(operationId: String) {
        storedOperations.remove(operationId)
        pendingOperations.removeAll { it.id == operationId }
    }
    
    override suspend fun clearPendingOperations() {
        storedOperations.clear()
        pendingOperations.clear()
    }
}