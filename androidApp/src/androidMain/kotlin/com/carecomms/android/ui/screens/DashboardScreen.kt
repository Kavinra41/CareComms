package com.carecomms.android.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Description
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carecomms.data.models.AnalyticsPeriod
import com.carecomms.presentation.analytics.AnalyticsAction
import com.carecomms.presentation.analytics.AnalyticsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreen(
    carerId: String,
    modifier: Modifier = Modifier,
    viewModel: AnalyticsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Initialize view model with carer ID
    LaunchedEffect(carerId) {
        viewModel.handleAction(AnalyticsAction.LoadCarees)
    }
    
    val periods = listOf(
        "Daily" to AnalyticsPeriod.DAILY,
        "Weekly" to AnalyticsPeriod.WEEKLY,
        "Bi-weekly" to AnalyticsPeriod.BIWEEKLY
    )
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Text(
                text = "Data Dashboard",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        item {
            // Period Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Time Period",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        periods.forEach { (periodName, periodEnum) ->
                            FilterChip(
                                selected = state.selectedPeriod == periodEnum,
                                onClick = { 
                                    viewModel.handleAction(AnalyticsAction.ChangePeriod(periodEnum))
                                },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = periodName,
                                    style = MaterialTheme.typography.body2,
                                    fontWeight = if (state.selectedPeriod == periodEnum) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            // Caree Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Care Recipients",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colors.primary
                        )
                        
                        if (state.availableCarees.size > 1) {
                            Row {
                                TextButton(
                                    onClick = { viewModel.handleAction(AnalyticsAction.SelectAllCarees) }
                                ) {
                                    Text("All", style = MaterialTheme.typography.caption)
                                }
                                TextButton(
                                    onClick = { viewModel.handleAction(AnalyticsAction.DeselectAllCarees) }
                                ) {
                                    Text("None", style = MaterialTheme.typography.caption)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (state.isLoadingCarees) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (state.availableCarees.isEmpty()) {
                        Text(
                            text = "No care recipients found",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        state.availableCarees.forEach { caree ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = caree.id in state.selectedCareeIds,
                                        onClick = {
                                            if (caree.id in state.selectedCareeIds) {
                                                viewModel.handleAction(AnalyticsAction.DeselectCaree(caree.id))
                                            } else {
                                                viewModel.handleAction(AnalyticsAction.SelectCaree(caree.id))
                                            }
                                        }
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = caree.id in state.selectedCareeIds,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            viewModel.handleAction(AnalyticsAction.SelectCaree(caree.id))
                                        } else {
                                            viewModel.handleAction(AnalyticsAction.DeselectCaree(caree.id))
                                        }
                                    }
                                )
                                
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    tint = MaterialTheme.colors.primary
                                )
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = caree.name,
                                        style = MaterialTheme.typography.body1,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Age: ${caree.age}",
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        item {
            // Analytics Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Analytics Overview",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colors.primary
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        if (state.selectedCareeIds.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.handleAction(AnalyticsAction.RefreshData) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh data",
                                    tint = MaterialTheme.colors.primary
                                )
                            }
                        }
                    }
                    
                    if (state.selectedCareeIds.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Select care recipients to view analytics",
                                    style = MaterialTheme.typography.body1,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else if (state.isLoadingAnalytics) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Loading analytics...",
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        state.analyticsData?.let { data ->
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Display analytics based on selected period
                                when (state.selectedPeriod) {
                                    AnalyticsPeriod.DAILY -> {
                                        if (data.dailyData.isNotEmpty()) {
                                            val latest = data.dailyData.last()
                                            AnalyticsCard(
                                                title = "Daily Activity Level",
                                                value = "${latest.activityLevel}/10",
                                                trend = "Today",
                                                isPositive = latest.activityLevel >= 7
                                            )
                                            AnalyticsCard(
                                                title = "Daily Communications",
                                                value = "${latest.communicationCount} messages",
                                                trend = latest.date,
                                                isPositive = latest.communicationCount > 0
                                            )
                                        }
                                    }
                                    AnalyticsPeriod.WEEKLY -> {
                                        if (data.weeklyData.isNotEmpty()) {
                                            val latest = data.weeklyData.last()
                                            AnalyticsCard(
                                                title = "Weekly Avg Activity",
                                                value = "${latest.averageActivityLevel}/10",
                                                trend = "${latest.weekStart} - ${latest.weekEnd}",
                                                isPositive = latest.averageActivityLevel >= 7
                                            )
                                            AnalyticsCard(
                                                title = "Weekly Communications",
                                                value = "${latest.totalCommunications} total",
                                                trend = "This week",
                                                isPositive = latest.totalCommunications > 0
                                            )
                                        }
                                    }
                                    AnalyticsPeriod.BIWEEKLY -> {
                                        if (data.biweeklyData.isNotEmpty()) {
                                            val latest = data.biweeklyData.last()
                                            AnalyticsCard(
                                                title = "Bi-weekly Avg Activity",
                                                value = "${latest.averageActivityLevel}/10",
                                                trend = "${latest.periodStart} - ${latest.periodEnd}",
                                                isPositive = latest.averageActivityLevel >= 7
                                            )
                                            AnalyticsCard(
                                                title = "Bi-weekly Communications",
                                                value = "${latest.totalCommunications} total",
                                                trend = "This period",
                                                isPositive = latest.totalCommunications > 0
                                            )
                                        }
                                    }
                                    else -> {
                                        // Default fallback
                                        AnalyticsCard(
                                            title = "Communication Frequency",
                                            value = "12 messages/day",
                                            trend = "+15%",
                                            isPositive = true
                                        )
                                    }
                                }
                                
                                // Chart visualization placeholder
                                ChartVisualizationPlaceholder(
                                    period = state.selectedPeriod,
                                    data = data
                                )
                            }
                        } ?: run {
                            // Fallback mock data
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AnalyticsCard(
                                    title = "Communication Frequency",
                                    value = "12 messages/day",
                                    trend = "+15%",
                                    isPositive = true
                                )
                                
                                AnalyticsCard(
                                    title = "Response Time",
                                    value = "2.3 minutes",
                                    trend = "-8%",
                                    isPositive = true
                                )
                                
                                AnalyticsCard(
                                    title = "Activity Level",
                                    value = "High",
                                    trend = "Stable",
                                    isPositive = true
                                )
                                
                                ChartVisualizationPlaceholder(
                                    period = state.selectedPeriod,
                                    data = null
                                )
                            }
                        }
                    }
                    
                    // Error display
                    state.error?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colors.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.error,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.handleAction(AnalyticsAction.ClearError) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear error",
                                        tint = MaterialTheme.colors.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        item {
            // Notes Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Recent Notes",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colors.primary
                        )
                    }
                    
                    if (state.selectedCareeIds.isEmpty()) {
                        Text(
                            text = "Select care recipients to view notes",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        state.analyticsData?.notes?.let { notes ->
                            if (notes.isNotEmpty()) {
                                notes.take(5).forEach { note ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        elevation = 1.dp,
                                        backgroundColor = MaterialTheme.colors.surface
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp)
                                        ) {
                                            Text(
                                                text = note.content,
                                                style = MaterialTheme.typography.body2,
                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = note.type,
                                                    style = MaterialTheme.typography.caption,
                                                    color = MaterialTheme.colors.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                                                        .format(java.util.Date(note.timestamp)),
                                                    style = MaterialTheme.typography.caption,
                                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "No notes available for selected period",
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } ?: run {
                            // Fallback mock notes
                            val mockNotes = listOf(
                                "Increased communication frequency observed",
                                "Positive response to new medication routine",
                                "Regular sleep pattern maintained",
                                "Completed daily check-in successfully",
                                "Medication reminder acknowledged"
                            )
                            
                            mockNotes.forEach { note ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    elevation = 1.dp,
                                    backgroundColor = MaterialTheme.colors.surface
                                ) {
                                    Text(
                                        text = "• $note",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsCard(
    title: String,
    value: String,
    trend: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface
                )
            }
            
            Text(
                text = trend,
                style = MaterialTheme.typography.body2,
                fontWeight = FontWeight.Medium,
                color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}

@Composable
private fun ChartVisualizationPlaceholder(
    period: AnalyticsPeriod,
    data: com.carecomms.data.models.AnalyticsData?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (period) {
                        AnalyticsPeriod.DAILY -> "Daily Activity Chart"
                        AnalyticsPeriod.WEEKLY -> "Weekly Trends Chart"
                        AnalyticsPeriod.BIWEEKLY -> "Bi-weekly Overview Chart"
                        else -> "Activity Chart"
                    },
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colors.onSurface
                )
            }
            
            // Mock chart visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.05f))
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawMockChart(period, data)
                }
                
                // Chart overlay text
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Chart visualization",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Coming soon",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            // Chart legend/info
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChartLegendItem(
                    color = MaterialTheme.colors.primary,
                    label = "Activity",
                    modifier = Modifier.weight(1f)
                )
                ChartLegendItem(
                    color = MaterialTheme.colors.secondary,
                    label = "Communication",
                    modifier = Modifier.weight(1f)
                )
                ChartLegendItem(
                    color = Color(0xFF4CAF50),
                    label = "Trends",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ChartLegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
    }
}

private fun DrawScope.drawMockChart(
    period: AnalyticsPeriod,
    data: com.carecomms.data.models.AnalyticsData?
) {
    val primaryColor = Color(0xFF6200EE)
    val secondaryColor = Color(0xFF03DAC6)
    
    // Draw mock line chart
    val points = when (period) {
        AnalyticsPeriod.DAILY -> 7
        AnalyticsPeriod.WEEKLY -> 4
        AnalyticsPeriod.BIWEEKLY -> 2
        else -> 7
    }
    
    val width = size.width
    val height = size.height
    val stepX = width / (points - 1)
    
    // Generate mock data points
    val mockValues = (0 until points).map { 
        (0.3f + Math.random() * 0.4f).toFloat() 
    }
    
    // Draw activity line
    val activityPath = Path()
    mockValues.forEachIndexed { index, value ->
        val x = index * stepX
        val y = height * (1f - value)
        if (index == 0) {
            activityPath.moveTo(x, y)
        } else {
            activityPath.lineTo(x, y)
        }
    }
    
    drawPath(
        path = activityPath,
        color = primaryColor.copy(alpha = 0.3f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
    )
    
    // Draw communication bars (simplified)
    mockValues.forEachIndexed { index, value ->
        val x = index * stepX
        val barHeight = height * value * 0.6f
        drawRect(
            color = secondaryColor.copy(alpha = 0.2f),
            topLeft = Offset(x - 8.dp.toPx(), height - barHeight),
            size = androidx.compose.ui.geometry.Size(16.dp.toPx(), barHeight)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
        contentColor = if (selected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface,
        onClick = onClick,
        elevation = if (selected) 4.dp else 1.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}