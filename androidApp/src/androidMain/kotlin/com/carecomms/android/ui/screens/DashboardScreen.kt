package com.carecomms.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(
    carerId: String
) {
    var analyticsData by remember { mutableStateOf(generateMockAnalytics()) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Dashboard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary
            )
        }
        
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
                            text = "Care Overview",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Active Care Recipients: ${analyticsData.activeCareRecipients}")
                    Text("Messages Today: ${analyticsData.messagesToday}")
                    Text("Alerts: ${analyticsData.alerts}")
                }
            }
        }
        
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
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recent Activity",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column {
                        analyticsData.recentActivities.forEach { activity ->
                            Text(
                                text = "• $activity",
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class MockAnalyticsData(
    val activeCareRecipients: Int,
    val messagesToday: Int,
    val alerts: Int,
    val recentActivities: List<String>
)

private fun generateMockAnalytics() = MockAnalyticsData(
    activeCareRecipients = 3,
    messagesToday = 12,
    alerts = 1,
    recentActivities = listOf(
        "New message from John",
        "Medication reminder sent",
        "Weekly check-in completed",
        "Emergency contact updated"
    )
)