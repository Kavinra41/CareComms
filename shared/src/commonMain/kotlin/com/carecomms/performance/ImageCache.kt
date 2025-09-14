package com.carecomms.performance

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Cross-platform image caching system with memory management
 */
class ImageCache(
    private val maxMemorySize: Long = 50 * 1024 * 1024, // 50MB default
    private val memoryManager: MemoryManager
) : MemoryListener {
    
    private val cache = mutableMapOf<String, CachedImage>()
    private val accessOrder = mutableListOf<String>()
    private val mutex = Mutex()
    private var currentMemoryUsage = 0L
    
    init {
        memoryManager.addMemoryListener(this)
    }
    
    suspend fun get(key: String): ByteArray? = mutex.withLock {
        val cachedImage = cache[key]
        if (cachedImage != null) {
            // Update access order for LRU
            accessOrder.remove(key)
            accessOrder.add(key)
            return cachedImage.data
        }
        return null
    }
    
    suspend fun put(key: String, data: ByteArray) = mutex.withLock {
        val size = data.size.toLong()
        
        // Remove existing entry if present
        remove(key)
        
        // Ensure we have space
        while (currentMemoryUsage + size > maxMemorySize && accessOrder.isNotEmpty()) {
            val oldestKey = accessOrder.removeFirst()
            val removed = cache.remove(oldestKey)
            removed?.let { currentMemoryUsage -= it.size }
        }
        
        // Add new entry
        cache[key] = CachedImage(data, size)
        accessOrder.add(key)
        currentMemoryUsage += size
    }
    
    private suspend fun remove(key: String) = mutex.withLock {
        val removed = cache.remove(key)
        if (removed != null) {
            accessOrder.remove(key)
            currentMemoryUsage -= removed.size
        }
    }
    
    suspend fun clear() = mutex.withLock {
        cache.clear()
        accessOrder.clear()
        currentMemoryUsage = 0L
    }
    
    override fun onLowMemory() {
        // Clear half the cache on low memory
        val keysToRemove = accessOrder.take(accessOrder.size / 2)
        keysToRemove.forEach { key ->
            val removed = cache.remove(key)
            removed?.let { currentMemoryUsage -= it.size }
        }
        accessOrder.removeAll(keysToRemove.toSet())
    }
    
    override fun onCriticalMemory() {
        // Clear entire cache on critical memory
        cache.clear()
        accessOrder.clear()
        currentMemoryUsage = 0L
    }
    
    override fun onMemoryRecovered() {
        // No action needed on memory recovery
    }
    
    override fun onClearCaches() {
        cache.clear()
        accessOrder.clear()
        currentMemoryUsage = 0L
    }
    
    override fun onTrimMemory() {
        onLowMemory()
    }
    
    fun getMemoryUsage(): Long = currentMemoryUsage
    fun getCacheSize(): Int = cache.size
}

private data class CachedImage(
    val data: ByteArray,
    val size: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as CachedImage
        return data.contentEquals(other.data) && size == other.size
    }
    
    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + size.hashCode()
        return result
    }
}