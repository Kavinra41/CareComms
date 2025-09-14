import SwiftUI
import shared
import Combine

// Swift wrapper for the shared AnalyticsViewModel
@MainActor
class DashboardViewModel: ObservableObject {
    private let sharedViewModel: AnalyticsViewModel
    private var cancellables = Set<AnyCancellable>()
    
    @Published var state = DashboardViewState()
    @Published var isLoading = false
    @Published var error: String?
    
    init(carerId: String) {
        // Initialize shared ViewModel with dependencies
        let koinHelper = KoinHelper()
        let analyticsUseCase = koinHelper.getAnalyticsUseCase()
        self.sharedViewModel = AnalyticsViewModel(analyticsUseCase: analyticsUseCase, carerId: carerId)
        
        observeState()
        loadInitialData()
    }
    
    private func observeState() {
        // Observe shared ViewModel state changes
        sharedViewModel.state.collect { [weak self] sharedState in
            DispatchQueue.main.async {
                self?.updateState(from: sharedState)
            }
        }
    }
    
    private func updateState(from sharedState: AnalyticsViewState) {
        self.state = DashboardViewState(
            availableCarees: sharedState.availableCarees.map { CareeInfoSwift(from: $0) },
            selectedCareeIds: Set(sharedState.selectedCareeIds),
            selectedPeriod: AnalyticsPeriodSwift(from: sharedState.selectedPeriod),
            analyticsData: sharedState.analyticsData.map { AnalyticsDataSwift(from: $0) },
            isLoadingCarees: sharedState.isLoadingCarees,
            isLoadingAnalytics: sharedState.isLoadingAnalytics
        )
        
        self.isLoading = sharedState.isLoadingCarees || sharedState.isLoadingAnalytics
        self.error = sharedState.error
    }
    
    private func loadInitialData() {
        sharedViewModel.handleAction(action: AnalyticsAction.LoadCarees())
    }
    
    func selectCaree(_ careeId: String) {
        sharedViewModel.handleAction(action: AnalyticsAction.SelectCaree(careeId: careeId))
    }
    
    func deselectCaree(_ careeId: String) {
        sharedViewModel.handleAction(action: AnalyticsAction.DeselectCaree(careeId: careeId))
    }
    
    func changePeriod(_ period: AnalyticsPeriodSwift) {
        let sharedPeriod = period.toShared()
        sharedViewModel.handleAction(action: AnalyticsAction.ChangePeriod(period: sharedPeriod))
    }
    
    func refreshData() {
        sharedViewModel.handleAction(action: AnalyticsAction.RefreshData())
    }
    
    func clearError() {
        sharedViewModel.handleAction(action: AnalyticsAction.ClearError())
    }
}

// MARK: - Swift wrapper types
struct DashboardViewState {
    let availableCarees: [CareeInfoSwift]
    let selectedCareeIds: Set<String>
    let selectedPeriod: AnalyticsPeriodSwift
    let analyticsData: AnalyticsDataSwift?
    let isLoadingCarees: Bool
    let isLoadingAnalytics: Bool
    
    init(
        availableCarees: [CareeInfoSwift] = [],
        selectedCareeIds: Set<String> = [],
        selectedPeriod: AnalyticsPeriodSwift = .daily,
        analyticsData: AnalyticsDataSwift? = nil,
        isLoadingCarees: Bool = false,
        isLoadingAnalytics: Bool = false
    ) {
        self.availableCarees = availableCarees
        self.selectedCareeIds = selectedCareeIds
        self.selectedPeriod = selectedPeriod
        self.analyticsData = analyticsData
        self.isLoadingCarees = isLoadingCarees
        self.isLoadingAnalytics = isLoadingAnalytics
    }
}

struct CareeInfoSwift: Identifiable {
    let id: String
    let name: String
    let age: Int32
    let healthConditions: [String]
    
    init(from shared: CareeInfo) {
        self.id = shared.id
        self.name = shared.name
        self.age = shared.age
        self.healthConditions = shared.healthConditions
    }
}

enum AnalyticsPeriodSwift: String, CaseIterable {
    case daily = "Daily"
    case weekly = "Weekly"
    case biweekly = "Bi-weekly"
    case monthly = "Monthly"
    
    init(from shared: AnalyticsPeriod) {
        switch shared {
        case .daily: self = .daily
        case .weekly: self = .weekly
        case .biweekly: self = .biweekly
        case .monthly: self = .monthly
        default: self = .daily
        }
    }
    
    func toShared() -> AnalyticsPeriod {
        switch self {
        case .daily: return .daily
        case .weekly: return .weekly
        case .biweekly: return .biweekly
        case .monthly: return .monthly
        }
    }
}

struct AnalyticsDataSwift {
    let dailyData: [DailyMetricSwift]
    let weeklyData: [WeeklyMetricSwift]
    let biweeklyData: [BiweeklyMetricSwift]
    let notes: [AnalyticsNoteSwift]
    
    init(from shared: AnalyticsData) {
        self.dailyData = shared.dailyData.map { DailyMetricSwift(from: $0) }
        self.weeklyData = shared.weeklyData.map { WeeklyMetricSwift(from: $0) }
        self.biweeklyData = shared.biweeklyData.map { BiweeklyMetricSwift(from: $0) }
        self.notes = shared.notes.map { AnalyticsNoteSwift(from: $0) }
    }
}

struct DailyMetricSwift: Identifiable {
    let id = UUID()
    let date: String
    let activityLevel: Int32
    let communicationCount: Int32
    let notes: String
    
    init(from shared: DailyMetric) {
        self.date = shared.date
        self.activityLevel = shared.activityLevel
        self.communicationCount = shared.communicationCount
        self.notes = shared.notes
    }
}

struct WeeklyMetricSwift: Identifiable {
    let id = UUID()
    let weekStart: String
    let weekEnd: String
    let averageActivityLevel: Int32
    let totalCommunications: Int32
    let notes: String
    
    init(from shared: WeeklyMetric) {
        self.weekStart = shared.weekStart
        self.weekEnd = shared.weekEnd
        self.averageActivityLevel = shared.averageActivityLevel
        self.totalCommunications = shared.totalCommunications
        self.notes = shared.notes
    }
}

struct BiweeklyMetricSwift: Identifiable {
    let id = UUID()
    let periodStart: String
    let periodEnd: String
    let averageActivityLevel: Int32
    let totalCommunications: Int32
    let trends: [String]
    let notes: String
    
    init(from shared: BiweeklyMetric) {
        self.periodStart = shared.periodStart
        self.periodEnd = shared.periodEnd
        self.averageActivityLevel = shared.averageActivityLevel
        self.totalCommunications = shared.totalCommunications
        self.trends = shared.trends
        self.notes = shared.notes
    }
}

struct AnalyticsNoteSwift: Identifiable {
    let id: String
    let content: String
    let timestamp: Int64
    let type: String
    
    init(from shared: AnalyticsNote) {
        self.id = shared.id
        self.content = shared.content
        self.timestamp = shared.timestamp
        self.type = shared.type
    }
}