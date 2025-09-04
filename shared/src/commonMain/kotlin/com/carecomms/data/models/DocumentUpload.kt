package com.carecomms.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DocumentUpload(
    val id: String,
    val fileName: String,
    val fileType: DocumentType,
    val uploadStatus: UploadStatus,
    val filePath: String? = null, // Placeholder for actual file path
    val uploadedAt: Long? = null
)

@Serializable
enum class DocumentType {
    PROFESSIONAL_CERTIFICATE,
    IDENTITY_DOCUMENT,
    BACKGROUND_CHECK,
    REFERENCE_LETTER,
    OTHER
}

@Serializable
enum class UploadStatus {
    PENDING,
    UPLOADING,
    COMPLETED,
    FAILED
}

// Placeholder document upload service
interface DocumentUploadService {
    suspend fun uploadDocument(fileName: String, documentType: DocumentType): Result<DocumentUpload>
    suspend fun deleteDocument(documentId: String): Result<Unit>
    suspend fun getUploadedDocuments(userId: String): Result<List<DocumentUpload>>
}

// Mock implementation for placeholder functionality
class MockDocumentUploadService : DocumentUploadService {
    
    override suspend fun uploadDocument(fileName: String, documentType: DocumentType): Result<DocumentUpload> {
        // Simulate document upload with placeholder
        val document = DocumentUpload(
            id = generateDocumentId(),
            fileName = fileName,
            fileType = documentType,
            uploadStatus = UploadStatus.COMPLETED,
            filePath = "placeholder/path/$fileName",
            uploadedAt = System.currentTimeMillis()
        )
        
        return Result.success(document)
    }
    
    override suspend fun deleteDocument(documentId: String): Result<Unit> {
        // Placeholder implementation
        return Result.success(Unit)
    }
    
    override suspend fun getUploadedDocuments(userId: String): Result<List<DocumentUpload>> {
        // Return empty list as placeholder
        return Result.success(emptyList())
    }
    
    private fun generateDocumentId(): String {
        return "doc_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}