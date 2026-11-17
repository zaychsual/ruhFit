package com.ruhraga.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ruhraga.tracker.ui.navigation.RuhRagaApp
import com.ruhraga.tracker.ui.theme.RuhRagaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RuhRagaTheme {
                RuhRagaApp()
            }
        }
    }
}
