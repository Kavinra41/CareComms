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
    val careeIds: List<String>
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
    val dateOfBirth: String,
    val address: String
)