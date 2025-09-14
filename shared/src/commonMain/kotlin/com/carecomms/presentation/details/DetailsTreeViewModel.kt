package com.carecomms.presentation.details

import com.carecomms.data.models.*
import com.carecomms.domain.usecase.AnalyticsUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class DetailsTreeViewModel(
    private val analyticsUseCase: AnalyticsUseCase,
    private val carerId: String
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _state = MutableStateFlow(DetailsTreeViewState())
    val state: StateFlow<DetailsTreeViewState> = _state.asStateFlow()
    
    private val _effects = MutableSharedFlow<DetailsTreeEffect>()
    val effects: SharedFlow<DetailsTreeEffect> = _effects.asSharedFlow()

    init {
        loadCarees()
    }

    fun handleAction(action: DetailsTreeAction) {
        when (action) {
            is DetailsTreeAction.LoadCarees -> loadCarees()
            is DetailsTreeAction.SelectCaree -> selectCaree(action.careeId)
            is DetailsTreeAction.ExpandCategory -> expandCategory(action.categoryId)
            is DetailsTreeAction.CollapseCategory -> collapseCategory(action.categoryId)
            is DetailsTreeAction.ExpandDetail -> expandDetail(action.detailId)
            is DetailsTreeAction.CollapseDetail -> collapseDetail(action.detailId)
            is DetailsTreeAction.NavigateBack -> navigateBack()
            is DetailsTreeAction.RefreshData -> refreshData()
            is DetailsTreeAction.ClearError -> clearError()
        }
    }

    private fun loadCarees() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = analyticsUseCase.getAvailableCarees(carerId)
                
                if (result.isSuccess) {
                    val carees = result.getOrThrow()
                    val treeNodes = carees.map { caree ->
                        createCareeTreeNode(caree)
                    }
                    
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            treeNodes = treeNodes,
                            availableCarees = carees
                        ) 
                    }
                } else {
                    // Generate mock tree structure
                    val mockCarees = generateMockCarees()
                    val treeNodes = mockCarees.map { caree ->
                        createCareeTreeNode(caree)
                    }
                    
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            treeNodes = treeNodes,
                            availableCarees = mockCarees,
                            error = "Using mock data: ${result.exceptionOrNull()?.message}"
                        ) 
                    }
                }
            } catch (e: Exception) {
                // Generate mock tree structure on error
                val mockCarees = generateMockCarees()
                val treeNodes = mockCarees.map { caree ->
                    createCareeTreeNode(caree)
                }
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        treeNodes = treeNodes,
                        availableCarees = mockCarees,
                        error = "Using mock data: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun selectCaree(careeId: String) {
        _state.update { currentState ->
            val selectedCaree = currentState.availableCarees.find { it.id == careeId }
            if (selectedCaree != null) {
                val detailsTree = createDetailedTreeForCaree(selectedCaree)
                currentState.copy(
                    selectedCareeId = careeId,
                    currentView = DetailsTreeView.CATEGORIES,
                    navigationStack = listOf(DetailsTreeView.CAREES),
                    detailsTree = detailsTree
                )
            } else {
                currentState
            }
        }
    }

    private fun expandCategory(categoryId: String) {
        _state.update { currentState ->
            val expandedCategories = currentState.expandedCategories + categoryId
            currentState.copy(expandedCategories = expandedCategories)
        }
    }

    private fun collapseCategory(categoryId: String) {
        _state.update { currentState ->
            val expandedCategories = currentState.expandedCategories - categoryId
            currentState.copy(expandedCategories = expandedCategories)
        }
    }

    private fun expandDetail(detailId: String) {
        _state.update { currentState ->
            val expandedDetails = currentState.expandedDetails + detailId
            currentState.copy(expandedDetails = expandedDetails)
        }
    }

    private fun collapseDetail(detailId: String) {
        _state.update { currentState ->
            val expandedDetails = currentState.expandedDetails - detailId
            currentState.copy(expandedDetails = expandedDetails)
        }
    }

    private fun navigateBack() {
        _state.update { currentState ->
            if (currentState.navigationStack.isNotEmpty()) {
                val previousView = currentState.navigationStack.last()
                val newStack = currentState.navigationStack.dropLast(1)
                
                currentState.copy(
                    currentView = previousView,
                    navigationStack = newStack,
                    selectedCareeId = if (previousView == DetailsTreeView.CAREES) null else currentState.selectedCareeId
                )
            } else {
                currentState
            }
        }
    }

    private fun refreshData() {
        loadCarees()
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun createCareeTreeNode(caree: CareeInfo): DetailsTreeNode {
        return DetailsTreeNode(
            id = caree.id,
            title = caree.name,
            type = NodeType.CAREE,
            children = emptyList(),
            data = caree,
            isExpanded = false
        )
    }

    private fun createDetailedTreeForCaree(caree: CareeInfo): List<DetailsTreeNode> {
        return listOf(
            DetailsTreeNode(
                id = "${caree.id}_health",
                title = "Health Information",
                type = NodeType.CATEGORY,
                children = createHealthDetails(caree.id),
                data = null,
                isExpanded = false
            ),
            DetailsTreeNode(
                id = "${caree.id}_communication",
                title = "Communication History",
                type = NodeType.CATEGORY,
                children = createCommunicationDetails(caree.id),
                data = null,
                isExpanded = false
            ),
            DetailsTreeNode(
                id = "${caree.id}_activities",
                title = "Daily Activities",
                type = NodeType.CATEGORY,
                children = createActivityDetails(caree.id),
                data = null,
                isExpanded = false
            ),
            DetailsTreeNode(
                id = "${caree.id}_notes",
                title = "Care Notes",
                type = NodeType.CATEGORY,
                children = createNotesDetails(caree.id),
                data = null,
                isExpanded = false
            )
        )
    }

    private fun createHealthDetails(careeId: String): List<DetailsTreeNode> {
        return listOf(
            DetailsTreeNode(
                id = "${careeId}_health_vitals",
                title = "Vital Signs",
                type = NodeType.DETAIL,
                children = listOf(
                    DetailsTreeNode(
                        id = "${careeId}_health_vitals_bp",
                        title = "Blood Pressure: 120/80 mmHg",
                        type = NodeType.ITEM,
                        children = emptyList(),
                        data = "Normal range",
                        isExpanded = false
                    ),
                    DetailsTreeNode(
                        id = "${careeId}_health_vitals_hr",
                        title = "Heart Rate: 72 bpm",
                        type = NodeType.ITEM,
                        children = emptyList(),
                        data = "Normal",
                        isExpanded = false
                    )
                ),
                data = null,
                isExpanded = false
            ),
            DetailsTreeNode(
                id = "${careeId}_health_medications",
                title = "Medications",
                type = NodeType.DETAIL,
                children = listOf(
                    DetailsTreeNode(
                        id = "${careeId}_health_medications_daily",
                        title = "Daily Medications (3)",
                        type = NodeType.ITEM,
                        children = emptyList(),
                        data = "All taken on schedule",
                        isExpanded = false
                    )
                ),
                data = null,
                isExpanded = false
            )
        )
    }

    private fun createCommunicationDetails(careeId: String): List<DetailsTreeNode> {
        return listOf(
            DetailsTreeNode(
                id = "${careeId}_comm_recent",
                title = "Recent Messages",
                type = NodeType.DETAIL,
                children = listOf(
                    DetailsTreeNode(
                        id = "${careeId}_comm_recent_today",
                        title = "Today: 5 messages",
                        type = NodeType.ITEM,
                        children = emptyList(),
                        data = "Last message: 2 hours ago",
                        isExpanded = false
                    )
                ),
                data = null,
                isExpanded = false
            )
        )
    }

    private fun createActivityDetails(careeId: String): List<DetailsTreeNode> {
        return listOf(
            DetailsTreeNode(
                id = "${careeId}_activities_daily",
                title = "Daily Routine",
                type = NodeType.DETAIL,
                children = listOf(
                    DetailsTreeNode(
                        id = "${careeId}_activities_daily_morning",
                        title = "Morning Routine: Completed",
                        type = NodeType.ITEM,
                        children = emptyList(),
                        data = "8:00 AM - All tasks done",
                        isExpanded = false
                    )
                ),
                data = null,
                isExpanded = false
            )
        )
    }

    private fun createNotesDetails(careeId: String): List<DetailsTreeNode> {
        return listOf(
            DetailsTreeNode(
                id = "${careeId}_notes_care",
                title = "Care Notes",
                type = NodeType.DETAIL,
                children = listOf(
                    DetailsTreeNode(
                        id = "${careeId}_notes_care_latest",
                        title = "Latest Note",
                        type = NodeType.ITEM,
                        children = emptyList(),
                        data = "Patient is doing well, good spirits today",
                        isExpanded = false
                    )
                ),
                data = null,
                isExpanded = false
            )
        )
    }

    private fun generateMockCarees(): List<CareeInfo> {
        return listOf(
            CareeInfo(
                id = "caree1",
                name = "Mary Johnson",
                age = 78,
                healthConditions = listOf("Diabetes", "Hypertension")
            ),
            CareeInfo(
                id = "caree2", 
                name = "Robert Smith",
                age = 82,
                healthConditions = listOf("Arthritis")
            ),
            CareeInfo(
                id = "caree3",
                name = "Eleanor Davis",
                age = 75,
                healthConditions = listOf("Heart condition")
            )
        )
    }

    fun onCleared() {
        viewModelScope.cancel()
    }
}