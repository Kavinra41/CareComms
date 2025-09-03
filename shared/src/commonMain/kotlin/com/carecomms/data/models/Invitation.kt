package com.carecomms.data.models

import kotlinx.serialization.Serializable

@Serializable
data class InvitationData(
    val carerId: String,
    val carerName: String,
    val expirationTime: Long
)

@Serializable
data class CarerRegistrationData(
    val email: String,
    val password: String,
    val documents: List<String>,
    val age: Int,
    val phoneNumber: String,
    val location: String
)

@Serializable
data class CareeRegistrationData(
    val email: String,
    val password: String,
    val healthInfo: String,
    val basicDetails: PersonalDetails
)