package com.carecomms.domain.usecase

import com.carecomms.data.models.*
import com.carecomms.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthUseCase(
    private val authRepository: AuthRepository
) {
    
    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<AuthResult> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                return Result.failure(Exception("Email and password are required"))
            }
            
            authRepository.signInWithEmail(email.trim(), password)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sign out current user
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            authRepository.signOut()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): User? {
        return try {
            authRepository.getCurrentUser()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean {
        return getCurrentUser() != null
    }
    
    /**
     * Get authentication state as a flow
     */
    fun getAuthState(): Flow<AuthState> = flow {
        try {
            val user = getCurrentUser()
            if (user != null) {
                val userType = when (user) {
                    is Carer -> "carer"
                    is Caree -> "caree"
                }
                emit(AuthState.Authenticated(user.id, userType))
            } else {
                emit(AuthState.Unauthenticated)
            }
        } catch (e: Exception) {
            emit(AuthState.Error(e.message ?: "Authentication error"))
        }
    }
    
    /**
     * Validate email format
     */
    fun validateEmail(email: String): Result<String> {
        val trimmedEmail = email.trim()
        return when {
            trimmedEmail.isBlank() -> Result.failure(Exception("Email is required"))
            !isValidEmailFormat(trimmedEmail) -> Result.failure(Exception("Invalid email format"))
            else -> Result.success(trimmedEmail)
        }
    }
    
    /**
     * Validate password strength
     */
    fun validatePassword(password: String): Result<String> {
        return when {
            password.isBlank() -> Result.failure(Exception("Password is required"))
            password.length < 8 -> Result.failure(Exception("Password must be at least 8 characters"))
            !password.any { it.isDigit() } -> Result.failure(Exception("Password must contain at least one number"))
            !password.any { it.isLetter() } -> Result.failure(Exception("Password must contain at least one letter"))
            else -> Result.success(password)
        }
    }
    
    private fun isValidEmailFormat(email: String): Boolean {
        return email.contains("@") && email.contains(".") && email.length > 5
    }
}