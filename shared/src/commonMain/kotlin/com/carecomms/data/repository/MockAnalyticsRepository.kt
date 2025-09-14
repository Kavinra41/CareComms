package com.carecomms.data.repository

import com.carecomms.data.models.*
import com.carecomms.domain.repository.AnalyticsRepository

class MockAnalyticsRepository : AnalyticsRepository {
    
    override suspend fun getCareeAnalytics(careeId: String, period: AnalyticsPeriod): Result<AnalyticsData> {
        return Result.success(getMockAnalyticsData(careeId, period))
    }
    
    override suspend fun getMultiCareeAnalytics(careeIds: List<String>, period: AnalyticsPeriod): Result<AnalyticsData> {
        // Combine analytics for multiple carees
        return Result.success(getMockAnalyticsData(careeIds.joinToString(","), period))
    }
    
    override suspend fun getAvailableCarees(carerId: String): Result<List<CareeInfo>> {
        // Return mock carees
        val mockCarees = listOf(
            CareeInfo(
                id = "caree-1",
                name = "Alice Johnson",
                age = 78,
                healthConditions = listOf("Diabetes", "Hypertension")
            ),
            CareeInfo(
                id = "caree-2", 
                name = "Bob Smith",
                age = 82,
                healthConditions = listOf("Arthritis")
            ),
            CareeInfo(
                id = "caree-3",
                name = "Carol Davis", 
                age = 75,
                healthConditions = listOf("Heart condition")
            )
        )
        return Result.success(mockCarees)
    }
    
    override suspend fun getDetailsTree(careeId: String): Result<List<DetailsTreeNode>> {
        return Result.success(getMockDetailsTree(careeId))
    }
    
    override suspend fun getDetailsTreeForMultipleCarees(careeIds: List<String>): Result<List<DetailsTreeNode>> {
        val combinedTree = careeIds.flatMap { getMockDetailsTree(it) }
        return Result.success(combinedTree)
    }
    
    override suspend fun getMockAnalyticsData(careeId: String, period: AnalyticsPeriod): AnalyticsData {
        val currentTime = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L
        
        return when (period) {
            AnalyticsPeriod.DAILY -> generateDailyMockData(currentTime, dayInMillis)
            AnalyticsPeriod.WEEKLY -> generateWeeklyMockData(currentTime, dayInMillis)
            AnalyticsPeriod.BIWEEKLY -> generateBiweeklyMockData(currentTime, dayInMillis)
            AnalyticsPeriod.MONTHLY -> generateMonthlyMockData(currentTime, dayInMillis)
        }
    }
    
    override suspend fun getMockDetailsTree(careeId: String): List<DetailsTreeNode> {
        return listOf(
            DetailsTreeNode(
                id = "$careeId-health",
                title = "Health Information",
                type = NodeType.CATEGORY,
                children = listOf(
                    DetailsTreeNode(
                        id = "$careeId-medications",
                        title = "Medications",
                        type = NodeType.DETAIL,
                        data = "Daily medications list"
                    ),
                    DetailsTreeNode(
                        id = "$careeId-vitals",
                        title = "Vital Signs",
                        type = NodeType.DETAIL,
                        data = "Blood pressure, heart rate"
                    )
                )
            ),
            DetailsTreeNode(
                id = "$careeId-activities",
                title = "Daily Activities",
                type = NodeType.CATEGORY,
                children = listOf(
                    DetailsTreeNode(
                        id = "$careeId-exercise",
                        title = "Exercise",
                        type = NodeType.DETAIL,
                        data = "Walking, stretching"
                    )
                )
            )
        )
    }
    
    private fun generateDailyMockData(currentTime: Long, dayInMillis: Long): AnalyticsData {
        val dailyMetrics = (0..6).map { dayOffset ->
            val date = currentTime - (dayOffset * dayInMillis)
            DailyMetric(
                date = formatDate(date),
                activityLevel = (6..10).random(),
                communicationCount = (2..8).random(),
                notes = "Daily check-in completed"
            )
        }.reversed()
        
        return AnalyticsData(
            dailyData = dailyMetrics,
            weeklyData = emptyList(),
            biweeklyData = emptyList(),
            notes = generateMockNotes(currentTime)
        )
    }
    
    private fun generateWeeklyMockData(currentTime: Long, dayInMillis: Long): AnalyticsData {
        val weeklyMetrics = (0..3).map { weekOffset ->
            val weekStart = currentTime - (weekOffset * 7 * dayInMillis)
            WeeklyMetric(
                weekStart = formatDate(weekStart),
                weekEnd = formatDate(weekStart + (6 * dayInMillis)),
                averageActivityLevel = (6..9).random(),
                totalCommunications = (15..35).random(),
                notes = "Weekly summary available"
            )
        }.reversed()
        
        return AnalyticsData(
            dailyData = emptyList(),
            weeklyData = weeklyMetrics,
            biweeklyData = emptyList(),
            notes = generateMockNotes(currentTime)
        )
    }
    
    private fun generateBiweeklyMockData(currentTime: Long, dayInMillis: Long): AnalyticsData {
        val biweeklyMetrics = (0..1).map { biweekOffset ->
            val biweekStart = currentTime - (biweekOffset * 14 * dayInMillis)
            BiweeklyMetric(
                periodStart = formatDate(biweekStart),
                periodEnd = formatDate(biweekStart + (13 * dayInMillis)),
                averageActivityLevel = (7..9).random(),
                totalCommunications = (30..60).random(),
                trends = listOf("Improving", "Stable"),
                notes = "Bi-weekly analysis complete"
            )
        }.reversed()
        
        return AnalyticsData(
            dailyData = emptyList(),
            weeklyData = emptyList(),
            biweeklyData = biweeklyMetrics,
            notes = generateMockNotes(currentTime)
        )
    }
    
    private fun generateMonthlyMockData(currentTime: Long, dayInMillis: Long): AnalyticsData {
        // Similar to biweekly but with longer periods
        return generateBiweeklyMockData(currentTime, dayInMillis)
    }
    
    private fun generateMockNotes(currentTime: Long): List<AnalyticsNote> {
        return listOf(
            AnalyticsNote(
                id = "note-1",
                content = "Increased communication frequency observed",
                timestamp = currentTime - (1 * 60 * 60 * 1000), // 1 hour ago
                type = "observation"
            ),
            AnalyticsNote(
                id = "note-2", 
                content = "Positive response to new medication routine",
                timestamp = currentTime - (2 * 60 * 60 * 1000), // 2 hours ago
                type = "health"
            ),
            AnalyticsNote(
                id = "note-3",
                content = "Regular sleep pattern maintained",
                timestamp = currentTime - (4 * 60 * 60 * 1000), // 4 hours ago
                type = "wellness"
            ),
            AnalyticsNote(
                id = "note-4",
                content = "Completed daily check-in successfully",
                timestamp = currentTime - (6 * 60 * 60 * 1000), // 6 hours ago
                type = "routine"
            ),
            AnalyticsNote(
                id = "note-5",
                content = "Medication reminder acknowledged",
                timestamp = currentTime - (8 * 60 * 60 * 1000), // 8 hours ago
                type = "medication"
            )
        )
    }
    
    private fun formatDate(timestamp: Long): String {
        // Simple date formatting - in real app would use proper date formatting
        val date = java.util.Date(timestamp)
        return java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(date)
    }
}