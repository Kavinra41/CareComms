package com.carecomms.data.repository

import com.carecomms.data.models.*
import com.carecomms.data.storage.SecureStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class MockSecureStorage : SecureStorage {
    private val storage = mutableMapOf<String, String>()
    
    override suspend fun storeToken(key: String, token: String): Result<Unit> {
        storage[key] = token
        return Result.success(Unit)
    }
    
    override suspend fun getToken(key: String): Result<String?> {
        return Result.success(storage[key])
    }
    
    override suspend fun removeToken(key: String): Result<Unit> {
        storage.remove(key)
        return Result.success(Unit)
    }
    
    override suspend fun clearAll(): Result<Unit> {
        storage.clear()
        return Result.success(Unit)
    }
}

class MockAuthRepository(
    private val secureStorage: SecureStorage
) : AuthRepository {
    
    private var shouldFailAuth = false
    private var currentUser: User? = null
    
    fun setShouldFailAuth(shouldFail: Boolean) {
        shouldFailAuth = shouldFail
    }
    
    override suspend fun signInWithEmail(email: String, password: String): Result<AuthResult> {
        if (shouldFailAuth) {
            return Result.failure(AuthError.InvalidCredentials)
        }
        
        if (email.isEmpty() || password.length < 6) {
            return Result.failure(AuthError.InvalidCredentials)
        }
        
        val user = Carer(
            id = "test_user_id",
            email = email,
            createdAt = 1234567890L,
            documents = emptyList(),
            age = 30,
            phoneNumber = "123-456-7890",
            location = "Test City",
            careeIds = emptyList()
        )
        
        currentUser = user
        val token = "test_token_123"
        
        secureStorage.storeToken("auth_token", token)
        secureStorage.storeToken("user_id", user.id)
        secureStorage.storeToken("user_email", user.email)
        
        return Result.success(AuthResult(user, token))
    }
    
    override suspend fun signUpCarer(carerData: CarerRegistrationData): Result<AuthResult> {
        if (shouldFailAuth) {
            return Result.failure(AuthError.EmailAlreadyInUse)
        }
        
        if (carerData.email.isEmpty() || carerData.password.length < 6) {
            return Result.failure(AuthError.WeakPassword)
        }
        
        val carer = Carer(
            id = "new_carer_id",
            email = carerData.email,
            createdAt = 1234567890L,
            documents = carerData.documents,
            age = carerData.age,
            phoneNumber = carerData.phoneNumber,
            location = carerData.location,
            careeIds = emptyList()
        )
        
        currentUser = carer
        val token = "new_carer_token"
        
        secureStorage.storeToken("auth_token", token)
        secureStorage.storeToken("user_id", carer.id)
        secureStorage.storeToken("user_email", carer.email)
        
        return Result.success(AuthResult(carer, token))
    }
    
    override suspend fun signUpCaree(careeData: CareeRegistrationData, invitationToken: String): Result<AuthResult> {
        if (shouldFailAuth) {
            return Result.failure(AuthError.InvalidInvitationToken)
        }
        
        val carerInfo = validateInvitationToken(invitationToken).getOrElse { 
            return Result.failure(it as AuthError)
        }
        
        if (careeData.email.isEmpty() || careeData.password.length < 6) {
            return Result.failure(AuthError.WeakPassword)
        }
        
        val caree = Caree(
            id = "new_caree_id",
            email = careeData.email,
            createdAt = 1234567890L,
            healthInfo = careeData.healthInfo,
            personalDetails = careeData.basicDetails,
            carerId = carerInfo.id
        )
        
        currentUser = caree
        val token = "new_caree_token"
        
        secureStorage.storeToken("auth_token", token)
        secureStorage.storeToken("user_id", caree.id)
        secureStorage.storeToken("user_email", caree.email)
        
        return Result.success(AuthResult(caree, token))
    }
    
    override suspend fun signOut(): Result<Unit> {
        currentUser = null
        secureStorage.clearAll()
        return Result.success(Unit)
    }
    
    override suspend fun getCurrentUser(): User? = currentUser
    
    override suspend fun validateInvitationToken(token: String): Result<CarerInfo> {
        if (token.length < 10) {
            return Result.failure(AuthError.InvalidInvitationToken)
        }
        
        return Result.success(CarerInfo(
            id = "test_carer_id",
            name = "Test Carer",
            email = "carer@test.com"
        ))
    }
    
    override suspend fun generateInvitationToken(carerId: String): Result<String> {
        return Result.success("invitation_token_$carerId")
    }
    
    override suspend fun refreshToken(): Result<String> {
        val newToken = "refreshed_token_123"
        secureStorage.storeToken("auth_token", newToken)
        return Result.success(newToken)
    }
    
    override fun isUserLoggedIn() = kotlinx.coroutines.flow.flow {
        emit(currentUser != null)
    }
    
    override suspend fun deleteAccount(): Result<Unit> {
        currentUser = null
        secureStorage.clearAll()
        return Result.success(Unit)
    }
}

class AuthRepositoryTest {
    
    private lateinit var secureStorage: MockSecureStorage
    private lateinit var authRepository: MockAuthRepository
    
    @BeforeTest
    fun setup() {
        secureStorage = MockSecureStorage()
        authRepository = MockAuthRepository(secureStorage)
    }
    
    @Test
    fun testSuccessfulCarerSignIn() = runTest {
        val result = authRepository.signInWithEmail("test@example.com", "password123")
        
        assertTrue(result.isSuccess)
        val authResult = result.getOrNull()!!
        assertEquals("test@example.com", authResult.user.email)
        assertEquals("test_token_123", authResult.token)
        
        // Verify tokens are stored
        assertEquals("test_token_123", secureStorage.getToken("auth_token").getOrNull())
        assertEquals("test_user_id", secureStorage.getToken("user_id").getOrNull())
    }
    
