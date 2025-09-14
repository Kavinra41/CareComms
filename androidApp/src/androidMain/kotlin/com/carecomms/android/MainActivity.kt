package com.carecomms.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.style.TextAlign
import com.carecomms.android.navigation.AuthNavigation
import com.carecomms.android.navigation.CarerNavigation
import com.carecomms.android.ui.theme.CareCommsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle deep link from intent
        val deepLinkUrl = intent?.data?.toString()
        
        setContent {
            CareCommsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    CareCommsApp(deepLinkUrl = deepLinkUrl)
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Handle deep links when app is already running
        intent?.data?.toString()?.let { url ->
            // This would need to be handled by updating the navigation state
            // For now, we'll handle it in the initial onCreate
        }
    }
}

@Composable
fun CareCommsApp(deepLinkUrl: String? = null) {
    var isAuthenticated by remember { mutableStateOf(false) }
    var userType by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }

    if (isAuthenticated) {
        when (userType) {
            "carer" -> {
                CarerApp(
                    carerId = userId,
                    onLogout = {
                        isAuthenticated = false
                        userType = ""
                        userId = ""
                    }
                )
            }
            "caree" -> {
                // Placeholder for caree app content
                // This will be implemented in later tasks
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Welcome to CareComms!\n\nCaree interface will be implemented in upcoming tasks.",
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.primary
                    )
                }
            }
            else -> {
                // Fallback for unknown user type
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Unknown user type: $userType",
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.error
                    )
                }
            }
        }
    } else {
        AuthNavigation(
            deepLinkUrl = deepLinkUrl,
            onNavigateToHome = { type ->
                userType = type
                userId = "mock-user-id" // In real app, this would come from auth
                isAuthenticated = true
            }
        )
    }
}

@Composable
fun CarerApp(
    carerId: String,
    onLogout: () -> Unit
) {
    CarerNavigation(
        carerId = carerId,
        onLogout = onLogout
    )
}