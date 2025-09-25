package com.carecomms.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carecomms.data.models.SimpleUser
import com.carecomms.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<SimpleUser>>(emptyList())
    val users: StateFlow<List<SimpleUser>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            userRepository.getAllUsers()
                .onSuccess { userList ->
                    _users.value = userList
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load users"
                }
            
            _isLoading.value = false
        }
    }

    fun refreshUsers() {
        loadUsers()
    }
}