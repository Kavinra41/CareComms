package com.carecomms.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatPreview(
    val chatId: String,
    val otherUserId: String,
    val otherUserName: String,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
)