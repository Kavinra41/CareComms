import SwiftUI
import shared

struct ChatListScreen: View {
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject private var viewModel = ChatListScreenViewModel()
    @State private var searchText = ""
    @State private var showingInviteSheet = false
    @State private var invitationLink = ""
    @State private var isGeneratingLink = false
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Search Bar
                SearchBar(text: $searchText, onSearchButtonClicked: {
                    viewModel.searchChats(query: searchText)
                })
                .padding(.horizontal, 16)
                .padding(.top, 8)
                
                // Chat List
                if viewModel.isLoading && viewModel.chatPreviews.isEmpty {
                    LoadingView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if viewModel.filteredChats.isEmpty && !searchText.isEmpty {
                    EmptySearchView(searchQuery: searchText)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if viewModel.chatPreviews.isEmpty {
                    EmptyChatListView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List {
                        ForEach(viewModel.filteredChats, id: \.chatId) { chatPreview in
                            ChatPreviewRow(
                                chatPreview: chatPreview,
                                onTap: {
                                    navigationManager.navigate(to: .chat(chatId: chatPreview.chatId))
                                }
                            )
                            .listRowSeparator(.hidden)
                            .listRowInsets(EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16))
                        }
                    }
                    .listStyle(PlainListStyle())
                    .refreshable {
                        await viewModel.refreshChats()
                    }
                }
                
                // Error Message
                if let error = viewModel.error {
                    ErrorBanner(message: error) {
                        viewModel.clearError()
                    }
                }
            }
            .navigationTitle("Chats")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        generateInvitationLink()
                    }) {
                        if isGeneratingLink {
                            ProgressView()
                                .scaleEffect(0.8)
                        } else {
                            Image(systemName: "person.badge.plus")
                                .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6)) // Deep purple
                        }
                    }
                    .disabled(isGeneratingLink)
                }
            }
            .sheet(isPresented: $showingInviteSheet) {
                InviteShareSheet(invitationLink: invitationLink)
            }
            .onAppear {
                viewModel.loadChats()
            }
            .onChange(of: searchText) { newValue in
                viewModel.searchChats(query: newValue)
            }
        }
    }
    
    private func generateInvitationLink() {
        isGeneratingLink = true
        Task {
            await viewModel.generateInvitationLink()
            DispatchQueue.main.async {
                isGeneratingLink = false
                if let link = viewModel.generatedInvitationLink {
                    invitationLink = link
                    showingInviteSheet = true
                }
            }
        }
    }
}

// MARK: - Search Bar Component
struct SearchBar: View {
    @Binding var text: String
    var onSearchButtonClicked: () -> Void
    
    var body: some View {
        HStack {
            Image(systemName: "magnifyingglass")
                .foregroundColor(.gray)
            
            TextField("Search chats...", text: $text)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .onSubmit {
                    onSearchButtonClicked()
                }
        }
        .padding(.vertical, 8)
    }
}

// MARK: - Chat Preview Row Component
struct ChatPreviewRow: View {
    let chatPreview: ChatPreview
    let onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                // Avatar
                Circle()
                    .fill(Color(red: 0.8, green: 0.7, blue: 0.9)) // Light purple
                    .frame(width: 50, height: 50)
                    .overlay(
                        Text(String(chatPreview.careeName.prefix(1).uppercased()))
                            .font(.title2)
                            .fontWeight(.semibold)
                            .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6)) // Deep purple
                    )
                
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text(chatPreview.careeName)
                            .font(.headline)
                            .fontWeight(.semibold)
                            .foregroundColor(.primary)
                        
                        Spacer()
                        
                        HStack(spacing: 4) {
                            if chatPreview.isOnline {
                                Circle()
                                    .fill(Color.green)
                                    .frame(width: 8, height: 8)
                            }
                            
                            Text(formatTime(timestamp: chatPreview.lastMessageTime))
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    
                    HStack {
                        Text(chatPreview.lastMessage)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .lineLimit(2)
                            .multilineTextAlignment(.leading)
                        
                        Spacer()
                        
                        if chatPreview.unreadCount > 0 {
                            Text("\(chatPreview.unreadCount)")
                                .font(.caption)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color(red: 0.4, green: 0.2, blue: 0.6)) // Deep purple
                                .clipShape(Capsule())
                        }
                    }
                }
            }
            .padding(.vertical, 8)
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private func formatTime(timestamp: Long) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestamp / 1000))
        let formatter = DateFormatter()
        
        if Calendar.current.isToday(date) {
            formatter.timeStyle = .short
        } else if Calendar.current.isDate(date, equalTo: Date(), toGranularity: .weekOfYear) {
            formatter.dateFormat = "E"
        } else {
            formatter.dateFormat = "MM/dd"
        }
        
        return formatter.string(from: date)
    }
}

