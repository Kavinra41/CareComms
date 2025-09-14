package com.carecomms.android.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.screens.CareeRegistrationSuccessScreen
import com.carecomms.android.ui.theme.CareCommsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CareeRegistrationSuccessScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun careeRegistrationSuccessScreen_displaysWelcomeMessage() {
        val carerName = "Dr. Smith"
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationSuccessScreen(
                    carerName = carerName,
                    onNavigateToChat = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Welcome to CareComms!")
            .assertIsDisplayed()
    }

    @Test
    fun careeRegistrationSuccessScreen_displaysCarerName() {
        val carerName = "Dr. Smith"
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationSuccessScreen(
                    carerName = carerName,
                    onNavigateToChat = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Your account has been created successfully.\nYou're now connected with Dr. Smith.")
            .assertIsDisplayed()
    }

    @Test
    fun careeRegistrationSuccessScreen_displaysSuccessIcon() {
        val carerName = "Dr. Smith"
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationSuccessScreen(
                    carerName = carerName,
                    onNavigateToChat = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Success")
            .assertIsDisplayed()
    }

    @Test
    fun careeRegistrationSuccessScreen_displaysLoadingIndicator() {
        val carerName = "Dr. Smith"
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationSuccessScreen(
                    carerName = carerName,
                    onNavigateToChat = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Taking you to your chat...")
            .assertIsDisplayed()
    }

    @Test
    fun careeRegistrationSuccessScreen_displaysContinueButton() {
        val carerName = "Dr. Smith"
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationSuccessScreen(
                    carerName = carerName,
                    onNavigateToChat = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Continue to Chat")
            .assertIsDisplayed()
    }

    @Test
    fun careeRegistrationSuccessScreen_continueButton_triggersNavigation() {
        val carerName = "Dr. Smith"
        var navigationTriggered = false
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationSuccessScreen(
                    carerName = carerName,
                    onNavigateToChat = { navigationTriggered = true }
                )
            }
        }

        composeTestRule
            .onNodeWithText("Continue to Chat")
            .performClick()

        assert(navigationTriggered)
    }

    @Test
    fun careeRegistrationSuccessScreen_handlesLongCarerName() {
        val carerName = "Dr. Elizabeth Margaret Thompson-Williams"
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationSuccessScreen(
                    carerName = carerName,
                    onNavigateToChat = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Your account has been created successfully.\nYou're now connected with Dr. Elizabeth Margaret Thompson-Williams.")
            .assertIsDisplayed()
    }

    @Test
    fun careeRegistrationSuccessScreen_handlesEmptyCarerName() {
        val carerName = ""
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationSuccessScreen(
                    carerName = carerName,
                    onNavigateToChat = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Your account has been created successfully.\nYou're now connected with .")
            .assertIsDisplayed()
    }
}