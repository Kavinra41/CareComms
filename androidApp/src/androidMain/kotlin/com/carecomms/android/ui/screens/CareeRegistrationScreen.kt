package com.carecomms.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carecomms.data.models.AuthResult
import com.carecomms.data.repository.AuthRepository
import com.carecomms.data.repository.InvitationRepository
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@Composable
fun CareeRegistrationScreen(
    onNavigateToHome: (String) -> Unit,
    onNavigateBack: () -> Unit,
    invitationCode: String? = null,
    authRepository: AuthRepository = get(),
    invitationRepository: InvitationRepository = get()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var enteredInvitationCode by remember { mutableStateOf(invitationCode ?: "") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Register as Care Recipient",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = enteredInvitationCode,
            onValueChange = { 
                enteredInvitationCode = it.uppercase()
                errorMessage = null
            },
            label = { Text("Invitation Code (Optional)") },
            placeholder = { Text("Enter 6-character code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage != null,
            enabled = invitationCode == null // Disable if code came from deep link
        )
        
        if (invitationCode != null) {
            Text(
                text = "✅ Connected via invitation link",
                color = MaterialTheme.colors.primary,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {
            Text(
                text = "Enter an invitation code to connect with a carer",
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = city,
            onValueChange = { 
                city = it
                errorMessage = null
            },
            label = { Text("City") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = errorMessage != null
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = Icons.Filled.Lock
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = Icons.Filled.Lock
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        Button(
            onClick = {
                when {
                    firstName.isBlank() || lastName.isBlank() || email.isBlank() || phoneNumber.isBlank() || city.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
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
                    val fullName = "$firstName $lastName"
                    val result = authRepository.signUpWithEmail(email, password, fullName, phoneNumber, city)
                    
                    when (result) {
                        is AuthResult.Success -> {
                            println("CareeRegistrationScreen: Signup successful for user: ${result.user.uid}")
                            
                            // Now that user is authenticated, validate invitation code if provided
                            if (finalInvitationCode != null) {
                                println("CareeRegistrationScreen: Validating invitation code: $finalInvitationCode")
                                val invitationResult = invitationRepository.validateInvitationCode(finalInvitationCode)
                                println("CareeRegistrationScreen: Validation result - success: ${invitationResult.isSuccess}, failure: ${invitationResult.isFailure}")
                                
                                if (invitationResult.isSuccess) {
                                    val carerInvitation = invitationResult.getOrNull()
                                    println("CareeRegistrationScreen: Retrieved carer invitation: $carerInvitation")
                                    
                                    if (carerInvitation != null) {
                                        // Create the relationship
                                        val relationshipResult = invitationRepository.createCarerCareeRelationship(
                                            carerId = carerInvitation.carerId,
                                            careeId = result.user.uid,
                                            invitationCode = finalInvitationCode
                                        )
                                        
                                        if (relationshipResult.isFailure) {
                                            println("CareeRegistrationScreen: Failed to create carer-caree relationship: ${relationshipResult.exceptionOrNull()?.message}")
                                            // Don't fail signup, just show a warning
                                            errorMessage = "Account created successfully, but failed to connect with carer. You can try connecting later."
                                        } else {
                                            println("CareeRegistrationScreen: Successfully created carer-caree relationship")
                                        }
                                    } else {
                                        println("CareeRegistrationScreen: Invitation code not found")
                                        errorMessage = "Account created successfully, but invitation code was not found. You can connect with a carer later."
                                    }
                                } else {
                                    val error = invitationResult.exceptionOrNull()
                                    println("CareeRegistrationScreen: Validation failed with error: ${error?.message}")
                                    // Don't fail signup, just show a warning
                                    errorMessage = "Account created successfully, but failed to validate invitation code. You can connect with a carer later."
                                }
                            }
                            
                            // Navigate to home regardless of invitation code validation
                            onNavigateToHome("caree")
                        }
                        is AuthResult.Error -> {
                            println("CareeRegistrationScreen: Signup error: ${result.message}")
                            errorMessage = result.message
                        }
                    }
                    
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colors.onPrimary
                )
            } else {
                Text("Register")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onNavigateBack) {
            Text("Back")
        }
    }
}