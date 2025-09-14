package com.carecomms.data.repository

import com.carecomms.data.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Repository that implements offline-first pattern with local caching and sync
 */
class OfflineFirstRepository(
    private val localCacheRepository: LocalCacheRepository,
    private val networkMonitor: NetworkMonitor
) {
    private val syncMutex = Mutex()
    private val pendingSyncOperations = mutableSetOf<com.carecomms.data.sync.SyncOperation>()
    
    /**
     * Get data with offline-first approach
     * Returns cached data immediately, then updates with fresh data when online
     */
    fun <T> getDataOfflineFirst(
        cacheKey: String,
        networkFetch: suspend () -> Result<T>,
        cacheStore: suspend (T) -> Unit,
        cacheRetrieve: suspend () -> T?
    ): Flow<Result<T>> = flow {
        // First emit cached data if available
        val cachedData = cacheRetrieve()
        if (cachedData != null) {
            emit(Result.success(cachedData))
        }
        
        // Then try to fetch fresh data if online
        networkMonitor.isOnline.collect { isOnline ->
            if (isOnline) {
                try {
                    val networkResult = networkFetch()
                    if (networkResult.isSuccess) {
                        val freshData = networkResult.getOrThrow()
                        cacheStore(freshData)
                        emit(Result.success(freshData))
                    } else if (cachedData == null) {
                        // Only emit network error if no cached data available
                        emit(networkResult)
                    }
                } catch (e: Exception) {
                    if (cachedData == null) {
                        emit(Result.failure(e))
                    }
                }
            } else if (cachedData == null) {
                emit(Result.failure(OfflineException("No cached data available and device is offline")))
            }
        }
    }
    
    /**
     * Perform write operation with offline support
     * Queues operation for sync when offline
     */
    suspend fun performWriteOperation(
        operation: com.carecomms.data.sync.SyncOperation,
        localUpdate: suspend () -> Unit,
        networkUpdate: suspend () -> Result<Unit>
    ): Result<Unit> {
        return try {
            // Always perform local update first
            localUpdate()
            
            networkMonitor.isOnline.first().let { isOnline ->
                if (isOnline) {
                    // Try network update immediately if online
                    val result = networkUpdate()
                    if (result.isFailure) {
                        // Queue for retry if network update fails
                        queueSyncOperation(operation)
                    }
                    result
                } else {
                    // Queue for sync when back online
                    queueSyncOperation(operation)
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sync pending operations when coming back online
     */
    suspend fun syncPendingOperations() {
        syncMutex.withLock {
            val operationsToSync = pendingSyncOperations.toList()
            val successfulOperations = mutableSetOf<com.carecomms.data.sync.SyncOperation>()
            
            for (operation in operationsToSync) {
                try {
                    val result = executeSyncOperation(operation)
                    if (result.isSuccess) {
                        successfulOperations.add(operation)
                    }
                } catch (e: Exception) {
                    // Log error but continue with other operations
                    println("Sync operation failed: ${operation.id} - ${e.message}")
                }
            }
            
            // Remove successful operations from pending queue
            pendingSyncOperations.removeAll(successfulOperations)
        }
    }
    
    /**
     * Get cached data for offline access
     */
    suspend fun getCachedMessages(chatId: String): List<Message> {
        return localCacheRepository.getCachedMessages(chatId)
    }
    
    /**
     * Cache messages for offline access
     */
    suspend fun cacheMessages(chatId: String, messages: List<Message>) {
        localCacheRepository.cacheMessages(chatId, messages)
    }
    
    /**
     * Get cached chat previews
     */
    suspend fun getCachedChatPreviews(carerId: String): List<ChatPreview> {
        return localCacheRepository.getCachedChatPreviews(carerId)
    }
    
    /**
     * Cache chat previews
     */
    suspend fun cacheChatPreviews(carerId: String, previews: List<ChatPreview>) {
        localCacheRepository.cacheChatPreviews(carerId, previews)
    }
    
    /**
     * Check if data is stale and needs refresh
     */
    suspend fun isDataStale(cacheKey: String, maxAgeMillis: Long = DEFAULT_CACHE_AGE): Boolean {
        return localCacheRepository.isDataStale(cacheKey, maxAgeMillis)
    }
    
    /**
     * Clear expired cache entries
     */
    suspend fun clearExpiredCache() {
        localCacheRepository.clearExpiredCache()
    }
    
    /**
     * Get network connectivity status
     */
    fun getNetworkStatus(): Flow<Boolean> = networkMonitor.isOnline
    
    private suspend fun queueSyncOperation(operation: com.carecomms.data.sync.SyncOperation) {
        syncMutex.withLock {
            pendingSyncOperations.add(operation)
        }
    }
    
    private suspend fun executeSyncOperation(operation: com.carecomms.data.sync.SyncOperation): Result<Unit> {
        return when (operation.type) {
            com.carecomms.data.sync.SyncOperationType.SEND_MESSAGE -> {
                // Implementation would depend on specific repository
                Result.success(Unit)
            }
            com.carecomms.data.sync.SyncOperationType.UPDATE_USER -> {
                // Implementation would depend on specific repository
                Result.success(Unit)
            }
            com.carecomms.data.sync.SyncOperationType.ACCEPT_INVITATION -> {
                // Implementation would depend on specific repository
                Result.success(Unit)
            }
        }
    }
    
    companion object {
        private const val DEFAULT_CACHE_AGE = 5 * 60 * 1000L // 5 minutes
    }
}

// Legacy sync operation for backward compatibility
data class SyncOperation(
    val id: String,
    val type: SyncOperationType,
    val data: Map<String, Any>,
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)

enum class SyncOperationType {
    SEND_MESSAGE,
    UPDATE_USER,
    ACCEPT_INVITATION
}

class OfflineException(message: String) : Exception(message)