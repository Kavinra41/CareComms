package com.carecomms.domain.usecase

import com.carecomms.data.models.*
import com.carecomms.data.repository.AuthRepository
import com.carecomms.data.repository.LocalUserRepository
import com.carecomms.data.validation.CareeRegistrationValidator
import com.carecomms.data.validation.CareeValidationResult

class CareeRegistrationUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: LocalUserRepository,
    private val validator: CareeRegistrationValidator
) {
    
    suspend fun validateInvitationToken(token: String): Result<CarerInfo> {
        return authRepository.validateInvitationToken(token)
    }
    
    suspend fun registerCaree(
        registrationData: CareeRegistrationData,
        invitationToken: String
    ): Result<AuthResult> {
        return try {
            // Validate registration data
            val validationResult = validator.validate(registrationData)
            if (!validationResult.isValid) {
                return Result.failure(
                    ValidationException("Registration data is invalid: ${validationResult.errors}")
                )
            }
            
            // Validate invitation token
            val carerInfoResult = authRepository.validateInvitationToken(invitationToken)
            if (carerInfoResult.isFailure) {
                return Result.failure(
                    InvitationException("Invalid or expired invitation token")
                )
            }
            
            val carerInfo = carerInfoResult.getOrThrow()
            
            // Register caree with Firebase
            val authResult = authRepository.signUpCaree(registrationData, invitationToken)
            if (authResult.isFailure) {
                return authResult
            }
            
            val result = authResult.getOrThrow()
            
            // Store user locally
            userRepository.saveUser(result.user)
            
            // Create carer-caree relationship
            createCarerCareeRelationship(carerInfo.id, result.user.id)
            
            Result.success(result)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun createCarerCareeRelationship(carerId: String, careeId: String) {
        try {
            // Get existing carer
            val carer = userRepository.getUserById(carerId) as? Carer
            if (carer != null) {
                // Update carer's caree list
                val updatedCarer = carer.copy(
                    careeIds = carer.careeIds + careeId
                )
                userRepository.saveUser(updatedCarer)
            }
        } catch (e: Exception) {
            // Log error but don't fail the registration
            println("Warning: Failed to update carer-caree relationship: ${e.message}")
        }
    }
}

class ValidationException(message: String) : Exception(message)
class InvitationException(message: String) : Exception(message)