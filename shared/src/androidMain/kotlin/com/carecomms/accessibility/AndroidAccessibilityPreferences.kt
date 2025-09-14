package com.carecomms.accessibility

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidAccessibilityPreferences(
    private val context: Context
) : AccessibilityPreferences {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "accessibility_preferences", 
        Context.MODE_PRIVATE
    )
    
    private val _accessibilitySettings = MutableStateFlow(loadSettings())
    
    private fun loadSettings(): AccessibilitySettings {
        return AccessibilitySettings(
            textScale = prefs.getFloat(KEY_TEXT_SCALE, AccessibilityConstants.DEFAULT_TEXT_SCALE),
            isHighContrastEnabled = prefs.getBoolean(KEY_HIGH_CONTRAST, false),
            isReducedMotionEnabled = prefs.getBoolean(KEY_REDUCED_MOTION, false),
            isVoiceOverEnabled = prefs.getBoolean(KEY_VOICE_OVER, false),
            isLargeTouchTargetsEnabled = prefs.getBoolean(KEY_LARGE_TOUCH_TARGETS, true)
        )
    }
    
    override suspend fun getAccessibilitySettings(): AccessibilitySettings {
        return _accessibilitySettings.value
    }
    
    override suspend fun updateTextScale(scale: Float) {
        if (AccessibilityUtils.isValidTextScale(scale)) {
            prefs.edit().putFloat(KEY_TEXT_SCALE, scale).apply()
            _accessibilitySettings.value = _accessibilitySettings.value.copy(textScale = scale)
        }
    }
    
    override suspend fun updateHighContrast(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
        _accessibilitySettings.value = _accessibilitySettings.value.copy(isHighContrastEnabled = enabled)
    }
    
    override suspend fun updateReducedMotion(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCED_MOTION, enabled).apply()
        _accessibilitySettings.value = _accessibilitySettings.value.copy(isReducedMotionEnabled = enabled)
    }
    
    override suspend fun updateVoiceOver(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VOICE_OVER, enabled).apply()
        _accessibilitySettings.value = _accessibilitySettings.value.copy(isVoiceOverEnabled = enabled)
    }
    
    override suspend fun updateLargeTouchTargets(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LARGE_TOUCH_TARGETS, enabled).apply()
        _accessibilitySettings.value = _accessibilitySettings.value.copy(isLargeTouchTargetsEnabled = enabled)
    }
    
    override fun observeAccessibilitySettings(): Flow<AccessibilitySettings> {
        return _accessibilitySettings.asStateFlow()
    }
    
    companion object {
        private const val KEY_TEXT_SCALE = "text_scale"
        private const val KEY_HIGH_CONTRAST = "high_contrast"
        private const val KEY_REDUCED_MOTION = "reduced_motion"
        private const val KEY_VOICE_OVER = "voice_over"
        private const val KEY_LARGE_TOUCH_TARGETS = "large_touch_targets"
    }
}