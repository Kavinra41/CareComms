import SwiftUI
import shared
import Combine

@MainActor
class DetailsTreeViewModel: ObservableObject {
    @Published var state = DetailsTreeViewState()
    @Published var isLoading = false
    @Published var error: String?
    @Published var availableCarees: [CareeInfo] = []
    @Published var selectedCarees: Set<String> = []
    @Published var expandedCategories: Set<String> = []
    @Published var expandedDetails: Set<String> = []
    @Published var treeData: [TreeNode] = []
    
    private let sharedViewModel: shared.DetailsTreeViewModel
    private var cancellables = Set<AnyCancellable>()
    
    init(carerId: String) {
        // Initialize shared ViewModel with mock analytics repository
        let mockRepository = MockAnalyticsRepository()
        let analyticsUseCase = shared.AnalyticsUseCase(analyticsRepository: mockRepository)
        self.sharedViewModel = shared.DetailsTreeViewModel(
            analyticsUseCase: analyticsUseCase,
            carerId: carerId
        )
        
        observeSharedState()
        loadInitialData()
    }
    
    private func observeSharedState() {
        // Observe shared state changes
        Task {
            do {
                for try await state in sharedViewModel.state {
                    await MainActor.run {
                        self.state = state
                        self.isLoading = state.isLoading
                        self.error = state.error
                        self.availableCarees = state.availableCarees
                        self.expandedCategories = state.expandedCategories
                        self.expandedDetails = state.expandedDetails
                        
                        // Convert shared tree nodes to iOS tree nodes
                        self.treeData = convertToIOSTreeNodes(state.treeNodes)
                        
                        // Auto-select first caree if only one available and none selected
                        if self.availableCarees.count == 1 && self.selectedCarees.isEmpty {
                            self.selectedCarees.insert(self.availableCarees[0].id)
                            self.loadTreeDataForSelectedCarees()
                        }
                    }
                }
            } catch {
                await MainActor.run {
                    self.error = "Failed to observe state: \(error.localizedDescription)"
                }
            }
        }
    }
    
    private func loadInitialData() {
        sharedViewModel.handleAction(action: DetailsTreeAction.LoadCarees())
    }
    
    func toggleCareeSelection(_ careeId: String) {
        if selectedCarees.contains(careeId) {
            selectedCarees.remove(careeId)
        } else {
            selectedCarees.insert(careeId)
        }
        loadTreeDataForSelectedCarees()
    }
    
    func expandCategory(_ categoryId: String) {
        expandedCategories.insert(categoryId)
        sharedViewModel.handleAction(action: DetailsTreeAction.ExpandCategory(categoryId: categoryId))
    }
    
    func collapseCategory(_ categoryId: String) {
        expandedCategories.remove(categoryId)
        sharedViewModel.handleAction(action: DetailsTreeAction.CollapseCategory(categoryId: categoryId))
    }
    
    func expandDetail(_ detailId: String) {
        expandedDetails.insert(detailId)
        sharedViewModel.handleAction(action: DetailsTreeAction.ExpandDetail(detailId: detailId))
    }
    
    func collapseDetail(_ detailId: String) {
        expandedDetails.remove(detailId)
        sharedViewModel.handleAction(action: DetailsTreeAction.CollapseDetail(detailId: detailId))
    }
    
    func refreshData() {
        sharedViewModel.handleAction(action: DetailsTreeAction.RefreshData())
    }
    
    func clearError() {
        error = nil
        sharedViewModel.handleAction(action: DetailsTreeAction.ClearError())
    }
    
    private func loadTreeDataForSelectedCarees() {
        guard !selectedCarees.isEmpty else {
            treeData = []
            return
        }
        
        // Generate tree data for selected carees
        treeData = generateTreeDataForCarees(Array(selectedCarees))
    }
    
