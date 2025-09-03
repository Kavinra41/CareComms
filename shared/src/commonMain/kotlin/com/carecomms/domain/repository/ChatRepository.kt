package com.carecomms.domain.repository

import com.carecomms.data.models.ChatPreview
import com.carecomms.data.models.Message
import com.carecomms.data.models.TypingStatus
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getChatList(carerId: String): Flow<List<ChatPreview>>
    suspend fun getMessages(chatId: String): Flow<List<Message>>
    suspend fun sendMessage(chatId: String, message: Message): Result<Unit>
    suspend fun markAsRead(chatId: String, messageId: String): Result<Unit>
    suspend fun getTypingStatus(chatId: String): Flow<TypingStatus>
    suspend fun setTypingStatus(chatId: String, isTyping: Boolean): Result<Unit>
}