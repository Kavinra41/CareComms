package com.carecomms.android.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carecomms.android.ui.theme.CareCommsTheme

@Composable
fun LandingScreen(
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo section
        Box(
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for app logo
            Card(
                modifier = Modifier.fillMaxSize(),
                backgroundColor = MaterialTheme.colors.primary,
                elevation = 8.dp
            ) {
                // TODO: Replace with actual app logo
                // Image(
                //     painter = painterResource(id = R.drawable.app_logo),
                //     contentDescription = "CareComms Logo",
                //     modifier = Modifier.fillMaxSize()
                // )
            }
        }

        // Welcome text
        Text(
            text = "Welcome to CareComms",
            style = MaterialTheme.typography.h3,
            color = MaterialTheme.colors.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Connecting carers and care recipients for better communication and care coordination",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Action buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Login button
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // Large touch target for accessibility
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary
                )
            ) {
                Text(
                    text = "Login",
                    style = MaterialTheme.typography.button
                )
            }

            // Signup button (for carers only)
            OutlinedButton(
                onClick = onSignupClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // Large touch target for accessibility
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colors.primary
                )
            ) {
                Text(
                    text = "Sign Up as Carer",
                    style = MaterialTheme.typography.button
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Information text
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "For Care Recipients",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "If you are a care recipient (caree), you will receive an invitation link from your carer to join the platform.",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LandingScreenPreview() {
    CareCommsTheme {
        LandingScreen(
            onLoginClick = {},
            onSignupClick = {}
        )
    }
}