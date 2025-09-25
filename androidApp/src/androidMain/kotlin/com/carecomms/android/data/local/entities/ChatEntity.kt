package com.carecomms.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    val id: String,
    val carerId: String,
    val careeId: String,
    val participants: String, // JSON string of participant IDs
    val participantNames: String, // JSON string of participant names map
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val createdAt: Long,
    val lastActivity: Long
)