package com.carecomms.domain.repository

import com.carecomms.data.models.User
import com.carecomms.data.models.CarerRegistrationData
import com.carecomms.data.models.CareeRegistrationData
import com.carecomms.data.models.InvitationData
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signUpCarer(carerData: CarerRegistrationData): Result<User>
    suspend fun signUpCaree(careeData: CareeRegistrationData, invitationToken: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun validateInvitationToken(token: String): Result<InvitationData>
    fun getCurrentUserFlow(): Flow<User?>
}