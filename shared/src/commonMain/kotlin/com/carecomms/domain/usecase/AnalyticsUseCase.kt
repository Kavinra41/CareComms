package com.carecomms.domain.usecase

import com.carecomms.data.models.*
import com.carecomms.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AnalyticsUseCase(
    private val analyticsRepository: AnalyticsRepository
) {
    
    /**
     * Get analytics data for a single caree
     */
    suspend fun getCareeAnalytics(
        careeId: String, 
        period: AnalyticsPeriod
    ): Result<AnalyticsData> {
        return try {
            if (careeId.isBlank()) {
                return Result.failure(Exception("Caree ID is required"))
            }
            
            analyticsRepository.getCareeAnalytics(careeId, period)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get combined analytics data for multiple carees
     */
    suspend fun getMultiCareeAnalytics(
        careeIds: List<String>, 
        period: AnalyticsPeriod
    ): Result<AnalyticsData> {
        return try {
            if (careeIds.isEmpty()) {
                return Result.failure(Exception("At least one caree must be selected"))
            }
            
            val validIds = careeIds.filter { it.isNotBlank() }
            if (validIds.isEmpty()) {
                return Result.failure(Exception("No valid caree IDs provided"))
            }
            
            analyticsRepository.getMultiCareeAnalytics(validIds, period)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get analytics data as a flow for real-time updates
     */
    fun getAnalyticsFlow(
        careeIds: List<String>, 
        period: AnalyticsPeriod
    ): Flow<Result<AnalyticsData>> = flow {
        try {
            val result = if (careeIds.size == 1) {
                getCareeAnalytics(careeIds.first(), period)
            } else {
                getMultiCareeAnalytics(careeIds, period)
            }
            emit(result)
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get available carees for analytics selection
     */
    suspend fun getAvailableCarees(carerId: String): Result<List<CareeInfo>> {
        return try {
            if (carerId.isBlank()) {
                return Result.failure(Exception("Carer ID is required"))
            }
            
            analyticsRepository.getAvailableCarees(carerId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate mock analytics data for demonstration
     */
    fun generateMockAnalytics(
        careeIds: List<String>, 
        period: AnalyticsPeriod
    ): AnalyticsData {
        val currentTime = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L
        
        return when (period) {
            AnalyticsPeriod.DAILY -> generateDailyMockData(currentTime, dayInMillis)
            AnalyticsPeriod.WEEKLY -> generateWeeklyMockData(currentTime, dayInMillis)
            AnalyticsPeriod.BIWEEKLY -> generateBiweeklyMockData(currentTime, dayInMillis)
        }
    }
    
    private fun generateDailyMockData(currentTime: Long, dayInMillis: Long): AnalyticsData {
        val dailyMetrics = (0..6).map { dayOffset ->
            val date = currentTime - (dayOffset * dayInMillis)
            DailyMetric(
                date = formatDate(date),
                activityLevel = (60..100).random(),
                communicationCount = (2..8).random(),
                notes = "Daily check-in completed"
            )
        }.reversed()
        
        return AnalyticsData(
            dailyData = dailyMetrics,
            weeklyData = emptyList(),
            biweeklyData = emptyList(),
            notes = listOf(
                AnalyticsNote(
                    id = "1",
                    content = "Good communication pattern this week",
                    timestamp = currentTime,
                    type = "positive"
                )
            )
        )
    }
    
    private fun generateWeeklyMockData(currentTime: Long, dayInMillis: Long): AnalyticsData {
        val weeklyMetrics = (0..3).map { weekOffset ->
            val weekStart = currentTime - (weekOffset * 7 * dayInMillis)
            WeeklyMetric(
                weekStart = formatDate(weekStart),
                weekEnd = formatDate(weekStart + (6 * dayInMillis)),
                averageActivityLevel = (65..95).random(),
                totalCommunications = (15..35).random(),
                notes = "Weekly summary available"
            )
        }.reversed()
        
        return AnalyticsData(
            dailyData = emptyList(),
            weeklyData = weeklyMetrics,
            biweeklyData = emptyList(),
            notes = listOf(
                AnalyticsNote(
                    id = "2",
                    content = "Consistent weekly patterns observed",
                    timestamp = currentTime,
                    type = "info"
                )
            )
        )
    }
    
    private fun generateBiweeklyMockData(currentTime: Long, dayInMillis: Long): AnalyticsData {
        val biweeklyMetrics = (0..1).map { biweekOffset ->
            val biweekStart = currentTime - (biweekOffset * 14 * dayInMillis)
            BiweeklyMetric(
                periodStart = formatDate(biweekStart),
                periodEnd = formatDate(biweekStart + (13 * dayInMillis)),
                averageActivityLevel = (70..90).random(),
                totalCommunications = (30..60).random(),
                trends = listOf("Improving", "Stable"),
                notes = "Bi-weekly analysis complete"
            )
        }.reversed()
        
        return AnalyticsData(
            dailyData = emptyList(),
            weeklyData = emptyList(),
            biweeklyData = biweeklyMetrics,
            notes = listOf(
                AnalyticsNote(
                    id = "3",
                    content = "Long-term trends looking positive",
                    timestamp = currentTime,
                    type = "positive"
                )
            )
        )
    }
    
    private fun formatDate(timestamp: Long): String {
        // Simple date formatting - in real app would use proper date formatting
        return "Date-${timestamp / (24 * 60 * 60 * 1000L)}"
    }
}