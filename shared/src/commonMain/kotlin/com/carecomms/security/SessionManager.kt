package com.carecomms.security

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

/**
 * Manages user sessions with automatic refresh and security features
 */
class SessionManager(
    private val secureStorage: SecureStorage,
    private val encryptionManager: EncryptionManager
) {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.NotAuthenticated)
    val sessionState: Flow<SessionState> = _sessionState.asStateFlow()
    
    private val sessionTimeoutMinutes = 30L
    private val refreshThresholdMinutes = 5L
    
    suspend fun createSession(
        userId: String,
        authToken: String,
        refreshToken: String
    ): Result<Unit> {
        return try {
            val expiryTime = Clock.System.now().plus(sessionTimeoutMinutes.minutes)
            
            // Store encrypted session data
            secureStorage.store(SecureStorageKeys.USER_ID, userId)
            secureStorage.store(SecureStorageKeys.AUTH_TOKEN, authToken)
            secureStorage.store(SecureStorageKeys.REFRESH_TOKEN, refreshToken)
            secureStorage.store(SecureStorageKeys.SESSION_EXPIRY, expiryTime.toString())
            
            _sessionState.value = SessionState.Authenticated(
                userId = userId,
                expiryTime = expiryTime,
                needsRefresh = false
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to create session: ${e.message}", e))
        }
    }
    
    suspend fun validateSession(): Result<SessionValidation> {
        return try {
            val userId = secureStorage.retrieve(SecureStorageKeys.USER_ID).getOrNull()
            val authToken = secureStorage.retrieve(SecureStorageKeys.AUTH_TOKEN).getOrNull()
            val expiryTimeString = secureStorage.retrieve(SecureStorageKeys.SESSION_EXPIRY).getOrNull()
            
            if (userId == null || authToken == null || expiryTimeString == null) {
                _sessionState.value = SessionState.NotAuthenticated
                return Result.success(SessionValidation.Invalid)
            }
            
            val expiryTime = Instant.parse(expiryTimeString)
            val currentTime = Clock.System.now()
            
            when {
                currentTime > expiryTime -> {
                    _sessionState.value = SessionState.Expired
                    Result.success(SessionValidation.Expired)
                }
                currentTime > expiryTime.minus(refreshThresholdMinutes.minutes) -> {
                    _sessionState.value = SessionState.Authenticated(
                        userId = userId,
                        expiryTime = expiryTime,
                        needsRefresh = true
                    )
                    Result.success(SessionValidation.NeedsRefresh)
                }
                else -> {
                    _sessionState.value = SessionState.Authenticated(
                        userId = userId,
                        expiryTime = expiryTime,
                        needsRefresh = false
                    )
                    Result.success(SessionValidation.Valid)
                }
            }
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to validate session: ${e.message}", e))
        }
    }
    
    suspend fun refreshSession(newAuthToken: String, newRefreshToken: String): Result<Unit> {
        return try {
            val userId = secureStorage.retrieve(SecureStorageKeys.USER_ID).getOrNull()
                ?: return Result.failure(SecurityException("No active session to refresh"))
            
            val newExpiryTime = Clock.System.now().plus(sessionTimeoutMinutes.minutes)
            
            secureStorage.store(SecureStorageKeys.AUTH_TOKEN, newAuthToken)
            secureStorage.store(SecureStorageKeys.REFRESH_TOKEN, newRefreshToken)
            secureStorage.store(SecureStorageKeys.SESSION_EXPIRY, newExpiryTime.toString())
            
            _sessionState.value = SessionState.Authenticated(
                userId = userId,
                expiryTime = newExpiryTime,
                needsRefresh = false
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to refresh session: ${e.message}", e))
        }
    }
    
    suspend fun clearSession(): Result<Unit> {
        return try {
            secureStorage.clear()
            _sessionState.value = SessionState.NotAuthenticated
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to clear session: ${e.message}", e))
        }
    }
    
    suspend fun getCurrentUserId(): String? {
        return secureStorage.retrieve(SecureStorageKeys.USER_ID).getOrNull()
    }
    
    suspend fun getCurrentAuthToken(): String? {
        return secureStorage.retrieve(SecureStorageKeys.AUTH_TOKEN).getOrNull()
    }
    
    suspend fun getCurrentRefreshToken(): String? {
        return secureStorage.retrieve(SecureStorageKeys.REFRESH_TOKEN).getOrNull()
    }
}

sealed class SessionState {
    object NotAuthenticated : SessionState()
    object Expired : SessionState()
    data class Authenticated(
        val userId: String,
        val expiryTime: Instant,
        val needsRefresh: Boolean
    ) : SessionState()
}

enum class SessionValidation {
    Valid,
    Invalid,
    Expired,
    NeedsRefresh
}