import SwiftUI

struct SplashScreen: View {
    @EnvironmentObject private var navigationManager: NavigationManager
    @State private var isActive = false
    @State private var logoScale: CGFloat = 0.8
    @State private var logoOpacity: Double = 0.5
    
    var body: some View {
        ZStack {
            // Deep purple background
            Color(red: 0.4, green: 0.2, blue: 0.6)
                .ignoresSafeArea()
            
            VStack(spacing: 20) {
                // App Logo
                Image(systemName: "heart.text.square.fill")
                    .font(.system(size: 120))
                    .foregroundColor(.white)
                    .scaleEffect(logoScale)
                    .opacity(logoOpacity)
                
                Text("CareComms")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                    .scaleEffect(logoScale)
                    .opacity(logoOpacity)
            }
        }
        .onAppear {
            // Animate logo appearance
            withAnimation(.easeInOut(duration: 1.0)) {
                logoScale = 1.0
                logoOpacity = 1.0
            }
            
            // Navigate to terms screen after 5 seconds maximum
            DispatchQueue.main.asyncAfter(deadline: .now() + 5.0) {
                if !isActive {
                    isActive = true
                    navigationManager.navigate(to: .terms)
                }
            }
        }
        .navigationBarHidden(true)
    }
}

#Preview {
    SplashScreen()
        .environmentObject(NavigationManager())
}