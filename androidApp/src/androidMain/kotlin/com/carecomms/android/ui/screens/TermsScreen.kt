package com.carecomms.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TermsScreen(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Terms and Conditions",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "CareComms Terms of Service",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = """
                        Welcome to CareComms. By using our service, you agree to these terms.
                        
                        1. Service Description
                        CareComms is a communication platform designed to connect care providers with care recipients.
                        
                        2. Privacy and Data Protection
                        We take your privacy seriously and comply with all applicable data protection laws.
                        
                        3. User Responsibilities
                        Users must provide accurate information and use the service responsibly.
                        
                        4. Medical Disclaimer
                        CareComms is not a substitute for professional medical advice, diagnosis, or treatment.
                        
                        5. Limitation of Liability
                        CareComms is provided "as is" without warranties of any kind.
                        
                        6. Changes to Terms
                        We may update these terms from time to time. Continued use constitutes acceptance.
                        
                        For full terms and conditions, please visit our website.
                    """.trimIndent(),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onDecline,
                modifier = Modifier.weight(1f)
            ) {
                Text("Decline")
            }
            
            Button(
                onClick = onAccept,
                modifier = Modifier.weight(1f)
            ) {
                Text("Accept")
            }
        }
    }
}