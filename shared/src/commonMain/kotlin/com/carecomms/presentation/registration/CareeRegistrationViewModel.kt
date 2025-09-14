package com.carecomms.presentation.registration

import com.carecomms.data.models.CareeRegistrationData
import com.carecomms.data.validation.CareeRegistrationValidator
import com.carecomms.domain.usecase.CareeRegistrationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CareeRegistrationViewModel(
    private val careeRegistrationUseCase: CareeRegistrationUseCase,
    private val validator: CareeRegistrationValidator,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    
    private val _state = MutableStateFlow(CareeRegistrationState())
    val state: StateFlow<CareeRegistrationState> = _state.asStateFlow()
    
    fun handleIntent(intent: CareeRegistrationIntent) {
        when (intent) {
            is CareeRegistrationIntent.UpdateEmail -> {
                _state.value = _state.value.copy(email = intent.email)
                validateForm()
            }
            is CareeRegistrationIntent.UpdatePassword -> {
                _state.value = _state.value.copy(password = intent.password)
                validateForm()
            }
            is CareeRegistrationIntent.UpdateConfirmPassword -> {
                _state.value = _state.value.copy(confirmPassword = intent.confirmPassword)
                validateForm()
            }
            is CareeRegistrationIntent.UpdateHealthInfo -> {
                _state.value = _state.value.copy(healthInfo = intent.healthInfo)
                validateForm()
            }
            is CareeRegistrationIntent.UpdateFirstName -> {
                _state.value = _state.value.copy(firstName = intent.firstName)
                validateForm()
            }
            is CareeRegistrationIntent.UpdateLastName -> {
                _state.value = _state.value.copy(lastName = intent.lastName)
                validateForm()
            }
            is CareeRegistrationIntent.UpdateDateOfBirth -> {
                _state.value = _state.value.copy(dateOfBirth = intent.dateOfBirth)
                validateForm()
            }
            is CareeRegistrationIntent.UpdateAddress -> {
                _state.value = _state.value.copy(address = intent.address)
            }
            is CareeRegistrationIntent.UpdateEmergencyContact -> {
                _state.value = _state.value.copy(emergencyContact = intent.emergencyContact)
            }
            is CareeRegistrationIntent.ValidateInvitation -> {
                validateInvitation(intent.token)
            }
            is CareeRegistrationIntent.RegisterCaree -> {
                registerCaree()
            }
            is CareeRegistrationIntent.ClearError -> {
                _state.value = _state.value.copy(errorMessage = null)
            }
        }
    }
    
    private fun validateForm() {
        val currentState = _state.value
        val registrationData = CareeRegistrationData(
            email = currentState.email,
            password = currentState.password,
            healthInfo = currentState.healthInfo,
            basicDetails = currentState.toPersonalDetails()
        )
        
        val validationResult = validator.validate(registrationData)
        _state.value = currentState.copy(
            validationErrors = validationResult.errors
        )
    }
    
    private fun validateInvitation(token: String) {
        _state.value = _state.value.copy(
            invitationToken = token,
            isValidatingInvitation = true,
            errorMessage = null
        )
        
        coroutineScope.launch {
            try {
                val result = careeRegistrationUseCase.validateInvitationToken(token)
                if (result.isSuccess) {
                    val carerInfo = result.getOrThrow()
                    _state.value = _state.value.copy(
                        carerInfo = carerInfo,
                        isInvitationValid = true,
                        isValidatingInvitation = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        isInvitationValid = false,
                        isValidatingInvitation = false,
                        errorMessage = "Invalid or expired invitation link"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isInvitationValid = false,
                    isValidatingInvitation = false,
                    errorMessage = "Failed to validate invitation: ${e.message}"
                )
            }
        }
    }
    
    private fun registerCaree() {
        val currentState = _state.value
        
        if (!currentState.isFormValid) {
            _state.value = currentState.copy(
                errorMessage = "Please fill in all required fields correctly"
            )
            return
        }
        
        _state.value = currentState.copy(isLoading = true, errorMessage = null)
        
        coroutineScope.launch {
            try {
                val registrationData = CareeRegistrationData(
                    email = currentState.email,
                    password = currentState.password,
                    healthInfo = currentState.healthInfo,
                    basicDetails = currentState.toPersonalDetails()
                )
                
                val result = careeRegistrationUseCase.registerCaree(
                    registrationData,
                    currentState.invitationToken
                )
                
                if (result.isSuccess) {
                    _state.value = currentState.copy(
                        isLoading = false,
                        isRegistrationSuccessful = true
                    )
                } else {
                    _state.value = currentState.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                    )
                }
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isLoading = false,
                    errorMessage = "Registration failed: ${e.message}"
                )
            }
        }
    }
}