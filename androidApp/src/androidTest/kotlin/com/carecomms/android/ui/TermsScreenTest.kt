package com.carecomms.android.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.screens.TermsScreen
import com.carecomms.android.ui.theme.CareCommsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TermsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun termsScreen_displaysCorrectly() {
        composeTestRule.setContent {
            CareCommsTheme {
                TermsScreen(
                    onAccept = {},
                    onDecline = {}
                )
            }
        }

        // Verify title is displayed
        composeTestRule.onNodeWithText("Terms and Conditions").assertIsDisplayed()
        
        // Verify buttons are displayed
        composeTestRule.onNodeWithText("Accept").assertIsDisplayed()
        composeTestRule.onNodeWithText("Decline").assertIsDisplayed()
        
        // Verify scroll instruction is displayed initially
        composeTestRule.onNodeWithText("Please scroll to the bottom to continue").assertIsDisplayed()
    }

    @Test
    fun termsScreen_acceptButtonInitiallyDisabled() {
        composeTestRule.setContent {
            CareCommsTheme {
                TermsScreen(
                    onAccept = {},
                    onDecline = {}
                )
            }
        }

        // Accept button should be disabled initially
        composeTestRule.onNodeWithText("Accept").assertIsNotEnabled()
        
        // Decline button should always be enabled
        composeTestRule.onNodeWithText("Decline").assertIsEnabled()
    }

    @Test
    fun termsScreen_declineButtonWorks() {
        var declineClicked = false

        composeTestRule.setContent {
            CareCommsTheme {
                TermsScreen(
                    onAccept = {},
                    onDecline = { declineClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Decline").performClick()
        assert(declineClicked)
    }

    @Test
    fun termsScreen_scrollableContent() {
        composeTestRule.setContent {
            CareCommsTheme {
                TermsScreen(
                    onAccept = {},
                    onDecline = {}
                )
            }
        }

        // Verify that terms content is present
        composeTestRule.onNodeWithText("Welcome to CareComms", substring = true).assertIsDisplayed()
        
        // Verify that we can scroll (content should be scrollable)
        composeTestRule.onNodeWithText("Welcome to CareComms", substring = true)
            .performScrollTo()
    }
}