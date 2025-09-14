package com.carecomms.data.repository

import com.carecomms.data.models.NotificationPreferences
import com.carecomms.data.models.PushNotificationData
import com.carecomms.data.models.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import platform.Foundation.*
import platform.UserNotifications.*

class IOSNotificationRepository(
    private val localUserRepository: LocalUserRepository
) : NotificationRepository {
    
    private val _notificationPreferences = MutableStateFlow(NotificationPreferences())
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    
    companion object {
        private const val PREFS_KEY_NOTIFICATION_SETTINGS = "notification_preferences"
    }
    
    override suspend fun initialize(): Result<Unit> {
        return try {
            loadNotificationPreferences()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getToken(): Result<String> {
        return try {
            // FCM token retrieval for iOS would be handled through Firebase iOS SDK
            // This is a placeholder implementation
            Result.failure(NotImplementedError("FCM token retrieval needs Firebase iOS SDK integration"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun subscribeToTopic(topic: String): Result<Unit> {
        return try {
            // Topic subscription for iOS would be handled through Firebase iOS SDK
            Result.failure(NotImplementedError("Topic subscription needs Firebase iOS SDK integration"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        return try {
            // Topic unsubscription for iOS would be handled through Firebase iOS SDK
            Result.failure(NotImplementedError("Topic unsubscription needs Firebase iOS SDK integration"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun areNotificationsEnabled(): Boolean {
        return try {
            // Check notification authorization status
            var isEnabled = false
            notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
                isEnabled = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
            }
            isEnabled
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun requestNotificationPermission(): Result<Boolean> {
        return try {
            var permissionGranted = false
            
            notificationCenter.requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
            ) { granted, error ->
                permissionGranted = granted
            }
            
            Result.success(permissionGranted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getNotificationPreferences(): Flow<NotificationPreferences> {
        return _notificationPreferences.asStateFlow()
    }
    
    override suspend fun updateNotificationPreferences(preferences: NotificationPreferences): Result<Unit> {
        return try {
            _notificationPreferences.value = preferences
            saveNotificationPreferences(preferences)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun showLocalNotification(
        title: String,
        body: String,
        data: Map<String, String>
    ): Result<Unit> {
        return try {
            val content = UNMutableNotificationContent().apply {
                setTitle(title)
                setBody(body)
                setSound(UNNotificationSound.defaultSound)
                
                // Add custom data
                val userInfo = NSMutableDictionary()
                data.forEach { (key, value) ->
                    userInfo.setObject(value, key)
                }
                setUserInfo(userInfo)
            }
            
            val identifier = NSUUID().UUIDString
            val request = UNNotificationRequest.requestWithIdentifier(
                identifier = identifier,
                content = content,
                trigger = null // Immediate delivery
            )
            
            notificationCenter.addNotificationRequest(request) { error ->
                if (error != null) {
                    println("Error showing notification: ${error.localizedDescription}")
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearAllNotifications(): Result<Unit> {
        return try {
            notificationCenter.removeAllDeliveredNotifications()
            notificationCenter.removeAllPendingNotificationRequests()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun handleForegroundNotification(
        title: String,
        body: String,
        data: Map<String, String>
    ): Result<Unit> {
        return showLocalNotification(title, body, data)
    }
    
    override suspend fun handleNotificationClick(data: Map<String, String>): Result<Unit> {
        return try {
            // Handle navigation based on notification data
            val notificationData = data["notification_data"]?.let { 
                Json.decodeFromString<PushNotificationData>(it) 
            }
            
            when (notificationData?.type) {
                NotificationType.NEW_MESSAGE -> {
                    // Navigate to chat screen
                    // This will be handled by the UI layer
                }
                NotificationType.INVITATION_RECEIVED -> {
                    // Navigate to invitation screen
                    // This will be handled by the UI layer
                }
                else -> {
                    // Default handling
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun loadNotificationPreferences() {
        try {
            val currentUser = localUserRepository.getCurrentUser()
            if (currentUser != null) {
                // Load from UserDefaults or use defaults
                val userDefaults = NSUserDefaults.standardUserDefaults
                val prefsData = userDefaults.stringForKey(PREFS_KEY_NOTIFICATION_SETTINGS)
                
                if (prefsData != null) {
                    val preferences = Json.decodeFromString<NotificationPreferences>(prefsData)
                    _notificationPreferences.value = preferences
                } else {
                    _notificationPreferences.value = NotificationPreferences()
                }
            }
        } catch (e: Exception) {
            // Use default preferences
            _notificationPreferences.value = NotificationPreferences()
        }
    }
    
    private suspend fun saveNotificationPreferences(preferences: NotificationPreferences) {
        try {
            val currentUser = localUserRepository.getCurrentUser()
            if (currentUser != null) {
                val prefsJson = Json.encodeToString(preferences)
                val userDefaults = NSUserDefaults.standardUserDefaults
                userDefaults.setObject(prefsJson, PREFS_KEY_NOTIFICATION_SETTINGS)
                userDefaults.synchronize()
            }
        } catch (e: Exception) {
            // Handle save error
        }
    }
}