// MARK: - Loading View
struct LoadingView: View {
    var body: some View {
        VStack(spacing: 16) {
            ProgressView()
                .scaleEffect(1.2)
            Text("Loading chats...")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
    }
}

// MARK: - Empty States
struct EmptyChatListView: View {
    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "message.circle")
                .font(.system(size: 60))
                .foregroundColor(Color(red: 0.8, green: 0.7, blue: 0.9)) // Light purple
            
            VStack(spacing: 8) {
                Text("No Chats Yet")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
                
                Text("Invite carees to start chatting")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }
        }
        .padding(.horizontal, 40)
    }
}

struct EmptySearchView: View {
    let searchQuery: String
    
    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 60))
                .foregroundColor(Color(red: 0.8, green: 0.7, blue: 0.9)) // Light purple
            
            VStack(spacing: 8) {
                Text("No Results")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
                
                Text("No chats found for \"\(searchQuery)\"")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }
        }
        .padding(.horizontal, 40)
    }
}

// MARK: - Error Banner
struct ErrorBanner: View {
    let message: String
    let onDismiss: () -> Void
    
    var body: some View {
        HStack {
            Image(systemName: "exclamationmark.triangle.fill")
                .foregroundColor(.red)
            
            Text(message)
                .font(.subheadline)
                .foregroundColor(.primary)
                .lineLimit(2)
            
            Spacer()
            
            Button("Dismiss") {
                onDismiss()
            }
            .font(.caption)
            .foregroundColor(.blue)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.red.opacity(0.1))
        .cornerRadius(8)
        .padding(.horizontal, 16)
        .padding(.bottom, 8)
    }
}

// MARK: - Invite Share Sheet
struct InviteShareSheet: View {
    let invitationLink: String
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        NavigationView {
            VStack(spacing: 24) {
                VStack(spacing: 16) {
                    Image(systemName: "person.badge.plus.fill")
                        .font(.system(size: 60))
                        .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6)) // Deep purple
                    
                    Text("Invite a Caree")
                        .font(.title2)
                        .fontWeight(.semibold)
                    
                    Text("Share this link with someone you care for to invite them to join CareComms")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 20)
                }
                
                VStack(spacing: 16) {
                    HStack {
                        Text(invitationLink)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 8)
                            .background(Color.gray.opacity(0.1))
                            .cornerRadius(8)
                            .lineLimit(1)
                        
                        Button(action: {
                            UIPasteboard.general.string = invitationLink
                        }) {
                            Image(systemName: "doc.on.doc")
                                .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6)) // Deep purple
                        }
                    }
                    .padding(.horizontal, 20)
                    
                    ShareLink(item: invitationLink) {
                        HStack {
                            Image(systemName: "square.and.arrow.up")
                            Text("Share Invitation")
                        }
                        .font(.headline)
                        .foregroundColor(.white)
                        .padding(.vertical, 12)
                        .padding(.horizontal, 24)
                        .background(Color(red: 0.4, green: 0.2, blue: 0.6)) // Deep purple
                        .cornerRadius(10)
                    }
                }
                
                Spacer()
            }
            .padding(.top, 20)
            .navigationTitle("Invite Caree")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
        }
    }
}

// MARK: - Preview
#Preview {
    ChatListScreen()
        .environmentObject(NavigationManager())
}