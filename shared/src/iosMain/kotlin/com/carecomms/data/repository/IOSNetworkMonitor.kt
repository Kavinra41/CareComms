package com.carecomms.data.repository

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import platform.Network.*
import platform.Foundation.*
import platform.darwin.dispatch_get_main_queue

/**
 * iOS implementation of NetworkMonitor using Network framework
 */
class IOSNetworkMonitor : NetworkMonitor {
    
    override val isOnline: Flow<Boolean> = callbackFlow {
        val monitor = nw_path_monitor_create()
        
        nw_path_monitor_set_update_handler(monitor) { path ->
            val isConnected = nw_path_get_status(path) == nw_path_status_satisfied
            trySend(isConnected)
        }
        
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
        
        awaitClose {
            nw_path_monitor_cancel(monitor)
        }
    }.distinctUntilChanged()
    
    override val connectionQuality: Flow<ConnectionQuality> = callbackFlow {
        val monitor = nw_path_monitor_create()
        
        nw_path_monitor_set_update_handler(monitor) { path ->
            val quality = when (nw_path_get_status(path)) {
                nw_path_status_satisfied -> {
                    when {
                        nw_path_uses_interface_type(path, nw_interface_type_wifi) -> 
                            ConnectionQuality.EXCELLENT
                        nw_path_uses_interface_type(path, nw_interface_type_cellular) -> 
                            ConnectionQuality.GOOD
                        nw_path_uses_interface_type(path, nw_interface_type_wired) -> 
                            ConnectionQuality.EXCELLENT
                        else -> ConnectionQuality.POOR
                    }
                }
                nw_path_status_unsatisfied -> ConnectionQuality.OFFLINE
                nw_path_status_requiresConnection -> ConnectionQuality.POOR
                else -> ConnectionQuality.OFFLINE
            }
            trySend(quality)
        }
        
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
        
        awaitClose {
            nw_path_monitor_cancel(monitor)
        }
    }.distinctUntilChanged()
    
    override suspend fun checkConnectivity(): Boolean {
        val monitor = nw_path_monitor_create()
        val path = nw_path_monitor_copy_current_path(monitor)
        val isConnected = nw_path_get_status(path) == nw_path_status_satisfied
        nw_path_monitor_cancel(monitor)
        return isConnected
    }
}