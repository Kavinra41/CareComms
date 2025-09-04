package com.carecomms.domain.repository

import com.carecomms.data.models.InvitationData

interface InvitationRepository {
    suspend fun generateInvitationLink(carerId: String): Result<String>
    suspend fun validateInvitation(token: String): Result<InvitationData>
    suspend fun acceptInvitation(token: String, careeId: String): Result<Unit>
    suspend fun getInvitationByToken(token: String): Result<InvitationData>
    suspend fun markInvitationAsUsed(token: String): Result<Unit>
    suspend fun getActiveInvitations(carerId: String): Result<List<InvitationData>>
    suspend fun revokeInvitation(token: String): Result<Unit>
}