package com.carecomms.android.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.carecomms.android.ui.viewmodels.ChatScreenViewModel
import com.carecomms.presentation.chat.ChatAction
import com.carecomms.presentation.chat.ChatEffect
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ChatContainer(
    chatId: String,
    currentUserId: String,
    otherUserName: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { 
        ChatScreenViewModel(
            chatId = chatId,
            currentUserId = currentUserId
        )
    }
    
    val state by viewModel.state.collectAsState()

    // Handle side effects
    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is ChatEffect.MessageSent -> {
                    // Message sent successfully - no action needed
                }
                is ChatEffect.MessagesMarkedAsRead -> {
                    // Messages marked as read - no action needed
                }
                is ChatEffect.ShowError -> {
                    // Error is already shown in the UI state
                }
                is ChatEffect.NavigateToChat -> {
                    // Not applicable for this screen
                }
            }
        }
    }

    // Load messages when the screen is first displayed
    LaunchedEffect(chatId) {
        viewModel.handleAction(ChatAction.LoadMessages(chatId))
    }

    // Mark all messages as read when entering the chat
    LaunchedEffect(chatId) {
        viewModel.handleAction(ChatAction.MarkAllAsRead)
    }

    // Clean up when leaving the screen
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.onCleared()
        }
    }

    ChatScreen(
        state = state,
        currentUserId = currentUserId,
        otherUserName = otherUserName,
        onAction = viewModel::handleAction,
        onBackClick = onBackClick
    )
}