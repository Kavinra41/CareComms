package com.carecomms.data.repository

import com.carecomms.data.database.DatabaseManager
import com.carecomms.data.models.Carer
import com.carecomms.data.models.User
import com.carecomms.database.Invitation
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LocalInvitationRepositoryTest {
    
    private val mockDatabaseManager = object : DatabaseManager(null as any) {
        private val invitations = mutableMapOf<String, Invitation>()
        private val users = mutableMapOf<String, User>()
        
        init {
            // Add a test carer
            users["carer-123"] = Carer(
                id = "carer-123",
                email = "carer@test.com",
                createdAt = System.currentTimeMillis(),
                documents = listOf("doc1.pdf"),
                age = 30,
                phoneNumber = "+1234567890",
                location = "Test City",
                careeIds = emptyList()
            )
        }
        
        override suspend fun insertInvitation(token: String, carerId: String, expirationTime: Long, createdAt: Long) {
            invitations[token] = Invitation(
                token = token,
                carerId = carerId,
                expirationTime = expirationTime,
                isUsed = 0,
                createdAt = createdAt
            )
        }
        
        override suspend fun getValidInvitation(token: String, currentTime: Long): Invitation? {
            val invitation = invitations[token]
            return if (invitation != null && invitation.isUsed == 0L && invitation.expirationTime > currentTime) {
                invitation
            } else null
        }
        
        override suspend fun getInvitationByToken(token: String): Invitation? {
            return invitations[token]
        }
        
        override suspend fun markInvitationAsUsed(token: String) {
            val invitation = invitations[token]
            if (invitation != null) {
                invitations[token] = invitation.copy(isUsed = 1)
            }
        }
        
        override suspend fun deleteExpiredInvitations(currentTime: Long) {
            val expiredTokens = invitations.filter { it.value.expirationTime < currentTime }.keys
            expiredTokens.forEach { invitations.remove(it) }
        }
        
        override suspend fun getInvitationsByCarerId(carerId: String): List<Invitation> {
            return invitations.values.filter { it.carerId == carerId }
        }
    }
    
    private val mockUserRepository = object : LocalUserRepository(mockDatabaseManager, Json.Default) {
        override suspend fun getUserById(id: String): User? {
            return when (id) {
                "carer-123" -> Carer(
                    id = "carer-123",
                    email = "carer@test.com",
                    createdAt = System.currentTimeMillis(),
                    documents = listOf("doc1.pdf"),
                    age = 30,
                    phoneNumber = "+1234567890",
                    location = "Test City",
                    careeIds = emptyList()
                )
                "caree-456" -> com.carecomms.data.models.Caree(
                    id = "caree-456",
                    email = "caree@test.com",
                    createdAt = System.currentTimeMillis(),
                    healthInfo = "Good health",
                    personalDetails = com.carecomms.data.models.PersonalDetails(
                        firstName = "Jane",
                        lastName = "Doe",
                        dateOfBirth = "1990-01-01",
                        address = "123 Test St"
                    ),
                    carerId = "carer-123"
                )
                else -> null
            }
        }
    }
    
    private val repository = LocalInvitationRepository(mockDatabaseManager, mockUserRepository)
    
    @Test
    fun `generateInvitationLink should create valid invitation for existing carer`() = runTest {
        val result = repository.generateInvitationLink("carer-123")
        
        assertTrue(result.isSuccess)
        val link = result.getOrThrow()
        assertTrue(link.startsWith("carecomms://invite?token="))
        
        // Extract token and verify it was stored
        val token = link.substringAfter("token=")
        val storedInvitation = mockDatabaseManager.getInvitationByToken(token)
        assertNotNull(storedInvitation)
        assertEquals("carer-123", storedInvitation.carerId)
        assertEquals(0L, storedInvitation.isUsed)
    }
    
    @Test
    fun `generateInvitationLink should fail for non-existent carer`() = runTest {
        val result = repository.generateInvitationLink("non-existent-carer")
        
        assertTrue(result.isFailure)
        assertEquals("Carer not found", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `validateInvitation should return invitation data for valid token`() = runTest {
        // First generate an invitation
        val generateResult = repository.generateInvitationLink("carer-123")
        assertTrue(generateResult.isSuccess)
        val link = generateResult.getOrThrow()
        val token = link.substringAfter("token=")
        
        // Then validate it
        val validateResult = repository.validateInvitation(token)
        assertTrue(validateResult.isSuccess)
        
        val invitationData = validateResult.getOrThrow()
        assertEquals("carer-123", invitationData.carerId)
        assertEquals("+1234567890", invitationData.carerName) // Using phone as name
        assertEquals(token, invitationData.token)
        assertFalse(invitationData.isUsed)
    }
    
    @Test
    fun `validateInvitation should fail for invalid token`() = runTest {
        val result = repository.validateInvitation("invalid-token")
        
        assertTrue(result.isFailure)
        assertEquals("Invalid or expired invitation", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `validateInvitation should fail for expired invitation`() = runTest {
        // Create an expired invitation manually
        val token = "expired-token"
        val pastTime = System.currentTimeMillis() - 86400000 // 1 day ago
        mockDatabaseManager.insertInvitation(token, "carer-123", pastTime, pastTime - 86400000)
        
        val result = repository.validateInvitation(token)
        
        assertTrue(result.isFailure)
        assertEquals("Invalid or expired invitation", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `acceptInvitation should successfully accept valid invitation`() = runTest {
        // First generate an invitation
        val generateResult = repository.generateInvitationLink("carer-123")
        assertTrue(generateResult.isSuccess)
        val link = generateResult.getOrThrow()
        val token = link.substringAfter("token=")
        
        // Accept the invitation
        val acceptResult = repository.acceptInvitation(token, "caree-456")
        assertTrue(acceptResult.isSuccess)
        
        // Verify invitation is marked as used
        val invitation = mockDatabaseManager.getInvitationByToken(token)
        assertNotNull(invitation)
        assertEquals(1L, invitation.isUsed)
    }
    
    @Test
    fun `acceptInvitation should fail for invalid token`() = runTest {
        val result = repository.acceptInvitation("invalid-token", "caree-456")
        
        assertTrue(result.isFailure)
        assertEquals("Invalid or expired invitation", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `acceptInvitation should fail for non-existent caree`() = runTest {
        // First generate an invitation
        val generateResult = repository.generateInvitationLink("carer-123")
        assertTrue(generateResult.isSuccess)
        val link = generateResult.getOrThrow()
        val token = link.substringAfter("token=")
        
        // Try to accept with non-existent caree
        val acceptResult = repository.acceptInvitation(token, "non-existent-caree")
        assertTrue(acceptResult.isFailure)
        assertEquals("Caree not found", acceptResult.exceptionOrNull()?.message)
    }
    
    @Test
    fun `getInvitationByToken should return invitation data`() = runTest {
        // First generate an invitation
        val generateResult = repository.generateInvitationLink("carer-123")
        assertTrue(generateResult.isSuccess)
        val link = generateResult.getOrThrow()
        val token = link.substringAfter("token=")
        
        // Get invitation by token
        val result = repository.getInvitationByToken(token)
        assertTrue(result.isSuccess)
        
        val invitationData = result.getOrThrow()
        assertEquals("carer-123", invitationData.carerId)
        assertEquals(token, invitationData.token)
        assertFalse(invitationData.isUsed)
    }
    
    @Test
    fun `markInvitationAsUsed should mark invitation as used`() = runTest {
        // First generate an invitation
        val generateResult = repository.generateInvitationLink("carer-123")
        assertTrue(generateResult.isSuccess)
        val link = generateResult.getOrThrow()
        val token = link.substringAfter("token=")
        
        // Mark as used
        val markResult = repository.markInvitationAsUsed(token)
        assertTrue(markResult.isSuccess)
        
        // Verify it's marked as used
        val invitation = mockDatabaseManager.getInvitationByToken(token)
        assertNotNull(invitation)
        assertEquals(1L, invitation.isUsed)
    }
    
    @Test
    fun `getActiveInvitations should return only active invitations`() = runTest {
        // Generate multiple invitations
        val generateResult1 = repository.generateInvitationLink("carer-123")
        val generateResult2 = repository.generateInvitationLink("carer-123")
        assertTrue(generateResult1.isSuccess)
        assertTrue(generateResult2.isSuccess)
        
        val link1 = generateResult1.getOrThrow()
        val token1 = link1.substringAfter("token=")
        
        // Mark one as used
        repository.markInvitationAsUsed(token1)
        
        // Get active invitations
        val activeResult = repository.getActiveInvitations("carer-123")
        assertTrue(activeResult.isSuccess)
        
        val activeInvitations = activeResult.getOrThrow()
        assertEquals(1, activeInvitations.size) // Only one should be active
        assertFalse(activeInvitations[0].isUsed)
    }
    
    @Test
    fun `revokeInvitation should mark invitation as used`() = runTest {
        // First generate an invitation
        val generateResult = repository.generateInvitationLink("carer-123")
        assertTrue(generateResult.isSuccess)
        val link = generateResult.getOrThrow()
        val token = link.substringAfter("token=")
        
        // Revoke the invitation
        val revokeResult = repository.revokeInvitation(token)
        assertTrue(revokeResult.isSuccess)
        
        // Verify it's marked as used (revoked)
        val invitation = mockDatabaseManager.getInvitationByToken(token)
        assertNotNull(invitation)
        assertEquals(1L, invitation.isUsed)
    }
}