package com.carecomms.data.storage

interface SecureStorage {
    suspend fun storeToken(key: String, token: String): Result<Unit>
    suspend fun getToken(key: String): Result<String?>
    suspend fun removeToken(key: String): Result<Unit>
    suspend fun clearAll(): Result<Unit>
}

object SecureStorageKeys {
    const val AUTH_TOKEN = "auth_token"
    const val REFRESH_TOKEN = "refresh_token"
    const val USER_ID = "user_id"
    const val USER_EMAIL = "user_email"
}