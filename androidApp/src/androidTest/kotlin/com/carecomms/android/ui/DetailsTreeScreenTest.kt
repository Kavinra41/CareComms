package com.carecomms.android.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.screens.DetailsTreeScreen
import com.carecomms.android.ui.theme.CareCommsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailsTreeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockCarerId = "test-carer-123"

    @Test
    fun detailsTreeScreen_displaysCorrectInitialContent() {
        composeTestRule.setContent {
            CareCommsTheme {
                DetailsTreeScreen(carerId = mockCarerId)
            }
        }

        // Verify main content is displayed
        composeTestRule.onNodeWithText("Details Tree").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select a care recipient to explore their detailed information").assertIsDisplayed()
    }

    @Test
    fun detailsTreeScreen_displaysTileStyleCareeSelection() {
        composeTestRule.setContent {
            CareCommsTheme {
                DetailsTreeScreen(carerId = mockCarerId)
            }
        }

        // Verify caree tiles are displayed in grid format
        composeTestRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob Smith").assertIsDisplayed()
        composeTestRule.onNodeWithText("Eleanor Davis").assertIsDisplayed()
        composeTestRule.onNodeWithText("Margaret Wilson").assertIsDisplayed()
        
        // Verify age information is displayed
        composeTestRule.onNodeWithText("Age 78").assertIsDisplayed()
        composeTestRule.onNodeWithText("Age 82").assertIsDisplayed()
    }

    @Test
    fun detailsTreeScreen_careeTilesShowHealthConditions() {
        composeTestRule.setContent {
            CareCommsTheme {
                DetailsTreeScreen(carerId = mockCarerId)
            }
        }

        // Verify health conditions are displayed on tiles
        composeTestRule.onNodeWithText("Diabetes, Hypertension").assertIsDisplayed()
        composeTestRule.onNodeWithText("Arthritis").assertIsDisplayed()
        composeTestRule.onNodeWithText("Heart condition").assertIsDisplayed()
    }

    @Test
    fun detailsTreeScreen_careeTilesShowLastActivity() {
        composeTestRule.setContent {
            CareCommsTheme {
                DetailsTreeScreen(carerId = mockCarerId)
            }
        }

        // Verify last activity information is displayed
        composeTestRule.onNodeWithText("Last activity: 2 hours ago").assertIsDisplayed()
        composeTestRule.onNodeWithText("Last activity: 1 day ago").assertIsDisplayed()
        composeTestRule.onNodeWithText("Last activity: 3 hours ago").assertIsDisplayed()
    }

    @Test
    fun detailsTreeScreen_careeTileSelectionNavigatesToDetails() {
        composeTestRule.setContent {
            CareCommsTheme {
                DetailsTreeScreen(carerId = mockCarerId)
            }
        }

        // Click on Alice Johnson tile
        composeTestRule.onNodeWithText("Alice Johnson").performClick()
        
        // Should navigate to details view
        composeTestRule.onNodeWithText("Exploring details for Alice Johnson").assertIsDisplayed()
        composeTestRule.onNodeWithText("Back to Care Recipients").assertIsDisplayed()
        
        // Should show category nodes
        composeTestRule.onNodeWithText("Health Information").assertIsDisplayed()
        composeTestRule.onNodeWithText("Daily Activities").assertIsDisplayed()
        composeTestRule.onNodeWithText("Communication History").assertIsDisplayed()
        composeTestRule.onNodeWithText("Care Notes").assertIsDisplayed()
    }

    @Test
    fun detailsTreeScreen_backButtonReturnsToCareeSelection() {
        composeTestRule.setContent {
            CareCommsTheme {
                DetailsTreeScreen(carerId = mockCarerId)
            }
        }

        // Navigate to details
        composeTestRule.onNodeWithText("Alice Johnson").performClick()
        composeTestRule.onNodeWithText("Back to Care Recipients").assertIsDisplayed()
        
        // Click back button
        composeTestRule.onNodeWithContentDescription("Back to caree selection").performClick()
        
        // Should return to tile selection
        composeTestRule.onNodeWithText("Select a care recipient to explore their detailed information").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alice Johnson").assertIsDisplayed()
    }

    @Test
    fun detailsTreeScreen_accordionExpansionWorksCorrectly() {
        composeTestRule.setContent {
            CareCommsTheme {
                DetailsTreeScreen(carerId = mockCarerId)
            }
        }

        // Navigate to details
        composeTestRule.onNodeWithText("Alice Johnson").performClick()
        
        // Expand Health Information category
        composeTestRule.onNodeWithText("Health Information").performClick()
        
        // Should show health details
        composeTestRule.onNodeWithText("Medications").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vital Signs").assertIsDisplayed()
    }

    @Test
    fun detailsTreeScreen_nestedAccordionExpansion() {
        composeTestRule.setContent {
            CareCommsTheme {
                DetailsTreeScreen(carerId = mockCarerId)
            }
        }

        // Navigate to details and expand hierarchy
        composeTestRule.onNodeWithText("Alice Johnson").performClick()
        composeTestRule.onNodeWithText("Health Information").performClick()
        composeTestRule.onNodeWithText("Medications").performClick()
        
        // Should show medication items
        composeTestRule.onNodeWithText("Blood Pressure Medication").assertIsDisplayed()
        composeTestRule.onNodeWithText("Diabetes Medication").assertIsDisplayed()
        composeTestRule.onNodeWithText("Taken daily at 8 AM, 10mg dosage").assertIsDisplayed()
    }

    @Test
    fun detailsTreeScreen_accordionCollapseWorksCorrectly() {
        composeTestRule.setContent {
            CareCommsTheme {
                DetailsTreeScreen(carerId = mockCarerId)
            }
        }

        // Navigate to details and expand
        composeTestRule.onNodeWithText("Alice Johnson").performClick()
        composeTestRule.onNodeWithText("Health Information").performClick()
        composeTestRule.onNodeWithText("Medications").assertIsDisplayed()
        
        // Collapse Health Information
        composeTestRule.onNodeWithText("Health Information").performClick()
        composeTestRule.onNodeWithText("Medications").assertDoesNotExist()
    }

    @Test
    fun detailsTreeScreen_multipleAccordionSectionsCanBeExpanded() {
        composeTestRule.setContent {
            CareCommsTheme {
                DetailsTreeScreen(carerId = mockCarerId)
            }
        }

        // Navigate to details
        composeTestRule.onNodeWithText("Alice Johnson").performClick()
        
        // Expand multiple sections
        composeTestRule.onNodeWithText("Health Information").performClick()
        composeTestRule.onNodeWithText("Daily Activities").performClick()
        composeTestRule.onNodeWithText("Communication History").performClick()
        
        // All should be visible
        composeTestRule.onNodeWithText("Medications").assertIsDisplayed()
        composeTestRule.onNodeWithText("Exercise Routine").assertIsDisplayed()
        composeTestRule.onNodeWithText("Recent Messages").assertIsDisplayed()
    }

    @Test
    fun detailsTreeScreen_displaysItemDataCorrectly() {
        composeTestRule.setContent {
            CareCommsTheme {
                DetailsTreeScreen(carerId = mockCarerId)
            }
        }

        // Navigate to specific item data
        composeTestRule.onNodeWithText("Alice Johnson").performClick()
        composeTestRule.onNodeWithText("Communication History").performClick()
        composeTestRule.onNodeWithText("Recent Messages").performClick()
        
        // Should show communication details
        composeTestRule.onNodeWithText("Today: 5 messages").assertIsDisplayed()
        composeTestRule.onNodeWithText("Last message: 2 hours ago").assertIsDisplayed()
        composeTestRule.onNodeWithText("Yesterday: 3 messages").assertIsDisplayed()
    }

    @Test
    fun detailsTreeScreen_displaysFooterNote() {
        composeTestRule.setContent {
            CareCommsTheme {
                DetailsTreeScreen(carerId = mockCarerId)
            }
        }

        // Verify footer note about mock data
        composeTestRule.onNodeWithText("📋 This is mock data for demonstration").assertIsDisplayed()
    }

    @Test
    fun detailsTreeScreen_hierarchicalNavigationMaintainsState() {
        composeTestRule.setContent {
            CareCommsTheme {
                DetailsTreeScreen(carerId = mockCarerId)
            }
        }

        // Navigate to details and expand some sections
        composeTestRule.onNodeWithText("Bob Smith").performClick()
        composeTestRule.onNodeWithText("Health Information").performClick()
        
        // Go back to selection
        composeTestRule.onNodeWithContentDescription("Back to caree selection").performClick()
        
        // Navigate to same caree again
        composeTestRule.onNodeWithText("Bob Smith").performClick()
        
        // State should be reset (collapsed)
        composeTestRule.onNodeWithText("Medications").assertDoesNotExist()
    }

    @Test
    fun detailsTreeScreen_careNotesAccordionExpansion() {
        composeTestRule.setContent {
            CareCommsTheme {
                DetailsTreeScreen(carerId = mockCarerId)
            }
        }

        // Navigate to care notes
        composeTestRule.onNodeWithText("Alice Johnson").performClick()
        composeTestRule.onNodeWithText("Care Notes").performClick()
        composeTestRule.onNodeWithText("Care Observations").performClick()
        
        // Should show care note details
        composeTestRule.onNodeWithText("Latest Note").assertIsDisplayed()
        composeTestRule.onNodeWithText("Patient is doing well, good spirits today").assertIsDisplayed()
        composeTestRule.onNodeWithText("Previous Note").assertIsDisplayed()
    }
}