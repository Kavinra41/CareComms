package com.carecomms.android.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.screens.LandingScreen
import com.carecomms.android.ui.theme.CareCommsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LandingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun landingScreen_displaysCorrectly() {
        composeTestRule.setContent {
            CareCommsTheme {
                LandingScreen(
                    onLoginClick = {},
                    onSignupClick = {}
                )
            }
        }

        // Verify welcome text is displayed
        composeTestRule.onNodeWithText("Welcome to CareComms").assertIsDisplayed()
        
        // Verify description text is displayed
        composeTestRule.onNodeWithText("Connecting carers and care recipients", substring = true).assertIsDisplayed()
        
        // Verify buttons are displayed
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up as Carer").assertIsDisplayed()
        
        // Verify information for care recipients
        composeTestRule.onNodeWithText("For Care Recipients").assertIsDisplayed()
        composeTestRule.onNodeWithText("invitation link from your carer", substring = true).assertIsDisplayed()
    }

    @Test
    fun landingScreen_loginButtonWorks() {
        var loginClicked = false

        composeTestRule.setContent {
            CareCommsTheme {
                LandingScreen(
                    onLoginClick = { loginClicked = true },
                    onSignupClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Login").performClick()
        assert(loginClicked)
    }

    @Test
    fun landingScreen_signupButtonWorks() {
        var signupClicked = false

        composeTestRule.setContent {
            CareCommsTheme {
                LandingScreen(
                    onLoginClick = {},
                    onSignupClick = { signupClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Sign Up as Carer").performClick()
        assert(signupClicked)
    }

    @Test
    fun landingScreen_buttonsHaveCorrectAccessibility() {
        composeTestRule.setContent {
            CareCommsTheme {
                LandingScreen(
                    onLoginClick = {},
                    onSignupClick = {}
                )
            }
        }

        // Verify buttons have minimum touch target size (they should be 56.dp height)
        composeTestRule.onNodeWithText("Login").assertHeightIsAtLeast(56.dp)
        composeTestRule.onNodeWithText("Sign Up as Carer").assertHeightIsAtLeast(56.dp)
    }
}