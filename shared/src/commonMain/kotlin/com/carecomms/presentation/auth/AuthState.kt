package com.carecomms.presentation.auth

import com.carecomms.presentation.state.AuthState

data class AuthViewState(
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val error: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val authState: AuthState = AuthState.Loading
)

sealed class AuthAction {
    data class SignIn(val email: String, val password: String) : AuthAction()
    object SignOut : AuthAction()
    data class UpdateEmail(val email: String) : AuthAction()
    data class UpdatePassword(val password: String) : AuthAction()
    object ClearError : AuthAction()
    object CheckAuthStatus : AuthAction()
}

sealed class AuthEffect {
    object NavigateToHome : AuthEffect()
    object NavigateToLogin : AuthEffect()
    data class ShowError(val message: String) : AuthEffect()
}