package com.carecomms.security

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.*

class SessionManagerTest {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var mockSecureStorage: MockSecureStorage
    private lateinit var mockEncryptionManager: MockEncryptionManager
    
    @BeforeTest
    fun setup() {
        mockSecureStorage = MockSecureStorage()
        mockEncryptionManager = MockEncryptionManager()
        sessionManager = SessionManager(mockSecureStorage, mockEncryptionManager)
    }
    
    @Test
    fun `createSession should store session data`() = runTest {
        val userId = "test_user_id"
        val authToken = "test_auth_token"
        val refreshToken = "test_refresh_token"
        
        val result = sessionManager.createSession(userId, authToken, refreshToken)
        assertTrue(result.isSuccess)
        
        // Verify data is stored
        assertTrue(mockSecureStorage.exists(SecureStorageKeys.USER_ID))
        assertTrue(mockSecureStorage.exists(SecureStorageKeys.AUTH_TOKEN))
        assertTrue(mockSecureStorage.exists(SecureStorageKeys.REFRESH_TOKEN))
        assertTrue(mockSecureStorage.exists(SecureStorageKeys.SESSION_EXPIRY))
        
        // Verify stored values
        assertEquals(userId, mockSecureStorage.retrieve(SecureStorageKeys.USER_ID).getOrThrow())
        assertEquals(authToken, mockSecureStorage.retrieve(SecureStorageKeys.AUTH_TOKEN).getOrThrow())
        assertEquals(refreshToken, mockSecureStorage.retrieve(SecureStorageKeys.REFRESH_TOKEN).getOrThrow())
    }
    
    @Test
    fun `validateSession should return Valid for active session`() = runTest {
        val userId = "test_user_id"
        val authToken = "test_auth_token"
        val refreshToken = "test_refresh_token"
        
        sessionManager.createSession(userId, authToken, refreshToken)
        
        val validation = sessionManager.validateSession()
        assertTrue(validation.isSuccess)
        assertEquals(SessionValidation.Valid, validation.getOrThrow())
    }
    
    @Test
    fun `validateSession should return Invalid for no session`() = runTest {
        val validation = sessionManager.validateSession()
        assertTrue(validation.isSuccess)
        assertEquals(SessionValidation.Invalid, validation.getOrThrow())
    }
    
    @Test
    fun `refreshSession should update tokens and expiry`() = runTest {
        val userId = "test_user_id"
        val authToken = "test_auth_token"
        val refreshToken = "test_refresh_token"
        
        sessionManager.createSession(userId, authToken, refreshToken)
        
        val newAuthToken = "new_auth_token"
        val newRefreshToken = "new_refresh_token"
        
        val result = sessionManager.refreshSession(newAuthToken, newRefreshToken)
        assertTrue(result.isSuccess)
        
        // Verify tokens are updated
        assertEquals(newAuthToken, mockSecureStorage.retrieve(SecureStorageKeys.AUTH_TOKEN).getOrThrow())
        assertEquals(newRefreshToken, mockSecureStorage.retrieve(SecureStorageKeys.REFRESH_TOKEN).getOrThrow())
    }
    
    @Test
    fun `clearSession should remove all session data`() = runTest {
        val userId = "test_user_id"
        val authToken = "test_auth_token"
        val refreshToken = "test_refresh_token"
        
        sessionManager.createSession(userId, authToken, refreshToken)
        
        val result = sessionManager.clearSession()
        assertTrue(result.isSuccess)
        
        // Verify all data is cleared
        assertFalse(mockSecureStorage.exists(SecureStorageKeys.USER_ID))
        assertFalse(mockSecureStorage.exists(SecureStorageKeys.AUTH_TOKEN))
        assertFalse(mockSecureStorage.exists(SecureStorageKeys.REFRESH_TOKEN))
        assertFalse(mockSecureStorage.exists(SecureStorageKeys.SESSION_EXPIRY))
    }
    
    @Test
    fun `getCurrentUserId should return stored user ID`() = runTest {
        val userId = "test_user_id"
        val authToken = "test_auth_token"
        val refreshToken = "test_refresh_token"
        
        sessionManager.createSession(userId, authToken, refreshToken)
        
        val retrievedUserId = sessionManager.getCurrentUserId()
        assertEquals(userId, retrievedUserId)
    }
    
    @Test
    fun `getCurrentUserId should return null when no session`() = runTest {
        val retrievedUserId = sessionManager.getCurrentUserId()
        assertNull(retrievedUserId)
    }
    
    @Test
    fun `getCurrentAuthToken should return stored auth token`() = runTest {
        val userId = "test_user_id"
        val authToken = "test_auth_token"
        val refreshToken = "test_refresh_token"
        
        sessionManager.createSession(userId, authToken, refreshToken)
        
        val retrievedToken = sessionManager.getCurrentAuthToken()
        assertEquals(authToken, retrievedToken)
    }
    
    @Test
    fun `getCurrentRefreshToken should return stored refresh token`() = runTest {
        val userId = "test_user_id"
        val authToken = "test_auth_token"
        val refreshToken = "test_refresh_token"
        
        sessionManager.createSession(userId, authToken, refreshToken)
        
        val retrievedToken = sessionManager.getCurrentRefreshToken()
        assertEquals(refreshToken, retrievedToken)
    }
}