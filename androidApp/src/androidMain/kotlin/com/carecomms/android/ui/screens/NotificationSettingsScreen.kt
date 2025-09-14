package com.carecomms.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carecomms.presentation.notification.NotificationState
import com.carecomms.presentation.notification.NotificationViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.initializeNotifications()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF6B46C1)
                )
            }
            
            Text(
                text = "Notification Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B46C1),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Permission Status Card
            NotificationPermissionCard(
                state = state,
                onRequestPermission = { viewModel.requestNotificationPermission() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (state.permissionGranted) {
                // Notification Type Settings
                NotificationTypeSettings(
                    state = state,
                    onToggleMessages = viewModel::toggleMessageNotifications,
                    onToggleInvitations = viewModel::toggleInvitationNotifications,
                    onToggleSystem = viewModel::toggleSystemNotifications
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sound and Vibration Settings
                SoundVibrationSettings(
                    state = state,
                    onToggleSound = viewModel::toggleSound,
                    onToggleVibration = viewModel::toggleVibration
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quiet Hours Settings
                QuietHoursSettings(
                    state = state,
                    onToggleQuietHours = viewModel::toggleQuietHours,
                    onUpdateQuietHours = viewModel::updateQuietHours
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Clear Notifications Button
                Button(
                    onClick = { viewModel.clearAllNotifications() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFE5E7EB)
                    )
                ) {
                    Text(
                        text = "Clear All Notifications",
                        color = Color(0xFF374151)
                    )
                }
            }
        }
        
        // Error Display
        state.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFFFEE2E2),
                elevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { viewModel.clearError() }
                    ) {
                        Text("Dismiss", color = Color(0xFFDC2626))
                    }
                }
            }
        }
        
        // Loading Indicator
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF6B46C1)
                )
            }
        }
    }
}

@Composable
private fun NotificationPermissionCard(
    state: NotificationState,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = if (state.permissionGranted) Color(0xFFECFDF5) else Color(0xFFFEF3C7)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (state.permissionGranted) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = if (state.permissionGranted) Color(0xFF059669) else Color(0xFFD97706)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = if (state.permissionGranted) "Notifications Enabled" else "Notifications Disabled",
                        fontWeight = FontWeight.Bold,
                        color = if (state.permissionGranted) Color(0xFF059669) else Color(0xFFD97706)
                    )
                    
                    Text(
                        text = if (state.permissionGranted) 
                            "You'll receive notifications for messages and updates" 
                        else 
                            "Enable notifications to stay updated",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
            
            if (!state.permissionGranted) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF6B46C1)
                    )
                ) {
                    Text("Enable Notifications", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun NotificationTypeSettings(
    state: NotificationState,
    onToggleMessages: (Boolean) -> Unit,
    onToggleInvitations: (Boolean) -> Unit,
    onToggleSystem: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Notification Types",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF374151)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SwitchRow(
                title = "Message Notifications",
                subtitle = "Get notified when you receive new messages",
                checked = state.preferences.messageNotifications,
                onCheckedChange = onToggleMessages
            )
            
            SwitchRow(
                title = "Invitation Notifications",
                subtitle = "Get notified about care invitations",
                checked = state.preferences.invitationNotifications,
                onCheckedChange = onToggleInvitations
            )
            
            SwitchRow(
                title = "System Notifications",
                subtitle = "Get notified about app updates and system messages",
                checked = state.preferences.systemNotifications,
                onCheckedChange = onToggleSystem
            )
        }
    }
}

@Composable
private fun SoundVibrationSettings(
    state: NotificationState,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Sound & Vibration",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF374151)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SwitchRow(
                title = "Sound",
                subtitle = "Play sound for notifications",
                checked = state.preferences.soundEnabled,
                onCheckedChange = onToggleSound
            )
            
            SwitchRow(
                title = "Vibration",
                subtitle = "Vibrate for notifications",
                checked = state.preferences.vibrationEnabled,
                onCheckedChange = onToggleVibration
            )
        }
    }
}

@Composable
private fun QuietHoursSettings(
    state: NotificationState,
    onToggleQuietHours: (Boolean) -> Unit,
    onUpdateQuietHours: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quiet Hours",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF374151)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SwitchRow(
                title = "Enable Quiet Hours",
                subtitle = "Silence notifications during specified hours",
                checked = state.preferences.quietHoursEnabled,
                onCheckedChange = onToggleQuietHours
            )
            
            if (state.preferences.quietHoursEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Start Time",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                        Text(
                            text = state.preferences.quietHoursStart,
                            fontSize = 16.sp,
                            color = Color(0xFF374151)
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "End Time",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                        Text(
                            text = state.preferences.quietHoursEnd,
                            fontSize = 16.sp,
                            color = Color(0xFF374151)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color(0xFF374151)
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF6B46C1),
                checkedTrackColor = Color(0xFFDDD6FE)
            )
        )
    }
}