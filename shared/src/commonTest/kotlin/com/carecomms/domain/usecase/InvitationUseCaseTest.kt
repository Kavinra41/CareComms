package com.carecomms.domain.usecase

import com.carecomms.data.models.InvitationData
import com.carecomms.data.utils.DeepLinkHandler
import com.carecomms.domain.repository.InvitationRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InvitationUseCaseTest {
    
    private val mockInvitationRepository = object : InvitationRepository {
        private val invitations = mutableMapOf<String, InvitationData>()
        
        override suspend fun generateInvitationLink(carerId: String): Result<String> {
            return if (carerId == "valid-carer") {
                val token = "generated-token-123"
                invitations[token] = InvitationData(
                    carerId = carerId,
                    carerName = "Test Carer",
                    expirationTime = System.currentTimeMillis() + 86400000,
                    token = token,
                    isUsed = false
                )
                Result.success("carecomms://invite?token=$token")
            } else {
                Result.failure(Exception("Carer not found"))
            }
        }
        
        override suspend fun validateInvitation(token: String): Result<InvitationData> {
            val invitation = invitations[token]
            return if (invitation != null && !invitation.isUsed && invitation.expirationTime > System.currentTimeMillis()) {
                Result.success(invitation)
            } else {
                Result.failure(Exception("Invalid or expired invitation"))
            }
        }
        
        override suspend fun acceptInvitation(token: String, careeId: String): Result<Unit> {
            val invitation = invitations[token]
            return if (invitation != null && !invitation.isUsed) {
                invitations[token] = invitation.copy(isUsed = true)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Invalid invitation"))
            }
        }
        
        override suspend fun getInvitationByToken(token: String): Result<InvitationData> {
            return validateInvitation(token)
        }
        
        override suspend fun markInvitationAsUsed(token: String): Result<Unit> {
            val invitation = invitations[token]
            return if (invitation != null) {
                invitations[token] = invitation.copy(isUsed = true)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Invitation not found"))
            }
        }
        
        override suspend fun getActiveInvitations(carerId: String): Result<List<InvitationData>> {
            val activeInvitations = invitations.values.filter { 
                it.carerId == carerId && !it.isUsed && it.expirationTime > System.currentTimeMillis() 
            }
            return Result.success(activeInvitations)
        }
        
        override suspend fun revokeInvitation(token: String): Result<Unit> {
            return markInvitationAsUsed(token)
        }
    }
    
    private val mockDeepLinkHandler = DeepLinkHandler(mockInvitationRepository)
    private val invitationUseCase = InvitationUseCase(mockInvitationRepository, mockDeepLinkHandler)
    
    @Test
    fun `generateInvitationLink should return shareable URL for valid carer`() = runTest {
        val result = invitationUseCase.generateInvitationLink("valid-carer")
        
        assertTrue(result.isSuccess)
        val url = result.getOrNull()
        assertNotNull(url)
        assertTrue(url.startsWith("carecomms://invite?token="))
    }
    
    @Test
    fun `generateInvitationLink should fail for invalid carer`() = runTest {
        val result = invitationUseCase.generateInvitationLink("invalid-carer")
        
        assertTrue(result.isFailure)
        assertEquals("Carer not found", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `validateInvitationFromUrl should validate invitation from deep link URL`() = runTest {
        // First generate an invitation
        val generateResult = invitationUseCase.generateInvitationLink("valid-carer")
        assertTrue(generateResult.isSuccess)
        val url = generateResult.getOrThrow()
        
        // Then validate it
        val validateResult = invitationUseCase.validateInvitationFromUrl(url)
        assertTrue(validateResult.isSuccess)
        
        val invitationData = validateResult.getOrThrow()
        assertEquals("valid-carer", invitationData.carerId)
        assertEquals("Test Carer", invitationData.carerName)
        assertFalse(invitationData.isUsed)
    }
    
    @Test
    fun `validateInvitationFromUrl should fail for invalid URL`() = runTest {
        val result = invitationUseCase.validateInvitationFromUrl("https://invalid.com/invite?token=abc")
        
        assertTrue(result.isFailure)
        assertEquals("Invalid invitation URL format", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `validateInvitationFromToken should validate invitation directly`() = runTest {
        // First generate an invitation
        val generateResult = invitationUseCase.generateInvitationLink("valid-carer")
        assertTrue(generateResult.isSuccess)
        val url = generateResult.getOrThrow()
        val token = mockDeepLinkHandler.parseInvitationUrl(url)
        assertNotNull(token)
        
        // Then validate it by token
        val validateResult = invitationUseCase.validateInvitationFromToken(token)
        assertTrue(validateResult.isSuccess)
        
        val invitationData = validateResult.getOrThrow()
        assertEquals("valid-carer", invitationData.carerId)
        assertEquals("Test Carer", invitationData.carerName)
        assertFalse(invitationData.isUsed)
    }
    
    @Test
    fun `validateInvitationFromToken should fail for invalid token`() = runTest {
        val result = invitationUseCase.validateInvitationFromToken("invalid-token")
        
        assertTrue(result.isFailure)
        assertEquals("Invalid or expired invitation", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `acceptInvitation should successfully accept valid invitation`() = runTest {
        // First generate an invitation
        val generateResult = invitationUseCase.generateInvitationLink("valid-carer")
        assertTrue(generateResult.isSuccess)
        val url = generateResult.getOrThrow()
        val token = mockDeepLinkHandler.parseInvitationUrl(url)
        assertNotNull(token)
        
        // Accept the invitation
        val acceptResult = invitationUseCase.acceptInvitation(token, "caree-123")
        assertTrue(acceptResult.isSuccess)
        
        // Verify invitation is now used
        val validateResult = invitationUseCase.validateInvitationFromToken(token)
        assertTrue(validateResult.isFailure) // Should fail because it's used
    }
    
    @Test
    fun `acceptInvitation should fail for already used invitation`() = runTest {
        // First generate and accept an invitation
        val generateResult = invitationUseCase.generateInvitationLink("valid-carer")
        assertTrue(generateResult.isSuccess)
        val url = generateResult.getOrThrow()
        val token = mockDeepLinkHandler.parseInvitationUrl(url)
        assertNotNull(token)
        
        val firstAcceptResult = invitationUseCase.acceptInvitation(token, "caree-123")
        assertTrue(firstAcceptResult.isSuccess)
        
        // Try to accept again
        val secondAcceptResult = invitationUseCase.acceptInvitation(token, "caree-456")
        assertTrue(secondAcceptResult.isFailure)
        assertEquals("Invalid invitation", secondAcceptResult.exceptionOrNull()?.message)
    }
    
    @Test
    fun `acceptInvitation should fail for invalid token`() = runTest {
        val result = invitationUseCase.acceptInvitation("invalid-token", "caree-123")
        
        assertTrue(result.isFailure)
        assertEquals("Invalid or expired invitation", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `getActiveInvitations should return active invitations for carer`() = runTest {
        // Generate some invitations
        val generateResult1 = invitationUseCase.generateInvitationLink("valid-carer")
        val generateResult2 = invitationUseCase.generateInvitationLink("valid-carer")
        assertTrue(generateResult1.isSuccess)
        assertTrue(generateResult2.isSuccess)
        
        // Get active invitations
        val activeResult = invitationUseCase.getActiveInvitations("valid-carer")
        assertTrue(activeResult.isSuccess)
        
        val activeInvitations = activeResult.getOrThrow()
        assertEquals(2, activeInvitations.size)
        assertTrue(activeInvitations.all { it.carerId == "valid-carer" && !it.isUsed })
    }
    
    @Test
    fun `revokeInvitation should successfully revoke invitation`() = runTest {
        // First generate an invitation
        val generateResult = invitationUseCase.generateInvitationLink("valid-carer")
        assertTrue(generateResult.isSuccess)
        val url = generateResult.getOrThrow()
        val token = mockDeepLinkHandler.parseInvitationUrl(url)
        assertNotNull(token)
        
        // Revoke the invitation
        val revokeResult = invitationUseCase.revokeInvitation(token)
        assertTrue(revokeResult.isSuccess)
        
        // Verify invitation is no longer valid
        val validateResult = invitationUseCase.validateInvitationFromToken(token)
        assertTrue(validateResult.isFailure)
    }
    
    @Test
    fun `isValidInvitationUrl should return true for valid URL`() {
        val validUrl = "carecomms://invite?token=abc123"
        assertTrue(invitationUseCase.isValidInvitationUrl(validUrl))
    }
    
    @Test
    fun `isValidInvitationUrl should return false for invalid URL`() {
        val invalidUrl = "https://example.com/invite?token=abc123"
        assertFalse(invitationUseCase.isValidInvitationUrl(invalidUrl))
    }
    
    @Test
    fun `extractTokenFromUrl should extract token from valid URL`() {
        val url = "carecomms://invite?token=test-token-123"
        val token = invitationUseCase.extractTokenFromUrl(url)
        assertEquals("test-token-123", token)
    }
    
    @Test
    fun `extractTokenFromUrl should return null for invalid URL`() {
        val url = "https://example.com/invite?token=test-token-123"
        val token = invitationUseCase.extractTokenFromUrl(url)
        assertNull(token)
    }
}