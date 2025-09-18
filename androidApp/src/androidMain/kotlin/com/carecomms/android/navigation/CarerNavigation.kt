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

sealed class CarerScreen {
    object Dashboard : CarerScreen()
    object ChatList : CarerScreen()
    object Profile : CarerScreen()
    data class Chat(val chatId: String) : CarerScreen()
}

@Composable
fun CarerNavigation(
    carerId: String,
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<CarerScreen>(CarerScreen.Dashboard) }
    
    Scaffold(
        bottomBar = {
            if (currentScreen !is CarerScreen.Chat) {
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
                ChatListScreen(
                    carerId = carerId,
                    onNavigateToChat = { chatId ->
                        currentScreen = CarerScreen.Chat(chatId)
                    }
                )
            }
            
            CarerScreen.Profile -> {
                ProfileScreen(
                    carerId = carerId,
                    onLogout = onLogout
                )
            }
            
            is CarerScreen.Chat -> {
                val chatScreen = currentScreen as CarerScreen.Chat
                ChatScreen(
                    chatId = chatScreen.chatId,
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