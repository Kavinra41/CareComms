package com.carecomms.data

import com.carecomms.data.models.AppError
import com.carecomms.data.repository.OfflineException
import com.carecomms.data.sync.*
import com.carecomms.data.utils.RetryMechanism
import com.carecomms.presentation.error.ErrorHandler
import com.carecomms.presentation.error.ErrorState
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Verification test for error handling and offline support implementation
 */
class ErrorHandlingOfflineVerificationTest {
    
    @Test
    fun `verify retry mechanism works for network errors`() = runTest {
        val retryMechanism = RetryMechanism()
        var attemptCount = 0
        
        val result = retryMechanism.executeWithRetry(
            maxRetries = 2,
            initialDelayMs = 10L
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
    fun `verify error handler provides user-friendly messages`() = runTest {
        val errorHandler = ErrorHandler()
        
        // Test network error
        val networkError = errorHandler.handleError(AppError.NetworkError, "chat")
        assertTrue(networkError is ErrorState.Network)
        assertTrue(networkError.message.contains("Unable to send message"))
        assertTrue(networkError.canRetry)
        
        // Test offline error
        val offlineError = errorHandler.handleError(OfflineException("No connection"), "chat")
        assertTrue(offlineError is ErrorState.Offline)
        assertTrue(offlineError.message.contains("offline"))
        assertFalse(offlineError.canRetry)
        
        // Test authentication error
        val authError = errorHandler.handleError(AppError.AuthenticationError, "login")
        assertTrue(authError is ErrorState.Authentication)
        assertTrue(authError.message.contains("Invalid email or password"))
        assertFalse(authError.canRetry)
    }
    
    @Test
    fun `verify pending operation data structure`() {
        val operation = PendingOperation(
            id = "test-op",
            type = OperationType.SEND_MESSAGE,
            data = mapOf("message" to "Hello"),
            timestamp = System.currentTimeMillis(),
            retryCount = 0
        )
        
        assertEquals("test-op", operation.id)
        assertEquals(OperationType.SEND_MESSAGE, operation.type)
        assertEquals("Hello", operation.data["message"])
        assertEquals(0, operation.retryCount)
    }
    
    @Test
    fun `verify sync status states`() {
        val idleStatus = SyncStatus.Idle
        assertTrue(idleStatus is SyncStatus.Idle)
        
        val syncingStatus = SyncStatus.Syncing(remaining = 5, completed = 2, failed = 1)
        assertTrue(syncingStatus is SyncStatus.Syncing)
        assertEquals(5, syncingStatus.remaining)
        assertEquals(2, syncingStatus.completed)
        assertEquals(1, syncingStatus.failed)
        
        val completedStatus = SyncStatus.Completed(successful = 10, failed = 2)
        assertTrue(completedStatus is SyncStatus.Completed)
        assertEquals(10, completedStatus.successful)
        assertEquals(2, completedStatus.failed)
    }
    
    @Test
    fun `verify error state action texts are appropriate`() = runTest {
        val errorHandler = ErrorHandler()
        
        val networkError = errorHandler.handleError(AppError.NetworkError, "chat")
        assertEquals("Retry", networkError.actionText)
        
        val authError = errorHandler.handleError(AppError.AuthenticationError, "login")
        assertEquals("Sign In Again", authError.actionText)
        
        val invitationExpiredError = errorHandler.handleError(AppError.InvitationExpiredError, "invitation")
        assertEquals("Request New Invitation", invitationExpiredError.actionText)
        
        val offlineError = errorHandler.handleError(OfflineException("No connection"), "chat")
        assertEquals("Check Connection", offlineError.actionText)
    }
    
    @Test
    fun `verify retry conditions work correctly`() {
        val retryMechanism = RetryMechanism()
        
        // Network errors should be retried
        assertTrue(retryMechanism.authRetryCondition(AppError.NetworkError))
        
        // Authentication errors should not be retried
        assertFalse(retryMechanism.authRetryCondition(AppError.AuthenticationError))
        
        // Server errors 5xx should be retried
        assertTrue(retryMechanism.authRetryCondition(AppError.ServerError(500, "Internal Error")))
        
        // Client errors 4xx should not be retried
        assertFalse(retryMechanism.authRetryCondition(AppError.ServerError(400, "Bad Request")))
        
        // Chat not found should not be retried
        assertFalse(retryMechanism.chatRetryCondition(AppError.ChatNotFoundError))
        
        // Expired invitations should not be retried
        assertFalse(retryMechanism.invitationRetryCondition(AppError.InvitationExpiredError))
    }
    
    @Test
    fun `verify operation types are defined correctly`() {
        val sendMessage = OperationType.SEND_MESSAGE
        val updateProfile = OperationType.UPDATE_PROFILE
        val acceptInvitation = OperationType.ACCEPT_INVITATION
        val generic = OperationType.GENERIC
        
        assertNotNull(sendMessage)
        assertNotNull(updateProfile)
        assertNotNull(acceptInvitation)
        assertNotNull(generic)
    }
}