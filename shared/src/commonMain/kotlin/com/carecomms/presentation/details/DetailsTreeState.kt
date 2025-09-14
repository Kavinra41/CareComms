package com.carecomms.presentation.details

import com.carecomms.data.models.*

data class DetailsTreeViewState(
    val isLoading: Boolean = false,
    val treeNodes: List<DetailsTreeNode> = emptyList(),
    val availableCarees: List<CareeInfo> = emptyList(),
    val selectedCareeId: String? = null,
    val currentView: DetailsTreeView = DetailsTreeView.CAREES,
    val navigationStack: List<DetailsTreeView> = emptyList(),
    val detailsTree: List<DetailsTreeNode> = emptyList(),
    val expandedCategories: Set<String> = emptySet(),
    val expandedDetails: Set<String> = emptySet(),
    val error: String? = null
)

enum class DetailsTreeView {
    CAREES,
    CATEGORIES,
    DETAILS,
    ITEMS
}

sealed class DetailsTreeAction {
    object LoadCarees : DetailsTreeAction()
    data class SelectCaree(val careeId: String) : DetailsTreeAction()
    data class ExpandCategory(val categoryId: String) : DetailsTreeAction()
    data class CollapseCategory(val categoryId: String) : DetailsTreeAction()
    data class ExpandDetail(val detailId: String) : DetailsTreeAction()
    data class CollapseDetail(val detailId: String) : DetailsTreeAction()
    object NavigateBack : DetailsTreeAction()
    object RefreshData : DetailsTreeAction()
    object ClearError : DetailsTreeAction()
}

sealed class DetailsTreeEffect {
    data class ShowError(val message: String) : DetailsTreeEffect()
    object DataRefreshed : DetailsTreeEffect()
    data class NavigateToDetail(val nodeId: String) : DetailsTreeEffect()
}