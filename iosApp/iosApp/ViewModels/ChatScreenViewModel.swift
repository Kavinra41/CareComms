import SwiftUI
import shared
import Combine

@MainActor
class ChatScreenViewModel: ObservableObject {
    @Published var messages: [Message] = []
    @Published var currentMessage: String = ""
    @Published var isLoading: Bool = false
    @Published var isTyping: Bool = false
    @Published var otherUserTyping: TypingStatus? = nil
    @Published var error: String? = nil
    @Published var isSendingMessage: Bool = false
    @Published var otherUserName: String = ""
    @Published var isOnline: Bool = false
    
    private let chatViewModel: ChatViewModel
    private let currentUserId: String
    private var cancellables = Set<AnyCancellable>()
    
    init(chatId: String, currentUserId: String) {
        self.currentUserId = currentUserId
        self.chatViewModel = KoinHelper.createChatViewModel(currentUserId: currentUserId)
        
        setupStateObservation()
        loadMessages(chatId: chatId)
    }
    
    private func setupStateObservation() {
        // Observe state changes from the shared ViewModel
        Task {
            for await state in chatViewModel.state {
                await MainActor.run {
                    self.messages = state.messages
                    self.currentMessage = state.currentMessage
                    self.isLoading = state.isLoading
                    self.isTyping = state.isTyping
                    self.otherUserTyping = state.otherUserTyping
                    self.error = state.error
                    self.isSendingMessage = state.isSendingMessage
                    self.otherUserName = state.otherUserName
                    self.isOnline = state.isOnline
                }
            }
        }
    }
    
    func loadMessages(chatId: String) {
        chatViewModel.handleAction(action: ChatAction.LoadMessages(chatId: chatId))
    }
    
    func sendMessage() {
        guard !currentMessage.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
        chatViewModel.handleAction(action: ChatAction.SendMessage(content: currentMessage))
    }
    
    func updateCurrentMessage(_ message: String) {
        chatViewModel.handleAction(action: ChatAction.UpdateCurrentMessage(message: message))
    }
    
    func markMessageAsRead(_ messageId: String) {
        chatViewModel.handleAction(action: ChatAction.MarkMessageAsRead(messageId: messageId))
    }
    
    func markAllAsRead() {
        chatViewModel.handleAction(action: ChatAction.MarkAllAsRead())
    }
    
    func clearError() {
        chatViewModel.handleAction(action: ChatAction.ClearError())
    }
    
    func refreshMessages() {
        chatViewModel.handleAction(action: ChatAction.RefreshMessages())
    }
    
    deinit {
        chatViewModel.onCleared()
    }
}