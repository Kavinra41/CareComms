package com.carecomms.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.carecomms.accessibility.AccessibilityConstants
import com.carecomms.accessibility.AndroidAccessibilityPreferences
import com.carecomms.android.ui.components.AccessibleButton
import com.carecomms.android.ui.components.AccessibleIconButton
import kotlinx.coroutines.launch

/**
 * Accessibility settings screen for configuring user preferences
 */
@Composable
fun AccessibilitySettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val accessibilityPreferences = remember { AndroidAccessibilityPreferences(context) }
    val accessibilitySettings by accessibilityPreferences.observeAccessibilitySettings().collectAsState()
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top app bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccessibleIconButton(
                icon = Icons.Default.ArrowBack,
                contentDescription = AccessibilityConstants.ContentDescriptions.BACK_BUTTON,
                onClick = onNavigateBack
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Accessibility Settings",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.semantics {
                    contentDescription = "Accessibility Settings Screen"
                }
            )
        }
        
        // Text Scale Setting
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Text Size",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Adjust text size for better readability",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Current size: ${(accessibilitySettings.textScale * 100).toInt()}%",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Slider(
                    value = accessibilitySettings.textScale,
                    onValueChange = { scale ->
                        scope.launch {
                            accessibilityPreferences.updateTextScale(scale)
                        }
                    },
                    valueRange = AccessibilityConstants.MIN_TEXT_SCALE..AccessibilityConstants.MAX_TEXT_SCALE,
                    steps = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Text size slider, current value ${(accessibilitySettings.textScale * 100).toInt()} percent"
                        }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Small", style = MaterialTheme.typography.caption)
                    Text("Large", style = MaterialTheme.typography.caption)
                }
            }
        }
        
        // High Contrast Setting
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "High Contrast",
                        style = MaterialTheme.typography.h6
                    )
                    Text(
                        text = "Increase contrast for better visibility",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = accessibilitySettings.isHighContrastEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            accessibilityPreferences.updateHighContrast(enabled)
                        }
                    },
                    modifier = Modifier.semantics {
                        contentDescription = if (accessibilitySettings.isHighContrastEnabled) {
                            "High contrast enabled, tap to disable"
                        } else {
                            "High contrast disabled, tap to enable"
                        }
                    }
                )
            }
        }
        
        // Large Touch Targets Setting
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Large Touch Targets",
                        style = MaterialTheme.typography.h6
                    )
                    Text(
                        text = "Make buttons and interactive elements larger",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = accessibilitySettings.isLargeTouchTargetsEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            accessibilityPreferences.updateLargeTouchTargets(enabled)
                        }
                    },
                    modifier = Modifier.semantics {
                        contentDescription = if (accessibilitySettings.isLargeTouchTargetsEnabled) {
                            "Large touch targets enabled, tap to disable"
                        } else {
                            "Large touch targets disabled, tap to enable"
                        }
                    }
                )
            }
        }
        
        // Reduced Motion Setting
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reduce Motion",
                        style = MaterialTheme.typography.h6
                    )
                    Text(
                        text = "Minimize animations and transitions",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = accessibilitySettings.isReducedMotionEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            accessibilityPreferences.updateReducedMotion(enabled)
                        }
                    },
                    modifier = Modifier.semantics {
                        contentDescription = if (accessibilitySettings.isReducedMotionEnabled) {
                            "Reduced motion enabled, tap to disable"
                        } else {
                            "Reduced motion disabled, tap to enable"
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Reset to defaults button
        AccessibleButton(
            text = "Reset to Defaults",
            onClick = {
                scope.launch {
                    accessibilityPreferences.updateTextScale(AccessibilityConstants.DEFAULT_TEXT_SCALE)
                    accessibilityPreferences.updateHighContrast(false)
                    accessibilityPreferences.updateLargeTouchTargets(true)
                    accessibilityPreferences.updateReducedMotion(false)
                }
            },
            contentDescription = "Reset all accessibility settings to default values",
            modifier = Modifier.fillMaxWidth()
        )
    }
}