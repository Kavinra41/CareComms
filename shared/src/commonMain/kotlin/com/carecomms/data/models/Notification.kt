package com.carecomms.data.models

import kotlinx.serialization.Serializable

@Serializable
data class NotificationPreferences(
    val messageNotifications: Boolean = true,
    val invitationNotifications: Boolean = true,
    val systemNotifications: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "08:00"
)

@Serializable
data class PushNotificationData(
    val type: NotificationType,
    val chatId: String? = null,
    val senderId: String? = null,
    val senderName: String? = null,
    val messageId: String? = null,
    val invitationToken: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class NotificationType {
    NEW_MESSAGE,
    INVITATION_RECEIVED,
    SYSTEM_UPDATE,
    CARE_ALERT
}

@Serializable
data class NotificationPayload(
    val title: String,
    val body: String,
    val data: PushNotificationData,
    val priority: NotificationPriority = NotificationPriority.NORMAL
)

@Serializable
enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH
}