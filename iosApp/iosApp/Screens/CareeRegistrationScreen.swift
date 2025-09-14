import SwiftUI
import shared

struct CareeRegistrationScreen: View {
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject private var viewModel = CareeRegistrationScreenViewModel()
    
    let invitationToken: String
    
    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var healthInfo = ""
    @State private var firstName = ""
    @State private var lastName = ""
    @State private var dateOfBirth = ""
    @State private var address = ""
    @State private var emergencyContact = ""
    @State private var showPassword = false
    @State private var showConfirmPassword = false
    @State private var showAlert = false
    @State private var alertMessage = ""
    @State private var alertTitle = ""
    @State private var showDatePicker = false
    @State private var selectedDate = Date()
    
    var body: some View {
        ZStack {
            // Light purple background
            Color(red: 0.95, green: 0.92, blue: 0.98)
                .ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 24) {
                    // Header with carer information
                    VStack(spacing: 16) {
                        Image(systemName: "heart.circle.fill")
                            .font(.system(size: 60))
                            .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                        
                        VStack(spacing: 8) {
                            Text("Join CareComms")
                                .font(.largeTitle)
                                .fontWeight(.bold)
                                .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                            
                            if let carerInfo = viewModel.carerInfo {
                                Text("You've been invited by \(carerInfo.name)")
                                    .font(.body)
                                    .foregroundColor(.secondary)
                                    .multilineTextAlignment(.center)
                            } else if viewModel.isValidatingInvitation {
                                HStack {
                                    ProgressView()
                                        .scaleEffect(0.8)
                                    Text("Validating invitation...")
                                        .font(.body)
                                        .foregroundColor(.secondary)
                                }
                            } else {
                                Text("Complete your registration to get started")
                                    .font(.body)
                                    .foregroundColor(.secondary)
                                    .multilineTextAlignment(.center)
                            }
                        }
                    }
                    .padding(.top, 20)
                    
                    // Registration form
                    if viewModel.isInvitationValid {
                        VStack(spacing: 20) {
                            // Account Information Section
                            VStack(alignment: .leading, spacing: 16) {
                                Text("Account Information")
                                    .font(.title2)
                                    .fontWeight(.semibold)
                                    .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                                
                                // Email field
                                CareeFormField(
                                    title: "Email",
                                    text: $email,
                                    placeholder: "Enter your email",
                                    keyboardType: .emailAddress,
                                    errorMessage: viewModel.getErrorMessage(for: "email")
                                )
                                
                                // Password field
                                CareeSecureFormField(
                                    title: "Password",
                                    text: $password,
                                    placeholder: "Enter your password",
                                    showPassword: $showPassword,
                                    errorMessage: viewModel.getErrorMessage(for: "password")
                                )
                                
                                // Confirm Password field
                                CareeSecureFormField(
                                    title: "Confirm Password",
                                    text: $confirmPassword,
                                    placeholder: "Confirm your password",
                                    showPassword: $showConfirmPassword,
                                    errorMessage: viewModel.getErrorMessage(for: "confirmPassword")
                                )
                            }
                            
                            // Personal Information Section
                            VStack(alignment: .leading, spacing: 16) {
                                Text("Personal Information")
                                    .font(.title2)
                                    .fontWeight(.semibold)
                                    .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                                
                                // First Name field
                                CareeFormField(
                                    title: "First Name",
                                    text: $firstName,
                                    placeholder: "Enter your first name",
                                    keyboardType: .default,
                                    errorMessage: viewModel.getErrorMessage(for: "firstName")
                                )
                                
                                // Last Name field
                                CareeFormField(
                                    title: "Last Name",
                                    text: $lastName,
                                    placeholder: "Enter your last name",
                                    keyboardType: .default,
                                    errorMessage: viewModel.getErrorMessage(for: "lastName")
                                )
                                
                                // Date of Birth field
                                VStack(alignment: .leading, spacing: 8) {
                                    Text("Date of Birth")
                                        .font(.headline)
                                        .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                                    
                                    Button(action: {
                                        showDatePicker = true
                                    }) {
                                        HStack {
                                            Text(dateOfBirth.isEmpty ? "Select your date of birth" : dateOfBirth)
                                                .foregroundColor(dateOfBirth.isEmpty ? .secondary : .primary)
                                            Spacer()
                                            Image(systemName: "calendar")
                                                .foregroundColor(.secondary)
                                        }
                                        .padding(.horizontal, 16)
                                        .padding(.vertical, 12)
                                        .background(Color.white)
                                        .cornerRadius(12)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 12)
                                                .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                                        )
                                    }
                                    
                                    if let errorMessage = viewModel.getErrorMessage(for: "dateOfBirth") {
                                        Text(errorMessage)
                                            .font(.caption)
                                            .foregroundColor(.red)
                                    }
                                }
                                
                                // Address field (optional)
                                CareeFormField(
                                    title: "Address (Optional)",
                                    text: $address,
                                    placeholder: "Enter your address",
                                    keyboardType: .default,
                                    errorMessage: nil
                                )
                                
                                // Emergency Contact field (optional)
                                CareeFormField(
                                    title: "Emergency Contact (Optional)",
                                    text: $emergencyContact,
                                    placeholder: "Enter emergency contact",
                                    keyboardType: .phonePad,
                                    errorMessage: nil
                                )
                            }
                            
                            // Health Information Section
                            VStack(alignment: .leading, spacing: 16) {
                                Text("Health Information")
                                    .font(.title2)
                                    .fontWeight(.semibold)
                                    .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                                
                                VStack(alignment: .leading, spacing: 8) {
                                    Text("Health Details")
                                        .font(.headline)
                                        .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                                    
                                    Text("Please provide any relevant health information, medical conditions, medications, or care requirements")
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                    
                                    TextEditor(text: $healthInfo)
                                        .frame(minHeight: 100)
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 8)
                                        .background(Color.white)
                                        .cornerRadius(12)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 12)
                                                .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                                        )
                                    
                                    if let errorMessage = viewModel.getErrorMessage(for: "healthInfo") {
                                        Text(errorMessage)
                                            .font(.caption)
                                            .foregroundColor(.red)
                                    }
                                }
                            }
                            
                            // Submit button
                            Button(action: {
                                submitRegistration()
                            }) {
                                HStack {
                                    if viewModel.isLoading {
                                        ProgressView()
                                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                            .scaleEffect(0.8)
                                    } else {
                                        Image(systemName: "checkmark.circle.fill")
                                            .font(.title2)
                                    }
                                    
                                    Text(viewModel.isLoading ? "Creating Account..." : "Complete Registration")
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
                            .disabled(!isFormValid || viewModel.isLoading)
                        }
                        .padding(.horizontal, 24)
                    } else if !viewModel.isValidatingInvitation && !viewModel.isInvitationValid {
                        // Invalid invitation message
                        VStack(spacing: 16) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .font(.system(size: 50))
                                .foregroundColor(.orange)
                            
                            Text("Invalid Invitation")
                                .font(.title2)
                                .fontWeight(.semibold)
                                .foregroundColor(.primary)
                            
                            Text("This invitation link is invalid or has expired. Please contact your carer for a new invitation.")
                                .font(.body)
                                .foregroundColor(.secondary)
                                .multilineTextAlignment(.center)
                                .padding(.horizontal, 24)
                        }
                        .padding(.vertical, 40)
                    }
                    
                    Spacer(minLength: 40)
                }
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: {
                    navigationManager.navigateToRoot()
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
        .sheet(isPresented: $showDatePicker) {
            DatePickerSheet(
                selectedDate: $selectedDate,
                dateOfBirth: $dateOfBirth,
                isPresented: $showDatePicker
            )
        }
        .alert(alertTitle, isPresented: $showAlert) {
            Button("OK") {
                if viewModel.isRegistrationSuccessful {
                    // Navigate to chat screen
                    // This will be implemented when chat screen is available
                    navigationManager.navigateToRoot()
                }
            }
        } message: {
            Text(alertMessage)
        }
        .onAppear {
            viewModel.validateInvitation(token: invitationToken)
        }
        .onReceive(viewModel.$registrationState) { state in
            handleRegistrationState(state)
        }
        .onReceive(viewModel.$errorMessage) { error in
            if let error = error {
                alertTitle = "Registration Error"
                alertMessage = error
                showAlert = true
            }
        }
        .onChange(of: email) { _ in updateViewModel() }
        .onChange(of: password) { _ in updateViewModel() }
        .onChange(of: confirmPassword) { _ in updateViewModel() }
        .onChange(of: healthInfo) { _ in updateViewModel() }
        .onChange(of: firstName) { _ in updateViewModel() }
        .onChange(of: lastName) { _ in updateViewModel() }
        .onChange(of: dateOfBirth) { _ in updateViewModel() }
        .onChange(of: address) { _ in updateViewModel() }
        .onChange(of: emergencyContact) { _ in updateViewModel() }
    }
    
    private var isFormValid: Bool {
        !email.isEmpty && 
        !password.isEmpty && 
        !confirmPassword.isEmpty &&
        !healthInfo.isEmpty &&
        !firstName.isEmpty &&
        !lastName.isEmpty &&
        !dateOfBirth.isEmpty &&
        email.contains("@") &&
        password == confirmPassword &&
        viewModel.isInvitationValid
    }
    
    private func updateViewModel() {
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        viewModel.updateConfirmPassword(confirmPassword)
        viewModel.updateHealthInfo(healthInfo)
        viewModel.updateFirstName(firstName)
        viewModel.updateLastName(lastName)
        viewModel.updateDateOfBirth(dateOfBirth)
        viewModel.updateAddress(address)
        viewModel.updateEmergencyContact(emergencyContact)
    }
    
    private func submitRegistration() {
        guard isFormValid else { return }
        viewModel.registerCaree()
    }
    
    private func handleRegistrationState(_ state: CareeRegistrationState) {
        if state.isRegistrationSuccessful {
            alertTitle = "Registration Successful"
            alertMessage = "Welcome to CareComms! Your account has been created successfully. You can now start communicating with your carer."
            showAlert = true
        }
    }
}

