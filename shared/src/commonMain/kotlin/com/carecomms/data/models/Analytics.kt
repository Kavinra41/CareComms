package com.carecomms.data.models

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsData(
    val dailyData: List<DailyMetric>,
    val weeklyData: List<WeeklyMetric>,
    val biweeklyData: List<BiweeklyMetric>,
    val notes: List<AnalyticsNote>
)

@Serializable
data class DailyMetric(
    val date: String,
    val activityLevel: Int,
    val communicationCount: Int,
    val notes: String
)

@Serializable
data class WeeklyMetric(
    val weekStart: String,
    val weekEnd: String,
    val averageActivityLevel: Int,
    val totalCommunications: Int,
    val notes: String
)

@Serializable
data class BiweeklyMetric(
    val periodStart: String,
    val periodEnd: String,
    val averageActivityLevel: Int,
    val totalCommunications: Int,
    val trends: List<String>,
    val notes: String
)

@Serializable
data class AnalyticsNote(
    val id: String,
    val content: String,
    val timestamp: Long,
    val type: String
)

@Serializable
enum class NotePriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

@Serializable
enum class AnalyticsPeriod {
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY
}

@Serializable
data class DetailsTreeNode(
    val id: String,
    val title: String,
    val type: NodeType,
    val children: List<DetailsTreeNode> = emptyList(),
    val data: Any? = null,
    val isExpanded: Boolean = false
)

@Serializable
enum class NodeType {
    CAREE,
    CATEGORY,
    DETAIL,
    ITEM
}