import SwiftUI

/**
 * Accessible text field component for iOS
 */
struct AccessibleTextField: View {
    @Binding var text: String
    let label: String
    let placeholder: String
    let contentDescription: String?
    let isSecure: Bool
    let isEnabled: Bool
    let errorMessage: String?
    
    @FocusState private var isFocused: Bool
    
    init(
        text: Binding<String>,
        label: String,
        placeholder: String = "",
        contentDescription: String? = nil,
        isSecure: Bool = false,
        isEnabled: Bool = true,
        errorMessage: String? = nil
    ) {
        self._text = text
        self.label = label
        self.placeholder = placeholder
        self.contentDescription = contentDescription
        self.isSecure = isSecure
        self.isEnabled = isEnabled
        self.errorMessage = errorMessage
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Label
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
                .accessibilityHidden(true) // Hidden because it's part of the text field's label
            
            // Text Field
            Group {
                if isSecure {
                    SecureField(placeholder, text: $text)
                } else {
                    TextField(placeholder, text: $text)
                }
            }
            .textFieldStyle(AccessibleTextFieldStyle(
                isError: errorMessage != nil,
                isFocused: isFocused
            ))
            .focused($isFocused)
            .disabled(!isEnabled)
            .accessibilityLabel(contentDescription ?? label)
            .accessibilityValue(text.isEmpty ? placeholder : text)
            .accessibilityAddTraits(.isSearchField)
            .dynamicTypeSupport()
            
            // Error Message
            if let errorMessage = errorMessage {
                Text(errorMessage)
                    .font(.caption)
                    .foregroundColor(.red)
                    .accessibilityLabel("Error: \(errorMessage)")
                    .accessibilityAddTraits(.isStaticText)
            }
        }
    }
}

/**
 * Custom text field style for accessibility
 */
struct AccessibleTextFieldStyle: TextFieldStyle {
    let isError: Bool
    let isFocused: Bool
    
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(Color(.systemBackground))
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(borderColor, lineWidth: borderWidth)
            )
            .frame(minHeight: AccessibilityConstants.minTouchTargetSize)
    }
    
    private var borderColor: Color {
        if isError {
            return .red
        } else if isFocused {
            return Color("DeepPurple")
        } else {
            return Color(.systemGray4)
        }
    }
    
    private var borderWidth: CGFloat {
        isFocused ? 2 : 1
    }
}

/**
 * Accessible search field component
 */
struct AccessibleSearchField: View {
    @Binding var searchText: String
    let placeholder: String
    let onSearchChanged: (String) -> Void
    
    init(
        searchText: Binding<String>,
        placeholder: String = "Search",
        onSearchChanged: @escaping (String) -> Void = { _ in }
    ) {
        self._searchText = searchText
        self.placeholder = placeholder
        self.onSearchChanged = onSearchChanged
    }
    
    var body: some View {
        HStack {
            Image(systemName: "magnifyingglass")
                .foregroundColor(.secondary)
                .accessibilityHidden(true)
            
            TextField(placeholder, text: $searchText)
                .textFieldStyle(PlainTextFieldStyle())
                .onChange(of: searchText) { newValue in
                    onSearchChanged(newValue)
                }
                .accessibilityLabel("Search field")
                .accessibilityValue(searchText.isEmpty ? placeholder : searchText)
                .accessibilityAddTraits(.isSearchField)
                .dynamicTypeSupport()
            
            if !searchText.isEmpty {
                AccessibleIconButton(
                    systemName: "xmark.circle.fill",
                    action: {
                        searchText = ""
                        onSearchChanged("")
                    },
                    contentDescription: "Clear search",
                    size: 16
                )
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(Color(.systemGray6))
        .cornerRadius(10)
        .frame(minHeight: AccessibilityConstants.minTouchTargetSize)
    }
}

// MARK: - Preview

struct AccessibleTextField_Previews: PreviewProvider {
    @State static var text = ""
    @State static var searchText = ""
    
    static var previews: some View {
        VStack(spacing: 20) {
            AccessibleTextField(
                text: $text,
                label: "Email",
                placeholder: "Enter your email",
                contentDescription: "Email input field"
            )
            
            AccessibleTextField(
                text: $text,
                label: "Password",
                placeholder: "Enter your password",
                contentDescription: "Password input field",
                isSecure: true
            )
            
            AccessibleTextField(
                text: $text,
                label: "Name",
                placeholder: "Enter your name",
                contentDescription: "Name input field",
                errorMessage: "Name is required"
            )
            
            AccessibleSearchField(
                searchText: $searchText,
                placeholder: "Search carees"
            )
        }
        .padding()
        .previewLayout(.sizeThatFits)
    }
}