package com.carecomms.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.carecomms.database.CareCommsDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class DatabaseManagerTest {
    
    private lateinit var driver: SqlDriver
    private lateinit var database: CareCommsDatabase
    private lateinit var databaseManager: DatabaseManager
    
    @BeforeTest
    fun setup() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        CareCommsDatabase.Schema.create(driver)
        database = CareCommsDatabase(driver)
        databaseManager = DatabaseManager(database)
    }
    
    @AfterTest
    fun teardown() {
        driver.close()
    }
    
    @Test
    fun testInsertAndGetUser() = runTest {
        // Given
        val userId = "user123"
        val email = "test@example.com"
        val userType = "CARER"
        val createdAt = System.currentTimeMillis()
        val data = """{"name": "Test User"}"""
        
        // When
        databaseManager.insertUser(userId, email, userType, createdAt, data)
        val retrievedUser = databaseManager.getUserById(userId)
        
        // Then
        assertNotNull(retrievedUser)
        assertEquals(userId, retrievedUser.id)
        assertEquals(email, retrievedUser.email)
        assertEquals(userType, retrievedUser.userType)
        assertEquals(createdAt, retrievedUser.createdAt)
        assertEquals(data, retrievedUser.data)
    }
    
    @Test
    fun testGetUserByEmail() = runTest {
        // Given
        val userId = "user123"
        val email = "test@example.com"
        val userType = "CARER"
        val createdAt = System.currentTimeMillis()
        val data = """{"name": "Test User"}"""
        
        // When
        databaseManager.insertUser(userId, email, userType, createdAt, data)
        val retrievedUser = databaseManager.getUserByEmail(email)
        
        // Then
        assertNotNull(retrievedUser)
        assertEquals(userId, retrievedUser.id)
        assertEquals(email, retrievedUser.email)
    }
    
    @Test
    fun testUpdateUser() = runTest {
        // Given
        val userId = "user123"
        val originalEmail = "test@example.com"
        val updatedEmail = "updated@example.com"
        val userType = "CARER"
        val createdAt = System.currentTimeMillis()
        val originalData = """{"name": "Test User"}"""
        val updatedData = """{"name": "Updated User"}"""
        
        // When
        databaseManager.insertUser(userId, originalEmail, userType, createdAt, originalData)
        databaseManager.updateUser(userId, updatedEmail, updatedData)
        val retrievedUser = databaseManager.getUserById(userId)
        
        // Then
        assertNotNull(retrievedUser)
        assertEquals(updatedEmail, retrievedUser.email)
        assertEquals(updatedData, retrievedUser.data)
    }
    
    @Test
    fun testInsertAndGetChat() = runTest {
        // Given
        val chatId = "chat123"
        val carerId = "carer123"
        val careeId = "caree123"
        val createdAt = System.currentTimeMillis()
        val lastActivity = createdAt + 1000
        
        // When
        databaseManager.insertChat(chatId, carerId, careeId, createdAt, lastActivity)
        val retrievedChat = databaseManager.getChatById(chatId)
        
        // Then
        assertNotNull(retrievedChat)
        assertEquals(chatId, retrievedChat.id)
        assertEquals(carerId, retrievedChat.carerId)
        assertEquals(careeId, retrievedChat.careeId)
        assertEquals(createdAt, retrievedChat.createdAt)
        assertEquals(lastActivity, retrievedChat.lastActivity)
    }
    
    @Test
    fun testGetChatByParticipants() = runTest {
        // Given
        val chatId = "chat123"
        val carerId = "carer123"
        val careeId = "caree123"
        val createdAt = System.currentTimeMillis()
        val lastActivity = createdAt + 1000
        
        // When
        databaseManager.insertChat(chatId, carerId, careeId, createdAt, lastActivity)
        val retrievedChat = databaseManager.getChatByParticipants(carerId, careeId)
        
        // Then
        assertNotNull(retrievedChat)
        assertEquals(chatId, retrievedChat.id)
        assertEquals(carerId, retrievedChat.carerId)
        assertEquals(careeId, retrievedChat.careeId)
    }
    
    @Test
    fun testInsertAndGetMessage() = runTest {
        // Given
        val messageId = "message123"
        val chatId = "chat123"
        val senderId = "sender123"
        val content = "Hello, world!"
        val timestamp = System.currentTimeMillis()
        val status = "SENT"
        val messageType = "TEXT"
        
        // When
        databaseManager.insertMessage(messageId, chatId, senderId, content, timestamp, status, messageType)
        val messages = databaseManager.getMessagesByChatIdFlow(chatId)
        
        // Then
        // Note: In a real test, you'd collect from the flow
        // For simplicity, we'll test the database directly
        val dbMessage = database.careCommsDatabaseQueries.selectMessageById(messageId).executeAsOneOrNull()
        assertNotNull(dbMessage)
        assertEquals(messageId, dbMessage.id)
        assertEquals(chatId, dbMessage.chatId)
        assertEquals(senderId, dbMessage.senderId)
        assertEquals(content, dbMessage.content)
        assertEquals(timestamp, dbMessage.timestamp)
        assertEquals(status, dbMessage.status)
        assertEquals(messageType, dbMessage.messageType)
    }
    
    @Test
    fun testUpdateMessageStatus() = runTest {
        // Given
        val messageId = "message123"
        val chatId = "chat123"
        val senderId = "sender123"
        val content = "Hello, world!"
        val timestamp = System.currentTimeMillis()
        val originalStatus = "SENT"
        val updatedStatus = "READ"
        val messageType = "TEXT"
        
        // When
        databaseManager.insertMessage(messageId, chatId, senderId, content, timestamp, originalStatus, messageType)
        databaseManager.updateMessageStatus(messageId, updatedStatus)
        
        // Then
        val dbMessage = database.careCommsDatabaseQueries.selectMessageById(messageId).executeAsOneOrNull()
        assertNotNull(dbMessage)
        assertEquals(updatedStatus, dbMessage.status)
    }
    
    @Test
    fun testInsertAndGetInvitation() = runTest {
        // Given
        val token = "invitation123"
        val carerId = "carer123"
        val expirationTime = System.currentTimeMillis() + 86400000 // 24 hours
        val createdAt = System.currentTimeMillis()
        
        // When
        databaseManager.insertInvitation(token, carerId, expirationTime, createdAt)
        val retrievedInvitation = databaseManager.getValidInvitation(token, System.currentTimeMillis())
        
        // Then
        assertNotNull(retrievedInvitation)
        assertEquals(token, retrievedInvitation.token)
        assertEquals(carerId, retrievedInvitation.carerId)
        assertEquals(expirationTime, retrievedInvitation.expirationTime)
        assertEquals(0, retrievedInvitation.isUsed)
    }
    
    @Test
    fun testMarkInvitationAsUsed() = runTest {
        // Given
        val token = "invitation123"
        val carerId = "carer123"
        val expirationTime = System.currentTimeMillis() + 86400000
        val createdAt = System.currentTimeMillis()
        
        // When
        databaseManager.insertInvitation(token, carerId, expirationTime, createdAt)
        databaseManager.markInvitationAsUsed(token)
        val retrievedInvitation = databaseManager.getValidInvitation(token, System.currentTimeMillis())
        
        // Then
        assertNull(retrievedInvitation) // Should be null because it's marked as used
    }
    
    @Test
    fun testCacheOperations() = runTest {
        // Given
        val key = "test_key"
        val value = "test_value"
        val expirationTime = System.currentTimeMillis() + 86400000
        val createdAt = System.currentTimeMillis()
        
        // When
        databaseManager.insertCache(key, value, expirationTime, createdAt)
        val retrievedCache = databaseManager.getCache(key, System.currentTimeMillis())
        
        // Then
        assertNotNull(retrievedCache)
        assertEquals(key, retrievedCache.key)
        assertEquals(value, retrievedCache.value)
        assertEquals(expirationTime, retrievedCache.expirationTime)
    }
    
    @Test
    fun testExpiredCacheNotRetrieved() = runTest {
        // Given
        val key = "test_key"
        val value = "test_value"
        val expirationTime = System.currentTimeMillis() - 1000 // Expired
        val createdAt = System.currentTimeMillis() - 2000
        
        // When
        databaseManager.insertCache(key, value, expirationTime, createdAt)
        val retrievedCache = databaseManager.getCache(key, System.currentTimeMillis())
        
        // Then
        assertNull(retrievedCache) // Should be null because it's expired
    }
    
    @Test
    fun testUnreadMessageCount() = runTest {
        // Given
        val chatId = "chat123"
        val senderId = "sender123"
        val currentUserId = "current123"
        val messageId1 = "message1"
        val messageId2 = "message2"
        val timestamp = System.currentTimeMillis()
        
        // When - Insert messages from different sender
        databaseManager.insertMessage(messageId1, chatId, senderId, "Message 1", timestamp, "SENT", "TEXT")
        databaseManager.insertMessage(messageId2, chatId, senderId, "Message 2", timestamp + 1000, "DELIVERED", "TEXT")
        
        val unreadCount = databaseManager.getUnreadMessageCount(chatId, currentUserId)
        
        // Then
        assertEquals(2, unreadCount) // Both messages are unread for current user
    }
}