package com.carecomms.data.models

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AnalyticsTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testAnalyticsDataSerialization() {
        val dailyMetric = DailyMetric(
            date = "2024-01-15",
            activityLevel = 7,
            communicationCount = 5,
            notes = "Good day overall"
        )

        val weeklyMetric = WeeklyMetric(
            weekStart = "2024-01-08",
            weekEnd = "2024-01-14",
            averageActivityLevel = 6.5,
            totalCommunications = 25,
            trends = listOf("Increasing activity", "Regular communication")
        )

        val biweeklyMetric = BiweeklyMetric(
            periodStart = "2024-01-01",
            periodEnd = "2024-01-14",
            overallTrend = "Positive",
            keyInsights = listOf("More active in mornings", "Prefers text over calls"),
            recommendations = listOf("Continue current routine", "Encourage afternoon activities")
        )

        val analyticsNote = AnalyticsNote(
            id = "note123",
            content = "Patient showed improvement in mobility",
            timestamp = 1234567890L,
            category = "Health",
            priority = NotePriority.HIGH
        )

        val analyticsData = AnalyticsData(
            dailyData = listOf(dailyMetric),
            weeklyData = listOf(weeklyMetric),
            biweeklyData = listOf(biweeklyMetric),
            notes = listOf(analyticsNote)
        )

        val serialized = json.encodeToString(AnalyticsData.serializer(), analyticsData)
        val deserialized = json.decodeFromString(AnalyticsData.serializer(), serialized)

        assertEquals(analyticsData, deserialized)
    }

    @Test
    fun testDailyMetricSerialization() {
        val dailyMetric = DailyMetric(
            date = "2024-01-15",
            activityLevel = 8,
            communicationCount = 3,
            notes = "Very active day"
        )

        val serialized = json.encodeToString(DailyMetric.serializer(), dailyMetric)
        val deserialized = json.decodeFromString(DailyMetric.serializer(), serialized)

        assertEquals(dailyMetric, deserialized)
    }

    @Test
    fun testDetailsTreeNodeSerialization() {
        val childNode = DetailsTreeNode(
            id = "child1",
            title = "Blood Pressure",
            type = NodeType.DETAIL,
            data = "120/80 mmHg"
        )

        val parentNode = DetailsTreeNode(
            id = "parent1",
            title = "Health Metrics",
            type = NodeType.CATEGORY,
            children = listOf(childNode),
            data = null
        )

        val serialized = json.encodeToString(DetailsTreeNode.serializer(), parentNode)
        val deserialized = json.decodeFromString(DetailsTreeNode.serializer(), serialized)

        assertEquals(parentNode, deserialized)
    }

    @Test
    fun testNotePriorityEnum() {
        val priorities = NotePriority.values()
        assertEquals(4, priorities.size)
        assertTrue(priorities.contains(NotePriority.LOW))
        assertTrue(priorities.contains(NotePriority.NORMAL))
        assertTrue(priorities.contains(NotePriority.HIGH))
        assertTrue(priorities.contains(NotePriority.URGENT))
    }

    @Test
    fun testAnalyticsPeriodEnum() {
        val periods = AnalyticsPeriod.values()
        assertEquals(4, periods.size)
        assertTrue(periods.contains(AnalyticsPeriod.DAILY))
        assertTrue(periods.contains(AnalyticsPeriod.WEEKLY))
        assertTrue(periods.contains(AnalyticsPeriod.BIWEEKLY))
        assertTrue(periods.contains(AnalyticsPeriod.MONTHLY))
    }

    @Test
    fun testNodeTypeEnum() {
        val nodeTypes = NodeType.values()
        assertEquals(4, nodeTypes.size)
        assertTrue(nodeTypes.contains(NodeType.CAREE))
        assertTrue(nodeTypes.contains(NodeType.CATEGORY))
        assertTrue(nodeTypes.contains(NodeType.DETAIL))
        assertTrue(nodeTypes.contains(NodeType.ITEM))
    }

    @Test
    fun testAnalyticsNoteDefaultPriority() {
        val note = AnalyticsNote(
            id = "note456",
            content = "Regular check-in completed",
            timestamp = 1234567890L,
            category = "Communication"
        )

        assertEquals(NotePriority.NORMAL, note.priority)
    }

    @Test
    fun testDetailsTreeNodeDefaults() {
        val node = DetailsTreeNode(
            id = "node1",
            title = "Test Node",
            type = NodeType.ITEM
        )

        assertTrue(node.children.isEmpty())
        assertEquals(null, node.data)
    }
}