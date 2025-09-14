package com.carecomms.android.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.carecomms.android.ui.theme.CareCommsTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(5000) // 5 second maximum duration as per requirements
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Placeholder for app logo - using a simple colored box for now
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        Color.White,
                        shape = MaterialTheme.shapes.large
                    )
            ) {
                // TODO: Replace with actual app logo
                // Image(
                //     painter = painterResource(id = R.drawable.app_logo),
                //     contentDescription = "CareComms Logo",
                //     modifier = Modifier.fillMaxSize()
                // )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App name could be added here if needed
            // Text(
            //     text = "CareComms",
            //     style = MaterialTheme.typography.h2,
            //     color = Color.White
            // )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    CareCommsTheme {
        SplashScreen(onSplashComplete = {})
    }
}