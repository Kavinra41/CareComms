package com.carecomms.security

/**
 * Interface for secure storage operations
 */
interface SecureStorage {
    suspend fun store(key: String, value: String): Result<Unit>
    suspend fun retrieve(key: String): Result<String?>
    suspend fun delete(key: String): Result<Unit>
    suspend fun clear(): Result<Unit>
    suspend fun exists(key: String): Boolean
}

/**
 * Keys for secure storage
 */
object SecureStorageKeys {
    const val AUTH_TOKEN = "auth_token"
    const val REFRESH_TOKEN = "refresh_token"
    const val USER_ID = "user_id"
    const val ENCRYPTION_KEY = "encryption_key"
    const val SESSION_EXPIRY = "session_expiry"
    const val BIOMETRIC_KEY = "biometric_key"
}