import SwiftUI

struct TermsScreen: View {
    @EnvironmentObject private var navigationManager: NavigationManager
    @State private var hasScrolledToBottom = false
    @State private var scrollViewContentOffset: CGFloat = 0
    
    var body: some View {
        ZStack {
            // Light purple background
            Color(red: 0.95, green: 0.92, blue: 0.98)
                .ignoresSafeArea()
            
            VStack(spacing: 0) {
                // Header
                VStack(spacing: 16) {
                    Text("Terms and Conditions")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                        .padding(.top, 20)
                    
                    Text("Please read and accept our terms to continue")
                        .font(.body)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 20)
                
                // Scrollable Terms Content
                ScrollViewReader { proxy in
                    ScrollView {
                        VStack(alignment: .leading, spacing: 16) {
                            termsContent
                        }
                        .padding(.horizontal, 24)
                        .padding(.bottom, 100) // Extra space for button
                    }
                    .background(
                        GeometryReader { geometry in
                            Color.clear
                                .preference(key: ScrollOffsetPreferenceKey.self, value: geometry.frame(in: .named("scroll")).minY)
                        }
                    )
                    .coordinateSpace(name: "scroll")
                    .onPreferenceChange(ScrollOffsetPreferenceKey.self) { value in
                        scrollViewContentOffset = value
                        // Check if scrolled near bottom (within 100 points)
                        hasScrolledToBottom = value <= -200
                    }
                }
                
                Spacer()
            }
            
            // Bottom buttons overlay
            VStack {
                Spacer()
                
                VStack(spacing: 12) {
                    // Accept button
                    Button(action: {
                        navigationManager.navigate(to: .landing)
                    }) {
                        Text("Accept and Continue")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .background(
                                Color(red: 0.4, green: 0.2, blue: 0.6)
                            )
                            .cornerRadius(12)
                    }
                    .opacity(hasScrolledToBottom ? 1.0 : 0.6)
                    .disabled(!hasScrolledToBottom)
                    
                    // Decline button
                    Button(action: {
                        // In a real app, this would exit the app
                        // For now, we'll just stay on this screen
                    }) {
                        Text("Decline")
                            .font(.headline)
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
                .padding(.bottom, 34)
                .background(
                    LinearGradient(
                        gradient: Gradient(colors: [
                            Color(red: 0.95, green: 0.92, blue: 0.98).opacity(0),
                            Color(red: 0.95, green: 0.92, blue: 0.98)
                        ]),
                        startPoint: .top,
                        endPoint: .bottom
                    )
                    .frame(height: 120)
                )
            }
        }
        .navigationBarHidden(true)
    }
    
    private var termsContent: some View {
        VStack(alignment: .leading, spacing: 20) {
            Group {
                Text("1. Acceptance of Terms")
                    .font(.headline)
                    .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                
                Text("By using CareComms, you agree to be bound by these Terms and Conditions. If you do not agree to these terms, please do not use our service.")
                    .font(.body)
                    .foregroundColor(.primary)
                
                Text("2. Description of Service")
                    .font(.headline)
                    .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                
                Text("CareComms is a mobile application designed to facilitate communication between professional carers and care recipients. The service includes messaging, data analytics, and care coordination features.")
                    .font(.body)
                    .foregroundColor(.primary)
                
                Text("3. User Responsibilities")
                    .font(.headline)
                    .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                
                Text("Users are responsible for maintaining the confidentiality of their account information and for all activities that occur under their account. You agree to notify us immediately of any unauthorized use of your account.")
                    .font(.body)
                    .foregroundColor(.primary)
            }
            
            Group {
                Text("4. Privacy and Data Protection")
                    .font(.headline)
                    .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                
                Text("We are committed to protecting your privacy and handling your personal data in accordance with applicable data protection laws. Health information is treated with the highest level of security and confidentiality.")
                    .font(.body)
                    .foregroundColor(.primary)
                
                Text("5. Professional Use")
                    .font(.headline)
                    .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                
                Text("This application is intended for use by professional carers and their care recipients. Users must ensure they have appropriate qualifications and permissions to provide or receive care services.")
                    .font(.body)
                    .foregroundColor(.primary)
                
                Text("6. Limitation of Liability")
                    .font(.headline)
                    .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                
                Text("CareComms is provided 'as is' without warranties of any kind. We shall not be liable for any indirect, incidental, special, or consequential damages arising from the use of our service.")
                    .font(.body)
                    .foregroundColor(.primary)
            }
            
            Group {
                Text("7. Modifications to Terms")
                    .font(.headline)
                    .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                
                Text("We reserve the right to modify these terms at any time. Users will be notified of significant changes and continued use of the service constitutes acceptance of the modified terms.")
                    .font(.body)
                    .foregroundColor(.primary)
                
                Text("8. Contact Information")
                    .font(.headline)
                    .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                
                Text("If you have any questions about these Terms and Conditions, please contact us at support@carecomms.com.")
                    .font(.body)
                    .foregroundColor(.primary)
                
                Text("Last updated: January 2024")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding(.top, 20)
            }
        }
    }
}

struct ScrollOffsetPreferenceKey: PreferenceKey {
    static var defaultValue: CGFloat = 0
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
        value = nextValue()
    }
}

#Preview {
    TermsScreen()
        .environmentObject(NavigationManager())
}