    private func convertToIOSTreeNodes(_ sharedNodes: [shared.DetailsTreeNode]) -> [TreeNode] {
        return sharedNodes.map { sharedNode in
            TreeNode(
                id: sharedNode.id,
                title: sharedNode.title,
                type: convertNodeType(sharedNode.type),
                children: convertToIOSTreeNodes(sharedNode.children),
                data: sharedNode.data as? String
            )
        }
    }
    
    private func convertNodeType(_ sharedType: shared.NodeType) -> TreeNode.TreeNodeType {
        switch sharedType {
        case .caree:
            return .caree
        case .category:
            return .category
        case .detail:
            return .detail
        case .item:
            return .item
        default:
            return .item
        }
    }
    
    private func generateTreeDataForCarees(_ careeIds: [String]) -> [TreeNode] {
        return careeIds.compactMap { careeId in
            guard let caree = availableCarees.first(where: { $0.id == careeId }) else {
                return nil
            }
            
            return TreeNode(
                id: "caree-\(careeId)",
                title: caree.name,
                type: .caree,
                children: [
                    // Health Information Category
                    TreeNode(
                        id: "health-\(careeId)",
                        title: "Health Information",
                        type: .category,
                        children: [
                            TreeNode(
                                id: "medications-\(careeId)",
                                title: "Medications",
                                type: .detail,
                                children: [
                                    TreeNode(id: "med1-\(careeId)", title: "Lisinopril 10mg", type: .item, children: [], data: "Daily, Morning"),
                                    TreeNode(id: "med2-\(careeId)", title: "Metformin 500mg", type: .item, children: [], data: "Twice daily"),
                                    TreeNode(id: "med3-\(careeId)", title: "Vitamin D3", type: .item, children: [], data: "Weekly")
                                ],
                                data: nil
                            ),
                            TreeNode(
                                id: "vitals-\(careeId)",
                                title: "Vital Signs",
                                type: .detail,
                                children: [
                                    TreeNode(id: "bp-\(careeId)", title: "Blood Pressure", type: .item, children: [], data: "120/80 mmHg"),
                                    TreeNode(id: "hr-\(careeId)", title: "Heart Rate", type: .item, children: [], data: "72 bpm"),
                                    TreeNode(id: "temp-\(careeId)", title: "Temperature", type: .item, children: [], data: "98.6°F")
                                ],
                                data: nil
                            ),
                            TreeNode(
                                id: "conditions-\(careeId)",
                                title: "Medical Conditions",
                                type: .detail,
                                children: caree.healthConditions.enumerated().map { index, condition in
                                    TreeNode(
                                        id: "condition\(index)-\(careeId)",
                                        title: condition,
                                        type: .item,
                                        children: [],
                                        data: "Managed"
                                    )
                                },
                                data: nil
                            )
                        ],
                        data: nil
                    ),
                    
                    // Daily Activities Category
                    TreeNode(
                        id: "activities-\(careeId)",
                        title: "Daily Activities",
                        type: .category,
                        children: [
                            TreeNode(
                                id: "mobility-\(careeId)",
                                title: "Mobility",
                                type: .detail,
                                children: [
                                    TreeNode(id: "walking-\(careeId)", title: "Walking", type: .item, children: [], data: "30 min daily"),
                                    TreeNode(id: "stairs-\(careeId)", title: "Stairs", type: .item, children: [], data: "With assistance"),
                                    TreeNode(id: "balance-\(careeId)", title: "Balance", type: .item, children: [], data: "Good")
                                ],
                                data: nil
                            ),
                            TreeNode(
                                id: "selfcare-\(careeId)",
                                title: "Self Care",
                                type: .detail,
                                children: [
                                    TreeNode(id: "bathing-\(careeId)", title: "Bathing", type: .item, children: [], data: "Independent"),
                                    TreeNode(id: "dressing-\(careeId)", title: "Dressing", type: .item, children: [], data: "Independent"),
                                    TreeNode(id: "eating-\(careeId)", title: "Eating", type: .item, children: [], data: "Independent")
                                ],
                                data: nil
                            )
                        ],
                        data: nil
                    ),
                    
                    // Communication Category
                    TreeNode(
                        id: "communication-\(careeId)",
                        title: "Communication",
                        type: .category,
                        children: [
                            TreeNode(
                                id: "messages-\(careeId)",
                                title: "Recent Messages",
                                type: .detail,
                                children: [
                                    TreeNode(id: "msg1-\(careeId)", title: "Good morning!", type: .item, children: [], data: "Today 8:00 AM"),
                                    TreeNode(id: "msg2-\(careeId)", title: "Took my medication", type: .item, children: [], data: "Yesterday 9:15 AM"),
                                    TreeNode(id: "msg3-\(careeId)", title: "Feeling well today", type: .item, children: [], data: "Yesterday 2:30 PM")
                                ],
                                data: nil
                            ),
                            TreeNode(
                                id: "frequency-\(careeId)",
                                title: "Communication Frequency",
                                type: .detail,
                                children: [
                                    TreeNode(id: "daily-\(careeId)", title: "Daily Average", type: .item, children: [], data: "5 messages"),
                                    TreeNode(id: "weekly-\(careeId)", title: "Weekly Total", type: .item, children: [], data: "35 messages"),
                                    TreeNode(id: "response-\(careeId)", title: "Response Time", type: .item, children: [], data: "< 2 hours")
                                ],
                                data: nil
                            )
                        ],
                        data: nil
                    )
                ],
                data: nil
            )
        }
    }
    
