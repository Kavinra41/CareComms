package com.carecomms.android.integration

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.navigation.AuthNavigation
import com.carecomms.android.ui.theme.CareCommsTheme
import com.carecomms.data.models.CarerInfo
import com.carecomms.presentation.registration.CareeRegistrationState
import com.carecomms.presentation.auth.AuthViewModel
import com.carecomms.presentation.registration.CarerRegistrationViewModel
import com.carecomms.presentation.registration.CareeRegistrationViewModel
import com.carecomms.presentation.invitation.InvitationViewModel
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CareeRegistrationIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun careeRegistrationFlow_completeWorkflow_fromDeepLinkToSuccess() {
        val mockAuthViewModel: AuthViewModel = mockk(relaxed = true)
        val mockCarerRegistrationViewModel: CarerRegistrationViewModel = mockk(relaxed = true)
        val mockCareeRegistrationViewModel: CareeRegistrationViewModel = mockk(relaxed = true)
        val mockInvitationViewModel: InvitationViewModel = mockk(relaxed = true)
        
        val carerInfo = CarerInfo(
            id = "carer123",
            name = "Dr. Sarah Wilson",
            location = "Boston, MA",
            phoneNumber = "+1555123456"
        )
        
        // Initial state with valid invitation
        val initialState = CareeRegistrationState(
            invitationToken = "valid-token",
            carerInfo = carerInfo,
            isInvitationValid = true
        )
        
        val stateFlow = MutableStateFlow(initialState)
        every { mockCareeRegistrationViewModel.state } returns stateFlow
        every { mockInvitationViewModel.isValidInvitationUrl(any()) } returns true
        every { mockInvitationViewModel.extractTokenFromUrl(any()) } returns "valid-token"
        
        var homeNavigationCalled = false
        var homeUserType = ""
        
        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    deepLinkUrl = "carecomms://invite?token=valid-token",
                    onNavigateToHome = { userType ->
                        homeNavigationCalled = true
                        homeUserType = userType
                    }
                )
            }
        }

        // Step 1: Verify caree registration screen is displayed
        composeTestRule
            .onNodeWithText("Join CareComms")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Dr. Sarah Wilson")
            .assertIsDisplayed()

        // Step 2: Fill out the registration form
        composeTestRule
            .onNodeWithText("Email Address")
            .performTextInput("jane.doe@example.com")
        
        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("securePassword123")
        
        composeTestRule
            .onNodeWithText("Confirm Password")
            .performTextInput("securePassword123")
        
        composeTestRule
            .onNodeWithText("First Name")
            .performTextInput("Jane")
        
        composeTestRule
            .onNodeWithText("Last Name")
            .performTextInput("Doe")
        
        composeTestRule
            .onNodeWithText("Date of Birth (YYYY-MM-DD)")
            .performTextInput("1985-03-15")
        
        composeTestRule
            .onNodeWithText("Health Conditions & Notes")
            .performTextInput("Diabetes Type 2, takes medication daily")

        // Step 3: Update state to valid form
        val validFormState = initialState.copy(
            email = "jane.doe@example.com",
            password = "securePassword123",
            confirmPassword = "securePassword123",
            firstName = "Jane",
            lastName = "Doe",
            dateOfBirth = "1985-03-15",
            healthInfo = "Diabetes Type 2, takes medication daily"
        )
        stateFlow.value = validFormState

        // Step 4: Verify register button is enabled and click it
        composeTestRule
            .onNodeWithText("Create Account")
            .assertIsEnabled()
            .performClick()

        // Step 5: Simulate loading state
        stateFlow.value = validFormState.copy(isLoading = true)
        
        composeTestRule
            .onNodeWithContentDescription("Loading")
            .assertIsDisplayed()

        // Step 6: Simulate successful registration
        stateFlow.value = validFormState.copy(
            isLoading = false,
            isRegistrationSuccessful = true
        )

        // Step 7: Verify success screen is displayed
        composeTestRule
            .onNodeWithText("Welcome to CareComms!")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Your account has been created successfully.\nYou're now connected with Dr. Sarah Wilson.")
            .assertIsDisplayed()

        // Step 8: Click continue to chat
        composeTestRule
            .onNodeWithText("Continue to Chat")
            .performClick()

        // Step 9: Verify navigation to main app as caree
        assert(homeNavigationCalled)
        assert(homeUserType == "caree")
    }

    @Test
    fun careeRegistrationFlow_handlesValidationErrors() {
        val mockAuthViewModel: AuthViewModel = mockk(relaxed = true)
        val mockCarerRegistrationViewModel: CarerRegistrationViewModel = mockk(relaxed = true)
        val mockCareeRegistrationViewModel: CareeRegistrationViewModel = mockk(relaxed = true)
        val mockInvitationViewModel: InvitationViewModel = mockk(relaxed = true)
        
        val carerInfo = CarerInfo(
            id = "carer123",
            name = "Dr. Sarah Wilson",
            location = "Boston, MA",
            phoneNumber = "+1555123456"
        )
        
        val initialState = CareeRegistrationState(
            invitationToken = "valid-token",
            carerInfo = carerInfo,
            isInvitationValid = true
        )
        
        val stateFlow = MutableStateFlow(initialState)
        every { mockCareeRegistrationViewModel.state } returns stateFlow
        every { mockInvitationViewModel.isValidInvitationUrl(any()) } returns true
        every { mockInvitationViewModel.extractTokenFromUrl(any()) } returns "valid-token"
        
        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    deepLinkUrl = "carecomms://invite?token=valid-token",
                    onNavigateToHome = {}
                )
            }
        }

        // Fill out form with mismatched passwords
        composeTestRule
            .onNodeWithText("Email Address")
            .performTextInput("jane.doe@example.com")
        
        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("password123")
        
        composeTestRule
            .onNodeWithText("Confirm Password")
            .performTextInput("differentPassword")

        // Update state to show password mismatch
        stateFlow.value = initialState.copy(
            email = "jane.doe@example.com",
            password = "password123",
            confirmPassword = "differentPassword"
        )

        // Register button should be disabled due to password mismatch
        composeTestRule
            .onNodeWithText("Create Account")
            .assertIsNotEnabled()
    }

    @Test
    fun careeRegistrationFlow_handlesRegistrationError() {
        val mockAuthViewModel: AuthViewModel = mockk(relaxed = true)
        val mockCarerRegistrationViewModel: CarerRegistrationViewModel = mockk(relaxed = true)
        val mockCareeRegistrationViewModel: CareeRegistrationViewModel = mockk(relaxed = true)
        val mockInvitationViewModel: InvitationViewModel = mockk(relaxed = true)
        
        val carerInfo = CarerInfo(
            id = "carer123",
            name = "Dr. Sarah Wilson",
            location = "Boston, MA",
            phoneNumber = "+1555123456"
        )
        
        val initialState = CareeRegistrationState(
            invitationToken = "valid-token",
            carerInfo = carerInfo,
            isInvitationValid = true,
            email = "jane.doe@example.com",
            password = "password123",
            confirmPassword = "password123",
            firstName = "Jane",
            lastName = "Doe",
            dateOfBirth = "1985-03-15",
            healthInfo = "No major health issues"
        )
        
        val stateFlow = MutableStateFlow(initialState)
        every { mockCareeRegistrationViewModel.state } returns stateFlow
        every { mockInvitationViewModel.isValidInvitationUrl(any()) } returns true
        every { mockInvitationViewModel.extractTokenFromUrl(any()) } returns "valid-token"
        
        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    deepLinkUrl = "carecomms://invite?token=valid-token",
                    onNavigateToHome = {}
                )
            }
        }

        // Click register button
        composeTestRule
            .onNodeWithText("Create Account")
            .performClick()

        // Simulate registration error
        stateFlow.value = initialState.copy(
            isLoading = false,
            errorMessage = "Email already exists. Please use a different email address."
        )

        // Verify error message is displayed
        composeTestRule
            .onNodeWithText("Email already exists. Please use a different email address.")
            .assertIsDisplayed()
    }

    @Test
    fun careeRegistrationFlow_handlesInvitationValidationError() {
        val mockAuthViewModel: AuthViewModel = mockk(relaxed = true)
        val mockCarerRegistrationViewModel: CarerRegistrationViewModel = mockk(relaxed = true)
        val mockCareeRegistrationViewModel: CareeRegistrationViewModel = mockk(relaxed = true)
        val mockInvitationViewModel: InvitationViewModel = mockk(relaxed = true)
        
        val errorState = CareeRegistrationState(
            invitationToken = "expired-token",
            isValidatingInvitation = false,
            isInvitationValid = false,
            errorMessage = "This invitation has expired. Please request a new invitation from your carer."
        )
        
        every { mockCareeRegistrationViewModel.state } returns MutableStateFlow(errorState)
        every { mockInvitationViewModel.isValidInvitationUrl(any()) } returns true
        every { mockInvitationViewModel.extractTokenFromUrl(any()) } returns "expired-token"
        
        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    deepLinkUrl = "carecomms://invite?token=expired-token",
                    onNavigateToHome = {}
                )
            }
        }

        // Verify error message is displayed
        composeTestRule
            .onNodeWithText("This invitation has expired. Please request a new invitation from your carer.")
            .assertIsDisplayed()
        
        // Register button should be disabled
        composeTestRule
            .onNodeWithText("Create Account")
            .assertIsNotEnabled()
    }

    @Test
    fun careeRegistrationFlow_autoNavigationAfterSuccess() {
        val mockAuthViewModel: AuthViewModel = mockk(relaxed = true)
        val mockCarerRegistrationViewModel: CarerRegistrationViewModel = mockk(relaxed = true)
        val mockCareeRegistrationViewModel: CareeRegistrationViewModel = mockk(relaxed = true)
        val mockInvitationViewModel: InvitationViewModel = mockk(relaxed = true)
        
        val successState = CareeRegistrationState(
            isRegistrationSuccessful = true,
            carerInfo = CarerInfo(
                id = "carer123",
                name = "Dr. Sarah Wilson",
                location = "Boston, MA",
                phoneNumber = "+1555123456"
            )
        )
        
        every { mockCareeRegistrationViewModel.state } returns MutableStateFlow(successState)
        
        var homeNavigationCalled = false
        var homeUserType = ""
        
        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    onNavigateToHome = { userType ->
                        homeNavigationCalled = true
                        homeUserType = userType
                    }
                )
            }
        }

        // Should display success screen
        composeTestRule
            .onNodeWithText("Welcome to CareComms!")
            .assertIsDisplayed()

        // Wait for auto-navigation (3 seconds in real app, but test should be immediate)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            homeNavigationCalled
        }

        assert(homeUserType == "caree")
    }
}