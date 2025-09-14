package com.carecomms.data.repository

import com.carecomms.data.models.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class FirebaseChatRepositoryTest {

    @Test
    fun `Firebase data classes have correct default values`() {
        // Test FirebaseChat
        val firebaseChat = FirebaseChat()
        assertEquals("", firebaseChat.id)
        assertEquals("", firebaseChat.carerId)
        assertEquals("", firebaseChat.careeId)
        assertEquals(0L, firebaseChat.createdAt)
        assertEquals(0L, firebaseChat.lastActivity)

        // Test FirebaseMessage
        val firebaseMessage = FirebaseMessage()
        assertEquals("", firebaseMessage.id)
        assertEquals("", firebaseMessage.senderId)
        assertEquals("", firebaseMessage.content)
        assertEquals(0L, firebaseMessage.timestamp)
        assertEquals("SENT", firebaseMessage.status)
        assertEquals("TEXT", firebaseMessage.type)

        // Test FirebaseTypingStatus
        val firebaseTypingStatus = FirebaseTypingStatus()
        assertEquals("", firebaseTypingStatus.userId)
        assertEquals(false, firebaseTypingStatus.isTyping)
        assertEquals(0L, firebaseTypingStatus.timestamp)

        // Test FirebaseUser
        val firebaseUser = FirebaseUser()
        assertEquals("", firebaseUser.id)
        assertEquals("", firebaseUser.email)
        assertEquals("", firebaseUser.displayName)
        assertEquals("", firebaseUser.userType)
    }

    @Test
    fun `Firebase data classes can be created with custom values`() {
        // Test FirebaseChat with custom values
        val firebaseChat = FirebaseChat(
            id = "chat123",
            carerId = "carer123",
            careeId = "caree123",
            createdAt = 1234567890L,
            lastActivity = 1234567891L
        )
        assertEquals("chat123", firebaseChat.id)
        assertEquals("carer123", firebaseChat.carerId)
        assertEquals("caree123", firebaseChat.careeId)
        assertEquals(1234567890L, firebaseChat.createdAt)
        assertEquals(1234567891L, firebaseChat.lastActivity)

        // Test FirebaseMessage with custom values
        val firebaseMessage = FirebaseMessage(
            id = "msg123",
            senderId = "user123",
            content = "Hello world",
            timestamp = 1234567890L,
            status = "READ",
            type = "IMAGE"
        )
        assertEquals("msg123", firebaseMessage.id)
        assertEquals("user123", firebaseMessage.senderId)
        assertEquals("Hello world", firebaseMessage.content)
        assertEquals(1234567890L, firebaseMessage.timestamp)
        assertEquals("READ", firebaseMessage.status)
        assertEquals("IMAGE", firebaseMessage.type)

        // Test FirebaseTypingStatus with custom values
        val firebaseTypingStatus = FirebaseTypingStatus(
            userId = "user123",
            isTyping = true,
            timestamp = 1234567890L
        )
        assertEquals("user123", firebaseTypingStatus.userId)
        assertEquals(true, firebaseTypingStatus.isTyping)
        assertEquals(1234567890L, firebaseTypingStatus.timestamp)

        // Test FirebaseUser with custom values
        val firebaseUser = FirebaseUser(
            id = "user123",
            email = "test@example.com",
            displayName = "Test User",
            userType = "CARER"
        )
        assertEquals("user123", firebaseUser.id)
        assertEquals("test@example.com", firebaseUser.email)
        assertEquals("Test User", firebaseUser.displayName)
        assertEquals("CARER", firebaseUser.userType)
    }

    @Test
    fun `Message conversion from Firebase format works correctly`() {
        // Given
        val firebaseMessage = FirebaseMessage(
            id = "msg123",
            senderId = "user123",
            content = "Test message",
            timestamp = 1234567890L,
            status = "DELIVERED",
            type = "TEXT"
        )

        // When
        val message = Message(
            id = firebaseMessage.id,
            senderId = firebaseMessage.senderId,
            content = firebaseMessage.content,
            timestamp = firebaseMessage.timestamp,
            status = MessageStatus.valueOf(firebaseMessage.status),
            type = MessageType.valueOf(firebaseMessage.type)
        )

        // Then
        assertEquals("msg123", message.id)
        assertEquals("user123", message.senderId)
        assertEquals("Test message", message.content)
        assertEquals(1234567890L, message.timestamp)
        assertEquals(MessageStatus.DELIVERED, message.status)
        assertEquals(MessageType.TEXT, message.type)
    }

    @Test
    fun `Message conversion to Firebase format works correctly`() {
        // Given
        val message = Message(
            id = "msg123",
            senderId = "user123",
            content = "Test message",
            timestamp = 1234567890L,
            status = MessageStatus.READ,
            type = MessageType.IMAGE
        )

        // When
        val firebaseMessage = FirebaseMessage(
            id = message.id,
            senderId = message.senderId,
            content = message.content,
            timestamp = message.timestamp,
            status = message.status.name,
            type = message.type.name
        )

        // Then
        assertEquals("msg123", firebaseMessage.id)
        assertEquals("user123", firebaseMessage.senderId)
        assertEquals("Test message", firebaseMessage.content)
        assertEquals(1234567890L, firebaseMessage.timestamp)
        assertEquals("READ", firebaseMessage.status)
        assertEquals("IMAGE", firebaseMessage.type)
    }

    @Test
    fun `TypingStatus conversion from Firebase format works correctly`() {
        // Given
        val firebaseTypingStatus = FirebaseTypingStatus(
            userId = "user123",
            isTyping = true,
            timestamp = 1234567890L
        )

        // When
        val typingStatus = TypingStatus(
            userId = firebaseTypingStatus.userId,
            isTyping = firebaseTypingStatus.isTyping,
            timestamp = firebaseTypingStatus.timestamp
        )

        // Then
        assertEquals("user123", typingStatus.userId)
        assertEquals(true, typingStatus.isTyping)
        assertEquals(1234567890L, typingStatus.timestamp)
    }

    @Test
    fun `TypingStatus conversion to Firebase format works correctly`() {
        // Given
        val typingStatus = TypingStatus(
            userId = "user123",
            isTyping = false,
            timestamp = 1234567890L
        )

        // When
        val firebaseTypingStatus = FirebaseTypingStatus(
            userId = typingStatus.userId,
            isTyping = typingStatus.isTyping,
            timestamp = typingStatus.timestamp
        )

        // Then
        assertEquals("user123", firebaseTypingStatus.userId)
        assertEquals(false, firebaseTypingStatus.isTyping)
        assertEquals(1234567890L, firebaseTypingStatus.timestamp)
    }

    @Test
    fun `Chat conversion from Firebase format works correctly`() {
        // Given
        val firebaseChat = FirebaseChat(
            id = "chat123",
            carerId = "carer123",
            careeId = "caree123",
            createdAt = 1234567890L,
            lastActivity = 1234567891L
        )

        // When
        val chat = Chat(
            id = firebaseChat.id,
            carerId = firebaseChat.carerId,
            careeId = firebaseChat.careeId,
            createdAt = firebaseChat.createdAt,
            lastActivity = firebaseChat.lastActivity
        )

        // Then
        assertEquals("chat123", chat.id)
        assertEquals("carer123", chat.carerId)
        assertEquals("caree123", chat.careeId)
        assertEquals(1234567890L, chat.createdAt)
        assertEquals(1234567891L, chat.lastActivity)
    }

    @Test
    fun `Chat conversion to Firebase format works correctly`() {
        // Given
        val chat = Chat(
            id = "chat123",
            carerId = "carer123",
            careeId = "caree123",
            createdAt = 1234567890L,
            lastActivity = 1234567891L
        )

        // When
        val firebaseChat = FirebaseChat(
            id = chat.id,
            carerId = chat.carerId,
            careeId = chat.careeId,
            createdAt = chat.createdAt,
            lastActivity = chat.lastActivity
        )

        // Then
        assertEquals("chat123", firebaseChat.id)
        assertEquals("carer123", firebaseChat.carerId)
        assertEquals("caree123", firebaseChat.careeId)
        assertEquals(1234567890L, firebaseChat.createdAt)
        assertEquals(1234567891L, firebaseChat.lastActivity)
    }
}