package com.carecomms.data.models

import kotlinx.serialization.Serializable

@Serializable
data class InvitationData(
    val carerId: String,
    val carerName: String,
    val expirationTime: Long,
    val token: String,
    val isUsed: Boolean = false
)

@Serializable
data class CarerInfo(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val location: String
)