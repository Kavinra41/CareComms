package com.carecomms.data.models

import kotlinx.serialization.Serializable

@Serializable
sealed class User {
    abstract val id: String
    abstract val email: String
    abstract val createdAt: Long
}

@Serializable
data class Carer(
    override val id: String,
    override val email: String,
    override val createdAt: Long,
    val documents: List<String>,
    val age: Int,
    val phoneNumber: String,
    val location: String,
    val careeIds: List<String> = emptyList()
) : User()

@Serializable
data class Caree(
    override val id: String,
    override val email: String,
    override val createdAt: Long,
    val healthInfo: String,
    val personalDetails: PersonalDetails,
    val carerId: String
) : User()

@Serializable
data class PersonalDetails(
    val firstName: String,
    val lastName: String,
    val age: Int,
    val dateOfBirth: String? = null,
    val address: String? = null,
    val emergencyContact: String? = null
)

@Serializable
data class CareeInfo(
    val id: String,
    val name: String,
    val age: Int,
    val healthConditions: List<String>
)

@Serializable
data class CarerInfo(
    val id: String,
    val name: String,
    val email: String,
    val location: String,
    val phoneNumber: String
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

