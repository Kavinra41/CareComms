package com.carecomms.data.models

import kotlinx.serialization.Serializable

@Serializable
data class InvitationData(
    val carerId: String,
    val carerName: String,
    val carerEmail: String,
    val expirationTime: Long,
    val token: String,
    val isUsed: Boolean = false
)