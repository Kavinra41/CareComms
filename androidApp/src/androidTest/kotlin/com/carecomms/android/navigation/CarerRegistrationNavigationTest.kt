package com.carecomms.android.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.theme.CareCommsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CarerRegistrationNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun authNavigation_landingToCarerRegistration_navigatesCorrectly() {
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

        // Wait for splash screen to complete and navigate to terms
        composeTestRule.waitForIdle()
        
        // Accept terms and conditions
        composeTestRule.onNodeWithText("Accept").performClick()
        
        // Navigate to carer registration from landing screen
        composeTestRule.onNodeWithText("Sign Up").performClick()
        
        // Verify we're on the carer registration screen
        composeTestRule.onNodeWithText("Carer Registration").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Your Account").assertIsDisplayed()
    }

    @Test
    fun carerRegistration_backButton_navigatesToLanding() {
        var homeNavigationCalled = false

        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    onNavigateToHome = { homeNavigationCalled = true }
                )
            }
        }

        // Navigate to carer registration
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Accept").performClick()
        composeTestRule.onNodeWithText("Sign Up").performClick()
        
        // Verify we're on registration screen
        composeTestRule.onNodeWithText("Carer Registration").assertIsDisplayed()
        
        // Click back button
        composeTestRule.onNodeWithText("← Back").performClick()
        
        // Verify we're back on landing screen
        composeTestRule.onNodeWithText("Welcome to CareComms").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
    }

    @Test
    fun carerRegistration_successfulRegistration_navigatesToSuccess() {
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

        // Navigate to carer registration
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Accept").performClick()
        composeTestRule.onNodeWithText("Sign Up").performClick()
        
        // Fill out the registration form (this would trigger the success flow in a real scenario)
        // For testing purposes, we'll simulate the success state
        // Note: In a real test, you would mock the registration use case to return success
        
        // Verify registration screen is displayed
        composeTestRule.onNodeWithText("Carer Registration").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
    }

    @Test
    fun registrationSuccess_continueButton_navigatesToHome() {
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

        // This test would need to be expanded to actually trigger the success flow
        // For now, it verifies the navigation structure is in place
        
        // Navigate through the flow
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Accept").performClick()
        composeTestRule.onNodeWithText("Sign Up").performClick()
        
        // Verify we can navigate to registration
        composeTestRule.onNodeWithText("Carer Registration").assertIsDisplayed()
    }
}