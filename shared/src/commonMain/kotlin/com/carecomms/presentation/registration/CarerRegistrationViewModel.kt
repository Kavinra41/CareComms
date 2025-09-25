package com.carecomms.presentation.registration

import com.carecomms.data.models.CarerRegistrationData
import com.carecomms.data.models.DocumentUpload
import com.carecomms.data.models.DocumentUploadService
import com.carecomms.domain.usecase.CarerRegistrationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CarerRegistrationViewModel(
    private val registrationUseCase: CarerRegistrationUseCase,
    private val documentUploadService: DocumentUploadService,
    private val coroutineScope: CoroutineScope
) {
    
    private val _state = MutableStateFlow(CarerRegistrationState())
    val state: StateFlow<CarerRegistrationState> = _state.asStateFlow()
    
    private val _effects = MutableSharedFlow<CarerRegistrationEffect>()
    val effects: SharedFlow<CarerRegistrationEffect> = _effects.asSharedFlow()
    
    fun handleAction(action: CarerRegistrationAction) {
        when (action) {
            is CarerRegistrationAction.UpdateEmail -> {
                _state.value = _state.value.copy(
                    email = action.email,
                    validationErrors = _state.value.validationErrors - "email"
                )
            }
            is CarerRegistrationAction.UpdatePassword -> {
                _state.value = _state.value.copy(
                    password = action.password,
                    validationErrors = _state.value.validationErrors - "password"
                )
            }
            is CarerRegistrationAction.UpdateConfirmPassword -> {
                _state.value = _state.value.copy(
                    confirmPassword = action.confirmPassword,
                    validationErrors = _state.value.validationErrors - "confirmPassword"
                )
            }
            is CarerRegistrationAction.UpdateAge -> {
                _state.value = _state.value.copy(
                    age = action.age,
                    validationErrors = _state.value.validationErrors - "age"
                )
            }
            is CarerRegistrationAction.UpdatePhoneNumber -> {
                _state.value = _state.value.copy(
                    phoneNumber = action.phoneNumber,
                    validationErrors = _state.value.validationErrors - "phoneNumber"
                )
            }
            is CarerRegistrationAction.UpdateLocation -> {
                _state.value = _state.value.copy(
                    location = action.location,
                    validationErrors = _state.value.validationErrors - "location"
                )
            }
            is CarerRegistrationAction.AddDocument -> {
                val currentDocuments = _state.value.uploadedDocuments
                _state.value = _state.value.copy(
                    uploadedDocuments = currentDocuments + action.document,
                    validationErrors = _state.value.validationErrors - "documents"
                )
            }
            is CarerRegistrationAction.RemoveDocument -> {
                val currentDocuments = _state.value.uploadedDocuments
                _state.value = _state.value.copy(
                    uploadedDocuments = currentDocuments.filter { it.id != action.documentId }
                )
            }
            is CarerRegistrationAction.SubmitRegistration -> {
                submitRegistration()
            }
            is CarerRegistrationAction.ClearErrors -> {
                _state.value = _state.value.copy(
                    validationErrors = emptyMap(),
                    registrationError = null
                )
            }
            is CarerRegistrationAction.ResetState -> {
                _state.value = CarerRegistrationState()
            }
        }
    }
    
    private fun submitRegistration() {
        val currentState = _state.value
        
        // Validate form completeness
        val validationErrors = validateForm(currentState)
        if (validationErrors.isNotEmpty()) {
            _state.value = currentState.copy(validationErrors = validationErrors)
            return
        }
        
        // Set loading state
        _state.value = currentState.copy(isLoading = true, registrationError = null)
        
        coroutineScope.launch {
            try {
                val registrationData = CarerRegistrationData(
                    email = currentState.email,
                    password = currentState.password,
                    documents = currentState.uploadedDocuments.map { it.fileName },
                    age = currentState.age.toInt(),
                    phoneNumber = currentState.phoneNumber,
                    location = currentState.location
                )
                
                val result = registrationUseCase.execute(registrationData)
                
                when (result) {
                    is com.carecomms.data.models.AuthResult.Success -> {
                        _state.value = currentState.copy(
                            isLoading = false,
                            isRegistrationSuccessful = true,
                            authResult = result
                        )
                        _effects.emit(CarerRegistrationEffect.NavigateToChatList)
                    }
                    is com.carecomms.data.models.AuthResult.Error -> {
                        _state.value = currentState.copy(
                            isLoading = false,
                            registrationError = result.message
                        )
                        _effects.emit(CarerRegistrationEffect.ShowError(result.message))
                    }
                }
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isLoading = false,
                    registrationError = e.message ?: "An unexpected error occurred"
                )
                _effects.emit(CarerRegistrationEffect.ShowError(
                    e.message ?: "An unexpected error occurred"
                ))
            }
        }
    }
    
    private fun validateForm(state: CarerRegistrationState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        
        if (state.email.isBlank()) {
            errors["email"] = "Email is required"
        }
        
        if (state.password.isBlank()) {
            errors["password"] = "Password is required"
        }
        
        if (state.confirmPassword.isBlank()) {
            errors["confirmPassword"] = "Please confirm your password"
        } else if (state.password != state.confirmPassword) {
            errors["confirmPassword"] = "Passwords do not match"
        }
        
        if (state.age.isBlank()) {
            errors["age"] = "Age is required"
        } else {
            try {
                val ageInt = state.age.toInt()
                if (ageInt < 18 || ageInt > 100) {
                    errors["age"] = "Age must be between 18 and 100"
                }
            } catch (e: NumberFormatException) {
                errors["age"] = "Please enter a valid age"
            }
        }
        
        if (state.phoneNumber.isBlank()) {
            errors["phoneNumber"] = "Phone number is required"
        }
        
        if (state.location.isBlank()) {
            errors["location"] = "Location is required"
        }
        
        if (state.uploadedDocuments.isEmpty()) {
            errors["documents"] = "At least one professional document is required"
        }
        
        return errors
    }
    
    fun uploadDocument(fileName: String, documentType: com.carecomms.data.models.DocumentType) {
        coroutineScope.launch {
            try {
                val result = documentUploadService.uploadDocument(fileName, documentType)
                result.fold(
                    onSuccess = { document ->
                        handleAction(CarerRegistrationAction.AddDocument(document))
                        _effects.emit(CarerRegistrationEffect.ShowSuccess("Document uploaded successfully"))
                    },
                    onFailure = { error ->
                        _effects.emit(CarerRegistrationEffect.ShowError(
                            error.message ?: "Failed to upload document"
                        ))
                    }
                )
            } catch (e: Exception) {
                _effects.emit(CarerRegistrationEffect.ShowError(
                    e.message ?: "Failed to upload document"
                ))
            }
        }
    }
}