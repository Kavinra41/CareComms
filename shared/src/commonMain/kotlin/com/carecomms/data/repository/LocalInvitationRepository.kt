package com.carecomms.data.repository

import com.carecomms.data.database.DatabaseManager
import com.carecomms.data.models.InvitationData
import com.carecomms.domain.repository.InvitationRepository
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class LocalInvitationRepository(
    private val databaseManager: DatabaseManager,
    private val userRepository: LocalUserRepository,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : InvitationRepository {

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun generateInvitationLink(carerId: String): Result<String> {
        return try {
            // Verify carer exists
            val carer = userRepository.getUserById(carerId)
                ?: return Result.failure(Exception("Carer not found"))

            val token = Uuid.random().toString()
            val currentTime = System.currentTimeMillis()
            val expirationTime = currentTime + (7 * 24 * 60 * 60 * 1000L) // 7 days

            databaseManager.insertInvitation(
                token = token,
                carerId = carerId,
                expirationTime = expirationTime,
                createdAt = currentTime
            )

            // Clean up expired invitations
            databaseManager.deleteExpiredInvitations(currentTime)

            // Generate invitation link (in a real app, this would be a deep link)
            val invitationLink = "carecomms://invite?token=$token"
            
            Result.success(invitationLink)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun validateInvitation(token: String): Result<InvitationData> {
        return try {
            val currentTime = System.currentTimeMillis()
            val invitation = databaseManager.getValidInvitation(token, currentTime)
                ?: return Result.failure(Exception("Invalid or expired invitation"))

            val carer = userRepository.getUserById(invitation.carerId)
                ?: return Result.failure(Exception("Carer not found"))

            val carerName = when (carer) {
                is com.carecomms.data.models.Carer -> "${carer.phoneNumber}" // Using phone as identifier for now
                else -> carer.email
            }

            val invitationData = InvitationData(
                carerId = invitation.carerId,
                carerName = carerName,
                expirationTime = invitation.expirationTime,
                token = token,
                isUsed = invitation.isUsed == 1L
            )

            Result.success(invitationData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun acceptInvitation(token: String, careeId: String): Result<Unit> {
        return try {
            val currentTime = System.currentTimeMillis()
            
            // Validate invitation first
            val invitation = databaseManager.getValidInvitation(token, currentTime)
                ?: return Result.failure(Exception("Invalid or expired invitation"))

            // Verify caree exists
            val caree = userRepository.getUserById(careeId)
                ?: return Result.failure(Exception("Caree not found"))

            // Mark invitation as used
            databaseManager.markInvitationAsUsed(token)

            // Create chat between carer and caree
            val chatRepository = LocalChatRepository(databaseManager, json)
            chatRepository.createChat(invitation.carerId, careeId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getInvitationByToken(token: String): Result<InvitationData> {
        return try {
            val invitation = databaseManager.getInvitationByToken(token)
                ?: return Result.failure(Exception("Invitation not found"))

            val carer = userRepository.getUserById(invitation.carerId)
                ?: return Result.failure(Exception("Carer not found"))

            val carerName = when (carer) {
                is com.carecomms.data.models.Carer -> "${carer.phoneNumber}"
                else -> carer.email
            }

            val invitationData = InvitationData(
                carerId = invitation.carerId,
                carerName = carerName,
                expirationTime = invitation.expirationTime,
                token = token,
                isUsed = invitation.isUsed == 1L
            )

            Result.success(invitationData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markInvitationAsUsed(token: String): Result<Unit> {
        return try {
            databaseManager.markInvitationAsUsed(token)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActiveInvitations(carerId: String): Result<List<InvitationData>> {
        return try {
            val currentTime = System.currentTimeMillis()
            val invitations = databaseManager.getInvitationsByCarerId(carerId)
            
            val carer = userRepository.getUserById(carerId)
                ?: return Result.failure(Exception("Carer not found"))

            val carerName = when (carer) {
                is com.carecomms.data.models.Carer -> "${carer.phoneNumber}"
                else -> carer.email
            }

            val activeInvitations = invitations
                .filter { it.isUsed == 0L && it.expirationTime > currentTime }
                .map { invitation ->
                    InvitationData(
                        carerId = invitation.carerId,
                        carerName = carerName,
                        expirationTime = invitation.expirationTime,
                        token = invitation.token,
                        isUsed = false
                    )
                }

            Result.success(activeInvitations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun revokeInvitation(token: String): Result<Unit> {
        return try {
            // Mark invitation as used to effectively revoke it
            databaseManager.markInvitationAsUsed(token)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}