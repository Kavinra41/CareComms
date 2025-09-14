import SwiftUI
import shared

struct ContentView: View {
    @StateObject private var navigationManager = NavigationManager()
    
    var body: some View {
        NavigationStack(path: $navigationManager.path) {
            SplashScreen()
                .navigationDestination(for: AppDestination.self) { destination in
                    switch destination {
                    case .splash:
                        SplashScreen()
                    case .terms:
                        TermsScreen()
                    case .landing:
                        LandingScreen()
                    case .login:
                        LoginScreen()
                    case .carerRegistration:
                        CarerRegistrationScreen()
                    case .careeRegistration(let invitationToken):
                        CareeRegistrationScreen(invitationToken: invitationToken)
                    case .registrationSuccess:
                        RegistrationSuccessScreen()
                    case .carerTabs:
                        CarerTabView()
                    case .chatList:
                        ChatListScreen()
                    case .chat(let chatId):
                        ChatScreen(chatId: chatId)
                    }
                }
        }
        .environmentObject(navigationManager)
        .onOpenURL { url in
            handleDeepLink(url)
        }
    }
    
    private func handleDeepLink(_ url: URL) {
        guard url.scheme == "carecomms" else { return }
        
        if url.host == "invite" {
            // Extract invitation token from URL
            let components = URLComponents(url: url, resolvingAgainstBaseURL: false)
            if let token = components?.queryItems?.first(where: { $0.name == "token" })?.value {
                navigationManager.navigate(to: .careeRegistration(invitationToken: token))
            }
        }
    }
}

enum AppDestination: Hashable {
    case splash
    case terms
    case landing
    case login
    case carerRegistration
    case careeRegistration(invitationToken: String)
    case registrationSuccess
    case carerTabs
    case chatList
    case chat(chatId: String)
}

class NavigationManager: ObservableObject {
    @Published var path = NavigationPath()
    
    func navigate(to destination: AppDestination) {
        path.append(destination)
    }
    
    func navigateBack() {
        path.removeLast()
    }
    
    func navigateToRoot() {
        path.removeLast(path.count)
    }
}

#Preview {
    ContentView()
}