    @Test
    fun testFailedSignInWithInvalidCredentials() = runTest {
        val result = authRepository.signInWithEmail("", "123")
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AuthError.InvalidCredentials)
    }
    
    @Test
    fun testSuccessfulCarerSignUp() = runTest {
        val carerData = CarerRegistrationData(
            email = "newcarer@example.com",
            password = "password123",
            documents = listOf("doc1.pdf", "doc2.pdf"),
            age = 35,
            phoneNumber = "555-0123",
            location = "New York"
        )
        
        val result = authRepository.signUpCarer(carerData)
        
        assertTrue(result.isSuccess)
        val authResult = result.getOrNull()!!
        assertTrue(authResult.user is Carer)
        
        val carer = authResult.user as Carer
        assertEquals("newcarer@example.com", carer.email)
        assertEquals(35, carer.age)
        assertEquals("555-0123", carer.phoneNumber)
        assertEquals("New York", carer.location)
    }
    
    @Test
    fun testFailedCarerSignUpWithWeakPassword() = runTest {
        val carerData = CarerRegistrationData(
            email = "newcarer@example.com",
            password = "123",
            documents = emptyList(),
            age = 35,
            phoneNumber = "555-0123",
            location = "New York"
        )
        
        val result = authRepository.signUpCarer(carerData)
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AuthError.WeakPassword)
    }
    
    @Test
    fun testSuccessfulCareeSignUp() = runTest {
        val personalDetails = PersonalDetails(
            firstName = "Jane",
            lastName = "Doe",
            dateOfBirth = "1950-01-01",
            address = "123 Main St",
            emergencyContact = "555-0199"
        )
        
        val careeData = CareeRegistrationData(
            email = "caree@example.com",
            password = "password123",
            healthInfo = "Diabetes, High Blood Pressure",
            basicDetails = personalDetails
        )
        
        val result = authRepository.signUpCaree(careeData, "valid_invitation_token_123")
        
        assertTrue(result.isSuccess)
        val authResult = result.getOrNull()!!
        assertTrue(authResult.user is Caree)
        
        val caree = authResult.user as Caree
        assertEquals("caree@example.com", caree.email)
        assertEquals("Diabetes, High Blood Pressure", caree.healthInfo)
        assertEquals("test_carer_id", caree.carerId)
    }
    
    @Test
    fun testFailedCareeSignUpWithInvalidInvitation() = runTest {
        val personalDetails = PersonalDetails(
            firstName = "Jane",
            lastName = "Doe",
            dateOfBirth = "1950-01-01"
        )
        
        val careeData = CareeRegistrationData(
            email = "caree@example.com",
            password = "password123",
            healthInfo = "Diabetes",
            basicDetails = personalDetails
        )
        
        val result = authRepository.signUpCaree(careeData, "short")
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AuthError.InvalidInvitationToken)
    }
    
    @Test
    fun testSignOut() = runTest {
        // First sign in
        authRepository.signInWithEmail("test@example.com", "password123")
        assertNotNull(authRepository.getCurrentUser())
        
        // Then sign out
        val result = authRepository.signOut()
        
        assertTrue(result.isSuccess)
        assertNull(authRepository.getCurrentUser())
        assertNull(secureStorage.getToken("auth_token").getOrNull())
    }
    
    @Test
    fun testValidateInvitationToken() = runTest {
        val result = authRepository.validateInvitationToken("valid_token_123456")
        
        assertTrue(result.isSuccess)
        val carerInfo = result.getOrNull()!!
        assertEquals("test_carer_id", carerInfo.id)
        assertEquals("Test Carer", carerInfo.name)
        assertEquals("carer@test.com", carerInfo.email)
    }
    
    @Test
    fun testInvalidInvitationToken() = runTest {
        val result = authRepository.validateInvitationToken("short")
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AuthError.InvalidInvitationToken)
    }
    
    @Test
    fun testGenerateInvitationToken() = runTest {
        val result = authRepository.generateInvitationToken("carer123")
        
        assertTrue(result.isSuccess)
        val token = result.getOrNull()!!
        assertEquals("invitation_token_carer123", token)
    }
    
    @Test
    fun testRefreshToken() = runTest {
        val result = authRepository.refreshToken()
        
        assertTrue(result.isSuccess)
        val newToken = result.getOrNull()!!
        assertEquals("refreshed_token_123", newToken)
        assertEquals(newToken, secureStorage.getToken("auth_token").getOrNull())
    }
    
    @Test
    fun testDeleteAccount() = runTest {
        // First sign in
        authRepository.signInWithEmail("test@example.com", "password123")
        assertNotNull(authRepository.getCurrentUser())
        
        // Then delete account
        val result = authRepository.deleteAccount()
        
        assertTrue(result.isSuccess)
        assertNull(authRepository.getCurrentUser())
        assertNull(secureStorage.getToken("auth_token").getOrNull())
    }
    
    @Test
    fun testAuthErrorTypes() {
        assertTrue(AuthError.InvalidCredentials is AuthError)
        assertTrue(AuthError.UserNotFound is AuthError)
        assertTrue(AuthError.EmailAlreadyInUse is AuthError)
        assertTrue(AuthError.WeakPassword is AuthError)
        assertTrue(AuthError.InvalidEmail is AuthError)
        assertTrue(AuthError.InvalidInvitationToken is AuthError)
        assertTrue(AuthError.InvitationExpired is AuthError)
        assertTrue(AuthError.NetworkError is AuthError)
        
        val unknownError = AuthError.UnknownError("Test error")
        assertEquals("Test error", unknownError.message)
    }
}