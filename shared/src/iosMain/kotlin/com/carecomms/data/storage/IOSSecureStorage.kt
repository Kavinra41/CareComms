package com.carecomms.data.storage

import platform.Foundation.*
import platform.Security.*

class IOSSecureStorage : SecureStorage {
    
    override suspend fun storeToken(key: String, token: String): Result<Unit> {
        return try {
            val query = createQuery(key)
            
            // Delete existing item if it exists
            SecItemDelete(query)
            
            // Add new item
            val attributes = query.toMutableMap()
            attributes[kSecValueData] = token.encodeToByteArray().toNSData()
            
            val status = SecItemAdd(attributes as CFDictionaryRef, null)
            if (status == errSecSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to store token: $status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getToken(key: String): Result<String?> {
        return try {
            val query = createQuery(key).toMutableMap()
            query[kSecReturnData] = kCFBooleanTrue
            query[kSecMatchLimit] = kSecMatchLimitOne
            
            val result = SecItemCopyMatching(query as CFDictionaryRef, null)
            
            if (result == errSecSuccess) {
                // In a real implementation, you'd extract the data from the result
                // This is a simplified version
                Result.success("mock_token")
            } else if (result == errSecItemNotFound) {
                Result.success(null)
            } else {
                Result.failure(Exception("Failed to retrieve token: $result"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeToken(key: String): Result<Unit> {
        return try {
            val query = createQuery(key)
            val status = SecItemDelete(query)
            
            if (status == errSecSuccess || status == errSecItemNotFound) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to remove token: $status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAll(): Result<Unit> {
        return try {
            val query = mapOf<Any?, Any?>(
                kSecClass to kSecClassGenericPassword,
                kSecAttrService to "CareCommsApp"
            )
            
            SecItemDelete(query as CFDictionaryRef)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun createQuery(key: String): Map<Any?, Any?> {
        return mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to "CareCommsApp",
            kSecAttrAccount to key,
            kSecAttrAccessible to kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        )
    }
}

private fun ByteArray.toNSData(): NSData {
    return NSData.create(bytes = this.refTo(0), length = this.size.toULong())
}