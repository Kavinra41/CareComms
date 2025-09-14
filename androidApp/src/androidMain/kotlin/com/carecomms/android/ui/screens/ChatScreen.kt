package com.carecomms.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carecomms.android.ui.components.MessageBubble
import com.carecomms.android.ui.components.TypingIndicator
import com.carecomms.android.ui.theme.*
import com.carecomms.data.models.Message
import com.carecomms.data.models.MessageStatus
import com.carecomms.data.models.TypingStatus
import com.carecomms.presentation.chat.ChatAction
import com.carecomms.presentation.chat.ChatState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    state: ChatState,
    currentUserId: String,
    otherUserName: String,
    onAction: (ChatAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(state.messages.size - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = otherUserName,
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.onPrimary
                    )
                    if (state.isOnline) {
                        Text(
                            text = "Online",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colors.onPrimary
                    )
                }
            },
            backgroundColor = MaterialTheme.colors.primary,
            elevation = 4.dp
        )

        // Messages List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (state.isLoading && state.messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colors.primary
                    )
                }
            } else if (state.messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Start a conversation with ${otherUserName}",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.messages,
                        key = { it.id }
                    ) { message ->
                        MessageBubble(
                            message = message,
                            isFromCurrentUser = message.senderId == currentUserId,
                            onMessageClick = {
                                if (message.status != MessageStatus.READ && message.senderId != currentUserId) {
                                    onAction(ChatAction.MarkMessageAsRead(message.id))
                                }
                            }
                        )
                    }

                    // Typing indicator
                    state.otherUserTyping?.let { typingStatus ->
                        if (typingStatus.isTyping) {
                            item {
                                TypingIndicator(
                                    userName = otherUserName,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Error message overlay
            state.error?.let { error ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    backgroundColor = MaterialTheme.colors.error,
                    elevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colors.onError,
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { onAction(ChatAction.ClearError) }
                        ) {
                            Text(
                                text = "Dismiss",
                                color = MaterialTheme.colors.onError
                            )
                        }
                    }
                }
            }
        }

        // Message Input
        MessageInput(
            currentMessage = state.currentMessage,
            isSending = state.isSendingMessage,
            onMessageChange = { message ->
                onAction(ChatAction.UpdateCurrentMessage(message))
            },
            onSendMessage = { message ->
                if (message.isNotBlank()) {
                    onAction(ChatAction.SendMessage(message))
                    keyboardController?.hide()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MessageInput(
    currentMessage: String,
    isSending: Boolean,
    onMessageChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = currentMessage,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp, max = 120.dp),
                placeholder = {
                    Text(
                        text = "Type a message...",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (currentMessage.isNotBlank() && !isSending) {
                            onSendMessage(currentMessage)
                        }
                    }
                ),
                textStyle = MaterialTheme.typography.body1,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                    textColor = MaterialTheme.colors.onSurface
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            Button(
                onClick = {
                    if (currentMessage.isNotBlank() && !isSending) {
                        onSendMessage(currentMessage)
                    }
                },
                enabled = currentMessage.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(56.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(28.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colors.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send message",
                        tint = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}