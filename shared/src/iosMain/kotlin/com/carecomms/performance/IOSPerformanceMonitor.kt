package com.carecomms.performance

import kotlinx.coroutines.CoroutineScope
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSThread

/**
 * iOS-specific performance monitoring implementation
 */
class IOSPerformanceMonitor(
    scope: CoroutineScope
) : PerformanceMonitor(scope) {
    
    fun getIOSMemoryInfo(): IOSMemoryInfo {
        val processInfo = NSProcessInfo.processInfo
        
        return IOSMemoryInfo(
            physicalMemory = processInfo.physicalMemory,
            activeProcessorCount = processInfo.activeProcessorCount.toInt(),
            processorCount = processInfo.processorCount.toInt(),
            systemUptime = processInfo.systemUptime,
            isLowPowerModeEnabled = processInfo.isLowPowerModeEnabled
        )
    }
    
    fun getCurrentThreadInfo(): IOSThreadInfo {
        val currentThread = NSThread.currentThread
        
        return IOSThreadInfo(
            isMainThread = currentThread.isMainThread,
            threadPriority = currentThread.threadPriority,
            qualityOfService = currentThread.qualityOfService.toInt()
        )
    }
    
    fun isLowPowerModeEnabled(): Boolean {
        return NSProcessInfo.processInfo.isLowPowerModeEnabled
    }
}

data class IOSMemoryInfo(
    val physicalMemory: ULong,
    val activeProcessorCount: Int,
    val processorCount: Int,
    val systemUptime: Double,
    val isLowPowerModeEnabled: Boolean
)

data class IOSThreadInfo(
    val isMainThread: Boolean,
    val threadPriority: Double,
    val qualityOfService: Int
)