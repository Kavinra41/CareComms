package com.carecomms.domain.usecase

import com.carecomms.data.models.*
import com.carecomms.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ChatUseCase(
    private val chatRepository: ChatRepository,
    private val notificationUseCase: NotificationUseCase? = null
) {

    suspend fun getChatList(carerId: String): Flow<List<ChatPreview>> {
        return chatRepository.getChatList(carerId)
    }

    suspend fun getMessages(chatId: String): Flow<List<Message>> {
        return chatRepository.getMessages(chatId)
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        content: String,
        type: MessageType = MessageType.TEXT,
        recipientUser: User? = null,
        senderName: String? = null
    ): Result<Unit> {
        if (content.isBlank()) {
            return Result.failure(IllegalArgumentException("Message content cannot be empty"))
        }

        val message = Message(
            id = Uuid.random().toString(),
            senderId = senderId,
            content = content.trim(),
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT,
            type = type,
            data = mapOf("chatId" to chatId)
        )

        val result = chatRepository.sendMessage(chatId, message)
        
        // Send notification if message was sent successfully and we have recipient info
        if (result.isSuccess && recipientUser != null && senderName != null && notificationUseCase != null) {
            notificationUseCase.sendMessageNotification(
                targetUser = recipientUser,
                senderName = senderName,
                message = message
            )
        }
        
        return result
    }

    suspend fun markMessageAsRead(chatId: String, messageId: String): Result<Unit> {
        return chatRepository.markAsRead(chatId, messageId)
    }

    suspend fun markAllMessagesAsRead(chatId: String): Result<Unit> {
        return chatRepository.markAllAsRead(chatId)
    }

    suspend fun getTypingStatus(chatId: String): Flow<TypingStatus> {
        return chatRepository.getTypingStatus(chatId)
    }

    suspend fun setTypingStatus(chatId: String, isTyping: Boolean): Result<Unit> {
        return chatRepository.setTypingStatus(chatId, isTyping)
    }

    suspend fun createOrGetChat(carerId: String, careeId: String): Result<String> {
        // First try to get existing chat
        val existingChatId = chatRepository.getChatId(carerId, careeId)
        if (existingChatId != null) {
            return Result.success(existingChatId)
        }

        // Create new chat if none exists
        return chatRepository.createChat(carerId, careeId)
    }

    suspend fun searchChats(carerId: String, query: String): Flow<List<ChatPreview>> {
        if (query.isBlank()) {
            return getChatList(carerId)
        }
        return chatRepository.searchChats(carerId, query.trim())
    }

    suspend fun getUnreadMessageCount(carerId: String): Flow<Int> {
        return getChatList(carerId).map { chatPreviews ->
            chatPreviews.sumOf { it.unreadCount }
        }
    }

    suspend fun getChatPreview(chatId: String, carerId: String): Flow<ChatPreview?> {
        return getChatList(carerId).map { chatPreviews ->
            chatPreviews.find { it.chatId == chatId }
        }
    }

    fun validateMessageContent(content: String): Result<String> {
        val trimmedContent = content.trim()
        
        return when {
            trimmedContent.isBlank() -> Result.failure(
                IllegalArgumentException("Message cannot be empty")
            )
            trimmedContent.length > MAX_MESSAGE_LENGTH -> Result.failure(
                IllegalArgumentException("Message is too long (max $MAX_MESSAGE_LENGTH characters)")
            )
            else -> Result.success(trimmedContent)
        }
    }

    companion object {
        private const val MAX_MESSAGE_LENGTH = 1000
    }
}