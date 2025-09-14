import SwiftUI

class NavigationManager: ObservableObject {
    @Published var currentScreen: String = "splash"
    
    func navigateTo(_ screen: String) {
        currentScreen = screen
    }
    
    func goBack() {
        // Implementation for going back
    }
}