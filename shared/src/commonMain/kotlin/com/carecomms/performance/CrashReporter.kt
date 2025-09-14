package com.carecomms.performance

import kotlinx.datetime.Clock

/**
 * Handles crash reporting and error tracking
 */
class CrashReporter {
    private val crashes = mutableListOf<CrashReport>()
    private val maxCrashReports = 100
    
    fun recordCrash(throwable: Throwable, context: String = "") {
        val crashReport = CrashReport(
            timestamp = Clock.System.now().toEpochMilliseconds(),
            exception = throwable.toString(),
            stackTrace = throwable.stackTraceToString(),
            context = context,
            threadName = getCurrentThreadName()
        )
        
        synchronized(crashes) {
            crashes.add(crashReport)
            
            // Keep only the most recent crashes
            if (crashes.size > maxCrashReports) {
                crashes.removeFirst()
            }
        }
        
        // Log crash immediately
        logCrash(crashReport)
        
        // In a real implementation, this would send to crash reporting service
        sendCrashReport(crashReport)
    }
    
    fun getCrashCount(): Int = synchronized(crashes) { crashes.size }
    
    fun getRecentCrashes(limit: Int = 10): List<CrashReport> {
        return synchronized(crashes) {
            crashes.takeLast(limit)
        }
    }
    
    fun clearCrashes() {
        synchronized(crashes) {
            crashes.clear()
        }
    }
    
    private fun logCrash(crashReport: CrashReport) {
        println("CRASH REPORTED: ${crashReport.exception}")
        println("Context: ${crashReport.context}")
        println("Thread: ${crashReport.threadName}")
        println("Stack trace: ${crashReport.stackTrace}")
    }
    
    private fun sendCrashReport(crashReport: CrashReport) {
        // In a real implementation, this would send the crash report to a service
        // like Firebase Crashlytics or a custom crash reporting endpoint
        println("Sending crash report to crash reporting service...")
    }
    
    private fun getCurrentThreadName(): String {
        // Platform-specific implementation would be needed
        return "main"
    }
}

data class CrashReport(
    val timestamp: Long,
    val exception: String,
    val stackTrace: String,
    val context: String,
    val threadName: String
)