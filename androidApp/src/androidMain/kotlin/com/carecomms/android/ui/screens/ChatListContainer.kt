package com.carecomms.android.ui.screens

import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carecomms.android.ui.components.InvitationShareDialog
import com.carecomms.android.ui.viewmodels.ChatListScreenViewModel
import com.carecomms.presentation.chat.ChatListAction
import com.carecomms.presentation.chat.ChatEffect
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ChatListContainer(
    carerId: String,
    onChatClick: (String) -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToDetailsTree: () -> Unit = {}
) {
    val viewModel: ChatListScreenViewModel = koinViewModel { parametersOf(carerId) }
    val state by viewModel.state.collectAsState()
    val invitationState by viewModel.invitationState.collectAsState()
    
    var showInvitationDialog by remember { mutableStateOf(false) }
    
    // Handle chat effects
    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is ChatEffect.NavigateToChat -> {
                    onChatClick(effect.chatId)
                }
                is ChatEffect.ShowError -> {
                    // Error is already handled in the state
                }
                else -> {
                    // Handle other effects if needed
                }
            }
        }
    }
    
    ChatListScreen(
        state = state,
        onAction = viewModel::handleAction,
        onChatClick = onChatClick,
        onInviteClick = {
            showInvitationDialog = true
            viewModel.generateInvitationLink()
        }
    )
    
    // Invitation Dialog
    if (showInvitationDialog) {
        InvitationShareDialog(
            invitationUrl = invitationState.invitationUrl,
            isLoading = invitationState.isLoading,
            error = invitationState.error,
            onDismiss = {
                showInvitationDialog = false
                viewModel.dismissInvitationDialog()
            },
            onRetry = viewModel::retryInvitationGeneration
        )
    }
}