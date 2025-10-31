package com.carecomms.performance

class MemoryManager {
    
    fun getMemoryUsage(): Long {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
    }
    
    fun getMaxMemory(): Long {
        return Runtime.getRuntime().maxMemory()
    }
    
    fun getAvailableMemory(): Long {
        return Runtime.getRuntime().freeMemory()
    }
    
    fun getMemoryUsagePercentage(): Float {
        val used = getMemoryUsage()
        val max = getMaxMemory()
        return if (max > 0) (used.toFloat() / max.toFloat()) * 100f else 0f
    }
    
    fun requestGarbageCollection() {
        System.gc()
    }
    
    fun isMemoryLow(): Boolean {
        return getMemoryUsagePercentage() > 80f
    }
}