package com.accessibilityyoutube

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.accessibilityyoutube.SharedState.playerState
import com.accessibilityyoutube.ui.theme.AccessibilityYoutubeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccessibilityYoutubeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Enable Accessibility Permission")
        }

        OutlinedTextField(
            value = SharedState.sleepBeforePlayTime.value,
            onValueChange = { SharedState.sleepBeforePlayTime.value = it },
            label = { Text("Sleep Before Play Time (s)") },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(8.dp)
        )

        OutlinedTextField(
            value = SharedState.playTime.value,
            onValueChange = { SharedState.playTime.value = it },
            label = { Text("Play Time (s)") },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(8.dp)
        )

        OutlinedTextField(
            value = SharedState.sleepAfterPlayTime.value,
            onValueChange = { SharedState.sleepAfterPlayTime.value = it },
            label = { Text("Sleep After Play Time (s)") },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(8.dp)
        )

        Button(
            onClick = {
                val url = SharedState.getRandomYoutubeUrl()
                Log.d("YoutubeAccessibility", "url: $url")
                playerState = PlayerState.IDLE
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setPackage(AppPackageNames.youtube)
                intent.setData(Uri.parse(url))
                context.startActivity(intent)
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Open YouTube")
        }
    }
}