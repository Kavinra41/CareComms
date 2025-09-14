package com.carecomms.data.utils

import com.carecomms.data.models.AppError
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.random.Random

/**
 * Retry mechanism with exponential backoff and jitter
 */
class RetryMechanism {
    
    /**
     * Execute operation with retry logic
     */
    suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000L,
        maxDelayMs: Long = 30000L,
        backoffMultiplier: Double = 2.0,
        jitterFactor: Double = 0.1,
        retryCondition: (Throwable) -> Boolean = ::defaultRetryCondition,
        operation: suspend () -> T
    ): Result<T> {
        var lastException: Throwable? = null
        
        repeat(maxRetries + 1) { attempt ->
            try {
                return Result.success(operation())
            } catch (e: Throwable) {
                lastException = e
                
                // Don't retry if condition is not met or it's the last attempt
                if (!retryCondition(e) || attempt == maxRetries) {
                    break
                }
                
                // Calculate delay with exponential backoff and jitter
                val baseDelay = (initialDelayMs * backoffMultiplier.pow(attempt)).toLong()
                val jitter = (baseDelay * jitterFactor * Random.nextDouble()).toLong()
                val delayMs = minOf(baseDelay + jitter, maxDelayMs)
                
                delay(delayMs)
            }
        }
        
        return Result.failure(lastException ?: Exception("Unknown error during retry"))
    }
    
    /**
     * Default retry condition - retry on network errors and server errors (5xx)
     */
    private fun defaultRetryCondition(throwable: Throwable): Boolean {
        return when (throwable) {
            is AppError.NetworkError -> true
            is AppError.ServerError -> throwable.code >= 500
            else -> false
        }
    }
    
    /**
     * Retry condition for authentication operations
     */
    fun authRetryCondition(throwable: Throwable): Boolean {
        return when (throwable) {
            is AppError.NetworkError -> true
            is AppError.ServerError -> throwable.code >= 500
            is AppError.AuthenticationError -> false // Don't retry auth errors
            else -> false
        }
    }
    
    /**
     * Retry condition for chat operations
     */
    fun chatRetryCondition(throwable: Throwable): Boolean {
        return when (throwable) {
            is AppError.NetworkError -> true
            is AppError.ServerError -> throwable.code >= 500
            is AppError.ChatNotFoundError -> false // Don't retry if chat doesn't exist
            else -> false
        }
    }
    
    /**
     * Retry condition for invitation operations
     */
    fun invitationRetryCondition(throwable: Throwable): Boolean {
        return when (throwable) {
            is AppError.NetworkError -> true
            is AppError.ServerError -> throwable.code >= 500
            is AppError.InvitationExpiredError -> false // Don't retry expired invitations
            is AppError.InvitationAlreadyUsedError -> false // Don't retry used invitations
            else -> false
        }
    }
}

/**
 * Extension function for easy retry usage
 */
suspend fun <T> retryOperation(
    maxRetries: Int = 3,
    initialDelayMs: Long = 1000L,
    retryCondition: (Throwable) -> Boolean = { it is AppError.NetworkError },
    operation: suspend () -> T
): Result<T> {
    return RetryMechanism().executeWithRetry(
        maxRetries = maxRetries,
        initialDelayMs = initialDelayMs,
        retryCondition = retryCondition,
        operation = operation
    )
}