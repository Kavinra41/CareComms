package com.carecomms.data.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthResult(
    val user: User,
    val token: String
)

@Serializable
data class InvitationData(
    val carerId: String,
    val carerName: String,
    val carerEmail: String,
    val expirationTime: Long,
    val token: String
)

@Serializable
data class CarerInfo(
    val id: String,
    val name: String,
    val email: String
)

sealed class AuthError : Exception() {
    object InvalidCredentials : AuthError()
    object UserNotFound : AuthError()
    object EmailAlreadyInUse : AuthError()
    object WeakPassword : AuthError()
    object InvalidEmail : AuthError()
    object InvalidInvitationToken : AuthError()
    object InvitationExpired : AuthError()
    object NetworkError : AuthError()
    data class UnknownError(val message: String) : AuthError()
}