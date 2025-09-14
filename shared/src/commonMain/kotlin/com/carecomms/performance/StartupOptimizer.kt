package com.carecomms.performance

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Handles app startup optimization by managing initialization order
 * and deferring non-critical operations
 */
class StartupOptimizer {
    private val criticalInitializers = mutableListOf<suspend () -> Unit>()
    private val deferredInitializers = mutableListOf<suspend () -> Unit>()
    
    fun addCriticalInitializer(initializer: suspend () -> Unit) {
        criticalInitializers.add(initializer)
    }
    
    fun addDeferredInitializer(initializer: suspend () -> Unit) {
        deferredInitializers.add(initializer)
    }
    
    suspend fun initializeCritical() {
        criticalInitializers.forEach { initializer ->
            try {
                initializer()
            } catch (e: Exception) {
                // Log error but continue with other critical initializers
                println("Critical initializer failed: ${e.message}")
            }
        }
    }
    
    fun initializeDeferred(scope: CoroutineScope) {
        scope.launch {
            withContext(Dispatchers.Default) {
                deferredInitializers.forEach { initializer ->
                    try {
                        initializer()
                    } catch (e: Exception) {
                        // Log error but continue with other deferred initializers
                        println("Deferred initializer failed: ${e.message}")
                    }
                }
            }
        }
    }
}