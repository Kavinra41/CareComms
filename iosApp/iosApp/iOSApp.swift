import SwiftUI
import shared

@main
struct iOSApp: App {
    
    init() {
        // Initialize Koin DI
        KoinHelperKt.doInitKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}