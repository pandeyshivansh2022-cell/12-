package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.GangstreamViewModel
import com.example.ui.screens.WorkspaceScreen
import com.example.ui.theme.GangstreamTheme
import com.example.ui.theme.ObsidianBlack

class MainActivity : ComponentActivity() {
    private val viewModel: GangstreamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GangstreamTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianBlack
                ) {
                    WorkspaceScreen(viewModel = viewModel)
                }
            }
        }
    }
}
