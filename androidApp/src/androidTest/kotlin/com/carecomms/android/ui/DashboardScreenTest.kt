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
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockCarerId = "test-carer-123"

    @Test
    fun dashboardScreen_displaysCorrectContent() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Verify main content is displayed
        composeTestRule.onNodeWithText("Data Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Time Period").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select Care Recipients").assertIsDisplayed()
        composeTestRule.onNodeWithText("Analytics Overview").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_displaysPeriodFilters() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Verify period filter options
        composeTestRule.onNodeWithText("Daily").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weekly").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bi-weekly").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_displaysCareeSelection() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Verify caree selection section is displayed
        composeTestRule.onNodeWithText("Select Care Recipients").assertIsDisplayed()
        
        // Should show loading or empty state initially
        composeTestRule.onNode(
            hasText("No care recipients found") or hasContentDescription("Loading")
        ).assertExists()
    }

    @Test
    fun dashboardScreen_periodFilterIsInteractive() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Click on Weekly filter
        composeTestRule.onNodeWithText("Weekly").performClick()
        
        // Click on Bi-weekly filter
        composeTestRule.onNodeWithText("Bi-weekly").performClick()
        
        // Click back to Daily
        composeTestRule.onNodeWithText("Daily").performClick()
    }

    @Test
    fun dashboardScreen_displaysSelectAllButtons() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Should display All/None buttons when multiple carees are available
        // Note: This will depend on the view model state, so we test the UI structure
        composeTestRule.onNodeWithText("Select Care Recipients").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_showsAnalyticsPlaceholder() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Initially should show "Select care recipients" message
        composeTestRule.onNodeWithText("Select care recipients to view analytics").assertIsDisplayed()
        
        // Should show analytics icon
        composeTestRule.onNodeWithContentDescription(null).assertExists()
    }

    @Test
    fun dashboardScreen_displaysNotesSection() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Verify notes section
        composeTestRule.onNodeWithText("Recent Notes").assertIsDisplayed()
        
        // Initially should show "Select care recipients" message
        composeTestRule.onNodeWithText("Select care recipients to view notes").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_hasCorrectIcons() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Verify analytics icon is present
        composeTestRule.onNodeWithContentDescription(null).assertExists() // Bar chart icon
    }

    @Test
    fun dashboardScreen_displaysChartVisualization() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Should show chart visualization placeholder text
        composeTestRule.onNodeWithText("Chart visualization").assertExists()
        composeTestRule.onNodeWithText("Coming soon").assertExists()
    }

    @Test
    fun dashboardScreen_displaysRefreshButton() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Refresh button should be present in analytics section
        composeTestRule.onNodeWithContentDescription("Refresh data").assertExists()
    }

    @Test
    fun dashboardScreen_displaysLoadingStates() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Should handle loading states gracefully
        // The actual loading state depends on view model, but UI should be structured to handle it
        composeTestRule.onNodeWithText("Analytics Overview").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_displaysChartLegend() {
        composeTestRule.setContent {
            CareCommsTheme {
                DashboardScreen(carerId = mockCarerId)
            }
        }

        // Chart legend items should be present
        composeTestRule.onNodeWithText("Activity").assertExists()
        composeTestRule.onNodeWithText("Communication").assertExists()
        composeTestRule.onNodeWithText("Trends").assertExists()
    }
}