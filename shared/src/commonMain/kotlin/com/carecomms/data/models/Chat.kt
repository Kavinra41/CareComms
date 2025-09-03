package com.carecomms.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val status: MessageStatus,
    val type: MessageType = MessageType.TEXT
)

@Serializable
enum class MessageStatus { SENT, DELIVERED, READ }

@Serializable
enum class MessageType { TEXT, IMAGE, FILE }

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
data class Chat(
    val id: String,
    val carerId: String,
    val careeId: String,
    val createdAt: Long,
    val lastActivity: Long
)

@Serializable
data class TypingStatus(
    val userId: String,
    val isTyping: Boolean,
    val timestamp: Long
)