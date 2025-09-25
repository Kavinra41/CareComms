package com.carecomms.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String,
    val senderName: String = "",
    val content: String,
    val timestamp: Long,
    val status: MessageStatus,
    val type: MessageType = MessageType.TEXT
)

@Serializable
enum class MessageStatus {
    SENT,
    DELIVERED,
    READ
}

@Serializable
enum class MessageType {
    TEXT,
    IMAGE,
    SYSTEM
}

@Serializable
data class Chat(
    val id: String,
    val carerId: String,
    val careeId: String,
    val participants: List<String> = listOf(carerId, careeId),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val createdAt: Long,
    val lastActivity: Long
)

@Serializable
data class ChatPreview(
    val chatId: String,
    val careeName: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int,
    val isOnline: Boolean
)

@Serializable
data class TypingStatus(
    val userId: String,
    val isTyping: Boolean,
    val timestamp: Long
)