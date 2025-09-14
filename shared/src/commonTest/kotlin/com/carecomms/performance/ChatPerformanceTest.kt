package com.carecomms.performance

import com.carecomms.data.models.Message
import com.carecomms.data.models.MessageStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.measureTime

/**
 * Performance tests for chat real-time synchronization and large data handling
 */
class ChatPerformanceTest {
    
    @Test
    fun testChatRealTimeSyncPerformance() = runTest {
        val chatUseCase = MockChatUseCase()
        val chatId = "performance_test_chat"
        val messageCount = 100
        
        // Measure time to send multiple messages
        val sendTime = measureTime {
            repeat(messageCount) { index ->
                val message = createTestMessage("msg_$index", "user1", "Message $index")
                val result = chatUseCase.sendMessage(chatId, message)
                assertTrue(result.isSuccess, "Message $index should be sent successfully")
            }
        }
        
        println("Time to send $messageCount messages: ${sendTime.inWholeMilliseconds}ms")
        
        // Measure time to retrieve all messages
        val retrieveTime = measureTime {
            val result = chatUseCase.getMessages(chatId)
            assertTrue(result.isSuccess, "Should retrieve messages successfully")
            val messages = result.getOrNull()!!
            assertTrue(messages.size == messageCount, "Should retrieve all $messageCount messages")
        }
        
        println("Time to retrieve $messageCount messages: ${retrieveTime.inWholeMilliseconds}ms")
        
        // Performance assertions
        assertTrue(
            sendTime.inWholeMilliseconds < 5000, // 5 seconds max for 100 messages
            "Sending messages should complete within 5 seconds"
        )
        assertTrue(
            retrieveTime.inWholeMilliseconds < 1000, // 1 second max for retrieval
            "Retrieving messages should complete within 1 second"
        )
    }
    
    @Test
    fun testConcurrentMessageSending() = runTest {
        val chatUseCase = MockChatUseCase()
        val chatId = "concurrent_test_chat"
        val concurrentUsers = 10
        val messagesPerUser = 20
        
        val totalTime = measureTime {
            // Simulate multiple users sending messages concurrently
            val jobs = (1..concurrentUsers).map { userId ->
                async {
                    repeat(messagesPerUser) { messageIndex ->
                        val message = createTestMessage(
                            "user${userId}_msg_$messageIndex",
                            "user$userId",
                            "Message $messageIndex from user $userId"
                        )
                        chatUseCase.sendMessage(chatId, message)
                    }
                }
            }
            jobs.awaitAll()
        }
        
        println("Time for $concurrentUsers users to send ${messagesPerUser * concurrentUsers} messages: ${totalTime.inWholeMilliseconds}ms")
        
        // Verify all messages were sent
        val messages = chatUseCase.getMessages(chatId).getOrNull()!!
        assertTrue(
            messages.size == concurrentUsers * messagesPerUser,
            "Should have ${concurrentUsers * messagesPerUser} messages, but got ${messages.size}"
        )
        
        // Performance assertion
        assertTrue(
            totalTime.inWholeMilliseconds < 10000, // 10 seconds max
            "Concurrent message sending should complete within 10 seconds"
        )
    }
    
    @Test
    fun testLargeDataSetHandling() = runTest {
        val chatUseCase = MockChatUseCase()
        val chatId = "large_dataset_chat"
        val largeMessageCount = 1000
        
        // Create a large dataset
        val setupTime = measureTime {
            repeat(largeMessageCount) { index ->
                val message = createTestMessage(
                    "large_msg_$index",
                    "user${index % 10}", // 10 different users
                    "This is a longer message content to simulate real chat messages with more text content. Message number $index."
                )
                chatUseCase.sendMessage(chatId, message)
            }
        }
        
        println("Time to create dataset of $largeMessageCount messages: ${setupTime.inWholeMilliseconds}ms")
        
        // Test pagination performance
        val paginationTime = measureTime {
            var offset = 0
            val pageSize = 50
            var totalRetrieved = 0
            
            while (offset < largeMessageCount) {
                val result = chatUseCase.getMessagesPaginated(chatId, offset, pageSize)
                assertTrue(result.isSuccess, "Paginated retrieval should succeed")
                val messages = result.getOrNull()!!
                totalRetrieved += messages.size
                offset += pageSize
            }
            
            assertTrue(
                totalRetrieved == largeMessageCount,
                "Should retrieve all $largeMessageCount messages via pagination"
            )
        }
        
        println("Time to paginate through $largeMessageCount messages: ${paginationTime.inWholeMilliseconds}ms")
        
        // Test search performance
        val searchTime = measureTime {
            val searchResult = chatUseCase.searchMessages(chatId, "Message number 500")
            assertTrue(searchResult.isSuccess, "Search should succeed")
            val foundMessages = searchResult.getOrNull()!!
            assertTrue(foundMessages.isNotEmpty(), "Should find matching messages")
        }
        
        println("Time to search in $largeMessageCount messages: ${searchTime.inWholeMilliseconds}ms")
        
        // Performance assertions
        assertTrue(
            paginationTime.inWholeMilliseconds < 5000,
            "Pagination should complete within 5 seconds"
        )
        assertTrue(
            searchTime.inWholeMilliseconds < 2000,
            "Search should complete within 2 seconds"
        )
    }
    
