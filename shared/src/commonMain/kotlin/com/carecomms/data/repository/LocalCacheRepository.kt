package com.carecomms.data.repository

import com.carecomms.data.database.DatabaseManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface LocalCacheRepository {
    suspend fun put(key: String, value: String, expirationTimeMillis: Long? = null): Result<Unit>
    suspend fun get(key: String): String?
    suspend fun remove(key: String): Result<Unit>
    suspend fun clearExpired(): Result<Unit>
    suspend fun clearAll(): Result<Unit>
    
    // Convenience methods for serializable objects
    suspend inline fun <reified T> putObject(key: String, value: T, expirationTimeMillis: Long? = null): Result<Unit>
    suspend inline fun <reified T> getObject(key: String): T?
    
    // Offline support methods
    suspend fun getCachedMessages(chatId: String): List<com.carecomms.data.models.Message>
    suspend fun cacheMessages(chatId: String, messages: List<com.carecomms.data.models.Message>)
    suspend fun getCachedChatPreviews(carerId: String): List<com.carecomms.data.models.ChatPreview>
    suspend fun cacheChatPreviews(carerId: String, previews: List<com.carecomms.data.models.ChatPreview>)
    suspend fun isDataStale(cacheKey: String, maxAgeMillis: Long): Boolean
    suspend fun clearExpiredCache()
    
    // Pending operations support
    suspend fun storePendingOperation(operation: com.carecomms.data.sync.PendingOperation)
    suspend fun getPendingOperations(): List<com.carecomms.data.sync.PendingOperation>
    suspend fun removePendingOperation(operationId: String)
    suspend fun clearPendingOperations()
}

class LocalCacheRepositoryImpl(
    private val databaseManager: DatabaseManager,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : LocalCacheRepository {

    override suspend fun put(key: String, value: String, expirationTimeMillis: Long?): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            databaseManager.insertCache(
                key = key,
                value = value,
                expirationTime = expirationTimeMillis,
                createdAt = currentTime
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun get(key: String): String? {
        return try {
            val currentTime = System.currentTimeMillis()
            databaseManager.getCache(key, currentTime)?.value
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun remove(key: String): Result<Unit> {
        return try {
            databaseManager.deleteCache(key)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearExpired(): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            databaseManager.clearExpiredCache(currentTime)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAll(): Result<Unit> {
        return try {
            databaseManager.clearAllCache()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend inline fun <reified T> putObject(key: String, value: T, expirationTimeMillis: Long?): Result<Unit> {
        return try {
            val jsonString = json.encodeToString(value)
            put(key, jsonString, expirationTimeMillis)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend inline fun <reified T> getObject(key: String): T? {
        return try {
            val jsonString = get(key) ?: return null
            json.decodeFromString<T>(jsonString)
        } catch (e: Exception) {
            null
        }
    }
    
    // Offline support methods
    override suspend fun getCachedMessages(chatId: String): List<com.carecomms.data.models.Message> {
        return getObject<List<com.carecomms.data.models.Message>>("messages_$chatId") ?: emptyList()
    }
    
    override suspend fun cacheMessages(chatId: String, messages: List<com.carecomms.data.models.Message>) {
        putObject("messages_$chatId", messages, System.currentTimeMillis() + CACHE_DURATION_MESSAGES)
    }
    
    override suspend fun getCachedChatPreviews(carerId: String): List<com.carecomms.data.models.ChatPreview> {
        return getObject<List<com.carecomms.data.models.ChatPreview>>("chat_previews_$carerId") ?: emptyList()
    }
    
    override suspend fun cacheChatPreviews(carerId: String, previews: List<com.carecomms.data.models.ChatPreview>) {
        putObject("chat_previews_$carerId", previews, System.currentTimeMillis() + CACHE_DURATION_CHAT_PREVIEWS)
    }
    
    override suspend fun isDataStale(cacheKey: String, maxAgeMillis: Long): Boolean {
        val cacheEntry = try {
            val currentTime = System.currentTimeMillis()
            databaseManager.getCache(cacheKey, currentTime)
        } catch (e: Exception) {
            return true
        }
        
        return if (cacheEntry != null) {
            val age = System.currentTimeMillis() - cacheEntry.createdAt
            age > maxAgeMillis
        } else {
            true
        }
    }
    
    override suspend fun clearExpiredCache() {
        clearExpired()
    }
    
    // Pending operations support
    override suspend fun storePendingOperation(operation: com.carecomms.data.sync.PendingOperation) {
        val operations = getPendingOperations().toMutableList()
        operations.removeAll { it.id == operation.id } // Remove existing operation with same ID
        operations.add(operation)
        putObject(PENDING_OPERATIONS_KEY, operations)
    }
    
    override suspend fun getPendingOperations(): List<com.carecomms.data.sync.PendingOperation> {
        return getObject<List<com.carecomms.data.sync.PendingOperation>>(PENDING_OPERATIONS_KEY) ?: emptyList()
    }
    
    override suspend fun removePendingOperation(operationId: String) {
        val operations = getPendingOperations().toMutableList()
        operations.removeAll { it.id == operationId }
        putObject(PENDING_OPERATIONS_KEY, operations)
    }
    
    override suspend fun clearPendingOperations() {
        remove(PENDING_OPERATIONS_KEY)
    }
    
    companion object {
        private const val CACHE_DURATION_MESSAGES = 24 * 60 * 60 * 1000L // 24 hours
        private const val CACHE_DURATION_CHAT_PREVIEWS = 30 * 60 * 1000L // 30 minutes
        private const val PENDING_OPERATIONS_KEY = "pending_operations"
    }
}