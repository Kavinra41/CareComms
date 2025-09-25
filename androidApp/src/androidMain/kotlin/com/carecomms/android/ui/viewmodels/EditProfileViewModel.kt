package com.carecomms.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carecomms.data.models.SimpleUser
import com.carecomms.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _city = MutableStateFlow("")
    val city: StateFlow<String> = _city.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private var currentUser: SimpleUser? = null

    fun initializeWithUser(user: SimpleUser) {
        currentUser = user
        _name.value = user.name
        _email.value = user.email
        _phoneNumber.value = user.phoneNumber
        _city.value = user.city
    }

    fun updateName(newName: String) {
        _name.value = newName
    }

    fun updatePhoneNumber(newPhoneNumber: String) {
        _phoneNumber.value = newPhoneNumber
    }

    fun updateCity(newCity: String) {
        _city.value = newCity
    }

    fun saveProfile() {
        val user = currentUser ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccess.value = false

            val updatedUser = user.copy(
                name = _name.value.trim(),
                phoneNumber = _phoneNumber.value.trim(),
                city = _city.value.trim()
            )

            userRepository.updateUser(updatedUser)
                .onSuccess {
                    _isSuccess.value = true
                    currentUser = updatedUser
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to update profile"
                }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccess() {
        _isSuccess.value = false
    }

    fun getUpdatedUser(): SimpleUser? = currentUser
}