package com.carecomms.domain.repository

import com.carecomms.data.models.AnalyticsData
import com.carecomms.data.models.AnalyticsPeriod
import com.carecomms.data.models.DetailsTreeNode

interface AnalyticsRepository {
    suspend fun getCareeAnalytics(careeId: String, period: AnalyticsPeriod): Result<AnalyticsData>
    suspend fun getMultiCareeAnalytics(careeIds: List<String>, period: AnalyticsPeriod): Result<AnalyticsData>
    suspend fun getDetailsTree(careeId: String): Result<List<DetailsTreeNode>>
    suspend fun getDetailsTreeForMultipleCarees(careeIds: List<String>): Result<List<DetailsTreeNode>>
    suspend fun getMockAnalyticsData(careeId: String, period: AnalyticsPeriod): AnalyticsData
    suspend fun getMockDetailsTree(careeId: String): List<DetailsTreeNode>
}