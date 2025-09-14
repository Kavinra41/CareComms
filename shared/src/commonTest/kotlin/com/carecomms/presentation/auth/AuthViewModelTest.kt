package com.carecomms.presentation.auth

import com.carecomms.data.models.*
import com.carecomms.domain.usecase.AuthUseCase
import com.carecomms.presentation.state.AuthState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class AuthViewModelTest {
    
    private lateinit var mockAuthUseCase: MockAuthUseCase
    private lateinit var authViewModel: AuthViewModel
    
    @BeforeTest
    fun setup() {
        mockAuthUseCase = MockAuthUseCase()
        authViewModel = AuthViewModel(mockAuthUseCase)
    }
    
    @AfterTest
    fun tearDown() {
        authViewModel.onCleared()
    }
    
    @Test
    fun `initial state should be loading`() = runTest {
        // When
        val initialState = authViewModel.state.value
        
        // Then
        assertEquals(AuthState.Loading, initialState.authState)
        assertEquals("", initialState.email)
        assertEquals("", initialState.password)
        assertNull(initialState.error)
    }
    
    @Test
    fun `handleAction SignIn with valid credentials should update state to authenticated`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val user = Carer(
            id = "user1",
            email = email,
            createdAt = System.currentTimeMillis(),
            documents = emptyList(),
            age = 30,
            phoneNumber = "1234567890",
            location = "Test City",
            careeIds = emptyList()
        )
        val authResult = AuthResult(user, "token123")
        
        mockAuthUseCase.signInResult = Result.success(authResult)
        mockAuthUseCase.validateEmailResult = Result.success(email)
        mockAuthUseCase.validatePasswordResult = Result.success(password)
        
        // When
        authViewModel.handleAction(AuthAction.SignIn(email, password))
        
        // Wait for async operation to complete
        kotlinx.coroutines.delay(100)
        
        // Then
        val finalState = authViewModel.state.value
        assertTrue(finalState.authState is AuthState.Authenticated)
        assertEquals("user1", (finalState.authState as AuthState.Authenticated).userId)
        assertEquals("carer", (finalState.authState as AuthState.Authenticated).userType)
        assertFalse(finalState.isLoading)
        assertNull(finalState.error)
    }
    
    @Test
    fun `handleAction SignIn with invalid email should show validation error`() = runTest {
        // Given
        val email = "invalid-email"
        val password = "password123"
        
        mockAuthUseCase.validateEmailResult = Result.failure(Exception("Invalid email format"))
        mockAuthUseCase.validatePasswordResult = Result.success(password)
        
        // When
        authViewModel.handleAction(AuthAction.SignIn(email, password))
        
        // Wait for async operation to complete
        kotlinx.coroutines.delay(100)
        
        // Then
        val finalState = authViewModel.state.value
        assertEquals("Invalid email format", finalState.emailError)
        assertFalse(finalState.isLoading)
    }
    
    @Test
    fun `handleAction SignIn with invalid password should show validation error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "weak"
        
        mockAuthUseCase.validateEmailResult = Result.success(email)
        mockAuthUseCase.validatePasswordResult = Result.failure(Exception("Password must be at least 8 characters"))
        
        // When
        authViewModel.handleAction(AuthAction.SignIn(email, password))
        
        // Wait for async operation to complete
        kotlinx.coroutines.delay(100)
        
        // Then
        val finalState = authViewModel.state.value
        assertEquals("Password must be at least 8 characters", finalState.passwordError)
        assertFalse(finalState.isLoading)
    }
    
    @Test
    fun `handleAction SignOut should update state to unauthenticated`() = runTest {
        // Given
        mockAuthUseCase.signOutResult = Result.success(Unit)
        
        // When
        authViewModel.handleAction(AuthAction.SignOut)
        
        // Wait for async operation to complete
        kotlinx.coroutines.delay(100)
        
        // Then
        val finalState = authViewModel.state.value
        assertEquals(AuthState.Unauthenticated, finalState.authState)
        assertEquals("", finalState.email)
        assertEquals("", finalState.password)
        assertFalse(finalState.isLoading)
    }
    
    @Test
    fun `handleAction UpdateEmail should update email in state`() = runTest {
        // Given
        val newEmail = "newemail@example.com"
        
        // When
        authViewModel.handleAction(AuthAction.UpdateEmail(newEmail))
        
        // Then
        val finalState = authViewModel.state.value
        assertEquals(newEmail, finalState.email)
        assertNull(finalState.emailError)
    }
    
    @Test
    fun `handleAction UpdatePassword should update password in state`() = runTest {
        // Given
        val newPassword = "newpassword123"
        
        // When
        authViewModel.handleAction(AuthAction.UpdatePassword(newPassword))
        
        // Then
        val finalState = authViewModel.state.value
        assertEquals(newPassword, finalState.password)
        assertNull(finalState.passwordError)
    }
    
    @Test
    fun `handleAction ClearError should clear all errors`() = runTest {
        // Given - set some errors first
        authViewModel.handleAction(AuthAction.UpdateEmail("invalid"))
        mockAuthUseCase.validateEmailResult = Result.failure(Exception("Invalid email"))
        authViewModel.handleAction(AuthAction.SignIn("invalid", "weak"))
        kotlinx.coroutines.delay(100)
        
        // When
        authViewModel.handleAction(AuthAction.ClearError)
        
        // Then
        val finalState = authViewModel.state.value
        assertNull(finalState.error)
        assertNull(finalState.emailError)
        assertNull(finalState.passwordError)
    }
}

class MockAuthUseCase : AuthUseCase(MockAuthRepository()) {
    var signInResult: Result<AuthResult> = Result.failure(Exception("Not implemented"))
    var signOutResult: Result<Unit> = Result.failure(Exception("Not implemented"))
    var currentUser: User? = null
    var validateEmailResult: Result<String> = Result.failure(Exception("Not implemented"))
    var validatePasswordResult: Result<String> = Result.failure(Exception("Not implemented"))
    
    override suspend fun signIn(email: String, password: String): Result<AuthResult> {
        return signInResult
    }
    
    override suspend fun signOut(): Result<Unit> {
        return signOutResult
    }
    
    override suspend fun getCurrentUser(): User? {
        return currentUser
    }
    
    override fun validateEmail(email: String): Result<String> {
        return validateEmailResult
    }
    
    override fun validatePassword(password: String): Result<String> {
        return validatePasswordResult
    }
}

class MockAuthRepository : com.carecomms.domain.repository.AuthRepository {
    override suspend fun signInWithEmail(email: String, password: String): Result<AuthResult> {
        return Result.failure(Exception("Not implemented"))
    }
    
    override suspend fun signUpCarer(carerData: CarerRegistrationData): Result<AuthResult> {
        return Result.failure(Exception("Not implemented"))
    }
    
    override suspend fun signUpCaree(careeData: CareeRegistrationData, invitationToken: String): Result<AuthResult> {
        return Result.failure(Exception("Not implemented"))
    }
    
    override suspend fun signOut(): Result<Unit> {
        return Result.failure(Exception("Not implemented"))
    }
    
    override suspend fun getCurrentUser(): User? {
        return null
    }
    
    override suspend fun validateInvitationToken(token: String): Result<CarerInfo> {
        return Result.failure(Exception("Not implemented"))
    }
}