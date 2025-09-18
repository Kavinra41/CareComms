package com.carecomms.data.models

sealed class AuthError : Exception() {
    object InvalidCredentials : AuthError()
    object UserNotFound : AuthError()
    object EmailAlreadyInUse : AuthError()
    object WeakPassword : AuthError()
    object InvalidEmail : AuthError()
    object InvalidInvitationToken : AuthError()
    object InvitationExpired : AuthError()
    object NetworkError : AuthError()
    data class UnknownError(override val message: String) : AuthError()
}