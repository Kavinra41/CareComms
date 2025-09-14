package com.carecomms.presentation.analytics

import com.carecomms.data.models.*
import com.carecomms.domain.usecase.AnalyticsUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class AnalyticsViewModelTest {
    
    private lateinit var mockAnalyticsUseCase: MockAnalyticsUseCase
    private lateinit var analyticsViewModel: AnalyticsViewModel
    private val carerId = "carer1"
    
    @BeforeTest
    fun setup() {
        mockAnalyticsUseCase = MockAnalyticsUseCase()
        analyticsViewModel = AnalyticsViewModel(mockAnalyticsUseCase, carerId)
    }
    
    @AfterTest
    fun tearDown() {
        analyticsViewModel.onCleared()
    }
    
    @Test
    fun `initial state should be loading carees`() = runTest {
        // When
        val initialState = analyticsViewModel.state.value
        
        // Then
        assertTrue(initialState.isLoadingCarees)
        assertTrue(initialState.availableCarees.isEmpty())
        assertTrue(initialState.selectedCareeIds.isEmpty())
        assertEquals(AnalyticsPeriod.DAILY, initialState.selectedPeriod)
        assertNull(initialState.analyticsData)
    }
    
    @Test
    fun `handleAction LoadCarees should load available carees`() = runTest {
        // Given
        val carees = listOf(
            CareeInfo("caree1", "John Doe", 75, listOf("Diabetes")),
            CareeInfo("caree2", "Jane Smith", 80, listOf("Hypertension"))
        )
        mockAnalyticsUseCase.getAvailableCareesResult = Result.success(carees)
        
        // When
        analyticsViewModel.handleAction(AnalyticsAction.LoadCarees)
        
        // Wait for async operation to complete
        kotlinx.coroutines.delay(100)
        
        // Then
        val finalState = analyticsViewModel.state.value
        assertFalse(finalState.isLoadingCarees)
        assertEquals(carees, finalState.availableCarees)
        assertEquals(listOf("caree1"), finalState.selectedCareeIds) // First caree auto-selected
        assertNull(finalState.error)
    }
    
    @Test
    fun `handleAction SelectCaree should add caree to selection`() = runTest {
        // Given - setup initial state with carees
        val carees = listOf(
            CareeInfo("caree1", "John Doe", 75, listOf("Diabetes")),
            CareeInfo("caree2", "Jane Smith", 80, listOf("Hypertension"))
        )
        mockAnalyticsUseCase.getAvailableCareesResult = Result.success(carees)
        analyticsViewModel.handleAction(AnalyticsAction.LoadCarees)
        kotlinx.coroutines.delay(100)
        
        // When
        analyticsViewModel.handleAction(AnalyticsAction.SelectCaree("caree2"))
        
        // Then
        val finalState = analyticsViewModel.state.value
        assertTrue(finalState.selectedCareeIds.contains("caree1"))
        assertTrue(finalState.selectedCareeIds.contains("caree2"))
    }
    
    @Test
    fun `handleAction DeselectCaree should remove caree from selection`() = runTest {
        // Given - setup initial state with multiple selected carees
        val carees = listOf(
            CareeInfo("caree1", "John Doe", 75, listOf("Diabetes")),
            CareeInfo("caree2", "Jane Smith", 80, listOf("Hypertension"))
        )
        mockAnalyticsUseCase.getAvailableCareesResult = Result.success(carees)
        analyticsViewModel.handleAction(AnalyticsAction.LoadCarees)
        kotlinx.coroutines.delay(100)
        analyticsViewModel.handleAction(AnalyticsAction.SelectCaree("caree2"))
        
        // When
        analyticsViewModel.handleAction(AnalyticsAction.DeselectCaree("caree1"))
        
        // Then
        val finalState = analyticsViewModel.state.value
        assertFalse(finalState.selectedCareeIds.contains("caree1"))
        assertTrue(finalState.selectedCareeIds.contains("caree2"))
    }
    
    @Test
    fun `handleAction SelectAllCarees should select all available carees`() = runTest {
        // Given - setup initial state with carees
        val carees = listOf(
            CareeInfo("caree1", "John Doe", 75, listOf("Diabetes")),
            CareeInfo("caree2", "Jane Smith", 80, listOf("Hypertension")),
            CareeInfo("caree3", "Bob Johnson", 70, listOf("Arthritis"))
        )
        mockAnalyticsUseCase.getAvailableCareesResult = Result.success(carees)
        analyticsViewModel.handleAction(AnalyticsAction.LoadCarees)
        kotlinx.coroutines.delay(100)
        
        // When
        analyticsViewModel.handleAction(AnalyticsAction.SelectAllCarees)
        
        // Then
        val finalState = analyticsViewModel.state.value
        assertEquals(3, finalState.selectedCareeIds.size)
        assertTrue(finalState.selectedCareeIds.containsAll(listOf("caree1", "caree2", "caree3")))
    }
    
    @Test
    fun `handleAction DeselectAllCarees should clear all selections`() = runTest {
        // Given - setup initial state with selected carees
        val carees = listOf(
            CareeInfo("caree1", "John Doe", 75, listOf("Diabetes")),
            CareeInfo("caree2", "Jane Smith", 80, listOf("Hypertension"))
        )
        mockAnalyticsUseCase.getAvailableCareesResult = Result.success(carees)
        analyticsViewModel.handleAction(AnalyticsAction.LoadCarees)
        kotlinx.coroutines.delay(100)
        
        // When
        analyticsViewModel.handleAction(AnalyticsAction.DeselectAllCarees)
        
        // Then
        val finalState = analyticsViewModel.state.value
        assertTrue(finalState.selectedCareeIds.isEmpty())
        assertNull(finalState.analyticsData)
    }
    
    @Test
    fun `handleAction ChangePeriod should update selected period`() = runTest {
        // Given
        val newPeriod = AnalyticsPeriod.WEEKLY
        
        // When
        analyticsViewModel.handleAction(AnalyticsAction.ChangePeriod(newPeriod))
        
        // Then
        val finalState = analyticsViewModel.state.value
        assertEquals(newPeriod, finalState.selectedPeriod)
    }
    
    @Test
    fun `handleAction LoadAnalytics should load analytics data for single caree`() = runTest {
        // Given - setup initial state with one selected caree
        val carees = listOf(CareeInfo("caree1", "John Doe", 75, listOf("Diabetes")))
        val analyticsData = AnalyticsData(
            dailyData = listOf(DailyMetric("2024-01-01", 80, 5, "Good day")),
            weeklyData = emptyList(),
            biweeklyData = emptyList(),
            notes = emptyList()
        )
        
        mockAnalyticsUseCase.getAvailableCareesResult = Result.success(carees)
        mockAnalyticsUseCase.getCareeAnalyticsResult = Result.success(analyticsData)
        
        analyticsViewModel.handleAction(AnalyticsAction.LoadCarees)
        kotlinx.coroutines.delay(100)
        
        // When
        analyticsViewModel.handleAction(AnalyticsAction.LoadAnalytics)
        
        // Wait for async operation to complete
        kotlinx.coroutines.delay(100)
        
        // Then
        val finalState = analyticsViewModel.state.value
        assertFalse(finalState.isLoadingAnalytics)
        assertEquals(analyticsData, finalState.analyticsData)
        assertNull(finalState.error)
    }
    
    @Test
    fun `getSelectedCareeNames should return names of selected carees`() = runTest {
        // Given - setup initial state with carees
        val carees = listOf(
            CareeInfo("caree1", "John Doe", 75, listOf("Diabetes")),
            CareeInfo("caree2", "Jane Smith", 80, listOf("Hypertension"))
        )
        mockAnalyticsUseCase.getAvailableCareesResult = Result.success(carees)
        analyticsViewModel.handleAction(AnalyticsAction.LoadCarees)
        kotlinx.coroutines.delay(100)
        analyticsViewModel.handleAction(AnalyticsAction.SelectCaree("caree2"))
        
        // When
        val selectedNames = analyticsViewModel.getSelectedCareeNames()
        
        // Then
        assertEquals(listOf("John Doe", "Jane Smith"), selectedNames)
    }
    
    @Test
    fun `hasMultipleCarees should return true when multiple carees available`() = runTest {
        // Given - setup initial state with multiple carees
        val carees = listOf(
            CareeInfo("caree1", "John Doe", 75, listOf("Diabetes")),
            CareeInfo("caree2", "Jane Smith", 80, listOf("Hypertension"))
        )
        mockAnalyticsUseCase.getAvailableCareesResult = Result.success(carees)
        analyticsViewModel.handleAction(AnalyticsAction.LoadCarees)
        kotlinx.coroutines.delay(100)
        
        // When
        val hasMultiple = analyticsViewModel.hasMultipleCarees()
        
        // Then
        assertTrue(hasMultiple)
    }
}

