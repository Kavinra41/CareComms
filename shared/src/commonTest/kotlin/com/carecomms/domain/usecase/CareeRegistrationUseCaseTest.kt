package com.carecomms.domain.usecase

import com.carecomms.data.models.*
import com.carecomms.data.repository.AuthRepository
import com.carecomms.data.repository.LocalUserRepository
import com.carecomms.data.validation.CareeRegistrationValidator
import com.carecomms.data.validation.CareeValidationError
import com.carecomms.data.validation.CareeValidationResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CareeRegistrationUseCaseTest {
    
    private val mockAuthRepository = MockAuthRepository()
    private val mockUserRepository = MockUserRepository()
    private val mockValidator = MockCareeRegistrationValidator()
    
    private val useCase = CareeRegistrationUseCase(
        authRepository = mockAuthRepository,
        userRepository = mockUserRepository,
        validator = mockValidator
    )
    
    @Test
    fun `registerCaree should succeed with valid data and invitation`() = runTest {
        // Arrange
        val registrationData = createValidRegistrationData()
        val invitationToken = "valid-token"
        val carerInfo = CarerInfo("carer-id", "John Carer", "555-0123", "City")
        val authResult = AuthResult(
            user = createCaree("caree-id"),
            token = "auth-token"
        )
        
        mockValidator.validationResult = CareeValidationResult(isValid = true)
        mockAuthRepository.validateInvitationResult = Result.success(carerInfo)
        mockAuthRepository.signUpCareeResult = Result.success(authResult)
        
        // Act
        val result = useCase.registerCaree(registrationData, invitationToken)
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals(authResult, result.getOrNull())
        assertTrue(mockUserRepository.savedUsers.contains(authResult.user))
    }
    
    @Test
    fun `registerCaree should fail with invalid registration data`() = runTest {
        // Arrange
        val registrationData = createValidRegistrationData()
        val invitationToken = "valid-token"
        
        mockValidator.validationResult = CareeValidationResult(
            isValid = false,
            errors = listOf(CareeValidationError.InvalidEmail)
        )
        
        // Act
        val result = useCase.registerCaree(registrationData, invitationToken)
        
        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationException)
    }
    
    @Test
    fun `registerCaree should fail with invalid invitation token`() = runTest {
        // Arrange
        val registrationData = createValidRegistrationData()
        val invitationToken = "invalid-token"
        
        mockValidator.validationResult = CareeValidationResult(isValid = true)
        mockAuthRepository.validateInvitationResult = Result.failure(Exception("Invalid token"))
        
        // Act
        val result = useCase.registerCaree(registrationData, invitationToken)
        
        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvitationException)
    }
    
    @Test
    fun `registerCaree should fail when auth registration fails`() = runTest {
        // Arrange
        val registrationData = createValidRegistrationData()
        val invitationToken = "valid-token"
        val carerInfo = CarerInfo("carer-id", "John Carer", "555-0123", "City")
        
        mockValidator.validationResult = CareeValidationResult(isValid = true)
        mockAuthRepository.validateInvitationResult = Result.success(carerInfo)
        mockAuthRepository.signUpCareeResult = Result.failure(Exception("Auth failed"))
        
        // Act
        val result = useCase.registerCaree(registrationData, invitationToken)
        
        // Assert
        assertTrue(result.isFailure)
        assertEquals("Auth failed", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `validateInvitationToken should delegate to auth repository`() = runTest {
        // Arrange
        val token = "test-token"
        val carerInfo = CarerInfo("carer-id", "John Carer", "555-0123", "City")
        mockAuthRepository.validateInvitationResult = Result.success(carerInfo)
        
        // Act
        val result = useCase.validateInvitationToken(token)
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals(carerInfo, result.getOrNull())
    }
    
    @Test
    fun `registerCaree should update carer-caree relationship`() = runTest {
        // Arrange
        val registrationData = createValidRegistrationData()
        val invitationToken = "valid-token"
        val carerId = "carer-id"
        val careeId = "caree-id"
        
        val carer = Carer(
            id = carerId,
            email = "carer@example.com",
            createdAt = System.currentTimeMillis(),
            documents = listOf("doc1"),
            age = 30,
            phoneNumber = "555-0123",
            location = "City",
            careeIds = emptyList()
        )
        
        val caree = createCaree(careeId)
        val carerInfo = CarerInfo(carerId, "John Carer", "555-0123", "City")
        val authResult = AuthResult(user = caree, token = "auth-token")
        
        mockValidator.validationResult = CareeValidationResult(isValid = true)
        mockAuthRepository.validateInvitationResult = Result.success(carerInfo)
        mockAuthRepository.signUpCareeResult = Result.success(authResult)
        mockUserRepository.users[carerId] = carer
        
        // Act
        val result = useCase.registerCaree(registrationData, invitationToken)
        
        // Assert
        assertTrue(result.isSuccess)
        val updatedCarer = mockUserRepository.users[carerId] as? Carer
        assertTrue(updatedCarer?.careeIds?.contains(careeId) == true)
    }
    
    private fun createValidRegistrationData(): CareeRegistrationData {
        return CareeRegistrationData(
            email = "caree@example.com",
            password = "password123",
            healthInfo = "No known allergies",
            basicDetails = PersonalDetails(
                firstName = "Jane",
                lastName = "Doe",
                dateOfBirth = "1950-05-15",
                address = "123 Main St",
                emergencyContact = "John Doe - 555-0123"
            )
        )
    }
    
    private fun createCaree(id: String): Caree {
        return Caree(
            id = id,
            email = "caree@example.com",
            createdAt = System.currentTimeMillis(),
            healthInfo = "No known allergies",
            personalDetails = PersonalDetails(
                firstName = "Jane",
                lastName = "Doe",
                dateOfBirth = "1950-05-15"
            ),
            carerId = "carer-id"
        )
    }
}

// Mock implementations
class MockAuthRepository : AuthRepository {
    var validateInvitationResult: Result<CarerInfo> = Result.success(
        CarerInfo("carer-id", "John Carer", "555-0123", "City")
    )
    var signUpCareeResult: Result<AuthResult> = Result.success(
        AuthResult(
            user = Caree(
                id = "caree-id",
                email = "caree@example.com",
                createdAt = System.currentTimeMillis(),
                healthInfo = "Health info",
                personalDetails = PersonalDetails("Jane", "Doe", "1950-05-15"),
                carerId = "carer-id"
            ),
            token = "auth-token"
        )
    )
    
    override suspend fun signInWithEmail(email: String, password: String): Result<AuthResult> {
        TODO("Not needed for this test")
    }
    
    override suspend fun signUpCarer(carerData: CarerRegistrationData): Result<AuthResult> {
        TODO("Not needed for this test")
    }
    
    override suspend fun signUpCaree(careeData: CareeRegistrationData, invitationToken: String): Result<AuthResult> {
        return signUpCareeResult
    }
    
    override suspend fun signOut(): Result<Unit> {
        TODO("Not needed for this test")
    }
    
    override suspend fun getCurrentUser(): User? {
        TODO("Not needed for this test")
    }
    
    override suspend fun validateInvitationToken(token: String): Result<CarerInfo> {
        return validateInvitationResult
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

class MockUserRepository : LocalUserRepository {
    val users = mutableMapOf<String, User>()
    val savedUsers = mutableListOf<User>()
    
    override suspend fun saveUser(user: User) {
        users[user.id] = user
        savedUsers.add(user)
    }
    
    override suspend fun getUserById(id: String): User? {
        return users[id]
    }
    
    override suspend fun getUserByEmail(email: String): User? {
        return users.values.find { it.email == email }
    }
    
    override suspend fun deleteUser(id: String) {
        users.remove(id)
    }
    
    override suspend fun getAllUsers(): List<User> {
        return users.values.toList()
    }
    
    override suspend fun updateUser(user: User) {
        users[user.id] = user
    }
}

class MockCareeRegistrationValidator : CareeRegistrationValidator() {
    var validationResult: CareeValidationResult = CareeValidationResult(isValid = true)
    
    override fun validate(data: CareeRegistrationData): CareeValidationResult {
        return validationResult
    }
}