package com.carecomms.data.models

import kotlinx.serialization.Serializable

@Serializable
sealed class AppError : Exception() {
    @Serializable
    object NetworkError : AppError()
    
    @Serializable
    object AuthenticationError : AppError()
    
    @Serializable
    object ValidationError : AppError()
    
    @Serializable
    data class ServerError(val code: Int, val message: String) : AppError()
    
    @Serializable
    data class UnknownError(val message: String) : AppError()
    
    @Serializable
    object InvitationExpiredError : AppError()
    
    @Serializable
    object InvitationAlreadyUsedError : AppError()
    
    @Serializable
    object UserNotFoundError : AppError()
    
    @Serializable
    object ChatNotFoundError : AppError()
}