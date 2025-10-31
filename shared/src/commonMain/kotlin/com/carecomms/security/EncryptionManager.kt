package com.carecomms.security

interface EncryptionManager {
    suspend fun encrypt(data: String): String
    suspend fun decrypt(encryptedData: String): String
    suspend fun generateKey(): String
    suspend fun hashPassword(password: String): String
    suspend fun verifyPassword(password: String, hash: String): Boolean
}