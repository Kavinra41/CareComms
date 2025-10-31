package com.carecomms.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carecomms.data.repository.InvitationRepository
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@Composable
fun DashboardScreen(
    carerId: String,
    currentUser: com.carecomms.data.models.SimpleUser? = null,
    onNavigateToInvitation: () -> Unit = {},
    invitationRepository: InvitationRepository = get()
) {
    var connectedUsers by remember { mutableStateOf<List<ConnectedUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var userType by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Determine user type and load data
    LaunchedEffect(carerId) {
        scope.launch {
            try {
                // Check if user is a carer (has carees)
                val carerRelationships = invitationRepository.getCarerRelationships(carerId).getOrNull() ?: emptyList()
                // Check if user is a caree (has carers)
                val careeRelationships = invitationRepository.getCareeRelationships(carerId).getOrNull() ?: emptyList()
                
                when {
                    carerRelationships.isNotEmpty() -> {
                        userType = "carer"
                        connectedUsers = carerRelationships.map { relationship ->
                            ConnectedUser(
                                id = relationship.careeId,
                                name = relationship.careeName,
                                role = "Care Recipient",
                                lastActivity = "Active today",
                                status = "Online"
                            )
                        }
                    }
                    careeRelationships.isNotEmpty() -> {
                        userType = "caree"
                        connectedUsers = careeRelationships.map { relationship ->
                            ConnectedUser(
                                id = relationship.carerId,
                                name = relationship.carerName,
                                role = "Carer",
                                lastActivity = "Available",
                                status = "Online"
                            )
                        }
                    }
                    else -> {
                        userType = "carer" // Default to carer if no relationships
                        connectedUsers = emptyList()
                    }
                }
            } catch (e: Exception) {
                println("DashboardScreen: Error loading relationships: ${e.message}")
                userType = "carer" // Default fallback
            } finally {
                isLoading = false
            }
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        when (userType) {
            "carer" -> CarerDashboard(
                currentUser = currentUser,
                connectedUsers = connectedUsers,
                onNavigateToInvitation = onNavigateToInvitation
            )
            "caree" -> CareeDashboard(
                currentUser = currentUser,
                connectedUsers = connectedUsers
            )
            else -> CarerDashboard(
                currentUser = currentUser,
                connectedUsers = connectedUsers,
                onNavigateToInvitation = onNavigateToInvitation
            )
        }
    }
}

@Composable
private fun CarerDashboard(
    currentUser: com.carecomms.data.models.SimpleUser?,
    connectedUsers: List<ConnectedUser>,
    onNavigateToInvitation: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${currentUser?.name?.split(" ")?.firstOrNull() ?: "Carer"}!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )
                    Text(
                        text = "Care Dashboard",
                        fontSize = 16.sp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Button(
                    onClick = onNavigateToInvitation,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Invite")
                }
            }
        }
        
        // Quick Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Care Recipients",
                    value = connectedUsers.size.toString(),
                    icon = Icons.Default.Person,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Messages Today",
                    value = "12",
                    icon = Icons.Default.Email,
                    color = MaterialTheme.colors.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Connected Care Recipients
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your Care Recipients",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (connectedUsers.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No care recipients yet",
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Tap 'Invite' to connect with someone",
                                fontSize = 12.sp,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    } else {
                        connectedUsers.forEach { user ->
                            ConnectedUserItem(user = user)
                            if (user != connectedUsers.last()) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }
        
        // Recent Activity
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recent Activity",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val activities = listOf(
                        "New message from John - 2 hours ago",
                        "Medication reminder sent - 4 hours ago",
                        "Weekly check-in completed - 1 day ago",
                        "Emergency contact updated - 2 days ago"
                    )
                    
                    activities.forEach { activity ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(8.dp),
                                tint = MaterialTheme.colors.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activity,
                                fontSize = 14.sp,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CareeDashboard(
    currentUser: com.carecomms.data.models.SimpleUser?,
    connectedUsers: List<ConnectedUser>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Hello, ${currentUser?.name?.split(" ")?.firstOrNull() ?: "Friend"}!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
                Text(
                    text = "How are you feeling today?",
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        // Daily Mood Check
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Daily Mood Check",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val moods = listOf("😊", "😐", "😔", "😴", "🤒")
                        moods.forEach { mood ->
                            Card(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { /* Handle mood selection */ },
                                elevation = 2.dp
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = mood,
                                        fontSize = 24.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Your Carers
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your Care Team",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (connectedUsers.isEmpty()) {
                        Text(
                            text = "No carers connected yet",
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        connectedUsers.forEach { user ->
                            ConnectedUserItem(user = user)
                            if (user != connectedUsers.last()) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }
        
        // Daily Activities Suggestions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Wellness Suggestions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val suggestions = listOf(
                        WellnessSuggestion("💧", "Drink Water", "Stay hydrated - aim for 8 glasses today"),
                        WellnessSuggestion("🚶", "Take a Walk", "A 10-minute walk can boost your mood"),
                        WellnessSuggestion("📞", "Call a Friend", "Social connection is important for wellbeing"),
                        WellnessSuggestion("🧘", "Practice Mindfulness", "Try 5 minutes of deep breathing"),
                        WellnessSuggestion("📚", "Read Something", "Engage your mind with a good book or article")
                    )
                    
                    suggestions.forEach { suggestion ->
                        SuggestionItem(suggestion = suggestion)
                        if (suggestion != suggestions.last()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
        
        // Quick Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Quick Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.Phone,
                            label = "Emergency",
                            color = MaterialTheme.colors.error,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Handle emergency call
                        }
                        
                        QuickActionButton(
                            icon = Icons.Default.List,
                            label = "Medication",
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Handle medication reminder
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ConnectedUserItem(user: ConnectedUser) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colors.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colors.primary
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.name,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = user.role,
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Green)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = user.status,
                    fontSize = 12.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = user.lastActivity,
                fontSize = 10.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun SuggestionItem(suggestion: WellnessSuggestion) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle suggestion click */ }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = suggestion.emoji,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = suggestion.title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Text(
                text = suggestion.description,
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Data classes
data class ConnectedUser(
    val id: String,
    val name: String,
    val role: String,
    val lastActivity: String,
    val status: String
)

data class WellnessSuggestion(
    val emoji: String,
    val title: String,
    val description: String
)