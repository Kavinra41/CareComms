package com.carecomms.data.models

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppErrorTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testNetworkErrorSerialization() {
        val error = AppError.NetworkError
        val serialized = json.encodeToString(AppError.serializer(), error)
        val deserialized = json.decodeFromString(AppError.serializer(), serialized)

        assertEquals(error, deserialized)
        assertTrue(deserialized is AppError.NetworkError)
    }

    @Test
    fun testAuthenticationErrorSerialization() {
        val error = AppError.AuthenticationError
        val serialized = json.encodeToString(AppError.serializer(), error)
        val deserialized = json.decodeFromString(AppError.serializer(), serialized)

        assertEquals(error, deserialized)
        assertTrue(deserialized is AppError.AuthenticationError)
    }

    @Test
    fun testServerErrorSerialization() {
        val error = AppError.ServerError(500, "Internal Server Error")
        val serialized = json.encodeToString(AppError.serializer(), error)
        val deserialized = json.decodeFromString(AppError.serializer(), serialized)

        assertEquals(error, deserialized)
        assertTrue(deserialized is AppError.ServerError)
        assertEquals(500, (deserialized as AppError.ServerError).code)
        assertEquals("Internal Server Error", deserialized.message)
    }

    @Test
    fun testUnknownErrorSerialization() {
        val error = AppError.UnknownError("Something went wrong")
        val serialized = json.encodeToString(AppError.serializer(), error)
        val deserialized = json.decodeFromString(AppError.serializer(), serialized)

        assertEquals(error, deserialized)
        assertTrue(deserialized is AppError.UnknownError)
        assertEquals("Something went wrong", (deserialized as AppError.UnknownError).message)
    }

    @Test
    fun testInvitationExpiredErrorSerialization() {
        val error = AppError.InvitationExpiredError
        val serialized = json.encodeToString(AppError.serializer(), error)
        val deserialized = json.decodeFromString(AppError.serializer(), serialized)

        assertEquals(error, deserialized)
        assertTrue(deserialized is AppError.InvitationExpiredError)
    }

    @Test
    fun testInvitationAlreadyUsedErrorSerialization() {
        val error = AppError.InvitationAlreadyUsedError
        val serialized = json.encodeToString(AppError.serializer(), error)
        val deserialized = json.decodeFromString(AppError.serializer(), serialized)

        assertEquals(error, deserialized)
        assertTrue(deserialized is AppError.InvitationAlreadyUsedError)
    }

    @Test
    fun testUserNotFoundErrorSerialization() {
        val error = AppError.UserNotFoundError
        val serialized = json.encodeToString(AppError.serializer(), error)
        val deserialized = json.decodeFromString(AppError.serializer(), serialized)

        assertEquals(error, deserialized)
        assertTrue(deserialized is AppError.UserNotFoundError)
    }

    @Test
    fun testChatNotFoundErrorSerialization() {
        val error = AppError.ChatNotFoundError
        val serialized = json.encodeToString(AppError.serializer(), error)
        val deserialized = json.decodeFromString(AppError.serializer(), serialized)

        assertEquals(error, deserialized)
        assertTrue(deserialized is AppError.ChatNotFoundError)
    }
}