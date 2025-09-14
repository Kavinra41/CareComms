package com.carecomms.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidSecureStorage(private val context: Context) : SecureStorage {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "carecomms_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    override suspend fun store(key: String, value: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.edit()
                .putString(key, value)
                .apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to store secure data: ${e.message}", e))
        }
    }
    
    override suspend fun retrieve(key: String): Result<String?> = withContext(Dispatchers.IO) {
        try {
            val value = sharedPreferences.getString(key, null)
            Result.success(value)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to retrieve secure data: ${e.message}", e))
        }
    }
    
    override suspend fun delete(key: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.edit()
                .remove(key)
                .apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to delete secure data: ${e.message}", e))
        }
    }
    
    override suspend fun clear(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.edit()
                .clear()
                .apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to clear secure data: ${e.message}", e))
        }
    }
    
    override suspend fun exists(key: String): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.contains(key)
    }
}