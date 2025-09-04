package com.carecomms.presentation.invitation

import com.carecomms.data.models.InvitationData
import com.carecomms.data.utils.DeepLinkHandler
import com.carecomms.domain.repository.InvitationRepository
import com.carecomms.domain.usecase.InvitationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InvitationViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    
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
            return if (invitation != null && !invitation.isUsed) {
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
                it.carerId == carerId && !it.isUsed 
            }
            return Result.success(activeInvitations)
        }
        
        override suspend fun revokeInvitation(token: String): Result<Unit> {
            return markInvitationAsUsed(token)
        }
    }
    
    private lateinit var deepLinkHandler: DeepLinkHandler
    private lateinit var invitationUseCase: InvitationUseCase
    private lateinit var viewModel: InvitationViewModel
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        deepLinkHandler = DeepLinkHandler(mockInvitationRepository)
        invitationUseCase = InvitationUseCase(mockInvitationRepository, deepLinkHandler)
        viewModel = InvitationViewModel(
            invitationUseCase = invitationUseCase,
            deepLinkHandler = deepLinkHandler,
            coroutineScope = CoroutineScope(testDispatcher)
        )
    }
    
    @Test
    fun `initial state should be correct`() = runTest {
        val state = viewModel.state.first()
        
        assertFalse(state.isLoading)
        assertNull(state.invitationData)
        assertNull(state.generatedInvitationUrl)
        assertTrue(state.activeInvitations.isEmpty())
        assertNull(state.error)
        assertFalse(state.isValidatingInvitation)
        assertFalse(state.isAcceptingInvitation)
        assertFalse(state.invitationAccepted)
        assertFalse(state.isGeneratingLink)
    }
    
    @Test
    fun `generateInvitationLink should update state correctly on success`() = runTest {
        viewModel.handleIntent(InvitationIntent.GenerateInvitationLink("valid-carer"))
        advanceUntilIdle()
        
        val state = viewModel.state.first()
        assertFalse(state.isGeneratingLink)
        assertNotNull(state.generatedInvitationUrl)
        assertTrue(state.generatedInvitationUrl!!.startsWith("carecomms://invite?token="))
        assertNull(state.error)
    }
    
    @Test
    fun `generateInvitationLink should update state correctly on failure`() = runTest {
        viewModel.handleIntent(InvitationIntent.GenerateInvitationLink("invalid-carer"))
        advanceUntilIdle()
        
        val state = viewModel.state.first()
        assertFalse(state.isGeneratingLink)
        assertNull(state.generatedInvitationUrl)
        assertEquals("Carer not found", state.error)
    }
    
    @Test
    fun `validateInvitationFromToken should update state correctly on success`() = runTest {
        // First generate an invitation to get a valid token
        viewModel.handleIntent(InvitationIntent.GenerateInvitationLink("valid-carer"))
        advanceUntilIdle()
        
        val generatedUrl = viewModel.state.first().generatedInvitationUrl!!
        val token = deepLinkHandler.parseInvitationUrl(generatedUrl)!!
        
        // Now validate it
        viewModel.handleIntent(InvitationIntent.ValidateInvitationFromToken(token))
        advanceUntilIdle()
        
        val state = viewModel.state.first()
        assertFalse(state.isValidatingInvitation)
        assertNotNull(state.invitationData)
        assertEquals("valid-carer", state.invitationData!!.carerId)
        assertEquals("Test Carer", state.invitationData!!.carerName)
        assertNull(state.error)
    }
    
    @Test
    fun `validateInvitationFromToken should update state correctly on failure`() = runTest {
        viewModel.handleIntent(InvitationIntent.ValidateInvitationFromToken("invalid-token"))
        advanceUntilIdle()
        
        val state = viewModel.state.first()
        assertFalse(state.isValidatingInvitation)
        assertNull(state.invitationData)
        assertEquals("Invalid or expired invitation", state.error)
    }
    
    @Test
    fun `validateInvitationFromUrl should update state correctly on success`() = runTest {
        // First generate an invitation
        viewModel.handleIntent(InvitationIntent.GenerateInvitationLink("valid-carer"))
        advanceUntilIdle()
        
        val generatedUrl = viewModel.state.first().generatedInvitationUrl!!
        
        // Now validate it from URL
        viewModel.handleIntent(InvitationIntent.ValidateInvitationFromUrl(generatedUrl))
        advanceUntilIdle()
        
        val state = viewModel.state.first()
        assertFalse(state.isValidatingInvitation)
        assertNotNull(state.invitationData)
        assertEquals("valid-carer", state.invitationData!!.carerId)
        assertNull(state.error)
    }
    
    @Test
    fun `validateInvitationFromUrl should update state correctly on failure`() = runTest {
        viewModel.handleIntent(InvitationIntent.ValidateInvitationFromUrl("https://invalid.com/invite?token=abc"))
        advanceUntilIdle()
        
        val state = viewModel.state.first()
        assertFalse(state.isValidatingInvitation)
        assertNull(state.invitationData)
        assertEquals("Invalid invitation URL format", state.error)
    }
    
    @Test
    fun `acceptInvitation should update state correctly on success`() = runTest {
        // First generate an invitation
        viewModel.handleIntent(InvitationIntent.GenerateInvitationLink("valid-carer"))
        advanceUntilIdle()
        
        val generatedUrl = viewModel.state.first().generatedInvitationUrl!!
        val token = deepLinkHandler.parseInvitationUrl(generatedUrl)!!
        
        // Accept the invitation
        viewModel.handleIntent(InvitationIntent.AcceptInvitation(token, "caree-123"))
        advanceUntilIdle()
        
        val state = viewModel.state.first()
        assertFalse(state.isAcceptingInvitation)
        assertTrue(state.invitationAccepted)
        assertNull(state.error)
    }
    
    @Test
    fun `acceptInvitation should update state correctly on failure`() = runTest {
        viewModel.handleIntent(InvitationIntent.AcceptInvitation("invalid-token", "caree-123"))
        advanceUntilIdle()
        
        val state = viewModel.state.first()
        assertFalse(state.isAcceptingInvitation)
        assertFalse(state.invitationAccepted)
        assertEquals("Invalid or expired invitation", state.error)
    }
    
    @Test
    fun `loadActiveInvitations should update state correctly on success`() = runTest {
        // First generate some invitations
        viewModel.handleIntent(InvitationIntent.GenerateInvitationLink("valid-carer"))
        advanceUntilIdle()
        
        // Load active invitations
        viewModel.handleIntent(InvitationIntent.LoadActiveInvitations("valid-carer"))
        advanceUntilIdle()
        
        val state = viewModel.state.first()
        assertFalse(state.isLoading)
        assertEquals(1, state.activeInvitations.size)
        assertEquals("valid-carer", state.activeInvitations[0].carerId)
        assertNull(state.error)
    }
    
    @Test
    fun `revokeInvitation should update state correctly`() = runTest {
        // First generate an invitation
        viewModel.handleIntent(InvitationIntent.GenerateInvitationLink("valid-carer"))
        advanceUntilIdle()
        
        val generatedUrl = viewModel.state.first().generatedInvitationUrl!!
        val token = deepLinkHandler.parseInvitationUrl(generatedUrl)!!
        
        // Load active invitations first
        viewModel.handleIntent(InvitationIntent.LoadActiveInvitations("valid-carer"))
        advanceUntilIdle()
        
        assertEquals(1, viewModel.state.first().activeInvitations.size)
        
        // Revoke the invitation
        viewModel.handleIntent(InvitationIntent.RevokeInvitation(token))
        advanceUntilIdle()
        
        val state = viewModel.state.first()
        assertFalse(state.isLoading)
        assertEquals(0, state.activeInvitations.size) // Should be removed from list
        assertNull(state.error)
    }
    
    @Test
    fun `clearError should clear error state`() = runTest {
        // First cause an error
        viewModel.handleIntent(InvitationIntent.GenerateInvitationLink("invalid-carer"))
        advanceUntilIdle()
        
        assertNotNull(viewModel.state.first().error)
        
        // Clear the error
        viewModel.handleIntent(InvitationIntent.ClearError)
        advanceUntilIdle()
        
        assertNull(viewModel.state.first().error)
    }
    
    @Test
    fun `clearGeneratedUrl should clear generated URL`() = runTest {
        // First generate a URL
        viewModel.handleIntent(InvitationIntent.GenerateInvitationLink("valid-carer"))
        advanceUntilIdle()
        
        assertNotNull(viewModel.state.first().generatedInvitationUrl)
        
        // Clear the URL
        viewModel.handleIntent(InvitationIntent.ClearGeneratedUrl)
        advanceUntilIdle()
        
        assertNull(viewModel.state.first().generatedInvitationUrl)
    }
    
    @Test
    fun `resetAcceptedState should reset accepted state`() = runTest {
        // First accept an invitation
        viewModel.handleIntent(InvitationIntent.GenerateInvitationLink("valid-carer"))
        advanceUntilIdle()
        
        val generatedUrl = viewModel.state.first().generatedInvitationUrl!!
        val token = deepLinkHandler.parseInvitationUrl(generatedUrl)!!
        
        viewModel.handleIntent(InvitationIntent.AcceptInvitation(token, "caree-123"))
        advanceUntilIdle()
        
        assertTrue(viewModel.state.first().invitationAccepted)
        
        // Reset the accepted state
        viewModel.handleIntent(InvitationIntent.ResetAcceptedState)
        advanceUntilIdle()
        
        assertFalse(viewModel.state.first().invitationAccepted)
    }
    
    @Test
    fun `handleDeepLink should validate invitation for valid URL`() = runTest {
        // First generate an invitation
        viewModel.handleIntent(InvitationIntent.GenerateInvitationLink("valid-carer"))
        advanceUntilIdle()
        
        val generatedUrl = viewModel.state.first().generatedInvitationUrl!!
        
        // Handle the deep link
        viewModel.handleDeepLink(generatedUrl)
        advanceUntilIdle()
        
        val state = viewModel.state.first()
        assertNotNull(state.invitationData)
        assertEquals("valid-carer", state.invitationData!!.carerId)
    }
    
    @Test
    fun `isValidInvitationUrl should return correct result`() {
        assertTrue(viewModel.isValidInvitationUrl("carecomms://invite?token=abc123"))
        assertFalse(viewModel.isValidInvitationUrl("https://example.com/invite?token=abc123"))
    }
    
    @Test
    fun `extractTokenFromUrl should return correct token`() {
        val token = viewModel.extractTokenFromUrl("carecomms://invite?token=test-token-123")
        assertEquals("test-token-123", token)
        
        val nullToken = viewModel.extractTokenFromUrl("https://example.com/invite?token=test-token-123")
        assertNull(nullToken)
    }
    
    @Test
    fun `generateShareText should return formatted text`() {
        val shareText = viewModel.generateShareText("John Doe", "carecomms://invite?token=abc123")
        assertTrue(shareText.contains("John Doe"))
        assertTrue(shareText.contains("carecomms://invite?token=abc123"))
        assertTrue(shareText.contains("CareComms"))
    }
    
    @Test
    fun `generateShareSubject should return correct subject`() {
        val subject = viewModel.generateShareSubject()
        assertEquals("CareComms Invitation", subject)
    }
}