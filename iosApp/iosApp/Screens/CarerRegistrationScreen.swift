import SwiftUI
import shared
import UniformTypeIdentifiers

struct CarerRegistrationScreen: View {
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject private var viewModel = CarerRegistrationScreenViewModel()
    
    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var age = ""
    @State private var phoneNumber = ""
    @State private var location = ""
    @State private var showPassword = false
    @State private var showConfirmPassword = false
    @State private var showDocumentPicker = false
    @State private var showAlert = false
    @State private var alertMessage = ""
    @State private var alertTitle = ""
    @State private var selectedDocumentType: DocumentType = .professionalCertificate
    @State private var showDocumentTypeSelector = false
    
    var body: some View {
        ZStack {
            // Light purple background
            Color(red: 0.95, green: 0.92, blue: 0.98)
                .ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 24) {
                    // Header
                    VStack(spacing: 16) {
                        Image(systemName: "person.badge.plus.fill")
                            .font(.system(size: 60))
                            .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                        
                        VStack(spacing: 8) {
                            Text("Carer Registration")
                                .font(.largeTitle)
                                .fontWeight(.bold)
                                .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                            
                            Text("Join our community of professional carers")
                                .font(.body)
                                .foregroundColor(.secondary)
                                .multilineTextAlignment(.center)
                        }
                    }
                    .padding(.top, 20)
                    
                    // Registration form
                    VStack(spacing: 20) {
                        // Email field
                        FormField(
                            title: "Email",
                            text: $email,
                            placeholder: "Enter your email",
                            keyboardType: .emailAddress,
                            errorMessage: viewModel.getErrorMessage(for: "email")
                        )
                        
                        // Password field
                        SecureFormField(
                            title: "Password",
                            text: $password,
                            placeholder: "Enter your password",
                            showPassword: $showPassword,
                            errorMessage: viewModel.getErrorMessage(for: "password")
                        )
                        
                        // Confirm Password field
                        SecureFormField(
                            title: "Confirm Password",
                            text: $confirmPassword,
                            placeholder: "Confirm your password",
                            showPassword: $showConfirmPassword,
                            errorMessage: viewModel.getErrorMessage(for: "confirmPassword")
                        )
                        
                        // Age field
                        FormField(
                            title: "Age",
                            text: $age,
                            placeholder: "Enter your age",
                            keyboardType: .numberPad,
                            errorMessage: viewModel.getErrorMessage(for: "age")
                        )
                        
                        // Phone Number field
                        FormField(
                            title: "Phone Number",
                            text: $phoneNumber,
                            placeholder: "Enter your phone number",
                            keyboardType: .phonePad,
                            errorMessage: viewModel.getErrorMessage(for: "phoneNumber")
                        )
                        
                        // Location field
                        FormField(
                            title: "Location",
                            text: $location,
                            placeholder: "Enter your location",
                            keyboardType: .default,
                            errorMessage: viewModel.getErrorMessage(for: "location")
                        )
                        
                        // Document upload section
                        DocumentUploadSection(
                            uploadedDocuments: viewModel.uploadedDocuments,
                            onAddDocument: {
                                showDocumentTypeSelector = true
                            },
                            onRemoveDocument: { documentId in
                                viewModel.removeDocument(documentId: documentId)
                            },
                            errorMessage: viewModel.getErrorMessage(for: "documents")
                        )
                        
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
                                
                                Text(viewModel.isLoading ? "Creating Account..." : "Create Account")
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
        .alert(alertTitle, isPresented: $showAlert) {
            Button("OK") {
                if viewModel.isRegistrationSuccessful {
                    navigationManager.navigate(to: .registrationSuccess)
                }
            }
        } message: {
            Text(alertMessage)
        }
        .sheet(isPresented: $showDocumentTypeSelector) {
            DocumentTypeSelector(
                selectedType: $selectedDocumentType,
                onConfirm: {
                    showDocumentTypeSelector = false
                    showDocumentPicker = true
                }
            )
        }
        .sheet(isPresented: $showDocumentPicker) {
            DocumentPicker(documentType: selectedDocumentType) { fileName in
                viewModel.uploadDocument(fileName: fileName, documentType: selectedDocumentType)
            }
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
        .onReceive(viewModel.$successMessage) { success in
            if let success = success {
                alertTitle = "Success"
                alertMessage = success
                showAlert = true
            }
        }
    }
    
    private var isFormValid: Bool {
        !email.isEmpty && 
        !password.isEmpty && 
        !confirmPassword.isEmpty &&
        !age.isEmpty &&
        !phoneNumber.isEmpty &&
        !location.isEmpty &&
        email.contains("@") &&
        password == confirmPassword &&
        !viewModel.uploadedDocuments.isEmpty
    }
    
    private func submitRegistration() {
        guard isFormValid else { return }
        
        viewModel.submitRegistration(
            email: email,
            password: password,
            confirmPassword: confirmPassword,
            age: age,
            phoneNumber: phoneNumber,
            location: location
        )
    }
    
    private func handleRegistrationState(_ state: CarerRegistrationState) {
        if state.isRegistrationSuccessful {
            alertTitle = "Registration Successful"
            alertMessage = "Your carer account has been created successfully! You can now start connecting with care recipients."
            showAlert = true
        }
    }
}

// MARK: - Form Field Components

struct FormField: View {
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

struct SecureFormField: View {
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

// MARK: - Document Upload Components

struct DocumentUploadSection: View {
    let uploadedDocuments: [DocumentUpload]
    let onAddDocument: () -> Void
    let onRemoveDocument: (String) -> Void
    let errorMessage: String?
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Professional Documents")
                .font(.headline)
                .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
            
            Text("Upload your professional certificates, ID, and other required documents")
                .font(.caption)
                .foregroundColor(.secondary)
            
            // Add document button
            Button(action: onAddDocument) {
                HStack {
                    Image(systemName: "plus.circle.fill")
                        .font(.title2)
                    Text("Add Document")
                        .font(.headline)
                }
                .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                .frame(maxWidth: .infinity)
                .frame(height: 48)
                .background(Color.white)
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(Color(red: 0.4, green: 0.2, blue: 0.6), lineWidth: 2)
                        .strokeStyle(StrokeStyle(lineWidth: 2, dash: [5]))
                )
                .cornerRadius(12)
            }
            
            // Uploaded documents list
            if !uploadedDocuments.isEmpty {
                VStack(spacing: 8) {
                    ForEach(uploadedDocuments, id: \.id) { document in
                        DocumentRow(
                            document: document,
                            onRemove: { onRemoveDocument(document.id) }
                        )
                    }
                }
            }
            
            if let errorMessage = errorMessage {
                Text(errorMessage)
                    .font(.caption)
                    .foregroundColor(.red)
            }
        }
    }
}

struct DocumentRow: View {
    let document: DocumentUpload
    let onRemove: () -> Void
    
    var body: some View {
        HStack {
            Image(systemName: documentIcon(for: document.fileType))
                .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
            
            VStack(alignment: .leading, spacing: 2) {
                Text(document.fileName)
                    .font(.body)
                    .foregroundColor(.primary)
                
                Text(documentTypeDisplayName(document.fileType))
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Button(action: onRemove) {
                Image(systemName: "trash.circle.fill")
                    .foregroundColor(.red)
                    .font(.title2)
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(Color.white)
        .cornerRadius(8)
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(Color.gray.opacity(0.3), lineWidth: 1)
        )
    }
    
    private func documentIcon(for type: DocumentType) -> String {
        switch type {
        case .professionalCertificate:
            return "doc.badge.plus"
        case .identityDocument:
            return "person.text.rectangle"
        case .backgroundCheck:
            return "checkmark.shield"
        case .referenceLetter:
            return "envelope.badge"
        case .other:
            return "doc"
        }
    }
    
    private func documentTypeDisplayName(_ type: DocumentType) -> String {
        switch type {
        case .professionalCertificate:
            return "Professional Certificate"
        case .identityDocument:
            return "Identity Document"
        case .backgroundCheck:
            return "Background Check"
        case .referenceLetter:
            return "Reference Letter"
        case .other:
            return "Other Document"
        }
    }
}

struct DocumentTypeSelector: View {
    @Binding var selectedType: DocumentType
    let onConfirm: () -> Void
    @Environment(\.dismiss) private var dismiss
    
    let documentTypes: [DocumentType] = [
        .professionalCertificate,
        .identityDocument,
        .backgroundCheck,
        .referenceLetter,
        .other
    ]
    
    var body: some View {
        NavigationView {
            List {
                ForEach(documentTypes, id: \.self) { type in
                    Button(action: {
                        selectedType = type
                    }) {
                        HStack {
                            Text(documentTypeDisplayName(type))
                                .foregroundColor(.primary)
                            
                            Spacer()
                            
                            if selectedType == type {
                                Image(systemName: "checkmark.circle.fill")
                                    .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                            }
                        }
                    }
                }
            }
            .navigationTitle("Document Type")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Select") {
                        onConfirm()
                    }
                    .fontWeight(.semibold)
                }
            }
        }
    }
    
    private func documentTypeDisplayName(_ type: DocumentType) -> String {
        switch type {
        case .professionalCertificate:
            return "Professional Certificate"
        case .identityDocument:
            return "Identity Document"
        case .backgroundCheck:
            return "Background Check"
        case .referenceLetter:
            return "Reference Letter"
        case .other:
            return "Other Document"
        }
    }
}

struct DocumentPicker: UIViewControllerRepresentable {
    let documentType: DocumentType
    let onDocumentSelected: (String) -> Void
    