// MARK: - Form Field Components

struct CareeFormField: View {
    let title: String
    @Binding var text: String
    let placeholder: String
    let keyboardType: UIKeyboardType
    let errorMessage: String?
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.headline)
                .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
            
            TextField(placeholder, text: $text)
                .textFieldStyle(CustomTextFieldStyle())
                .keyboardType(keyboardType)
                .autocapitalization(keyboardType == .emailAddress ? .none : .words)
                .disableAutocorrection(keyboardType == .emailAddress)
            
            if let errorMessage = errorMessage {
                Text(errorMessage)
                    .font(.caption)
                    .foregroundColor(.red)
            }
        }
    }
}

struct CareeSecureFormField: View {
    let title: String
    @Binding var text: String
    let placeholder: String
    @Binding var showPassword: Bool
    let errorMessage: String?
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.headline)
                .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
            
            HStack {
                if showPassword {
                    TextField(placeholder, text: $text)
                } else {
                    SecureField(placeholder, text: $text)
                }
                
                Button(action: {
                    showPassword.toggle()
                }) {
                    Image(systemName: showPassword ? "eye.slash.fill" : "eye.fill")
                        .foregroundColor(.secondary)
                }
            }
            .textFieldStyle(CustomTextFieldStyle())
            
            if let errorMessage = errorMessage {
                Text(errorMessage)
                    .font(.caption)
                    .foregroundColor(.red)
            }
        }
    }
}