    @Test
    fun testMemoryUsageWithLargeDataSet() = runTest {
        val memoryManager = MemoryManager()
        val imageCache = ImageCache(memoryManager = memoryManager)
        
        // Simulate loading many images
        val imageCount = 100
        val imageSize = 1024 * 1024 // 1MB per image
        
        val loadTime = measureTime {
            repeat(imageCount) { index ->
                val imageData = ByteArray(imageSize) { (it % 256).toByte() }
                imageCache.put("image_$index", imageData)
            }
        }
        
        println("Time to cache $imageCount images (${imageCount}MB): ${loadTime.inWholeMilliseconds}ms")
        
        val memoryUsage = imageCache.getMemoryUsage()
        println("Memory usage after caching: ${memoryUsage / (1024 * 1024)}MB")
        
        // Test memory pressure handling
        memoryManager.onMemoryPressure(MemoryPressure.LOW)
        
        val memoryAfterPressure = imageCache.getMemoryUsage()
        println("Memory usage after low memory pressure: ${memoryAfterPressure / (1024 * 1024)}MB")
        
        assertTrue(
            memoryAfterPressure < memoryUsage,
            "Memory usage should decrease after memory pressure"
        )
        
        // Performance assertion
        assertTrue(
            loadTime.inWholeMilliseconds < 3000,
            "Image caching should complete within 3 seconds"
        )
    }
    
    @Test
    fun testNetworkLatencySimulation() = runTest {
        val performanceMonitor = PerformanceMonitor(this)
        val chatUseCase = MockChatUseCase()
        
        // Simulate various network conditions
        val networkConditions = listOf(
            "fast" to 50L,    // 50ms latency
            "medium" to 200L, // 200ms latency
            "slow" to 1000L   // 1000ms latency
        )
        
        networkConditions.forEach { (condition, latency) ->
            performanceMonitor.startOperation("send_message_$condition")
            
            // Simulate network latency
            delay(latency)
            
            val message = createTestMessage("test_$condition", "user1", "Test message for $condition network")
            val result = chatUseCase.sendMessage("test_chat", message)
            assertTrue(result.isSuccess, "Message should be sent successfully on $condition network")
            
            performanceMonitor.endOperation("send_message_$condition")
            performanceMonitor.recordNetworkLatency("chat_endpoint", latency)
        }
        
        val report = performanceMonitor.getPerformanceReport()
        
        // Verify performance monitoring
        assertTrue(
            report.networkLatencies.isNotEmpty(),
            "Should record network latencies"
        )
        assertTrue(
            report.averageOperationTimes.isNotEmpty(),
            "Should record operation times"
        )
        
        println("Performance report: $report")
    }
    
    private fun createTestMessage(id: String, senderId: String, content: String) = Message(
        id = id,
        senderId = senderId,
        content = content,
        timestamp = System.currentTimeMillis(),
        status = MessageStatus.SENT
    )
}

// Extended mock for performance testing
class MockChatUseCase {
    private val messages = mutableMapOf<String, MutableList<Message>>()
    
    suspend fun sendMessage(chatId: String, message: Message): Result<Unit> {
        messages.getOrPut(chatId) { mutableListOf() }.add(message)
        return Result.success(Unit)
    }
    
    suspend fun getMessages(chatId: String): Result<List<Message>> {
        return Result.success(messages[chatId] ?: emptyList())
    }
    
    suspend fun getMessagesPaginated(chatId: String, offset: Int, limit: Int): Result<List<Message>> {
        val chatMessages = messages[chatId] ?: emptyList()
        val endIndex = minOf(offset + limit, chatMessages.size)
        val pageMessages = if (offset < chatMessages.size) {
            chatMessages.subList(offset, endIndex)
        } else {
            emptyList()
        }
        return Result.success(pageMessages)
    }
    
    suspend fun searchMessages(chatId: String, query: String): Result<List<Message>> {
        val chatMessages = messages[chatId] ?: emptyList()
        val matchingMessages = chatMessages.filter { it.content.contains(query, ignoreCase = true) }
        return Result.success(matchingMessages)
    }
}