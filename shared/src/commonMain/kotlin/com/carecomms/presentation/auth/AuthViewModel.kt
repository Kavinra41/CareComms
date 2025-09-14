package com.carecomms.presentation.auth

import com.carecomms.domain.usecase.AuthUseCase
import com.carecomms.presentation.state.AuthState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class AuthViewModel(
    private val authUseCase: AuthUseCase
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _state = MutableStateFlow(AuthViewState())
    val state: StateFlow<AuthViewState> = _state.asStateFlow()
    
    private val _effects = MutableSharedFlow<AuthEffect>()
    val effects: SharedFlow<AuthEffect> = _effects.asSharedFlow()

    init {
        checkAuthenticationStatus()
    }

    fun handleAction(action: AuthAction) {
        when (action) {
            is AuthAction.SignIn -> signIn(action.email, action.password)
            is AuthAction.SignOut -> signOut()
            is AuthAction.UpdateEmail -> updateEmail(action.email)
            is AuthAction.UpdatePassword -> updatePassword(action.password)
            is AuthAction.ClearError -> clearError()
            is AuthAction.CheckAuthStatus -> checkAuthenticationStatus()
        }
    }

    private fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _state.update { 
                it.copy(
                    isLoading = true, 
                    error = null,
                    emailError = null,
                    passwordError = null
                ) 
            }
            
            // Validate inputs
            val emailValidation = authUseCase.validateEmail(email)
            val passwordValidation = authUseCase.validatePassword(password)
            
            if (emailValidation.isFailure || passwordValidation.isFailure) {
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        emailError = emailValidation.exceptionOrNull()?.message,
                        passwordError = passwordValidation.exceptionOrNull()?.message
                    )
                }
                return@launch
            }
            
            try {
                val result = authUseCase.signIn(email, password)
                
                if (result.isSuccess) {
                    val authResult = result.getOrThrow()
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            authState = AuthState.Authenticated(
                                authResult.user.id, 
                                when (authResult.user) {
                                    is com.carecomms.data.models.Carer -> "carer"
                                    is com.carecomms.data.models.Caree -> "caree"
                                }
                            )
                        ) 
                    }
                    _effects.emit(AuthEffect.NavigateToHome)
                } else {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message ?: "Sign in failed"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Sign in failed: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = authUseCase.signOut()
                
                if (result.isSuccess) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            authState = AuthState.Unauthenticated,
                            email = "",
                            password = ""
                        ) 
                    }
                    _effects.emit(AuthEffect.NavigateToLogin)
                } else {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Sign out failed: ${result.exceptionOrNull()?.message}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Sign out failed: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun updateEmail(email: String) {
        _state.update { 
            it.copy(
                email = email,
                emailError = null
            ) 
        }
    }

    private fun updatePassword(password: String) {
        _state.update { 
            it.copy(
                password = password,
                passwordError = null
            ) 
        }
    }

    private fun clearError() {
        _state.update { 
            it.copy(
                error = null,
                emailError = null,
                passwordError = null
            ) 
        }
    }

    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                authUseCase.getAuthState().collect { authState ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            authState = authState
                        ) 
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        authState = AuthState.Error(e.message ?: "Authentication check failed")
                    ) 
                }
            }
        }
    }

    fun onCleared() {
        viewModelScope.cancel()
    }
}