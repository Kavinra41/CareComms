package com.carecomms.android.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.theme.CareCommsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun authNavigation_startsWithSplashScreen() {
        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    onNavigateToHome = {}
                )
            }
        }

        // Should start with splash screen
        // Since splash screen doesn't have specific text, we just verify it's displayed
        composeTestRule.onRoot().assertIsDisplayed()
        
        // Wait for splash to complete and navigate to terms
        composeTestRule.waitUntil(timeoutMillis = 6000) {
            try {
                composeTestRule.onNodeWithText("Terms and Conditions").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    @Test
    fun authNavigation_navigatesFromTermsToLanding() {
        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    onNavigateToHome = {}
                )
            }
        }

        // Wait for terms screen
        composeTestRule.waitUntil(timeoutMillis = 6000) {
            try {
                composeTestRule.onNodeWithText("Terms and Conditions").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Scroll to bottom to enable accept button
        composeTestRule.onNodeWithText("Welcome to CareComms", substring = true)
            .performScrollTo()

        // Wait a bit for scroll to complete and button to enable
        composeTestRule.waitForIdle()

        // Accept terms
        composeTestRule.onNodeWithText("Accept").performClick()

        // Should navigate to landing screen
        composeTestRule.onNodeWithText("Welcome to CareComms").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }

    @Test
    fun authNavigation_navigatesFromLandingToLogin() {
        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    onNavigateToHome = {}
                )
            }
        }

        // Wait for and navigate through splash and terms
        composeTestRule.waitUntil(timeoutMillis = 6000) {
            try {
                composeTestRule.onNodeWithText("Terms and Conditions").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Accept terms to get to landing
        composeTestRule.onNodeWithText("Accept").performClick()

        // Click login button
        composeTestRule.onNodeWithText("Login").performClick()

        // Should navigate to login screen
        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please sign in to your account").assertIsDisplayed()
    }

    @Test
    fun authNavigation_navigatesBackFromLogin() {
        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    onNavigateToHome = {}
                )
            }
        }

        // Navigate to login screen
        composeTestRule.waitUntil(timeoutMillis = 6000) {
            try {
                composeTestRule.onNodeWithText("Terms and Conditions").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithText("Accept").performClick()
        composeTestRule.onNodeWithText("Login").performClick()

        // Click back button
        composeTestRule.onNodeWithText("← Back").performClick()

        // Should navigate back to landing screen
        composeTestRule.onNodeWithText("Welcome to CareComms").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up as Carer").assertIsDisplayed()
    }
}