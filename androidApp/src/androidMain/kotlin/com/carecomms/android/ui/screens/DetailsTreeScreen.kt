package com.carecomms.android.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carecomms.presentation.details.DetailsTreeViewModel
import org.koin.androidx.compose.koinViewModel

data class TreeNode(
    val id: String,
    val title: String,
    val type: NodeType,
    val icon: ImageVector,
    val children: List<TreeNode> = emptyList(),
    val data: String? = null
)

data class CareeInfo(
    val id: String,
    val name: String,
    val age: Int,
    val healthConditions: List<String>,
    val lastActivity: String
)

enum class NodeType {
    CAREE, CATEGORY, DETAIL, ITEM
}

@Composable
fun DetailsTreeScreen(
    carerId: String,
    modifier: Modifier = Modifier,
    viewModel: DetailsTreeViewModel = koinViewModel()
) {
    var selectedCareeId by remember { mutableStateOf<String?>(null) }
    var expandedNodes by remember { mutableStateOf(setOf<String>()) }
    
    // Mock caree data for tile display
    val mockCarees = remember {
        listOf(
            CareeInfo(
                id = "caree-1",
                name = "Alice Johnson",
                age = 78,
                healthConditions = listOf("Diabetes", "Hypertension"),
                lastActivity = "2 hours ago"
            ),
            CareeInfo(
                id = "caree-2", 
                name = "Bob Smith",
                age = 82,
                healthConditions = listOf("Arthritis"),
                lastActivity = "1 day ago"
            ),
            CareeInfo(
                id = "caree-3",
                name = "Eleanor Davis",
                age = 75,
                healthConditions = listOf("Heart condition"),
                lastActivity = "3 hours ago"
            ),
            CareeInfo(
                id = "caree-4",
                name = "Margaret Wilson",
                age = 80,
                healthConditions = listOf("Osteoporosis"),
                lastActivity = "5 hours ago"
            )
        )
    }
    
    // Mock detailed tree data for selected caree
    val getTreeDataForCaree = { careeId: String ->
        listOf(
            TreeNode(
                id = "${careeId}_health",
                title = "Health Information",
                type = NodeType.CATEGORY,
                icon = Icons.Default.LocalHospital,
                children = listOf(
                    TreeNode(
                        id = "${careeId}_medications",
                        title = "Medications",
                        type = NodeType.DETAIL,
                        icon = Icons.Default.Medication,
                        children = listOf(
                            TreeNode(
                                id = "${careeId}_med_1",
                                title = "Blood Pressure Medication",
                                type = NodeType.ITEM,
                                icon = Icons.Default.Circle,
                                data = "Taken daily at 8 AM, 10mg dosage"
                            ),
                            TreeNode(
                                id = "${careeId}_med_2",
                                title = "Diabetes Medication",
                                type = NodeType.ITEM,
                                icon = Icons.Default.Circle,
                                data = "Taken twice daily with meals"
                            )
                        )
                    ),
                    TreeNode(
                        id = "${careeId}_vitals",
                        title = "Vital Signs",
                        type = NodeType.DETAIL,
                        icon = Icons.Default.MonitorHeart,
                        children = listOf(
                            TreeNode(
                                id = "${careeId}_bp",
                                title = "Blood Pressure",
                                type = NodeType.ITEM,
                                icon = Icons.Default.Circle,
                                data = "Average: 120/80 mmHg"
                            ),
                            TreeNode(
                                id = "${careeId}_hr",
                                title = "Heart Rate",
                                type = NodeType.ITEM,
                                icon = Icons.Default.Circle,
                                data = "Average: 72 BPM"
                            )
                        )
                    )
                )
            ),
            TreeNode(
                id = "${careeId}_activities",
                title = "Daily Activities",
                type = NodeType.CATEGORY,
                icon = Icons.Default.DirectionsWalk,
                children = listOf(
                    TreeNode(
                        id = "${careeId}_exercise",
                        title = "Exercise Routine",
                        type = NodeType.DETAIL,
                        icon = Icons.Default.FitnessCenter,
                        children = listOf(
                            TreeNode(
                                id = "${careeId}_walk",
                                title = "Morning Walk",
                                type = NodeType.ITEM,
                                icon = Icons.Default.Circle,
                                data = "30 minutes daily, 7 AM"
                            )
                        )
                    ),
                    TreeNode(
                        id = "${careeId}_meals",
                        title = "Meal Schedule",
                        type = NodeType.DETAIL,
                        icon = Icons.Default.Restaurant,
                        children = listOf(
                            TreeNode(
                                id = "${careeId}_breakfast",
                                title = "Breakfast",
                                type = NodeType.ITEM,
                                icon = Icons.Default.Circle,
                                data = "8:00 AM - Balanced diet"
                            ),
                            TreeNode(
                                id = "${careeId}_lunch",
                                title = "Lunch",
                                type = NodeType.ITEM,
                                icon = Icons.Default.Circle,
                                data = "12:30 PM - Light meal"
                            )
                        )
                    )
                )
            ),
            TreeNode(
                id = "${careeId}_communication",
                title = "Communication History",
                type = NodeType.CATEGORY,
                icon = Icons.Default.Chat,
                children = listOf(
                    TreeNode(
                        id = "${careeId}_recent_messages",
                        title = "Recent Messages",
                        type = NodeType.DETAIL,
                        icon = Icons.Default.Message,
                        children = listOf(
                            TreeNode(
                                id = "${careeId}_msg_today",
                                title = "Today: 5 messages",
                                type = NodeType.ITEM,
                                icon = Icons.Default.Circle,
                                data = "Last message: 2 hours ago"
                            ),
                            TreeNode(
                                id = "${careeId}_msg_yesterday",
                                title = "Yesterday: 3 messages",
                                type = NodeType.ITEM,
                                icon = Icons.Default.Circle,
                                data = "Good morning check-in"
                            )
                        )
                    )
                )
            ),
            TreeNode(
                id = "${careeId}_notes",
                title = "Care Notes",
                type = NodeType.CATEGORY,
                icon = Icons.Default.Note,
                children = listOf(
                    TreeNode(
                        id = "${careeId}_care_notes",
                        title = "Care Observations",
                        type = NodeType.DETAIL,
                        icon = Icons.Default.EditNote,
                        children = listOf(
                            TreeNode(
                                id = "${careeId}_note_latest",
                                title = "Latest Note",
                                type = NodeType.ITEM,
                                icon = Icons.Default.Circle,
                                data = "Patient is doing well, good spirits today"
                            ),
                            TreeNode(
                                id = "${careeId}_note_previous",
                                title = "Previous Note",
                                type = NodeType.ITEM,
                                icon = Icons.Default.Circle,
                                data = "Completed all daily activities on schedule"
                            )
                        )
                    )
                )
            )
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Details Tree",
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = if (selectedCareeId == null) {
                "Select a care recipient to explore their detailed information in an organized structure."
            } else {
                "Exploring details for ${mockCarees.find { it.id == selectedCareeId }?.name ?: "Unknown"}"
            },
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Back button when viewing details
        if (selectedCareeId != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        selectedCareeId = null
                        expandedNodes = emptySet()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to caree selection",
                        tint = MaterialTheme.colors.primary
                    )
                }
                Text(
                    text = "Back to Care Recipients",
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.clickable {
                        selectedCareeId = null
                        expandedNodes = emptySet()
                    }
                )
            }
        }
        
        if (selectedCareeId == null) {
            // Tile-style caree selection
            CareeSelectionGrid(
                carees = mockCarees,
                onCareeSelected = { careeId ->
                    selectedCareeId = careeId
                    expandedNodes = emptySet()
                }
            )
        } else {
            // Accordion-style details tree
            val treeData = getTreeDataForCaree(selectedCareeId!!)
            DetailsTreeView(
                treeNodes = treeData,
                expandedNodes = expandedNodes,
                onToggleExpanded = { nodeId ->
                    expandedNodes = if (expandedNodes.contains(nodeId)) {
                        expandedNodes - nodeId
                    } else {
                        expandedNodes + nodeId
                    }
                }
            )
        }
        
        // Footer note
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            elevation = 2.dp,
            shape = RoundedCornerShape(12.dp),
            backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)
        ) {
            Text(
                text = "📋 This is mock data for demonstration. Real data integration will be implemented in future updates.",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun CareeSelectionGrid(
    carees: List<CareeInfo>,
    onCareeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(carees) { caree ->
            CareeTile(
                caree = caree,
                onClick = { onCareeSelected(caree.id) }
            )
        }
    }
}

@Composable
private fun CareeTile(
    caree: CareeInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        elevation = 8.dp,
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colors.primary,
                            MaterialTheme.colors.primary.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header with icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    // Health conditions count
                    if (caree.healthConditions.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            backgroundColor = Color.White.copy(alpha = 0.2f),
                            elevation = 0.dp
                        ) {
                            Text(
                                text = "${caree.healthConditions.size}",
                                style = MaterialTheme.typography.caption,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                // Caree information
                Column {
                    Text(
                        text = caree.name,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Text(
                        text = "Age ${caree.age}",
                        style = MaterialTheme.typography.body2,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    
                    if (caree.healthConditions.isNotEmpty()) {
                        Text(
                            text = caree.healthConditions.take(2).joinToString(", "),
                            style = MaterialTheme.typography.caption,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    Text(
                        text = "Last activity: ${caree.lastActivity}",
                        style = MaterialTheme.typography.caption,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun DetailsTreeView(
    treeNodes: List<TreeNode>,
    expandedNodes: Set<String>,
    onToggleExpanded: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(treeNodes) { node ->
            TreeNodeItem(
                node = node,
                level = 0,
                expandedNodes = expandedNodes,
                onToggleExpanded = onToggleExpanded
            )
        }
    }
}

@Composable
private fun TreeNodeItem(
    node: TreeNode,
    level: Int,
    expandedNodes: Set<String>,
    onToggleExpanded: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpanded = expandedNodes.contains(node.id)
    val hasChildren = node.children.isNotEmpty()
    
    // Animation for expansion
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )
    
    Column(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = hasChildren) {
                    if (hasChildren) {
                        onToggleExpanded(node.id)
                    }
                },
            elevation = when (node.type) {
                NodeType.CAREE -> 6.dp
                NodeType.CATEGORY -> 4.dp
                NodeType.DETAIL -> 2.dp
                NodeType.ITEM -> 1.dp
            },
            shape = RoundedCornerShape(
                when (node.type) {
                    NodeType.CAREE -> 16.dp
                    NodeType.CATEGORY -> 12.dp
                    NodeType.DETAIL -> 8.dp
                    NodeType.ITEM -> 6.dp
                }
            ),
            backgroundColor = when (node.type) {
                NodeType.CAREE -> MaterialTheme.colors.primary
                NodeType.CATEGORY -> MaterialTheme.colors.primary.copy(alpha = 0.8f)
                NodeType.DETAIL -> MaterialTheme.colors.primary.copy(alpha = 0.6f)
                NodeType.ITEM -> MaterialTheme.colors.surface
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = (16 + level * 24).dp,
                        top = 12.dp,
                        end = 16.dp,
                        bottom = 12.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = node.icon,
                    contentDescription = null,
                    tint = when (node.type) {
                        NodeType.CAREE, NodeType.CATEGORY, NodeType.DETAIL -> Color.White
                        NodeType.ITEM -> MaterialTheme.colors.primary
                    },
                    modifier = Modifier.size(
                        when (node.type) {
                            NodeType.CAREE -> 28.dp
                            NodeType.CATEGORY -> 24.dp
                            NodeType.DETAIL -> 20.dp
                            NodeType.ITEM -> 16.dp
                        }
                    )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = node.title,
                        style = when (node.type) {
                            NodeType.CAREE -> MaterialTheme.typography.h6
                            NodeType.CATEGORY -> MaterialTheme.typography.subtitle1
                            NodeType.DETAIL -> MaterialTheme.typography.subtitle2
                            NodeType.ITEM -> MaterialTheme.typography.body2
                        },
                        fontWeight = when (node.type) {
                            NodeType.CAREE -> FontWeight.Bold
                            NodeType.CATEGORY -> FontWeight.SemiBold
                            NodeType.DETAIL -> FontWeight.Medium
                            NodeType.ITEM -> FontWeight.Normal
                        },
                        color = when (node.type) {
                            NodeType.CAREE, NodeType.CATEGORY, NodeType.DETAIL -> Color.White
                            NodeType.ITEM -> MaterialTheme.colors.onSurface
                        }
                    )
                    
                    node.data?.let { data ->
                        Text(
                            text = data,
                            style = MaterialTheme.typography.caption,
                            color = when (node.type) {
                                NodeType.CAREE, NodeType.CATEGORY, NodeType.DETAIL -> Color.White.copy(alpha = 0.8f)
                                NodeType.ITEM -> MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            },
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                
                if (hasChildren) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = when (node.type) {
                            NodeType.CAREE, NodeType.CATEGORY, NodeType.DETAIL -> Color.White
                            NodeType.ITEM -> MaterialTheme.colors.primary
                        },
                        modifier = Modifier.graphicsLayer {
                            rotationZ = rotationAngle
                        }
                    )
                }
            }
        }
        
        // Animated expansion of children with smooth accordion effect
        AnimatedVisibility(
            visible = isExpanded && hasChildren,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 300, delayMillis = 100)
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 200)
            )
        ) {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                node.children.forEach { child ->
                    TreeNodeItem(
                        node = child,
                        level = level + 1,
                        expandedNodes = expandedNodes,
                        onToggleExpanded = onToggleExpanded
                    )
                }
            }
        }
    }
}