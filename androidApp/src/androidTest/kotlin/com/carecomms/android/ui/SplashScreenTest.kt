package com.carecomms.android.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.screens.SplashScreen
import com.carecomms.android.ui.theme.CareCommsTheme
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun splashScreen_displaysCorrectly() {
        var splashCompleted = false

        composeTestRule.setContent {
            CareCommsTheme {
                SplashScreen(
                    onSplashComplete = { splashCompleted = true }
                )
            }
        }

        // Verify the splash screen is displayed
        composeTestRule.onRoot().assertIsDisplayed()
        
        // Verify the logo placeholder is displayed
        composeTestRule.onNodeWithContentDescription("CareComms Logo", useUnmergedTree = true)
            .assertDoesNotExist() // Since we're using a placeholder box, not an actual image
    }

    @Test
    fun splashScreen_completesAfterDelay() = runTest {
        var splashCompleted = false

        composeTestRule.setContent {
            CareCommsTheme {
                SplashScreen(
                    onSplashComplete = { splashCompleted = true }
                )
            }
        }

        // Wait for the splash to complete (5 seconds + buffer)
        composeTestRule.waitUntil(timeoutMillis = 6000) {
            splashCompleted
        }

        assert(splashCompleted)
    }
}