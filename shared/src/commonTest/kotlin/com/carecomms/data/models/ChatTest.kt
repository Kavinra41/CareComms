package com.carecomms.data.models

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ChatTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testMessageSerialization() {
        val message = Message(
            id = "msg123",
            senderId = "user123",
            content = "Hello, how are you?",
            timestamp = 1234567890L,
            status = MessageStatus.DELIVERED,
            type = MessageType.TEXT
        )

        val serialized = json.encodeToString(Message.serializer(), message)
        val deserialized = json.decodeFromString(Message.serializer(), serialized)

        assertEquals(message, deserialized)
    }

    @Test
    fun testChatSerialization() {
        val chat = Chat(
            id = "chat123",
            carerId = "carer123",
            careeId = "caree123",
            createdAt = 1234567890L,
            lastActivity = 1234567900L
        )

        val serialized = json.encodeToString(Chat.serializer(), chat)
        val deserialized = json.decodeFromString(Chat.serializer(), serialized)

        assertEquals(chat, deserialized)
    }

    @Test
    fun testChatPreviewSerialization() {
        val chatPreview = ChatPreview(
            chatId = "chat123",
            careeName = "John Doe",
            lastMessage = "Good morning!",
            lastMessageTime = 1234567890L,
            unreadCount = 3,
            isOnline = true
        )

        val serialized = json.encodeToString(ChatPreview.serializer(), chatPreview)
        val deserialized = json.decodeFromString(ChatPreview.serializer(), serialized)

        assertEquals(chatPreview, deserialized)
    }

    @Test
    fun testTypingStatusSerialization() {
        val typingStatus = TypingStatus(
            userId = "user123",
            isTyping = true,
            timestamp = 1234567890L
        )

        val serialized = json.encodeToString(TypingStatus.serializer(), typingStatus)
        val deserialized = json.decodeFromString(TypingStatus.serializer(), serialized)

        assertEquals(typingStatus, deserialized)
    }

    @Test
    fun testMessageStatusEnum() {
        val statuses = MessageStatus.values()
        assertEquals(3, statuses.size)
        assertTrue(statuses.contains(MessageStatus.SENT))
        assertTrue(statuses.contains(MessageStatus.DELIVERED))
        assertTrue(statuses.contains(MessageStatus.READ))
    }

    @Test
    fun testMessageTypeEnum() {
        val types = MessageType.values()
        assertEquals(3, types.size)
        assertTrue(types.contains(MessageType.TEXT))
        assertTrue(types.contains(MessageType.IMAGE))
        assertTrue(types.contains(MessageType.SYSTEM))
    }

    @Test
    fun testMessageDefaultType() {
        val message = Message(
            id = "msg123",
            senderId = "user123",
            content = "Test message",
            timestamp = 1234567890L,
            status = MessageStatus.SENT
        )

        assertEquals(MessageType.TEXT, message.type)
    }
}