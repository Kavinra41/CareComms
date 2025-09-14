package com.carecomms.android.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carecomms.android.ui.theme.CareCommsTheme
import com.carecomms.data.models.DocumentType
import com.carecomms.data.models.DocumentUpload
import com.carecomms.data.models.UploadStatus
import com.carecomms.presentation.registration.CarerRegistrationAction
import com.carecomms.presentation.registration.CarerRegistrationState

@Composable
fun CarerRegistrationScreen(
    state: CarerRegistrationState,
    onAction: (CarerRegistrationAction) -> Unit,
    onBackClick: () -> Unit,
    onDocumentUpload: (String, DocumentType) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showDocumentTypeDialog by remember { mutableStateOf(false) }
    var selectedDocumentType by remember { mutableStateOf(DocumentType.PROFESSIONAL_CERTIFICATE) }
    
    val focusManager = LocalFocusManager.current
    
    // Document picker launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // In a real implementation, you would extract the filename from the URI
            val fileName = "document_${System.currentTimeMillis()}.pdf"
            onDocumentUpload(fileName, selectedDocumentType)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onBackClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colors.primary
                    )
                ) {
                    Text("← Back")
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "Carer Registration",
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.primary
                )
                
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Create Your Account",
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Please provide your professional details to register as a carer",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            // Email field
            OutlinedTextField(
                value = state.email,
                onValueChange = { onAction(CarerRegistrationAction.UpdateEmail(it)) },
                label = { Text("Email Address") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                isError = state.validationErrors.containsKey("email"),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            state.validationErrors["email"]?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            // Password field
            OutlinedTextField(
                value = state.password,
                onValueChange = { onAction(CarerRegistrationAction.UpdatePassword(it)) },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password"
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible }
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                isError = state.validationErrors.containsKey("password"),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            state.validationErrors["password"]?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            // Confirm Password field
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { onAction(CarerRegistrationAction.UpdateConfirmPassword(it)) },
                label = { Text("Confirm Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Confirm Password"
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                    ) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                isError = state.validationErrors.containsKey("confirmPassword"),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            state.validationErrors["confirmPassword"]?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            // Age field
            OutlinedTextField(
                value = state.age,
                onValueChange = { onAction(CarerRegistrationAction.UpdateAge(it)) },
                label = { Text("Age") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Age"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                isError = state.validationErrors.containsKey("age"),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            state.validationErrors["age"]?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            // Phone Number field
            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = { onAction(CarerRegistrationAction.UpdatePhoneNumber(it)) },
                label = { Text("Phone Number") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone Number"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                isError = state.validationErrors.containsKey("phoneNumber"),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            state.validationErrors["phoneNumber"]?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            // Location field
            OutlinedTextField(
                value = state.location,
                onValueChange = { onAction(CarerRegistrationAction.UpdateLocation(it)) },
                label = { Text("Location") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                isError = state.validationErrors.containsKey("location"),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            state.validationErrors["location"]?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            // Document Upload Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Professional Documents",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.primary
                    )
                    
                    Text(
                        text = "Please upload your professional certificates and identification documents",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface
                    )

                    Button(
                        onClick = { showDocumentTypeDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.secondary,
                            contentColor = MaterialTheme.colors.onSecondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Document",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Document")
                    }

                    if (state.validationErrors.containsKey("documents")) {
                        Text(
                            text = state.validationErrors["documents"] ?: "",
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }

        // Document list
        if (state.uploadedDocuments.isNotEmpty()) {
            items(state.uploadedDocuments) { document ->
                DocumentItem(
                    document = document,
                    onRemove = { onAction(CarerRegistrationAction.RemoveDocument(document.id)) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Register button
            Button(
                onClick = { onAction(CarerRegistrationAction.SubmitRegistration) },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.button
                    )
                }
            }
        }

        // Error message
        state.registrationError?.let { error ->
            item {
                Card(
                    backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Document Type Selection Dialog
    if (showDocumentTypeDialog) {
        AlertDialog(
            onDismissRequest = { showDocumentTypeDialog = false },
            title = { Text("Select Document Type") },
            text = {
                Column {
                    DocumentType.values().forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDocumentType == type,
                                onClick = { selectedDocumentType = type }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = type.name.replace("_", " ").lowercase()
                                    .split(" ")
                                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                                style = MaterialTheme.typography.body2
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDocumentTypeDialog = false
                        documentPickerLauncher.launch("*/*")
                    }
                ) {
                    Text("Select File")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDocumentTypeDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DocumentItem(
    document: DocumentUpload,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "Document",
                tint = MaterialTheme.colors.primary
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.fileName,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = document.fileType.name.replace("_", " ").lowercase()
                        .split(" ")
                        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
            
            when (document.uploadStatus) {
                UploadStatus.UPLOADING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colors.primary
                    )
                }
                UploadStatus.COMPLETED -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Uploaded",
                        tint = MaterialTheme.colors.primary
                    )
                }
                UploadStatus.FAILED -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Failed",
                        tint = MaterialTheme.colors.error
                    )
                }
                UploadStatus.PENDING -> {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Pending",
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Document",
                    tint = MaterialTheme.colors.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CarerRegistrationScreenPreview() {
    CareCommsTheme {
        CarerRegistrationScreen(
            state = CarerRegistrationState(
                email = "john.doe@example.com",
                age = "35",
                phoneNumber = "+1234567890",
                location = "New York, NY",
                uploadedDocuments = listOf(
                    DocumentUpload(
                        id = "1",
                        fileName = "certificate.pdf",
                        fileType = DocumentType.PROFESSIONAL_CERTIFICATE,
                        uploadStatus = UploadStatus.COMPLETED
                    )
                )
            ),
            onAction = {},
            onBackClick = {},
            onDocumentUpload = { _, _ -> }
        )
    }
}