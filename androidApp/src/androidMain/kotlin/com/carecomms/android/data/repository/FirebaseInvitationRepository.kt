package com.carecomms.android.data.repository

import com.carecomms.data.models.CarerInvitation
import com.carecomms.data.models.EmailInvitation
import com.carecomms.data.models.CarerCareeRelationship
import com.carecomms.data.models.RelationshipStatus
import com.carecomms.data.repository.InvitationRepository
import com.carecomms.data.repository.UserRepository
import com.carecomms.utils.CodeGenerator
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseInvitationRepository(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    private val emailService: EmailService
) : InvitationRepository {

    private val invitationsCollection = firestore.collection("carer_invitations")
    private val relationshipsCollection = firestore.collection("carer_caree_relationships")

    override suspend fun generateCarerCode(carerId: String): Result<String> {
        return try {
            println("FirebaseInvitationRepository: Generating code for carer: $carerId")
            val user = userRepository.getUser(carerId).getOrNull()
                ?: return Result.failure(Exception("User not found"))

            val invitationCode = CodeGenerator.generateCarerCode(carerId)
            println("FirebaseInvitationRepository: Generated invitation code: $invitationCode")
            
            val carerInvitation = CarerInvitation(
                carerId = carerId,
                carerName = user.name,
                carerEmail = user.email,
                invitationCode = invitationCode
            )

            // Store in Firestore with code as document ID for easy lookup
            val invitationData = mapOf(
                "carerId" to carerInvitation.carerId,
                "carerName" to carerInvitation.carerName,
                "carerEmail" to carerInvitation.carerEmail,
                "invitationCode" to carerInvitation.invitationCode,
                "createdAt" to carerInvitation.createdAt
            )

            println("FirebaseInvitationRepository: Storing invitation data in Firestore")
            invitationsCollection.document(invitationCode).set(invitationData).await()
            println("FirebaseInvitationRepository: Successfully stored invitation code: $invitationCode")
            
            Result.success(invitationCode)
        } catch (e: Exception) {
            println("FirebaseInvitationRepository: Error generating carer code: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getCarerByCode(invitationCode: String): Result<CarerInvitation?> {
        return try {
            println("FirebaseInvitationRepository: Looking up invitation code: $invitationCode")
            val document = invitationsCollection.document(invitationCode).get().await()
            println("FirebaseInvitationRepository: Document exists: ${document.exists()}")
            
            if (document.exists()) {
                val data = document.data!!
                println("FirebaseInvitationRepository: Document data: $data")
                val carerInvitation = CarerInvitation(
                    carerId = data["carerId"] as String,
                    carerName = data["carerName"] as String,
                    carerEmail = data["carerEmail"] as String,
                    invitationCode = data["invitationCode"] as String,
                    createdAt = data["createdAt"] as Long
                )
                println("FirebaseInvitationRepository: Successfully retrieved carer invitation: $carerInvitation")
                Result.success(carerInvitation)
            } else {
                println("FirebaseInvitationRepository: No document found for code: $invitationCode")
                Result.success(null)
            }
        } catch (e: Exception) {
            println("FirebaseInvitationRepository: Error getting carer by code: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun sendEmailInvitation(emailInvitation: EmailInvitation): Result<Unit> {
        return try {
            emailService.sendInvitationEmail(emailInvitation)
            Result.success(Unit)
        } catch (e: Exception) {
            println("FirebaseInvitationRepository: Error sending email: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun createCarerCareeRelationship(
        carerId: String, 
        careeId: String, 
        invitationCode: String
    ): Result<Unit> {
        return try {
            val carer = userRepository.getUser(carerId).getOrNull()
            val caree = userRepository.getUser(careeId).getOrNull()
            
            if (carer == null || caree == null) {
                return Result.failure(Exception("User not found"))
            }

            val relationshipId = "${carerId}_${careeId}"
            val relationship = CarerCareeRelationship(
                id = relationshipId,
                carerId = carerId,
                careeId = careeId,
                carerName = carer.name,
                careeName = caree.name,
                invitationCode = invitationCode,
                status = RelationshipStatus.ACTIVE
            )

            val relationshipData = mapOf(
                "id" to relationship.id,
                "carerId" to relationship.carerId,
                "careeId" to relationship.careeId,
                "carerName" to relationship.carerName,
                "careeName" to relationship.careeName,
                "invitationCode" to relationship.invitationCode,
                "createdAt" to relationship.createdAt,
                "status" to relationship.status.name
            )

            relationshipsCollection.document(relationshipId).set(relationshipData).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("FirebaseInvitationRepository: Error creating relationship: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getCarerRelationships(carerId: String): Result<List<CarerCareeRelationship>> {
        return try {
            val querySnapshot = relationshipsCollection
                .whereEqualTo("carerId", carerId)
                .whereEqualTo("status", RelationshipStatus.ACTIVE.name)
                .get()
                .await()

            val relationships = querySnapshot.documents.mapNotNull { document ->
                val data = document.data
                if (data != null) {
                    CarerCareeRelationship(
                        id = data["id"] as String,
                        carerId = data["carerId"] as String,
                        careeId = data["careeId"] as String,
                        carerName = data["carerName"] as String,
                        careeName = data["careeName"] as String,
                        invitationCode = data["invitationCode"] as String,
                        createdAt = data["createdAt"] as Long,
                        status = RelationshipStatus.valueOf(data["status"] as String)
                    )
                } else null
            }

            Result.success(relationships)
        } catch (e: Exception) {
            println("FirebaseInvitationRepository: Error getting carer relationships: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getCareeRelationships(careeId: String): Result<List<CarerCareeRelationship>> {
        return try {
            val querySnapshot = relationshipsCollection
                .whereEqualTo("careeId", careeId)
                .whereEqualTo("status", RelationshipStatus.ACTIVE.name)
                .get()
                .await()

            val relationships = querySnapshot.documents.mapNotNull { document ->
                val data = document.data
                if (data != null) {
                    CarerCareeRelationship(
                        id = data["id"] as String,
                        carerId = data["carerId"] as String,
                        careeId = data["careeId"] as String,
                        carerName = data["carerName"] as String,
                        careeName = data["careeName"] as String,
                        invitationCode = data["invitationCode"] as String,
                        createdAt = data["createdAt"] as Long,
                        status = RelationshipStatus.valueOf(data["status"] as String)
                    )
                } else null
            }

            Result.success(relationships)
        } catch (e: Exception) {
            println("FirebaseInvitationRepository: Error getting caree relationships: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun validateInvitationCode(code: String): Result<CarerInvitation?> {
        return getCarerByCode(code)
    }
}