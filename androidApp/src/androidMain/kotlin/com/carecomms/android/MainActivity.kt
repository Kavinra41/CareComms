package com.carecomms.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.carecomms.android.navigation.AuthNavigation
import com.carecomms.android.navigation.CarerNavigation
import com.carecomms.android.ui.screens.FirebaseLoginScreen
import com.carecomms.android.ui.theme.CareCommsTheme
import com.carecomms.android.utils.DeepLinkHandler
import com.carecomms.data.repository.AuthRepository
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle deep link from intent
        println("MainActivity: onCreate - Intent: ${intent}")
        println("MainActivity: onCreate - Intent action: ${intent?.action}")
        println("MainActivity: onCreate - Intent data: ${intent?.data}")
        val invitationCode = DeepLinkHandler.extractInvitationCode(intent)
        println("MainActivity: onCreate - Extracted invitation code: $invitationCode")
        
        setContent {
            CareCommsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    CareCommsApp(invitationCode = invitationCode)
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Handle deep links when app is already running
        println("MainActivity: onNewIntent - Intent: ${intent}")
        println("MainActivity: onNewIntent - Intent action: ${intent?.action}")
        println("MainActivity: onNewIntent - Intent data: ${intent?.data}")
        val invitationCode = DeepLinkHandler.extractInvitationCode(intent)
        println("MainActivity: onNewIntent - Extracted invitation code: $invitationCode")
        if (invitationCode != null) {
            // For now, restart the activity to handle the deep link
            // In a more sophisticated implementation, you'd update the navigation state
            recreate()
        }
    }
}

@Composable
fun CareCommsApp(
    invitationCode: String? = null,
    authRepository: AuthRepository = get()
) {
    var isAuthenticated by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf<com.carecomms.data.models.SimpleUser?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    val scope = rememberCoroutineScope()
    
    // Check if user is already signed in
    LaunchedEffect(Unit) {
        scope.launch {
            isAuthenticated = authRepository.isUserSignedIn()
            if (isAuthenticated) {
                currentUser = authRepository.getCurrentUser()
            }
            isLoading = false
        }
    }
    
    if (isLoading) {
        // Show splash screen while checking authentication
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (isAuthenticated && currentUser != null) {
        CarerApp(
            carerId = currentUser!!.uid,
            currentUser = currentUser!!,
            onLogout = {
                scope.launch {
                    authRepository.signOut()
                    isAuthenticated = false
                    currentUser = null
                }
            }
        )
    } else {
        // Show the original auth navigation flow (splash -> landing -> login/signup)
        AuthNavigation(
            invitationCode = invitationCode,
            onNavigateToHome = { userType ->
                // This callback will be triggered after successful authentication
                scope.launch {
                    isAuthenticated = authRepository.isUserSignedIn()
                    if (isAuthenticated) {
                        currentUser = authRepository.getCurrentUser()
                    }
                }
            }
        )
    }
}

@Composable
fun CarerApp(
    carerId: String,
    currentUser: com.carecomms.data.models.SimpleUser,
    onLogout: () -> Unit
) {
    CarerNavigation(
        carerId = carerId,
        currentUser = currentUser,
        onLogout = onLogout
    )
}