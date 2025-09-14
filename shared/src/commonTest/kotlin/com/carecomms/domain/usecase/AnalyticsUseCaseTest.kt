package com.carecomms.domain.usecase

import com.carecomms.data.models.*
import com.carecomms.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class AnalyticsUseCaseTest {
    
    private lateinit var mockAnalyticsRepository: MockAnalyticsRepository
    private lateinit var analyticsUseCase: AnalyticsUseCase
    
    @BeforeTest
    fun setup() {
        mockAnalyticsRepository = MockAnalyticsRepository()
        analyticsUseCase = AnalyticsUseCase(mockAnalyticsRepository)
    }
    
    @Test
    fun `getCareeAnalytics with valid careeId should return success`() = runTest {
        // Given
        val careeId = "caree1"
        val period = AnalyticsPeriod.DAILY
        val expectedData = AnalyticsData(
            dailyData = listOf(
                DailyMetric("2024-01-01", 80, 5, "Good day")
            ),
            weeklyData = emptyList(),
            biweeklyData = emptyList(),
            notes = emptyList()
        )
        mockAnalyticsRepository.getCareeAnalyticsResult = Result.success(expectedData)
        
        // When
        val result = analyticsUseCase.getCareeAnalytics(careeId, period)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedData, result.getOrNull())
    }
    
    @Test
    fun `getCareeAnalytics with empty careeId should return failure`() = runTest {
        // Given
        val careeId = ""
        val period = AnalyticsPeriod.DAILY
        
        // When
        val result = analyticsUseCase.getCareeAnalytics(careeId, period)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("required") == true)
    }
    
    @Test
    fun `getMultiCareeAnalytics with valid careeIds should return success`() = runTest {
        // Given
        val careeIds = listOf("caree1", "caree2")
        val period = AnalyticsPeriod.WEEKLY
        val expectedData = AnalyticsData(
            dailyData = emptyList(),
            weeklyData = listOf(
                WeeklyMetric("2024-01-01", "2024-01-07", 85, 25, "Good week")
            ),
            biweeklyData = emptyList(),
            notes = emptyList()
        )
        mockAnalyticsRepository.getMultiCareeAnalyticsResult = Result.success(expectedData)
        
        // When
        val result = analyticsUseCase.getMultiCareeAnalytics(careeIds, period)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedData, result.getOrNull())
    }
    
    @Test
    fun `getMultiCareeAnalytics with empty careeIds should return failure`() = runTest {
        // Given
        val careeIds = emptyList<String>()
        val period = AnalyticsPeriod.WEEKLY
        
        // When
        val result = analyticsUseCase.getMultiCareeAnalytics(careeIds, period)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("At least one caree") == true)
    }
    
    @Test
    fun `getAnalyticsFlow with single caree should call getCareeAnalytics`() = runTest {
        // Given
        val careeIds = listOf("caree1")
        val period = AnalyticsPeriod.DAILY
        val expectedData = AnalyticsData(
            dailyData = listOf(DailyMetric("2024-01-01", 80, 5, "Good day")),
            weeklyData = emptyList(),
            biweeklyData = emptyList(),
            notes = emptyList()
        )
        mockAnalyticsRepository.getCareeAnalyticsResult = Result.success(expectedData)
        
        // When
        val result = analyticsUseCase.getAnalyticsFlow(careeIds, period).first()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedData, result.getOrNull())
    }
    
    @Test
    fun `getAnalyticsFlow with multiple carees should call getMultiCareeAnalytics`() = runTest {
        // Given
        val careeIds = listOf("caree1", "caree2")
        val period = AnalyticsPeriod.WEEKLY
        val expectedData = AnalyticsData(
            dailyData = emptyList(),
            weeklyData = listOf(WeeklyMetric("2024-01-01", "2024-01-07", 85, 25, "Good week")),
            biweeklyData = emptyList(),
            notes = emptyList()
        )
        mockAnalyticsRepository.getMultiCareeAnalyticsResult = Result.success(expectedData)
        
        // When
        val result = analyticsUseCase.getAnalyticsFlow(careeIds, period).first()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedData, result.getOrNull())
    }
    
    @Test
    fun `generateMockAnalytics for daily period should return daily data`() = runTest {
        // Given
        val careeIds = listOf("caree1")
        val period = AnalyticsPeriod.DAILY
        
        // When
        val result = analyticsUseCase.generateMockAnalytics(careeIds, period)
        
        // Then
        assertTrue(result.dailyData.isNotEmpty())
        assertTrue(result.weeklyData.isEmpty())
        assertTrue(result.biweeklyData.isEmpty())
        assertEquals(7, result.dailyData.size) // 7 days of data
    }
    
    @Test
    fun `generateMockAnalytics for weekly period should return weekly data`() = runTest {
        // Given
        val careeIds = listOf("caree1")
        val period = AnalyticsPeriod.WEEKLY
        
        // When
        val result = analyticsUseCase.generateMockAnalytics(careeIds, period)
        
        // Then
        assertTrue(result.dailyData.isEmpty())
        assertTrue(result.weeklyData.isNotEmpty())
        assertTrue(result.biweeklyData.isEmpty())
        assertEquals(4, result.weeklyData.size) // 4 weeks of data
    }
    
    @Test
    fun `generateMockAnalytics for biweekly period should return biweekly data`() = runTest {
        // Given
        val careeIds = listOf("caree1")
        val period = AnalyticsPeriod.BIWEEKLY
        
        // When
        val result = analyticsUseCase.generateMockAnalytics(careeIds, period)
        
        // Then
        assertTrue(result.dailyData.isEmpty())
        assertTrue(result.weeklyData.isEmpty())
        assertTrue(result.biweeklyData.isNotEmpty())
        assertEquals(2, result.biweeklyData.size) // 2 biweeks of data
    }
}

class MockAnalyticsRepository : AnalyticsRepository {
    var getCareeAnalyticsResult: Result<AnalyticsData> = Result.failure(Exception("Not implemented"))
    var getMultiCareeAnalyticsResult: Result<AnalyticsData> = Result.failure(Exception("Not implemented"))
    var getAvailableCareesResult: Result<List<CareeInfo>> = Result.failure(Exception("Not implemented"))
    
    override suspend fun getCareeAnalytics(careeId: String, period: AnalyticsPeriod): Result<AnalyticsData> {
        return getCareeAnalyticsResult
    }
    
    override suspend fun getMultiCareeAnalytics(careeIds: List<String>, period: AnalyticsPeriod): Result<AnalyticsData> {
        return getMultiCareeAnalyticsResult
    }
    
    override suspend fun getAvailableCarees(carerId: String): Result<List<CareeInfo>> {
        return getAvailableCareesResult
    }
}