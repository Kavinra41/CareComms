import SwiftUI

/**
 * SwiftUI modifiers for accessibility support in CareComms iOS app
 */

// MARK: - Accessibility Modifiers

extension View {
    
    /// Applies accessibility label and hint to a view
    func accessibilityLabel(_ label: String, hint: String? = nil) -> some View {
        self.modifier(AccessibilityLabelModifier(label: label, hint: hint))
    }
    
    /// Applies minimum touch target size for accessibility
    func accessibleTouchTarget(minSize: CGFloat = 44) -> some View {
        self.modifier(AccessibleTouchTargetModifier(minSize: minSize))
    }
    
    /// Applies high contrast styling when enabled
    func highContrastSupport() -> some View {
        self.modifier(HighContrastModifier())
    }
    
    /// Applies reduced motion animations when enabled
    func reducedMotionSupport(normalDuration: Double = 0.3, reducedDuration: Double = 0.15) -> some View {
        self.modifier(ReducedMotionModifier(normalDuration: normalDuration, reducedDuration: reducedDuration))
    }
    
    /// Applies dynamic type scaling for text
    func dynamicTypeSupport(textScale: CGFloat = 1.0) -> some View {
        self.modifier(DynamicTypeModifier(textScale: textScale))
    }
}

// MARK: - Modifier Implementations

struct AccessibilityLabelModifier: ViewModifier {
    let label: String
    let hint: String?
    
    func body(content: Content) -> some View {
        content
            .accessibilityLabel(label)
            .accessibilityHint(hint ?? "")
    }
}

struct AccessibleTouchTargetModifier: ViewModifier {
    let minSize: CGFloat
    
    func body(content: Content) -> some View {
        content
            .frame(minWidth: minSize, minHeight: minSize)
            .contentShape(Rectangle())
    }
}

struct HighContrastModifier: ViewModifier {
    @Environment(\.accessibilityReduceTransparency) var reduceTransparency
    @Environment(\.accessibilityDifferentiateWithoutColor) var differentiateWithoutColor
    
    func body(content: Content) -> some View {
        content
            .background(reduceTransparency ? Color.black : Color.clear)
            .foregroundColor(differentiateWithoutColor ? .primary : .accentColor)
    }
}

struct ReducedMotionModifier: ViewModifier {
    let normalDuration: Double
    let reducedDuration: Double
    
    @Environment(\.accessibilityReduceMotion) var reduceMotion
    
    func body(content: Content) -> some View {
        content
            .animation(
                .easeInOut(duration: reduceMotion ? reducedDuration : normalDuration),
                value: UUID()
            )
    }
}

struct DynamicTypeModifier: ViewModifier {
    let textScale: CGFloat
    
    @Environment(\.dynamicTypeSize) var dynamicTypeSize
    
    func body(content: Content) -> some View {
        content
            .scaleEffect(textScale)
            .dynamicTypeSize(dynamicTypeSize)
    }
}

// MARK: - Accessibility Constants

struct AccessibilityConstants {
    static let minTouchTargetSize: CGFloat = 44
    static let recommendedTouchTargetSize: CGFloat = 48
    static let minTextScale: CGFloat = 0.85
    static let maxTextScale: CGFloat = 2.0
    static let defaultTextScale: CGFloat = 1.0
    
    struct ContentDescriptions {
        static let backButton = "Navigate back"
        static let closeButton = "Close"
        static let menuButton = "Open menu"
        static let searchButton = "Search"
        static let sendMessage = "Send message"
        static let inviteCaree = "Invite caree"
        static let logout = "Logout"
        static let profile = "Profile"
        static let chatList = "Chat list"
        static let dashboard = "Data dashboard"
        static let detailsTree = "Details tree"
        static let typingIndicator = "User is typing"
        static let messageSent = "Message sent"
        static let messageDelivered = "Message delivered"
        static let messageRead = "Message read"
    }
    
    struct SemanticLabels {
        static let chatMessage = "Chat message"
        static let careeItem = "Caree"
        static let navigationTab = "Navigation tab"
        static let formField = "Form field"
        static let errorMessage = "Error message"
        static let successMessage = "Success message"
        static let loadingIndicator = "Loading"
    }
}

// MARK: - Accessibility Helpers

class AccessibilityHelper: ObservableObject {
    @Published var isVoiceOverRunning = UIAccessibility.isVoiceOverRunning
    @Published var isSwitchControlRunning = UIAccessibility.isSwitchControlRunning
    @Published var isReduceMotionEnabled = UIAccessibility.isReduceMotionEnabled
    @Published var isReduceTransparencyEnabled = UIAccessibility.isReduceTransparencyEnabled
    @Published var isDarkerSystemColorsEnabled = UIAccessibility.isDarkerSystemColorsEnabled
    
    init() {
        // Listen for accessibility changes
        NotificationCenter.default.addObserver(
            forName: UIAccessibility.voiceOverStatusDidChangeNotification,
            object: nil,
            queue: .main
        ) { _ in
            self.isVoiceOverRunning = UIAccessibility.isVoiceOverRunning
        }
        
        NotificationCenter.default.addObserver(
            forName: UIAccessibility.switchControlStatusDidChangeNotification,
            object: nil,
            queue: .main
        ) { _ in
            self.isSwitchControlRunning = UIAccessibility.isSwitchControlRunning
        }
        
        NotificationCenter.default.addObserver(
            forName: UIAccessibility.reduceMotionStatusDidChangeNotification,
            object: nil,
            queue: .main
        ) { _ in
            self.isReduceMotionEnabled = UIAccessibility.isReduceMotionEnabled
        }
        
        NotificationCenter.default.addObserver(
            forName: UIAccessibility.reduceTransparencyStatusDidChangeNotification,
            object: nil,
            queue: .main
        ) { _ in
            self.isReduceTransparencyEnabled = UIAccessibility.isReduceTransparencyEnabled
        }
        
        NotificationCenter.default.addObserver(
            forName: UIAccessibility.darkerSystemColorsStatusDidChangeNotification,
            object: nil,
            queue: .main
        ) { _ in
            self.isDarkerSystemColorsEnabled = UIAccessibility.isDarkerSystemColorsEnabled
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}