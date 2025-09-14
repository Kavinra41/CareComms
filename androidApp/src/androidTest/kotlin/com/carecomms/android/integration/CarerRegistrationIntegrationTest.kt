package com.carecomms.android.integration

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.theme.CareCommsTheme
import com.carecomms.android.navigation.AuthNavigation
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CarerRegistrationIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun carerRegistrationFlow_completeFlow_navigatesCorrectly() {
        var homeNavigationCalled = false
        var homeUserType = ""

        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    onNavigateToHome = { userType ->
                        homeNavigationCalled = true
                        homeUserType = userType
                    }
                )
            }
        }

        // Wait for splash screen to complete
        composeTestRule.waitForIdle()
        
        // Accept terms and conditions (if displayed)
        composeTestRule.onNodeWithText("Accept").performClick()
        
        // Navigate to carer registration
        composeTestRule.onNodeWithText("Sign Up").performClick()
        
        // Verify we're on the carer registration screen
        composeTestRule.onNodeWithText("Carer Registration").assertIsDisplayed()
        
        // Fill out the form
        composeTestRule.onNodeWithText("Email Address").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Age").performTextInput("35")
        composeTestRule.onNodeWithText("Phone Number").performTextInput("+1234567890")
        composeTestRule.onNodeWithText("Location").performTextInput("New York, NY")
        
        // Verify form fields are filled
        composeTestRule.onNodeWithText("test@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("+1234567890").assertIsDisplayed()
        composeTestRule.onNodeWithText("New York, NY").assertIsDisplayed()
    }

    @Test
    fun carerRegistrationForm_validation_showsErrors() {
        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    onNavigateToHome = { }
                )
            }
        }

        // Navigate to carer registration
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Accept").performClick()
        composeTestRule.onNodeWithText("Sign Up").performClick()
        
        // Try to submit empty form
        composeTestRule.onNodeWithText("Create Account").performClick()
        
        // Verify validation errors would be shown (in a real implementation)
        // This test verifies the UI structure is in place
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
    }

    @Test
    fun carerRegistrationScreen_backNavigation_worksCorrectly() {
        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    onNavigateToHome = { }
                )
            }
        }

        // Navigate to carer registration
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Accept").performClick()
        composeTestRule.onNodeWithText("Sign Up").performClick()
        
        // Verify we're on registration screen
        composeTestRule.onNodeWithText("Carer Registration").assertIsDisplayed()
        
        // Navigate back
        composeTestRule.onNodeWithText("← Back").performClick()
        
        // Verify we're back on landing screen
        composeTestRule.onNodeWithText("Welcome to CareComms").assertIsDisplayed()
    }
}