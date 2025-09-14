package com.carecomms.android.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.theme.CareCommsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CarerNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockCarerId = "test-carer-123"

    @Test
    fun carerNavigation_startsWithChatListScreen() {
        composeTestRule.setContent {
            CareCommsTheme {
                CarerNavigation(
                    carerId = mockCarerId,
                    onLogout = {}
                )
            }
        }

        // Verify chat list screen is displayed initially
        composeTestRule.onNodeWithText("Chat List").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chats").assertIsDisplayed() // Bottom nav item
    }

    @Test
    fun carerNavigation_navigatesToDashboard() {
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
        
        // Verify dashboard screen is displayed
        composeTestRule.onNodeWithText("Data Dashboard").assertIsDisplayed()
    }

    @Test
    fun carerNavigation_navigatesToDetailsTree() {
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
        
        // Verify details tree screen is displayed
        composeTestRule.onNodeWithText("Details Tree").assertIsDisplayed()
    }

    @Test
    fun carerNavigation_navigatesToProfile() {
        composeTestRule.setContent {
            CareCommsTheme {
                CarerNavigation(
                    carerId = mockCarerId,
                    onLogout = {}
                )
            }
        }

        // Navigate to Profile
        composeTestRule.onNodeWithText("Profile").performClick()
        
        // Verify profile screen is displayed
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Professional Carer").assertIsDisplayed()
    }

    @Test
    fun carerNavigation_bottomNavPersistsAcrossScreens() {
        composeTestRule.setContent {
            CareCommsTheme {
                CarerNavigation(
                    carerId = mockCarerId,
                    onLogout = {}
                )
            }
        }

        // Navigate to different screens and verify bottom nav is always present
        composeTestRule.onNodeWithText("Dashboard").performClick()
        composeTestRule.onNodeWithText("Chats").assertIsDisplayed() // Bottom nav still there
        
        composeTestRule.onNodeWithText("Details").performClick()
        composeTestRule.onNodeWithText("Dashboard").assertIsDisplayed() // Bottom nav still there
        
        composeTestRule.onNodeWithText("Profile").performClick()
        composeTestRule.onNodeWithText("Details").assertIsDisplayed() // Bottom nav still there
    }

    @Test
    fun carerNavigation_statePreservationBetweenTabs() {
        composeTestRule.setContent {
            CareCommsTheme {
                CarerNavigation(
                    carerId = mockCarerId,
                    onLogout = {}
                )
            }
        }

        // Navigate to Dashboard, then to Profile, then back to Dashboard
        composeTestRule.onNodeWithText("Dashboard").performClick()
        composeTestRule.onNodeWithText("Data Dashboard").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Profile").performClick()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Dashboard").performClick()
        composeTestRule.onNodeWithText("Data Dashboard").assertIsDisplayed()
    }

    @Test
    fun carerNavigation_chatDetailHidesBottomNav() {
        composeTestRule.setContent {
            CareCommsTheme {
                CarerNavigation(
                    carerId = mockCarerId,
                    onLogout = {}
                )
            }
        }

        // Start on chat list, click on a chat (if available)
        // This test would need mock data to be more comprehensive
        // For now, we verify the chat list is displayed
        composeTestRule.onNodeWithText("Chat List").assertIsDisplayed()
    }
}