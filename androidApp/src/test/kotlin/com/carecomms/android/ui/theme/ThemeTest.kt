package com.carecomms.android.ui.theme

import org.junit.Test
import org.junit.Assert.*

class ThemeTest {
    
    @Test
    fun colorPalette_hasCorrectValues() {
        // Test that our color constants are defined correctly
        assertEquals(0xFF4A148C, DeepPurple.value)
        assertEquals(0xFF9C27B0, LightPurple.value)
        assertEquals(0xFFE1BEE7, LightPurpleVariant.value)
        assertEquals(0xFFFFFFFF, White.value)
    }
    
    @Test
    fun typography_hasAccessibleSizes() {
        // Test that typography uses larger sizes for accessibility
        assertTrue("Body text should be at least 16sp for accessibility", 
            Typography.body1.fontSize.value >= 16f)
        assertTrue("Body2 text should be at least 14sp for accessibility", 
            Typography.body2.fontSize.value >= 14f)
        assertTrue("Button text should be at least 14sp for accessibility", 
            Typography.button.fontSize.value >= 14f)
    }
}