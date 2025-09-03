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
    val averageActivity: Double,
    val totalCommunications: Int,
    val summary: String
)

@Serializable
data class BiweeklyMetric(
    val periodStart: String,
    val periodEnd: String,
    val trends: List<String>,
    val insights: String
)

@Serializable
data class AnalyticsNote(
    val id: String,
    val content: String,
    val timestamp: Long,
    val category: String
)

@Serializable
enum class AnalyticsPeriod { DAILY, WEEKLY, BIWEEKLY }

@Serializable
data class DetailsTreeNode(
    val id: String,
    val title: String,
    val type: NodeType,
    val children: List<DetailsTreeNode>,
    val data: String?
)

@Serializable
enum class NodeType { CAREE, CATEGORY, DETAIL, ITEM }