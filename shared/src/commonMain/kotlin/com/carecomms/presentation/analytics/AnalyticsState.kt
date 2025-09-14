package com.carecomms.presentation.analytics

import com.carecomms.data.models.*

data class AnalyticsViewState(
    val isLoadingCarees: Boolean = false,
    val isLoadingAnalytics: Boolean = false,
    val availableCarees: List<CareeInfo> = emptyList(),
    val selectedCareeIds: List<String> = emptyList(),
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.DAILY,
    val analyticsData: AnalyticsData? = null,
    val error: String? = null
)

sealed class AnalyticsAction {
    object LoadCarees : AnalyticsAction()
    data class SelectCaree(val careeId: String) : AnalyticsAction()
    data class DeselectCaree(val careeId: String) : AnalyticsAction()
    object SelectAllCarees : AnalyticsAction()
    object DeselectAllCarees : AnalyticsAction()
    data class ChangePeriod(val period: AnalyticsPeriod) : AnalyticsAction()
    object LoadAnalytics : AnalyticsAction()
    object RefreshData : AnalyticsAction()
    object ClearError : AnalyticsAction()
}

sealed class AnalyticsEffect {
    data class ShowError(val message: String) : AnalyticsEffect()
    object DataRefreshed : AnalyticsEffect()
}