    deinit {
        sharedViewModel.onCleared()
    }
}

// MARK: - Mock Analytics Repository
class MockAnalyticsRepository: AnalyticsRepository {
    func getCareeAnalytics(careeId: String, period: AnalyticsPeriod) async -> KotlinResult<AnalyticsData> {
        let mockData = AnalyticsData(
            dailyData: [],
            weeklyData: [],
            biweeklyData: [],
            notes: []
        )
        return KotlinResult.success(value: mockData)
    }
    
    func getMultiCareeAnalytics(careeIds: [String], period: AnalyticsPeriod) async -> KotlinResult<AnalyticsData> {
        let mockData = AnalyticsData(
            dailyData: [],
            weeklyData: [],
            biweeklyData: [],
            notes: []
        )
        return KotlinResult.success(value: mockData)
    }
    
    func getAvailableCarees(carerId: String) async -> KotlinResult<[CareeInfo]> {
        let mockCarees = [
            CareeInfo(
                id: "caree1",
                name: "Mary Johnson",
                age: 78,
                healthConditions: ["Diabetes", "Hypertension"]
            ),
            CareeInfo(
                id: "caree2",
                name: "Robert Smith",
                age: 82,
                healthConditions: ["Arthritis"]
            ),
            CareeInfo(
                id: "caree3",
                name: "Eleanor Davis",
                age: 75,
                healthConditions: ["Heart condition"]
            )
        ]
        
        return KotlinResult.success(value: mockCarees)
    }
    
    func getDetailsTree(careeId: String) async -> KotlinResult<[shared.DetailsTreeNode]> {
        let mockNodes: [shared.DetailsTreeNode] = []
        return KotlinResult.success(value: mockNodes)
    }
    
    func getDetailsTreeForMultipleCarees(careeIds: [String]) async -> KotlinResult<[shared.DetailsTreeNode]> {
        let mockNodes: [shared.DetailsTreeNode] = []
        return KotlinResult.success(value: mockNodes)
    }
    
    func getMockAnalyticsData(careeId: String, period: AnalyticsPeriod) async -> AnalyticsData {
        return AnalyticsData(
            dailyData: [],
            weeklyData: [],
            biweeklyData: [],
            notes: []
        )
    }
    
    func getMockDetailsTree(careeId: String) async -> [shared.DetailsTreeNode] {
        return []
    }
}

// MARK: - Supporting Types
struct MockTreeCaree {
    let id: String
    let name: String
}

struct TreeNode {
    let id: String
    let title: String
    let type: TreeNodeType
    let children: [TreeNode]
    let data: String?
    
    enum TreeNodeType {
        case caree, category, detail, item
    }
}