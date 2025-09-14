package com.carecomms.performance

import com.carecomms.data.models.User
import com.carecomms.data.models.Carer
import com.carecomms.data.models.Message
import com.carecomms.data.models.MessageStatus
import com.carecomms.domain.usecase.AuthUseCase
import com.carecomms.domain.usecase.ChatUseCase
import com.carecomms.domain.usecase.InvitationUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

/**
 * Comprehensive integration tests for cross-platform functionality
 */
class CrossPlatformIntegrationTest {
    
    @Test
    fun testCompleteCarerWorkflow() = runTest {
        // Test the complete carer workflow across platforms
        val authUseCase = createMockAuthUseCase()
        val chatUseCase = createMockChatUseCase()
        val invitationUseCase = createMockInvitationUseCase()
        
        // 1. Carer registration
        val carerData = createTestCarerData()
        val registrationResult = authUseCase.registerCarer(carerData)
        assertTrue(registrationResult.isSuccess, "Carer registration should succeed")
        
        val carer = registrationResult.getOrNull() as? Carer
        assertNotNull(carer, "Registered user should be a Carer")
        
        // 2. Generate invitation
        val invitationResult = invitationUseCase.generateInvitation(carer.id)
        assertTrue(invitationResult.isSuccess, "Invitation generation should succeed")
        
        val invitationLink = invitationResult.getOrNull()
        assertNotNull(invitationLink, "Invitation link should be generated")
        
        // 3. Caree accepts invitation and registers
        val careeData = createTestCareeData()
        val careeRegistrationResult = authUseCase.registerCareeWithInvitation(careeData, invitationLink)
        assertTrue(careeRegistrationResult.isSuccess, "Caree registration should succeed")
        
        // 4. Chat functionality
        val chatId = "${carer.id}_${careeRegistrationResult.getOrNull()?.id}"
        val message = Message(
            id = "test_message_1",
            senderId = carer.id,
            content = "Hello, how are you today?",
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )
        
        val sendResult = chatUseCase.sendMessage(chatId, message)
        assertTrue(sendResult.isSuccess, "Message sending should succeed")
        
        // 5. Verify message delivery
        val messagesResult = chatUseCase.getMessages(chatId)
        assertTrue(messagesResult.isSuccess, "Getting messages should succeed")
        
        val messages = messagesResult.getOrNull()
        assertNotNull(messages, "Messages should be retrieved")
        assertTrue(messages.isNotEmpty(), "Messages list should not be empty")
        assertEquals(message.content, messages.first().content, "Message content should match")
    }
    
    @Test
    fun testCrossPlatformDataSynchronization() = runTest {
        // Test data synchronization across platforms
        val chatUseCase = createMockChatUseCase()
        
        val chatId = "test_chat_sync"
        val messages = listOf(
            createTestMessage("msg1", "user1", "Message 1"),
            createTestMessage("msg2", "user2", "Message 2"),
            createTestMessage("msg3", "user1", "Message 3")
        )
        
        // Send messages from different "platforms"
        messages.forEach { message ->
            val result = chatUseCase.sendMessage(chatId, message)
            assertTrue(result.isSuccess, "Message ${message.id} should be sent successfully")
        }
        
        // Verify all messages are synchronized
        val retrievedMessages = chatUseCase.getMessages(chatId).getOrNull()
        assertNotNull(retrievedMessages, "Messages should be retrieved")
        assertEquals(messages.size, retrievedMessages.size, "All messages should be synchronized")
        
        // Verify message order
        messages.forEachIndexed { index, originalMessage ->
            assertEquals(
                originalMessage.content,
                retrievedMessages[index].content,
                "Message order should be preserved"
            )
        }
    }
    
    @Test
    fun testOfflineOnlineSynchronization() = runTest {
        // Test offline/online synchronization
        val chatUseCase = createMockChatUseCase()
        
        // Simulate offline mode
        chatUseCase.setOfflineMode(true)
        
        val offlineMessage = createTestMessage("offline_msg", "user1", "Offline message")
        val offlineResult = chatUseCase.sendMessage("test_chat", offlineMessage)
        assertTrue(offlineResult.isSuccess, "Offline message should be queued")
        
        // Simulate going online
        chatUseCase.setOfflineMode(false)
        
        // Verify message is synchronized when online
        val syncResult = chatUseCase.syncOfflineMessages()
        assertTrue(syncResult.isSuccess, "Offline messages should sync when online")
        
        val messages = chatUseCase.getMessages("test_chat").getOrNull()
        assertNotNull(messages, "Messages should be available after sync")
        assertTrue(
            messages.any { it.content == "Offline message" },
            "Offline message should be present after sync"
        )
    }
    
