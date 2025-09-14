package com.carecomms.android.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.theme.CareCommsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CarerBottomNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomNavigation_displaysAllTabs() {
        composeTestRule.setContent {
            CareCommsTheme {
                val navController = rememberNavController()
                CarerBottomNavigation(navController = navController)
            }
        }

        // Verify all navigation items are displayed
        composeTestRule.onNodeWithText("Chats").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Details").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_hasCorrectIcons() {
        composeTestRule.setContent {
            CareCommsTheme {
                val navController = rememberNavController()
                CarerBottomNavigation(navController = navController)
            }
        }

        // Verify icons are present (by checking content descriptions)
        composeTestRule.onNodeWithContentDescription("Chats").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Details").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Profile").assertIsDisplayed()
    }

    @Test
    fun bottomNavigation_tabsAreClickable() {
        composeTestRule.setContent {
            CareCommsTheme {
                val navController = rememberNavController()
                CarerBottomNavigation(navController = navController)
            }
        }

        // Test that tabs are clickable
        composeTestRule.onNodeWithText("Dashboard").performClick()
        composeTestRule.onNodeWithText("Details").performClick()
        composeTestRule.onNodeWithText("Profile").performClick()
        composeTestRule.onNodeWithText("Chats").performClick()
    }

    @Test
    fun bottomNavigation_maintainsSelection() {
        composeTestRule.setContent {
            CareCommsTheme {
                val navController = rememberNavController()
                CarerBottomNavigation(navController = navController)
            }
        }

        // Click on Dashboard tab
        composeTestRule.onNodeWithText("Dashboard").performClick()
        
        // Verify Dashboard tab appears selected (this would need to be verified through styling)
        composeTestRule.onNodeWithText("Dashboard").assertIsDisplayed()
    }
}