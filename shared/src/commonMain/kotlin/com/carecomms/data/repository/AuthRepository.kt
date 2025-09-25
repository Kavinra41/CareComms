package com.carecomms.data.repository

import com.carecomms.data.models.AuthResult
import com.carecomms.data.models.SimpleUser

interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): AuthResult
    suspend fun signUpWithEmail(email: String, password: String, name: String, phoneNumber: String, city: String): AuthResult
    suspend fun signOut()
    suspend fun getCurrentUser(): SimpleUser?
    suspend fun isUserSignedIn(): Boolean
}