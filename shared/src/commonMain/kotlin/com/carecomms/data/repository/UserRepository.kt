package com.carecomms.data.repository

import com.carecomms.data.models.SimpleUser

interface UserRepository {
    suspend fun saveUser(user: SimpleUser): Result<Unit>
    suspend fun getUser(userId: String): Result<SimpleUser?>
    suspend fun getAllUsers(): Result<List<SimpleUser>>
    suspend fun updateUser(user: SimpleUser): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
}