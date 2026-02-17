package com.example.first_app_0_0_1.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MoreScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {
            Text(text = "More Options Screen", style = MaterialTheme.typography.headlineMedium)
            // Placeholder for voice interaction, will be more sophisticated later
            Button(onClick = { /* TODO: Implement voice interaction */ }) { Text("Start Voice Input") }
        }
    }
}
