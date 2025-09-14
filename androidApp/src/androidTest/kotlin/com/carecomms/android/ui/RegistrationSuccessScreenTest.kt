package com.carecomms.android.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.screens.RegistrationSuccessScreen
import com.carecomms.android.ui.theme.CareCommsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationSuccessScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun registrationSuccessScreen_displaysSuccessMessage() {
        composeTestRule.setContent {
            CareCommsTheme {
                RegistrationSuccessScreen(
                    onNavigateToChatList = {}
                )
            }
        }

        // Verify success elements are displayed
        composeTestRule.onNodeWithText("Registration Successful!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome to CareComms! Your carer account has been created successfully.").assertIsDisplayed()
        composeTestRule.onNodeWithText("You can now start inviting carees and managing your care coordination.").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Success").assertIsDisplayed()
    }

    @Test
    fun registrationSuccessScreen_displaysContinueButton() {
        composeTestRule.setContent {
            CareCommsTheme {
                RegistrationSuccessScreen(
                    onNavigateToChatList = {}
                )
            }
        }

        // Verify continue button is displayed
        composeTestRule.onNodeWithText("Continue to Chat List").assertIsDisplayed()
    }

    @Test
    fun registrationSuccessScreen_continueButton_triggersNavigation() {
        var navigationTriggered = false

        composeTestRule.setContent {
            CareCommsTheme {
                RegistrationSuccessScreen(
                    onNavigateToChatList = { navigationTriggered = true }
                )
            }
        }

        // Click continue button
        composeTestRule.onNodeWithText("Continue to Chat List").performClick()
        
        // Verify navigation was triggered
        assert(navigationTriggered)
    }

    @Test
    fun registrationSuccessScreen_displaysAutoRedirectMessage() {
        composeTestRule.setContent {
            CareCommsTheme {
                RegistrationSuccessScreen(
                    onNavigateToChatList = {}
                )
            }
        }

        // Verify auto-redirect message is displayed
        composeTestRule.onNodeWithText("Automatically redirecting in a few seconds...").assertIsDisplayed()
    }
}