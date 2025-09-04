package com.carecomms.data.repository

import com.carecomms.data.database.DatabaseManager
import com.carecomms.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface LocalUserRepository {
    suspend fun insertUser(user: User): Result<Unit>
    suspend fun getUserById(id: String): User?
    suspend fun getUserByEmail(email: String): User?
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun deleteUser(id: String): Result<Unit>
    fun getAllUsersFlow(): Flow<List<User>>
    suspend fun getCarers(): List<Carer>
    suspend fun getCarees(): List<Caree>
}

class LocalUserRepositoryImpl(
    private val databaseManager: DatabaseManager,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : LocalUserRepository {

    override suspend fun insertUser(user: User): Result<Unit> {
        return try {
            val userType = when (user) {
                is Carer -> "CARER"
                is Caree -> "CAREE"
            }
            
            val userData = json.encodeToString(user)
            
            databaseManager.insertUser(
                id = user.id,
                email = user.email,
                userType = userType,
                createdAt = user.createdAt,
                data = userData
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(id: String): User? {
        return try {
            val dbUser = databaseManager.getUserById(id) ?: return null
            deserializeUser(dbUser)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getUserByEmail(email: String): User? {
        return try {
            val dbUser = databaseManager.getUserByEmail(email) ?: return null
            deserializeUser(dbUser)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val userData = json.encodeToString(user)
            databaseManager.updateUser(user.id, user.email, userData)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(id: String): Result<Unit> {
        return try {
            databaseManager.deleteUser(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllUsersFlow(): Flow<List<User>> {
        return databaseManager.getAllUsersFlow().map { dbUsers ->
            dbUsers.mapNotNull { dbUser ->
                try {
                    deserializeUser(dbUser)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    override suspend fun getCarers(): List<Carer> {
        return try {
            databaseManager.getAllUsersFlow().map { dbUsers ->
                dbUsers.filter { it.userType == "CARER" }
                    .mapNotNull { dbUser ->
                        try {
                            deserializeUser(dbUser) as? Carer
                        } catch (e: Exception) {
                            null
                        }
                    }
            }.let { flow ->
                // For this implementation, we'll collect the first emission
                // In a real app, you might want to return a Flow<List<Carer>>
                kotlinx.coroutines.flow.first(flow)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCarees(): List<Caree> {
        return try {
            databaseManager.getAllUsersFlow().map { dbUsers ->
                dbUsers.filter { it.userType == "CAREE" }
                    .mapNotNull { dbUser ->
                        try {
                            deserializeUser(dbUser) as? Caree
                        } catch (e: Exception) {
                            null
                        }
                    }
            }.let { flow ->
                // For this implementation, we'll collect the first emission
                kotlinx.coroutines.flow.first(flow)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun deserializeUser(dbUser: com.carecomms.database.User): User? {
        return try {
            when (dbUser.userType) {
                "CARER" -> json.decodeFromString<Carer>(dbUser.data)
                "CAREE" -> json.decodeFromString<Caree>(dbUser.data)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}