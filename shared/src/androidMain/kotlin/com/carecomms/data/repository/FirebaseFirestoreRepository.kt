package com.carecomms.data.repository

import com.carecomms.data.models.SimpleUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseFirestoreRepository(
    private val firestore: FirebaseFirestore
) : FirestoreRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun saveUser(user: SimpleUser): Result<Unit> {
        return try {
            println("Attempting to save user to Firestore: ${user.uid}")
            val userMap = mapOf(
                "uid" to user.uid,
                "email" to user.email,
                "name" to user.name,
                "phoneNumber" to user.phoneNumber,
                "city" to user.city,
                "createdAt" to user.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            
            println("User data map: $userMap")
            usersCollection.document(user.uid).set(userMap).await()
            println("User successfully saved to Firestore collection 'users'")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Firestore save error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getUser(userId: String): Result<SimpleUser?> {
        return try {
            val document = usersCollection.document(userId).get().await()
            
            if (document.exists()) {
                val data = document.data
                val user = SimpleUser(
                    uid = data?.get("uid") as? String ?: userId,
                    email = data?.get("email") as? String ?: "",
                    name = data?.get("name") as? String ?: "",
                    phoneNumber = data?.get("phoneNumber") as? String ?: "",
                    city = data?.get("city") as? String ?: "",
                    createdAt = data?.get("createdAt") as? Long ?: System.currentTimeMillis()
                )
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: SimpleUser): Result<Unit> {
        return try {
            val userMap = mapOf(
                "email" to user.email,
                "name" to user.name,
                "phoneNumber" to user.phoneNumber,
                "city" to user.city,
                "updatedAt" to System.currentTimeMillis()
            )
            
            usersCollection.document(user.uid).update(userMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllUsers(): Result<List<SimpleUser>> {
        return try {
            val querySnapshot = usersCollection.get().await()
            val users = querySnapshot.documents.mapNotNull { document ->
                val data = document.data
                if (data != null) {
                    SimpleUser(
                        uid = data["uid"] as? String ?: document.id,
                        email = data["email"] as? String ?: "",
                        name = data["name"] as? String ?: "",
                        phoneNumber = data["phoneNumber"] as? String ?: "",
                        city = data["city"] as? String ?: "",
                        createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis()
                    )
                } else null
            }
            Result.success(users)
        } catch (e: Exception) {
            println("Firestore getAllUsers error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Chat operations removed - now handled by Room database
}