class MockAnalyticsUseCase : AnalyticsUseCase(MockAnalyticsRepository()) {
    var getAvailableCareesResult: Result<List<CareeInfo>> = Result.failure(Exception("Not implemented"))
    var getCareeAnalyticsResult: Result<AnalyticsData> = Result.failure(Exception("Not implemented"))
    var getMultiCareeAnalyticsResult: Result<AnalyticsData> = Result.failure(Exception("Not implemented"))
    
    override suspend fun getAvailableCarees(carerId: String): Result<List<CareeInfo>> {
        return getAvailableCareesResult
    }
    
    override suspend fun getCareeAnalytics(careeId: String, period: AnalyticsPeriod): Result<AnalyticsData> {
        return getCareeAnalyticsResult
    }
    
    override suspend fun getMultiCareeAnalytics(careeIds: List<String>, period: AnalyticsPeriod): Result<AnalyticsData> {
        return getMultiCareeAnalyticsResult
    }
}

class MockAnalyticsRepository : com.carecomms.domain.repository.AnalyticsRepository {
    override suspend fun getCareeAnalytics(careeId: String, period: AnalyticsPeriod): Result<AnalyticsData> {
        return Result.failure(Exception("Not implemented"))
    }
    
    override suspend fun getMultiCareeAnalytics(careeIds: List<String>, period: AnalyticsPeriod): Result<AnalyticsData> {
        return Result.failure(Exception("Not implemented"))
    }
    
    override suspend fun getAvailableCarees(carerId: String): Result<List<CareeInfo>> {
        return Result.failure(Exception("Not implemented"))
    }
}