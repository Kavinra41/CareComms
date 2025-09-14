package com.carecomms.presentation.registration

import com.carecomms.data.models.*
import com.carecomms.data.validation.CareeRegistrationValidator
import com.carecomms.data.validation.CareeValidationError
import com.carecomms.data.validation.CareeValidationResult
import com.carecomms.domain.usecase.CareeRegistrationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class CareeRegistrationViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    
    private val mockUseCase = MockCareeRegistrationUseCase()
    private val mockValidator = MockCareeValidator()
    
    private lateinit var viewModel: CareeRegistrationViewModel
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CareeRegistrationViewModel(
            careeRegistrationUseCase = mockUseCase,
            validator = mockValidator,
            coroutineScope = testScope
        )
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be correct`() {
        val state = viewModel.state.value
        
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.confirmPassword)
        assertEquals("", state.healthInfo)
        assertEquals("", state.firstName)
        assertEquals("", state.lastName)
        assertEquals("", state.dateOfBirth)
        assertEquals("", state.address)
        assertEquals("", state.emergencyContact)
        assertEquals("", state.invitationToken)
        assertNull(state.carerInfo)
        assertTrue(state.validationErrors.isEmpty())
        assertFalse(state.isRegistrationSuccessful)
        assertNull(state.errorMessage)
        assertFalse(state.isInvitationValid)
        assertFalse(state.isValidatingInvitation)
        assertFalse(state.isLoading)
    }
    
    @Test
    fun `updateEmail should update state and validate form`() {
        val email = "test@example.com"
        
        viewModel.handleIntent(CareeRegistrationIntent.UpdateEmail(email))
        
        assertEquals(email, viewModel.state.value.email)
        assertTrue(mockValidator.validateCalled)
    }
    
    @Test
    fun `updatePassword should update state and validate form`() {
        val password = "password123"
        
        viewModel.handleIntent(CareeRegistrationIntent.UpdatePassword(password))
        
        assertEquals(password, viewModel.state.value.password)
        assertTrue(mockValidator.validateCalled)
    }
    
    @Test
    fun `updateHealthInfo should update state and validate form`() {
        val healthInfo = "No known allergies"
        
        viewModel.handleIntent(CareeRegistrationIntent.UpdateHealthInfo(healthInfo))
        
        assertEquals(healthInfo, viewModel.state.value.healthInfo)
        assertTrue(mockValidator.validateCalled)
    }
    
    @Test
    fun `validateInvitation should call use case and update state on success`() = runTest {
        val token = "valid-token"
        val carerInfo = CarerInfo("carer-id", "John Carer", "555-0123", "City")
        mockUseCase.validateInvitationResult = Result.success(carerInfo)
        
        viewModel.handleIntent(CareeRegistrationIntent.ValidateInvitation(token))
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.state.value
        assertEquals(token, state.invitationToken)
        assertEquals(carerInfo, state.carerInfo)
        assertTrue(state.isInvitationValid)
        assertFalse(state.isValidatingInvitation)
        assertNull(state.errorMessage)
    }
    
    @Test
    fun `validateInvitation should update state on failure`() = runTest {
        val token = "invalid-token"
        mockUseCase.validateInvitationResult = Result.failure(Exception("Invalid token"))
        
        viewModel.handleIntent(CareeRegistrationIntent.ValidateInvitation(token))
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.state.value
        assertEquals(token, state.invitationToken)
        assertNull(state.carerInfo)
        assertFalse(state.isInvitationValid)
        assertFalse(state.isValidatingInvitation)
        assertEquals("Invalid or expired invitation link", state.errorMessage)
    }
    
    @Test
    fun `registerCaree should succeed with valid form`() = runTest {
        // Setup valid state
        setupValidState()
        mockValidator.validationResult = CareeValidationResult(isValid = true)
        mockUseCase.registerCareeResult = Result.success(
            AuthResult(
                user = createCaree(),
                token = "auth-token"
            )
        )
        
        viewModel.handleIntent(CareeRegistrationIntent.RegisterCaree)
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isRegistrationSuccessful)
        assertNull(state.errorMessage)
    }
    
    @Test
    fun `registerCaree should fail with invalid form`() {
        // Don't setup valid state - form will be invalid
        mockValidator.validationResult = CareeValidationResult(
            isValid = false,
            errors = listOf(CareeValidationError.InvalidEmail)
        )
        
        viewModel.handleIntent(CareeRegistrationIntent.RegisterCaree)
        
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isRegistrationSuccessful)
        assertEquals("Please fill in all required fields correctly", state.errorMessage)
    }
    
    @Test
    fun `registerCaree should handle registration failure`() = runTest {
        setupValidState()
        mockValidator.validationResult = CareeValidationResult(isValid = true)
        mockUseCase.registerCareeResult = Result.failure(Exception("Registration failed"))
        
        viewModel.handleIntent(CareeRegistrationIntent.RegisterCaree)
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isRegistrationSuccessful)
        assertEquals("Registration failed", state.errorMessage)
    }
    
    @Test
    fun `clearError should clear error message`() {
        // Set an error first
        viewModel.handleIntent(CareeRegistrationIntent.RegisterCaree)
        assertTrue(viewModel.state.value.errorMessage != null)
        
        viewModel.handleIntent(CareeRegistrationIntent.ClearError)
        
        assertNull(viewModel.state.value.errorMessage)
    }
    
    @Test
    fun `isFormValid should return true for complete valid form`() {
        setupValidState()
        mockValidator.validationResult = CareeValidationResult(isValid = true)
        
        // Trigger validation
        viewModel.handleIntent(CareeRegistrationIntent.UpdateEmail("test@example.com"))
        
        assertTrue(viewModel.state.value.isFormValid)
    }
    
    @Test
    fun `isFormValid should return false for incomplete form`() {
        // Don't setup complete state
        viewModel.handleIntent(CareeRegistrationIntent.UpdateEmail("test@example.com"))
        
        assertFalse(viewModel.state.value.isFormValid)
    }
    
    private fun setupValidState() {
        viewModel.handleIntent(CareeRegistrationIntent.UpdateEmail("caree@example.com"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdatePassword("password123"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateConfirmPassword("password123"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateHealthInfo("No known allergies"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateFirstName("Jane"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateLastName("Doe"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateDateOfBirth("1950-05-15"))
        
        // Setup valid invitation
        val carerInfo = CarerInfo("carer-id", "John Carer", "555-0123", "City")
        mockUseCase.validateInvitationResult = Result.success(carerInfo)
        viewModel.handleIntent(CareeRegistrationIntent.ValidateInvitation("valid-token"))
        testScope.testScheduler.advanceUntilIdle()
    }
    
    private fun createCaree(): Caree {
        return Caree(
            id = "caree-id",
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
class MockCareeRegistrationUseCase : CareeRegistrationUseCase(
    authRepository = object : com.carecomms.data.repository.AuthRepository {
        override suspend fun signInWithEmail(email: String, password: String): Result<AuthResult> = TODO()
        override suspend fun signUpCarer(carerData: CarerRegistrationData): Result<AuthResult> = TODO()
        override suspend fun signUpCaree(careeData: CareeRegistrationData, invitationToken: String): Result<AuthResult> = TODO()
        override suspend fun signOut(): Result<Unit> = TODO()
        override suspend fun getCurrentUser(): User? = TODO()
        override suspend fun validateInvitationToken(token: String): Result<CarerInfo> = TODO()
        override suspend fun generateInvitationToken(carerId: String): Result<String> = TODO()
        override suspend fun refreshToken(): Result<String> = TODO()
        override fun isUserLoggedIn(): kotlinx.coroutines.flow.Flow<Boolean> = TODO()
        override suspend fun deleteAccount(): Result<Unit> = TODO()
    },
    userRepository = object : com.carecomms.data.repository.LocalUserRepository {
        override suspend fun saveUser(user: User) = TODO()
        override suspend fun getUserById(id: String): User? = TODO()
        override suspend fun getUserByEmail(email: String): User? = TODO()
        override suspend fun deleteUser(id: String) = TODO()
        override suspend fun getAllUsers(): List<User> = TODO()
        override suspend fun updateUser(user: User) = TODO()
    },
    validator = CareeRegistrationValidator()
) {
    var validateInvitationResult: Result<CarerInfo> = Result.success(
        CarerInfo("carer-id", "John Carer", "555-0123", "City")
    )
    var registerCareeResult: Result<AuthResult> = Result.success(
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
    
    override suspend fun validateInvitationToken(token: String): Result<CarerInfo> {
        return validateInvitationResult
    }
    
    override suspend fun registerCaree(
        registrationData: CareeRegistrationData,
        invitationToken: String
    ): Result<AuthResult> {
        return registerCareeResult
    }
}

class MockCareeValidator : CareeRegistrationValidator() {
    var validationResult: CareeValidationResult = CareeValidationResult(isValid = true)
    var validateCalled = false
    
    override fun validate(data: CareeRegistrationData): CareeValidationResult {
        validateCalled = true
        return validationResult
    }
}