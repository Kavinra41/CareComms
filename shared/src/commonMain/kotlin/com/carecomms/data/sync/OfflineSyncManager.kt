package com.carecomms.data.sync

import com.carecomms.data.models.*
import com.carecomms.data.repository.LocalCacheRepository
import com.carecomms.data.repository.NetworkMonitor
import com.carecomms.data.utils.RetryMechanism
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Manages offline operations and synchronization when connection is restored
 */
class OfflineSyncManager(
    private val localCacheRepository: LocalCacheRepository,
    private val networkMonitor: NetworkMonitor,
    private val retryMechanism: RetryMechanism,
    private val coroutineScope: CoroutineScope
) {
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    private val pendingOperations = mutableMapOf<String, PendingOperation>()
    private var syncJob: Job? = null
    
    init {
        // Start monitoring network status and sync when online
        coroutineScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline && pendingOperations.isNotEmpty()) {
                    startSync()
                }
            }
        }
    }
    
    /**
     * Queue an operation for offline execution
     */
    suspend fun queueOperation(operation: PendingOperation) {
        pendingOperations[operation.id] = operation
        
        // Store in local cache for persistence
        localCacheRepository.storePendingOperation(operation)
        
        // Try to sync immediately if online
        if (networkMonitor.isOnline.first()) {
            startSync()
        }
    }
    
    /**
     * Execute operation with offline support
     */
    suspend fun <T> executeWithOfflineSupport(
        operationId: String,
        localOperation: suspend () -> T,
        remoteOperation: suspend () -> Result<T>,
        onSuccess: suspend (T) -> Unit = {},
        onError: suspend (Throwable) -> Unit = {}
    ): Result<T> {
        return try {
            // Always execute local operation first
            val localResult = localOperation()
            
            if (networkMonitor.isOnline.first()) {
                // Try remote operation if online
                val remoteResult = remoteOperation()
                if (remoteResult.isSuccess) {
                    onSuccess(remoteResult.getOrThrow())
                    Result.success(localResult)
                } else {
                    // Queue for retry if remote fails
                    queueOperation(
                        PendingOperation(
                            id = operationId,
                            type = OperationType.GENERIC,
                            data = mapOf("operation" to "retry"),
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    Result.success(localResult)
                }
            } else {
                // Queue for sync when back online
                queueOperation(
                    PendingOperation(
                        id = operationId,
                        type = OperationType.GENERIC,
                        data = mapOf("operation" to "offline"),
                        timestamp = System.currentTimeMillis()
                    )
                )
                Result.success(localResult)
            }
        } catch (e: Exception) {
            onError(e)
            Result.failure(e)
        }
    }
    
    /**
     * Start synchronization process
     */
    private fun startSync() {
        if (syncJob?.isActive == true) return
        
        syncJob = coroutineScope.launch {
            _syncStatus.value = SyncStatus.Syncing(pendingOperations.size)
            
            try {
                val operationsToSync = pendingOperations.values.toList()
                var successCount = 0
                var failureCount = 0
                
                for (operation in operationsToSync) {
                    try {
                        val result = executePendingOperation(operation)
                        if (result.isSuccess) {
                            pendingOperations.remove(operation.id)
                            localCacheRepository.removePendingOperation(operation.id)
                            successCount++
                        } else {
                            failureCount++
                            // Update retry count
                            val updatedOperation = operation.copy(retryCount = operation.retryCount + 1)
                            if (updatedOperation.retryCount >= MAX_RETRY_COUNT) {
                                // Remove operation after max retries
                                pendingOperations.remove(operation.id)
                                localCacheRepository.removePendingOperation(operation.id)
                                failureCount++
                            } else {
                                pendingOperations[operation.id] = updatedOperation
                                localCacheRepository.storePendingOperation(updatedOperation)
                            }
                        }
                        
                        // Update sync status
                        _syncStatus.value = SyncStatus.Syncing(
                            remaining = pendingOperations.size,
                            completed = successCount,
                            failed = failureCount
                        )
                        
                    } catch (e: Exception) {
                        failureCount++
                        println("Sync operation failed: ${operation.id} - ${e.message}")
                    }
                }
                
                _syncStatus.value = if (pendingOperations.isEmpty()) {
                    SyncStatus.Completed(successCount, failureCount)
                } else {
                    SyncStatus.PartiallyCompleted(successCount, failureCount, pendingOperations.size)
                }
                
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.Failed(e.message ?: "Sync failed")
            }
        }
    }
    
    /**
     * Execute a pending operation
     */
    private suspend fun executePendingOperation(operation: PendingOperation): Result<Unit> {
        return retryMechanism.executeWithRetry(
            maxRetries = 2,
            initialDelayMs = 500L
        ) {
            when (operation.type) {
                OperationType.SEND_MESSAGE -> executeSendMessage(operation)
                OperationType.UPDATE_PROFILE -> executeUpdateProfile(operation)
                OperationType.ACCEPT_INVITATION -> executeAcceptInvitation(operation)
                OperationType.GENERIC -> executeGenericOperation(operation)
            }
        }
    }
    
    private suspend fun executeSendMessage(operation: PendingOperation): Unit {
        // Implementation would call the actual chat repository
        // This is a placeholder for the actual implementation
        delay(100) // Simulate network call
    }
    
    private suspend fun executeUpdateProfile(operation: PendingOperation): Unit {
        // Implementation would call the actual user repository
        delay(100) // Simulate network call
    }
    
    private suspend fun executeAcceptInvitation(operation: PendingOperation): Unit {
        // Implementation would call the actual invitation repository
        delay(100) // Simulate network call
    }
    
    private suspend fun executeGenericOperation(operation: PendingOperation): Unit {
        // Generic operation handler
        delay(100) // Simulate network call
    }
    
    /**
     * Load pending operations from local storage on app start
     */
    suspend fun loadPendingOperations() {
        val storedOperations = localCacheRepository.getPendingOperations()
        storedOperations.forEach { operation ->
            pendingOperations[operation.id] = operation
        }
        
        if (pendingOperations.isNotEmpty() && networkMonitor.isOnline.first()) {
            startSync()
        }
    }
    
    /**
     * Clear all pending operations (use with caution)
     */
    suspend fun clearPendingOperations() {
        pendingOperations.clear()
        localCacheRepository.clearPendingOperations()
        _syncStatus.value = SyncStatus.Idle
    }
    
    /**
     * Get count of pending operations
     */
    fun getPendingOperationCount(): Int = pendingOperations.size
    
    /**
     * Check if there are pending operations
     */
    fun hasPendingOperations(): Boolean = pendingOperations.isNotEmpty()
    
    companion object {
        private const val MAX_RETRY_COUNT = 5
    }
}

@Serializable
data class PendingOperation(
    val id: String,
    val type: OperationType,
    val data: Map<String, String>,
    val timestamp: Long,
    val retryCount: Int = 0
)

@Serializable
enum class OperationType {
    SEND_MESSAGE,
    UPDATE_PROFILE,
    ACCEPT_INVITATION,
    GENERIC
}

sealed class SyncStatus {
    object Idle : SyncStatus()
    
    data class Syncing(
        val remaining: Int,
        val completed: Int = 0,
        val failed: Int = 0
    ) : SyncStatus()
    
    data class Completed(
        val successful: Int,
        val failed: Int
    ) : SyncStatus()
    
    data class PartiallyCompleted(
        val successful: Int,
        val failed: Int,
        val remaining: Int
    ) : SyncStatus()
    
    data class Failed(val error: String) : SyncStatus()
}