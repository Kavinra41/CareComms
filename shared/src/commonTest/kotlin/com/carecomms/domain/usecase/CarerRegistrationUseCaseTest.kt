package com.carecomms.domain.usecase

import com.carecomms.data.models.AuthResult
import com.carecomms.data.models.Carer
import com.carecomms.data.models.CarerRegistrationData
import com.carecomms.data.repository.AuthRepository
import com.carecomms.data.validation.CarerRegistrationValidator
import com.carecomms.data.validation.ValidationError
import com.carecomms.data.validation.ValidationResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarerRegistrationUseCaseTest {
    
    private val mockAuthRepository = MockAuthRepository()
    private val mockValidator = MockCarerRegistrationValidator()
    private val useCase = CarerRegistrationUseCase(mockAuthRepository, mockValidator)
    
    @Test
    fun `execute should return success when validation passes and registration succeeds`() = runTest {
        val registrationData = CarerRegistrationData(
            email = "carer@example.com",
            password = "SecurePass123",
            documents = listOf("certificate.pdf"),
            age = 30,
            phoneNumber = "1234567890",
            location = "New York, NY"
        )
        
        mockValidator.validationResult = ValidationResult(isValid = true)
        mockAuthRepository.signUpResult = Result.success(
            AuthResult(
                user = Carer(
                    id = "carer123",
                    email = "carer@example.com",
                    createdAt = System.currentTimeMillis(),
                    documents = listOf("certificate.pdf"),
                    age = 30,
                    phoneNumber = "1234567890",
                    location = "New York, NY"
                ),
                token = "auth_token_123"
            )
        )
        
        val result = useCase.execute(registrationData)
        
        assertTrue(result.isSuccess)
        assertEquals("carer123", result.getOrNull()?.user?.id)
    }
    
    @Test
    fun `execute should return failure when validation fails`() = runTest {
        val registrationData = CarerRegistrationData(
            email = "invalid-email",
            password = "weak",
            documents = emptyList(),
            age = 17,
            phoneNumber = "123",
            location = ""
        )
        
        mockValidator.validationResult = ValidationResult(
            isValid = false,
            errors = listOf(
                ValidationError.InvalidEmail,
                ValidationError.WeakPassword,
                ValidationError.NoDocuments
            )
        )
        
        val result = useCase.execute(registrationData)
        
        assertTrue(result.isFailure)
        val errorMessage = result.exceptionOrNull()?.message
        assertTrue(errorMessage?.contains("Please enter a valid email address") == true)
        assertTrue(errorMessage?.contains("Password must be at least 8 characters") == true)
        assertTrue(errorMessage?.contains("At least one professional document is required") == true)
    }
    
    @Test
    fun `execute should return failure when auth repository fails`() = runTest {
        val registrationData = CarerRegistrationData(
            email = "carer@example.com",
            password = "SecurePass123",
            documents = listOf("certificate.pdf"),
            age = 30,
            phoneNumber = "1234567890",
            location = "New York, NY"
        )
        
        mockValidator.validationResult = ValidationResult(isValid = true)
        mockAuthRepository.signUpResult = Result.failure(Exception("Network error"))
        
        val result = useCase.execute(registrationData)
        
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `execute should handle auth repository exception`() = runTest {
        val registrationData = CarerRegistrationData(
            email = "carer@example.com",
            password = "SecurePass123",
            documents = listOf("certificate.pdf"),
            age = 30,
            phoneNumber = "1234567890",
            location = "New York, NY"
        )
        
        mockValidator.validationResult = ValidationResult(isValid = true)
        mockAuthRepository.shouldThrowException = true
        
        val result = useCase.execute(registrationData)
        
        assertTrue(result.isFailure)
        assertEquals("Auth repository exception", result.exceptionOrNull()?.message)
    }
}

// Mock implementations for testing
class MockAuthRepository : AuthRepository {
    var signUpResult: Result<AuthResult> = Result.success(
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
    var shouldThrowException = false
    
    override suspend fun signInWithEmail(email: String, password: String): Result<AuthResult> {
        TODO("Not needed for this test")
    }
    
    override suspend fun signUpCarer(carerData: CarerRegistrationData): Result<AuthResult> {
        if (shouldThrowException) {
            throw Exception("Auth repository exception")
        }
        return signUpResult
    }
    
    override suspend fun signUpCaree(
        careeData: com.carecomms.data.models.CareeRegistrationData,
        invitationToken: String
    ): Result<AuthResult> {
        TODO("Not needed for this test")
    }
    
    override suspend fun signOut(): Result<Unit> {
        TODO("Not needed for this test")
    }
    
    override suspend fun getCurrentUser(): com.carecomms.data.models.User? {
        TODO("Not needed for this test")
    }
    
    override suspend fun validateInvitationToken(token: String): Result<com.carecomms.data.models.CarerInfo> {
        TODO("Not needed for this test")
    }
    
    override suspend fun generateInvitationToken(carerId: String): Result<String> {
        TODO("Not needed for this test")
    }
    
    override suspend fun refreshToken(): Result<String> {
        TODO("Not needed for this test")
    }
    
    override fun isUserLoggedIn(): kotlinx.coroutines.flow.Flow<Boolean> {
        TODO("Not needed for this test")
    }
    
    override suspend fun deleteAccount(): Result<Unit> {
        TODO("Not needed for this test")
    }
}

class MockCarerRegistrationValidator : CarerRegistrationValidator() {
    var validationResult: ValidationResult = ValidationResult(isValid = true)
    
    override fun validate(data: CarerRegistrationData): ValidationResult {
        return validationResult
    }
}