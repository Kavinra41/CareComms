package com.carecomms.presentation.analytics

import com.carecomms.data.models.*
import com.carecomms.domain.usecase.AnalyticsUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class AnalyticsViewModel(
    private val analyticsUseCase: AnalyticsUseCase,
    private val carerId: String
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _state = MutableStateFlow(AnalyticsViewState())
    val state: StateFlow<AnalyticsViewState> = _state.asStateFlow()
    
    private val _effects = MutableSharedFlow<AnalyticsEffect>()
    val effects: SharedFlow<AnalyticsEffect> = _effects.asSharedFlow()

    init {
        loadAvailableCarees()
    }

    fun handleAction(action: AnalyticsAction) {
        when (action) {
            is AnalyticsAction.LoadCarees -> loadAvailableCarees()
            is AnalyticsAction.SelectCaree -> selectCaree(action.careeId)
            is AnalyticsAction.DeselectCaree -> deselectCaree(action.careeId)
            is AnalyticsAction.SelectAllCarees -> selectAllCarees()
            is AnalyticsAction.DeselectAllCarees -> deselectAllCarees()
            is AnalyticsAction.ChangePeriod -> changePeriod(action.period)
            is AnalyticsAction.LoadAnalytics -> loadAnalytics()
            is AnalyticsAction.RefreshData -> refreshData()
            is AnalyticsAction.ClearError -> clearError()
        }
    }

    private fun loadAvailableCarees() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingCarees = true, error = null) }
            
            try {
                val result = analyticsUseCase.getAvailableCarees(carerId)
                
                if (result.isSuccess) {
                    val carees = result.getOrThrow()
                    _state.update { 
                        it.copy(
                            isLoadingCarees = false,
                            availableCarees = carees,
                            selectedCareeIds = if (carees.isNotEmpty()) listOf(carees.first().id) else emptyList()
                        ) 
                    }
                    
                    // Auto-load analytics for first caree
                    if (carees.isNotEmpty()) {
                        loadAnalytics()
                    }
                } else {
                    _state.update { 
                        it.copy(
                            isLoadingCarees = false,
                            error = "Failed to load carees: ${result.exceptionOrNull()?.message}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoadingCarees = false,
                        error = "Failed to load carees: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun selectCaree(careeId: String) {
        _state.update { currentState ->
            val newSelection = if (careeId in currentState.selectedCareeIds) {
                currentState.selectedCareeIds
            } else {
                currentState.selectedCareeIds + careeId
            }
            currentState.copy(selectedCareeIds = newSelection)
        }
        loadAnalytics()
    }

    private fun deselectCaree(careeId: String) {
        _state.update { currentState ->
            val newSelection = currentState.selectedCareeIds - careeId
            currentState.copy(selectedCareeIds = newSelection)
        }
        
        if (_state.value.selectedCareeIds.isNotEmpty()) {
            loadAnalytics()
        } else {
            _state.update { it.copy(analyticsData = null) }
        }
    }

    private fun selectAllCarees() {
        _state.update { currentState ->
            currentState.copy(
                selectedCareeIds = currentState.availableCarees.map { it.id }
            )
        }
        loadAnalytics()
    }

    private fun deselectAllCarees() {
        _state.update { 
            it.copy(
                selectedCareeIds = emptyList(),
                analyticsData = null
            ) 
        }
    }

    private fun changePeriod(period: AnalyticsPeriod) {
        _state.update { it.copy(selectedPeriod = period) }
        loadAnalytics()
    }

    private fun loadAnalytics() {
        val currentState = _state.value
        if (currentState.selectedCareeIds.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoadingAnalytics = true, error = null) }
            
            try {
                val result = if (currentState.selectedCareeIds.size == 1) {
                    analyticsUseCase.getCareeAnalytics(
                        currentState.selectedCareeIds.first(),
                        currentState.selectedPeriod
                    )
                } else {
                    analyticsUseCase.getMultiCareeAnalytics(
                        currentState.selectedCareeIds,
                        currentState.selectedPeriod
                    )
                }
                
                if (result.isSuccess) {
                    _state.update { 
                        it.copy(
                            isLoadingAnalytics = false,
                            analyticsData = result.getOrThrow()
                        ) 
                    }
                } else {
                    // Fallback to mock data if real data fails
                    val mockData = analyticsUseCase.generateMockAnalytics(
                        currentState.selectedCareeIds,
                        currentState.selectedPeriod
                    )
                    
                    _state.update { 
                        it.copy(
                            isLoadingAnalytics = false,
                            analyticsData = mockData,
                            error = "Using mock data: ${result.exceptionOrNull()?.message}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                // Fallback to mock data on exception
                val mockData = analyticsUseCase.generateMockAnalytics(
                    currentState.selectedCareeIds,
                    currentState.selectedPeriod
                )
                
                _state.update { 
                    it.copy(
                        isLoadingAnalytics = false,
                        analyticsData = mockData,
                        error = "Using mock data: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun refreshData() {
        loadAvailableCarees()
        if (_state.value.selectedCareeIds.isNotEmpty()) {
            loadAnalytics()
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun getSelectedCareeNames(): List<String> {
        val currentState = _state.value
        return currentState.availableCarees
            .filter { it.id in currentState.selectedCareeIds }
            .map { it.name }
    }

    fun hasMultipleCarees(): Boolean {
        return _state.value.availableCarees.size > 1
    }

    fun onCleared() {
        viewModelScope.cancel()
    }
}