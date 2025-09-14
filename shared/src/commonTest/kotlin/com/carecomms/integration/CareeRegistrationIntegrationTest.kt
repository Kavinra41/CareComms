package com.carecomms.integration

import com.carecomms.data.database.DatabaseManager
import com.carecomms.data.models.*
import com.carecomms.data.repository.LocalInvitationRepository
import com.carecomms.data.repository.LocalUserRepository
import com.carecomms.data.validation.CareeRegistrationValidator
import com.carecomms.domain.usecase.CareeRegistrationUseCase
import com.carecomms.presentation.registration.CareeRegistrationViewModel
import com.carecomms.presentation.registration.CareeRegistrationIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class CareeRegistrationIntegrationTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    
    private lateinit var databaseManager: DatabaseManager
    private lateinit var userRepository: LocalUserRepository
    private lateinit var invitationRepository: LocalInvitationRepository
    private lateinit var authRepository: MockAuthRepository
    private lateinit var validator: CareeRegistrationValidator
    private lateinit var useCase: CareeRegistrationUseCase
    private lateinit var viewModel: CareeRegistrationViewModel
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Setup test database
        databaseManager = DatabaseManager(":memory:")
        userRepository = LocalUserRepository(databaseManager)
        invitationRepository = LocalInvitationRepository(databaseManager, userRepository)
        authRepository = MockAuthRepository()
        validator = CareeRegistrationValidator()
        
        useCase = CareeRegistrationUseCase(
            authRepository = authRepository,
            userRepository = userRepository,
            validator = validator
        )
        
        viewModel = CareeRegistrationViewModel(
            careeRegistrationUseCase = useCase,
            validator = validator,
            coroutineScope = testScope
        )
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `complete caree registration flow should work end to end`() = runTest {
        // Step 1: Create a carer and invitation
        val carer = createTestCarer()
        userRepository.saveUser(carer)
        
        val invitationResult = invitationRepository.generateInvitationLink(carer.id)
        assertTrue(invitationResult.isSuccess)
        
        val invitationUrl = invitationResult.getOrThrow()
        val token = extractTokenFromUrl(invitationUrl)
        
        // Step 2: Setup auth repository to return success
        val caree = createTestCaree()
        authRepository.validateInvitationResult = Result.success(
            CarerInfo(carer.id, "John Carer", carer.phoneNumber, carer.location)
        )
        authRepository.signUpCareeResult = Result.success(
            AuthResult(user = caree, token = "auth-token")
        )
        
        // Step 3: Validate invitation through ViewModel
        viewModel.handleIntent(CareeRegistrationIntent.ValidateInvitation(token))
        testScheduler.advanceUntilIdle()
        
        var state = viewModel.state.value
        assertTrue(state.isInvitationValid)
        assertNotNull(state.carerInfo)
        assertEquals(carer.id, state.carerInfo?.id)
        
        // Step 4: Fill in registration form
        viewModel.handleIntent(CareeRegistrationIntent.UpdateEmail("caree@example.com"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdatePassword("password123"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateConfirmPassword("password123"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateHealthInfo("No known allergies. Takes blood pressure medication."))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateFirstName("Jane"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateLastName("Doe"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateDateOfBirth("1950-05-15"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateAddress("123 Main St"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateEmergencyContact("John Doe - 555-0123"))
        
        state = viewModel.state.value
        assertTrue(state.isFormValid)
        
        // Step 5: Register caree
        viewModel.handleIntent(CareeRegistrationIntent.RegisterCaree)
        testScheduler.advanceUntilIdle()
        
        state = viewModel.state.value
        assertTrue(state.isRegistrationSuccessful)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        
        // Step 6: Verify caree was saved to database
        val savedCaree = userRepository.getUserById(caree.id)
        assertNotNull(savedCaree)
        assertTrue(savedCaree is Caree)
        assertEquals(caree.email, savedCaree.email)
        
        // Step 7: Verify carer-caree relationship was created
        val updatedCarer = userRepository.getUserById(carer.id) as? Carer
        assertNotNull(updatedCarer)
        assertTrue(updatedCarer.careeIds.contains(caree.id))
    }
    
    @Test
    fun `caree registration should fail with invalid invitation token`() = runTest {
        val invalidToken = "invalid-token"
        
        authRepository.validateInvitationResult = Result.failure(Exception("Invalid token"))
        
        viewModel.handleIntent(CareeRegistrationIntent.ValidateInvitation(invalidToken))
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.state.value
        assertFalse(state.isInvitationValid)
        assertEquals("Invalid or expired invitation link", state.errorMessage)
    }
    
    @Test
    fun `caree registration should fail with invalid form data`() = runTest {
        // Setup valid invitation
        val carer = createTestCarer()
        userRepository.saveUser(carer)
        
        authRepository.validateInvitationResult = Result.success(
            CarerInfo(carer.id, "John Carer", carer.phoneNumber, carer.location)
        )
        
        viewModel.handleIntent(CareeRegistrationIntent.ValidateInvitation("valid-token"))
        testScheduler.advanceUntilIdle()
        
        // Fill in invalid form data
        viewModel.handleIntent(CareeRegistrationIntent.UpdateEmail("invalid-email"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdatePassword("123")) // Too short
        viewModel.handleIntent(CareeRegistrationIntent.UpdateConfirmPassword("123"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateHealthInfo("")) // Empty
        viewModel.handleIntent(CareeRegistrationIntent.UpdateFirstName("")) // Empty
        viewModel.handleIntent(CareeRegistrationIntent.UpdateLastName("")) // Empty
        viewModel.handleIntent(CareeRegistrationIntent.UpdateDateOfBirth("invalid-date"))
        
        val state = viewModel.state.value
        assertFalse(state.isFormValid)
        assertTrue(state.validationErrors.isNotEmpty())
        
        // Try to register
        viewModel.handleIntent(CareeRegistrationIntent.RegisterCaree)
        
        val finalState = viewModel.state.value
        assertFalse(finalState.isRegistrationSuccessful)
        assertEquals("Please fill in all required fields correctly", finalState.errorMessage)
    }
    
    @Test
    fun `caree registration should handle auth failure gracefully`() = runTest {
        // Setup valid invitation and form
        val carer = createTestCarer()
        userRepository.saveUser(carer)
        
        authRepository.validateInvitationResult = Result.success(
            CarerInfo(carer.id, "John Carer", carer.phoneNumber, carer.location)
        )
        authRepository.signUpCareeResult = Result.failure(Exception("Auth service unavailable"))
        
        viewModel.handleIntent(CareeRegistrationIntent.ValidateInvitation("valid-token"))
        testScheduler.advanceUntilIdle()
        
        // Fill valid form
        fillValidForm()
        
        // Try to register
        viewModel.handleIntent(CareeRegistrationIntent.RegisterCaree)
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.state.value
        assertFalse(state.isRegistrationSuccessful)
        assertEquals("Auth service unavailable", state.errorMessage)
    }
    
    private fun createTestCarer(): Carer {
        return Carer(
            id = "carer-123",
            email = "carer@example.com",
            createdAt = System.currentTimeMillis(),
            documents = listOf("license.pdf", "certification.pdf"),
            age = 35,
            phoneNumber = "555-0123",
            location = "New York, NY",
            careeIds = emptyList()
        )
    }
    
    private fun createTestCaree(): Caree {
        return Caree(
            id = "caree-456",
            email = "caree@example.com",
            createdAt = System.currentTimeMillis(),
            healthInfo = "No known allergies. Takes blood pressure medication.",
            personalDetails = PersonalDetails(
                firstName = "Jane",
                lastName = "Doe",
                dateOfBirth = "1950-05-15",
                address = "123 Main St",
                emergencyContact = "John Doe - 555-0123"
            ),
            carerId = "carer-123"
        )
    }
    
    private fun extractTokenFromUrl(url: String): String {
        // Extract token from URL like "carecomms://invite?token=abc123"
        val tokenStart = url.indexOf("token=") + 6
        return url.substring(tokenStart)
    }
    
    private fun fillValidForm() {
        viewModel.handleIntent(CareeRegistrationIntent.UpdateEmail("caree@example.com"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdatePassword("password123"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateConfirmPassword("password123"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateHealthInfo("No known allergies"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateFirstName("Jane"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateLastName("Doe"))
        viewModel.handleIntent(CareeRegistrationIntent.UpdateDateOfBirth("1950-05-15"))
    }
}

// Mock Auth Repository for integration testing
class MockAuthRepository : com.carecomms.data.repository.AuthRepository {
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
    
    override suspend fun signInWithEmail(email: String, password: String): Result<AuthResult> = TODO()
    override suspend fun signUpCarer(carerData: CarerRegistrationData): Result<AuthResult> = TODO()
    
    override suspend fun signUpCaree(careeData: CareeRegistrationData, invitationToken: String): Result<AuthResult> {
        return signUpCareeResult
    }
    
    override suspend fun signOut(): Result<Unit> = TODO()
    override suspend fun getCurrentUser(): User? = TODO()
    
    override suspend fun validateInvitationToken(token: String): Result<CarerInfo> {
        return validateInvitationResult
    }
    
    override suspend fun generateInvitationToken(carerId: String): Result<String> = TODO()
    override suspend fun refreshToken(): Result<String> = TODO()
    override fun isUserLoggedIn(): kotlinx.coroutines.flow.Flow<Boolean> = TODO()
    override suspend fun deleteAccount(): Result<Unit> = TODO()
}