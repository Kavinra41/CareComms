package com.carecomms.domain.usecase

import com.carecomms.data.models.*
import com.carecomms.domain.repository.AuthRepository
import com.carecomms.presentation.state.AuthState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class AuthUseCaseTest {
    
    private lateinit var mockAuthRepository: MockAuthRepository
    private lateinit var authUseCase: AuthUseCase
    
    @BeforeTest
    fun setup() {
        mockAuthRepository = MockAuthRepository()
        authUseCase = AuthUseCase(mockAuthRepository)
    }
    
    @Test
    fun `signIn with valid credentials should return success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val expectedUser = Carer(
            id = "user1",
            email = email,
            createdAt = System.currentTimeMillis(),
            documents = emptyList(),
            age = 30,
            phoneNumber = "1234567890",
            location = "Test City",
            careeIds = emptyList()
        )
        val expectedResult = AuthResult(expectedUser, "token123")
        mockAuthRepository.signInResult = Result.success(expectedResult)
        
        // When
        val result = authUseCase.signIn(email, password)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
    }
    
    @Test
    fun `signIn with empty email should return failure`() = runTest {
        // Given
        val email = ""
        val password = "password123"
        
        // When
        val result = authUseCase.signIn(email, password)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("required") == true)
    }
    
    @Test
    fun `signIn with empty password should return failure`() = runTest {
        // Given
        val email = "test@example.com"
        val password = ""
        
        // When
        val result = authUseCase.signIn(email, password)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("required") == true)
    }
    
    @Test
    fun `signOut should call repository signOut`() = runTest {
        // Given
        mockAuthRepository.signOutResult = Result.success(Unit)
        
        // When
        val result = authUseCase.signOut()
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(mockAuthRepository.signOutCalled)
    }
    
    @Test
    fun `getCurrentUser should return user from repository`() = runTest {
        // Given
        val expectedUser = Carer(
            id = "user1",
            email = "test@example.com",
            createdAt = System.currentTimeMillis(),
            documents = emptyList(),
            age = 30,
            phoneNumber = "1234567890",
            location = "Test City",
            careeIds = emptyList()
        )
        mockAuthRepository.currentUser = expectedUser
        
        // When
        val result = authUseCase.getCurrentUser()
        
        // Then
        assertEquals(expectedUser, result)
    }
    
    @Test
    fun `isAuthenticated should return true when user exists`() = runTest {
        // Given
        mockAuthRepository.currentUser = Carer(
            id = "user1",
            email = "test@example.com",
            createdAt = System.currentTimeMillis(),
            documents = emptyList(),
            age = 30,
            phoneNumber = "1234567890",
            location = "Test City",
            careeIds = emptyList()
        )
        
        // When
        val result = authUseCase.isAuthenticated()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isAuthenticated should return false when no user`() = runTest {
        // Given
        mockAuthRepository.currentUser = null
        
        // When
        val result = authUseCase.isAuthenticated()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `getAuthState should return Authenticated for carer`() = runTest {
        // Given
        val carer = Carer(
            id = "user1",
            email = "test@example.com",
            createdAt = System.currentTimeMillis(),
            documents = emptyList(),
            age = 30,
            phoneNumber = "1234567890",
            location = "Test City",
            careeIds = emptyList()
        )
        mockAuthRepository.currentUser = carer
        
        // When
        val authState = authUseCase.getAuthState().first()
        
        // Then
        assertTrue(authState is AuthState.Authenticated)
        assertEquals("user1", (authState as AuthState.Authenticated).userId)
        assertEquals("carer", authState.userType)
    }
    
    @Test
    fun `getAuthState should return Authenticated for caree`() = runTest {
        // Given
        val caree = Caree(
            id = "user2",
            email = "caree@example.com",
            createdAt = System.currentTimeMillis(),
            healthInfo = "Good health",
            personalDetails = PersonalDetails("John", "Doe", 75),
            carerId = "carer1"
        )
        mockAuthRepository.currentUser = caree
        
        // When
        val authState = authUseCase.getAuthState().first()
        
        // Then
        assertTrue(authState is AuthState.Authenticated)
        assertEquals("user2", (authState as AuthState.Authenticated).userId)
        assertEquals("caree", authState.userType)
    }
    
    @Test
    fun `validateEmail should accept valid email`() = runTest {
        // Given
        val validEmail = "test@example.com"
        
        // When
        val result = authUseCase.validateEmail(validEmail)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(validEmail, result.getOrNull())
    }
    
    @Test
    fun `validateEmail should reject invalid email`() = runTest {
        // Given
        val invalidEmail = "invalid-email"
        
        // When
        val result = authUseCase.validateEmail(invalidEmail)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Invalid email") == true)
    }
    
    @Test
    fun `validatePassword should accept strong password`() = runTest {
        // Given
        val strongPassword = "password123"
        
        // When
        val result = authUseCase.validatePassword(strongPassword)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(strongPassword, result.getOrNull())
    }
    
    @Test
    fun `validatePassword should reject weak password`() = runTest {
        // Given
        val weakPassword = "weak"
        
        // When
        val result = authUseCase.validatePassword(weakPassword)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("at least 8 characters") == true)
    }
}

class MockAuthRepository : AuthRepository {
    var signInResult: Result<AuthResult> = Result.failure(Exception("Not implemented"))
    var signOutResult: Result<Unit> = Result.failure(Exception("Not implemented"))
    var currentUser: User? = null
    var signOutCalled = false
    
    override suspend fun signInWithEmail(email: String, password: String): Result<AuthResult> {
        return signInResult
    }
    
    override suspend fun signUpCarer(carerData: CarerRegistrationData): Result<AuthResult> {
        return Result.failure(Exception("Not implemented"))
    }
    
    override suspend fun signUpCaree(careeData: CareeRegistrationData, invitationToken: String): Result<AuthResult> {
        return Result.failure(Exception("Not implemented"))
    }
    
    override suspend fun signOut(): Result<Unit> {
        signOutCalled = true
        return signOutResult
    }
    
    override suspend fun getCurrentUser(): User? {
        return currentUser
    }
    
    override suspend fun validateInvitationToken(token: String): Result<CarerInfo> {
        return Result.failure(Exception("Not implemented"))
    }
}