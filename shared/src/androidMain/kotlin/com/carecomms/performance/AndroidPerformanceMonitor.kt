package com.carecomms.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import kotlinx.coroutines.CoroutineScope

/**
 * Android-specific performance monitoring implementation
 */
class AndroidPerformanceMonitor(
    private val context: Context,
    scope: CoroutineScope
) : PerformanceMonitor(scope) {
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    
    fun getAndroidMemoryInfo(): AndroidMemoryInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val runtime = Runtime.getRuntime()
        val nativeHeapSize = Debug.getNativeHeapSize()
        val nativeHeapAllocatedSize = Debug.getNativeHeapAllocatedSize()
        val nativeHeapFreeSize = Debug.getNativeHeapFreeSize()
        
        return AndroidMemoryInfo(
            availableMemory = memoryInfo.availMem,
            totalMemory = memoryInfo.totalMem,
            lowMemory = memoryInfo.lowMemory,
            threshold = memoryInfo.threshold,
            jvmMaxMemory = runtime.maxMemory(),
            jvmTotalMemory = runtime.totalMemory(),
            jvmFreeMemory = runtime.freeMemory(),
            jvmUsedMemory = runtime.totalMemory() - runtime.freeMemory(),
            nativeHeapSize = nativeHeapSize,
            nativeHeapAllocatedSize = nativeHeapAllocatedSize,
            nativeHeapFreeSize = nativeHeapFreeSize
        )
    }
    
    fun isLowMemory(): Boolean {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.lowMemory
    }
    
    fun getAppMemoryUsage(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }
}

data class AndroidMemoryInfo(
    val availableMemory: Long,
    val totalMemory: Long,
    val lowMemory: Boolean,
    val threshold: Long,
    val jvmMaxMemory: Long,
    val jvmTotalMemory: Long,
    val jvmFreeMemory: Long,
    val jvmUsedMemory: Long,
    val nativeHeapSize: Long,
    val nativeHeapAllocatedSize: Long,
    val nativeHeapFreeSize: Long
)