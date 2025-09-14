package com.carecomms.android.ui.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.carecomms.accessibility.AccessibilityConstants
import com.carecomms.accessibility.AccessibilityUtils
import com.carecomms.accessibility.AndroidAccessibilityPreferences

/**
 * Accessible button component that follows accessibility guidelines
 * - Minimum touch target size of 44dp
 * - Proper content descriptions
 * - High contrast support
 */
@Composable
fun AccessibleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val accessibilityPreferences = AndroidAccessibilityPreferences(context)
    val accessibilitySettings by accessibilityPreferences.observeAccessibilitySettings().collectAsState()
    
    val minSize = AccessibilityUtils.getTouchTargetSize(
        baseSize = 40f,
        isLargeTouchTargetsEnabled = accessibilitySettings.isLargeTouchTargetsEnabled
    ).dp
    
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(minWidth = minSize, minHeight = minSize)
            .padding(4.dp)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary,
            disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.38f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.button
        )
    }
}