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
import com.carecomms.data.repository.AuthRepository
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

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
fun CareCommsApp(
    deepLinkUrl: String? = null,
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
            deepLinkUrl = deepLinkUrl,
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