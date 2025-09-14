package com.carecomms.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carecomms.android.ui.theme.CareCommsTheme

@Composable
fun TermsScreen(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var hasScrolledToBottom by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Check if user has scrolled to the bottom
    LaunchedEffect(scrollState.value, scrollState.maxValue) {
        hasScrolledToBottom = scrollState.value >= scrollState.maxValue - 50
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Terms and Conditions",
            style = MaterialTheme.typography.h4,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Text(
                    text = buildTermsAndConditionsText(),
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Justify,
                    lineHeight = MaterialTheme.typography.body1.lineHeight
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onDecline,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp), // Large touch target for accessibility
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colors.primary
                )
            ) {
                Text(
                    text = "Decline",
                    style = MaterialTheme.typography.button
                )
            }

            Button(
                onClick = onAccept,
                enabled = hasScrolledToBottom,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp), // Large touch target for accessibility
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                    disabledBackgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "Accept",
                    style = MaterialTheme.typography.button
                )
            }
        }

        if (!hasScrolledToBottom) {
            Text(
                text = "Please scroll to the bottom to continue",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

private fun buildTermsAndConditionsText(): String {
    return """
        Welcome to CareComms
        
        By using this application, you agree to the following terms and conditions:
        
        1. PURPOSE AND SCOPE
        CareComms is designed to facilitate communication and care coordination between professional carers and their care recipients (carees). This application is intended for use by healthcare professionals and individuals receiving care services.
        
        2. USER RESPONSIBILITIES
        - Carers must provide accurate professional credentials and documentation
        - All users must maintain the confidentiality of their login credentials
        - Users are responsible for the accuracy of information they provide
        - Professional carers must comply with applicable healthcare regulations
        
        3. PRIVACY AND DATA PROTECTION
        - We collect and process personal and health information in accordance with applicable privacy laws
        - Health information is encrypted and stored securely
        - Data is only shared between authorized carer-caree pairs
        - Users may request deletion of their data at any time
        
        4. COMMUNICATION GUIDELINES
        - All communications should be professional and appropriate
        - Emergency situations should not rely solely on this application
        - Users should follow established care protocols and procedures
        
        5. TECHNICAL REQUIREMENTS
        - Users are responsible for maintaining compatible devices and internet connectivity
        - The application requires certain permissions to function properly
        - Regular updates may be required for security and functionality
        
        6. LIMITATION OF LIABILITY
        - This application is a communication tool and does not replace professional medical judgment
        - Users are responsible for following appropriate care protocols
        - We are not liable for decisions made based on information exchanged through the application
        
        7. ACCOUNT TERMINATION
        - Accounts may be terminated for violation of these terms
        - Users may delete their accounts at any time
        - Data retention policies apply after account deletion
        
        8. UPDATES TO TERMS
        - These terms may be updated periodically
        - Users will be notified of significant changes
        - Continued use constitutes acceptance of updated terms
        
        9. CONTACT INFORMATION
        For questions about these terms or the application, please contact our support team.
        
        By accepting these terms, you acknowledge that you have read, understood, and agree to be bound by these conditions.
        
        Last updated: ${java.text.SimpleDateFormat("MMMM dd, yyyy").format(java.util.Date())}
    """.trimIndent()
}

@Preview(showBackground = true)
@Composable
fun TermsScreenPreview() {
    CareCommsTheme {
        TermsScreen(
            onAccept = {},
            onDecline = {}
        )
    }
}