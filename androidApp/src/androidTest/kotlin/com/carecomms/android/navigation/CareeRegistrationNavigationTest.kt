package com.carecomms.android.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.theme.CareCommsTheme
import com.carecomms.data.models.CarerInfo
import com.carecomms.presentation.registration.CareeRegistrationState
import com.carecomms.presentation.registration.CareeRegistrationIntent
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
class CareeRegistrationNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun authNavigation_handlesDeepLinkToCareeRegistration() {
        val mockAuthViewModel: AuthViewModel = mockk(relaxed = true)
        val mockCarerRegistrationViewModel: CarerRegistrationViewModel = mockk(relaxed = true)
        val mockCareeRegistrationViewModel: CareeRegistrationViewModel = mockk(relaxed = true)
        val mockInvitationViewModel: InvitationViewModel = mockk(relaxed = true)
        
        val careeRegistrationState = CareeRegistrationState(
            invitationToken = "test-token",
            carerInfo = CarerInfo(
                id = "carer123",
                name = "Dr. Smith",
                location = "New York, NY",
                phoneNumber = "+1234567890"
            ),
            isInvitationValid = true
        )
        
        every { mockCareeRegistrationViewModel.state } returns MutableStateFlow(careeRegistrationState)
        every { mockInvitationViewModel.isValidInvitationUrl(any()) } returns true
        every { mockInvitationViewModel.extractTokenFromUrl(any()) } returns "test-token"
        
