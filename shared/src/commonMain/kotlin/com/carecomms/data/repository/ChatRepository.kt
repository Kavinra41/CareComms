package com.carecomms.data.repository

import com.carecomms.data.models.Message
import com.carecomms.data.models.Chat
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun createOrGetChat(currentUserId: String, otherUserId: String): Result<Chat>
    suspend fun sendMessage(message: Message): Result<Unit>
    fun getMessagesFlow(chatId: String): Flow<List<Message>>
    suspend fun markMessagesAsRead(chatId: String, userId: String): Result<Unit>
}