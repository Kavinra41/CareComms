package com.carecomms.presentation.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages loading states for different operations in the application
 */
class LoadingStateManager {
    private val _loadingStates = MutableStateFlow<Map<String, LoadingState>>(emptyMap())
    val loadingStates: StateFlow<Map<String, LoadingState>> = _loadingStates.asStateFlow()
    
    private val mutex = Mutex()
    
    /**
     * Set loading state for a specific operation
     */
    suspend fun setLoading(operationId: String, isLoading: Boolean, message: String = "") {
        mutex.withLock {
            val currentStates = _loadingStates.value.toMutableMap()
            
            if (isLoading) {
                currentStates[operationId] = LoadingState.Loading(message)
            } else {
                currentStates.remove(operationId)
            }
            
            _loadingStates.value = currentStates
        }
    }
    
    /**
     * Check if any operation is currently loading
     */
    fun isAnyLoading(): Boolean {
        return _loadingStates.value.isNotEmpty()
    }
    
    /**
     * Check if a specific operation is loading
     */
    fun isLoading(operationId: String): Boolean {
        return _loadingStates.value.containsKey(operationId)
    }
    
    /**
     * Get loading message for a specific operation
     */
    fun getLoadingMessage(operationId: String): String {
        return (_loadingStates.value[operationId] as? LoadingState.Loading)?.message ?: ""
    }
    
    /**
     * Clear all loading states
     */
    suspend fun clearAll() {
        mutex.withLock {
            _loadingStates.value = emptyMap()
        }
    }
    
    /**
     * Execute operation with automatic loading state management
     */
    suspend inline fun <T> withLoading(
        operationId: String,
        loadingMessage: String = "Loading...",
        operation: suspend () -> T
    ): T {
        return try {
            setLoading(operationId, true, loadingMessage)
            operation()
        } finally {
            setLoading(operationId, false)
        }
    }
}

/**
 * Represents different loading states
 */
sealed class LoadingState {
    data class Loading(val message: String = "Loading...") : LoadingState()
    object NotLoading : LoadingState()
}

/**
 * Common loading operation IDs
 */
object LoadingOperations {
    const val SIGN_IN = "sign_in"
    const val SIGN_OUT = "sign_out"
    const val LOAD_MESSAGES = "load_messages"
    const val SEND_MESSAGE = "send_message"
    const val LOAD_CHAT_LIST = "load_chat_list"
    const val LOAD_ANALYTICS = "load_analytics"
    const val LOAD_CAREES = "load_carees"
    const val GENERATE_INVITATION = "generate_invitation"
    const val ACCEPT_INVITATION = "accept_invitation"
    const val REGISTER_CARER = "register_carer"
    const val REGISTER_CAREE = "register_caree"
    const val SYNC_DATA = "sync_data"
}