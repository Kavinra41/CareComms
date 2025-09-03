package com.carecomms.domain.repository

import com.carecomms.data.models.AnalyticsData
import com.carecomms.data.models.AnalyticsPeriod

interface AnalyticsRepository {
    suspend fun getCareeAnalytics(careeId: String, period: AnalyticsPeriod): Result<AnalyticsData>
    suspend fun getMultiCareeAnalytics(careeIds: List<String>, period: AnalyticsPeriod): Result<AnalyticsData>
}