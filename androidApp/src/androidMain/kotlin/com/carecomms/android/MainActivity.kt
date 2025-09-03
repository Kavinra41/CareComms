package com.carecomms.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CareCommsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Text("CareComms App - Setup Complete")
                }
            }
        }
    }
}

@Composable
fun CareCommsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content
    )
}

@Preview
@Composable
fun DefaultPreview() {
    CareCommsTheme {
        Text("Hello, CareComms!")
    }
}