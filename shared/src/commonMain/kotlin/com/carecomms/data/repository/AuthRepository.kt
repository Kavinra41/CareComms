package com.carecomms.data.repository

import com.carecomms.data.models.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): Result<AuthResult>
    suspend fun signUpCarer(carerData: CarerRegistrationData): Result<AuthResult>
    suspend fun signUpCaree(careeData: CareeRegistrationData, invitationToken: String): Result<AuthResult>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun validateInvitationToken(token: String): Result<CarerInfo>
    suspend fun generateInvitationToken(carerId: String): Result<String>
    suspend fun refreshToken(): Result<String>
    fun isUserLoggedIn(): Flow<Boolean>
    suspend fun deleteAccount(): Result<Unit>
}