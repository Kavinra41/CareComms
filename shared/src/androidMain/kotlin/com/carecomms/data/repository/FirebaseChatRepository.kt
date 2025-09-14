package com.carecomms.data.repository

import com.carecomms.data.models.*
import com.carecomms.domain.repository.ChatRepository
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class FirebaseChatRepository(
    private val database: FirebaseDatabase
) : ChatRepository {

    companion object {
        private const val CHATS_PATH = "chats"
        private const val MESSAGES_PATH = "messages"
        private const val TYPING_PATH = "typing"
        private const val PRESENCE_PATH = "presence"
        private const val USERS_PATH = "users"
    }

    override suspend fun getChatList(carerId: String): Flow<List<ChatPreview>> = callbackFlow {
        val chatsRef = database.getReference(CHATS_PATH)
            .orderByChild("carerId")
            .equalTo(carerId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatPreviews = mutableListOf<ChatPreview>()
                
                for (chatSnapshot in snapshot.children) {
                    val chat = chatSnapshot.getValue(FirebaseChat::class.java)
                    if (chat != null) {
                        // Get last message for this chat
                        val messagesRef = database.getReference("$MESSAGES_PATH/${chat.id}")
                            .orderByChild("timestamp")
                            .limitToLast(1)
                        
                        messagesRef.get().addOnSuccessListener { messageSnapshot ->
                            val lastMessage = messageSnapshot.children.firstOrNull()
                                ?.getValue(FirebaseMessage::class.java)
                            
                            // Get caree name
                            val careeRef = database.getReference("$USERS_PATH/${chat.careeId}")
                            careeRef.get().addOnSuccessListener { userSnapshot ->
                                val careeData = userSnapshot.getValue(FirebaseUser::class.java)
                                val careeName = careeData?.displayName ?: "Unknown User"
                                
                                // Get unread count
                                val unreadRef = database.getReference("$MESSAGES_PATH/${chat.id}")
                                    .orderByChild("status")
                                    .equalTo("SENT")
                                
                                unreadRef.get().addOnSuccessListener { unreadSnapshot ->
                                    val unreadCount = unreadSnapshot.children.count { messageSnap ->
                                        val msg = messageSnap.getValue(FirebaseMessage::class.java)
                                        msg?.senderId != carerId
                                    }
                                    
                                    // Check online status
                                    val presenceRef = database.getReference("$PRESENCE_PATH/${chat.careeId}")
                                    presenceRef.get().addOnSuccessListener { presenceSnapshot ->
                                        val isOnline = presenceSnapshot.getValue(Boolean::class.java) ?: false
                                        
                                        val preview = ChatPreview(
                                            chatId = chat.id,
                                            careeName = careeName,
                                            lastMessage = lastMessage?.content ?: "",
                                            lastMessageTime = lastMessage?.timestamp ?: chat.createdAt,
                                            unreadCount = unreadCount,
                                            isOnline = isOnline
                                        )
                                        
                                        chatPreviews.add(preview)
                                        trySend(chatPreviews.toList())
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (snapshot.children.count() == 0) {
                    trySend(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        chatsRef.addValueEventListener(listener)
        
        awaitClose {
            chatsRef.removeEventListener(listener)
        }
    }

    override suspend fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val messagesRef = database.getReference("$MESSAGES_PATH/$chatId")
            .orderByChild("timestamp")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                
                for (messageSnapshot in snapshot.children) {
                    val firebaseMessage = messageSnapshot.getValue(FirebaseMessage::class.java)
                    if (firebaseMessage != null) {
                        val message = Message(
                            id = firebaseMessage.id,
                            senderId = firebaseMessage.senderId,
                            content = firebaseMessage.content,
                            timestamp = firebaseMessage.timestamp,
                            status = MessageStatus.valueOf(firebaseMessage.status),
                            type = MessageType.valueOf(firebaseMessage.type)
                        )
                        messages.add(message)
                    }
                }
                
                trySend(messages.sortedBy { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        messagesRef.addValueEventListener(listener)
        
        awaitClose {
            messagesRef.removeEventListener(listener)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun sendMessage(chatId: String, message: Message): Result<Unit> {
        return try {
            val messageId = message.id.ifEmpty { Uuid.random().toString() }
            val firebaseMessage = FirebaseMessage(
                id = messageId,
                senderId = message.senderId,
                content = message.content,
                timestamp = message.timestamp,
                status = message.status.name,
                type = message.type.name
            )

            // Send message to Firebase
            database.getReference("$MESSAGES_PATH/$chatId/$messageId")
                .setValue(firebaseMessage)
                .await()

            // Update chat last activity
            database.getReference("$CHATS_PATH/$chatId/lastActivity")
                .setValue(message.timestamp)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(chatId: String, messageId: String): Result<Unit> {
        return try {
            database.getReference("$MESSAGES_PATH/$chatId/$messageId/status")
                .setValue(MessageStatus.READ.name)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(chatId: String): Result<Unit> {
        return try {
            val messagesRef = database.getReference("$MESSAGES_PATH/$chatId")
            val snapshot = messagesRef.get().await()
            
            val updates = mutableMapOf<String, Any>()
            for (messageSnapshot in snapshot.children) {
                val message = messageSnapshot.getValue(FirebaseMessage::class.java)
                if (message != null && message.status != MessageStatus.READ.name) {
                    updates["${message.id}/status"] = MessageStatus.READ.name
                }
            }
            
            if (updates.isNotEmpty()) {
                messagesRef.updateChildren(updates).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTypingStatus(chatId: String): Flow<TypingStatus> = callbackFlow {
        val typingRef = database.getReference("$TYPING_PATH/$chatId")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val typingData = snapshot.getValue(FirebaseTypingStatus::class.java)
                if (typingData != null) {
                    val typingStatus = TypingStatus(
                        userId = typingData.userId,
                        isTyping = typingData.isTyping,
                        timestamp = typingData.timestamp
                    )
                    trySend(typingStatus)
                } else {
                    trySend(TypingStatus("", false, System.currentTimeMillis()))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        typingRef.addValueEventListener(listener)
        
        awaitClose {
            typingRef.removeEventListener(listener)
        }
    }

    override suspend fun setTypingStatus(chatId: String, isTyping: Boolean): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val typingStatus = FirebaseTypingStatus(
                userId = currentUserId,
                isTyping = isTyping,
                timestamp = System.currentTimeMillis()
            )

            database.getReference("$TYPING_PATH/$chatId")
                .setValue(typingStatus)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createChat(carerId: String, careeId: String): Result<String> {
        return try {
            // Check if chat already exists
            val existingChatSnapshot = database.getReference(CHATS_PATH)
                .orderByChild("carerId")
                .equalTo(carerId)
                .get()
                .await()

            for (chatSnapshot in existingChatSnapshot.children) {
                val chat = chatSnapshot.getValue(FirebaseChat::class.java)
                if (chat?.careeId == careeId) {
                    return Result.success(chat.id)
                }
            }

            // Create new chat
            val chatId = Uuid.random().toString()
            val currentTime = System.currentTimeMillis()
            
            val firebaseChat = FirebaseChat(
                id = chatId,
                carerId = carerId,
                careeId = careeId,
                createdAt = currentTime,
                lastActivity = currentTime
            )

            database.getReference("$CHATS_PATH/$chatId")
                .setValue(firebaseChat)
                .await()

            Result.success(chatId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChatId(carerId: String, careeId: String): String? {
        return try {
            val snapshot = database.getReference(CHATS_PATH)
                .orderByChild("carerId")
                .equalTo(carerId)
                .get()
                .await()

            for (chatSnapshot in snapshot.children) {
                val chat = chatSnapshot.getValue(FirebaseChat::class.java)
                if (chat?.careeId == careeId) {
                    return chat.id
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun searchChats(carerId: String, query: String): Flow<List<ChatPreview>> {
        // For Firebase implementation, we'll get all chats and filter locally
        // In a production app, you might want to implement server-side search
        return callbackFlow {
            getChatList(carerId).collect { chatPreviews ->
                val filteredChats = chatPreviews.filter { preview ->
                    preview.careeName.contains(query, ignoreCase = true) ||
                    preview.lastMessage.contains(query, ignoreCase = true)
                }
                trySend(filteredChats)
            }
        }
    }

    private fun getCurrentUserId(): String? {
        // This should be implemented to get current user ID from Firebase Auth
        // For now, returning null - this will be implemented when integrating with auth
        return null
    }
}

// Firebase data classes
data class FirebaseChat(
    val id: String = "",
    val carerId: String = "",
    val careeId: String = "",
    val createdAt: Long = 0L,
    val lastActivity: Long = 0L
)

data class FirebaseMessage(
    val id: String = "",
    val senderId: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val status: String = "SENT",
    val type: String = "TEXT"
)

data class FirebaseTypingStatus(
    val userId: String = "",
    val isTyping: Boolean = false,
    val timestamp: Long = 0L
)

data class FirebaseUser(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val userType: String = ""
)