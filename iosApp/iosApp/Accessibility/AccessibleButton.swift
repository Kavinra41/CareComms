import SwiftUI

/**
 * Accessible button component for iOS that follows accessibility guidelines
 */
struct AccessibleButton: View {
    let title: String
    let action: () -> Void
    let contentDescription: String?
    let isEnabled: Bool
    let style: ButtonStyle
    
    enum ButtonStyle {
        case primary
        case secondary
        case destructive
    }
    
    init(
        title: String,
        action: @escaping () -> Void,
        contentDescription: String? = nil,
        isEnabled: Bool = true,
        style: ButtonStyle = .primary
    ) {
        self.title = title
        self.action = action
        self.contentDescription = contentDescription
        self.isEnabled = isEnabled
        self.style = style
    }
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.body)
                .fontWeight(.medium)
                .foregroundColor(textColor)
                .padding(.horizontal, 24)
                .padding(.vertical, 12)
                .background(backgroundColor)
                .cornerRadius(8)
        }
        .disabled(!isEnabled)
        .accessibleTouchTarget(minSize: AccessibilityConstants.minTouchTargetSize)
        .accessibilityLabel(contentDescription ?? title)
        .accessibilityAddTraits(.isButton)
        .highContrastSupport()
        .dynamicTypeSupport()
    }
    
    private var backgroundColor: Color {
        switch style {
        case .primary:
            return isEnabled ? Color("DeepPurple") : Color.gray.opacity(0.3)
        case .secondary:
            return isEnabled ? Color("LightPurple") : Color.gray.opacity(0.3)
        case .destructive:
            return isEnabled ? Color.red : Color.gray.opacity(0.3)
        }
    }
    
    private var textColor: Color {
        switch style {
        case .primary, .destructive:
            return .white
        case .secondary:
            return isEnabled ? Color("DeepPurple") : Color.gray
        }
    }
}

/**
 * Accessible icon button for iOS
 */
struct AccessibleIconButton: View {
    let systemName: String
    let action: () -> Void
    let contentDescription: String
    let isEnabled: Bool
    let size: CGFloat
    
    init(
        systemName: String,
        action: @escaping () -> Void,
        contentDescription: String,
        isEnabled: Bool = true,
        size: CGFloat = 24
    ) {
        self.systemName = systemName
        self.action = action
        self.contentDescription = contentDescription
        self.isEnabled = isEnabled
        self.size = size
    }
    
    var body: some View {
        Button(action: action) {
            Image(systemName: systemName)
                .font(.system(size: size))
                .foregroundColor(isEnabled ? .primary : .gray)
        }
        .disabled(!isEnabled)
        .accessibleTouchTarget(minSize: AccessibilityConstants.minTouchTargetSize)
        .accessibilityLabel(contentDescription)
        .accessibilityAddTraits(.isButton)
        .highContrastSupport()
    }
}

// MARK: - Preview

struct AccessibleButton_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 20) {
            AccessibleButton(
                title: "Primary Button",
                action: {},
                contentDescription: "Primary action button",
                style: .primary
            )
            
            AccessibleButton(
                title: "Secondary Button",
                action: {},
                contentDescription: "Secondary action button",
                style: .secondary
            )
            
            AccessibleButton(
                title: "Destructive Button",
                action: {},
                contentDescription: "Destructive action button",
                style: .destructive
            )
            
            AccessibleButton(
                title: "Disabled Button",
                action: {},
                contentDescription: "Disabled button",
                isEnabled: false
            )
            
            AccessibleIconButton(
                systemName: "arrow.left",
                action: {},
                contentDescription: "Navigate back"
            )
        }
        .padding()
        .previewLayout(.sizeThatFits)
    }
}