package com.carecomms.android.integration

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.screens.ChatContainer
import com.carecomms.android.ui.theme.CareCommsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun chatIntegration_fullUserFlow() {
        var backClicked = false

        composeTestRule.setContent {
            CareCommsTheme {
                ChatContainer(
                    chatId = "test-chat-id",
                    currentUserId = "test-user-id",
                    otherUserName = "Test User",
                    onBackClick = { backClicked = true }
                )
            }
        }

        // Wait for the screen to load
        composeTestRule.waitForIdle()

        // Verify the chat screen is displayed
        composeTestRule.onNodeWithText("Test User").assertIsDisplayed()

        // Test message input
        composeTestRule.onNodeWithText("Type a message...")
            .performTextInput("Hello, this is a test message!")

        // Verify the message appears in the input field
        composeTestRule.onNodeWithText("Hello, this is a test message!")
            .assertIsDisplayed()

        // Test send button becomes enabled
        composeTestRule.onNodeWithContentDescription("Send message")
            .assertIsEnabled()

        // Test back navigation
        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()

        assert(backClicked)
    }

    @Test
    fun chatIntegration_messageInputValidation() {
        composeTestRule.setContent {
            CareCommsTheme {
                ChatContainer(
                    chatId = "test-chat-id",
                    currentUserId = "test-user-id",
                    otherUserName = "Test User",
                    onBackClick = {}
                )
            }
        }

        // Wait for the screen to load
        composeTestRule.waitForIdle()

        // Verify send button is initially disabled
        composeTestRule.onNodeWithContentDescription("Send message")
            .assertIsNotEnabled()

        // Type a message
        composeTestRule.onNodeWithText("Type a message...")
            .performTextInput("Test")

        // Verify send button becomes enabled
        composeTestRule.onNodeWithContentDescription("Send message")
            .assertIsEnabled()

        // Clear the message
        composeTestRule.onNodeWithText("Test")
            .performTextClearance()

        // Verify send button becomes disabled again
        composeTestRule.onNodeWithContentDescription("Send message")
            .assertIsNotEnabled()
    }

    @Test
    fun chatIntegration_keyboardInteraction() {
        composeTestRule.setContent {
            CareCommsTheme {
                ChatContainer(
                    chatId = "test-chat-id",
                    currentUserId = "test-user-id",
                    otherUserName = "Test User",
                    onBackClick = {}
                )
            }
        }

        // Wait for the screen to load
        composeTestRule.waitForIdle()

        // Type a message
        composeTestRule.onNodeWithText("Type a message...")
            .performTextInput("Test message for keyboard")

        // Test IME action (send via keyboard)
        composeTestRule.onNodeWithText("Test message for keyboard")
            .performImeAction()

        // The message should be processed (in a real app, it would be sent)
        // For now, we just verify the interaction doesn't crash
        composeTestRule.waitForIdle()
    }

    @Test
    fun chatIntegration_longMessageHandling() {
        composeTestRule.setContent {
            CareCommsTheme {
                ChatContainer(
                    chatId = "test-chat-id",
                    currentUserId = "test-user-id",
                    otherUserName = "Test User",
                    onBackClick = {}
                )
            }
        }

        // Wait for the screen to load
        composeTestRule.waitForIdle()

        // Type a very long message
        val longMessage = "This is a very long message that should test the text field's ability to handle multiple lines and ensure that the UI remains responsive and properly formatted when dealing with extensive text input from the user."

        composeTestRule.onNodeWithText("Type a message...")
            .performTextInput(longMessage)

        // Verify the long message is handled properly
        composeTestRule.onNodeWithText(longMessage)
            .assertIsDisplayed()

        // Verify send button is still enabled
        composeTestRule.onNodeWithContentDescription("Send message")
            .assertIsEnabled()
    }

    @Test
    fun chatIntegration_multilineMessageHandling() {
        composeTestRule.setContent {
            CareCommsTheme {
                ChatContainer(
                    chatId = "test-chat-id",
                    currentUserId = "test-user-id",
                    otherUserName = "Test User",
                    onBackClick = {}
                )
            }
        }

        // Wait for the screen to load
        composeTestRule.waitForIdle()

        // Type a multiline message
        val multilineMessage = "Line 1\nLine 2\nLine 3"

        composeTestRule.onNodeWithText("Type a message...")
            .performTextInput(multilineMessage)

        // Verify the multiline message is handled properly
        composeTestRule.onNodeWithText(multilineMessage)
            .assertIsDisplayed()

        // Verify send button is enabled
        composeTestRule.onNodeWithContentDescription("Send message")
            .assertIsEnabled()
    }
}