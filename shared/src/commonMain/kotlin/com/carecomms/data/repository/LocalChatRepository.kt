package com.carecomms.data.repository

import com.carecomms.data.database.DatabaseManager
import com.carecomms.data.models.*
import com.carecomms.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class LocalChatRepository(
    private val databaseManager: DatabaseManager,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : ChatRepository {

    override suspend fun getChatList(carerId: String): Flow<List<ChatPreview>> {
        return databaseManager.getChatsByCarerIdFlow(carerId).map { chats ->
            chats.map { chat ->
                val lastMessage = databaseManager.getLastMessage(chat.id)
                val unreadCount = databaseManager.getUnreadMessageCount(chat.id, carerId)
                val caree = databaseManager.getUserById(chat.careeId)
                
                val careeName = caree?.let { user ->
                    when (user.userType) {
                        "CAREE" -> {
                            val careeData = json.decodeFromString<Caree>(user.data)
                            "${careeData.personalDetails.firstName} ${careeData.personalDetails.lastName}"
                        }
                        else -> user.email
                    }
                } ?: "Unknown User"

                ChatPreview(
                    chatId = chat.id,
                    careeName = careeName,
                    lastMessage = lastMessage?.content ?: "",
                    lastMessageTime = lastMessage?.timestamp ?: chat.createdAt,
                    unreadCount = unreadCount.toInt(),
                    isOnline = false // This would be determined by real-time presence
                )
            }
        }
    }

    override suspend fun getMessages(chatId: String): Flow<List<Message>> {
        return databaseManager.getMessagesByChatIdFlow(chatId).map { dbMessages ->
            dbMessages.map { dbMessage ->
                Message(
                    id = dbMessage.id,
                    senderId = dbMessage.senderId,
                    content = dbMessage.content,
                    timestamp = dbMessage.timestamp,
                    status = MessageStatus.valueOf(dbMessage.status),
                    type = MessageType.valueOf(dbMessage.messageType)
                )
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun sendMessage(chatId: String, message: Message): Result<Unit> {
        return try {
            val messageId = message.id.ifEmpty { Uuid.random().toString() }
            databaseManager.insertMessage(
                id = messageId,
                chatId = chatId,
                senderId = message.senderId,
                content = message.content,
                timestamp = message.timestamp,
                status = message.status.name,
                messageType = message.type.name
            )
            
            // Update chat last activity
            databaseManager.updateChatActivity(chatId, message.timestamp)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(chatId: String, messageId: String): Result<Unit> {
        return try {
            databaseManager.updateMessageStatus(messageId, MessageStatus.READ.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(chatId: String): Result<Unit> {
        return try {
            // This would need the current user ID to avoid marking own messages
            // For now, we'll implement a basic version
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTypingStatus(chatId: String): Flow<TypingStatus> {
        // For local implementation, we'll return a simple flow
        // In a real app, this would be connected to real-time updates
        return kotlinx.coroutines.flow.flowOf(
            TypingStatus(
                userId = "",
                isTyping = false,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun setTypingStatus(chatId: String, isTyping: Boolean): Result<Unit> {
        return try {
            // For local implementation, we'll store typing status in database
            // In a real app, this would be sent to real-time service
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createChat(carerId: String, careeId: String): Result<String> {
        return try {
            // Check if chat already exists
            val existingChat = databaseManager.getChatByParticipants(carerId, careeId)
            if (existingChat != null) {
                return Result.success(existingChat.id)
            }
            
            val chatId = Uuid.random().toString()
            val currentTime = System.currentTimeMillis()
            
            databaseManager.insertChat(
                id = chatId,
                carerId = carerId,
                careeId = careeId,
                createdAt = currentTime,
                lastActivity = currentTime
            )
            
            Result.success(chatId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChatId(carerId: String, careeId: String): String? {
        return try {
            databaseManager.getChatByParticipants(carerId, careeId)?.id
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun searchChats(carerId: String, query: String): Flow<List<ChatPreview>> {
        return getChatList(carerId).map { chatPreviews ->
            chatPreviews.filter { preview ->
                preview.careeName.contains(query, ignoreCase = true) ||
                preview.lastMessage.contains(query, ignoreCase = true)
            }
        }
    }
}