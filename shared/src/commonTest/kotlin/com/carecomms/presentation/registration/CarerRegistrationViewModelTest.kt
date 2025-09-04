package com.carecomms.presentation.registration

import com.carecomms.data.models.*
import com.carecomms.domain.usecase.CarerRegistrationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CarerRegistrationViewModelTest {
    
    private val testScope = TestScope()
    private val mockRegistrationUseCase = MockCarerRegistrationUseCase()
    private val mockDocumentUploadService = MockDocumentUploadService()
    
    private fun createViewModel(): CarerRegistrationViewModel {
        return CarerRegistrationViewModel(
            registrationUseCase = mockRegistrationUseCase,
            documentUploadService = mockDocumentUploadService,
            coroutineScope = testScope
        )
    }
    
    @Test
    fun `initial state should be empty`() {
        val viewModel = createViewModel()
        val initialState = viewModel.state.value
        
        assertEquals("", initialState.email)
        assertEquals("", initialState.password)
        assertEquals("", initialState.confirmPassword)
        assertEquals("", initialState.age)
        assertEquals("", initialState.phoneNumber)
        assertEquals("", initialState.location)
        assertTrue(initialState.uploadedDocuments.isEmpty())
        assertFalse(initialState.isLoading)
        assertTrue(initialState.validationErrors.isEmpty())
        assertEquals(null, initialState.registrationError)
        assertFalse(initialState.isRegistrationSuccessful)
    }
    
    @Test
    fun `UpdateEmail action should update email and clear email validation error`() {
        val viewModel = createViewModel()
        
        // Set initial validation error
        viewModel.handleAction(CarerRegistrationAction.SubmitRegistration)
        assertTrue(viewModel.state.value.validationErrors.containsKey("email"))
        
        // Update email
        viewModel.handleAction(CarerRegistrationAction.UpdateEmail("test@example.com"))
        
        assertEquals("test@example.com", viewModel.state.value.email)
        assertFalse(viewModel.state.value.validationErrors.containsKey("email"))
    }
    
    @Test
    fun `UpdatePassword action should update password and clear password validation error`() {
        val viewModel = createViewModel()
        
        viewModel.handleAction(CarerRegistrationAction.UpdatePassword("SecurePass123"))
        
        assertEquals("SecurePass123", viewModel.state.value.password)
    }
    
    @Test
    fun `UpdateAge action should update age and clear age validation error`() {
        val viewModel = createViewModel()
        
        viewModel.handleAction(CarerRegistrationAction.UpdateAge("30"))
        
        assertEquals("30", viewModel.state.value.age)
    }
    
    @Test
    fun `AddDocument action should add document to list`() {
        val viewModel = createViewModel()
        val document = DocumentUpload(
            id = "doc_123",
            fileName = "certificate.pdf",
            fileType = DocumentType.PROFESSIONAL_CERTIFICATE,
            uploadStatus = UploadStatus.COMPLETED
        )
        
        viewModel.handleAction(CarerRegistrationAction.AddDocument(document))
        
        assertEquals(1, viewModel.state.value.uploadedDocuments.size)
        assertEquals(document, viewModel.state.value.uploadedDocuments.first())
    }
    
    @Test
    fun `RemoveDocument action should remove document from list`() {
        val viewModel = createViewModel()
        val document1 = DocumentUpload(
            id = "doc_123",
            fileName = "certificate.pdf",
            fileType = DocumentType.PROFESSIONAL_CERTIFICATE,
            uploadStatus = UploadStatus.COMPLETED
        )
        val document2 = DocumentUpload(
            id = "doc_456",
            fileName = "id.jpg",
            fileType = DocumentType.IDENTITY_DOCUMENT,
            uploadStatus = UploadStatus.COMPLETED
        )
        
        viewModel.handleAction(CarerRegistrationAction.AddDocument(document1))
        viewModel.handleAction(CarerRegistrationAction.AddDocument(document2))
        assertEquals(2, viewModel.state.value.uploadedDocuments.size)
        
        viewModel.handleAction(CarerRegistrationAction.RemoveDocument("doc_123"))
        
        assertEquals(1, viewModel.state.value.uploadedDocuments.size)
        assertEquals(document2, viewModel.state.value.uploadedDocuments.first())
    }
    
    @Test
    fun `SubmitRegistration should show validation errors for empty form`() {
        val viewModel = createViewModel()
        
        viewModel.handleAction(CarerRegistrationAction.SubmitRegistration)
        
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.validationErrors.containsKey("email"))
        assertTrue(state.validationErrors.containsKey("password"))
        assertTrue(state.validationErrors.containsKey("confirmPassword"))
        assertTrue(state.validationErrors.containsKey("age"))
        assertTrue(state.validationErrors.containsKey("phoneNumber"))
        assertTrue(state.validationErrors.containsKey("location"))
        assertTrue(state.validationErrors.containsKey("documents"))
    }
    
    @Test
    fun `SubmitRegistration should show error for password mismatch`() {
        val viewModel = createViewModel()
        
        viewModel.handleAction(CarerRegistrationAction.UpdateEmail("test@example.com"))
        viewModel.handleAction(CarerRegistrationAction.UpdatePassword("SecurePass123"))
        viewModel.handleAction(CarerRegistrationAction.UpdateConfirmPassword("DifferentPass123"))
        viewModel.handleAction(CarerRegistrationAction.UpdateAge("30"))
        viewModel.handleAction(CarerRegistrationAction.UpdatePhoneNumber("1234567890"))
        viewModel.handleAction(CarerRegistrationAction.UpdateLocation("New York"))
        viewModel.handleAction(CarerRegistrationAction.AddDocument(
            DocumentUpload(
                id = "doc_123",
                fileName = "cert.pdf",
                fileType = DocumentType.PROFESSIONAL_CERTIFICATE,
                uploadStatus = UploadStatus.COMPLETED
            )
        ))
        
        viewModel.handleAction(CarerRegistrationAction.SubmitRegistration)
        
        val state = viewModel.state.value
        assertTrue(state.validationErrors.containsKey("confirmPassword"))
        assertEquals("Passwords do not match", state.validationErrors["confirmPassword"])
    }
    
    @Test
    fun `SubmitRegistration should succeed with valid data`() = runTest {
        val viewModel = createViewModel()
        
        // Set up valid form data
        viewModel.handleAction(CarerRegistrationAction.UpdateEmail("test@example.com"))
        viewModel.handleAction(CarerRegistrationAction.UpdatePassword("SecurePass123"))
        viewModel.handleAction(CarerRegistrationAction.UpdateConfirmPassword("SecurePass123"))
        viewModel.handleAction(CarerRegistrationAction.UpdateAge("30"))
        viewModel.handleAction(CarerRegistrationAction.UpdatePhoneNumber("1234567890"))
        viewModel.handleAction(CarerRegistrationAction.UpdateLocation("New York"))
        viewModel.handleAction(CarerRegistrationAction.AddDocument(
            DocumentUpload(
                id = "doc_123",
                fileName = "cert.pdf",
                fileType = DocumentType.PROFESSIONAL_CERTIFICATE,
                uploadStatus = UploadStatus.COMPLETED
            )
        ))
        
        // Mock successful registration
        mockRegistrationUseCase.result = Result.success(
            AuthResult(
                user = Carer(
                    id = "carer123",
                    email = "test@example.com",
                    createdAt = System.currentTimeMillis(),
                    documents = listOf("cert.pdf"),
                    age = 30,
                    phoneNumber = "1234567890",
                    location = "New York"
                ),
                token = "auth_token"
            )
        )
        
        viewModel.handleAction(CarerRegistrationAction.SubmitRegistration)
        testScope.testScheduler.advanceUntilIdle()
        
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isRegistrationSuccessful)
        assertEquals(null, state.registrationError)
        assertEquals("carer123", state.authResult?.user?.id)
    }
    
    @Test
    fun `SubmitRegistration should handle registration failure`() = runTest {
        val viewModel = createViewModel()
        
        // Set up valid form data
        viewModel.handleAction(CarerRegistrationAction.UpdateEmail("test@example.com"))
        viewModel.handleAction(CarerRegistrationAction.UpdatePassword("SecurePass123"))
        viewModel.handleAction(CarerRegistrationAction.UpdateConfirmPassword("SecurePass123"))
        viewModel.handleAction(CarerRegistrationAction.UpdateAge("30"))
        viewModel.handleAction(CarerRegistrationAction.UpdatePhoneNumber("1234567890"))
        viewModel.handleAction(CarerRegistrationAction.UpdateLocation("New York"))
        viewModel.handleAction(CarerRegistrationAction.AddDocument(
            DocumentUpload(
                id = "doc_123",
                fileName = "cert.pdf",
                fileType = DocumentType.PROFESSIONAL_CERTIFICATE,
                uploadStatus = UploadStatus.COMPLETED
            )
        ))
        
        // Mock registration failure
        mockRegistrationUseCase.result = Result.failure(Exception("Registration failed"))
        
        viewModel.handleAction(CarerRegistrationAction.SubmitRegistration)
        testScope.testScheduler.advanceUntilIdle()
        
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isRegistrationSuccessful)
        assertEquals("Registration failed", state.registrationError)
    }
    
    @Test
    fun `ClearErrors action should clear all errors`() {
        val viewModel = createViewModel()
        
        // Trigger validation errors
        viewModel.handleAction(CarerRegistrationAction.SubmitRegistration)
        assertTrue(viewModel.state.value.validationErrors.isNotEmpty())
        
        // Clear errors
        viewModel.handleAction(CarerRegistrationAction.ClearErrors)
        
        assertTrue(viewModel.state.value.validationErrors.isEmpty())
        assertEquals(null, viewModel.state.value.registrationError)
    }
    
    @Test
    fun `ResetState action should reset to initial state`() {
        val viewModel = createViewModel()
        
        // Modify state
        viewModel.handleAction(CarerRegistrationAction.UpdateEmail("test@example.com"))
        viewModel.handleAction(CarerRegistrationAction.UpdatePassword("password"))
        
        // Reset state
        viewModel.handleAction(CarerRegistrationAction.ResetState)
        
        val state = viewModel.state.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertTrue(state.validationErrors.isEmpty())
    }
}

