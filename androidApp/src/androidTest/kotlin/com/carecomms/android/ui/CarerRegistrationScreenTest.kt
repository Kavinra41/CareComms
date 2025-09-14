package com.carecomms.android.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.screens.CarerRegistrationScreen
import com.carecomms.android.ui.theme.CareCommsTheme
import com.carecomms.data.models.DocumentType
import com.carecomms.data.models.DocumentUpload
import com.carecomms.data.models.UploadStatus
import com.carecomms.presentation.registration.CarerRegistrationAction
import com.carecomms.presentation.registration.CarerRegistrationState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CarerRegistrationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun carerRegistrationScreen_displaysAllFormFields() {
        composeTestRule.setContent {
            CareCommsTheme {
                CarerRegistrationScreen(
                    state = CarerRegistrationState(),
                    onAction = {},
                    onBackClick = {},
                    onDocumentUpload = { _, _ -> }
                )
            }
        }

        // Verify all form fields are displayed
        composeTestRule.onNodeWithText("Email Address").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Age").assertIsDisplayed()
        composeTestRule.onNodeWithText("Phone Number").assertIsDisplayed()
        composeTestRule.onNodeWithText("Location").assertIsDisplayed()
        composeTestRule.onNodeWithText("Professional Documents").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add Document").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
    }

    @Test
    fun carerRegistrationScreen_displaysHeaderAndBackButton() {
        var backClicked = false

        composeTestRule.setContent {
            CareCommsTheme {
                CarerRegistrationScreen(
                    state = CarerRegistrationState(),
                    onAction = {},
                    onBackClick = { backClicked = true },
                    onDocumentUpload = { _, _ -> }
                )
            }
        }

        // Verify header and back button
        composeTestRule.onNodeWithText("Carer Registration").assertIsDisplayed()
        composeTestRule.onNodeWithText("← Back").assertIsDisplayed()

        // Test back button functionality
        composeTestRule.onNodeWithText("← Back").performClick()
        assert(backClicked)
    }

    @Test
    fun carerRegistrationScreen_emailInput_triggersAction() {
        var lastAction: CarerRegistrationAction? = null

        composeTestRule.setContent {
            CareCommsTheme {
                CarerRegistrationScreen(
                    state = CarerRegistrationState(),
                    onAction = { lastAction = it },
                    onBackClick = {},
                    onDocumentUpload = { _, _ -> }
                )
            }
        }

        // Test email input
        composeTestRule.onNodeWithText("Email Address").performTextInput("test@example.com")
        
        assert(lastAction is CarerRegistrationAction.UpdateEmail)
        assert((lastAction as CarerRegistrationAction.UpdateEmail).email == "test@example.com")
    }

    @Test
    fun carerRegistrationScreen_passwordInput_triggersAction() {
        var lastAction: CarerRegistrationAction? = null

        composeTestRule.setContent {
            CareCommsTheme {
                CarerRegistrationScreen(
                    state = CarerRegistrationState(),
                    onAction = { lastAction = it },
                    onBackClick = {},
                    onDocumentUpload = { _, _ -> }
                )
            }
        }

        // Test password input
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        
        assert(lastAction is CarerRegistrationAction.UpdatePassword)
        assert((lastAction as CarerRegistrationAction.UpdatePassword).password == "password123")
    }

    @Test
    fun carerRegistrationScreen_ageInput_triggersAction() {
        var lastAction: CarerRegistrationAction? = null

        composeTestRule.setContent {
            CareCommsTheme {
                CarerRegistrationScreen(
                    state = CarerRegistrationState(),
                    onAction = { lastAction = it },
                    onBackClick = {},
                    onDocumentUpload = { _, _ -> }
                )
            }
        }

        // Test age input
        composeTestRule.onNodeWithText("Age").performTextInput("35")
        
        assert(lastAction is CarerRegistrationAction.UpdateAge)
        assert((lastAction as CarerRegistrationAction.UpdateAge).age == "35")
    }

    @Test
    fun carerRegistrationScreen_phoneInput_triggersAction() {
        var lastAction: CarerRegistrationAction? = null

        composeTestRule.setContent {
            CareCommsTheme {
                CarerRegistrationScreen(
                    state = CarerRegistrationState(),
                    onAction = { lastAction = it },
                    onBackClick = {},
                    onDocumentUpload = { _, _ -> }
                )
            }
        }

        // Test phone input
        composeTestRule.onNodeWithText("Phone Number").performTextInput("+1234567890")
        
        assert(lastAction is CarerRegistrationAction.UpdatePhoneNumber)
        assert((lastAction as CarerRegistrationAction.UpdatePhoneNumber).phoneNumber == "+1234567890")
    }

    @Test
    fun carerRegistrationScreen_locationInput_triggersAction() {
        var lastAction: CarerRegistrationAction? = null

        composeTestRule.setContent {
            CareCommsTheme {
                CarerRegistrationScreen(
                    state = CarerRegistrationState(),
                    onAction = { lastAction = it },
                    onBackClick = {},
                    onDocumentUpload = { _, _ -> }
                )
            }
        }

        // Test location input
        composeTestRule.onNodeWithText("Location").performTextInput("New York, NY")
        
        assert(lastAction is CarerRegistrationAction.UpdateLocation)
        assert((lastAction as CarerRegistrationAction.UpdateLocation).location == "New York, NY")
    }

    @Test
    fun carerRegistrationScreen_displayValidationErrors() {
        val stateWithErrors = CarerRegistrationState(
            validationErrors = mapOf(
                "email" to "Email is required",
                "password" to "Password is required",
                "age" to "Age must be between 18 and 100",
                "documents" to "At least one professional document is required"
            )
        )

        composeTestRule.setContent {
            CareCommsTheme {
                CarerRegistrationScreen(
                    state = stateWithErrors,
                    onAction = {},
                    onBackClick = {},
                    onDocumentUpload = { _, _ -> }
                )
            }
        }

        // Verify validation errors are displayed
        composeTestRule.onNodeWithText("Email is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Age must be between 18 and 100").assertIsDisplayed()
        composeTestRule.onNodeWithText("At least one professional document is required").assertIsDisplayed()
    }

    @Test
    fun carerRegistrationScreen_displayUploadedDocuments() {
        val stateWithDocuments = CarerRegistrationState(
            uploadedDocuments = listOf(
                DocumentUpload(
                    id = "1",
                    fileName = "certificate.pdf",
                    fileType = DocumentType.PROFESSIONAL_CERTIFICATE,
                    uploadStatus = UploadStatus.COMPLETED
                ),
                DocumentUpload(
                    id = "2",
                    fileName = "id_document.pdf",
                    fileType = DocumentType.IDENTITY_DOCUMENT,
                    uploadStatus = UploadStatus.UPLOADING
                )
            )
        )

        composeTestRule.setContent {
            CareCommsTheme {
                CarerRegistrationScreen(
                    state = stateWithDocuments,
                    onAction = {},
                    onBackClick = {},
                    onDocumentUpload = { _, _ -> }
                )
            }
        }

        // Verify documents are displayed
        composeTestRule.onNodeWithText("certificate.pdf").assertIsDisplayed()
        composeTestRule.onNodeWithText("id_document.pdf").assertIsDisplayed()
        composeTestRule.onNodeWithText("Professional Certificate").assertIsDisplayed()
        composeTestRule.onNodeWithText("Identity Document").assertIsDisplayed()
    }

    @Test
    fun carerRegistrationScreen_removeDocument_triggersAction() {
        var lastAction: CarerRegistrationAction? = null
        val stateWithDocuments = CarerRegistrationState(
            uploadedDocuments = listOf(
                DocumentUpload(
                    id = "1",
                    fileName = "certificate.pdf",
                    fileType = DocumentType.PROFESSIONAL_CERTIFICATE,
                    uploadStatus = UploadStatus.COMPLETED
                )
            )
        )

        composeTestRule.setContent {
            CareCommsTheme {
                CarerRegistrationScreen(
                    state = stateWithDocuments,
                    onAction = { lastAction = it },
                    onBackClick = {},
                    onDocumentUpload = { _, _ -> }
                )
            }
        }

        // Find and click the remove button for the document
        composeTestRule.onNodeWithContentDescription("Remove Document").performClick()
        
        assert(lastAction is CarerRegistrationAction.RemoveDocument)
        assert((lastAction as CarerRegistrationAction.RemoveDocument).documentId == "1")
    }

    @Test
    fun carerRegistrationScreen_submitButton_triggersAction() {
        var lastAction: CarerRegistrationAction? = null

        composeTestRule.setContent {
            CareCommsTheme {
                CarerRegistrationScreen(
                    state = CarerRegistrationState(),
                    onAction = { lastAction = it },
                    onBackClick = {},
                    onDocumentUpload = { _, _ -> }
                )
            }
        }

        // Test submit button
        composeTestRule.onNodeWithText("Create Account").performClick()
        
        assert(lastAction is CarerRegistrationAction.SubmitRegistration)
    }

    @Test
    fun carerRegistrationScreen_loadingState_disablesButton() {
        val loadingState = CarerRegistrationState(isLoading = true)

        composeTestRule.setContent {
            CareCommsTheme {
                CarerRegistrationScreen(
                    state = loadingState,
                    onAction = {},
                    onBackClick = {},
                    onDocumentUpload = { _, _ -> }
                )
            }
        }

        // Verify button is disabled and shows loading indicator
        composeTestRule.onNodeWithText("Create Account").assertIsNotEnabled()
        composeTestRule.onNode(hasContentDescription("Loading")).assertIsDisplayed()
    }

    @Test
    fun carerRegistrationScreen_displayRegistrationError() {
        val errorState = CarerRegistrationState(
            registrationError = "Registration failed. Please try again."
        )

        composeTestRule.setContent {
            CareCommsTheme {
                CarerRegistrationScreen(
                    state = errorState,
                    onAction = {},
                    onBackClick = {},
                    onDocumentUpload = { _, _ -> }
                )
            }
        }

        // Verify error message is displayed
        composeTestRule.onNodeWithText("Registration failed. Please try again.").assertIsDisplayed()
    }

    @Test
    fun carerRegistrationScreen_addDocumentButton_opensDialog() {
        composeTestRule.setContent {
            CareCommsTheme {
                CarerRegistrationScreen(
                    state = CarerRegistrationState(),
                    onAction = {},
                    onBackClick = {},
                    onDocumentUpload = { _, _ -> }
                )
            }
        }

        // Click add document button
        composeTestRule.onNodeWithText("Add Document").performClick()

        // Verify dialog is displayed
        composeTestRule.onNodeWithText("Select Document Type").assertIsDisplayed()
        composeTestRule.onNodeWithText("Professional Certificate").assertIsDisplayed()
        composeTestRule.onNodeWithText("Identity Document").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select File").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }
}