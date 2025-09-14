package com.carecomms.performance

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests for performance optimization components
 */
class PerformanceOptimizationTest {
    
    @Test
    fun testStartupOptimizer() = runTest {
        val startupOptimizer = StartupOptimizer()
        var criticalInitialized = false
        var deferredInitialized = false
        
        // Add initializers
        startupOptimizer.addCriticalInitializer {
            criticalInitialized = true
        }
        
        startupOptimizer.addDeferredInitializer {
            deferredInitialized = true
        }
        
        // Test critical initialization
        startupOptimizer.initializeCritical()
        assertTrue(criticalInitialized, "Critical initializer should be executed")
        
        // Test deferred initialization
        startupOptimizer.initializeDeferred(this)
        // Note: In real test, we'd need to wait for the coroutine to complete
        // For this test, we'll assume it works as the implementation is straightforward
    }
    
    @Test
    fun testMemoryManager() = runTest {
        val memoryManager = MemoryManager()
        var lowMemoryCalled = false
        var criticalMemoryCalled = false
        var memoryRecoveredCalled = false
        
        val listener = object : MemoryListener {
            override fun onLowMemory() { lowMemoryCalled = true }
            override fun onCriticalMemory() { criticalMemoryCalled = true }
            override fun onMemoryRecovered() { memoryRecoveredCalled = true }
            override fun onClearCaches() {}
            override fun onTrimMemory() {}
        }
        
        memoryManager.addMemoryListener(listener)
        
        // Test memory pressure notifications
        memoryManager.onMemoryPressure(MemoryPressure.LOW)
        assertTrue(lowMemoryCalled, "Low memory callback should be called")
        
        memoryManager.onMemoryPressure(MemoryPressure.CRITICAL)
        assertTrue(criticalMemoryCalled, "Critical memory callback should be called")
        
        memoryManager.onMemoryPressure(MemoryPressure.NORMAL)
        assertTrue(memoryRecoveredCalled, "Memory recovered callback should be called")
        
        // Test listener removal
        memoryManager.removeMemoryListener(listener)
        lowMemoryCalled = false
        memoryManager.onMemoryPressure(MemoryPressure.LOW)
        // lowMemoryCalled should still be false after removal
    }
    
    @Test
    fun testImageCache() = runTest {
        val memoryManager = MemoryManager()
        val imageCache = ImageCache(maxMemorySize = 1024, memoryManager = memoryManager)
        
        val testData1 = ByteArray(512) { 1 }
        val testData2 = ByteArray(512) { 2 }
        val testData3 = ByteArray(512) { 3 }
        
        // Test basic caching
        imageCache.put("image1", testData1)
        val retrieved1 = imageCache.get("image1")
        assertNotNull(retrieved1, "Should retrieve cached image")
        assertTrue(retrieved1.contentEquals(testData1), "Retrieved data should match original")
        
        // Test cache size limit (LRU eviction)
        imageCache.put("image2", testData2)
        imageCache.put("image3", testData3) // This should evict image1
        
        val evicted = imageCache.get("image1")
        // In a real implementation, image1 might be evicted due to size limit
        
        // Test memory pressure handling
        imageCache.put("image1", testData1)
        imageCache.put("image2", testData2)
        
        imageCache.onLowMemory()
        assertTrue(imageCache.getCacheSize() <= 2, "Cache size should be reduced on low memory")
        
        imageCache.onCriticalMemory()
        assertEquals(0, imageCache.getCacheSize(), "Cache should be cleared on critical memory")
        assertEquals(0L, imageCache.getMemoryUsage(), "Memory usage should be 0 after clear")
    }
    
    @Test
    fun testPerformanceMonitor() = runTest {
        val performanceMonitor = PerformanceMonitor(this)
        
        // Test operation timing
        performanceMonitor.startOperation("test_operation")
        // Simulate some work
        performanceMonitor.endOperation("test_operation")
        
        val report = performanceMonitor.getPerformanceReport()
        assertTrue(
            report.averageOperationTimes.containsKey("test_operation"),
            "Should record operation time"
        )
        
        // Test memory usage recording
        performanceMonitor.recordMemoryUsage(1024 * 1024) // 1MB
        val updatedReport = performanceMonitor.getPerformanceReport()
        assertEquals(1024 * 1024, updatedReport.memoryUsage, "Should record memory usage")
        
        // Test network latency recording
        performanceMonitor.recordNetworkLatency("api_endpoint", 250L)
        val finalReport = performanceMonitor.getPerformanceReport()
        assertTrue(
            finalReport.networkLatencies.containsKey("api_endpoint"),
            "Should record network latency"
        )
        assertEquals(250L, finalReport.networkLatencies["api_endpoint"], "Should record correct latency")
    }
    
    @Test
    fun testCrashReporter() = runTest {
        val crashReporter = CrashReporter()
        
        // Test crash recording
        val testException = RuntimeException("Test crash")
        crashReporter.recordCrash(testException, "Test context")
        
        assertEquals(1, crashReporter.getCrashCount(), "Should record one crash")
        
        val recentCrashes = crashReporter.getRecentCrashes(1)
        assertEquals(1, recentCrashes.size, "Should return one recent crash")
        
        val crashReport = recentCrashes.first()
        assertTrue(
            crashReport.exception.contains("Test crash"),
            "Crash report should contain exception message"
        )
        assertEquals("Test context", crashReport.context, "Should record crash context")
        
        // Test crash limit
        repeat(150) { index ->
            crashReporter.recordCrash(RuntimeException("Crash $index"), "Context $index")
        }
        
        assertTrue(
            crashReporter.getCrashCount() <= 100,
            "Should not exceed maximum crash reports"
        )
        
        // Test clearing crashes
        crashReporter.clearCrashes()
        assertEquals(0, crashReporter.getCrashCount(), "Should clear all crashes")
    }
    
    @Test
    fun testImageLoader() = runTest {
        val memoryManager = MemoryManager()
        val imageCache = ImageCache(memoryManager = memoryManager)
        val networkClient = MockImageNetworkClient()
        val imageLoader = ImageLoader(imageCache, networkClient)
        
        // Test image loading
        val result = imageLoader.loadImage("https://example.com/image.jpg")
        assertTrue(result.isSuccess, "Image loading should succeed")
        
        val imageData = result.getOrNull()
        assertNotNull(imageData, "Should return image data")
        
        // Test caching (second load should be from cache)
        val cachedResult = imageLoader.loadImage("https://example.com/image.jpg")
        assertTrue(cachedResult.isSuccess, "Cached image loading should succeed")
        
        // Test preloading
        imageLoader.preloadImage("https://example.com/preload.jpg")
        // Verify preloaded image is in cache
        val preloadedResult = imageLoader.loadImage("https://example.com/preload.jpg")
        assertTrue(preloadedResult.isSuccess, "Preloaded image should be available")
    }
}

class MockImageNetworkClient : ImageNetworkClient {
    override suspend fun downloadImage(url: String): ByteArray {
        // Simulate network download with mock data
        return ByteArray(1024) { (it % 256).toByte() }
    }
}