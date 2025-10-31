package com.carecomms.performance

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PerformanceMonitor(
    private val scope: CoroutineScope
) {
    
    private val _metrics = MutableStateFlow(PerformanceMetrics())
    val metrics: StateFlow<PerformanceMetrics> = _metrics.asStateFlow()
    
    private var isMonitoring = false
    
    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        scope.launch {
            while (isMonitoring) {
                updateMetrics()
                delay(5000) // Update every 5 seconds
            }
        }
    }
    
    fun stopMonitoring() {
        isMonitoring = false
    }
    
    private fun updateMetrics() {
        val runtime = Runtime.getRuntime()
        val currentMetrics = PerformanceMetrics(
            memoryUsed = runtime.totalMemory() - runtime.freeMemory(),
            memoryMax = runtime.maxMemory(),
            memoryFree = runtime.freeMemory(),
            timestamp = System.currentTimeMillis()
        )
        _metrics.value = currentMetrics
    }
    
    fun recordEvent(event: String, duration: Long) {
        // Log performance events for analysis
        println("Performance Event: $event took ${duration}ms")
    }
    
    fun recordScreenLoad(screenName: String, loadTime: Long) {
        recordEvent("Screen Load: $screenName", loadTime)
    }
    
    fun recordNetworkRequest(url: String, duration: Long, success: Boolean) {
        val status = if (success) "SUCCESS" else "FAILED"
        recordEvent("Network Request: $url ($status)", duration)
    }
}

data class PerformanceMetrics(
    val memoryUsed: Long = 0,
    val memoryMax: Long = 0,
    val memoryFree: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    val memoryUsagePercentage: Float
        get() = if (memoryMax > 0) (memoryUsed.toFloat() / memoryMax.toFloat()) * 100f else 0f
}