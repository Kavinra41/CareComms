package com.carecomms.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CarerInvitation(
    val carerId: String,
    val carerName: String,
    val carerEmail: String,
    val invitationCode: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class EmailInvitation(
    val recipientEmail: String,
    val carerName: String,
    val invitationCode: String,
    val deepLink: String
)

@Serializable
data class CarerCareeRelationship(
    val id: String,
    val carerId: String,
    val careeId: String,
    val carerName: String,
    val careeName: String,
    val invitationCode: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: RelationshipStatus = RelationshipStatus.ACTIVE
)

enum class RelationshipStatus {
    PENDING,
    ACTIVE,
    INACTIVE
}