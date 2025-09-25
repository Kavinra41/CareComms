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
import com.carecomms.data.models.User
import com.carecomms.data.repository.AuthRepository
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@Composable
fun FirebaseLoginScreen(
    onLoginSuccess: (com.carecomms.data.models.SimpleUser) -> Unit,
    authRepository: AuthRepository = get()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSignUpMode by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Title
        Text(
            text = "CareComms",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        // Mode Title
        Text(
            text = if (isSignUpMode) "Create Account" else "Sign In",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Name Field (only for sign up)
        if (isSignUpMode) {
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
        }
        
        // Email Field
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
        
        // Password Field
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
                .padding(bottom = 24.dp),
            singleLine = true,
            isError = errorMessage != null
        )
        
        // Error Message
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colors.error,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Login/Sign Up Button
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill in all fields"
                    return@Button
                }
                
                isLoading = true
                scope.launch {
                    val result = if (isSignUpMode) {
                        authRepository.signUpWithEmail(email, password, name, "", "")
                    } else {
                        authRepository.signInWithEmail(email, password)
                    }
                    
                    isLoading = false
                    
                    when (result) {
                        is AuthResult.Success -> {
                            onLoginSuccess(result.user)
                        }
                        is AuthResult.Error -> {
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
                    text = if (isSignUpMode) "Create Account" else "Sign In",
                    fontSize = 16.sp
                )
            }
        }
        
        // Toggle Sign Up/Sign In
        TextButton(
            onClick = { 
                isSignUpMode = !isSignUpMode
                errorMessage = null
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = if (isSignUpMode) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                color = MaterialTheme.colors.primary
            )
        }
    }
}