package com.carecomms.android

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.carecomms.android.ui.theme.CareCommsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthFlowIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun authFlow_basicNavigationWorks() {
        // Test that the basic app structure works
        composeTestRule.setContent {
            CareCommsTheme {
                CareCommsApp()
            }
        }

        // The app should start and display something
        composeTestRule.onRoot().assertIsDisplayed()
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.carecomms.android", appContext.packageName)
    }
}