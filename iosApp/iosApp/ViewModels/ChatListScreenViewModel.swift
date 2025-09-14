import SwiftUI
import shared
import Combine

@MainActor
class ChatListScreenViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var chatPreviews: [ChatPreview] = []
    @Published var filteredChats: [ChatPreview] = []
    @Published var error: String?
    @Published var unreadCount = 0
    @Published var generatedInvitationLink: String?
    
    private var chatListViewModel: ChatListViewModel?
    private var invitationUseCase: InvitationUseCase?
    private var cancellables = Set<AnyCancellable>()
    private let currentCarerId = "mock_carer_id" // In real app, get from auth state
    
    init() {
        setupViewModels()
        observeState()
    }
    
    private func setupViewModels() {
        // Get dependencies from Koin
        let chatUseCase = KoinHelper.getChatUseCase()
        chatListViewModel = ChatListViewModel(chatUseCase: chatUseCase, carerId: currentCarerId)
        
        invitationUseCase = KoinHelper.getInvitationUseCase()
    }
    
    private func observeState() {
        guard let viewModel = chatListViewModel else { return }
        
        // Observe state changes
        Task {
            for await state in viewModel.state {
                await MainActor.run {
                    self.isLoading = state.isLoading
                    self.chatPreviews = state.chatPreviews
                    self.filteredChats = state.filteredChats
                    self.error = state.error
                    self.unreadCount = Int(state.unreadCount)
                }
            }
        }
        
        // Observe effects
        Task {
            for await effect in viewModel.effects {
                await MainActor.run {
                    switch effect {
                    case let effect as ChatEffect.ShowError:
                        self.error = effect.message
                    default:
                        break
                    }
                }
            }
        }
    }
    
    func loadChats() {
        // For now, load mock data for testing
        // In real implementation, this would use the chatListViewModel
        loadMockData()
        // chatListViewModel?.handleAction(action: ChatListAction.LoadChats())
    }
    
    private func loadMockData() {
        isLoading = true
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            let mockChats = [
                ChatPreview(
                    chatId: "chat_1",
                    careeName: "Eleanor Smith",
                    lastMessage: "Good morning! How are you feeling today?",
                    lastMessageTime: Int64(Date().timeIntervalSince1970 * 1000 - 3600000), // 1 hour ago
                    unreadCount: 2,
                    isOnline: true
                ),
                ChatPreview(
                    chatId: "chat_2",
                    careeName: "Robert Johnson",
                    lastMessage: "Thank you for checking on me. I'm doing well.",
                    lastMessageTime: Int64(Date().timeIntervalSince1970 * 1000 - 7200000), // 2 hours ago
                    unreadCount: 0,
                    isOnline: false
                ),
                ChatPreview(
                    chatId: "chat_3",
                    careeName: "Margaret Davis",
                    lastMessage: "I took my medication this morning as scheduled.",
                    lastMessageTime: Int64(Date().timeIntervalSince1970 * 1000 - 86400000), // 1 day ago
                    unreadCount: 1,
                    isOnline: true
                )
            ]
            
            self.isLoading = false
            self.chatPreviews = mockChats
            self.filteredChats = mockChats
            self.unreadCount = mockChats.reduce(0) { $0 + Int($1.unreadCount) }
        }
    }
    
    func searchChats(query: String) {
        // Filter mock data based on search query
        if query.isEmpty {
            filteredChats = chatPreviews
        } else {
            filteredChats = chatPreviews.filter { chat in
                chat.careeName.localizedCaseInsensitiveContains(query) ||
                chat.lastMessage.localizedCaseInsensitiveContains(query)
            }
        }
        // chatListViewModel?.handleAction(action: ChatListAction.SearchChats(query: query))
    }
    
    func refreshChats() async {
        // Simulate refresh with mock data
        try? await Task.sleep(nanoseconds: 500_000_000) // 0.5 seconds
        loadMockData()
        // chatListViewModel?.handleAction(action: ChatListAction.RefreshChats())
    }
    
    func clearError() {
        error = nil
        chatListViewModel?.handleAction(action: ChatListAction.ClearError())
    }
    
    func generateInvitationLink() async {
        guard let invitationUseCase = invitationUseCase else {
            error = "Invitation service not available"
            return
        }
        
        // For now, generate a mock invitation link
        // In a real implementation, this would call the Kotlin suspend function
        try? await Task.sleep(nanoseconds: 1_000_000_000) // 1 second delay to simulate network call
        
        let mockToken = UUID().uuidString
        generatedInvitationLink = "carecomms://invite?token=\(mockToken)"
    }
    
    deinit {
        chatListViewModel?.onCleared()
    }
}

