package com.carecomms.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SimpleUser(
    val uid: String,
    val email: String,
    val name: String,
    val phoneNumber: String,
    val city: String,
    val createdAt: Long = System.currentTimeMillis()
)