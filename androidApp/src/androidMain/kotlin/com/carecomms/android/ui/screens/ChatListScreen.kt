package com.carecomms.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatListScreen(
    carerId: String,
    onNavigateToChat: (String) -> Unit
) {
    var chats by remember { mutableStateOf(generateMockChats()) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Messages",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary
            )
            
            IconButton(onClick = { /* Add new chat */ }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new chat"
                )
            }
        }
        
        // Chat list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(chats) { chat ->
                ChatListItem(
                    chat = chat,
                    onClick = { onNavigateToChat(chat.id) }
                )
            }
        }
    }
}

@Composable
private fun ChatListItem(
    chat: MockChat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chat.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = chat.lastMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = chat.timestamp,
                    fontSize = 12.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                )
                
                if (chat.unreadCount > 0) {
                    Badge(
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

data class MockChat(
    val id: String,
    val name: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int
)

private fun generateMockChats() = listOf(
    MockChat("1", "John Smith", "How are you feeling today?", "10:30 AM", 2),
    MockChat("2", "Mary Johnson", "Thank you for the reminder", "Yesterday", 0),
    MockChat("3", "Robert Brown", "I took my medication", "Tuesday", 1),
    MockChat("4", "Sarah Wilson", "See you tomorrow", "Monday", 0)
)