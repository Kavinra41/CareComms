package com.carecomms.android.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.carecomms.android.MainActivity
import com.carecomms.data.models.NotificationType
import com.carecomms.data.models.PushNotificationData
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class CareCommsFirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "FCMService"
        const val CHANNEL_ID_MESSAGES = "carecomms_messages"
        const val CHANNEL_ID_INVITATIONS = "carecomms_invitations"
        const val CHANNEL_ID_SYSTEM = "carecomms_system"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "From: ${remoteMessage.from}")
        
        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // Check if message contains a notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message Notification Body: ${notification.body}")
            handleNotificationMessage(
                title = notification.title ?: "CareComms",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        
        // Send token to your app server
        sendRegistrationToServer(token)
    }
    
    private fun handleDataMessage(data: Map<String, String>) {
        try {
            val notificationData = data["notification_data"]?.let { 
                Json.decodeFromString<PushNotificationData>(it) 
            }
            
            val title = data["title"] ?: "CareComms"
            val body = data["body"] ?: "You have a new notification"
            
            when (notificationData?.type) {
                NotificationType.NEW_MESSAGE -> {
                    showNotification(
                        title = title,
                        body = body,
                        channelId = CHANNEL_ID_MESSAGES,
                        data = data
                    )
                }
                NotificationType.INVITATION_RECEIVED -> {
                    showNotification(
                        title = title,
                        body = body,
                        channelId = CHANNEL_ID_INVITATIONS,
                        data = data
                    )
                }
                NotificationType.SYSTEM_UPDATE -> {
                    showNotification(
                        title = title,
                        body = body,
                        channelId = CHANNEL_ID_SYSTEM,
                        data = data
                    )
                }
                else -> {
                    showNotification(
                        title = title,
                        body = body,
                        channelId = CHANNEL_ID_MESSAGES,
                        data = data
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling data message", e)
        }
    }
    
    private fun handleNotificationMessage(
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        showNotification(
            title = title,
            body = body,
            channelId = CHANNEL_ID_MESSAGES,
            data = data
        )
    }
    
    private fun showNotification(
        title: String,
        body: String,
        channelId: String,
        data: Map<String, String>
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // Add notification data to intent
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        
        // Add action buttons based on notification type
        val notificationData = data["notification_data"]?.let { 
            try {
                Json.decodeFromString<PushNotificationData>(it)
            } catch (e: Exception) {
                null
            }
        }
        
        when (notificationData?.type) {
            NotificationType.NEW_MESSAGE -> {
                // Add quick reply action for messages
                val replyIntent = Intent(this, MainActivity::class.java).apply {
                    putExtra("action", "quick_reply")
                    putExtra("chat_id", notificationData.chatId)
                }
                val replyPendingIntent = PendingIntent.getActivity(
                    this,
                    (System.currentTimeMillis() + 1).toInt(),
                    replyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                notificationBuilder.addAction(
                    android.R.drawable.ic_menu_send,
                    "Reply",
                    replyPendingIntent
                )
            }
            NotificationType.INVITATION_RECEIVED -> {
                // Add accept/decline actions for invitations
                val acceptIntent = Intent(this, MainActivity::class.java).apply {
                    putExtra("action", "accept_invitation")
                    putExtra("invitation_token", notificationData.invitationToken)
                }
                val acceptPendingIntent = PendingIntent.getActivity(
                    this,
                    (System.currentTimeMillis() + 2).toInt(),
                    acceptIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                notificationBuilder.addAction(
                    android.R.drawable.ic_menu_add,
                    "Accept",
                    acceptPendingIntent
                )
            }
            else -> {
                // Default notification without actions
            }
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
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
            
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }
    
    private fun sendRegistrationToServer(token: String) {
        // TODO: Send token to your app server
        Log.d(TAG, "Token sent to server: $token")
        
        // In a real implementation, you would send this token to your backend
        // so it can send push notifications to this device
    }
}