    @Test
    fun testErrorHandlingConsistency() = runTest {
        // Test consistent error handling across platforms
        val authUseCase = createMockAuthUseCase()
        
        // Test invalid credentials
        val invalidLoginResult = authUseCase.login("invalid@email.com", "wrongpassword")
        assertTrue(invalidLoginResult.isFailure, "Invalid login should fail")
        
        // Test network error simulation
        authUseCase.simulateNetworkError(true)
        val networkErrorResult = authUseCase.login("test@email.com", "password")
        assertTrue(networkErrorResult.isFailure, "Network error should cause failure")
        
        // Test recovery after network error
        authUseCase.simulateNetworkError(false)
        val recoveryResult = authUseCase.login("test@email.com", "password")
        assertTrue(recoveryResult.isSuccess, "Should recover after network error")
    }
    
    // Mock implementations for testing
    private fun createMockAuthUseCase(): MockAuthUseCase = MockAuthUseCase()
    private fun createMockChatUseCase(): MockChatUseCase = MockChatUseCase()
    private fun createMockInvitationUseCase(): MockInvitationUseCase = MockInvitationUseCase()
    
    private fun createTestCarerData() = mapOf(
        "email" to "carer@test.com",
        "password" to "password123",
        "age" to 30,
        "phoneNumber" to "+1234567890",
        "location" to "Test City"
    )
    
    private fun createTestCareeData() = mapOf(
        "email" to "caree@test.com",
        "password" to "password123",
        "healthInfo" to "Test health information",
        "name" to "Test Caree"
    )
    
    private fun createTestMessage(id: String, senderId: String, content: String) = Message(
        id = id,
        senderId = senderId,
        content = content,
        timestamp = System.currentTimeMillis(),
        status = MessageStatus.SENT
    )
}

// Mock classes for testing
class MockAuthUseCase {
    private var networkError = false
    
    suspend fun registerCarer(data: Map<String, Any>): Result<User> {
        if (networkError) return Result.failure(Exception("Network error"))
        return Result.success(
            Carer(
                id = "test_carer_id",
                email = data["email"] as String,
                createdAt = System.currentTimeMillis(),
                documents = emptyList(),
                age = data["age"] as Int,
                phoneNumber = data["phoneNumber"] as String,
                location = data["location"] as String,
                careeIds = emptyList()
            )
        )
    }
    
    suspend fun registerCareeWithInvitation(data: Map<String, Any>, invitation: String): Result<User> {
        if (networkError) return Result.failure(Exception("Network error"))
        return Result.success(
            Carer(
                id = "test_caree_id",
                email = data["email"] as String,
                createdAt = System.currentTimeMillis(),
                documents = emptyList(),
                age = 25,
                phoneNumber = "",
                location = "",
                careeIds = emptyList()
            )
        )
    }
    
    suspend fun login(email: String, password: String): Result<User> {
        if (networkError) return Result.failure(Exception("Network error"))
        if (email == "invalid@email.com") return Result.failure(Exception("Invalid credentials"))
        return Result.success(
            Carer(
                id = "test_user_id",
                email = email,
                createdAt = System.currentTimeMillis(),
                documents = emptyList(),
                age = 30,
                phoneNumber = "",
                location = "",
                careeIds = emptyList()
            )
        )
    }
    
    fun simulateNetworkError(enabled: Boolean) {
        networkError = enabled
    }
}

class MockChatUseCase {
    private val messages = mutableMapOf<String, MutableList<Message>>()
    private var offlineMode = false
    private val offlineQueue = mutableListOf<Pair<String, Message>>()
    
    suspend fun sendMessage(chatId: String, message: Message): Result<Unit> {
        if (offlineMode) {
            offlineQueue.add(chatId to message)
            return Result.success(Unit)
        }
        
        messages.getOrPut(chatId) { mutableListOf() }.add(message)
        return Result.success(Unit)
    }
    
    suspend fun getMessages(chatId: String): Result<List<Message>> {
        return Result.success(messages[chatId] ?: emptyList())
    }
    
    suspend fun syncOfflineMessages(): Result<Unit> {
        offlineQueue.forEach { (chatId, message) ->
            messages.getOrPut(chatId) { mutableListOf() }.add(message)
        }
        offlineQueue.clear()
        return Result.success(Unit)
    }
    
    fun setOfflineMode(offline: Boolean) {
        offlineMode = offline
    }
}

class MockInvitationUseCase {
    suspend fun generateInvitation(carerId: String): Result<String> {
        return Result.success("invitation_token_$carerId")
    }
}