package com.carecomms.data.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.carecomms.database.CareCommsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DatabaseManager(
    private val database: CareCommsDatabase
) {
    
    // User operations
    suspend fun insertUser(
        id: String,
        email: String,
        userType: String,
        createdAt: Long,
        data: String
    ) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.insertUser(id, email, userType, createdAt, data)
    }
    
    suspend fun getUserById(id: String) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.selectUserById(id).executeAsOneOrNull()
    }
    
    suspend fun getUserByEmail(email: String) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.selectUserByEmail(email).executeAsOneOrNull()
    }
    
    suspend fun updateUser(id: String, email: String, data: String) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.updateUser(email, data, id)
    }
    
    suspend fun deleteUser(id: String) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.deleteUser(id)
    }
    
    fun getAllUsersFlow(): Flow<List<com.carecomms.database.User>> {
        return database.careCommsDatabaseQueries.selectAllUsers()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }
    
    // Chat operations
    suspend fun insertChat(
        id: String,
        carerId: String,
        careeId: String,
        createdAt: Long,
        lastActivity: Long
    ) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.insertChat(id, carerId, careeId, createdAt, lastActivity)
    }
    
    suspend fun getChatById(id: String) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.selectChatById(id).executeAsOneOrNull()
    }
    
    suspend fun getChatByParticipants(carerId: String, careeId: String) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.selectChatByParticipants(carerId, careeId).executeAsOneOrNull()
    }
    
    suspend fun updateChatActivity(chatId: String, lastActivity: Long) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.updateChatActivity(lastActivity, chatId)
    }
    
    fun getChatsByCarerIdFlow(carerId: String): Flow<List<com.carecomms.database.Chat>> {
        return database.careCommsDatabaseQueries.selectChatsByCarerId(carerId)
            .asFlow()
            .mapToList(Dispatchers.Default)
    }
    
    fun getChatsByCareeIdFlow(careeId: String): Flow<List<com.carecomms.database.Chat>> {
        return database.careCommsDatabaseQueries.selectChatsByCareeId(careeId)
            .asFlow()
            .mapToList(Dispatchers.Default)
    }
    
    // Message operations
    suspend fun insertMessage(
        id: String,
        chatId: String,
        senderId: String,
        content: String,
        timestamp: Long,
        status: String,
        messageType: String
    ) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.insertMessage(id, chatId, senderId, content, timestamp, status, messageType)
    }
    
    suspend fun updateMessageStatus(messageId: String, status: String) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.updateMessageStatus(status, messageId)
    }
    
    suspend fun markAllMessagesAsRead(chatId: String, currentUserId: String) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.updateAllMessagesStatusInChat("READ", chatId, currentUserId)
    }
    
    suspend fun getLastMessage(chatId: String) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.selectLastMessageByChatId(chatId).executeAsOneOrNull()
    }
    
    suspend fun getUnreadMessageCount(chatId: String, currentUserId: String): Long = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.selectUnreadMessageCount(chatId, currentUserId).executeAsOne()
    }
    
    fun getMessagesByChatIdFlow(chatId: String): Flow<List<com.carecomms.database.Message>> {
        return database.careCommsDatabaseQueries.selectMessagesByChatId(chatId)
            .asFlow()
            .mapToList(Dispatchers.Default)
    }
    
    suspend fun getMessagesPaginated(chatId: String, limit: Long, offset: Long) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.selectMessagesByChatIdPaginated(chatId, limit, offset).executeAsList()
    }
    
    // Invitation operations
    suspend fun insertInvitation(
        token: String,
        carerId: String,
        expirationTime: Long,
        createdAt: Long
    ) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.insertInvitation(token, carerId, expirationTime, 0, createdAt)
    }
    
    suspend fun getValidInvitation(token: String, currentTime: Long) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.selectInvitationByToken(token, currentTime).executeAsOneOrNull()
    }
    
    suspend fun markInvitationAsUsed(token: String) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.markInvitationAsUsed(token)
    }
    
    suspend fun deleteExpiredInvitations(currentTime: Long) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.deleteExpiredInvitations(currentTime)
    }
    
    suspend fun getInvitationsByCarerId(carerId: String) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.selectInvitationsByCarerId(carerId).executeAsList()
    }
    
    suspend fun getInvitationByToken(token: String) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.selectInvitationByToken(token, System.currentTimeMillis()).executeAsOneOrNull()
    }
    
    // Cache operations
    suspend fun insertCache(key: String, value: String, expirationTime: Long?, createdAt: Long) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.insertCache(key, value, expirationTime, createdAt)
    }
    
    suspend fun getCache(key: String, currentTime: Long) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.selectCacheByKey(key, currentTime).executeAsOneOrNull()
    }
    
    suspend fun deleteCache(key: String) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.deleteCache(key)
    }
    
    suspend fun clearExpiredCache(currentTime: Long) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.deleteExpiredCache(currentTime)
    }
    
    suspend fun clearAllCache() = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.clearAllCache()
    }
    
    // Typing status operations
    suspend fun updateTypingStatus(chatId: String, userId: String, isTyping: Boolean, timestamp: Long) = withContext(Dispatchers.Default) {
        val typingValue = if (isTyping) 1L else 0L
        database.careCommsDatabaseQueries.insertOrUpdateTypingStatus(chatId, userId, typingValue, timestamp)
    }
    
    suspend fun getTypingStatus(chatId: String) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.selectTypingStatusByChatId(chatId).executeAsList()
    }
    
    suspend fun clearOldTypingStatus(cutoffTime: Long) = withContext(Dispatchers.Default) {
        database.careCommsDatabaseQueries.clearOldTypingStatus(cutoffTime)
    }
}