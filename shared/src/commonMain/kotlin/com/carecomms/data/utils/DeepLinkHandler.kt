package com.carecomms.data.utils

import com.carecomms.data.models.InvitationData
import com.carecomms.domain.repository.InvitationRepository

class DeepLinkHandler(
    private val invitationRepository: InvitationRepository
) {
    
    companion object {
        const val SCHEME = "carecomms"
        const val HOST_INVITE = "invite"
        const val PARAM_TOKEN = "token"
        
        fun createInvitationUrl(token: String): String {
            return "$SCHEME://$HOST_INVITE?$PARAM_TOKEN=$token"
        }
    }
    
    /**
     * Parses a deep link URL and extracts the invitation token
     * @param url The deep link URL to parse
     * @return The invitation token if valid, null otherwise
     */
    fun parseInvitationUrl(url: String): String? {
        return try {
            if (!url.startsWith("$SCHEME://$HOST_INVITE")) {
                return null
            }
            
            val queryStart = url.indexOf('?')
            if (queryStart == -1) return null
            
            val queryString = url.substring(queryStart + 1)
            val params = queryString.split('&')
            
            for (param in params) {
                val keyValue = param.split('=')
                if (keyValue.size == 2 && keyValue[0] == PARAM_TOKEN) {
                    return keyValue[1]
                }
            }
            
            null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Validates if a URL is a valid invitation deep link
     * @param url The URL to validate
     * @return true if it's a valid invitation URL format
     */
    fun isValidInvitationUrl(url: String): Boolean {
        return parseInvitationUrl(url) != null
    }
    
    /**
     * Handles an invitation deep link by validating the token
     * @param url The deep link URL
     * @return Result containing InvitationData if valid, error otherwise
     */
    suspend fun handleInvitationDeepLink(url: String): Result<InvitationData> {
        val token = parseInvitationUrl(url)
            ?: return Result.failure(Exception("Invalid invitation URL format"))
        
        return invitationRepository.validateInvitation(token)
    }
    
    /**
     * Creates a shareable invitation URL from a token
     * @param token The invitation token
     * @return The complete invitation URL
     */
    fun createShareableUrl(token: String): String {
        return createInvitationUrl(token)
    }
}