struct DatePickerSheet: View {
    @Binding var selectedDate: Date
    @Binding var dateOfBirth: String
    @Binding var isPresented: Bool
    
    private let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        return formatter
    }()
    
    var body: some View {
        NavigationView {
            VStack {
                DatePicker(
                    "Date of Birth",
                    selection: $selectedDate,
                    in: ...Date(),
                    displayedComponents: .date
                )
                .datePickerStyle(WheelDatePickerStyle())
                .padding()
                
                Spacer()
            }
            .navigationTitle("Date of Birth")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        isPresented = false
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dateOfBirth = dateFormatter.string(from: selectedDate)
                        isPresented = false
                    }
                    .fontWeight(.semibold)
                }
            }
        }
    }
}

// MARK: - View Model

class CareeRegistrationScreenViewModel: ObservableObject {
    @Published var registrationState = CareeRegistrationState()
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var validationErrors: [String: String] = [:]
    @Published var carerInfo: CarerInfo?
    @Published var isInvitationValid = false
    @Published var isValidatingInvitation = false
    
    private let viewModel: CareeRegistrationViewModel
    
    init() {
        self.viewModel = KoinHelper.createCareeRegistrationViewModel()
        
        // Observe state changes
        Task {
            for await state in viewModel.state {
                await MainActor.run {
                    self.registrationState = state
                    self.isLoading = state.isLoading
                    self.carerInfo = state.carerInfo
                    self.isInvitationValid = state.isInvitationValid
                    self.isValidatingInvitation = state.isValidatingInvitation
                    self.errorMessage = state.errorMessage
                    
                    // Convert validation errors to dictionary
                    self.validationErrors = self.convertValidationErrors(state.validationErrors)
                }
            }
        }
    }
    
