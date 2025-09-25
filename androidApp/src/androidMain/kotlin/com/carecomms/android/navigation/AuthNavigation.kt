package com.carecomms.android.navigation

import androidx.compose.runtime.*
import com.carecomms.android.ui.screens.*

sealed class AuthScreen {
    object Splash : AuthScreen()
    object Landing : AuthScreen()
    object Login : AuthScreen()
    object Signup : AuthScreen()
    object CarerRegistration : AuthScreen()
    object CareeRegistration : AuthScreen()
    object Terms : AuthScreen()
}

@Composable
fun AuthNavigation(
    deepLinkUrl: String? = null,
    onNavigateToHome: (String) -> Unit
) {
    var currentScreen by remember { mutableStateOf<AuthScreen>(AuthScreen.Splash) }
    
    when (currentScreen) {
        AuthScreen.Splash -> {
            SplashScreen(
                onNavigateToLanding = {
                    currentScreen = AuthScreen.Landing
                }
            )
        }
        
        AuthScreen.Landing -> {
            LandingScreen(
                onNavigateToLogin = {
                    currentScreen = AuthScreen.Login
                },
                onNavigateToSignup = {
                    currentScreen = AuthScreen.Signup
                },
                onNavigateToCarerRegistration = {
                    currentScreen = AuthScreen.CarerRegistration
                },
                onNavigateToCareeRegistration = {
                    currentScreen = AuthScreen.CareeRegistration
                }
            )
        }
        
        AuthScreen.Login -> {
            LoginScreen(
                onNavigateToHome = onNavigateToHome,
                onNavigateBack = {
                    currentScreen = AuthScreen.Landing
                }
            )
        }
        
        AuthScreen.Signup -> {
            SignupScreen(
                onNavigateToHome = onNavigateToHome,
                onNavigateBack = {
                    currentScreen = AuthScreen.Landing
                }
            )
        }
        
        AuthScreen.CarerRegistration -> {
            CarerRegistrationScreen(
                onNavigateToHome = onNavigateToHome,
                onNavigateBack = {
                    currentScreen = AuthScreen.Landing
                }
            )
        }
        
        AuthScreen.CareeRegistration -> {
            CareeRegistrationScreen(
                onNavigateToHome = onNavigateToHome,
                onNavigateBack = {
                    currentScreen = AuthScreen.Landing
                }
            )
        }
        
        AuthScreen.Terms -> {
            TermsScreen(
                onAccept = {
                    currentScreen = AuthScreen.Landing
                },
                onDecline = {
                    currentScreen = AuthScreen.Landing
                }
            )
        }
    }
}