        var homeNavigationCalled = false
        var homeUserType = ""
        
        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    deepLinkUrl = "carecomms://invite?token=test-token",
                    onNavigateToHome = { userType ->
                        homeNavigationCalled = true
                        homeUserType = userType
                    }
                )
            }
        }

        // Should display caree registration screen
        composeTestRule
            .onNodeWithText("Join CareComms")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Dr. Smith")
            .assertIsDisplayed()
    }

    @Test
    fun authNavigation_navigatesToChatAfterSuccessfulCareeRegistration() {
        val mockAuthViewModel: AuthViewModel = mockk(relaxed = true)
        val mockCarerRegistrationViewModel: CarerRegistrationViewModel = mockk(relaxed = true)
        val mockCareeRegistrationViewModel: CareeRegistrationViewModel = mockk(relaxed = true)
        val mockInvitationViewModel: InvitationViewModel = mockk(relaxed = true)
        
        val successState = CareeRegistrationState(
            isRegistrationSuccessful = true,
            carerInfo = CarerInfo(
                id = "carer123",
                name = "Dr. Smith",
                location = "New York, NY",
                phoneNumber = "+1234567890"
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
        
        // Click continue button
        composeTestRule
            .onNodeWithText("Continue to Chat")
            .performClick()

        assert(homeNavigationCalled)
        assert(homeUserType == "caree")
    }

    @Test
    fun authNavigation_handlesInvalidInvitationToken() {
        val mockAuthViewModel: AuthViewModel = mockk(relaxed = true)
        val mockCarerRegistrationViewModel: CarerRegistrationViewModel = mockk(relaxed = true)
        val mockCareeRegistrationViewModel: CareeRegistrationViewModel = mockk(relaxed = true)
        val mockInvitationViewModel: InvitationViewModel = mockk(relaxed = true)
        
        val errorState = CareeRegistrationState(
            invitationToken = "invalid-token",
            isInvitationValid = false,
            errorMessage = "Invalid or expired invitation link"
        )
        
        every { mockCareeRegistrationViewModel.state } returns MutableStateFlow(errorState)
        every { mockInvitationViewModel.isValidInvitationUrl(any()) } returns true
        every { mockInvitationViewModel.extractTokenFromUrl(any()) } returns "invalid-token"
        
        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    deepLinkUrl = "carecomms://invite?token=invalid-token",
                    onNavigateToHome = {}
                )
            }
        }

        // Should display error message
        composeTestRule
            .onNodeWithText("Invalid or expired invitation link")
            .assertIsDisplayed()
    }

    @Test
    fun authNavigation_backButtonFromCareeRegistration_navigatesToLanding() {
        val mockAuthViewModel: AuthViewModel = mockk(relaxed = true)
        val mockCarerRegistrationViewModel: CarerRegistrationViewModel = mockk(relaxed = true)
        val mockCareeRegistrationViewModel: CareeRegistrationViewModel = mockk(relaxed = true)
        val mockInvitationViewModel: InvitationViewModel = mockk(relaxed = true)
        
        val careeRegistrationState = CareeRegistrationState(
            invitationToken = "test-token",
            carerInfo = CarerInfo(
                id = "carer123",
                name = "Dr. Smith",
                location = "New York, NY",
                phoneNumber = "+1234567890"
            ),
            isInvitationValid = true
        )
        
        every { mockCareeRegistrationViewModel.state } returns MutableStateFlow(careeRegistrationState)
        every { mockInvitationViewModel.isValidInvitationUrl(any()) } returns true
        every { mockInvitationViewModel.extractTokenFromUrl(any()) } returns "test-token"
        
        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    deepLinkUrl = "carecomms://invite?token=test-token",
                    onNavigateToHome = {}
                )
            }
        }

        // Should be on caree registration screen
        composeTestRule
            .onNodeWithText("Join CareComms")
            .assertIsDisplayed()
        
        // Click back button
        composeTestRule
            .onNodeWithContentDescription("Back")
            .performClick()

        // Should navigate back to landing screen
        composeTestRule
            .onNodeWithText("Welcome to CareComms")
            .assertIsDisplayed()
    }

    @Test
    fun authNavigation_ignoresInvalidDeepLinkUrls() {
        val mockAuthViewModel: AuthViewModel = mockk(relaxed = true)
        val mockCarerRegistrationViewModel: CarerRegistrationViewModel = mockk(relaxed = true)
        val mockCareeRegistrationViewModel: CareeRegistrationViewModel = mockk(relaxed = true)
        val mockInvitationViewModel: InvitationViewModel = mockk(relaxed = true)
        
        every { mockInvitationViewModel.isValidInvitationUrl(any()) } returns false
        
        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    deepLinkUrl = "https://example.com/invalid",
                    onNavigateToHome = {}
                )
            }
        }

        // Should start with splash screen (normal flow)
        composeTestRule
            .onNodeWithText("CareComms")
            .assertIsDisplayed()
    }

    @Test
    fun authNavigation_handlesHttpsInvitationUrls() {
        val mockAuthViewModel: AuthViewModel = mockk(relaxed = true)
        val mockCarerRegistrationViewModel: CarerRegistrationViewModel = mockk(relaxed = true)
        val mockCareeRegistrationViewModel: CareeRegistrationViewModel = mockk(relaxed = true)
        val mockInvitationViewModel: InvitationViewModel = mockk(relaxed = true)
        
        val careeRegistrationState = CareeRegistrationState(
            invitationToken = "https-token",
            carerInfo = CarerInfo(
                id = "carer123",
                name = "Dr. Johnson",
                location = "Los Angeles, CA",
                phoneNumber = "+1987654321"
            ),
            isInvitationValid = true
        )
        
        every { mockCareeRegistrationViewModel.state } returns MutableStateFlow(careeRegistrationState)
        every { mockInvitationViewModel.isValidInvitationUrl(any()) } returns true
        every { mockInvitationViewModel.extractTokenFromUrl(any()) } returns "https-token"
        
        composeTestRule.setContent {
            CareCommsTheme {
                AuthNavigation(
                    deepLinkUrl = "https://carecomms.app/invite?token=https-token",
                    onNavigateToHome = {}
                )
            }
        }

        // Should display caree registration screen
        composeTestRule
            .onNodeWithText("Join CareComms")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Dr. Johnson")
            .assertIsDisplayed()
    }
}