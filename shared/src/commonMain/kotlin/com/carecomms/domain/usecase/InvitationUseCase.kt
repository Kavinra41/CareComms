package com.carecomms.domain.usecase

import com.carecomms.data.models.InvitationData
import com.carecomms.data.utils.DeepLinkHandler
import com.carecomms.domain.repository.InvitationRepository

class InvitationUseCase(
    private val invitationRepository: InvitationRepository,
    private val deepLinkHandler: DeepLinkHandler
) {
    
    /**
     * Generates an invitation link for a carer
     * @param carerId The ID of the carer creating the invitation
     * @return Result containing the shareable invitation URL
     */
    suspend fun generateInvitationLink(carerId: String): Result<String> {
        return try {
            val result = invitationRepository.generateInvitationLink(carerId)
            result.map { url ->
                // Extract token from the generated URL and create a shareable format
                val token = deepLinkHandler.parseInvitationUrl(url)
                    ?: throw Exception("Failed to parse generated invitation URL")
                deepLinkHandler.createShareableUrl(token)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validates an invitation using a deep link URL
     * @param invitationUrl The invitation deep link URL
     * @return Result containing InvitationData if valid
     */
    suspend fun validateInvitationFromUrl(invitationUrl: String): Result<InvitationData> {
        return deepLinkHandler.handleInvitationDeepLink(invitationUrl)
    }
    
    /**
     * Validates an invitation using a token directly
     * @param token The invitation token
     * @return Result containing InvitationData if valid
     */
    suspend fun validateInvitationFromToken(token: String): Result<InvitationData> {
        return invitationRepository.validateInvitation(token)
    }
    
    /**
     * Accepts an invitation and creates the carer-caree relationship
     * @param token The invitation token
     * @param careeId The ID of the caree accepting the invitation
     * @return Result indicating success or failure
     */
    suspend fun acceptInvitation(token: String, careeId: String): Result<Unit> {
        return try {
            // First validate the invitation
            val validationResult = invitationRepository.validateInvitation(token)
            if (validationResult.isFailure) {
                return Result.failure(validationResult.exceptionOrNull() ?: Exception("Invalid invitation"))
            }
            
            val invitationData = validationResult.getOrThrow()
            if (invitationData.isUsed) {
                return Result.failure(Exception("Invitation has already been used"))
            }
            
            // Accept the invitation
            invitationRepository.acceptInvitation(token, careeId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Gets all active invitations for a carer
     * @param carerId The ID of the carer
     * @return Result containing list of active invitations
     */
    suspend fun getActiveInvitations(carerId: String): Result<List<InvitationData>> {
        return invitationRepository.getActiveInvitations(carerId)
    }
    
    /**
     * Revokes an invitation
     * @param token The invitation token to revoke
     * @return Result indicating success or failure
     */
    suspend fun revokeInvitation(token: String): Result<Unit> {
        return invitationRepository.revokeInvitation(token)
    }
    
    /**
     * Checks if a URL is a valid invitation deep link
     * @param url The URL to check
     * @return true if it's a valid invitation URL
     */
    fun isValidInvitationUrl(url: String): Boolean {
        return deepLinkHandler.isValidInvitationUrl(url)
    }
    
    /**
     * Extracts invitation token from a deep link URL
     * @param url The deep link URL
     * @return The invitation token if valid, null otherwise
     */
    fun extractTokenFromUrl(url: String): String? {
        return deepLinkHandler.parseInvitationUrl(url)
    }
}