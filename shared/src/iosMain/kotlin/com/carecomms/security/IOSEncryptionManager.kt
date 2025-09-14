package com.carecomms.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.cinterop.*
import platform.Foundation.*
import platform.Security.*
import platform.CommonCrypto.*

class IOSEncryptionManager : EncryptionManager {
    
    private val keyTag = "com.carecomms.encryptionkey"
    
    override suspend fun encrypt(data: String, key: String): Result<String> = withContext(Dispatchers.Default) {
        try {
            val keyData = NSString.create(string = key).dataUsingEncoding(NSUTF8StringEncoding)
                ?: return@withContext Result.failure(SecurityException("Invalid key"))
            
            val dataToEncrypt = NSString.create(string = data).dataUsingEncoding(NSUTF8StringEncoding)
                ?: return@withContext Result.failure(SecurityException("Invalid data"))
            
            // Generate random IV
            val iv = NSMutableData.dataWithLength(kCCBlockSizeAES128.toULong())!!
            SecRandomCopyBytes(kSecRandomDefault, kCCBlockSizeAES128.toULong(), iv.mutableBytes)
            
            // Encrypt data
            val encryptedData = NSMutableData.dataWithLength((dataToEncrypt.length + kCCBlockSizeAES128).toULong())!!
            val encryptedLength = ULongArray(1)
            
            val status = CCCrypt(
                kCCEncrypt.toUInt(),
                kCCAlgorithmAES.toUInt(),
                kCCOptionPKCS7Padding.toUInt(),
                keyData.bytes,
                keyData.length.toULong(),
                iv.bytes,
                dataToEncrypt.bytes,
                dataToEncrypt.length.toULong(),
                encryptedData.mutableBytes,
                encryptedData.length.toULong(),
                encryptedLength.refTo(0)
            )
            
            if (status == kCCSuccess) {
                encryptedData.length = encryptedLength[0].toULong()
                
                // Combine IV and encrypted data
                val combined = NSMutableData.dataWithData(iv)!!
                combined.appendData(encryptedData)
                
                val base64String = combined.base64EncodedStringWithOptions(0u)
                Result.success(base64String)
            } else {
                Result.failure(SecurityException("Encryption failed with status: $status"))
            }
        } catch (e: Exception) {
            Result.failure(SecurityException("Encryption failed: ${e.message}", e))
        }
    }
    
    override suspend fun decrypt(encryptedData: String, key: String): Result<String> = withContext(Dispatchers.Default) {
        try {
            val keyData = NSString.create(string = key).dataUsingEncoding(NSUTF8StringEncoding)
                ?: return@withContext Result.failure(SecurityException("Invalid key"))
            
            val combinedData = NSData.create(base64EncodedString = encryptedData, options = 0u)
                ?: return@withContext Result.failure(SecurityException("Invalid encrypted data"))
            
            // Extract IV and encrypted data
            val iv = combinedData.subdataWithRange(NSMakeRange(0u, kCCBlockSizeAES128.toULong()))
            val encrypted = combinedData.subdataWithRange(
                NSMakeRange(kCCBlockSizeAES128.toULong(), combinedData.length - kCCBlockSizeAES128.toULong())
            )
            
            // Decrypt data
            val decryptedData = NSMutableData.dataWithLength(encrypted.length.toULong())!!
            val decryptedLength = ULongArray(1)
            
            val status = CCCrypt(
                kCCDecrypt.toUInt(),
                kCCAlgorithmAES.toUInt(),
                kCCOptionPKCS7Padding.toUInt(),
                keyData.bytes,
                keyData.length.toULong(),
                iv.bytes,
                encrypted.bytes,
                encrypted.length.toULong(),
                decryptedData.mutableBytes,
                decryptedData.length.toULong(),
                decryptedLength.refTo(0)
            )
            
            if (status == kCCSuccess) {
                decryptedData.length = decryptedLength[0].toULong()
                val result = NSString.create(data = decryptedData, encoding = NSUTF8StringEncoding) as String
                Result.success(result)
            } else {
                Result.failure(SecurityException("Decryption failed with status: $status"))
            }
        } catch (e: Exception) {
            Result.failure(SecurityException("Decryption failed: ${e.message}", e))
        }
    }
    
    override suspend fun generateKey(): String = withContext(Dispatchers.Default) {
        val keyData = NSMutableData.dataWithLength(32u)!! // 256-bit key
        SecRandomCopyBytes(kSecRandomDefault, 32u, keyData.mutableBytes)
        keyData.base64EncodedStringWithOptions(0u)
    }
    
    override suspend fun hashPassword(password: String, salt: String): String = withContext(Dispatchers.Default) {
        val passwordData = NSString.create(string = password).dataUsingEncoding(NSUTF8StringEncoding)!!
        val saltData = NSString.create(string = salt).dataUsingEncoding(NSUTF8StringEncoding)!!
        
        val derivedKey = NSMutableData.dataWithLength(32u)!! // 256-bit key
        
        val status = CCKeyDerivationPBKDF(
            kCCPBKDF2.toUInt(),
            passwordData.bytes?.reinterpret<ByteVar>(),
            passwordData.length.toULong(),
            saltData.bytes?.reinterpret<UByteVar>(),
            saltData.length.toULong(),
            kCCPRFHmacAlgSHA256.toUInt(),
            10000u, // iterations
            derivedKey.mutableBytes?.reinterpret<UByteVar>(),
            derivedKey.length.toULong()
        )
        
        if (status == kCCSuccess) {
            derivedKey.base64EncodedStringWithOptions(0u)
        } else {
            throw SecurityException("Password hashing failed with status: $status")
        }
    }
    
    override suspend fun generateSalt(): String = withContext(Dispatchers.Default) {
        val saltData = NSMutableData.dataWithLength(EncryptionUtils.SALT_SIZE.toULong())!!
        SecRandomCopyBytes(kSecRandomDefault, EncryptionUtils.SALT_SIZE.toULong(), saltData.mutableBytes)
        saltData.base64EncodedStringWithOptions(0u)
    }
}