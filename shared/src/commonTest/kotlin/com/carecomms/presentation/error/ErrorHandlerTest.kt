package com.carecomms.presentation.error

import com.carecomms.data.models.AppError
import com.carecomms.data.repository.OfflineException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class ErrorHandlerTest {
    
    private val errorHandler = ErrorHandler()
    
    @Test
    fun `handleError should convert network error to user-friendly message`() = runTest {
        val error = AppError.NetworkError
        val errorState = errorHandler.handleError(error, "chat")
        
        assertTrue(errorState is ErrorState.Network)
        assertTrue(errorState.message.contains("Unable to send message"))
        assertTrue(errorState.canRetry)
        assertEquals("chat", errorState.context)
        assertEquals("Retry", errorState.actionText)
    }
    
    @Test
    fun `handleError should convert authentication error with context`() = runTest {
        val error = AppError.AuthenticationError
        val errorState = errorHandler.handleError(error, "login")
        
        assertTrue(errorState is ErrorState.Authentication)
        assertTrue(errorState.message.contains("Invalid email or password"))
        assertFalse(errorState.canRetry)
        assertEquals("login", errorState.context)
        assertEquals("Sign In Again", errorState.actionText)
    }
    
    @Test
    fun `handleError should handle server errors with retry logic`() = runTest {
        val serverError = AppError.ServerError(500, "Internal Server Error")
        val errorState = errorHandler.handleError(serverError, "registration")
        
        assertTrue(errorState is ErrorState.Server)
        assertTrue(errorState.message.contains("Our servers are experiencing issues"))
        assertTrue(errorState.canRetry)
        assertEquals("Try Again", errorState.actionText)
    }
    
    @Test
    fun `handleError should not retry client errors`() = runTest {
        val clientError = AppError.ServerError(400, "Bad Request")
        val errorState = errorHandler.handleError(clientError, "registration")
        
        assertTrue(errorState is ErrorState.Server)
        assertFalse(errorState.canRetry)
        assertEquals("Contact Support", errorState.actionText)
    }
    
    @Test
    fun `handleError should handle offline exception`() = runTest {
        val offlineError = OfflineException("No connection")
        val errorState = errorHandler.handleError(offlineError, "chat")
        
        assertTrue(errorState is ErrorState.Offline)
        assertTrue(errorState.message.contains("You're offline"))
        assertTrue(errorState.message.contains("Messages will be sent when connection is restored"))
        assertFalse(errorState.canRetry)
        assertEquals("Check Connection", errorState.actionText)
    }
    
    @Test
    fun `handleError should handle invitation expired error`() = runTest {
        val expiredError = AppError.InvitationExpiredError
        val errorState = errorHandler.handleError(expiredError, "invitation")
        
        assertTrue(errorState is ErrorState.InvitationExpired)
        assertTrue(errorState.message.contains("invitation has expired"))
        assertFalse(errorState.canRetry)
        assertEquals("Request New Invitation", errorState.actionText)
    }
    
    @Test
    fun `handleError should handle invitation already used error`() = runTest {
        val usedError = AppError.InvitationAlreadyUsedError
        val errorState = errorHandler.handleError(usedError, "invitation")
        
        assertTrue(errorState is ErrorState.InvitationUsed)
        assertTrue(errorState.message.contains("already been used"))
        assertFalse(errorState.canRetry)
        assertEquals("Contact Carer", errorState.actionText)
    }
    
    @Test
    fun `handleError should handle user not found error`() = runTest {
        val userNotFoundError = AppError.UserNotFoundError
        val errorState = errorHandler.handleError(userNotFoundError, "login")
        
        assertTrue(errorState is ErrorState.UserNotFound)
        assertTrue(errorState.message.contains("User account not found"))
        assertFalse(errorState.canRetry)
        assertEquals("Create Account", errorState.actionText)
    }
    
    @Test
    fun `handleError should handle chat not found error`() = runTest {
        val chatNotFoundError = AppError.ChatNotFoundError
        val errorState = errorHandler.handleError(chatNotFoundError, "chat")
        
        assertTrue(errorState is ErrorState.ChatNotFound)
        assertTrue(errorState.message.contains("Chat conversation not found"))
        assertFalse(errorState.canRetry)
        assertEquals("Go Back", errorState.actionText)
    }
    
    @Test
    fun `handleError should emit error to flow`() = runTest {
        val error = AppError.NetworkError
        
        errorHandler.handleError(error, "test")
        
        val emittedError = errorHandler.errors.first()
        assertTrue(emittedError is ErrorState.Network)
        assertEquals("test", emittedError.context)
    }
    
    @Test
    fun `handleRecovery should emit recovery state`() = runTest {
        errorHandler.handleRecovery("test_context")
        
        val recoveryState = errorHandler.errors.first()
        assertTrue(recoveryState is ErrorState.Recovered)
        assertEquals("test_context", recoveryState.context)
        assertEquals("Connection restored", recoveryState.message)
    }
    
    @Test
    fun `clearErrors should emit cleared state`() = runTest {
        errorHandler.clearErrors()
        
        val clearedState = errorHandler.errors.first()
        assertTrue(clearedState is ErrorState.Cleared)
        assertEquals("", clearedState.message)
    }
    
    @Test
    fun `handleErrorWithRetry should retry on retryable errors`() = runTest {
        var attemptCount = 0
        
        val result = errorHandler.handleErrorWithRetry(
            error = AppError.NetworkError,
            context = "test"
        ) {
            attemptCount++
            if (attemptCount < 3) {
                throw AppError.NetworkError
            }
            "success"
        }
        
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
        assertEquals(3, attemptCount)
    }
    
    @Test
    fun `handleErrorWithRetry should not retry on non-retryable errors`() = runTest {
        var attemptCount = 0
        
        val result = errorHandler.handleErrorWithRetry(
            error = AppError.AuthenticationError,
            context = "test"
        ) {
            attemptCount++
            throw AppError.AuthenticationError
        }
        
        assertTrue(result.isFailure)
        assertEquals(1, attemptCount)
    }
}