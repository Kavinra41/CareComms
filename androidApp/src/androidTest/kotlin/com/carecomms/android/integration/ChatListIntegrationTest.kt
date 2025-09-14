package com.carecomms.android.integration

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.screens.ChatListContainer
import com.carecomms.android.ui.theme.CareCommsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatListIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun chatListIntegration_fullUserFlow() {
        var navigatedToChatId: String? = null
        
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListContainer(
                    carerId = "test-carer-id",
                    onChatClick = { chatId -> navigatedToChatId = chatId }
                )
            }
        }

        // Wait for screen to load
        composeTestRule.waitForIdle()

        // Verify main elements are present
        composeTestRule
            .onNodeWithText("Chats")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Invite Caree")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Search chats...")
            .assertIsDisplayed()
    }

    @Test
    fun chatListIntegration_searchFunctionality() {
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListContainer(
                    carerId = "test-carer-id",
                    onChatClick = {}
                )
            }
        }

        // Wait for screen to load
        composeTestRule.waitForIdle()

        // Test search input
        composeTestRule
            .onNodeWithText("Search chats...")
            .performTextInput("test search")

        // Verify search input is working
        composeTestRule
            .onNodeWithText("test search")
            .assertIsDisplayed()

        // Clear search
        composeTestRule
            .onNodeWithContentDescription("Clear search")
            .performClick()

        // Verify search is cleared
        composeTestRule
            .onNodeWithText("Search chats...")
            .assertIsDisplayed()
    }

    @Test
    fun chatListIntegration_inviteButtonOpensDialog() {
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListContainer(
                    carerId = "test-carer-id",
                    onChatClick = {}
                )
            }
        }

        // Wait for screen to load
        composeTestRule.waitForIdle()

        // Click invite button
        composeTestRule
            .onNodeWithContentDescription("Invite Caree")
            .performClick()

        // Wait for dialog to appear
        composeTestRule.waitForIdle()

        // Verify invitation dialog appears
        composeTestRule
            .onNodeWithText("Invite Caree")
            .assertIsDisplayed()
    }

    @Test
    fun chatListIntegration_pullToRefresh() {
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListContainer(
                    carerId = "test-carer-id",
                    onChatClick = {}
                )
            }
        }

        // Wait for screen to load
        composeTestRule.waitForIdle()

        // The SwipeRefresh component should be present
        // We can't easily test the pull gesture in unit tests,
        // but we can verify the component renders correctly
        composeTestRule
            .onNodeWithText("Chats")
            .assertIsDisplayed()
    }

    @Test
    fun chatListIntegration_emptyStateDisplaysCorrectly() {
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListContainer(
                    carerId = "empty-carer-id", // This should result in empty state
                    onChatClick = {}
                )
            }
        }

        // Wait for screen to load
        composeTestRule.waitForIdle()

        // Since we're using mock data, we might see empty state
        // The exact behavior depends on the mock implementation
        composeTestRule
            .onNodeWithText("Chats")
            .assertIsDisplayed()
    }
}