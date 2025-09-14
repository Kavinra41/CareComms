package com.carecomms.android.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import com.carecomms.android.ui.screens.*
import com.carecomms.presentation.auth.AuthViewModel
import com.carecomms.presentation.auth.AuthAction
import com.carecomms.presentation.auth.AuthEffect
import com.carecomms.presentation.state.AuthState
import com.carecomms.presentation.registration.CarerRegistrationViewModel
import com.carecomms.presentation.registration.CarerRegistrationAction
import com.carecomms.presentation.registration.CarerRegistrationEffect
import com.carecomms.presentation.registration.CareeRegistrationViewModel
import com.carecomms.presentation.registration.CareeRegistrationIntent
import com.carecomms.presentation.invitation.InvitationViewModel
import com.carecomms.presentation.invitation.InvitationIntent
import com.carecomms.data.models.DocumentType
import com.carecomms.data.utils.DeepLinkHandler
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

sealed class AuthScreen {
    object Splash : AuthScreen()
    object Terms : AuthScreen()
    object Landing : AuthScreen()
    object Login : AuthScreen()
    object CarerRegistration : AuthScreen()
    object RegistrationSuccess : AuthScreen()
    data class CareeRegistration(val invitationToken: String) : AuthScreen()
    data class CareeRegistrationSuccess(val carerName: String) : AuthScreen()
    object Home : AuthScreen() // Placeholder for authenticated home
}

@Composable
fun AuthNavigation(
    deepLinkUrl: String? = null,
    onNavigateToHome: (String) -> Unit // userType: "carer" or "caree"
) {
    var currentScreen by remember { mutableStateOf<AuthScreen>(AuthScreen.Splash) }
    val authViewModel: AuthViewModel = koinViewModel()
    val carerRegistrationViewModel: CarerRegistrationViewModel = koinViewModel()
    val careeRegistrationViewModel: CareeRegistrationViewModel = koinViewModel()
    val invitationViewModel: InvitationViewModel = koinViewModel()
    val authState by authViewModel.state.collectAsState()
    val carerRegistrationState by carerRegistrationViewModel.state.collectAsState()
    val careeRegistrationState by careeRegistrationViewModel.state.collectAsState()
    val invitationState by invitationViewModel.state.collectAsState()
    
    // Handle deep link on initial load
    LaunchedEffect(deepLinkUrl) {
        deepLinkUrl?.let { url ->
            if (invitationViewModel.isValidInvitationUrl(url)) {
                val token = invitationViewModel.extractTokenFromUrl(url)
                if (token != null) {
                    careeRegistrationViewModel.handleIntent(CareeRegistrationIntent.ValidateInvitation(token))
                    currentScreen = AuthScreen.CareeRegistration(token)
                }
            }
        }
    }

    // Handle navigation effects
    LaunchedEffect(authViewModel) {
        authViewModel.effects.collectLatest { effect ->
            when (effect) {
                is AuthEffect.NavigateToHome -> {
                    when (val state = authState.authState) {
                        is AuthState.Authenticated -> {
                            onNavigateToHome(state.userType)
                        }
                        else -> {
                            // Handle unexpected state
                            currentScreen = AuthScreen.Landing
                        }
                    }
                }
                is AuthEffect.NavigateToLogin -> {
                    currentScreen = AuthScreen.Login
                }
                is AuthEffect.ShowError -> {
                    // Error is already handled in the UI state
                }
            }
        }
    }

    // Handle carer registration effects
    LaunchedEffect(carerRegistrationViewModel) {
        carerRegistrationViewModel.effects.collectLatest { effect ->
            when (effect) {
                is CarerRegistrationEffect.NavigateToChatList -> {
                    currentScreen = AuthScreen.RegistrationSuccess
                }
                is CarerRegistrationEffect.NavigateToLogin -> {
                    currentScreen = AuthScreen.Login
                }
                is CarerRegistrationEffect.ShowError -> {
                    // Error is already handled in the UI state
                }
                is CarerRegistrationEffect.ShowSuccess -> {
                    // Success is already handled in the UI state
                }
            }
        }
    }
    
    // Handle caree registration success
    LaunchedEffect(careeRegistrationState.isRegistrationSuccessful) {
        if (careeRegistrationState.isRegistrationSuccessful) {
            val carerName = careeRegistrationState.carerInfo?.name ?: "Your Carer"
            currentScreen = AuthScreen.CareeRegistrationSuccess(carerName)
        }
    }

    // Handle authentication state changes
    LaunchedEffect(authState.authState) {
        when (authState.authState) {
            is AuthState.Loading -> {
                // Stay on current screen while loading
            }
            is AuthState.Unauthenticated -> {
                if (currentScreen == AuthScreen.Home) {
                    currentScreen = AuthScreen.Landing
                }
            }
            is AuthState.Authenticated -> {
                onNavigateToHome(authState.authState.userType)
            }
            is AuthState.Error -> {
                if (currentScreen == AuthScreen.Splash) {
                    currentScreen = AuthScreen.Terms
                }
            }
        }
    }

    when (currentScreen) {
        AuthScreen.Splash -> {
            SplashScreen(
                onSplashComplete = {
                    currentScreen = AuthScreen.Terms
                }
            )
        }
        
        AuthScreen.Terms -> {
            TermsScreen(
                onAccept = {
                    currentScreen = AuthScreen.Landing
                },
                onDecline = {
                    // In a real app, this might close the app or show an exit dialog
                    currentScreen = AuthScreen.Terms
                }
            )
        }
        
        AuthScreen.Landing -> {
            LandingScreen(
                onLoginClick = {
                    currentScreen = AuthScreen.Login
                },
                onSignupClick = {
                    currentScreen = AuthScreen.CarerRegistration
                }
            )
        }
        
        AuthScreen.Login -> {
            LoginScreen(
                state = authState,
                onAction = authViewModel::handleAction,
                onBackClick = {
                    currentScreen = AuthScreen.Landing
                    authViewModel.handleAction(AuthAction.ClearError)
                }
            )
        }

        AuthScreen.CarerRegistration -> {
            CarerRegistrationScreen(
                state = carerRegistrationState,
                onAction = carerRegistrationViewModel::handleAction,
                onBackClick = {
                    currentScreen = AuthScreen.Landing
                    carerRegistrationViewModel.handleAction(CarerRegistrationAction.ClearErrors)
                },
                onDocumentUpload = { fileName, documentType ->
                    carerRegistrationViewModel.uploadDocument(fileName, documentType)
                }
            )
        }

        AuthScreen.RegistrationSuccess -> {
            RegistrationSuccessScreen(
                onNavigateToChatList = {
                    // Navigate to the main app as a carer
                    onNavigateToHome("carer")
                }
            )
        }
        
        is AuthScreen.CareeRegistration -> {
            CareeRegistrationScreen(
                state = careeRegistrationState,
                onIntent = careeRegistrationViewModel::handleIntent,
                onBackClick = {
                    currentScreen = AuthScreen.Landing
                    careeRegistrationViewModel.handleIntent(CareeRegistrationIntent.ClearError)
                }
            )
        }
        
        is AuthScreen.CareeRegistrationSuccess -> {
            CareeRegistrationSuccessScreen(
                carerName = currentScreen.carerName,
                onNavigateToChat = {
                    // Navigate to the main app as a caree
                    onNavigateToHome("caree")
                }
            )
        }
        
        AuthScreen.Home -> {
            // This should not be reached as we navigate to the main app
            // But included for completeness
        }
    }
}