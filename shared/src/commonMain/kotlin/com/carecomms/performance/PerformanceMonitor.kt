package com.carecomms.performance

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Monitors app performance metrics and provides insights
 */
open class PerformanceMonitor(
    private val scope: CoroutineScope
) {
    private val _metrics = MutableStateFlow(PerformanceMetrics())
    val metrics: StateFlow<PerformanceMetrics> = _metrics
    
    private val operationTimes = mutableMapOf<String, Long>()
    private val crashReporter = CrashReporter()
    
    fun startOperation(operationName: String) {
        operationTimes[operationName] = Clock.System.now().toEpochMilliseconds()
    }
    
    fun endOperation(operationName: String) {
        val startTime = operationTimes.remove(operationName)
        if (startTime != null) {
            val duration = Clock.System.now().toEpochMilliseconds() - startTime
            recordOperationTime(operationName, duration)
        }
    }
    
    private fun recordOperationTime(operationName: String, duration: Long) {
        scope.launch(Dispatchers.Default) {
            val currentMetrics = _metrics.value
            val updatedOperations = currentMetrics.operationTimes.toMutableMap()
            updatedOperations[operationName] = duration
            
            _metrics.value = currentMetrics.copy(
                operationTimes = updatedOperations,
                lastUpdated = Clock.System.now().toEpochMilliseconds()
            )
            
            // Log slow operations
            if (duration > SLOW_OPERATION_THRESHOLD) {
                logSlowOperation(operationName, duration)
            }
        }
    }
    
    fun recordMemoryUsage(memoryUsage: Long) {
        scope.launch(Dispatchers.Default) {
            val currentMetrics = _metrics.value
            _metrics.value = currentMetrics.copy(
                memoryUsage = memoryUsage,
                lastUpdated = Clock.System.now().toEpochMilliseconds()
            )
        }
    }
    
    fun recordNetworkLatency(endpoint: String, latency: Long) {
        scope.launch(Dispatchers.Default) {
            val currentMetrics = _metrics.value
            val updatedLatencies = currentMetrics.networkLatencies.toMutableMap()
            updatedLatencies[endpoint] = latency
            
            _metrics.value = currentMetrics.copy(
                networkLatencies = updatedLatencies,
                lastUpdated = Clock.System.now().toEpochMilliseconds()
            )
        }
    }
    
    fun recordCrash(throwable: Throwable, context: String = "") {
        crashReporter.recordCrash(throwable, context)
    }
    
    private fun logSlowOperation(operationName: String, duration: Long) {
        println("PERFORMANCE WARNING: $operationName took ${duration}ms (threshold: ${SLOW_OPERATION_THRESHOLD}ms)")
    }
    
    fun getPerformanceReport(): PerformanceReport {
        val currentMetrics = _metrics.value
        return PerformanceReport(
            averageOperationTimes = calculateAverageOperationTimes(),
            slowOperations = findSlowOperations(),
            memoryUsage = currentMetrics.memoryUsage,
            networkLatencies = currentMetrics.networkLatencies,
            crashCount = crashReporter.getCrashCount(),
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
    }
    
    private fun calculateAverageOperationTimes(): Map<String, Long> {
        // In a real implementation, this would calculate averages over time
        return _metrics.value.operationTimes
    }
    
    private fun findSlowOperations(): List<String> {
        return _metrics.value.operationTimes
            .filter { it.value > SLOW_OPERATION_THRESHOLD }
            .keys.toList()
    }
    
    companion object {
        private const val SLOW_OPERATION_THRESHOLD = 1000L // 1 second
    }
}

data class PerformanceMetrics(
    val operationTimes: Map<String, Long> = emptyMap(),
    val memoryUsage: Long = 0L,
    val networkLatencies: Map<String, Long> = emptyMap(),
    val lastUpdated: Long = 0L
)

data class PerformanceReport(
    val averageOperationTimes: Map<String, Long>,
    val slowOperations: List<String>,
    val memoryUsage: Long,
    val networkLatencies: Map<String, Long>,
    val crashCount: Int,
    val timestamp: Long
)