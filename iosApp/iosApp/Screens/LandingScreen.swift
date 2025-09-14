import SwiftUI

struct LandingScreen: View {
    @EnvironmentObject private var navigationManager: NavigationManager
    
    var body: some View {
        ZStack {
            // Light purple background
            Color(red: 0.95, green: 0.92, blue: 0.98)
                .ignoresSafeArea()
            
            VStack(spacing: 40) {
                Spacer()
                
                // Logo and welcome section
                VStack(spacing: 24) {
                    Image(systemName: "heart.text.square.fill")
                        .font(.system(size: 80))
                        .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                    
                    VStack(spacing: 12) {
                        Text("Welcome to CareComms")
                            .font(.largeTitle)
                            .fontWeight(.bold)
                            .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                            .multilineTextAlignment(.center)
                        
                        Text("Connecting carers and care recipients with compassionate communication")
                            .font(.body)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 32)
                    }
                }
                
                Spacer()
                
                // Action buttons
                VStack(spacing: 16) {
                    // Login button
                    Button(action: {
                        navigationManager.navigate(to: .login)
                    }) {
                        HStack {
                            Image(systemName: "person.circle.fill")
                                .font(.title2)
                            Text("Login")
                                .font(.headline)
                        }
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color(red: 0.4, green: 0.2, blue: 0.6))
                        .cornerRadius(12)
                    }
                    
                    // Signup button (Carer only)
                    Button(action: {
                        navigationManager.navigate(to: .carerRegistration)
                    }) {
                        HStack {
                            Image(systemName: "person.badge.plus.fill")
                                .font(.title2)
                            Text("Sign Up as Carer")
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
                    
                    // Info text about caree signup
                    VStack(spacing: 8) {
                        Text("Care recipients (carees) can only join through")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        
                        Text("invitation links from their carers")
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .fontWeight(.medium)
                    }
                    .padding(.top, 8)
                }
                .padding(.horizontal, 24)
                
                Spacer()
            }
        }
        .navigationBarHidden(true)
    }
}

#Preview {
    LandingScreen()
        .environmentObject(NavigationManager())
}