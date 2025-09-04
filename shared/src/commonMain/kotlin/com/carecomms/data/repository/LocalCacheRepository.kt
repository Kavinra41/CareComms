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
}