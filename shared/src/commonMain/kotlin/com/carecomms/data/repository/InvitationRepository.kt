package com.carecomms.data.repository

import com.carecomms.data.models.CarerInvitation
import com.carecomms.data.models.EmailInvitation
import com.carecomms.data.models.CarerCareeRelationship

interface InvitationRepository {
    suspend fun generateCarerCode(carerId: String): Result<String>
    suspend fun getCarerByCode(invitationCode: String): Result<CarerInvitation?>
    suspend fun sendEmailInvitation(emailInvitation: EmailInvitation): Result<Unit>
    suspend fun createCarerCareeRelationship(carerId: String, careeId: String, invitationCode: String): Result<Unit>
    suspend fun getCarerRelationships(carerId: String): Result<List<CarerCareeRelationship>>
    suspend fun getCareeRelationships(careeId: String): Result<List<CarerCareeRelationship>>
    suspend fun validateInvitationCode(code: String): Result<CarerInvitation?>
}