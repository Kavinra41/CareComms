package com.carecomms.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carecomms.presentation.registration.CareeRegistrationState
import com.carecomms.presentation.registration.CareeRegistrationIntent

@Composable
fun CareeRegistrationScreen(
    state: CareeRegistrationState,
    onIntent: (CareeRegistrationIntent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Join CareComms") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = Color.White
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Carer Information Display
            state.carerInfo?.let { carerInfo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = 4.dp,
                    backgroundColor = MaterialTheme.colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "You've been invited by:",
                            style = MaterialTheme.typography.subtitle2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = carerInfo.name,
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Text(
                            text = "Location: ${carerInfo.location}",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Phone: ${carerInfo.phoneNumber}",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Registration Form
            Text(
                text = "Create Your Account",
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Email Field
            OutlinedTextField(
                value = state.email,
                onValueChange = { onIntent(CareeRegistrationIntent.UpdateEmail(it)) },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = state.validationErrors.any { it.field == "email" },
                singleLine = true
            )
            
            // Password Field
            OutlinedTextField(
                value = state.password,
                onValueChange = { onIntent(CareeRegistrationIntent.UpdatePassword(it)) },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = state.validationErrors.any { it.field == "password" },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )
            
            // Confirm Password Field
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { onIntent(CareeRegistrationIntent.UpdateConfirmPassword(it)) },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = state.password.isNotEmpty() && state.confirmPassword.isNotEmpty() && state.password != state.confirmPassword,
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Personal Details Section
            Text(
                text = "Personal Information",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // First Name Field
            OutlinedTextField(
                value = state.firstName,
                onValueChange = { onIntent(CareeRegistrationIntent.UpdateFirstName(it)) },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.validationErrors.any { it.field == "firstName" },
                singleLine = true
            )
            
            // Last Name Field
            OutlinedTextField(
                value = state.lastName,
                onValueChange = { onIntent(CareeRegistrationIntent.UpdateLastName(it)) },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.validationErrors.any { it.field == "lastName" },
                singleLine = true
            )
            
            // Date of Birth Field
            OutlinedTextField(
                value = state.dateOfBirth,
                onValueChange = { onIntent(CareeRegistrationIntent.UpdateDateOfBirth(it)) },
                label = { Text("Date of Birth (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.validationErrors.any { it.field == "dateOfBirth" },
                singleLine = true
            )
            
            // Address Field (Optional)
            OutlinedTextField(
                value = state.address,
                onValueChange = { onIntent(CareeRegistrationIntent.UpdateAddress(it)) },
                label = { Text("Address (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Emergency Contact Field (Optional)
            OutlinedTextField(
                value = state.emergencyContact,
                onValueChange = { onIntent(CareeRegistrationIntent.UpdateEmergencyContact(it)) },
                label = { Text("Emergency Contact (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Health Information Section
            Text(
                text = "Health Information",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Health Info Field
            OutlinedTextField(
                value = state.healthInfo,
                onValueChange = { onIntent(CareeRegistrationIntent.UpdateHealthInfo(it)) },
                label = { Text("Health Conditions & Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                isError = state.validationErrors.any { it.field == "healthInfo" }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Error Message
            state.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f),
                    elevation = 0.dp
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Register Button
            Button(
                onClick = { onIntent(CareeRegistrationIntent.RegisterCaree) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = state.isFormValid && !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = Color.White
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.button,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Validation Status
            if (state.isValidatingInvitation) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Validating invitation...",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}