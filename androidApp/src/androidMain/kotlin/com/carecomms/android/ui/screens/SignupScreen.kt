package com.carecomms.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carecomms.data.models.AuthResult
import com.carecomms.data.repository.AuthRepository
import com.carecomms.data.repository.InvitationRepository
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@Composable
fun SignupScreen(
    onNavigateToHome: (String) -> Unit,
    onNavigateBack: () -> Unit,
    invitationCode: String? = null,
    authRepository: AuthRepository = get(),
    invitationRepository: InvitationRepository = get()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var enteredInvitationCode by remember { mutableStateOf(invitationCode ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        OutlinedTextField(
            value = name,
            onValueChange = { 
                name = it
                errorMessage = null
            },
            label = { Text("Full Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            isError = errorMessage != null
        )
        
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                errorMessage = null
            },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            isError = errorMessage != null
        )
        
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { 
                phoneNumber = it
                errorMessage = null
            },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            isError = errorMessage != null
        )
        
        OutlinedTextField(
            value = city,
            onValueChange = { 
                city = it
                errorMessage = null
            },
            label = { Text("City") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            isError = errorMessage != null
        )
        
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                errorMessage = null
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            isError = errorMessage != null
        )
        
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                errorMessage = null
            },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            isError = errorMessage != null
        )
        
        // Invitation Code Field
        OutlinedTextField(
            value = enteredInvitationCode,
            onValueChange = { 
                enteredInvitationCode = it.uppercase()
                errorMessage = null
            },
            label = { Text("Invitation Code (Optional)") },
            placeholder = { Text("Enter 6-character code") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            singleLine = true,
            isError = errorMessage != null,
            enabled = invitationCode == null // Disable if code came from deep link
        )
        
        if (invitationCode != null) {
            Text(
                text = "✅ Connected via invitation link",
                color = MaterialTheme.colors.primary,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            Text(
                text = "Enter an invitation code to connect with a carer",
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colors.error,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        Button(
            onClick = {
                when {
                    name.isBlank() || email.isBlank() || phoneNumber.isBlank() || city.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                        errorMessage = "Please fill in all fields"
                        return@Button
                    }
                    password != confirmPassword -> {
                        errorMessage = "Passwords do not match"
                        return@Button
                    }
                    password.length < 6 -> {
                        errorMessage = "Password must be at least 6 characters"
                        return@Button
                    }
                }
                
                isLoading = true
                scope.launch {
                    val finalInvitationCode = enteredInvitationCode.takeIf { it.isNotBlank() }
                    
                    // First, create the user account
                    val result = authRepository.signUpWithEmail(email, password, name, phoneNumber, city)
                    
                    when (result) {
                        is AuthResult.Success -> {
                            println("SignupScreen: Signup successful for user: ${result.user.uid}")
                            var userType = "carer" // Default to carer
                            
                            // Now that user is authenticated, validate invitation code if provided
                            if (finalInvitationCode != null) {
                                println("SignupScreen: Validating invitation code: $finalInvitationCode")
                                val invitationResult = invitationRepository.validateInvitationCode(finalInvitationCode)
                                println("SignupScreen: Validation result - success: ${invitationResult.isSuccess}, failure: ${invitationResult.isFailure}")
                                
                                if (invitationResult.isSuccess) {
                                    val carerInvitation = invitationResult.getOrNull()
                                    println("SignupScreen: Retrieved carer invitation: $carerInvitation")
                                    
                                    if (carerInvitation != null) {
                                        // Create the relationship
                                        val relationshipResult = invitationRepository.createCarerCareeRelationship(
                                            carerId = carerInvitation.carerId,
                                            careeId = result.user.uid,
                                            invitationCode = finalInvitationCode
                                        )
                                        
                                        if (relationshipResult.isSuccess) {
                                            println("SignupScreen: Successfully created carer-caree relationship")
                                            userType = "caree" // User becomes a caree
                                        } else {
                                            println("SignupScreen: Failed to create carer-caree relationship: ${relationshipResult.exceptionOrNull()?.message}")
                                            // Don't fail signup, just show a warning
                                            errorMessage = "Account created successfully, but failed to connect with carer. You can try connecting later."
                                        }
                                    } else {
                                        println("SignupScreen: Invitation code not found")
                                        errorMessage = "Account created successfully, but invitation code was not found. You can connect with a carer later."
                                    }
                                } else {
                                    val error = invitationResult.exceptionOrNull()
                                    println("SignupScreen: Validation failed with error: ${error?.message}")
                                    // Don't fail signup, just show a warning
                                    errorMessage = "Account created successfully, but failed to validate invitation code. You can connect with a carer later."
                                }
                            }
                            
                            // Navigate to home regardless of invitation code validation
                            onNavigateToHome(userType)
                        }
                        is AuthResult.Error -> {
                            println("SignupScreen: Signup error: ${result.message}")
                            errorMessage = result.message
                        }
                    }
                    
                    isLoading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colors.onPrimary
                )
            } else {
                Text(
                    text = "Create Account",
                    fontSize = 16.sp
                )
            }
        }
        
        TextButton(
            onClick = onNavigateBack,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Back")
        }
        
        // Debug button to test invitation code validation
        TextButton(
            onClick = {
                errorMessage = "ℹ️ Invitation codes are validated after account creation. Create an account to test the invitation flow."
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("About Invitation Validation", color = MaterialTheme.colors.secondary)
        }
        
        // Debug button to test Firestore directly
        if (errorMessage?.contains("Failed to save user data") == true) {
            TextButton(
                onClick = {
                    scope.launch {
                        try {
                            val testUser = com.carecomms.data.models.SimpleUser(
                                uid = "test-uid-${System.currentTimeMillis()}",
                                email = "test@example.com",
                                name = "Test User",
                                phoneNumber = "1234567890",
                                city = "Test City"
                            )
                            val firestoreRepo = com.carecomms.data.repository.FirebaseFirestoreRepository(
                                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            )
                            val result = firestoreRepo.saveUser(testUser)
                            if (result.isSuccess) {
                                errorMessage = "Firestore test successful!"
                            } else {
                                errorMessage = "Firestore test failed: ${result.exceptionOrNull()?.message}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Firestore test exception: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Test Firestore Connection", color = MaterialTheme.colors.primary)
            }
        }
    }
}