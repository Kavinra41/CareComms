package com.carecomms.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.carecomms.data.database.DatabaseManager
import com.carecomms.data.models.*
import com.carecomms.database.CareCommsDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.*

class LocalUserRepositoryTest {
    
    private lateinit var driver: SqlDriver
    private lateinit var database: CareCommsDatabase
    private lateinit var databaseManager: DatabaseManager
    private lateinit var userRepository: LocalUserRepositoryImpl
    private val json = Json { ignoreUnknownKeys = true }
    
    @BeforeTest
    fun setup() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        CareCommsDatabase.Schema.create(driver)
        database = CareCommsDatabase(driver)
        databaseManager = DatabaseManager(database)
        userRepository = LocalUserRepositoryImpl(databaseManager, json)
    }
    
    @AfterTest
    fun teardown() {
        driver.close()
    }
    
    @Test
    fun testInsertAndGetCarer() = runTest {
        // Given
        val carer = Carer(
            id = "carer123",
            email = "carer@example.com",
            createdAt = System.currentTimeMillis(),
            documents = listOf("doc1.pdf", "doc2.pdf"),
            age = 30,
            phoneNumber = "+1234567890",
            location = "New York",
            careeIds = listOf("caree1", "caree2")
        )
        
        // When
        val insertResult = userRepository.insertUser(carer)
        val retrievedUser = userRepository.getUserById(carer.id)
        
        // Then
        assertTrue(insertResult.isSuccess)
        assertNotNull(retrievedUser)
        assertTrue(retrievedUser is Carer)
        val retrievedCarer = retrievedUser as Carer
        assertEquals(carer.id, retrievedCarer.id)
        assertEquals(carer.email, retrievedCarer.email)
        assertEquals(carer.age, retrievedCarer.age)
        assertEquals(carer.phoneNumber, retrievedCarer.phoneNumber)
        assertEquals(carer.location, retrievedCarer.location)
        assertEquals(carer.documents, retrievedCarer.documents)
        assertEquals(carer.careeIds, retrievedCarer.careeIds)
    }
}    @Te
st
    fun testInsertAndGetCaree() = runTest {
        // Given
        val caree = Caree(
            id = "caree123",
            email = "caree@example.com",
            createdAt = System.currentTimeMillis(),
            healthInfo = "Good health, no major issues",
            personalDetails = PersonalDetails(
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = "1950-01-01",
                address = "123 Main St",
                emergencyContact = "+1987654321"
            ),
            carerId = "carer123"
        )
        
        // When
        val insertResult = userRepository.insertUser(caree)
        val retrievedUser = userRepository.getUserById(caree.id)
        
        // Then
        assertTrue(insertResult.isSuccess)
        assertNotNull(retrievedUser)
        assertTrue(retrievedUser is Caree)
        val retrievedCaree = retrievedUser as Caree
        assertEquals(caree.id, retrievedCaree.id)
        assertEquals(caree.email, retrievedCaree.email)
        assertEquals(caree.healthInfo, retrievedCaree.healthInfo)
        assertEquals(caree.personalDetails.firstName, retrievedCaree.personalDetails.firstName)
        assertEquals(caree.personalDetails.lastName, retrievedCaree.personalDetails.lastName)
        assertEquals(caree.carerId, retrievedCaree.carerId)
    }
    
    @Test
    fun testGetUserByEmail() = runTest {
        // Given
        val carer = Carer(
            id = "carer123",
            email = "carer@example.com",
            createdAt = System.currentTimeMillis(),
            documents = listOf("doc1.pdf"),
            age = 30,
            phoneNumber = "+1234567890",
            location = "New York"
        )
        
        // When
        userRepository.insertUser(carer)
        val retrievedUser = userRepository.getUserByEmail(carer.email)
        
        // Then
        assertNotNull(retrievedUser)
        assertEquals(carer.id, retrievedUser.id)
        assertEquals(carer.email, retrievedUser.email)
    }
    
    @Test
    fun testUpdateUser() = runTest {
        // Given
        val originalCarer = Carer(
            id = "carer123",
            email = "carer@example.com",
            createdAt = System.currentTimeMillis(),
            documents = listOf("doc1.pdf"),
            age = 30,
            phoneNumber = "+1234567890",
            location = "New York"
        )
        
        val updatedCarer = originalCarer.copy(
            email = "updated@example.com",
            age = 31,
            location = "Boston"
        )
        
        // When
        userRepository.insertUser(originalCarer)
        val updateResult = userRepository.updateUser(updatedCarer)
        val retrievedUser = userRepository.getUserById(originalCarer.id)
        
        // Then
        assertTrue(updateResult.isSuccess)
        assertNotNull(retrievedUser)
        assertTrue(retrievedUser is Carer)
        val retrievedCarer = retrievedUser as Carer
        assertEquals(updatedCarer.email, retrievedCarer.email)
        assertEquals(updatedCarer.age, retrievedCarer.age)
        assertEquals(updatedCarer.location, retrievedCarer.location)
    }
}