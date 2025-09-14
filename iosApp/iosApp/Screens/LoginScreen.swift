import SwiftUI
import shared

struct LoginScreen: View {
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject private var viewModel = LoginViewModel()
    
    @State private var email = ""
    @State private var password = ""
    @State private var showPassword = false
    @State private var isLoading = false
    @State private var showAlert = false
    @State private var alertMessage = ""
    
    var body: some View {
        ZStack {
            // Light purple background
            Color(red: 0.95, green: 0.92, blue: 0.98)
                .ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 32) {
                    // Header
                    VStack(spacing: 16) {
                        Image(systemName: "person.circle.fill")
                            .font(.system(size: 60))
                            .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                        
                        VStack(spacing: 8) {
                            Text("Welcome Back")
                                .font(.largeTitle)
                                .fontWeight(.bold)
                                .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                            
                            Text("Sign in to your account")
                                .font(.body)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(.top, 40)
                    
                    // Login form
                    VStack(spacing: 20) {
                        // Email field
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Email")
                                .font(.headline)
                                .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                            
                            TextField("Enter your email", text: $email)
                                .textFieldStyle(CustomTextFieldStyle())
                                .keyboardType(.emailAddress)
                                .autocapitalization(.none)
                                .disableAutocorrection(true)
                        }
                        
                        // Password field
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Password")
                                .font(.headline)
                                .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                            
                            HStack {
                                if showPassword {
                                    TextField("Enter your password", text: $password)
                                } else {
                                    SecureField("Enter your password", text: $password)
                                }
                                
                                Button(action: {
                                    showPassword.toggle()
                                }) {
                                    Image(systemName: showPassword ? "eye.slash.fill" : "eye.fill")
                                        .foregroundColor(.secondary)
                                }
                            }
                            .textFieldStyle(CustomTextFieldStyle())
                        }
                        
                        // Login button
                        Button(action: {
                            loginUser()
                        }) {
                            HStack {
                                if isLoading {
                                    ProgressView()
                                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                        .scaleEffect(0.8)
                                } else {
                                    Image(systemName: "arrow.right.circle.fill")
                                        .font(.title2)
                                }
                                
                                Text(isLoading ? "Signing In..." : "Sign In")
                                    .font(.headline)
                            }
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .background(
                                Color(red: 0.4, green: 0.2, blue: 0.6)
                                    .opacity(isFormValid ? 1.0 : 0.6)
                            )
                            .cornerRadius(12)
                        }
                        .disabled(!isFormValid || isLoading)
                        
                        // Forgot password link
                        Button(action: {
                            // Handle forgot password
                            // For now, show alert
                            alertMessage = "Forgot password functionality will be implemented in a future update."
                            showAlert = true
                        }) {
                            Text("Forgot Password?")
                                .font(.body)
                                .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                        }
                        .padding(.top, 8)
                    }
                    .padding(.horizontal, 24)
                    
                    Spacer(minLength: 40)
                }
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: {
                    navigationManager.navigateBack()
                }) {
                    HStack(spacing: 4) {
                        Image(systemName: "chevron.left")
                            .font(.body.weight(.medium))
                        Text("Back")
                            .font(.body)
                    }
                    .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                }
            }
        }
        .alert("Login Status", isPresented: $showAlert) {
            Button("OK") { }
        } message: {
            Text(alertMessage)
        }
        .onReceive(viewModel.$authState) { state in
            handleAuthState(state)
        }
    }
    
    private var isFormValid: Bool {
        !email.isEmpty && !password.isEmpty && email.contains("@")
    }
    
    private func loginUser() {
        guard isFormValid else { return }
        
        isLoading = true
        viewModel.signIn(email: email, password: password)
    }
    
    private func handleAuthState(_ state: AuthState) {
        isLoading = false
        
        switch state {
        case is AuthState.Loading:
            isLoading = true
        case let successState as AuthState.Success:
            // Navigate to carer tabs for carers
            // In a real app, you'd check the user role here
            navigationManager.navigate(to: .carerTabs)
        case let errorState as AuthState.Error:
            alertMessage = errorState.message
            showAlert = true
        default:
            break
        }
    }
}

struct CustomTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding(.horizontal, 16)
            .padding(.vertical, 16)
            .background(Color.white)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color.gray.opacity(0.3), lineWidth: 1)
            )
    }
}

class LoginViewModel: ObservableObject {
    @Published var authState: AuthState = AuthState.Idle()
    
    private let authUseCase: AuthUseCase
    
    init() {
        // Get AuthUseCase from Koin DI
        self.authUseCase = KoinHelper.getAuthUseCase()
    }
    
    func signIn(email: String, password: String) {
        authState = AuthState.Loading()
        
        Task {
            do {
                let result = try await authUseCase.signIn(email: email, password: password)
                
                await MainActor.run {
                    if result.isSuccess {
                        authState = AuthState.Success(user: result.getOrNull()!)
                    } else {
                        let error = result.exceptionOrNull()
                        authState = AuthState.Error(message: error?.localizedDescription ?? "Login failed")
                    }
                }
            } catch {
                await MainActor.run {
                    authState = AuthState.Error(message: error.localizedDescription)
                }
            }
        }
    }
}

#Preview {
    NavigationStack {
        LoginScreen()
            .environmentObject(NavigationManager())
    }
}