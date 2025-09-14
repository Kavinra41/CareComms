package com.carecomms.accessibility

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

class IOSAccessibilityPreferences : AccessibilityPreferences {
    
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val _accessibilitySettings = MutableStateFlow(loadSettings())
    
    private fun loadSettings(): AccessibilitySettings {
        return AccessibilitySettings(
            textScale = userDefaults.floatForKey(KEY_TEXT_SCALE).takeIf { it != 0.0f } 
                ?: AccessibilityConstants.DEFAULT_TEXT_SCALE,
            isHighContrastEnabled = userDefaults.boolForKey(KEY_HIGH_CONTRAST),
            isReducedMotionEnabled = userDefaults.boolForKey(KEY_REDUCED_MOTION),
            isVoiceOverEnabled = userDefaults.boolForKey(KEY_VOICE_OVER),
            isLargeTouchTargetsEnabled = userDefaults.objectForKey(KEY_LARGE_TOUCH_TARGETS)?.let { 
                userDefaults.boolForKey(KEY_LARGE_TOUCH_TARGETS) 
            } ?: true
        )
    }
    
    override suspend fun getAccessibilitySettings(): AccessibilitySettings {
        return _accessibilitySettings.value
    }
    
    override suspend fun updateTextScale(scale: Float) {
        if (AccessibilityUtils.isValidTextScale(scale)) {
            userDefaults.setFloat(scale, KEY_TEXT_SCALE)
            _accessibilitySettings.value = _accessibilitySettings.value.copy(textScale = scale)
        }
    }
    
    override suspend fun updateHighContrast(enabled: Boolean) {
        userDefaults.setBool(enabled, KEY_HIGH_CONTRAST)
        _accessibilitySettings.value = _accessibilitySettings.value.copy(isHighContrastEnabled = enabled)
    }
    
    override suspend fun updateReducedMotion(enabled: Boolean) {
        userDefaults.setBool(enabled, KEY_REDUCED_MOTION)
        _accessibilitySettings.value = _accessibilitySettings.value.copy(isReducedMotionEnabled = enabled)
    }
    
    override suspend fun updateVoiceOver(enabled: Boolean) {
        userDefaults.setBool(enabled, KEY_VOICE_OVER)
        _accessibilitySettings.value = _accessibilitySettings.value.copy(isVoiceOverEnabled = enabled)
    }
    
    override suspend fun updateLargeTouchTargets(enabled: Boolean) {
        userDefaults.setBool(enabled, KEY_LARGE_TOUCH_TARGETS)
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