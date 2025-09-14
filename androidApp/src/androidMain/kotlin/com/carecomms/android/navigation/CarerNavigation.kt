package com.carecomms.android.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.carecomms.android.ui.screens.*

@Composable
fun CarerNavigation(
    carerId: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    var selectedChatId by remember { mutableStateOf("") }
    var selectedCareeeName by remember { mutableStateOf("") }
    
    Scaffold(
        modifier = modifier,
        bottomBar = {
            // Only show bottom navigation for main screens, not chat detail
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute != "chat_detail") {
                CarerBottomNavigation(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CarerBottomNavItem.ChatList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(CarerBottomNavItem.ChatList.route) {
                ChatListContainer(
                    carerId = carerId,
                    onChatClick = { chatId ->
                        selectedChatId = chatId
                        // In a real app, we would get the caree name from the chat data
                        // For now, we'll use a mock name based on chatId
                        selectedCareeeName = when (chatId) {
                            "chat-1" -> "Alice Johnson"
                            "chat-2" -> "Bob Smith"
                            "chat-3" -> "Carol Davis"
                            else -> "Care Recipient"
                        }
                        navController.navigate("chat_detail")
                    }
                )
            }
            
            composable(CarerBottomNavItem.Dashboard.route) {
                DashboardScreen(carerId = carerId)
            }
            
            composable(CarerBottomNavItem.DetailsTree.route) {
                DetailsTreeScreen(carerId = carerId)
            }
            
            composable(CarerBottomNavItem.Profile.route) {
                ProfileScreen(
                    carerId = carerId,
                    onLogout = onLogout
                )
            }
            
            composable("chat_detail") {
                ChatContainer(
                    chatId = selectedChatId,
                    currentUserId = carerId,
                    otherUserName = selectedCareeeName,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}