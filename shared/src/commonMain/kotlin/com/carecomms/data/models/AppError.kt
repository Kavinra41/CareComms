package com.carecomms.data.models

sealed class AppError : Exception() {
    object NetworkError : AppError()
    object AuthenticationError : AppError()
    object ValidationError : AppError()
    data class ServerError(val code: Int, override val message: String) : AppError()
    data class UnknownError(val throwable: Throwable) : AppError()
}