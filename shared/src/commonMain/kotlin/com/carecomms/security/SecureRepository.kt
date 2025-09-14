package com.carecomms.security

import com.carecomms.data.models.User
import com.carecomms.data.models.Caree
import com.carecomms.data.models.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository wrapper that provides encryption for sensitive data
 */
class SecureRepository(
    private val encryptionManager: EncryptionManager,
    private val secureStorage: SecureStorage
) {
    
    suspend fun storeEncryptedHealthData(userId: String, healthData: String): Result<Unit> {
        return try {
            val encryptionKey = getOrCreateEncryptionKey()
            val encryptedData = encryptionManager.encrypt(healthData, encryptionKey).getOrThrow()
            
            secureStorage.store("health_data_$userId", encryptedData)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to store encrypted health data: ${e.message}", e))
        }
    }
    
    suspend fun retrieveEncryptedHealthData(userId: String): Result<String?> {
        return try {
            val encryptionKey = getOrCreateEncryptionKey()
            val encryptedData = secureStorage.retrieve("health_data_$userId").getOrNull()
            
            if (encryptedData != null) {
                val decryptedData = encryptionManager.decrypt(encryptedData, encryptionKey).getOrThrow()
                Result.success(decryptedData)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to retrieve encrypted health data: ${e.message}", e))
        }
    }
    
    suspend fun encryptSensitiveUserData(user: User): Result<User> {
        return try {
            when (user) {
                is Caree -> {
                    val encryptionKey = getOrCreateEncryptionKey()
                    val encryptedHealthInfo = encryptionManager.encrypt(user.healthInfo, encryptionKey).getOrThrow()
                    
                    Result.success(user.copy(healthInfo = encryptedHealthInfo))
                }
                else -> Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to encrypt user data: ${e.message}", e))
        }
    }
    
    suspend fun decryptSensitiveUserData(user: User): Result<User> {
        return try {
            when (user) {
                is Caree -> {
                    val encryptionKey = getOrCreateEncryptionKey()
                    val decryptedHealthInfo = encryptionManager.decrypt(user.healthInfo, encryptionKey).getOrThrow()
                    
                    Result.success(user.copy(healthInfo = decryptedHealthInfo))
                }
                else -> Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to decrypt user data: ${e.message}", e))
        }
    }
    
    suspend fun encryptMessage(message: Message): Result<Message> {
        return try {
            val encryptionKey = getOrCreateEncryptionKey()
            val encryptedContent = encryptionManager.encrypt(message.content, encryptionKey).getOrThrow()
            
            Result.success(message.copy(content = encryptedContent))
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to encrypt message: ${e.message}", e))
        }
    }
    
    suspend fun decryptMessage(message: Message): Result<Message> {
        return try {
            val encryptionKey = getOrCreateEncryptionKey()
            val decryptedContent = encryptionManager.decrypt(message.content, encryptionKey).getOrThrow()
            
            Result.success(message.copy(content = decryptedContent))
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to decrypt message: ${e.message}", e))
        }
    }
    
    fun encryptMessageFlow(messages: Flow<List<Message>>): Flow<List<Message>> {
        return messages.map { messageList ->
            messageList.map { message ->
                decryptMessage(message).getOrElse { message }
            }
        }
    }
    
    suspend fun clearEncryptedData(userId: String): Result<Unit> {
        return try {
            secureStorage.delete("health_data_$userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to clear encrypted data: ${e.message}", e))
        }
    }
    
    private suspend fun getOrCreateEncryptionKey(): String {
        val existingKey = secureStorage.retrieve(SecureStorageKeys.ENCRYPTION_KEY).getOrNull()
        
        return if (existingKey != null) {
            existingKey
        } else {
            val newKey = encryptionManager.generateKey()
            secureStorage.store(SecureStorageKeys.ENCRYPTION_KEY, newKey)
            newKey
        }
    }
}