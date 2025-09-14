package com.carecomms.accessibility

/**
 * Accessibility constants for CareComms app
 * Following WCAG 2.1 AA guidelines and platform-specific accessibility standards
 */
object AccessibilityConstants {
    // Minimum touch target sizes (in dp)
    const val MIN_TOUCH_TARGET_SIZE = 44 // 44dp minimum for both iOS and Android
    const val RECOMMENDED_TOUCH_TARGET_SIZE = 48 // 48dp recommended
    
    // Text scaling factors for dynamic type support
    const val MIN_TEXT_SCALE = 0.85f
    const val DEFAULT_TEXT_SCALE = 1.0f
    const val MAX_TEXT_SCALE = 2.0f
    
    // High contrast color ratios (WCAG AA compliance)
    const val MIN_CONTRAST_RATIO_NORMAL = 4.5f
    const val MIN_CONTRAST_RATIO_LARGE = 3.0f
    
    // Animation durations for accessibility
    const val REDUCED_MOTION_DURATION = 150L
    const val NORMAL_MOTION_DURATION = 300L
    
    // Content descriptions for common UI elements
    object ContentDescriptions {
        const val BACK_BUTTON = "Navigate back"
        const val CLOSE_BUTTON = "Close"
        const val MENU_BUTTON = "Open menu"
        const val SEARCH_BUTTON = "Search"
        const val SEND_MESSAGE = "Send message"
        const val INVITE_CAREE = "Invite caree"
        const val LOGOUT = "Logout"
        const val PROFILE = "Profile"
        const val CHAT_LIST = "Chat list"
        const val DASHBOARD = "Data dashboard"
        const val DETAILS_TREE = "Details tree"
        const val TYPING_INDICATOR = "User is typing"
        const val MESSAGE_STATUS_SENT = "Message sent"
        const val MESSAGE_STATUS_DELIVERED = "Message delivered"
        const val MESSAGE_STATUS_READ = "Message read"
    }
    
    // Semantic labels for screen readers
    object SemanticLabels {
        const val CHAT_MESSAGE = "Chat message"
        const val CAREE_ITEM = "Caree"
        const val NAVIGATION_TAB = "Navigation tab"
        const val FORM_FIELD = "Form field"
        const val ERROR_MESSAGE = "Error message"
        const val SUCCESS_MESSAGE = "Success message"
        const val LOADING_INDICATOR = "Loading"
    }
}