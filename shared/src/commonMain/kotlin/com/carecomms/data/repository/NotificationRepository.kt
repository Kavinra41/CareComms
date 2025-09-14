package com.carecomms.data.repository

import com.carecomms.data.models.NotificationPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing push notifications across platforms
 */
interface NotificationRepository {
    
    /**
     * Initialize notification system and request permissions
     */
    suspend fun initialize(): Result<Unit>
    
    /**
     * Get the current FCM token for this device
     */
    suspend fun getToken(): Result<String>
    
    /**
     * Subscribe to a topic for receiving notifications
     */
    suspend fun subscribeToTopic(topic: String): Result<Unit>
    
    /**
     * Unsubscribe from a topic
     */
    suspend fun unsubscribeFromTopic(topic: String): Result<Unit>
    
    /**
     * Check if notification permissions are granted
     */
    suspend fun areNotificationsEnabled(): Boolean
    
    /**
     * Request notification permissions from user
     */
    suspend fun requestNotificationPermission(): Result<Boolean>
    
    /**
     * Get user notification preferences
     */
    suspend fun getNotificationPreferences(): Flow<NotificationPreferences>
    
    /**
     * Update user notification preferences
     */
    suspend fun updateNotificationPreferences(preferences: NotificationPreferences): Result<Unit>
    
    /**
     * Send a local notification (for testing or offline scenarios)
     */
    suspend fun showLocalNotification(
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ): Result<Unit>
    
    /**
     * Clear all notifications
     */
    suspend fun clearAllNotifications(): Result<Unit>
    
    /**
     * Handle notification received in foreground
     */
    suspend fun handleForegroundNotification(
        title: String,
        body: String,
        data: Map<String, String>
    ): Result<Unit>
    
    /**
     * Handle notification clicked/opened
     */
    suspend fun handleNotificationClick(data: Map<String, String>): Result<Unit>
}