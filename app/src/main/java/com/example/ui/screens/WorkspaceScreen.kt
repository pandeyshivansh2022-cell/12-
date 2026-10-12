package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GangstreamViewModel
import com.example.ui.WorkspaceTab
import com.example.ui.components.GangstreamHeader
import com.example.ui.components.ModelSelectorSheet
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonElectricBlue
import com.example.ui.theme.GradientStartPurple
import com.example.ui.theme.GradientEndPurple
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun WorkspaceScreen(
    viewModel: GangstreamViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showModelSelector by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(GradientStartPurple, GradientEndPurple)
                )
            ),
        containerColor = Color.Transparent,
        topBar = {
            GangstreamHeader(
                uiState = uiState,
                onNewChatClick = { viewModel.createNewSession() },
                onOpenSessionsClick = { viewModel.selectTab(WorkspaceTab.SESSIONS) },
                onToggleThinkingClick = { viewModel.toggleThinkingMode() },
                onToggleSearchClick = { viewModel.toggleSearchGrounding() },
                onOpenModelSelectorClick = { showModelSelector = true }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurfaceDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorder.copy(alpha = 0.5f), RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .testTag("gangstream_bottom_nav"),
                tonalElevation = 8.dp
            ) {
                // Chat Tab
                NavigationBarItem(
                    selected = uiState.activeTab == WorkspaceTab.CHAT,
                    onClick = { viewModel.selectTab(WorkspaceTab.CHAT) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeTab == WorkspaceTab.CHAT) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Chat",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "CHAT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBlack,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondaryDark,
                        unselectedTextColor = TextMutedDark
                    )
                )

                // Visuals Tab (Image & Veo Video)
                NavigationBarItem(
                    selected = uiState.activeTab == WorkspaceTab.VISUAL_STUDIO,
                    onClick = { viewModel.selectTab(WorkspaceTab.VISUAL_STUDIO) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeTab == WorkspaceTab.VISUAL_STUDIO) Icons.Filled.Image else Icons.Outlined.Image,
                            contentDescription = "Visuals",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "VISUALS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBlack,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondaryDark,
                        unselectedTextColor = TextMutedDark
                    )
                )

                // Voice Lab Tab
                NavigationBarItem(
                    selected = uiState.activeTab == WorkspaceTab.VOICE_LAB,
                    onClick = { viewModel.selectTab(WorkspaceTab.VOICE_LAB) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeTab == WorkspaceTab.VOICE_LAB) Icons.Filled.GraphicEq else Icons.Outlined.GraphicEq,
                            contentDescription = "Voice Lab",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "VOICE LAB",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBlack,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondaryDark,
                        unselectedTextColor = TextMutedDark
                    )
                )

                // Sessions Tab
                NavigationBarItem(
                    selected = uiState.activeTab == WorkspaceTab.SESSIONS,
                    onClick = { viewModel.selectTab(WorkspaceTab.SESSIONS) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeTab == WorkspaceTab.SESSIONS) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "Sessions",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "SESSIONS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ObsidianBlack,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan,
                        unselectedIconColor = TextSecondaryDark,
                        unselectedTextColor = TextMutedDark
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Transparent)
        ) {
            when (uiState.activeTab) {
                WorkspaceTab.CHAT -> ChatWorkspace(viewModel = viewModel, uiState = uiState)
                WorkspaceTab.VISUAL_STUDIO -> VisualStudioScreen(viewModel = viewModel, uiState = uiState)
                WorkspaceTab.VOICE_LAB -> VoiceStudioScreen(viewModel = viewModel, uiState = uiState)
                WorkspaceTab.SESSIONS -> SessionsHistoryScreen(viewModel = viewModel, uiState = uiState)
            }
        }

        // Model & Persona Selector Sheet
        if (showModelSelector) {
            ModelSelectorSheet(
                uiState = uiState,
                onPersonaSelected = { persona ->
                    viewModel.selectPersona(persona)
                    showModelSelector = false
                },
                onModelSelected = { model ->
                    viewModel.selectModel(model)
                    showModelSelector = false
                },
                onToggleThinking = { viewModel.toggleThinkingMode() },
                onToggleSearch = { viewModel.toggleSearchGrounding() },
                onToggleCustomBackend = { viewModel.toggleCustomBackend() },
                onCustomBackendUrlChanged = { viewModel.setCustomBackendUrl(it) },
                onTestBackendConnection = { viewModel.testBackendConnection() },
                onDismiss = { showModelSelector = false }
            )
        }
    }
}
