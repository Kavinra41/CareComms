package com.carecomms.data.repository

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.carecomms.data.models.NotificationPreferences
import com.carecomms.data.models.PushNotificationData
import com.carecomms.data.models.NotificationType
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class AndroidNotificationRepository(
    private val context: Context,
    private val localUserRepository: LocalUserRepository
) : NotificationRepository {
    
    private val firebaseMessaging = FirebaseMessaging.getInstance()
    private val notificationManager = NotificationManagerCompat.from(context)
    
    private val _notificationPreferences = MutableStateFlow(NotificationPreferences())
    
    companion object {
        const val CHANNEL_ID_MESSAGES = "carecomms_messages"
        const val CHANNEL_ID_INVITATIONS = "carecomms_invitations"
        const val CHANNEL_ID_SYSTEM = "carecomms_system"
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
        private const val PREFS_KEY_NOTIFICATION_SETTINGS = "notification_preferences"
    }
    
    override suspend fun initialize(): Result<Unit> {
        return try {
            createNotificationChannels()
            loadNotificationPreferences()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getToken(): Result<String> {
        return try {
            val token = firebaseMessaging.token.await()
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun subscribeToTopic(topic: String): Result<Unit> {
        return try {
            firebaseMessaging.subscribeToTopic(topic).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        return try {
            firebaseMessaging.unsubscribeFromTopic(topic).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            notificationManager.areNotificationsEnabled()
        }
    }
    
    override suspend fun requestNotificationPermission(): Result<Boolean> {
        return try {
            // Note: Actual permission request needs to be handled in the Activity
            // This method returns current permission status
            val hasPermission = areNotificationsEnabled()
            Result.success(hasPermission)
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
            if (!areNotificationsEnabled()) {
                return Result.failure(SecurityException("Notification permission not granted"))
            }
            
            val channelId = determineChannelId(data)
            val notificationId = System.currentTimeMillis().toInt()
            
            val intent = createNotificationIntent(data)
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            
            notificationManager.notify(notificationId, notification)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearAllNotifications(): Result<Unit> {
        return try {
            notificationManager.cancelAll()
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
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_MESSAGES,
                    "Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "New message notifications"
                    enableVibration(true)
                    enableLights(true)
                },
                NotificationChannel(
                    CHANNEL_ID_INVITATIONS,
                    "Invitations",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Care invitation notifications"
                },
                NotificationChannel(
                    CHANNEL_ID_SYSTEM,
                    "System",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "System and app notifications"
                }
            )
            
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }
    
    private fun determineChannelId(data: Map<String, String>): String {
        val notificationData = data["notification_data"]?.let { 
            try {
                Json.decodeFromString<PushNotificationData>(it)
            } catch (e: Exception) {
                null
            }
        }
        
        return when (notificationData?.type) {
            NotificationType.NEW_MESSAGE -> CHANNEL_ID_MESSAGES
            NotificationType.INVITATION_RECEIVED -> CHANNEL_ID_INVITATIONS
            NotificationType.SYSTEM_UPDATE -> CHANNEL_ID_SYSTEM
            else -> CHANNEL_ID_MESSAGES
        }
    }
    
    private fun createNotificationIntent(data: Map<String, String>): Intent {
        // Create intent to open the app with notification data
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent()
        
        data.forEach { (key, value) ->
            intent.putExtra(key, value)
        }
        
        return intent
    }
    
    private suspend fun loadNotificationPreferences() {
        try {
            val currentUser = localUserRepository.getCurrentUser()
            if (currentUser != null) {
                // Load from local storage or use defaults
                _notificationPreferences.value = NotificationPreferences()
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
                // Save to local storage
                val prefsJson = Json.encodeToString(preferences)
                // This would typically be saved to SharedPreferences or local database
            }
        } catch (e: Exception) {
            // Handle save error
        }
    }
}