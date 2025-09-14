package com.carecomms.android.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.screens.CareeRegistrationScreen
import com.carecomms.android.ui.theme.CareCommsTheme
import com.carecomms.data.models.CarerInfo
import com.carecomms.presentation.registration.CareeRegistrationState
import com.carecomms.presentation.registration.CareeRegistrationIntent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CareeRegistrationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun careeRegistrationScreen_displaysCorrectTitle() {
        val state = CareeRegistrationState()
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationScreen(
                    state = state,
                    onIntent = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Join CareComms")
            .assertIsDisplayed()
    }

    @Test
    fun careeRegistrationScreen_displaysCarerInformation_whenCarerInfoProvided() {
        val carerInfo = CarerInfo(
            id = "carer123",
            name = "John Smith",
            location = "New York, NY",
            phoneNumber = "+1234567890"
        )
        
        val state = CareeRegistrationState(
            carerInfo = carerInfo,
            isInvitationValid = true
        )
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationScreen(
                    state = state,
                    onIntent = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("You've been invited by:")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("John Smith")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Location: New York, NY")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Phone: +1234567890")
            .assertIsDisplayed()
    }

    @Test
    fun careeRegistrationScreen_displaysAllFormFields() {
        val state = CareeRegistrationState()
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationScreen(
                    state = state,
                    onIntent = {},
                    onBackClick = {}
                )
            }
        }

        // Check email field
        composeTestRule
            .onNodeWithText("Email Address")
            .assertIsDisplayed()
        
        // Check password fields
        composeTestRule
            .onNodeWithText("Password")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Confirm Password")
            .assertIsDisplayed()
        
        // Check personal information fields
        composeTestRule
            .onNodeWithText("First Name")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Last Name")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Date of Birth (YYYY-MM-DD)")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Address (Optional)")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Emergency Contact (Optional)")
            .assertIsDisplayed()
        
        // Check health information field
        composeTestRule
            .onNodeWithText("Health Conditions & Notes")
            .assertIsDisplayed()
    }

    @Test
    fun careeRegistrationScreen_emailInput_triggersCorrectIntent() {
        val state = CareeRegistrationState()
        var capturedIntent: CareeRegistrationIntent? = null
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationScreen(
                    state = state,
                    onIntent = { capturedIntent = it },
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Email Address")
            .performTextInput("test@example.com")

        assert(capturedIntent is CareeRegistrationIntent.UpdateEmail)
        assert((capturedIntent as CareeRegistrationIntent.UpdateEmail).email == "test@example.com")
    }

    @Test
    fun careeRegistrationScreen_passwordInput_triggersCorrectIntent() {
        val state = CareeRegistrationState()
        var capturedIntent: CareeRegistrationIntent? = null
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationScreen(
                    state = state,
                    onIntent = { capturedIntent = it },
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("password123")

        assert(capturedIntent is CareeRegistrationIntent.UpdatePassword)
        assert((capturedIntent as CareeRegistrationIntent.UpdatePassword).password == "password123")
    }

    @Test
    fun careeRegistrationScreen_firstNameInput_triggersCorrectIntent() {
        val state = CareeRegistrationState()
        var capturedIntent: CareeRegistrationIntent? = null
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationScreen(
                    state = state,
                    onIntent = { capturedIntent = it },
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("First Name")
            .performTextInput("Jane")

        assert(capturedIntent is CareeRegistrationIntent.UpdateFirstName)
        assert((capturedIntent as CareeRegistrationIntent.UpdateFirstName).firstName == "Jane")
    }

    @Test
    fun careeRegistrationScreen_healthInfoInput_triggersCorrectIntent() {
        val state = CareeRegistrationState()
        var capturedIntent: CareeRegistrationIntent? = null
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationScreen(
                    state = state,
                    onIntent = { capturedIntent = it },
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Health Conditions & Notes")
            .performTextInput("Diabetes, High Blood Pressure")

        assert(capturedIntent is CareeRegistrationIntent.UpdateHealthInfo)
        assert((capturedIntent as CareeRegistrationIntent.UpdateHealthInfo).healthInfo == "Diabetes, High Blood Pressure")
    }

    @Test
    fun careeRegistrationScreen_registerButton_isDisabled_whenFormInvalid() {
        val state = CareeRegistrationState(
            email = "test@example.com",
            password = "password123",
            // Missing required fields
        )
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationScreen(
                    state = state,
                    onIntent = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Create Account")
            .assertIsNotEnabled()
    }

    @Test
    fun careeRegistrationScreen_registerButton_isEnabled_whenFormValid() {
        val carerInfo = CarerInfo(
            id = "carer123",
            name = "John Smith",
            location = "New York, NY",
            phoneNumber = "+1234567890"
        )
        
        val state = CareeRegistrationState(
            email = "test@example.com",
            password = "password123",
            confirmPassword = "password123",
            firstName = "Jane",
            lastName = "Doe",
            dateOfBirth = "1990-01-01",
            healthInfo = "No major health issues",
            isInvitationValid = true,
            carerInfo = carerInfo
        )
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationScreen(
                    state = state,
                    onIntent = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Create Account")
            .assertIsEnabled()
    }

    @Test
    fun careeRegistrationScreen_registerButton_triggersCorrectIntent() {
        val carerInfo = CarerInfo(
            id = "carer123",
            name = "John Smith",
            location = "New York, NY",
            phoneNumber = "+1234567890"
        )
        
        val state = CareeRegistrationState(
            email = "test@example.com",
            password = "password123",
            confirmPassword = "password123",
            firstName = "Jane",
            lastName = "Doe",
            dateOfBirth = "1990-01-01",
            healthInfo = "No major health issues",
            isInvitationValid = true,
            carerInfo = carerInfo
        )
        
        var capturedIntent: CareeRegistrationIntent? = null
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationScreen(
                    state = state,
                    onIntent = { capturedIntent = it },
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Create Account")
            .performClick()

        assert(capturedIntent is CareeRegistrationIntent.RegisterCaree)
    }

    @Test
    fun careeRegistrationScreen_showsLoadingIndicator_whenLoading() {
        val state = CareeRegistrationState(isLoading = true)
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationScreen(
                    state = state,
                    onIntent = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Loading")
            .assertIsDisplayed()
    }

    @Test
    fun careeRegistrationScreen_showsErrorMessage_whenErrorExists() {
        val state = CareeRegistrationState(
            errorMessage = "Registration failed. Please try again."
        )
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationScreen(
                    state = state,
                    onIntent = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Registration failed. Please try again.")
            .assertIsDisplayed()
    }

    @Test
    fun careeRegistrationScreen_showsValidationIndicator_whenValidatingInvitation() {
        val state = CareeRegistrationState(isValidatingInvitation = true)
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationScreen(
                    state = state,
                    onIntent = {},
                    onBackClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Validating invitation...")
            .assertIsDisplayed()
    }

    @Test
    fun careeRegistrationScreen_backButton_triggersBackClick() {
        val state = CareeRegistrationState()
        var backClicked = false
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationScreen(
                    state = state,
                    onIntent = {},
                    onBackClick = { backClicked = true }
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Back")
            .performClick()

        assert(backClicked)
    }

    @Test
    fun careeRegistrationScreen_passwordVisibilityToggle_worksCorrectly() {
        val state = CareeRegistrationState()
        
        composeTestRule.setContent {
            CareCommsTheme {
                CareeRegistrationScreen(
                    state = state,
                    onIntent = {},
                    onBackClick = {}
                )
            }
        }

        // Initially password should be hidden
        composeTestRule
            .onNodeWithContentDescription("Show password")
            .assertIsDisplayed()

        // Click to show password
        composeTestRule
            .onNodeWithContentDescription("Show password")
            .performClick()

        // Now hide password option should be available
        composeTestRule
            .onNodeWithContentDescription("Hide password")
            .assertIsDisplayed()
    }
}