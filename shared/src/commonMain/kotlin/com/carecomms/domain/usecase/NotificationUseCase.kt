package com.carecomms.domain.usecase

import com.carecomms.data.models.NotificationPreferences
import com.carecomms.data.models.NotificationType
import com.carecomms.data.models.PushNotificationData
import com.carecomms.data.repository.NotificationRepository
import com.carecomms.data.repository.LocalUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class NotificationUseCase(
    private val notificationRepository: NotificationRepository,
    private val localUserRepository: LocalUserRepository
) {
    
    suspend fun initializeNotifications(): Result<Unit> {
        return try {
            notificationRepository.initialize().getOrThrow()
            
            // Subscribe to user-specific topics
            val currentUser = localUserRepository.getCurrentUser()
            if (currentUser != null) {
                subscribeToUserTopics(currentUser.id)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun requestNotificationPermission(): Result<Boolean> {
        return notificationRepository.requestNotificationPermission()
    }
    
    suspend fun areNotificationsEnabled(): Boolean {
        return notificationRepository.areNotificationsEnabled()
    }
    
    suspend fun getNotificationPreferences(): Flow<NotificationPreferences> {
        return notificationRepository.getNotificationPreferences()
    }
    
    suspend fun updateNotificationPreferences(preferences: NotificationPreferences): Result<Unit> {
        return notificationRepository.updateNotificationPreferences(preferences)
    }
    
    suspend fun subscribeToUserTopics(userId: String): Result<Unit> {
        return try {
            // Subscribe to user-specific message notifications
            notificationRepository.subscribeToTopic("user_$userId").getOrThrow()
            
            // Subscribe to general system notifications
            notificationRepository.subscribeToTopic("system_updates").getOrThrow()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun unsubscribeFromUserTopics(userId: String): Result<Unit> {
        return try {
            notificationRepository.unsubscribeFromTopic("user_$userId").getOrThrow()
            notificationRepository.unsubscribeFromTopic("system_updates").getOrThrow()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun handleNewMessageNotification(
        chatId: String,
        senderId: String,
        senderName: String,
        messageContent: String
    ): Result<Unit> {
        return try {
            val preferences = notificationRepository.getNotificationPreferences()
            
            // Check if message notifications are enabled
            // Note: In a real implementation, you'd collect the flow value
            // For now, we'll assume notifications are enabled
            
            val notificationData = PushNotificationData(
                type = NotificationType.NEW_MESSAGE,
                chatId = chatId,
                senderId = senderId,
                senderName = senderName
            )
            
            val dataMap = mapOf(
                "notification_data" to Json.encodeToString(notificationData)
            )
            
            notificationRepository.showLocalNotification(
                title = "New message from $senderName",
                body = messageContent,
                data = dataMap
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun handleInvitationNotification(
        invitationToken: String,
        carerName: String
    ): Result<Unit> {
        return try {
            val notificationData = PushNotificationData(
                type = NotificationType.INVITATION_RECEIVED,
                invitationToken = invitationToken,
                senderName = carerName
            )
            
            val dataMap = mapOf(
                "notification_data" to Json.encodeToString(notificationData)
            )
            
            notificationRepository.showLocalNotification(
                title = "Care Invitation",
                body = "$carerName has invited you to connect on CareComms",
                data = dataMap
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun handleSystemNotification(
        title: String,
        message: String
    ): Result<Unit> {
        return try {
            val notificationData = PushNotificationData(
                type = NotificationType.SYSTEM_UPDATE
            )
            
            val dataMap = mapOf(
                "notification_data" to Json.encodeToString(notificationData)
            )
            
            notificationRepository.showLocalNotification(
                title = title,
                body = message,
                data = dataMap
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun clearAllNotifications(): Result<Unit> {
        return notificationRepository.clearAllNotifications()
    }
    
    suspend fun handleNotificationClick(data: Map<String, String>): Result<Unit> {
        return notificationRepository.handleNotificationClick(data)
    }
    
    suspend fun getFCMToken(): Result<String> {
        return notificationRepository.getToken()
    }
    
    private fun isInQuietHours(preferences: NotificationPreferences): Boolean {
        if (!preferences.quietHoursEnabled) return false
        
        // Simple quiet hours check - in a real implementation you'd use proper time parsing
        // This is a placeholder implementation
        return false
    }
}