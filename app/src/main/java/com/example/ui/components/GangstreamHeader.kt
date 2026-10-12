package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotPersona
import com.example.ui.WorkspaceTab
import com.example.ui.WorkspaceUiState
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlowing
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonSkyBlue
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GangstreamHeader(
    uiState: WorkspaceUiState,
    onNewChatClick: () -> Unit,
    onOpenSessionsClick: () -> Unit,
    onToggleThinkingClick: () -> Unit,
    onToggleSearchClick: () -> Unit,
    onOpenModelSelectorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ObsidianBlack,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .testTag("gangstream_header")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Main Top Bar: Neon G Logo + Gangstream Identity + Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Neon 'G' Logo + Brand Title + Live Pulse
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenModelSelectorClick() }
                        .padding(end = 8.dp)
                ) {
                    NeonGLogo(
                        size = 42.dp,
                        showGlow = true,
                        animated = true
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "GANGSTREAM",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                color = TextPrimaryDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonCyan.copy(alpha = 0.15f))
                                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        // Neural link status
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.isGenerating) NeonCyan else NeonGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (uiState.isGenerating) "NEURAL PROCESSING..." else "G-STREAM // ACTIVE",
                                fontSize = 10.sp,
                                color = if (uiState.isGenerating) NeonCyan else TextSecondaryDark,
                                letterSpacing = 0.8.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Right Action Buttons: Thinking Mode, Search Grounding, New Chat
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Thinking Toggle Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (uiState.isThinkingMode) NeonIndigo.copy(alpha = 0.25f)
                                else CyberSurfaceElevated
                            )
                            .border(
                                width = 1.dp,
                                color = if (uiState.isThinkingMode) NeonIndigo else CyberBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onToggleThinkingClick() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("toggle_thinking_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "High Thinking Mode",
                                tint = if (uiState.isThinkingMode) NeonIndigo else TextSecondaryDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "THINK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isThinkingMode) Color.White else TextSecondaryDark
                            )
                        }
                    }

                    // Search Grounding Toggle Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (uiState.isSearchGrounding) NeonElectricBlue.copy(alpha = 0.25f)
                                else CyberSurfaceElevated
                            )
                            .border(
                                width = 1.dp,
                                color = if (uiState.isSearchGrounding) NeonElectricBlue else CyberBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onToggleSearchClick() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("toggle_search_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Google Search Grounding",
                                tint = if (uiState.isSearchGrounding) NeonCyan else TextSecondaryDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "WEB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isSearchGrounding) NeonCyan else TextSecondaryDark
                            )
                        }
                    }

                    // New Chat Action
                    IconButton(
                        onClick = onNewChatClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberSurfaceElevated)
                            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                            .testTag("new_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Gangstream Session",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-bar showing active Bot Persona + Model Tag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurfaceDark)
                    .border(1.dp, CyberBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .clickable { onOpenModelSelectorClick() }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${uiState.selectedPersona.displayName} • ${uiState.selectedModel}",
                        fontSize = 11.sp,
                        color = TextSecondaryDark,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CONFIGURE",
                        fontSize = 10.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Configure Settings",
                        tint = NeonCyan,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
