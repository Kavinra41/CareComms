package com.carecomms.data.repository

import com.carecomms.data.models.*
import com.carecomms.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf

/**
 * Composite chat repository that handles both real-time Firebase sync and local storage
 * Provides offline-first functionality with real-time updates when online
 */
class RealtimeChatRepository(
    private val firebaseChatRepository: ChatRepository,
    private val localChatRepository: ChatRepository,
    private val networkMonitor: NetworkMonitor
) : ChatRepository {

    override suspend fun getChatList(carerId: String): Flow<List<ChatPreview>> {
        return combine(
            networkMonitor.isOnline,
            localChatRepository.getChatList(carerId),
            firebaseChatRepository.getChatList(carerId)
        ) { isOnline, localChats, firebaseChats ->
            if (isOnline) {
                // Sync Firebase data to local storage
                syncChatsToLocal(firebaseChats)
                firebaseChats
            } else {
                localChats
            }
        }.distinctUntilChanged()
    }

    override suspend fun getMessages(chatId: String): Flow<List<Message>> {
        return combine(
            networkMonitor.isOnline,
            localChatRepository.getMessages(chatId),
            firebaseChatRepository.getMessages(chatId)
        ) { isOnline, localMessages, firebaseMessages ->
            if (isOnline) {
                // Sync Firebase messages to local storage
                syncMessagesToLocal(chatId, firebaseMessages)
                firebaseMessages
            } else {
                localMessages
            }
        }.distinctUntilChanged()
    }

    override suspend fun sendMessage(chatId: String, message: Message): Result<Unit> {
        // Always save to local first for immediate UI update
        val localResult = localChatRepository.sendMessage(chatId, message)
        
        return if (networkMonitor.isOnline.value) {
            // Try to send to Firebase
            val firebaseResult = firebaseChatRepository.sendMessage(chatId, message)
            if (firebaseResult.isFailure) {
                // Mark message as pending sync if Firebase fails
                markMessageAsPendingSync(chatId, message)
            }
            firebaseResult
        } else {
            // Mark message as pending sync when offline
            markMessageAsPendingSync(chatId, message)
            localResult
        }
    }

    override suspend fun markAsRead(chatId: String, messageId: String): Result<Unit> {
        val localResult = localChatRepository.markAsRead(chatId, messageId)
        
        return if (networkMonitor.isOnline.value) {
            firebaseChatRepository.markAsRead(chatId, messageId)
        } else {
            localResult
        }
    }

    override suspend fun markAllAsRead(chatId: String): Result<Unit> {
        val localResult = localChatRepository.markAllAsRead(chatId)
        
        return if (networkMonitor.isOnline.value) {
            firebaseChatRepository.markAllAsRead(chatId)
        } else {
            localResult
        }
    }

    override suspend fun getTypingStatus(chatId: String): Flow<TypingStatus> {
        return if (networkMonitor.isOnline.value) {
            firebaseChatRepository.getTypingStatus(chatId)
        } else {
            // Return empty typing status when offline
            flowOf(TypingStatus("", false, System.currentTimeMillis()))
        }
    }

    override suspend fun setTypingStatus(chatId: String, isTyping: Boolean): Result<Unit> {
        return if (networkMonitor.isOnline.value) {
            firebaseChatRepository.setTypingStatus(chatId, isTyping)
        } else {
            // Ignore typing status when offline
            Result.success(Unit)
        }
    }

    override suspend fun createChat(carerId: String, careeId: String): Result<String> {
        // Always create in local first
        val localResult = localChatRepository.createChat(carerId, careeId)
        
        return if (networkMonitor.isOnline.value && localResult.isSuccess) {
            // Try to create in Firebase
            val firebaseResult = firebaseChatRepository.createChat(carerId, careeId)
            if (firebaseResult.isSuccess) {
                firebaseResult
            } else {
                localResult
            }
        } else {
            localResult
        }
    }

    override suspend fun getChatId(carerId: String, careeId: String): String? {
        return if (networkMonitor.isOnline.value) {
            firebaseChatRepository.getChatId(carerId, careeId)
                ?: localChatRepository.getChatId(carerId, careeId)
        } else {
            localChatRepository.getChatId(carerId, careeId)
        }
    }

    override suspend fun searchChats(carerId: String, query: String): Flow<List<ChatPreview>> {
        return if (networkMonitor.isOnline.value) {
            firebaseChatRepository.searchChats(carerId, query)
        } else {
            localChatRepository.searchChats(carerId, query)
        }
    }

    private suspend fun syncChatsToLocal(firebaseChats: List<ChatPreview>) {
        // Implementation would sync Firebase chats to local database
        // This is a placeholder for the sync logic
    }

    private suspend fun syncMessagesToLocal(chatId: String, firebaseMessages: List<Message>) {
        // Implementation would sync Firebase messages to local database
        // This is a placeholder for the sync logic
    }

    private suspend fun markMessageAsPendingSync(chatId: String, message: Message) {
        // Implementation would mark message as needing sync when connection restored
        // This is a placeholder for the pending sync logic
    }
}

/**
 * Interface for monitoring network connectivity
 */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
    val connectionQuality: Flow<ConnectionQuality>
    suspend fun checkConnectivity(): Boolean
}

enum class ConnectionQuality {
    EXCELLENT,
    GOOD,
    POOR,
    OFFLINE
}

/**
 * Simple network monitor implementation
 */
class SimpleNetworkMonitor : NetworkMonitor {
    override val isOnline: Flow<Boolean> = flowOf(true) // Placeholder implementation
    override val connectionQuality: Flow<ConnectionQuality> = flowOf(ConnectionQuality.EXCELLENT)
    
    override suspend fun checkConnectivity(): Boolean = true
}