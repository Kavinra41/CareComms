package com.carecomms.data.utils

import com.carecomms.data.models.InvitationData
import com.carecomms.domain.repository.InvitationRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeepLinkHandlerTest {
    
    private val mockInvitationRepository = object : InvitationRepository {
        override suspend fun generateInvitationLink(carerId: String): Result<String> {
            return Result.success("carecomms://invite?token=test-token")
        }
        
        override suspend fun validateInvitation(token: String): Result<InvitationData> {
            return if (token == "valid-token") {
                Result.success(
                    InvitationData(
                        carerId = "carer-123",
                        carerName = "John Doe",
                        expirationTime = System.currentTimeMillis() + 86400000,
                        token = token,
                        isUsed = false
                    )
                )
            } else {
                Result.failure(Exception("Invalid token"))
            }
        }
        
        override suspend fun acceptInvitation(token: String, careeId: String): Result<Unit> {
            return Result.success(Unit)
        }
        
        override suspend fun getInvitationByToken(token: String): Result<InvitationData> {
            return validateInvitation(token)
        }
        
        override suspend fun markInvitationAsUsed(token: String): Result<Unit> {
            return Result.success(Unit)
        }
        
        override suspend fun getActiveInvitations(carerId: String): Result<List<InvitationData>> {
            return Result.success(emptyList())
        }
        
        override suspend fun revokeInvitation(token: String): Result<Unit> {
            return Result.success(Unit)
        }
    }
    
    private val deepLinkHandler = DeepLinkHandler(mockInvitationRepository)
    
    @Test
    fun `parseInvitationUrl should extract token from valid URL`() {
        val url = "carecomms://invite?token=abc123"
        val token = deepLinkHandler.parseInvitationUrl(url)
        assertEquals("abc123", token)
    }
    
    @Test
    fun `parseInvitationUrl should return null for invalid scheme`() {
        val url = "https://example.com/invite?token=abc123"
        val token = deepLinkHandler.parseInvitationUrl(url)
        assertNull(token)
    }
    
    @Test
    fun `parseInvitationUrl should return null for invalid host`() {
        val url = "carecomms://invalid?token=abc123"
        val token = deepLinkHandler.parseInvitationUrl(url)
        assertNull(token)
    }
    
    @Test
    fun `parseInvitationUrl should return null for missing token parameter`() {
        val url = "carecomms://invite?other=value"
        val token = deepLinkHandler.parseInvitationUrl(url)
        assertNull(token)
    }
    
    @Test
    fun `parseInvitationUrl should return null for URL without query parameters`() {
        val url = "carecomms://invite"
        val token = deepLinkHandler.parseInvitationUrl(url)
        assertNull(token)
    }
    
    @Test
    fun `parseInvitationUrl should handle multiple query parameters`() {
        val url = "carecomms://invite?other=value&token=abc123&another=param"
        val token = deepLinkHandler.parseInvitationUrl(url)
        assertEquals("abc123", token)
    }
    
    @Test
    fun `parseInvitationUrl should handle malformed URL gracefully`() {
        val url = "carecomms://invite?token="
        val token = deepLinkHandler.parseInvitationUrl(url)
        assertEquals("", token)
    }
    
    @Test
    fun `isValidInvitationUrl should return true for valid URL`() {
        val url = "carecomms://invite?token=abc123"
        assertTrue(deepLinkHandler.isValidInvitationUrl(url))
    }
    
    @Test
    fun `isValidInvitationUrl should return false for invalid URL`() {
        val url = "https://example.com/invite?token=abc123"
        assertFalse(deepLinkHandler.isValidInvitationUrl(url))
    }
    
    @Test
    fun `createInvitationUrl should generate correct URL format`() {
        val token = "test-token-123"
        val url = DeepLinkHandler.createInvitationUrl(token)
        assertEquals("carecomms://invite?token=test-token-123", url)
    }
    
    @Test
    fun `createShareableUrl should generate correct URL`() {
        val token = "test-token-123"
        val url = deepLinkHandler.createShareableUrl(token)
        assertEquals("carecomms://invite?token=test-token-123", url)
    }
    
    @Test
    fun `handleInvitationDeepLink should validate invitation for valid URL`() = runTest {
        val url = "carecomms://invite?token=valid-token"
        val result = deepLinkHandler.handleInvitationDeepLink(url)
        
        assertTrue(result.isSuccess)
        val invitationData = result.getOrNull()
        assertNotNull(invitationData)
        assertEquals("carer-123", invitationData.carerId)
        assertEquals("John Doe", invitationData.carerName)
        assertEquals("valid-token", invitationData.token)
        assertFalse(invitationData.isUsed)
    }
    
    @Test
    fun `handleInvitationDeepLink should fail for invalid URL format`() = runTest {
        val url = "https://example.com/invite?token=valid-token"
        val result = deepLinkHandler.handleInvitationDeepLink(url)
        
        assertTrue(result.isFailure)
        assertEquals("Invalid invitation URL format", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `handleInvitationDeepLink should fail for invalid token`() = runTest {
        val url = "carecomms://invite?token=invalid-token"
        val result = deepLinkHandler.handleInvitationDeepLink(url)
        
        assertTrue(result.isFailure)
        assertEquals("Invalid token", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `parseInvitationUrl should handle exception gracefully`() {
        // Test with a URL that might cause parsing issues
        val url = "carecomms://invite?token=abc%20123&malformed"
        val token = deepLinkHandler.parseInvitationUrl(url)
        // Should not crash and return a reasonable result
        assertNotNull(token) // The exact result may vary based on URL parsing behavior
    }
}