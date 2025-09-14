package com.carecomms.accessibility

import kotlin.test.*

/**
 * Tests for accessibility utilities and constants
 */
class AccessibilityTest {

    @Test
    fun testAccessibilityConstants() {
        // Test minimum touch target size
        assertEquals(44, AccessibilityConstants.MIN_TOUCH_TARGET_SIZE)
        assertEquals(48, AccessibilityConstants.RECOMMENDED_TOUCH_TARGET_SIZE)
        
        // Test text scaling constants
        assertEquals(0.85f, AccessibilityConstants.MIN_TEXT_SCALE)
        assertEquals(1.0f, AccessibilityConstants.DEFAULT_TEXT_SCALE)
        assertEquals(2.0f, AccessibilityConstants.MAX_TEXT_SCALE)
        
        // Test contrast ratios
        assertEquals(4.5f, AccessibilityConstants.MIN_CONTRAST_RATIO_NORMAL)
        assertEquals(3.0f, AccessibilityConstants.MIN_CONTRAST_RATIO_LARGE)
        
        // Test animation durations
        assertEquals(150L, AccessibilityConstants.REDUCED_MOTION_DURATION)
        assertEquals(300L, AccessibilityConstants.NORMAL_MOTION_DURATION)
    }

    @Test
    fun testAccessibilityUtils_getScaledTextSize() {
        val baseSize = 16f
        
        // Test normal scaling
        assertEquals(16f, AccessibilityUtils.getScaledTextSize(baseSize, 1.0f))
        assertEquals(24f, AccessibilityUtils.getScaledTextSize(baseSize, 1.5f))
        assertEquals(32f, AccessibilityUtils.getScaledTextSize(baseSize, 2.0f))
        
        // Test minimum scale clamping
        assertEquals(13.6f, AccessibilityUtils.getScaledTextSize(baseSize, 0.85f))
        assertEquals(13.6f, AccessibilityUtils.getScaledTextSize(baseSize, 0.5f)) // Should be clamped to min
        
        // Test maximum scale clamping
        assertEquals(32f, AccessibilityUtils.getScaledTextSize(baseSize, 2.0f))
        assertEquals(32f, AccessibilityUtils.getScaledTextSize(baseSize, 3.0f)) // Should be clamped to max
    }

    @Test
    fun testAccessibilityUtils_getTouchTargetSize() {
        val baseSize = 40f
        
        // Test with large touch targets enabled
        assertEquals(44f, AccessibilityUtils.getTouchTargetSize(baseSize, true))
        assertEquals(50f, AccessibilityUtils.getTouchTargetSize(50f, true)) // Already larger than minimum
        
        // Test with large touch targets disabled
        assertEquals(40f, AccessibilityUtils.getTouchTargetSize(baseSize, false))
        assertEquals(30f, AccessibilityUtils.getTouchTargetSize(30f, false))
    }

    @Test
    fun testAccessibilityUtils_getAnimationDuration() {
        val normalDuration = 300L
        
        // Test with reduced motion enabled
        assertEquals(150L, AccessibilityUtils.getAnimationDuration(normalDuration, true))
        
        // Test with reduced motion disabled
        assertEquals(300L, AccessibilityUtils.getAnimationDuration(normalDuration, false))
        
        // Test with different normal duration
        assertEquals(150L, AccessibilityUtils.getAnimationDuration(500L, true))
        assertEquals(500L, AccessibilityUtils.getAnimationDuration(500L, false))
    }

    @Test
    fun testAccessibilityUtils_isValidTextScale() {
        // Test valid scales
        assertTrue(AccessibilityUtils.isValidTextScale(0.85f))
        assertTrue(AccessibilityUtils.isValidTextScale(1.0f))
        assertTrue(AccessibilityUtils.isValidTextScale(1.5f))
        assertTrue(AccessibilityUtils.isValidTextScale(2.0f))
        
        // Test invalid scales
        assertFalse(AccessibilityUtils.isValidTextScale(0.5f))
        assertFalse(AccessibilityUtils.isValidTextScale(3.0f))
        assertFalse(AccessibilityUtils.isValidTextScale(-1.0f))
    }

    @Test
    fun testAccessibilitySettings_defaultValues() {
        val settings = AccessibilitySettings()
        
        assertEquals(AccessibilityConstants.DEFAULT_TEXT_SCALE, settings.textScale)
        assertFalse(settings.isHighContrastEnabled)
        assertFalse(settings.isReducedMotionEnabled)
        assertFalse(settings.isVoiceOverEnabled)
        assertTrue(settings.isLargeTouchTargetsEnabled)
    }

    @Test
    fun testAccessibilitySettings_customValues() {
        val settings = AccessibilitySettings(
            textScale = 1.5f,
            isHighContrastEnabled = true,
            isReducedMotionEnabled = true,
            isVoiceOverEnabled = true,
            isLargeTouchTargetsEnabled = false
        )
        
        assertEquals(1.5f, settings.textScale)
        assertTrue(settings.isHighContrastEnabled)
        assertTrue(settings.isReducedMotionEnabled)
        assertTrue(settings.isVoiceOverEnabled)
        assertFalse(settings.isLargeTouchTargetsEnabled)
    }

