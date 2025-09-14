package com.carecomms.performance

import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for performance monitoring dependencies
 */
fun performanceModule(applicationScope: CoroutineScope): Module = module {
    
    single<MemoryManager> { MemoryManager() }
    
    single<ImageCache> { 
        ImageCache(memoryManager = get())
    }
    
    single<PerformanceMonitor> { 
        PerformanceMonitor(applicationScope)
    }
    
    single<StartupOptimizer> { StartupOptimizer() }
    
    single<CrashReporter> { CrashReporter() }
    
    // Platform-specific implementations will be provided in platform modules
}