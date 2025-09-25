package com.carecomms.data.repository

import com.carecomms.data.models.AuthResult
import com.carecomms.data.models.SimpleUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestoreRepository: FirestoreRepository
) : AuthRepository {

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                // Try to get user from Firestore first
                val firestoreResult = firestoreRepository.getUser(firebaseUser.uid)
                val user = firestoreResult.getOrNull() ?: firebaseUser.toSimpleUser()
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Authentication failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun signUpWithEmail(
        email: String, 
        password: String, 
        name: String, 
        phoneNumber: String, 
        city: String
    ): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                val user = SimpleUser(
                    uid = firebaseUser.uid,
                    email = email,
                    name = name,
                    phoneNumber = phoneNumber,
                    city = city,
                    createdAt = System.currentTimeMillis()
                )
                
                // Save user to Firestore
                val saveResult = firestoreRepository.saveUser(user)
                if (saveResult.isFailure) {
                    val error = saveResult.exceptionOrNull()
                    println("Firestore save error: ${error?.message}")
                    error?.printStackTrace()
                    return AuthResult.Error("Failed to save user data: ${error?.message}")
                }
                
                println("User successfully saved to Firestore: ${user.uid}")
                
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Registration failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun getCurrentUser(): SimpleUser? {
        val firebaseUser = firebaseAuth.currentUser
        return if (firebaseUser != null) {
            // Try to get user from Firestore first
            val firestoreResult = firestoreRepository.getUser(firebaseUser.uid)
            firestoreResult.getOrNull() ?: firebaseUser.toSimpleUser()
        } else {
            null
        }
    }

    override suspend fun isUserSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    private fun FirebaseUser.toSimpleUser(): SimpleUser {
        return SimpleUser(
            uid = uid,
            email = email ?: "",
            name = displayName ?: "",
            phoneNumber = "",
            city = "",
            createdAt = System.currentTimeMillis()
        )
    }
}