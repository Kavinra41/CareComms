package com.carecomms.data.utils

import com.carecomms.data.models.AppError
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class RetryMechanismTest {
    
    private val retryMechanism = RetryMechanism()
    
    @Test
    fun `executeWithRetry should succeed on first attempt`() = runTest {
        var attemptCount = 0
        
        val result = retryMechanism.executeWithRetry(
            maxRetries = 3,
            initialDelayMs = 10L
        ) {
            attemptCount++
            "success"
        }
        
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
        assertEquals(1, attemptCount)
    }
    
    @Test
    fun `executeWithRetry should retry on network error`() = runTest {
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
    fun `executeWithRetry should not retry on authentication error`() = runTest {
        var attemptCount = 0
        
        val result = retryMechanism.executeWithRetry(
            maxRetries = 3,
            initialDelayMs = 10L
        ) {
            attemptCount++
            throw AppError.AuthenticationError
        }
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.AuthenticationError)
        assertEquals(1, attemptCount)
    }
    
    @Test
    fun `executeWithRetry should fail after max retries`() = runTest {
        var attemptCount = 0
        
        val result = retryMechanism.executeWithRetry(
            maxRetries = 2,
            initialDelayMs = 10L
        ) {
            attemptCount++
            throw AppError.NetworkError
        }
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AppError.NetworkError)
        assertEquals(3, attemptCount) // Initial attempt + 2 retries
    }
    
    @Test
    fun `executeWithRetry should respect custom retry condition`() = runTest {
        var attemptCount = 0
        
        val result = retryMechanism.executeWithRetry(
            maxRetries = 3,
            initialDelayMs = 10L,
            retryCondition = { false } // Never retry
        ) {
            attemptCount++
            throw AppError.NetworkError
        }
        
        assertTrue(result.isFailure)
        assertEquals(1, attemptCount)
    }
    
    @Test
    fun `authRetryCondition should not retry authentication errors`() {
        assertFalse(retryMechanism.authRetryCondition(AppError.AuthenticationError))
        assertTrue(retryMechanism.authRetryCondition(AppError.NetworkError))
        assertTrue(retryMechanism.authRetryCondition(AppError.ServerError(500, "Internal Error")))
        assertFalse(retryMechanism.authRetryCondition(AppError.ServerError(400, "Bad Request")))
    }
    
    @Test
    fun `chatRetryCondition should not retry chat not found errors`() {
        assertFalse(retryMechanism.chatRetryCondition(AppError.ChatNotFoundError))
        assertTrue(retryMechanism.chatRetryCondition(AppError.NetworkError))
        assertTrue(retryMechanism.chatRetryCondition(AppError.ServerError(500, "Internal Error")))
        assertFalse(retryMechanism.chatRetryCondition(AppError.ServerError(404, "Not Found")))
    }
    
    @Test
    fun `invitationRetryCondition should not retry expired invitations`() {
        assertFalse(retryMechanism.invitationRetryCondition(AppError.InvitationExpiredError))
        assertFalse(retryMechanism.invitationRetryCondition(AppError.InvitationAlreadyUsedError))
        assertTrue(retryMechanism.invitationRetryCondition(AppError.NetworkError))
        assertTrue(retryMechanism.invitationRetryCondition(AppError.ServerError(500, "Internal Error")))
    }
}