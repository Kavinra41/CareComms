package com.carecomms.android.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.screens.ProfileScreen
import com.carecomms.android.ui.theme.CareCommsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockCarerId = "test-carer-123"

    @Test
    fun profileScreen_displaysCorrectContent() {
        composeTestRule.setContent {
            CareCommsTheme {
                ProfileScreen(
                    carerId = mockCarerId,
                    onLogout = {}
                )
            }
        }

        // Verify main content is displayed
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Professional Carer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Account Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Logout").assertIsDisplayed()
    }

    @Test
    fun profileScreen_displaysCarerId() {
        composeTestRule.setContent {
            CareCommsTheme {
                ProfileScreen(
                    carerId = mockCarerId,
                    onLogout = {}
                )
            }
        }

        // Verify carer ID is displayed (truncated)
        composeTestRule.onNodeWithText("ID: test-car...").assertIsDisplayed()
    }

    @Test
    fun profileScreen_displaysAccountSettings() {
        composeTestRule.setContent {
            CareCommsTheme {
                ProfileScreen(
                    carerId = mockCarerId,
                    onLogout = {}
                )
            }
        }

        // Verify account settings are displayed
        composeTestRule.onNodeWithText("• Notification preferences").assertIsDisplayed()
        composeTestRule.onNodeWithText("• Privacy settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("• App preferences").assertIsDisplayed()
    }

    @Test
    fun profileScreen_logoutButtonTriggersDialog() {
        composeTestRule.setContent {
            CareCommsTheme {
                ProfileScreen(
                    carerId = mockCarerId,
                    onLogout = {}
                )
            }
        }

        // Click logout button
        composeTestRule.onNodeWithText("Logout").performClick()
        
        // Verify confirmation dialog appears
        composeTestRule.onNodeWithText("Confirm Logout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to logout?").assertIsDisplayed()
    }

    @Test
    fun profileScreen_logoutDialogCanBeCancelled() {
        composeTestRule.setContent {
            CareCommsTheme {
                ProfileScreen(
                    carerId = mockCarerId,
                    onLogout = {}
                )
            }
        }

        // Click logout button to open dialog
        composeTestRule.onNodeWithText("Logout").performClick()
        
        // Click cancel
        composeTestRule.onNodeWithText("Cancel").performClick()
        
        // Verify dialog is dismissed
        composeTestRule.onNodeWithText("Confirm Logout").assertDoesNotExist()
    }

    @Test
    fun profileScreen_logoutDialogConfirmsLogout() {
        var logoutCalled = false
        
        composeTestRule.setContent {
            CareCommsTheme {
                ProfileScreen(
                    carerId = mockCarerId,
                    onLogout = { logoutCalled = true }
                )
            }
        }

        // Click logout button to open dialog
        composeTestRule.onNodeWithText("Logout").performClick()
        
        // Click logout in dialog
        composeTestRule.onAllNodesWithText("Logout")[1].performClick() // Second "Logout" is in dialog
        
        // Verify logout callback was called
        assert(logoutCalled)
    }

    @Test
    fun profileScreen_hasCorrectStyling() {
        composeTestRule.setContent {
            CareCommsTheme {
                ProfileScreen(
                    carerId = mockCarerId,
                    onLogout = {}
                )
            }
        }

        // Verify profile icon is displayed
        composeTestRule.onNodeWithContentDescription("Profile").assertIsDisplayed()
        
        // Verify logout button has correct icon
        composeTestRule.onNodeWithContentDescription(null).assertExists() // Logout icon
    }
}