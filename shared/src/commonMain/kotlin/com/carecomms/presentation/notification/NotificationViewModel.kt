package com.carecomms.presentation.notification

import com.carecomms.data.models.NotificationPreferences
import com.carecomms.domain.usecase.NotificationUseCase
import com.carecomms.presentation.state.LoadingStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class NotificationState(
    val isLoading: Boolean = false,
    val preferences: NotificationPreferences = NotificationPreferences(),
    val permissionGranted: Boolean = false,
    val fcmToken: String? = null,
    val error: String? = null
)

class NotificationViewModel(
    private val notificationUseCase: NotificationUseCase
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val loadingStateManager = LoadingStateManager()
    
    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()
    
    init {
        initializeNotifications()
        observeNotificationPreferences()
    }
    
    fun initializeNotifications() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                // Initialize notification system
                notificationUseCase.initializeNotifications().getOrThrow()
                
                // Check permission status
                val permissionGranted = notificationUseCase.areNotificationsEnabled()
                
                // Get FCM token
                val tokenResult = notificationUseCase.getFCMToken()
                val fcmToken = tokenResult.getOrNull()
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    permissionGranted = permissionGranted,
                    fcmToken = fcmToken,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to initialize notifications"
                )
            }
        }
    }
    
    fun requestNotificationPermission() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val result = notificationUseCase.requestNotificationPermission()
                val permissionGranted = result.getOrThrow()
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    permissionGranted = permissionGranted,
                    error = if (!permissionGranted) "Notification permission denied" else null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to request notification permission"
                )
            }
        }
    }
    
    fun updateNotificationPreferences(preferences: NotificationPreferences) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                notificationUseCase.updateNotificationPreferences(preferences).getOrThrow()
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    preferences = preferences,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update notification preferences"
                )
            }
        }
    }
    
    fun toggleMessageNotifications(enabled: Boolean) {
        val currentPreferences = _state.value.preferences
        val updatedPreferences = currentPreferences.copy(messageNotifications = enabled)
        updateNotificationPreferences(updatedPreferences)
    }
    
    fun toggleInvitationNotifications(enabled: Boolean) {
        val currentPreferences = _state.value.preferences
        val updatedPreferences = currentPreferences.copy(invitationNotifications = enabled)
        updateNotificationPreferences(updatedPreferences)
    }
    
    fun toggleSystemNotifications(enabled: Boolean) {
        val currentPreferences = _state.value.preferences
        val updatedPreferences = currentPreferences.copy(systemNotifications = enabled)
        updateNotificationPreferences(updatedPreferences)
    }
    
    fun toggleSound(enabled: Boolean) {
        val currentPreferences = _state.value.preferences
        val updatedPreferences = currentPreferences.copy(soundEnabled = enabled)
        updateNotificationPreferences(updatedPreferences)
    }
    
    fun toggleVibration(enabled: Boolean) {
        val currentPreferences = _state.value.preferences
        val updatedPreferences = currentPreferences.copy(vibrationEnabled = enabled)
        updateNotificationPreferences(updatedPreferences)
    }
    
    fun toggleQuietHours(enabled: Boolean) {
        val currentPreferences = _state.value.preferences
        val updatedPreferences = currentPreferences.copy(quietHoursEnabled = enabled)
        updateNotificationPreferences(updatedPreferences)
    }
    
    fun updateQuietHours(startTime: String, endTime: String) {
        val currentPreferences = _state.value.preferences
        val updatedPreferences = currentPreferences.copy(
            quietHoursStart = startTime,
            quietHoursEnd = endTime
        )
        updateNotificationPreferences(updatedPreferences)
    }
    
    fun clearAllNotifications() {
        viewModelScope.launch {
            try {
                notificationUseCase.clearAllNotifications().getOrThrow()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Failed to clear notifications"
                )
            }
        }
    }
    
    fun handleNotificationClick(data: Map<String, String>) {
        viewModelScope.launch {
            try {
                notificationUseCase.handleNotificationClick(data).getOrThrow()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Failed to handle notification click"
                )
            }
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    private fun observeNotificationPreferences() {
        viewModelScope.launch {
            notificationUseCase.getNotificationPreferences()
                .catch { e ->
                    _state.value = _state.value.copy(
                        error = e.message ?: "Failed to load notification preferences"
                    )
                }
                .onEach { preferences ->
                    _state.value = _state.value.copy(preferences = preferences)
                }
                .launchIn(this)
        }
    }
    
    fun onCleared() {
        // Clean up resources if needed
    }
}