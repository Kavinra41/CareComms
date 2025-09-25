package com.carecomms.android.data.repository

import com.carecomms.android.data.local.dao.ChatDao
import com.carecomms.android.data.local.dao.MessageDao
import com.carecomms.android.data.local.entities.ChatEntity
import com.carecomms.android.data.local.entities.MessageEntity
import com.carecomms.data.models.Chat
import com.carecomms.data.models.Message
import com.carecomms.data.models.MessageStatus
import com.carecomms.data.models.MessageType
import com.carecomms.data.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.util.UUID

class LocalChatRepository(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao
) : ChatRepository {

    // Chat operations using Room
    override suspend fun createOrGetChat(currentUserId: String, otherUserId: String): Result<Chat> {
        return try {
            // Create a consistent chat ID by sorting user IDs
            val chatId = if (currentUserId < otherUserId) {
                "${currentUserId}_${otherUserId}"
            } else {
                "${otherUserId}_${currentUserId}"
            }

            println("LocalChatRepository: Creating/getting chat with ID: $chatId")

            // Check if chat already exists
            val existingChat = chatDao.getChatById(chatId)
            
            if (existingChat != null) {
                val chat = existingChat.toChat()
                println("LocalChatRepository: Found existing chat")
                Result.success(chat)
            } else {
                // Create new chat
                val participantNames = mapOf(
                    currentUserId to "You",
                    otherUserId to "User" // We'll update this with real names later
                )

                val newChat = Chat(
                    id = chatId,
                    carerId = currentUserId,
                    careeId = otherUserId,
                    participants = listOf(currentUserId, otherUserId),
                    participantNames = participantNames,
                    lastMessage = "",
                    lastMessageTimestamp = System.currentTimeMillis(),
                    createdAt = System.currentTimeMillis(),
                    lastActivity = System.currentTimeMillis()
                )

                val chatEntity = newChat.toChatEntity()
                chatDao.insertChat(chatEntity)
                
                println("LocalChatRepository: Created new chat")
                Result.success(newChat)
            }
        } catch (e: Exception) {
            println("LocalChatRepository: Error creating/getting chat: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(message: Message): Result<Unit> {
        return try {
            println("LocalChatRepository: Sending message to chatId: ${message.chatId}")
            
            // Generate unique ID for the message
            val messageWithId = message.copy(id = UUID.randomUUID().toString())
            
            // Insert message into local database
            val messageEntity = messageWithId.toMessageEntity()
            messageDao.insertMessage(messageEntity)

            // Update chat's last message
            chatDao.updateLastMessage(
                chatId = message.chatId,
                lastMessage = message.content,
                timestamp = message.timestamp
            )
            
            println("LocalChatRepository: Message sent successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            println("LocalChatRepository: Error sending message: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override fun getMessagesFlow(chatId: String): Flow<List<Message>> {
        println("LocalChatRepository: Setting up message flow for chatId: $chatId")
        
        return messageDao.getMessagesByChatId(chatId).map { messageEntities ->
            println("LocalChatRepository: Retrieved ${messageEntities.size} messages from local DB")
            messageEntities.map { it.toMessage() }
        }
    }

    override suspend fun markMessagesAsRead(chatId: String, userId: String): Result<Unit> {
        return try {
            messageDao.markMessagesAsRead(chatId, userId, "READ")
            Result.success(Unit)
        } catch (e: Exception) {
            println("LocalChatRepository: Error marking messages as read: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Extension functions for entity conversion
    private fun ChatEntity.toChat(): Chat {
        val participantsList = Json.decodeFromString<List<String>>(participants)
        val participantNamesMap = Json.decodeFromString<Map<String, String>>(participantNames)
        
        return Chat(
            id = id,
            carerId = carerId,
            careeId = careeId,
            participants = participantsList,
            participantNames = participantNamesMap,
            lastMessage = lastMessage,
            lastMessageTimestamp = lastMessageTimestamp,
            createdAt = createdAt,
            lastActivity = lastActivity
        )
    }

    private fun Chat.toChatEntity(): ChatEntity {
        return ChatEntity(
            id = id,
            carerId = carerId,
            careeId = careeId,
            participants = Json.encodeToString(participants),
            participantNames = Json.encodeToString(participantNames),
            lastMessage = lastMessage,
            lastMessageTimestamp = lastMessageTimestamp,
            createdAt = createdAt,
            lastActivity = lastActivity
        )
    }

    private fun MessageEntity.toMessage(): Message {
        return Message(
            id = id,
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            content = content,
            timestamp = timestamp,
            status = MessageStatus.valueOf(status),
            type = MessageType.valueOf(type)
        )
    }

    private fun Message.toMessageEntity(): MessageEntity {
        return MessageEntity(
            id = id,
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            content = content,
            timestamp = timestamp,
            status = status.name,
            type = type.name
        )
    }
}