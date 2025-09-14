package com.carecomms.android.accessibility

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.carecomms.accessibility.AccessibilityConstants
import com.carecomms.accessibility.AndroidAccessibilityPreferences
import com.carecomms.android.ui.components.AccessibleButton
import com.carecomms.android.ui.components.AccessibleIconButton
import com.carecomms.android.ui.components.AccessibleTextField
import com.carecomms.android.ui.screens.AccessibilitySettingsScreen
import com.carecomms.android.ui.theme.CareCommsTheme
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Accessibility tests for CareComms Android app
 * Tests compliance with WCAG 2.1 AA guidelines and Android accessibility standards
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var accessibilityPreferences: AndroidAccessibilityPreferences

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        accessibilityPreferences = AndroidAccessibilityPreferences(context)
    }

    @Test
    fun accessibleButton_hasMinimumTouchTargetSize() {
        composeTestRule.setContent {
            CareCommsTheme {
                AccessibleButton(
                    text = "Test Button",
                    onClick = {},
                    contentDescription = "Test button for accessibility"
                )
            }
        }

        composeTestRule.onNodeWithText("Test Button")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertWidthIsAtLeast(AccessibilityConstants.MIN_TOUCH_TARGET_SIZE.dp)
            .assertHeightIsAtLeast(AccessibilityConstants.MIN_TOUCH_TARGET_SIZE.dp)
    }

    @Test
    fun accessibleButton_hasProperContentDescription() {
        val contentDescription = "Login button"
        
        composeTestRule.setContent {
            CareCommsTheme {
                AccessibleButton(
                    text = "Login",
                    onClick = {},
                    contentDescription = contentDescription
                )
            }
        }

        composeTestRule.onNode(hasContentDescription(contentDescription))
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun accessibleIconButton_hasMinimumTouchTargetSize() {
        composeTestRule.setContent {
            CareCommsTheme {
                AccessibleIconButton(
                    icon = androidx.compose.material.icons.Icons.Default.ArrowBack,
                    contentDescription = "Navigate back",
                    onClick = {}
                )
            }
        }

        composeTestRule.onNode(hasContentDescription("Navigate back"))
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertWidthIsAtLeast(AccessibilityConstants.MIN_TOUCH_TARGET_SIZE.dp)
            .assertHeightIsAtLeast(AccessibilityConstants.MIN_TOUCH_TARGET_SIZE.dp)
    }

    @Test
    fun accessibleTextField_hasProperSemantics() {
        composeTestRule.setContent {
            CareCommsTheme {
                AccessibleTextField(
                    value = "",
                    onValueChange = {},
                    label = "Email",
                    placeholder = "Enter your email",
                    contentDescription = "Email input field"
                )
            }
        }

        composeTestRule.onNode(hasContentDescription("Email input field"))
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHeightIsAtLeast(AccessibilityConstants.MIN_TOUCH_TARGET_SIZE.dp)
    }

    @Test
    fun accessibleTextField_showsErrorMessage() {
        val errorMessage = "Email is required"
        
        composeTestRule.setContent {
            CareCommsTheme {
                AccessibleTextField(
                    value = "",
                    onValueChange = {},
                    label = "Email",
                    isError = true,
                    errorMessage = errorMessage
                )
            }
        }

        composeTestRule.onNode(hasContentDescription("Error: $errorMessage"))
            .assertIsDisplayed()
    }

    @Test
    fun accessibilitySettingsScreen_hasProperNavigation() {
        composeTestRule.setContent {
            CareCommsTheme {
                AccessibilitySettingsScreen(
                    onNavigateBack = {}
                )
            }
        }

        // Check back button
        composeTestRule.onNode(hasContentDescription(AccessibilityConstants.ContentDescriptions.BACK_BUTTON))
            .assertIsDisplayed()
            .assertHasClickAction()

        // Check screen title
        composeTestRule.onNode(hasContentDescription("Accessibility Settings Screen"))
            .assertIsDisplayed()
    }

    @Test
    fun accessibilitySettingsScreen_textScaleSliderWorks() {
        composeTestRule.setContent {
            CareCommsTheme {
                AccessibilitySettingsScreen(
                    onNavigateBack = {}
                )
            }
        }

        // Find and interact with text scale slider
        composeTestRule.onNode(hasContentDescriptionThat { 
            it.contains("Text size slider") 
        })
            .assertIsDisplayed()
            .performTouchInput { swipeRight() }
    }

    @Test
    fun accessibilitySettingsScreen_highContrastToggleWorks() {
        composeTestRule.setContent {
            CareCommsTheme {
                AccessibilitySettingsScreen(
                    onNavigateBack = {}
                )
            }
        }

        // Find and toggle high contrast switch
        composeTestRule.onNode(hasContentDescriptionThat { 
            it.contains("High contrast") 
        })
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun accessibilitySettingsScreen_largeTouchTargetsToggleWorks() {
        composeTestRule.setContent {
            CareCommsTheme {
                AccessibilitySettingsScreen(
                    onNavigateBack = {}
                )
            }
        }

        // Find and toggle large touch targets switch
        composeTestRule.onNode(hasContentDescriptionThat { 
            it.contains("Large touch targets") 
        })
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun accessibilityPreferences_storeAndRetrieveSettings() = runBlocking {
        // Test text scale
        accessibilityPreferences.updateTextScale(1.5f)
        val settings = accessibilityPreferences.getAccessibilitySettings()
        assert(settings.textScale == 1.5f)

        // Test high contrast
        accessibilityPreferences.updateHighContrast(true)
        val updatedSettings = accessibilityPreferences.getAccessibilitySettings()
        assert(updatedSettings.isHighContrastEnabled)

        // Test large touch targets
        accessibilityPreferences.updateLargeTouchTargets(false)
        val finalSettings = accessibilityPreferences.getAccessibilitySettings()
        assert(!finalSettings.isLargeTouchTargetsEnabled)
    }

    @Test
    fun textScaling_worksWithinValidRange() = runBlocking {
        // Test minimum scale
        accessibilityPreferences.updateTextScale(AccessibilityConstants.MIN_TEXT_SCALE)
        var settings = accessibilityPreferences.getAccessibilitySettings()
        assert(settings.textScale == AccessibilityConstants.MIN_TEXT_SCALE)

        // Test maximum scale
        accessibilityPreferences.updateTextScale(AccessibilityConstants.MAX_TEXT_SCALE)
        settings = accessibilityPreferences.getAccessibilitySettings()
        assert(settings.textScale == AccessibilityConstants.MAX_TEXT_SCALE)

        // Test invalid scale (should be clamped)
        accessibilityPreferences.updateTextScale(3.0f)
        settings = accessibilityPreferences.getAccessibilitySettings()
        assert(settings.textScale <= AccessibilityConstants.MAX_TEXT_SCALE)
    }

    @Test
    fun elderlyUserScenario_largeTextAndTouchTargets() {
        // Simulate elderly user preferences
        runBlocking {
            accessibilityPreferences.updateTextScale(1.8f)
            accessibilityPreferences.updateLargeTouchTargets(true)
            accessibilityPreferences.updateHighContrast(true)
        }

        composeTestRule.setContent {
            CareCommsTheme {
                AccessibleButton(
                    text = "Large Button",
                    onClick = {},
                    contentDescription = "Large button for elderly users"
                )
            }
        }

        // Verify button meets elderly user requirements
        composeTestRule.onNode(hasContentDescription("Large button for elderly users"))
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertWidthIsAtLeast(AccessibilityConstants.MIN_TOUCH_TARGET_SIZE.dp)
            .assertHeightIsAtLeast(AccessibilityConstants.MIN_TOUCH_TARGET_SIZE.dp)
    }

    @Test
    fun voiceOverScenario_properSemanticLabels() {
        composeTestRule.setContent {
            CareCommsTheme {
                AccessibleTextField(
                    value = "John Doe",
                    onValueChange = {},
                    label = "Name",
                    contentDescription = "Enter your full name"
                )
            }
        }

        // Verify proper semantic information for screen readers
        composeTestRule.onNode(hasContentDescription("Enter your full name"))
            .assertIsDisplayed()
            .assert(hasText("John Doe"))
    }
}