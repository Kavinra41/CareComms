package com.carecomms.android.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.screens.DashboardScreen
import com.carecomms.android.ui.theme.CareCommsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockCarerId = "test-carer-123"

    @Test
    fun dashboardScreen_rendersWithoutCrashing() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Verify the screen renders without crashing
        composeTestRule.onNodeWithText("Data Dashboard").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_displaysAllMainSections() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Verify all main sections are present
        composeTestRule.onNodeWithText("Data Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Time Period").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select Care Recipients").assertIsDisplayed()
        composeTestRule.onNodeWithText("Analytics Overview").assertIsDisplayed()
        composeTestRule.onNodeWithText("Recent Notes").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_periodSelectionWorks() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Test period selection
        composeTestRule.onNodeWithText("Daily").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weekly").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bi-weekly").assertIsDisplayed()

        // Click on different periods
        composeTestRule.onNodeWithText("Weekly").performClick()
        composeTestRule.onNodeWithText("Bi-weekly").performClick()
        composeTestRule.onNodeWithText("Daily").performClick()
    }

    @Test
    fun dashboardScreen_displaysChartPlaceholder() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Should show chart placeholder
        composeTestRule.onNodeWithText("Chart visualization").assertExists()
        composeTestRule.onNodeWithText("Coming soon").assertExists()
        
        // Should show chart legend
        composeTestRule.onNodeWithText("Activity").assertExists()
        composeTestRule.onNodeWithText("Communication").assertExists()
        composeTestRule.onNodeWithText("Trends").assertExists()
    }

    @Test
    fun dashboardScreen_handlesEmptyState() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Should show empty state messages
        composeTestRule.onNodeWithText("Select care recipients to view analytics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select care recipients to view notes").assertIsDisplayed()
    }
}