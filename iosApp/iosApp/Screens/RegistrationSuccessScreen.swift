import SwiftUI

struct RegistrationSuccessScreen: View {
    @EnvironmentObject private var navigationManager: NavigationManager
    
    var body: some View {
        ZStack {
            // Light purple background
            Color(red: 0.95, green: 0.92, blue: 0.98)
                .ignoresSafeArea()
            
            VStack(spacing: 40) {
                Spacer()
                
                // Success icon and message
                VStack(spacing: 24) {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 100))
                        .foregroundColor(.green)
                    
                    VStack(spacing: 16) {
                        Text("Registration Successful!")
                            .font(.largeTitle)
                            .fontWeight(.bold)
                            .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                            .multilineTextAlignment(.center)
                        
                        Text("Welcome to CareComms! Your carer account has been created successfully.")
                            .font(.body)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 32)
                        
                        Text("You can now start connecting with care recipients and managing their care.")
                            .font(.body)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 32)
                    }
                }
                
                Spacer()
                
                // Action buttons
                VStack(spacing: 16) {
                    // Continue to Chat List button
                    Button(action: {
                        navigationManager.navigateToRoot()
                        navigationManager.navigate(to: .carerTabs)
                    }) {
                        HStack {
                            Image(systemName: "message.circle.fill")
                                .font(.title2)
                            Text("Start Caring")
                                .font(.headline)
                        }
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color(red: 0.4, green: 0.2, blue: 0.6))
                        .cornerRadius(12)
                    }
                    
                    // Back to Home button
                    Button(action: {
                        navigationManager.navigateToRoot()
                        navigationManager.navigate(to: .landing)
                    }) {
                        HStack {
                            Image(systemName: "house.circle.fill")
                                .font(.title2)
                            Text("Back to Home")
                                .font(.headline)
                        }
                        .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color.white)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color(red: 0.4, green: 0.2, blue: 0.6), lineWidth: 2)
                        )
                        .cornerRadius(12)
                    }
                }
                .padding(.horizontal, 24)
                
                Spacer()
            }
        }
        .navigationBarHidden(true)
    }
}

#Preview {
    RegistrationSuccessScreen()
        .environmentObject(NavigationManager())
}