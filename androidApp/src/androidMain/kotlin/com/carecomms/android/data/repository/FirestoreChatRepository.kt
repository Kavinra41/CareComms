package com.carecomms.android.data.repository

import com.carecomms.data.models.Chat
import com.carecomms.data.models.Message
import com.carecomms.data.models.MessageStatus
import com.carecomms.data.repository.ChatRepository
import com.carecomms.data.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreChatRepository(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) : ChatRepository {

    private val chatRoomsCollection = firestore.collection("chat_rooms")

    override suspend fun createOrGetChat(currentUserId: String, otherUserId: String): Result<Chat> {
        return try {
            // Create a consistent chat ID by sorting user IDs
            val chatId = if (currentUserId < otherUserId) {
                "${currentUserId}_${otherUserId}"
            } else {
                "${otherUserId}_${currentUserId}"
            }

            println("FirestoreChatRepository: Creating/getting chat with ID: $chatId")

            // Check if chat room already exists
            val existingChatRoom = chatRoomsCollection.document(chatId).get().await()
            
            if (existingChatRoom.exists()) {
                val data = existingChatRoom.data!!
                val chat = Chat(
                    id = chatId,
                    carerId = currentUserId,
                    careeId = otherUserId,
                    participants = (data["participants"] as? List<String>) ?: emptyList(),
                    participantNames = (data["participantNames"] as? Map<String, String>) ?: emptyMap(),
                    lastMessage = data["lastMessage"] as? String ?: "",
                    lastMessageTimestamp = data["lastMessageTimestamp"] as? Long ?: System.currentTimeMillis(),
                    createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
                    lastActivity = data["lastActivity"] as? Long ?: System.currentTimeMillis()
                )
                println("FirestoreChatRepository: Found existing chat room")
                Result.success(chat)
            } else {
                // Get user names
                val currentUser = userRepository.getUser(currentUserId).getOrNull()
                val otherUser = userRepository.getUser(otherUserId).getOrNull()
                
                val participantNames = mapOf(
                    currentUserId to (currentUser?.name ?: "Unknown"),
                    otherUserId to (otherUser?.name ?: "Unknown")
                )

                // Create new chat room
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

                val chatData = mapOf(
                    "carerId" to newChat.carerId,
                    "careeId" to newChat.careeId,
                    "participants" to newChat.participants,
                    "participantNames" to newChat.participantNames,
                    "lastMessage" to newChat.lastMessage,
                    "lastMessageTimestamp" to newChat.lastMessageTimestamp,
                    "createdAt" to newChat.createdAt,
                    "lastActivity" to newChat.lastActivity
                )

                chatRoomsCollection.document(chatId).set(chatData).await()
                println("FirestoreChatRepository: Created new chat room")
                Result.success(newChat)
            }
        } catch (e: Exception) {
            println("FirestoreChatRepository: Error creating/getting chat: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(message: Message): Result<Unit> {
        return try {
            println("FirestoreChatRepository: Sending message to chatId: ${message.chatId}")
            
            // Add message to chat room's messages subcollection
            val messageData = mapOf(
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "content" to message.content,
                "timestamp" to message.timestamp,
                "status" to message.status.name,
                "type" to message.type.name
            )

            // Add to subcollection: chat_rooms/{chatId}/messages
            chatRoomsCollection
                .document(message.chatId)
                .collection("messages")
                .add(messageData)
                .await()

            // Update chat room's last message info
            val chatUpdateData = mapOf(
                "lastMessage" to message.content,
                "lastMessageTimestamp" to message.timestamp,
                "lastActivity" to message.timestamp
            )

            chatRoomsCollection.document(message.chatId).update(chatUpdateData).await()
            
            println("FirestoreChatRepository: Message sent successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            println("FirestoreChatRepository: Error sending message: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override fun getMessagesFlow(chatId: String): Flow<List<Message>> = callbackFlow {
        println("FirestoreChatRepository: Setting up message listener for chatId: $chatId")
        
        val listener = chatRoomsCollection
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("FirestoreChatRepository: Error listening to messages: ${error.message}")
                    error.printStackTrace()
                    return@addSnapshotListener
                }

                println("FirestoreChatRepository: Received snapshot with ${snapshot?.documents?.size ?: 0} documents")

                val messages = snapshot?.documents?.mapNotNull { document ->
                    val data = document.data
                    println("FirestoreChatRepository: Processing document ${document.id} with data: $data")
                    
                    if (data != null) {
                        try {
                            val message = Message(
                                id = document.id,
                                chatId = chatId, // Use the chatId parameter
                                senderId = data["senderId"] as? String ?: "",
                                senderName = data["senderName"] as? String ?: "",
                                content = data["content"] as? String ?: "",
                                timestamp = data["timestamp"] as? Long ?: System.currentTimeMillis(),
                                status = try {
                                    MessageStatus.valueOf(data["status"] as? String ?: "SENT")
                                } catch (e: Exception) {
                                    MessageStatus.SENT
                                }
                            )
                            println("FirestoreChatRepository: Created message: ${message.content}")
                            message
                        } catch (e: Exception) {
                            println("FirestoreChatRepository: Error creating message from document: ${e.message}")
                            null
                        }
                    } else {
                        println("FirestoreChatRepository: Document data is null")
                        null
                    }
                } ?: emptyList()

                println("FirestoreChatRepository: Sending ${messages.size} messages to flow")
                trySend(messages)
            }

        awaitClose { 
            println("FirestoreChatRepository: Removing message listener for chatId: $chatId")
            listener.remove() 
        }
    }

    override suspend fun markMessagesAsRead(chatId: String, userId: String): Result<Unit> {
        return try {
            val unreadMessages = chatRoomsCollection
                .document(chatId)
                .collection("messages")
                .whereNotEqualTo("senderId", userId)
                .whereEqualTo("status", "SENT")
                .get()
                .await()

            val batch = firestore.batch()
            unreadMessages.documents.forEach { document ->
                batch.update(document.reference, "status", "READ")
            }
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            println("FirestoreChatRepository: Error marking messages as read: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}