package com.carecomms.security

interface SecureStorage {
    suspend fun store(key: String, value: String)
    suspend fun retrieve(key: String): String?
    suspend fun delete(key: String)
    suspend fun clear()
    suspend fun contains(key: String): Boolean
}