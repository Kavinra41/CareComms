import SwiftUI
import shared

struct ProfileScreen: View {
    @EnvironmentObject private var navigationManager: NavigationManager
    @State private var showingLogoutAlert = false
    @State private var isLoggingOut = false
    
    // Mock user data - in real app this would come from shared ViewModel
    @State private var userName = "Dr. Sarah Johnson"
    @State private var userEmail = "sarah.johnson@carecomms.com"
    @State private var userPhone = "+1 (555) 123-4567"
    @State private var userLocation = "San Francisco, CA"
    @State private var documentsCount = 3
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    // Profile Header
                    VStack(spacing: 16) {
                        // Profile Avatar
                        Circle()
                            .fill(Color(red: 0.8, green: 0.7, blue: 0.9)) // Light purple
                            .frame(width: 100, height: 100)
                            .overlay(
                                Text(String(userName.prefix(1).uppercased()))
                                    .font(.largeTitle)
                                    .fontWeight(.bold)
                                    .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6)) // Deep purple
                            )
                        
                        VStack(spacing: 4) {
                            Text(userName)
                                .font(.title2)
                                .fontWeight(.semibold)
                                .foregroundColor(.primary)
                            
                            Text("Professional Carer")
                                .font(.subheadline)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(.top, 20)
                    
                    // Profile Information
                    VStack(spacing: 16) {
                        ProfileInfoRow(
                            icon: "envelope.fill",
                            title: "Email",
                            value: userEmail
                        )
                        
                        ProfileInfoRow(
                            icon: "phone.fill",
                            title: "Phone",
                            value: userPhone
                        )
                        
                        ProfileInfoRow(
                            icon: "location.fill",
                            title: "Location",
                            value: userLocation
                        )
                        
                        ProfileInfoRow(
                            icon: "doc.text.fill",
                            title: "Documents",
                            value: "\(documentsCount) verified"
                        )
                    }
                    .padding(.horizontal, 20)
                    
                    // Settings Section
                    VStack(spacing: 0) {
                        SettingsRow(
                            icon: "bell.fill",
                            title: "Notifications",
                            showChevron: true
                        ) {
                            // Handle notifications settings
                        }
                        
                        Divider()
                            .padding(.leading, 52)
                        
                        SettingsRow(
                            icon: "lock.fill",
                            title: "Privacy & Security",
                            showChevron: true
                        ) {
                            // Handle privacy settings
                        }
                        
                        Divider()
                            .padding(.leading, 52)
                        
                        SettingsRow(
                            icon: "questionmark.circle.fill",
                            title: "Help & Support",
                            showChevron: true
                        ) {
                            // Handle help
                        }
                        
                        Divider()
                            .padding(.leading, 52)
                        
                        SettingsRow(
                            icon: "info.circle.fill",
                            title: "About",
                            showChevron: true
                        ) {
                            // Handle about
                        }
                    }
                    .background(Color(.systemBackground))
                    .cornerRadius(12)
                    .padding(.horizontal, 20)
                    
                    // Logout Button
                    Button(action: {
                        showingLogoutAlert = true
                    }) {
                        HStack {
                            Image(systemName: "rectangle.portrait.and.arrow.right")
                                .font(.headline)
                            Text("Sign Out")
                                .font(.headline)
                                .fontWeight(.semibold)
                        }
                        .foregroundColor(.red)
                        .padding(.vertical, 16)
                        .frame(maxWidth: .infinity)
                        .background(Color.red.opacity(0.1))
                        .cornerRadius(12)
                    }
                    .padding(.horizontal, 20)
                    .disabled(isLoggingOut)
                    
                    Spacer(minLength: 20)
                }
            }
            .navigationTitle("Profile")
            .navigationBarTitleDisplayMode(.large)
            .alert("Sign Out", isPresented: $showingLogoutAlert) {
                Button("Cancel", role: .cancel) { }
                Button("Sign Out", role: .destructive) {
                    performLogout()
                }
            } message: {
                Text("Are you sure you want to sign out?")
            }
        }
    }
    
    private func performLogout() {
        isLoggingOut = true
        
        // Simulate logout process
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            isLoggingOut = false
            // Navigate back to landing screen
            navigationManager.navigateToRoot()
            navigationManager.navigate(to: .landing)
        }
    }
}

// MARK: - Profile Info Row Component
struct ProfileInfoRow: View {
    let icon: String
    let title: String
    let value: String
    
    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6)) // Deep purple
                .frame(width: 24, height: 24)
            
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.caption)
                    .foregroundColor(.secondary)
                
                Text(value)
                    .font(.subheadline)
                    .foregroundColor(.primary)
            }
            
            Spacer()
        }
        .padding(.vertical, 12)
        .padding(.horizontal, 16)
        .background(Color(.systemBackground))
        .cornerRadius(10)
    }
}

// MARK: - Settings Row Component
struct SettingsRow: View {
    let icon: String
    let title: String
    let showChevron: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 16) {
                Image(systemName: icon)
                    .font(.title3)
                    .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6)) // Deep purple
                    .frame(width: 24, height: 24)
                
                Text(title)
                    .font(.subheadline)
                    .foregroundColor(.primary)
                
                Spacer()
                
                if showChevron {
                    Image(systemName: "chevron.right")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            .padding(.vertical, 16)
            .padding(.horizontal, 16)
        }
        .buttonStyle(PlainButtonStyle())
    }
}

// MARK: - Preview
#Preview {
    ProfileScreen()
        .environmentObject(NavigationManager())
}