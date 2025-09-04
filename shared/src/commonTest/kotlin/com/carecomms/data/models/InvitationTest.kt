package com.carecomms.data.models

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class InvitationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testInvitationDataSerialization() {
        val invitationData = InvitationData(
            carerId = "carer123",
            carerName = "Dr. Smith",
            expirationTime = 1234567890L,
            token = "abc123def456",
            isUsed = false
        )

        val serialized = json.encodeToString(InvitationData.serializer(), invitationData)
        val deserialized = json.decodeFromString(InvitationData.serializer(), serialized)

        assertEquals(invitationData, deserialized)
    }

    @Test
    fun testCarerInfoSerialization() {
        val carerInfo = CarerInfo(
            id = "carer123",
            name = "Dr. Jane Smith",
            phoneNumber = "+1234567890",
            location = "Boston, MA"
        )

        val serialized = json.encodeToString(CarerInfo.serializer(), carerInfo)
        val deserialized = json.decodeFromString(CarerInfo.serializer(), serialized)

        assertEquals(carerInfo, deserialized)
    }

    @Test
    fun testInvitationDataDefaultValues() {
        val invitationData = InvitationData(
            carerId = "carer123",
            carerName = "Dr. Smith",
            expirationTime = 1234567890L,
            token = "abc123def456"
        )

        assertFalse(invitationData.isUsed)
    }

    @Test
    fun testInvitationDataValidation() {
        val invitationData = InvitationData(
            carerId = "carer123",
            carerName = "Dr. Smith",
            expirationTime = System.currentTimeMillis() + 86400000L, // 24 hours from now
            token = "unique-token-123",
            isUsed = false
        )

        assertNotNull(invitationData.carerId)
        assertNotNull(invitationData.carerName)
        assertNotNull(invitationData.token)
        assertEquals(false, invitationData.isUsed)
    }

    @Test
    fun testCarerInfoValidation() {
        val carerInfo = CarerInfo(
            id = "carer456",
            name = "Nurse Johnson",
            phoneNumber = "+0987654321",
            location = "Seattle, WA"
        )

        assertNotNull(carerInfo.id)
        assertNotNull(carerInfo.name)
        assertNotNull(carerInfo.phoneNumber)
        assertNotNull(carerInfo.location)
    }
}