package com.carecomms.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.carecomms.data.database.DatabaseManager
import com.carecomms.data.models.*
import com.carecomms.database.CareCommsDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.*

class LocalChatRepositoryTest {
    
    private lateinit var driver: SqlDriver
    private lateinit var database: CareCommsDatabase
    private lateinit var databaseManager: DatabaseManager
    private lateinit var userRepository: LocalUserRepositoryImpl
    private lateinit var chatRepository: LocalChatRepository
    private val json = Json { ignoreUnknownKeys = true }
    
    @BeforeTest
    fun setup() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        CareCommsDatabase.Schema.create(driver)
        database = CareCommsDatabase(driver)
        databaseManager = DatabaseManager(database)
        userRepository = LocalUserRepositoryImpl(databaseManager, json)
        chatRepository = LocalChatRepository(databaseManager, json)
    }
    
    @AfterTest
    fun teardown() {
        driver.close()
    }
    
    @Test
    fun testCreateChat() = runTest {
        // Given
        val carerId = "carer123"
        val careeId = "caree123"
        
        // When
        val result = chatRepository.createChat(carerId, careeId)
        
        // Then
        assertTrue(result.isSuccess)
        val chatId = result.getOrNull()
        assertNotNull(chatId)
        
        // Verify chat was created in database
        val chat = databaseManager.getChatById(chatId)
        assertNotNull(chat)
        assertEquals(carerId, chat.carerId)
        assertEquals(careeId, chat.careeId)
    }
    
    @Test
    fun testCreateChatAlreadyExists() = runTest {
        // Given
        val carerId = "carer123"
        val careeId = "caree123"
        
        // When - Create chat twice
        val result1 = chatRepository.createChat(carerId, careeId)
        val result2 = chatRepository.createChat(carerId, careeId)
        
        // Then - Should return same chat ID
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        assertEquals(result1.getOrNull(), result2.getOrNull())
    }
    
    @Test
    fun testSendMessage() = runTest {
        // Given
        val chatId = "chat123"
        val carerId = "carer123"
        val careeId = "caree123"
        val currentTime = System.currentTimeMillis()
        
        // Create chat first
        databaseManager.insertChat(chatId, carerId, careeId, currentTime, currentTime)
        
        val message = Message(
            id = "message123",
            senderId = carerId,
            content = "Hello, how are you?",
            timestamp = currentTime,
            status = MessageStatus.SENT,
            type = MessageType.TEXT
        )
        
        // When
        val result = chatRepository.sendMessage(chatId, message)
        
        // Then
        assertTrue(result.isSuccess)
        
        // Verify message was stored
        val messages = chatRepository.getMessages(chatId).first()
        assertEquals(1, messages.size)
        val storedMessage = messages.first()
        assertEquals(message.id, storedMessage.id)
        assertEquals(message.senderId, storedMessage.senderId)
        assertEquals(message.content, storedMessage.content)
        assertEquals(message.status, storedMessage.status)
        assertEquals(message.type, storedMessage.type)
    }
    
    @Test
    fun testGetMessages() = runTest {
        // Given
        val chatId = "chat123"
        val carerId = "carer123"
        val careeId = "caree123"
        val currentTime = System.currentTimeMillis()
        
        // Create chat and messages
        databaseManager.insertChat(chatId, carerId, careeId, currentTime, currentTime)
        databaseManager.insertMessage("msg1", chatId, carerId, "Hello", currentTime, "SENT", "TEXT")
        databaseManager.insertMessage("msg2", chatId, careeId, "Hi there", currentTime + 1000, "DELIVERED", "TEXT")
        
        // When
        val messages = chatRepository.getMessages(chatId).first()
        
        // Then
        assertEquals(2, messages.size)
        assertEquals("msg1", messages[0].id)
        assertEquals("msg2", messages[1].id)
        assertEquals("Hello", messages[0].content)
        assertEquals("Hi there", messages[1].content)
    }
    
    @Test
    fun testMarkAsRead() = runTest {
        // Given
        val chatId = "chat123"
        val messageId = "message123"
        val carerId = "carer123"
        val careeId = "caree123"
        val currentTime = System.currentTimeMillis()
        
        // Create chat and message
        databaseManager.insertChat(chatId, carerId, careeId, currentTime, currentTime)
        databaseManager.insertMessage(messageId, chatId, carerId, "Hello", currentTime, "SENT", "TEXT")
        
        // When
        val result = chatRepository.markAsRead(chatId, messageId)
        
        // Then
        assertTrue(result.isSuccess)
        
        // Verify message status was updated
        val messages = chatRepository.getMessages(chatId).first()
        assertEquals(MessageStatus.READ, messages.first().status)
    }
    
    @Test
    fun testGetChatId() = runTest {
        // Given
        val chatId = "chat123"
        val carerId = "carer123"
        val careeId = "caree123"
        val currentTime = System.currentTimeMillis()
        
        // Create chat
        databaseManager.insertChat(chatId, carerId, careeId, currentTime, currentTime)
        
        // When
        val retrievedChatId = chatRepository.getChatId(carerId, careeId)
        
        // Then
        assertEquals(chatId, retrievedChatId)
    }
    
    @Test
    fun testGetChatIdNotFound() = runTest {
        // Given
        val carerId = "carer123"
        val careeId = "caree123"
        
        // When
        val retrievedChatId = chatRepository.getChatId(carerId, careeId)
        
        // Then
        assertNull(retrievedChatId)
    }
    
    @Test
    fun testGetChatList() = runTest {
        // Given
        val carerId = "carer123"
        val careeId = "caree123"
        val chatId = "chat123"
        val currentTime = System.currentTimeMillis()
        
        // Create caree user
        val caree = Caree(
            id = careeId,
            email = "caree@example.com",
            createdAt = currentTime,
            healthInfo = "Good health",
            personalDetails = PersonalDetails(
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = "1950-01-01"
            ),
            carerId = carerId
        )
        userRepository.insertUser(caree)
        
        // Create chat and message
        databaseManager.insertChat(chatId, carerId, careeId, currentTime, currentTime)
        databaseManager.insertMessage("msg1", chatId, careeId, "Hello carer", currentTime, "SENT", "TEXT")
        
        // When
        val chatList = chatRepository.getChatList(carerId).first()
        
        // Then
        assertEquals(1, chatList.size)
        val chatPreview = chatList.first()
        assertEquals(chatId, chatPreview.chatId)
        assertEquals("John Doe", chatPreview.careeName)
        assertEquals("Hello carer", chatPreview.lastMessage)
        assertEquals(currentTime, chatPreview.lastMessageTime)
    }
    
    @Test
    fun testSearchChats() = runTest {
        // Given
        val carerId = "carer123"
        val careeId1 = "caree123"
        val careeId2 = "caree456"
        val chatId1 = "chat123"
        val chatId2 = "chat456"
        val currentTime = System.currentTimeMillis()
        
        // Create caree users
        val caree1 = Caree(
            id = careeId1,
            email = "john@example.com",
            createdAt = currentTime,
            healthInfo = "Good health",
            personalDetails = PersonalDetails(
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = "1950-01-01"
            ),
            carerId = carerId
        )
        val caree2 = Caree(
            id = careeId2,
            email = "jane@example.com",
            createdAt = currentTime,
            healthInfo = "Good health",
            personalDetails = PersonalDetails(
                firstName = "Jane",
                lastName = "Smith",
                dateOfBirth = "1955-01-01"
            ),
            carerId = carerId
        )
        userRepository.insertUser(caree1)
        userRepository.insertUser(caree2)
        
        // Create chats
        databaseManager.insertChat(chatId1, carerId, careeId1, currentTime, currentTime)
        databaseManager.insertChat(chatId2, carerId, careeId2, currentTime, currentTime)
        databaseManager.insertMessage("msg1", chatId1, careeId1, "Hello from John", currentTime, "SENT", "TEXT")
        databaseManager.insertMessage("msg2", chatId2, careeId2, "Hello from Jane", currentTime, "SENT", "TEXT")
        
        // When
        val searchResults = chatRepository.searchChats(carerId, "John").first()
        
        // Then
        assertEquals(1, searchResults.size)
        assertEquals("John Doe", searchResults.first().careeName)
    }
}