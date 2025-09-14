package com.carecomms.performance

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages memory usage and provides memory optimization utilities
 */
class MemoryManager {
    private val _memoryPressure = MutableStateFlow(MemoryPressure.NORMAL)
    val memoryPressure: StateFlow<MemoryPressure> = _memoryPressure
    
    private val memoryListeners = mutableListOf<MemoryListener>()
    
    fun addMemoryListener(listener: MemoryListener) {
        memoryListeners.add(listener)
    }
    
    fun removeMemoryListener(listener: MemoryListener) {
        memoryListeners.remove(listener)
    }
    
    fun onMemoryPressure(pressure: MemoryPressure) {
        _memoryPressure.value = pressure
        memoryListeners.forEach { listener ->
            when (pressure) {
                MemoryPressure.LOW -> listener.onLowMemory()
                MemoryPressure.CRITICAL -> listener.onCriticalMemory()
                MemoryPressure.NORMAL -> listener.onMemoryRecovered()
            }
        }
    }
    
    fun clearCaches() {
        memoryListeners.forEach { it.onClearCaches() }
    }
    
    fun trimMemory() {
        memoryListeners.forEach { it.onTrimMemory() }
    }
}

enum class MemoryPressure {
    NORMAL,
    LOW,
    CRITICAL
}

interface MemoryListener {
    fun onLowMemory()
    fun onCriticalMemory()
    fun onMemoryRecovered()
    fun onClearCaches()
    fun onTrimMemory()
}