package com.carecomms.integration

import com.carecomms.data.models.*
import com.carecomms.data.repository.AuthRepository
import com.carecomms.data.validation.CarerRegistrationValidator
import com.carecomms.domain.usecase.CarerRegistrationUseCase
import com.carecomms.presentation.registration.CarerRegistrationAction
import com.carecomms.presentation.registration.CarerRegistrationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CarerRegistrationIntegrationTest {
    
    @Test
    fun `complete carer registration flow should work end to end`() = runTest {
        // Setup
        val mockAuthRepository = TestAuthRepository()
        val validator = CarerRegistrationValidator()
        val useCase = CarerRegistrationUseCase(mockAuthRepository, validator)
        val documentUploadService = MockDocumentUploadService()
        val viewModel = CarerRegistrationViewModel(
            registrationUseCase = useCase,
            documentUploadService = documentUploadService,
            coroutineScope = TestScope()
        )
        
        // Test complete registration flow
        viewModel.handleAction(CarerRegistrationAction.UpdateEmail("carer@example.com"))
        viewModel.handleAction(CarerRegistrationAction.UpdatePassword("SecurePass123"))
        viewModel.handleAction(CarerRegistrationAction.UpdateConfirmPassword("SecurePass123"))
        viewModel.handleAction(CarerRegistrationAction.UpdateAge("30"))
        viewModel.handleAction(CarerRegistrationAction.UpdatePhoneNumber("1234567890"))
        viewModel.handleAction(CarerRegistrationAction.UpdateLocation("New York, NY"))
        
        // Upload a document
        viewModel.uploadDocument("certificate.pdf", DocumentType.PROFESSIONAL_CERTIFICATE)
        testScheduler.advanceUntilIdle()
        
        // Verify document was added
        assertEquals(1, viewModel.state.value.uploadedDocuments.size)
        
        // Submit registration
        viewModel.handleAction(CarerRegistrationAction.SubmitRegistration)
        testScheduler.advanceUntilIdle()
        
        // Verify successful registration
        val finalState = viewModel.state.value
        assertFalse(finalState.isLoading)
        assertTrue(finalState.isRegistrationSuccessful)
        assertEquals(null, finalState.registrationError)
        assertEquals("test_carer_id", finalState.authResult?.user?.id)
        assertEquals("test_token", finalState.authResult?.token)
    }
    
    @Test
    fun `registration should fail with invalid data`() = runTest {
        val mockAuthRepository = TestAuthRepository()
        val validator = CarerRegistrationValidator()
        val useCase = CarerRegistrationUseCase(mockAuthRepository, validator)
        val documentUploadService = MockDocumentUploadService()
        val viewModel = CarerRegistrationViewModel(
            registrationUseCase = useCase,
            documentUploadService = documentUploadService,
            coroutineScope = TestScope()
        )
        
        // Set invalid data
        viewModel.handleAction(CarerRegistrationAction.UpdateEmail("invalid-email"))
        viewModel.handleAction(CarerRegistrationAction.UpdatePassword("weak"))
        viewModel.handleAction(CarerRegistrationAction.UpdateConfirmPassword("weak"))
        viewModel.handleAction(CarerRegistrationAction.UpdateAge("17"))
        viewModel.handleAction(CarerRegistrationAction.UpdatePhoneNumber("123"))
        viewModel.handleAction(CarerRegistrationAction.UpdateLocation(""))
        // No documents uploaded
        
        // Submit registration
        viewModel.handleAction(CarerRegistrationAction.SubmitRegistration)
        testScheduler.advanceUntilIdle()
        
        // Verify validation errors
        val finalState = viewModel.state.value
        assertFalse(finalState.isLoading)
        assertFalse(finalState.isRegistrationSuccessful)
        assertTrue(finalState.validationErrors.isNotEmpty())
    }
    
    @Test
    fun `validation should work correctly for all fields`() {
        val validator = CarerRegistrationValidator()
        
        // Test valid data
        val validData = CarerRegistrationData(
            email = "carer@example.com",
            password = "SecurePass123",
            documents = listOf("certificate.pdf"),
            age = 30,
            phoneNumber = "1234567890",
            location = "New York, NY"
        )
        
        val validResult = validator.validate(validData)
        assertTrue(validResult.isValid)
        assertTrue(validResult.errors.isEmpty())
        
        // Test invalid data
        val invalidData = CarerRegistrationData(
            email = "invalid-email",
            password = "weak",
            documents = emptyList(),
            age = 17,
            phoneNumber = "123",
            location = ""
        )
        
        val invalidResult = validator.validate(invalidData)
        assertFalse(invalidResult.isValid)
        assertEquals(6, invalidResult.errors.size)
    }
}

// Test implementation of AuthRepository
class TestAuthRepository : AuthRepository {
    
    override suspend fun signUpCarer(carerData: CarerRegistrationData): Result<AuthResult> {
        return Result.success(
            AuthResult(
                user = Carer(
                    id = "test_carer_id",
                    email = carerData.email,
                    createdAt = System.currentTimeMillis(),
                    documents = carerData.documents,
                    age = carerData.age,
                    phoneNumber = carerData.phoneNumber,
                    location = carerData.location
                ),
                token = "test_token"
            )
        )
    }
    
    override suspend fun signInWithEmail(email: String, password: String): Result<AuthResult> {
        TODO("Not implemented for this test")
    }
    
    override suspend fun signUpCaree(careeData: CareeRegistrationData, invitationToken: String): Result<AuthResult> {
        TODO("Not implemented for this test")
    }
    
    override suspend fun signOut(): Result<Unit> {
        TODO("Not implemented for this test")
    }
    
    override suspend fun getCurrentUser(): User? {
        TODO("Not implemented for this test")
    }
    
    override suspend fun validateInvitationToken(token: String): Result<CarerInfo> {
        TODO("Not implemented for this test")
    }
    
    override suspend fun generateInvitationToken(carerId: String): Result<String> {
        TODO("Not implemented for this test")
    }
    
    override suspend fun refreshToken(): Result<String> {
        TODO("Not implemented for this test")
    }
    
    override fun isUserLoggedIn(): Flow<Boolean> {
        return flowOf(false)
    }
    
    override suspend fun deleteAccount(): Result<Unit> {
        TODO("Not implemented for this test")
    }
}