package com.carecomms.accessibility

import kotlinx.coroutines.flow.Flow

/**
 * Data class representing user accessibility preferences
 */
data class AccessibilitySettings(
    val textScale: Float = AccessibilityConstants.DEFAULT_TEXT_SCALE,
    val isHighContrastEnabled: Boolean = false,
    val isReducedMotionEnabled: Boolean = false,
    val isVoiceOverEnabled: Boolean = false,
    val isLargeTouchTargetsEnabled: Boolean = true
)

/**
 * Interface for managing accessibility preferences
 */
interface AccessibilityPreferences {
    suspend fun getAccessibilitySettings(): AccessibilitySettings
    suspend fun updateTextScale(scale: Float)
    suspend fun updateHighContrast(enabled: Boolean)
    suspend fun updateReducedMotion(enabled: Boolean)
    suspend fun updateVoiceOver(enabled: Boolean)
    suspend fun updateLargeTouchTargets(enabled: Boolean)
    fun observeAccessibilitySettings(): Flow<AccessibilitySettings>
}

/**
 * Utility functions for accessibility calculations
 */
object AccessibilityUtils {
    
    /**
     * Calculate scaled text size based on user preference
     */
    fun getScaledTextSize(baseSize: Float, scale: Float): Float {
        return baseSize * scale.coerceIn(
            AccessibilityConstants.MIN_TEXT_SCALE,
            AccessibilityConstants.MAX_TEXT_SCALE
        )
    }
    
    /**
     * Calculate touch target size based on accessibility settings
     */
    fun getTouchTargetSize(
        baseSize: Float,
        isLargeTouchTargetsEnabled: Boolean
    ): Float {
        return if (isLargeTouchTargetsEnabled) {
            maxOf(baseSize, AccessibilityConstants.MIN_TOUCH_TARGET_SIZE.toFloat())
        } else {
            baseSize
        }
    }
    
    /**
     * Get animation duration based on reduced motion preference
     */
    fun getAnimationDuration(
        normalDuration: Long,
        isReducedMotionEnabled: Boolean
    ): Long {
        return if (isReducedMotionEnabled) {
            AccessibilityConstants.REDUCED_MOTION_DURATION
        } else {
            normalDuration
        }
    }
    
    /**
     * Validate if text scale is within acceptable range
     */
    fun isValidTextScale(scale: Float): Boolean {
        return scale in AccessibilityConstants.MIN_TEXT_SCALE..AccessibilityConstants.MAX_TEXT_SCALE
    }
}