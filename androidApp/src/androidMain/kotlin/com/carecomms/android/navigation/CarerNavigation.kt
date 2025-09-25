package com.carecomms.android.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.carecomms.android.ui.screens.*
import com.carecomms.android.ui.viewmodels.ChatListViewModel
import com.carecomms.android.ui.viewmodels.EditProfileViewModel
import com.carecomms.android.ui.viewmodels.ChatViewModel
import org.koin.androidx.compose.getViewModel

sealed class CarerScreen {
    object Dashboard : CarerScreen()
    object ChatList : CarerScreen()
    object Profile : CarerScreen()
    object EditProfile : CarerScreen()
    data class Chat(val otherUserId: String) : CarerScreen()
}

@Composable
fun CarerNavigation(
    carerId: String,
    currentUser: com.carecomms.data.models.SimpleUser,
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<CarerScreen>(CarerScreen.Dashboard) }
    var updatedUser by remember { mutableStateOf(currentUser) }
    
    Scaffold(
        bottomBar = {
            if (currentScreen !is CarerScreen.Chat && currentScreen !is CarerScreen.EditProfile) {
                CarerBottomNavigation(
                    currentScreen = currentScreen,
                    onScreenSelected = { screen ->
                        currentScreen = screen
                    }
                )
            }
        }
    ) { paddingValues ->
        when (currentScreen) {
            CarerScreen.Dashboard -> {
                DashboardScreen(
                    carerId = carerId
                )
            }
            
            CarerScreen.ChatList -> {
                val chatListViewModel: ChatListViewModel = getViewModel()
                ChatListScreen(
                    carerId = carerId,
                    viewModel = chatListViewModel,
                    onNavigateToChat = { otherUserId ->
                        currentScreen = CarerScreen.Chat(otherUserId)
                    }
                )
            }
            
            CarerScreen.Profile -> {
                ProfileScreen(
                    carerId = carerId,
                    currentUser = updatedUser,
                    onLogout = onLogout,
                    onEditProfile = {
                        currentScreen = CarerScreen.EditProfile
                    }
                )
            }
            
            CarerScreen.EditProfile -> {
                val editProfileViewModel: EditProfileViewModel = getViewModel()
                EditProfileScreen(
                    currentUser = updatedUser,
                    viewModel = editProfileViewModel,
                    onNavigateBack = {
                        currentScreen = CarerScreen.Profile
                    },
                    onProfileUpdated = { user ->
                        updatedUser = user
                    }
                )
            }
            
            is CarerScreen.Chat -> {
                val chatScreen = currentScreen as CarerScreen.Chat
                val chatViewModel: ChatViewModel = getViewModel()
                ChatScreen(
                    otherUserId = chatScreen.otherUserId,
                    viewModel = chatViewModel,
                    onNavigateBack = {
                        currentScreen = CarerScreen.ChatList
                    }
                )
            }
        }
    }
}

@Composable
private fun CarerBottomNavigation(
    currentScreen: CarerScreen,
    onScreenSelected: (CarerScreen) -> Unit
) {
    BottomNavigation {
        val items = listOf(
            BottomNavItem("Dashboard", Icons.Default.Home, CarerScreen.Dashboard),
            BottomNavItem("Messages", Icons.Default.Email, CarerScreen.ChatList),
            BottomNavItem("Profile", Icons.Default.Person, CarerScreen.Profile)
        )
        
        items.forEach { item ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = currentScreen == item.screen,
                onClick = { onScreenSelected(item.screen) }
            )
        }
    }
}

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: CarerScreen
)