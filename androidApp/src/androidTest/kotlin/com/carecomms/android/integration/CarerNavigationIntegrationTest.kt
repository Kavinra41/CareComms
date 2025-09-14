package com.carecomms.android.integration

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.navigation.CarerNavigation
import com.carecomms.android.ui.theme.CareCommsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CarerNavigationIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockCarerId = "test-carer-123"

    @Test
    fun carerNavigation_completeNavigationFlow() {
        var logoutCalled = false
        
        composeTestRule.setContent {
            CareCommsTheme {
                CarerNavigation(
                    carerId = mockCarerId,
                    onLogout = { logoutCalled = true }
                )
            }
        }

        // Start on chat list
        composeTestRule.onNodeWithText("Chat List").assertIsDisplayed()
        
        // Navigate to Dashboard
        composeTestRule.onNodeWithText("Dashboard").performClick()
        composeTestRule.onNodeWithText("Data Dashboard").assertIsDisplayed()
        
        // Navigate to Details Tree
        composeTestRule.onNodeWithText("Details").performClick()
        composeTestRule.onNodeWithText("Details Tree").assertIsDisplayed()
        
        // Navigate to Profile
        composeTestRule.onNodeWithText("Profile").performClick()
        composeTestRule.onNodeWithText("Professional Carer").assertIsDisplayed()
        
        // Test logout functionality
        composeTestRule.onNodeWithText("Logout").performClick()
        composeTestRule.onNodeWithText("Confirm Logout").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Logout")[1].performClick() // Confirm logout
        
        // Verify logout was called
        assert(logoutCalled)
    }

    @Test
    fun carerNavigation_statePreservationAcrossScreens() {
        composeTestRule.setContent {
            CareCommsTheme {
                CarerNavigation(
                    carerId = mockCarerId,
                    onLogout = {}
                )
            }
        }

        // Go to Dashboard and interact with it
        composeTestRule.onNodeWithText("Dashboard").performClick()
        composeTestRule.onNodeWithText("Alice Johnson").performClick() // Select a caree
        
        // Navigate away and back
        composeTestRule.onNodeWithText("Details").performClick()
        composeTestRule.onNodeWithText("Dashboard").performClick()
        
        // State should be preserved (Alice Johnson should still be selected)
        composeTestRule.onNodeWithText("Communication Frequency").assertIsDisplayed()
    }

    @Test
    fun carerNavigation_detailsTreeInteraction() {
        composeTestRule.setContent {
            CareCommsTheme {
                CarerNavigation(
                    carerId = mockCarerId,
                    onLogout = {}
                )
            }
        }

        // Navigate to Details Tree
        composeTestRule.onNodeWithText("Details").performClick()
        
        // Expand Alice Johnson
        composeTestRule.onNodeWithText("Alice Johnson").performClick()
        composeTestRule.onNodeWithText("Health Information").assertIsDisplayed()
        
        // Expand Health Information
        composeTestRule.onNodeWithText("Health Information").performClick()
        composeTestRule.onNodeWithText("Medications").assertIsDisplayed()
        
        // Navigate away and back to verify state preservation
        composeTestRule.onNodeWithText("Profile").performClick()
        composeTestRule.onNodeWithText("Details").performClick()
        
        // Alice Johnson should still be expanded
        composeTestRule.onNodeWithText("Health Information").assertIsDisplayed()
    }

    @Test
    fun carerNavigation_dashboardFiltering() {
        composeTestRule.setContent {
            CareCommsTheme {
                CarerNavigation(
                    carerId = mockCarerId,
                    onLogout = {}
                )
            }
        }

        // Navigate to Dashboard
        composeTestRule.onNodeWithText("Dashboard").performClick()
        
        // Change time period
        composeTestRule.onNodeWithText("Weekly").performClick()
        
        // Select multiple carees
        composeTestRule.onNodeWithText("Alice Johnson").performClick()
        composeTestRule.onNodeWithText("Bob Smith").performClick()
        
        // Verify analytics are shown
        composeTestRule.onNodeWithText("Communication Frequency").assertIsDisplayed()
        composeTestRule.onNodeWithText("Response Time").assertIsDisplayed()
        
        // Navigate away and back
        composeTestRule.onNodeWithText("Chats").performClick()
        composeTestRule.onNodeWithText("Dashboard").performClick()
        
        // Settings should be preserved
        composeTestRule.onNodeWithText("Communication Frequency").assertIsDisplayed()
    }

    @Test
    fun carerNavigation_bottomNavVisibilityRules() {
        composeTestRule.setContent {
            CareCommsTheme {
                CarerNavigation(
                    carerId = mockCarerId,
                    onLogout = {}
                )
            }
        }

        // Bottom nav should be visible on main screens
        composeTestRule.onNodeWithText("Chats").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Details").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
        
        // Navigate to different screens and verify bottom nav persists
        composeTestRule.onNodeWithText("Dashboard").performClick()
        composeTestRule.onNodeWithText("Chats").assertIsDisplayed() // Still visible
        
        composeTestRule.onNodeWithText("Details").performClick()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed() // Still visible
        
        composeTestRule.onNodeWithText("Profile").performClick()
        composeTestRule.onNodeWithText("Details").assertIsDisplayed() // Still visible
    }

    @Test
    fun carerNavigation_profileLogoutFlow() {
        var logoutCalled = false
        
        composeTestRule.setContent {
            CareCommsTheme {
                CarerNavigation(
                    carerId = mockCarerId,
                    onLogout = { logoutCalled = true }
                )
            }
        }

        // Navigate to Profile
        composeTestRule.onNodeWithText("Profile").performClick()
        
        // Verify profile content
        composeTestRule.onNodeWithText("Professional Carer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Account Settings").assertIsDisplayed()
        
        // Test logout cancellation
        composeTestRule.onNodeWithText("Logout").performClick()
        composeTestRule.onNodeWithText("Cancel").performClick()
        assert(!logoutCalled) // Should not have logged out
        
        // Test actual logout
        composeTestRule.onNodeWithText("Logout").performClick()
        composeTestRule.onAllNodesWithText("Logout")[1].performClick() // Confirm
        assert(logoutCalled) // Should have logged out
    }
}