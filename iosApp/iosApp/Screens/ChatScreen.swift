import SwiftUI
import shared

struct ChatScreen: View {
    let chatId: String
    let otherUserName: String
    let currentUserId: String
    
    @StateObject private var viewModel: ChatScreenViewModel
    @EnvironmentObject private var navigationManager: NavigationManager
    @State private var scrollProxy: ScrollViewReader?
    
    init(chatId: String, otherUserName: String = "Chat", currentUserId: String) {
        self.chatId = chatId
        self.otherUserName = otherUserName
        self.currentUserId = currentUserId
        self._viewModel = StateObject(wrappedValue: ChatScreenViewModel(chatId: chatId, currentUserId: currentUserId))
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Messages List
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 8) {
                        if viewModel.isLoading && viewModel.messages.isEmpty {
                            ProgressView("Loading messages...")
                                .frame(maxWidth: .infinity, maxHeight: .infinity)
                                .padding()
                        } else {
                            ForEach(viewModel.messages, id: \.id) { message in
                                MessageBubble(
                                    message: message,
                                    isCurrentUser: message.senderId == currentUserId,
                                    currentUserId: currentUserId
                                )
                                .id(message.id)
                                .onAppear {
                                    // Mark message as read when it appears
                                    if message.senderId != currentUserId && message.status != .read {
                                        viewModel.markMessageAsRead(message.id)
                                    }
                                }
                            }
                            
                            // Typing Indicator
                            TypingIndicatorContainer(
                                isVisible: viewModel.otherUserTyping?.isTyping == true
                            )
                            .id("typing-indicator")
                        }
                    }
                    .padding(.vertical, 8)
                }
                .onAppear {
                    scrollProxy = proxy
                    scrollToBottom()
                }
                .onChange(of: viewModel.messages.count) { _ in
                    scrollToBottom()
                }
                .refreshable {
                    viewModel.refreshMessages()
                }
            }
            
            Divider()
            
            // Message Input
            MessageInput(
                message: $viewModel.currentMessage,
                onSend: {
                    viewModel.sendMessage()
                    scrollToBottom()
                },
                isSending: viewModel.isSendingMessage
            )
        }
        .navigationTitle(otherUserName)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                HStack(spacing: 4) {
                    Circle()
                        .fill(viewModel.isOnline ? .green : .gray)
                        .frame(width: 8, height: 8)
                    
                    Text(viewModel.isOnline ? "Online" : "Offline")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
        .alert("Error", isPresented: .constant(viewModel.error != nil)) {
            Button("OK") {
                viewModel.clearError()
            }
        } message: {
            if let error = viewModel.error {
                Text(error)
            }
        }
        .onAppear {
            viewModel.markAllAsRead()
        }
        .onDisappear {
            // Clean up when leaving the chat
        }
    }
    
    private func scrollToBottom() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
            withAnimation(.easeOut(duration: 0.3)) {
                if let lastMessage = viewModel.messages.last {
                    scrollProxy?.scrollTo(lastMessage.id, anchor: .bottom)
                } else if viewModel.otherUserTyping?.isTyping == true {
                    scrollProxy?.scrollTo("typing-indicator", anchor: .bottom)
                }
            }
        }
    }
}

#Preview {
    NavigationView {
        ChatScreen(
            chatId: "sample_chat_id",
            otherUserName: "John Doe",
            currentUserId: "current_user_id"
        )
        .environmentObject(NavigationManager())
    }
}