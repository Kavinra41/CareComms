package com.carecomms.android.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.carecomms.accessibility.AccessibilityUtils
import com.carecomms.accessibility.AndroidAccessibilityPreferences

/**
 * Accessible icon button with proper touch targets and content descriptions
 */
@Composable
fun AccessibleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val accessibilityPreferences = AndroidAccessibilityPreferences(context)
    val accessibilitySettings by accessibilityPreferences.observeAccessibilitySettings().collectAsState()
    
    val iconSize = AccessibilityUtils.getTouchTargetSize(
        baseSize = 24f,
        isLargeTouchTargetsEnabled = accessibilitySettings.isLargeTouchTargetsEnabled
    ).dp
    
    val buttonSize = AccessibilityUtils.getTouchTargetSize(
        baseSize = 48f,
        isLargeTouchTargetsEnabled = accessibilitySettings.isLargeTouchTargetsEnabled
    ).dp
    
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(buttonSize)
            .semantics {
                this.contentDescription = contentDescription
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Content description is set on the button
            modifier = Modifier.size(iconSize),
            tint = if (enabled) MaterialTheme.colors.onSurface else MaterialTheme.colors.onSurface.copy(alpha = 0.38f)
        )
    }
}