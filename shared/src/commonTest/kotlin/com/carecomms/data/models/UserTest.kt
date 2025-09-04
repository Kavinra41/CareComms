package com.carecomms.data.models

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testCarerSerialization() {
        val carer = Carer(
            id = "carer123",
            email = "carer@example.com",
            createdAt = 1234567890L,
            documents = listOf("doc1.pdf", "doc2.pdf"),
            age = 35,
            phoneNumber = "+1234567890",
            location = "New York, NY",
            careeIds = listOf("caree1", "caree2")
        )

        val serialized = json.encodeToString(Carer.serializer(), carer)
        val deserialized = json.decodeFromString(Carer.serializer(), serialized)

        assertEquals(carer, deserialized)
    }

    @Test
    fun testCareeSerialization() {
        val personalDetails = PersonalDetails(
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = "1950-01-01",
            address = "123 Main St",
            emergencyContact = "+0987654321"
        )

        val caree = Caree(
            id = "caree123",
            email = "caree@example.com",
            createdAt = 1234567890L,
            healthInfo = "Diabetes, High Blood Pressure",
            personalDetails = personalDetails,
            carerId = "carer123"
        )

        val serialized = json.encodeToString(Caree.serializer(), caree)
        val deserialized = json.decodeFromString(Caree.serializer(), serialized)

        assertEquals(caree, deserialized)
    }

    @Test
    fun testCarerRegistrationDataValidation() {
        val registrationData = CarerRegistrationData(
            email = "test@example.com",
            password = "password123",
            documents = listOf("license.pdf"),
            age = 30,
            phoneNumber = "+1234567890",
            location = "Boston, MA"
        )

        assertNotNull(registrationData.email)
        assertNotNull(registrationData.password)
        assertTrue(registrationData.age > 0)
        assertTrue(registrationData.documents.isNotEmpty())
    }

    @Test
    fun testCareeRegistrationDataValidation() {
        val personalDetails = PersonalDetails(
            firstName = "Jane",
            lastName = "Smith",
            dateOfBirth = "1945-05-15"
        )

        val registrationData = CareeRegistrationData(
            email = "jane@example.com",
            password = "password456",
            healthInfo = "Arthritis",
            basicDetails = personalDetails
        )

        assertNotNull(registrationData.email)
        assertNotNull(registrationData.password)
        assertNotNull(registrationData.healthInfo)
        assertNotNull(registrationData.basicDetails.firstName)
        assertNotNull(registrationData.basicDetails.lastName)
    }

    @Test
    fun testPersonalDetailsOptionalFields() {
        val personalDetails = PersonalDetails(
            firstName = "Test",
            lastName = "User",
            dateOfBirth = "1960-01-01"
        )

        assertEquals(null, personalDetails.address)
        assertEquals(null, personalDetails.emergencyContact)
    }
}