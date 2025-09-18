package com.carecomms.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatScreen(
    chatId: String,
    onNavigateBack: () -> Unit
) {
    var messages by remember { mutableStateOf(generateMockMessages()) }
    var newMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        TopAppBar(
            title = {
                Text(
                    text = "John Smith", // Mock name
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        // Messages
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message = message)
            }
        }
        
        // Message input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                placeholder = { Text("Type a message...") },
                modifier = Modifier.weight(1f),
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = {
                    if (newMessage.isNotBlank()) {
                        messages = messages + MockMessage(
                            id = (messages.size + 1).toString(),
                            text = newMessage,
                            isFromMe = true,
                            timestamp = "Now"
                        )
                        newMessage = ""
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colors.primary
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: MockMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                bottomEnd = if (message.isFromMe) 4.dp else 16.dp
            ),
            backgroundColor = if (message.isFromMe) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
            elevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (message.isFromMe) Color.White else MaterialTheme.colors.onSurface,
                    fontSize = 14.sp
                )
                
                Text(
                    text = message.timestamp,
                    color = if (message.isFromMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

data class MockMessage(
    val id: String,
    val text: String,
    val isFromMe: Boolean,
    val timestamp: String
)

private fun generateMockMessages() = listOf(
    MockMessage("1", "Hi John, how are you feeling today?", true, "10:00 AM"),
    MockMessage("2", "I'm doing well, thank you for asking!", false, "10:05 AM"),
    MockMessage("3", "Did you remember to take your morning medication?", true, "10:10 AM"),
    MockMessage("4", "Yes, I took it with breakfast as usual.", false, "10:15 AM"),
    MockMessage("5", "Great! Let me know if you need anything.", true, "10:20 AM")
)