package com.carecomms.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.cinterop.*
import platform.Foundation.*
import platform.Security.*

class IOSSecureStorage : SecureStorage {
    
    private val serviceName = "com.carecomms.securestorage"
    
    override suspend fun store(key: String, value: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val keyData = key.encodeToByteArray()
            val valueData = value.encodeToByteArray()
            
            // First, try to update existing item
            val updateQuery = CFDictionaryCreateMutable(null, 0, null, null)
            CFDictionarySetValue(updateQuery, kSecValueData, CFDataCreate(null, valueData.refTo(0), valueData.size.toLong()))
            
            val searchQuery = createKeychainQuery(key)
            
            val updateStatus = SecItemUpdate(searchQuery, updateQuery)
            
            if (updateStatus == errSecItemNotFound) {
                // Item doesn't exist, create new one
                val addQuery = createKeychainQuery(key)
                CFDictionarySetValue(addQuery, kSecValueData, CFDataCreate(null, valueData.refTo(0), valueData.size.toLong()))
                
                val addStatus = SecItemAdd(addQuery, null)
                if (addStatus != errSecSuccess) {
                    return@withContext Result.failure(SecurityException("Failed to store keychain item: $addStatus"))
                }
            } else if (updateStatus != errSecSuccess) {
                return@withContext Result.failure(SecurityException("Failed to update keychain item: $updateStatus"))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to store secure data: ${e.message}", e))
        }
    }
    
    override suspend fun retrieve(key: String): Result<String?> = withContext(Dispatchers.Default) {
        try {
            val query = createKeychainQuery(key)
            CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
            CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)
            
            val result = nativeHeap.alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, result.ptr)
            
            if (status == errSecSuccess) {
                val data = result.value as CFDataRef
                val length = CFDataGetLength(data).toInt()
                val bytes = CFDataGetBytePtr(data)
                val byteArray = ByteArray(length) { bytes!![it] }
                val value = byteArray.decodeToString()
                nativeHeap.free(result)
                Result.success(value)
            } else if (status == errSecItemNotFound) {
                nativeHeap.free(result)
                Result.success(null)
            } else {
                nativeHeap.free(result)
                Result.failure(SecurityException("Failed to retrieve keychain item: $status"))
            }
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to retrieve secure data: ${e.message}", e))
        }
    }
    
    override suspend fun delete(key: String): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val query = createKeychainQuery(key)
            val status = SecItemDelete(query)
            
            if (status == errSecSuccess || status == errSecItemNotFound) {
                Result.success(Unit)
            } else {
                Result.failure(SecurityException("Failed to delete keychain item: $status"))
            }
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to delete secure data: ${e.message}", e))
        }
    }
    
    override suspend fun clear(): Result<Unit> = withContext(Dispatchers.Default) {
        try {
            val query = CFDictionaryCreateMutable(null, 0, null, null)
            CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
            CFDictionarySetValue(query, kSecAttrService, CFStringCreateWithCString(null, serviceName, kCFStringEncodingUTF8))
            
            val status = SecItemDelete(query)
            
            if (status == errSecSuccess || status == errSecItemNotFound) {
                Result.success(Unit)
            } else {
                Result.failure(SecurityException("Failed to clear keychain: $status"))
            }
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to clear secure data: ${e.message}", e))
        }
    }
    
    override suspend fun exists(key: String): Boolean = withContext(Dispatchers.Default) {
        val query = createKeychainQuery(key)
        CFDictionarySetValue(query, kSecReturnData, kCFBooleanFalse)
        
        val status = SecItemCopyMatching(query, null)
        status == errSecSuccess
    }
    
    private fun createKeychainQuery(key: String): CFMutableDictionaryRef {
        val query = CFDictionaryCreateMutable(null, 0, null, null)
        CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(query, kSecAttrService, CFStringCreateWithCString(null, serviceName, kCFStringEncodingUTF8))
        CFDictionarySetValue(query, kSecAttrAccount, CFStringCreateWithCString(null, key, kCFStringEncodingUTF8))
        CFDictionarySetValue(query, kSecAttrAccessible, kSecAttrAccessibleWhenUnlockedThisDeviceOnly)
        return query
    }
}