package com.carecomms.presentation.notification

import com.carecomms.data.models.NotificationPermissionStatus
import com.carecomms.data.models.NotificationPreferences

data class NotificationState(
    val isInitialized: Boolean = false,
    val fcmToken: String? = null,
    val permissionStatus: NotificationPermissionStatus = NotificationPermissionStatus(
        isGranted = false,
        canRequestPermission = true
    ),
    val preferences: NotificationPreferences? = null
)