package com.carecomms.android.data.repository

import com.carecomms.data.models.Chat
import com.carecomms.data.models.ChatPreview
import com.carecomms.data.models.Message
import com.carecomms.data.models.MessageStatus
import com.carecomms.data.repository.ChatRepository
import com.carecomms.data.repository.InvitationRepository
import com.carecomms.data.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreChatRepository(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val invitationRepository: InvitationRepository
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

    override suspend fun getChatPreviews(userId: String): Flow<List<ChatPreview>> = callbackFlow {
        println("FirestoreChatRepository: Getting chat previews for user: $userId")
        
        var listener: com.google.firebase.firestore.ListenerRegistration? = null
        
        try {
            // Get user's relationships (both as carer and caree)
            val carerRelationships = invitationRepository.getCarerRelationships(userId).getOrNull() ?: emptyList()
            val careeRelationships = invitationRepository.getCareeRelationships(userId).getOrNull() ?: emptyList()
            
            val connectedUserIds = mutableSetOf<String>()
            
            // Add carees (if user is a carer)
            carerRelationships.forEach { relationship ->
                connectedUserIds.add(relationship.careeId)
            }
            
            // Add carers (if user is a caree)
            careeRelationships.forEach { relationship ->
                connectedUserIds.add(relationship.carerId)
            }
            
            println("FirestoreChatRepository: Found ${connectedUserIds.size} connected users")
            
            if (connectedUserIds.isEmpty()) {
                println("FirestoreChatRepository: No connected users found")
                trySend(emptyList())
            } else {
                // Create chat previews for all connected users, whether chat rooms exist or not
                
                // Add previews from existing chat rooms
                listener = chatRoomsCollection
                    .whereArrayContains("participants", userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            println("FirestoreChatRepository: Error listening to chat previews: ${error.message}")
                            return@addSnapshotListener
                        }
                        
                        val existingChatPreviews = snapshot?.documents?.mapNotNull { document ->
                            try {
                                val data = document.data ?: return@mapNotNull null
                                val participants = data["participants"] as? List<String> ?: return@mapNotNull null
                                val participantNames = data["participantNames"] as? Map<String, String> ?: return@mapNotNull null
                                
                                // Find the other user in the chat
                                val otherUserId = participants.firstOrNull { it != userId } ?: return@mapNotNull null
                                
                                // Only include chats with connected users
                                if (!connectedUserIds.contains(otherUserId)) {
                                    return@mapNotNull null
                                }
                                
                                val otherUserName = participantNames[otherUserId] ?: "Unknown User"
                                val lastMessage = data["lastMessage"] as? String ?: ""
                                val lastMessageTimestamp = data["lastMessageTimestamp"] as? Long ?: 0L
                                
                                ChatPreview(
                                    chatId = document.id,
                                    otherUserId = otherUserId,
                                    otherUserName = otherUserName,
                                    lastMessage = lastMessage,
                                    lastMessageTimestamp = lastMessageTimestamp,
                                    unreadCount = 0,
                                    isOnline = false
                                )
                            } catch (e: Exception) {
                                println("FirestoreChatRepository: Error creating chat preview: ${e.message}")
                                null
                            }
                        }?.toMutableList() ?: mutableListOf()
                        
                        // Add previews for connected users without existing chat rooms
                        val existingChatUserIds = existingChatPreviews.map { it.otherUserId }.toSet()
                        
                        connectedUserIds.forEach { connectedUserId ->
                            if (!existingChatUserIds.contains(connectedUserId)) {
                                // Find the user name from relationships
                                val userName = carerRelationships.find { it.careeId == connectedUserId }?.careeName
                                    ?: careeRelationships.find { it.carerId == connectedUserId }?.carerName
                                    ?: "Unknown User"
                                
                                // Create a chat ID for this potential chat
                                val chatId = if (userId < connectedUserId) {
                                    "${userId}_${connectedUserId}"
                                } else {
                                    "${connectedUserId}_${userId}"
                                }
                                
                                existingChatPreviews.add(
                                    ChatPreview(
                                        chatId = chatId,
                                        otherUserId = connectedUserId,
                                        otherUserName = userName,
                                        lastMessage = "Start a conversation",
                                        lastMessageTimestamp = 0L,
                                        unreadCount = 0,
                                        isOnline = false
                                    )
                                )
                            }
                        }
                        
                        val sortedPreviews = existingChatPreviews.sortedByDescending { it.lastMessageTimestamp }
                        println("FirestoreChatRepository: Sending ${sortedPreviews.size} chat previews (${existingChatUserIds.size} existing, ${connectedUserIds.size - existingChatUserIds.size} new)")
                        trySend(sortedPreviews)
                    }
            }
            
        } catch (e: Exception) {
            println("FirestoreChatRepository: Error setting up chat previews: ${e.message}")
            trySend(emptyList())
        }
        
        awaitClose { 
            println("FirestoreChatRepository: Removing chat previews listener")
            listener?.remove()
        }
    }
}