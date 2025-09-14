package com.carecomms.android.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.screens.LoginScreen
import com.carecomms.android.ui.theme.CareCommsTheme
import com.carecomms.presentation.auth.AuthViewState
import com.carecomms.presentation.auth.AuthAction
import com.carecomms.presentation.state.AuthState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysCorrectly() {
        composeTestRule.setContent {
            CareCommsTheme {
                LoginScreen(
                    state = AuthViewState(),
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        // Verify header elements
        composeTestRule.onNodeWithText("← Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        
        // Verify welcome text
        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please sign in to your account").assertIsDisplayed()
        
        // Verify form fields
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        
        // Verify sign in button
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
        
        // Verify help text
        composeTestRule.onNodeWithText("Having trouble signing in?", substring = true).assertIsDisplayed()
    }

    @Test
    fun loginScreen_backButtonWorks() {
        var backClicked = false

        composeTestRule.setContent {
            CareCommsTheme {
                LoginScreen(
                    state = AuthViewState(),
                    onAction = {},
                    onBackClick = { backClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("← Back").performClick()
        assert(backClicked)
    }

    @Test
    fun loginScreen_emailInputWorks() {
        var lastAction: AuthAction? = null

        composeTestRule.setContent {
            CareCommsTheme {
                LoginScreen(
                    state = AuthViewState(),
                    onAction = { lastAction = it },
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        
        assert(lastAction is AuthAction.UpdateEmail)
        assert((lastAction as AuthAction.UpdateEmail).email == "test@example.com")
    }

    @Test
    fun loginScreen_passwordInputWorks() {
        var lastAction: AuthAction? = null

        composeTestRule.setContent {
            CareCommsTheme {
                LoginScreen(
                    state = AuthViewState(),
                    onAction = { lastAction = it },
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        
        assert(lastAction is AuthAction.UpdatePassword)
        assert((lastAction as AuthAction.UpdatePassword).password == "password123")
    }

    @Test
    fun loginScreen_signInButtonDisabledWhenEmpty() {
        composeTestRule.setContent {
            CareCommsTheme {
                LoginScreen(
                    state = AuthViewState(email = "", password = ""),
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Sign In").assertIsNotEnabled()
    }

    @Test
    fun loginScreen_signInButtonEnabledWhenFieldsFilled() {
        composeTestRule.setContent {
            CareCommsTheme {
                LoginScreen(
                    state = AuthViewState(email = "test@example.com", password = "password123"),
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Sign In").assertIsEnabled()
    }

    @Test
    fun loginScreen_showsLoadingState() {
        composeTestRule.setContent {
            CareCommsTheme {
                LoginScreen(
                    state = AuthViewState(
                        email = "test@example.com",
                        password = "password123",
                        isLoading = true
                    ),
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        // Button should be disabled when loading
        composeTestRule.onNodeWithText("Sign In").assertIsNotEnabled()
        
        // Loading indicator should be visible
        composeTestRule.onNode(hasContentDescription("Loading") or hasTestTag("loading")).assertExists()
    }

    @Test
    fun loginScreen_showsErrorMessage() {
        val errorMessage = "Invalid email or password"
        
        composeTestRule.setContent {
            CareCommsTheme {
                LoginScreen(
                    state = AuthViewState(
                        email = "test@example.com",
                        password = "password123",
                        error = errorMessage
                    ),
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsFieldErrors() {
        composeTestRule.setContent {
            CareCommsTheme {
                LoginScreen(
                    state = AuthViewState(
                        emailError = "Invalid email format",
                        passwordError = "Password too short"
                    ),
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Invalid email format").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password too short").assertIsDisplayed()
    }

    @Test
    fun loginScreen_passwordVisibilityToggle() {
        composeTestRule.setContent {
            CareCommsTheme {
                LoginScreen(
                    state = AuthViewState(password = "password123"),
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        // Find and click the password visibility toggle
        composeTestRule.onNodeWithContentDescription("Show password").performClick()
        
        // After clicking, it should show "Hide password"
        composeTestRule.onNodeWithContentDescription("Hide password").assertExists()
    }

    @Test
    fun loginScreen_accessibilityFeatures() {
        composeTestRule.setContent {
            CareCommsTheme {
                LoginScreen(
                    state = AuthViewState(),
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        // Verify large touch targets (56.dp minimum)
        composeTestRule.onNodeWithText("Sign In").assertHeightIsAtLeast(56.dp)
        
        // Verify icons have content descriptions
        composeTestRule.onNodeWithContentDescription("Email").assertExists()
        composeTestRule.onNodeWithContentDescription("Password").assertExists()
    }
}