    func makeUIViewController(context: Context) -> UIDocumentPickerViewController {
        let picker = UIDocumentPickerViewController(forOpeningContentTypes: [
            UTType.pdf,
            UTType.image,
            UTType.plainText,
            UTType.rtf
        ])
        picker.delegate = context.coordinator
        picker.allowsMultipleSelection = false
        return picker
    }
    
    func updateUIViewController(_ uiViewController: UIDocumentPickerViewController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, UIDocumentPickerDelegate {
        let parent: DocumentPicker
        
        init(_ parent: DocumentPicker) {
            self.parent = parent
        }
        
        func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
            guard let url = urls.first else { return }
            let fileName = url.lastPathComponent
            parent.onDocumentSelected(fileName)
        }
    }
}

// MARK: - View Model

class CarerRegistrationScreenViewModel: ObservableObject {
    @Published var registrationState = CarerRegistrationState()
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var successMessage: String?
    @Published var uploadedDocuments: [DocumentUpload] = []
    @Published var validationErrors: [String: String] = [:]
    
    private let viewModel: CarerRegistrationViewModel
    
    init() {
        self.viewModel = KoinHelper.createCarerRegistrationViewModel()
        
        // Observe state changes
        Task {
            for await state in viewModel.state {
                await MainActor.run {
                    self.registrationState = state
                    self.isLoading = state.isLoading
                    self.uploadedDocuments = state.uploadedDocuments
                    self.validationErrors = state.validationErrors
                    self.errorMessage = state.registrationError
                }
            }
        }
        
        // Observe effects
        Task {
            for await effect in viewModel.effects {
                await MainActor.run {
                    switch effect {
                    case let showError as CarerRegistrationEffect.ShowError:
                        self.errorMessage = showError.message
                    case let showSuccess as CarerRegistrationEffect.ShowSuccess:
                        self.successMessage = showSuccess.message
                    default:
                        break
                    }
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
    
    func submitRegistration(
        email: String,
        password: String,
        confirmPassword: String,
        age: String,
        phoneNumber: String,
        location: String
    ) {
        viewModel.handleAction(action: CarerRegistrationAction.UpdateEmail(email: email))
        viewModel.handleAction(action: CarerRegistrationAction.UpdatePassword(password: password))
        viewModel.handleAction(action: CarerRegistrationAction.UpdateConfirmPassword(confirmPassword: confirmPassword))
        viewModel.handleAction(action: CarerRegistrationAction.UpdateAge(age: age))
        viewModel.handleAction(action: CarerRegistrationAction.UpdatePhoneNumber(phoneNumber: phoneNumber))
        viewModel.handleAction(action: CarerRegistrationAction.UpdateLocation(location: location))
        viewModel.handleAction(action: CarerRegistrationAction.SubmitRegistration())
    }
    
    func uploadDocument(fileName: String, documentType: DocumentType) {
        viewModel.uploadDocument(fileName: fileName, documentType: documentType)
    }
    
    func removeDocument(documentId: String) {
        viewModel.handleAction(action: CarerRegistrationAction.RemoveDocument(documentId: documentId))
    }
}

#Preview {
    NavigationStack {
        CarerRegistrationScreen()
            .environmentObject(NavigationManager())
    }
}