    var isRegistrationSuccessful: Bool {
        registrationState.isRegistrationSuccessful
    }
    
    func getErrorMessage(for field: String) -> String? {
        validationErrors[field]
    }
    
    func validateInvitation(token: String) {
        viewModel.handleIntent(intent: CareeRegistrationIntent.ValidateInvitation(token: token))
    }
    
    func updateEmail(_ email: String) {
        viewModel.handleIntent(intent: CareeRegistrationIntent.UpdateEmail(email: email))
    }
    
    func updatePassword(_ password: String) {
        viewModel.handleIntent(intent: CareeRegistrationIntent.UpdatePassword(password: password))
    }
    
    func updateConfirmPassword(_ confirmPassword: String) {
        viewModel.handleIntent(intent: CareeRegistrationIntent.UpdateConfirmPassword(confirmPassword: confirmPassword))
    }
    
    func updateHealthInfo(_ healthInfo: String) {
        viewModel.handleIntent(intent: CareeRegistrationIntent.UpdateHealthInfo(healthInfo: healthInfo))
    }
    
    func updateFirstName(_ firstName: String) {
        viewModel.handleIntent(intent: CareeRegistrationIntent.UpdateFirstName(firstName: firstName))
    }
    
    func updateLastName(_ lastName: String) {
        viewModel.handleIntent(intent: CareeRegistrationIntent.UpdateLastName(lastName: lastName))
    }
    
    func updateDateOfBirth(_ dateOfBirth: String) {
        viewModel.handleIntent(intent: CareeRegistrationIntent.UpdateDateOfBirth(dateOfBirth: dateOfBirth))
    }
    
    func updateAddress(_ address: String) {
        viewModel.handleIntent(intent: CareeRegistrationIntent.UpdateAddress(address: address))
    }
    
    func updateEmergencyContact(_ emergencyContact: String) {
        viewModel.handleIntent(intent: CareeRegistrationIntent.UpdateEmergencyContact(emergencyContact: emergencyContact))
    }
    
    func registerCaree() {
        viewModel.handleIntent(intent: CareeRegistrationIntent.RegisterCaree())
    }
    
    private func convertValidationErrors(_ errors: [CareeValidationError]) -> [String: String] {
        var result: [String: String] = [:]
        
        for error in errors {
            switch error {
            case is CareeValidationError.InvalidEmail:
                result["email"] = "Please enter a valid email address"
            case is CareeValidationError.WeakPassword:
                result["password"] = "Password must be at least 6 characters"
            case is CareeValidationError.EmptyHealthInfo:
                result["healthInfo"] = "Please provide health information"
            case is CareeValidationError.EmptyFirstName:
                result["firstName"] = "First name is required"
            case is CareeValidationError.EmptyLastName:
                result["lastName"] = "Last name is required"
            case is CareeValidationError.InvalidDateOfBirth:
                result["dateOfBirth"] = "Please select a valid date of birth"
            case let customError as CareeValidationError.CustomError:
                result["general"] = customError.message
            default:
                break
            }
        }
        
        return result
    }
}

// MARK: - Custom Text Field Style

struct CustomTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(Color.white)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color.gray.opacity(0.3), lineWidth: 1)
            )
    }
}

#Preview {
    NavigationStack {
        CareeRegistrationScreen(invitationToken: "sample-token")
            .environmentObject(NavigationManager())
    }
}