    @Test
    fun testContentDescriptions_exist() {
        // Test that all content descriptions are non-empty
        assertNotEquals("", AccessibilityConstants.ContentDescriptions.BACK_BUTTON)
        assertNotEquals("", AccessibilityConstants.ContentDescriptions.CLOSE_BUTTON)
        assertNotEquals("", AccessibilityConstants.ContentDescriptions.MENU_BUTTON)
        assertNotEquals("", AccessibilityConstants.ContentDescriptions.SEARCH_BUTTON)
        assertNotEquals("", AccessibilityConstants.ContentDescriptions.SEND_MESSAGE)
        assertNotEquals("", AccessibilityConstants.ContentDescriptions.INVITE_CAREE)
        assertNotEquals("", AccessibilityConstants.ContentDescriptions.LOGOUT)
        assertNotEquals("", AccessibilityConstants.ContentDescriptions.PROFILE)
        assertNotEquals("", AccessibilityConstants.ContentDescriptions.CHAT_LIST)
        assertNotEquals("", AccessibilityConstants.ContentDescriptions.DASHBOARD)
        assertNotEquals("", AccessibilityConstants.ContentDescriptions.DETAILS_TREE)
        assertNotEquals("", AccessibilityConstants.ContentDescriptions.TYPING_INDICATOR)
        assertNotEquals("", AccessibilityConstants.ContentDescriptions.MESSAGE_STATUS_SENT)
        assertNotEquals("", AccessibilityConstants.ContentDescriptions.MESSAGE_STATUS_DELIVERED)
        assertNotEquals("", AccessibilityConstants.ContentDescriptions.MESSAGE_STATUS_READ)
    }

    @Test
    fun testSemanticLabels_exist() {
        // Test that all semantic labels are non-empty
        assertNotEquals("", AccessibilityConstants.SemanticLabels.CHAT_MESSAGE)
        assertNotEquals("", AccessibilityConstants.SemanticLabels.CAREE_ITEM)
        assertNotEquals("", AccessibilityConstants.SemanticLabels.NAVIGATION_TAB)
        assertNotEquals("", AccessibilityConstants.SemanticLabels.FORM_FIELD)
        assertNotEquals("", AccessibilityConstants.SemanticLabels.ERROR_MESSAGE)
        assertNotEquals("", AccessibilityConstants.SemanticLabels.SUCCESS_MESSAGE)
        assertNotEquals("", AccessibilityConstants.SemanticLabels.LOADING_INDICATOR)
    }

    @Test
    fun testElderlyUserScenario() {
        // Test settings that would be appropriate for elderly users
        val elderlySettings = AccessibilitySettings(
            textScale = 1.8f, // Larger text
            isHighContrastEnabled = true, // Better visibility
            isReducedMotionEnabled = true, // Less distracting
            isLargeTouchTargetsEnabled = true // Easier to tap
        )
        
        // Verify text scaling
        val scaledTextSize = AccessibilityUtils.getScaledTextSize(16f, elderlySettings.textScale)
        assertEquals(28.8f, scaledTextSize)
        
        // Verify touch target sizing
        val touchTargetSize = AccessibilityUtils.getTouchTargetSize(40f, elderlySettings.isLargeTouchTargetsEnabled)
        assertEquals(44f, touchTargetSize) // Should meet minimum requirement
        
        // Verify animation duration
        val animationDuration = AccessibilityUtils.getAnimationDuration(300L, elderlySettings.isReducedMotionEnabled)
        assertEquals(150L, animationDuration) // Should be reduced
    }

    @Test
    fun testVisionImpairedUserScenario() {
        // Test settings for users with vision impairments
        val visionImpairedSettings = AccessibilitySettings(
            textScale = 2.0f, // Maximum text size
            isHighContrastEnabled = true, // High contrast for better visibility
            isVoiceOverEnabled = true, // Screen reader support
            isLargeTouchTargetsEnabled = true // Larger touch targets
        )
        
        // Verify maximum text scaling
        val scaledTextSize = AccessibilityUtils.getScaledTextSize(16f, visionImpairedSettings.textScale)
        assertEquals(32f, scaledTextSize)
        
        // Verify settings are appropriate
        assertTrue(visionImpairedSettings.isHighContrastEnabled)
        assertTrue(visionImpairedSettings.isVoiceOverEnabled)
        assertTrue(visionImpairedSettings.isLargeTouchTargetsEnabled)
    }

    @Test
    fun testMotorImpairedUserScenario() {
        // Test settings for users with motor impairments
        val motorImpairedSettings = AccessibilitySettings(
            textScale = 1.2f, // Slightly larger text
            isLargeTouchTargetsEnabled = true, // Larger touch targets for easier interaction
            isReducedMotionEnabled = true // Reduced motion to avoid accidental triggers
        )
        
        // Verify touch target sizing is prioritized
        val touchTargetSize = AccessibilityUtils.getTouchTargetSize(35f, motorImpairedSettings.isLargeTouchTargetsEnabled)
        assertEquals(44f, touchTargetSize) // Should meet minimum for easier tapping
        
        // Verify reduced motion
        val animationDuration = AccessibilityUtils.getAnimationDuration(300L, motorImpairedSettings.isReducedMotionEnabled)
        assertEquals(150L, animationDuration)
    }
}