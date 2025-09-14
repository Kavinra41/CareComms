package com.carecomms.android.ui.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
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
 * Accessible text field with proper touch targets and semantic labels
 */
@Composable
fun AccessibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    val accessibilityPreferences = AndroidAccessibilityPreferences(context)
    val accessibilitySettings by accessibilityPreferences.observeAccessibilitySettings().collectAsState()
    
    val minHeight = AccessibilityUtils.getTouchTargetSize(
        baseSize = 56f,
        isLargeTouchTargetsEnabled = accessibilitySettings.isLargeTouchTargetsEnabled
    ).dp
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = isError,
        enabled = enabled,
        singleLine = singleLine,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight)
            .padding(vertical = 4.dp)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = MaterialTheme.colors.primary,
            unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
            errorBorderColor = MaterialTheme.colors.error,
            focusedLabelColor = MaterialTheme.colors.primary,
            unfocusedLabelColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            errorLabelColor = MaterialTheme.colors.error
        ),
        textStyle = MaterialTheme.typography.body1
    )
    
    // Error message display
    if (isError && errorMessage != null) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colors.error,
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp)
                .semantics {
                    contentDescription = "Error: $errorMessage"
                }
        )
    }
}