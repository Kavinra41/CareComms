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
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@Composable
fun SignupScreen(
    onNavigateToHome: (String) -> Unit,
    onNavigateBack: () -> Unit,
    authRepository: AuthRepository = get()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
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
                .padding(bottom = 24.dp),
            singleLine = true,
            isError = errorMessage != null
        )
        
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
                    val result = authRepository.signUpWithEmail(email, password, name, phoneNumber, city)
                    isLoading = false
                    
                    when (result) {
                        is AuthResult.Success -> {
                            println("Signup successful for user: ${result.user.uid}")
                            onNavigateToHome("carer") // Navigate to main app
                        }
                        is AuthResult.Error -> {
                            println("Signup error: ${result.message}")
                            errorMessage = result.message
                        }
                    }
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