import SwiftUI
import shared

struct CarerTabView: View {
    @EnvironmentObject private var navigationManager: NavigationManager
    @State private var selectedTab: CarerTab = .chatList
    let carerId: String
    
    var body: some View {
        TabView(selection: $selectedTab) {
            // Chat List Tab
            ChatListScreen()
                .tabItem {
                    Image(systemName: selectedTab == .chatList ? "message.fill" : "message")
                    Text("Chats")
                }
                .tag(CarerTab.chatList)
            
            // Dashboard Tab
            DashboardScreen(carerId: carerId)
                .tabItem {
                    Image(systemName: selectedTab == .dashboard ? "chart.bar.fill" : "chart.bar")
                    Text("Dashboard")
                }
                .tag(CarerTab.dashboard)
            
            // Details Tree Tab
            DetailsTreeScreen()
                .tabItem {
                    Image(systemName: selectedTab == .detailsTree ? "tree.fill" : "tree")
                    Text("Details")
                }
                .tag(CarerTab.detailsTree)
            
            // Profile Tab
            ProfileScreen()
                .tabItem {
                    Image(systemName: selectedTab == .profile ? "person.fill" : "person")
                    Text("Profile")
                }
                .tag(CarerTab.profile)
        }
        .accentColor(Color(red: 0.4, green: 0.2, blue: 0.6)) // Deep purple
        .onAppear {
            // Customize tab bar appearance
            setupTabBarAppearance()
        }
    }
    
    private func setupTabBarAppearance() {
        let appearance = UITabBarAppearance()
        appearance.configureWithOpaqueBackground()
        appearance.backgroundColor = UIColor.systemBackground
        
        // Selected item color
        appearance.stackedLayoutAppearance.selected.iconColor = UIColor(red: 0.4, green: 0.2, blue: 0.6, alpha: 1.0)
        appearance.stackedLayoutAppearance.selected.titleTextAttributes = [
            .foregroundColor: UIColor(red: 0.4, green: 0.2, blue: 0.6, alpha: 1.0)
        ]
        
        // Normal item color
        appearance.stackedLayoutAppearance.normal.iconColor = UIColor.systemGray
        appearance.stackedLayoutAppearance.normal.titleTextAttributes = [
            .foregroundColor: UIColor.systemGray
        ]
        
        UITabBar.appearance().standardAppearance = appearance
        UITabBar.appearance().scrollEdgeAppearance = appearance
    }
}

enum CarerTab: String, CaseIterable {
    case chatList = "chatList"
    case dashboard = "dashboard"
    case detailsTree = "detailsTree"
    case profile = "profile"
    
    var title: String {
        switch self {
        case .chatList:
            return "Chats"
        case .dashboard:
            return "Dashboard"
        case .detailsTree:
            return "Details"
        case .profile:
            return "Profile"
        }
    }
    
    var iconName: String {
        switch self {
        case .chatList:
            return "message"
        case .dashboard:
            return "chart.bar"
        case .detailsTree:
            return "tree"
        case .profile:
            return "person"
        }
    }
    
    var selectedIconName: String {
        switch self {
        case .chatList:
            return "message.fill"
        case .dashboard:
            return "chart.bar.fill"
        case .detailsTree:
            return "tree.fill"
        case .profile:
            return "person.fill"
        }
    }
}

// MARK: - Preview
#Preview {
    CarerTabView(carerId: "test-carer-id")
        .environmentObject(NavigationManager())
}