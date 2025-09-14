package com.carecomms.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Interface for encryption operations across platforms
 */
interface EncryptionManager {
    suspend fun encrypt(data: String, key: String): Result<String>
    suspend fun decrypt(encryptedData: String, key: String): Result<String>
    suspend fun generateKey(): String
    suspend fun hashPassword(password: String, salt: String): String
    suspend fun generateSalt(): String
}

/**
 * Common encryption utilities
 */
object EncryptionUtils {
    const val KEY_SIZE = 256
    const val IV_SIZE = 16
    const val SALT_SIZE = 32
    
    fun validateEncryptionKey(key: String): Boolean {
        return key.isNotEmpty() && key.length >= 32
    }
    
    fun sanitizeHealthData(data: String): String {
        return data.trim()
            .replace(Regex("[<>\"'&]"), "")
            .take(1000) // Limit health data to 1000 characters
    }
    
    fun sanitizeUserInput(input: String): String {
        return input.trim()
            .replace(Regex("[<>\"'&;]"), "")
            .take(500)
    }
}