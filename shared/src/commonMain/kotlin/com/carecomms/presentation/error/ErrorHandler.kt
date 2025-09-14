package com.carecomms.presentation.error

import com.carecomms.data.models.AppError
import com.carecomms.data.repository.OfflineException
import com.carecomms.data.utils.RetryMechanism
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Global error handler for the application with enhanced user-friendly messages
 */
class ErrorHandler(
    private val retryMechanism: RetryMechanism = RetryMechanism()
) {
    private val _errors = MutableSharedFlow<ErrorState>()
    val errors: SharedFlow<ErrorState> = _errors.asSharedFlow()
    
    /**
     * Handle different types of errors and convert to user-friendly messages
     */
    suspend fun handleError(error: Throwable, context: String = ""): ErrorState {
        val errorState = when (error) {
            is AppError.NetworkError -> ErrorState.Network(
                message = getNetworkErrorMessage(context),
                canRetry = true,
                context = context,
                actionText = "Retry"
            )
            
            is AppError.AuthenticationError -> ErrorState.Authentication(
                message = getAuthErrorMessage(context),
                canRetry = false,
                context = context,
                actionText = "Sign In Again"
            )
            
            is AppError.ValidationError -> ErrorState.Validation(
                message = getValidationErrorMessage(context),
                canRetry = false,
                context = context,
                actionText = "Fix Input"
            )
            
            is AppError.ServerError -> ErrorState.Server(
                message = getServerErrorMessage(error.code, error.message, context),
                canRetry = error.code >= 500,
                context = context,
                actionText = if (error.code >= 500) "Try Again" else "Contact Support"
            )
            
            is OfflineException -> ErrorState.Offline(
                message = getOfflineErrorMessage(context),
                canRetry = false,
                context = context,
                actionText = "Check Connection"
            )
            
            is AppError.InvitationExpiredError -> ErrorState.InvitationExpired(
                message = "This invitation has expired. Please request a new invitation from your carer.",
                canRetry = false,
                context = context,
                actionText = "Request New Invitation"
            )
            
            is AppError.InvitationAlreadyUsedError -> ErrorState.InvitationUsed(
                message = "This invitation has already been used. Please contact your carer if you need assistance.",
                canRetry = false,
                context = context,
                actionText = "Contact Carer"
            )
            
            is AppError.UserNotFoundError -> ErrorState.UserNotFound(
                message = "User account not found. Please check your credentials or create a new account.",
                canRetry = false,
                context = context,
                actionText = "Create Account"
            )
            
            is AppError.ChatNotFoundError -> ErrorState.ChatNotFound(
                message = "Chat conversation not found. It may have been removed or you may not have access.",
                canRetry = false,
                context = context,
                actionText = "Go Back"
            )
            
            is AppError.UnknownError -> ErrorState.Unknown(
                message = getUnknownErrorMessage(error.message, context),
                canRetry = true,
                context = context,
                actionText = "Try Again"
            )
            
            else -> ErrorState.Unknown(
                message = error.message ?: "Something went wrong. Please try again.",
                canRetry = true,
                context = context,
                actionText = "Try Again"
            )
        }
        
        _errors.emit(errorState)
        return errorState
    }
    
    /**
     * Handle error with automatic retry
     */
    suspend fun <T> handleErrorWithRetry(
        error: Throwable,
        context: String = "",
        operation: suspend () -> T
    ): Result<T> {
        val errorState = handleError(error, context)
        
        return if (errorState.canRetry) {
            retryMechanism.executeWithRetry(
                maxRetries = 2,
                operation = operation
            )
        } else {
            Result.failure(error)
        }
    }
    
    /**
     * Handle success recovery from error state
     */
    suspend fun handleRecovery(context: String = "") {
        _errors.emit(ErrorState.Recovered(context))
    }
    
    /**
     * Clear all error states
     */
    suspend fun clearErrors() {
        _errors.emit(ErrorState.Cleared)
    }
    
    private fun getNetworkErrorMessage(context: String): String {
        return when (context) {
            "chat" -> "Unable to send message. Please check your internet connection and try again."
            "login" -> "Unable to sign in. Please check your internet connection."
            "registration" -> "Unable to complete registration. Please check your internet connection."
            "invitation" -> "Unable to process invitation. Please check your internet connection."
            else -> "Network connection failed. Please check your internet connection and try again."
        }
    }
    
    private fun getAuthErrorMessage(context: String): String {
        return when (context) {
            "login" -> "Invalid email or password. Please check your credentials and try again."
            "registration" -> "Unable to create account. Please check your information and try again."
            "session" -> "Your session has expired. Please sign in again."
            else -> "Authentication failed. Please sign in again."
        }
    }
    
    private fun getValidationErrorMessage(context: String): String {
        return when (context) {
            "email" -> "Please enter a valid email address."
            "password" -> "Password must be at least 8 characters long."
            "age" -> "Please enter a valid age."
            "phone" -> "Please enter a valid phone number."
            "health_info" -> "Please provide health information."
            else -> "Please check your input and try again."
        }
    }
    
    private fun getServerErrorMessage(code: Int, message: String, context: String): String {
        return when {
            code >= 500 -> "Our servers are experiencing issues. Please try again in a moment."
            code == 404 -> when (context) {
                "chat" -> "Chat conversation not found."
                "user" -> "User account not found."
                "invitation" -> "Invitation not found or has expired."
                else -> "The requested information could not be found."
            }
            code == 403 -> "You don't have permission to access this feature."
            code == 429 -> "Too many requests. Please wait a moment and try again."
            else -> "Server error: $message"
        }
    }
    
    private fun getOfflineErrorMessage(context: String): String {
        return when (context) {
            "chat" -> "You're offline. Messages will be sent when connection is restored."
            "login" -> "You're offline. Please connect to the internet to sign in."
            "registration" -> "You're offline. Please connect to the internet to complete registration."
            else -> "You're offline. Some features may not be available until connection is restored."
        }
    }
    
    private fun getUnknownErrorMessage(message: String?, context: String): String {
        val baseMessage = message ?: "Something unexpected happened"
        return when (context) {
            "chat" -> "$baseMessage while sending message. Please try again."
            "login" -> "$baseMessage during sign in. Please try again."
            "registration" -> "$baseMessage during registration. Please try again."
            else -> "$baseMessage. Please try again."
        }
    }
}