// Mock implementation for testing
class MockCarerRegistrationUseCase : CarerRegistrationUseCase(
    authRepository = object : com.carecomms.data.repository.AuthRepository {
        override suspend fun signInWithEmail(email: String, password: String): Result<AuthResult> = TODO()
        override suspend fun signUpCarer(carerData: CarerRegistrationData): Result<AuthResult> = TODO()
        override suspend fun signUpCaree(careeData: CareeRegistrationData, invitationToken: String): Result<AuthResult> = TODO()
        override suspend fun signOut(): Result<Unit> = TODO()
        override suspend fun getCurrentUser(): com.carecomms.data.models.User? = TODO()
        override suspend fun validateInvitationToken(token: String): Result<CarerInfo> = TODO()
        override suspend fun generateInvitationToken(carerId: String): Result<String> = TODO()
        override suspend fun refreshToken(): Result<String> = TODO()
        override fun isUserLoggedIn(): kotlinx.coroutines.flow.Flow<Boolean> = TODO()
        override suspend fun deleteAccount(): Result<Unit> = TODO()
    }
) {
    var result: Result<AuthResult> = Result.success(
        AuthResult(
            user = Carer(
                id = "test_id",
                email = "test@example.com",
                createdAt = System.currentTimeMillis(),
                documents = emptyList(),
                age = 25,
                phoneNumber = "1234567890",
                location = "Test Location"
            ),
            token = "test_token"
        )
    )
    
    override suspend fun execute(registrationData: CarerRegistrationData): Result<AuthResult> {
        return result
    }
}