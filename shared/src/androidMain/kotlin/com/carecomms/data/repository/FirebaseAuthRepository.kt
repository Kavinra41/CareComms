package com.carecomms.data.repository

import com.carecomms.data.models.*
import com.carecomms.data.storage.SecureStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val secureStorage: SecureStorage,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : AuthRepository {

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthResult> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.failure(AuthError.UserNotFound)
            
            val token = firebaseUser.getIdToken(false).await().token ?: ""
            val user = mapFirebaseUserToUser(firebaseUser)
            
            // Store tokens securely
            secureStorage.storeToken("auth_token", token)
            secureStorage.storeToken("user_id", user.id)
            secureStorage.storeToken("user_email", user.email)
            
            Result.success(AuthResult(user, token))
        } catch (e: FirebaseAuthException) {
            Result.failure(mapFirebaseException(e))
        } catch (e: Exception) {
            Result.failure(AuthError.NetworkError)
        }
    }

    override suspend fun signUpCarer(carerData: CarerRegistrationData): Result<AuthResult> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(
                carerData.email, 
                carerData.password
            ).await()
            
            val firebaseUser = authResult.user ?: return Result.failure(AuthError.UnknownError("User creation failed"))
            val token = firebaseUser.getIdToken(false).await().token ?: ""
            
            val carer = Carer(
                id = firebaseUser.uid,
                email = carerData.email,
                createdAt = Clock.System.now().toEpochMilliseconds(),
                documents = carerData.documents,
                age = carerData.age,
                phoneNumber = carerData.phoneNumber,
                location = carerData.location,
                careeIds = emptyList()
            )
            
            // Store user data and tokens
            secureStorage.storeToken("auth_token", token)
            secureStorage.storeToken("user_id", carer.id)
            secureStorage.storeToken("user_email", carer.email)
            
            Result.success(AuthResult(carer, token))
        } catch (e: FirebaseAuthException) {
            Result.failure(mapFirebaseException(e))
        } catch (e: Exception) {
            Result.failure(AuthError.NetworkError)
        }
    }

    override suspend fun signUpCaree(careeData: CareeRegistrationData, invitationToken: String): Result<AuthResult> {
        return try {
            // First validate the invitation token
            val carerInfo = validateInvitationToken(invitationToken).getOrElse { 
                return Result.failure(it as AuthError)
            }
            
            val authResult = firebaseAuth.createUserWithEmailAndPassword(
                careeData.email, 
                careeData.password
            ).await()
            
            val firebaseUser = authResult.user ?: return Result.failure(AuthError.UnknownError("User creation failed"))
            val token = firebaseUser.getIdToken(false).await().token ?: ""
            
            val caree = Caree(
                id = firebaseUser.uid,
                email = careeData.email,
                createdAt = Clock.System.now().toEpochMilliseconds(),
                healthInfo = careeData.healthInfo,
                personalDetails = careeData.basicDetails,
                carerId = carerInfo.id
            )
            
            // Store user data and tokens
            secureStorage.storeToken("auth_token", token)
            secureStorage.storeToken("user_id", caree.id)
            secureStorage.storeToken("user_email", caree.email)
            
            Result.success(AuthResult(caree, token))
        } catch (e: FirebaseAuthException) {
            Result.failure(mapFirebaseException(e))
        } catch (e: Exception) {
            Result.failure(AuthError.NetworkError)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            secureStorage.clearAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(AuthError.UnknownError(e.message ?: "Sign out failed"))
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return null
            mapFirebaseUserToUser(firebaseUser)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun validateInvitationToken(token: String): Result<CarerInfo> {
        return try {
            // For now, we'll implement a simple token validation
            // In a real implementation, this would validate against a backend service
            if (token.length < 10) {
                return Result.failure(AuthError.InvalidInvitationToken)
            }
            
            // Mock validation - in real implementation, decode JWT or validate with backend
            val mockCarerInfo = CarerInfo(
                id = "mock_carer_id",
                name = "Mock Carer",
                email = "carer@example.com"
            )
            
            Result.success(mockCarerInfo)
        } catch (e: Exception) {
            Result.failure(AuthError.InvalidInvitationToken)
        }
    }

    override suspend fun generateInvitationToken(carerId: String): Result<String> {
        return try {
            // Generate a simple invitation token
            // In a real implementation, this would be a JWT or secure token from backend
            val timestamp = Clock.System.now().toEpochMilliseconds()
            val token = "invite_${carerId}_$timestamp"
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(AuthError.UnknownError(e.message ?: "Token generation failed"))
        }
    }

    override suspend fun refreshToken(): Result<String> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return Result.failure(AuthError.UserNotFound)
            val token = firebaseUser.getIdToken(true).await().token ?: ""
            secureStorage.storeToken("auth_token", token)
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(AuthError.NetworkError)
        }
    }

    override fun isUserLoggedIn(): Flow<Boolean> = flow {
        emit(firebaseAuth.currentUser != null)
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return Result.failure(AuthError.UserNotFound)
            firebaseUser.delete().await()
            secureStorage.clearAll()
            Result.success(Unit)
        } catch (e: FirebaseAuthException) {
            Result.failure(mapFirebaseException(e))
        } catch (e: Exception) {
            Result.failure(AuthError.NetworkError)
        }
    }

    private suspend fun mapFirebaseUserToUser(firebaseUser: FirebaseUser): User {
        // This is a simplified mapping - in a real app, you'd fetch additional user data
        // from Firestore or another database
        return Carer(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            createdAt = firebaseUser.metadata?.creationTimestamp ?: Clock.System.now().toEpochMilliseconds(),
            documents = emptyList(),
            age = 0,
            phoneNumber = "",
            location = "",
            careeIds = emptyList()
        )
    }

    private fun mapFirebaseException(exception: FirebaseAuthException): AuthError {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> AuthError.InvalidEmail
            "ERROR_WRONG_PASSWORD" -> AuthError.InvalidCredentials
            "ERROR_USER_NOT_FOUND" -> AuthError.UserNotFound
            "ERROR_EMAIL_ALREADY_IN_USE" -> AuthError.EmailAlreadyInUse
            "ERROR_WEAK_PASSWORD" -> AuthError.WeakPassword
            "ERROR_NETWORK_REQUEST_FAILED" -> AuthError.NetworkError
            else -> AuthError.UnknownError(exception.message ?: "Authentication failed")
        }
    }
}