package com.carecomms.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carecomms.android.ui.viewmodels.ChatViewModel
import com.carecomms.data.models.Message
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    otherUserId: String,
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val newMessage by viewModel.newMessage.collectAsState()
    val otherUserName by viewModel.otherUserName.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val listState = rememberLazyListState()

    // Initialize chat when screen loads
    LaunchedEffect(otherUserId) {
        viewModel.initializeChat(otherUserId)
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            // Custom Header (WhatsApp-like)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.primary,
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    // Profile picture placeholder
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = otherUserName,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isLoading) "Loading..." else "Online",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Message input area (WhatsApp-like)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                elevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = Color(0xFFF0F0F0),
                        elevation = 0.dp
                    ) {
                        TextField(
                            value = newMessage,
                            onValueChange = viewModel::updateNewMessage,
                            placeholder = { 
                                Text(
                                    "Type a message...",
                                    color = Color.Gray
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            maxLines = 4,
                            enabled = !isLoading && !isSending
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Send button
                    FloatingActionButton(
                        onClick = { viewModel.sendMessage() },
                        modifier = Modifier.size(48.dp),
                        backgroundColor = if (newMessage.isNotBlank() && !isLoading && !isSending) {
                            MaterialTheme.colors.primary
                        } else {
                            Color.Gray
                        },
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp
                        )
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        backgroundColor = Color(0xFFF5F5F5) // Light gray background like WhatsApp
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Error message
            error?.let { errorMessage ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colors.error,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            // Messages area
            Box(modifier = Modifier.weight(1f)) {
                if (messages.isEmpty() && !isLoading) {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Start a conversation with $otherUserName",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        reverseLayout = false
                    ) {
                        items(messages) { message ->
                            MessageBubble(
                                message = message,
                                isFromCurrentUser = viewModel.isMessageFromCurrentUser(message)
                            )
                        }
                    }
                }
                
                // Loading indicator overlay
                if (isLoading && messages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isFromCurrentUser: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        // Add some space on the opposite side for better WhatsApp-like appearance
        if (isFromCurrentUser) {
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        // Dynamic width based on content length - more WhatsApp-like
        val dynamicModifier = if (message.content.length < 20) {
            Modifier.wrapContentWidth()
        } else {
            Modifier.widthIn(min = 120.dp, max = 280.dp)
        }
        
        Card(
            modifier = dynamicModifier,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isFromCurrentUser) 18.dp else 4.dp,
                bottomEnd = if (isFromCurrentUser) 4.dp else 18.dp
            ),
            backgroundColor = if (isFromCurrentUser) {
                Color(0xFF128C7E) // WhatsApp green for sent messages
            } else {
                Color.White
            },
            elevation = 1.dp
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = message.content,
                        color = if (isFromCurrentUser) Color.White else Color.Black,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(
                            end = if (isFromCurrentUser) 50.dp else 40.dp,
                            bottom = 2.dp
                        )
                    )
                }
                
                // Time and status positioned at bottom right
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestamp(message.timestamp),
                        color = if (isFromCurrentUser) {
                            Color.White.copy(alpha = 0.7f)
                        } else {
                            Color.Gray
                        },
                        fontSize = 11.sp
                    )
                    
                    if (isFromCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        // Message status indicator (like WhatsApp checkmarks)
                        Text(
                            text = when (message.status.name) {
                                "SENT" -> "✓"
                                "DELIVERED" -> "✓✓"
                                "READ" -> "✓✓"
                                else -> ""
                            },
                            color = if (message.status.name == "READ") {
                                Color(0xFF4FC3F7) // Blue for read
                            } else {
                                Color.White.copy(alpha = 0.7f)
                            },
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
        
        if (!isFromCurrentUser) {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Now" // Less than 1 minute
        diff < 3600_000 -> "${diff / 60_000}m ago" // Less than 1 hour
        diff < 86400_000 -> { // Less than 1 day
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
        else -> { // More than 1 day
            val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
    }
}

