package com.interview.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.interview.app.presentation.navigation.AppNavHost
import com.interview.app.presentation.theme.InterviewAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /**
     * Initializes the activity: enables edge-to-edge rendering and sets the Jetpack Compose UI using InterviewAppTheme with AppNavHost as the root navigation container.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InterviewAppTheme {
                AppNavHost()
            }
        }
    }
}
