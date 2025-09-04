package com.carecomms.presentation.invitation

import com.carecomms.data.models.InvitationData

data class InvitationState(
    val isLoading: Boolean = false,
    val invitationData: InvitationData? = null,
    val generatedInvitationUrl: String? = null,
    val activeInvitations: List<InvitationData> = emptyList(),
    val error: String? = null,
    val isValidatingInvitation: Boolean = false,
    val isAcceptingInvitation: Boolean = false,
    val invitationAccepted: Boolean = false,
    val isGeneratingLink: Boolean = false
)

sealed class InvitationIntent {
    data class GenerateInvitationLink(val carerId: String) : InvitationIntent()
    data class ValidateInvitationFromUrl(val invitationUrl: String) : InvitationIntent()
    data class ValidateInvitationFromToken(val token: String) : InvitationIntent()
    data class AcceptInvitation(val token: String, val careeId: String) : InvitationIntent()
    data class LoadActiveInvitations(val carerId: String) : InvitationIntent()
    data class RevokeInvitation(val token: String) : InvitationIntent()
    object ClearError : InvitationIntent()
    object ClearGeneratedUrl : InvitationIntent()
    object ResetAcceptedState : InvitationIntent()
}

sealed class InvitationEffect {
    data class ShowError(val message: String) : InvitationEffect()
    data class NavigateToChat(val chatId: String) : InvitationEffect()
    data class ShareInvitationUrl(val url: String) : InvitationEffect()
    object InvitationAcceptedSuccessfully : InvitationEffect()
}