/**
 * Represents different error states in the application with enhanced user experience
 */
sealed class ErrorState {
    abstract val message: String
    abstract val canRetry: Boolean
    abstract val context: String
    abstract val actionText: String
    
    data class Network(
        override val message: String,
        override val canRetry: Boolean = true,
        override val context: String = "",
        override val actionText: String = "Retry"
    ) : ErrorState()
    
    data class Authentication(
        override val message: String,
        override val canRetry: Boolean = false,
        override val context: String = "",
        override val actionText: String = "Sign In Again"
    ) : ErrorState()
    
    data class Validation(
        override val message: String,
        override val canRetry: Boolean = false,
        override val context: String = "",
        override val actionText: String = "Fix Input"
    ) : ErrorState()
    
    data class Server(
        override val message: String,
        override val canRetry: Boolean = true,
        override val context: String = "",
        override val actionText: String = "Try Again"
    ) : ErrorState()
    
    data class Offline(
        override val message: String,
        override val canRetry: Boolean = false,
        override val context: String = "",
        override val actionText: String = "Check Connection"
    ) : ErrorState()
    
    data class InvitationExpired(
        override val message: String,
        override val canRetry: Boolean = false,
        override val context: String = "",
        override val actionText: String = "Request New Invitation"
    ) : ErrorState()
    
    data class InvitationUsed(
        override val message: String,
        override val canRetry: Boolean = false,
        override val context: String = "",
        override val actionText: String = "Contact Carer"
    ) : ErrorState()
    
    data class UserNotFound(
        override val message: String,
        override val canRetry: Boolean = false,
        override val context: String = "",
        override val actionText: String = "Create Account"
    ) : ErrorState()
    
    data class ChatNotFound(
        override val message: String,
        override val canRetry: Boolean = false,
        override val context: String = "",
        override val actionText: String = "Go Back"
    ) : ErrorState()
    
    data class Unknown(
        override val message: String,
        override val canRetry: Boolean = true,
        override val context: String = "",
        override val actionText: String = "Try Again"
    ) : ErrorState()
    
    data class Recovered(
        val context: String = ""
    ) : ErrorState() {
        override val message: String = "Connection restored"
        override val canRetry: Boolean = false
        override val actionText: String = "Continue"
    }
    
    object Cleared : ErrorState() {
        override val message: String = ""
        override val canRetry: Boolean = false
        override val context: String = ""
        override val actionText: String = ""
    }
}

/**
 * Extension function to handle errors in ViewModels
 */
suspend inline fun <T> Result<T>.handleError(
    errorHandler: ErrorHandler,
    context: String = "",
    onSuccess: (T) -> Unit = {},
    onError: (ErrorState) -> Unit = {}
): T? {
    return if (isSuccess) {
        val data = getOrThrow()
        onSuccess(data)
        data
    } else {
        val error = exceptionOrNull() ?: Exception("Unknown error")
        val errorState = errorHandler.handleError(error, context)
        onError(errorState)
        null
    }
}