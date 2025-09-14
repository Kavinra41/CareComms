package com.carecomms.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory
import android.util.Base64

class AndroidEncryptionManager : EncryptionManager {
    
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keyAlias = "CareCommsEncryptionKey"
    
    override suspend fun encrypt(data: String, key: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            
            // Combine IV and encrypted data
            val combined = iv + encryptedData
            val encoded = Base64.encodeToString(combined, Base64.DEFAULT)
            
            Result.success(encoded)
        } catch (e: Exception) {
            Result.failure(SecurityException("Encryption failed: ${e.message}", e))
        }
    }
    
    override suspend fun decrypt(encryptedData: String, key: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val secretKey = getOrCreateSecretKey()
            val combined = Base64.decode(encryptedData, Base64.DEFAULT)
            
            // Extract IV and encrypted data
            val iv = combined.sliceArray(0..11) // GCM IV is 12 bytes
            val encrypted = combined.sliceArray(12 until combined.size)
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            val decryptedData = cipher.doFinal(encrypted)
            val result = String(decryptedData, Charsets.UTF_8)
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(SecurityException("Decryption failed: ${e.message}", e))
        }
    }
    
    override suspend fun generateKey(): String = withContext(Dispatchers.IO) {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val key = keyGenerator.generateKey()
        Base64.encodeToString(key.encoded, Base64.DEFAULT)
    }
    
    override suspend fun hashPassword(password: String, salt: String): String = withContext(Dispatchers.IO) {
        val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        Base64.encodeToString(hash, Base64.DEFAULT)
    }
    
    override suspend fun generateSalt(): String = withContext(Dispatchers.IO) {
        val salt = ByteArray(EncryptionUtils.SALT_SIZE)
        SecureRandom().nextBytes(salt)
        Base64.encodeToString(salt, Base64.DEFAULT)
    }
    
    private fun getOrCreateSecretKey(): SecretKey {
        return if (keyStore.containsAlias(keyAlias)) {
            keyStore.getKey(keyAlias, null) as SecretKey
        } else {
            createSecretKey()
        }
    }
    
    private fun createSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(EncryptionUtils.KEY_SIZE)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
}