package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BotPersona
import com.example.ui.WorkspaceUiState
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlowing
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonElectricBlue
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectorSheet(
    uiState: WorkspaceUiState,
    onPersonaSelected: (BotPersona) -> Unit,
    onModelSelected: (String) -> Unit,
    onToggleThinking: () -> Unit,
    onToggleSearch: () -> Unit,
    onToggleCustomBackend: () -> Unit = {},
    onCustomBackendUrlChanged: (String) -> Unit = {},
    onTestBackendConnection: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianBlack,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(CyberBorderGlowing.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("model_selector_sheet")
        ) {
            Text(
                text = "GANGSTREAM BOT PERSONAS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = NeonCyan
            )

            Text(
                text = "Select neural architecture and specialized agent directives.",
                fontSize = 12.sp,
                color = TextSecondaryDark,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Personas List
            BotPersona.values().forEach { persona ->
                val isSelected = uiState.selectedPersona == persona
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CyberSurfaceElevated else CyberSurfaceDark)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) NeonCyan else CyberBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onPersonaSelected(persona) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        val icon = when (persona) {
                            BotPersona.SOUL -> Icons.Default.Favorite
                            BotPersona.CORE -> Icons.Default.SmartToy
                            BotPersona.MUSE -> Icons.Default.AutoAwesome
                            BotPersona.THINKER -> Icons.Default.Psychology
                            BotPersona.CYBER_CODER -> Icons.Default.Terminal
                            BotPersona.CREATIVE_NEON -> Icons.Default.Palette
                            BotPersona.LIGHTNING -> Icons.Default.Bolt
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CyberSurfaceCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) NeonCyan else TextSecondaryDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = persona.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) NeonCyan else TextPrimaryDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = persona.defaultModel.replace("-preview", ""),
                                    fontSize = 9.sp,
                                    color = TextMutedDark
                                )
                            }
                            Text(
                                text = persona.tagLine,
                                fontSize = 11.sp,
                                color = TextSecondaryDark,
                                maxLines = 1
                            )
                        }
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "NEURAL COGNITION CONTROLS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = NeonElectricBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Thinking Mode Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberSurfaceDark)
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "High Thinking Mode (3.1 Pro)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Activates ThinkingLevel.HIGH for maximum reasoning depth and step-by-step logic.",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                }
                Switch(
                    checked = uiState.isThinkingMode,
                    onCheckedChange = { onToggleThinking() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = NeonIndigo,
                        uncheckedThumbColor = TextMutedDark,
                        uncheckedTrackColor = CyberSurfaceElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Google Search Grounding Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberSurfaceDark)
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Google Search Grounding (3.5 Flash)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Feeds real-time live search intelligence and citations into responses.",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                }
                Switch(
                    checked = uiState.isSearchGrounding,
                    onCheckedChange = { onToggleSearch() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBlack,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = TextMutedDark,
                        uncheckedTrackColor = CyberSurfaceElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Express / Dialogflow Backend Section
            Text(
                text = "EXPRESS & DIALOGFLOW BACKEND",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = com.example.ui.theme.NeonGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberSurfaceDark)
                    .border(1.dp, if (uiState.useCustomBackend) com.example.ui.theme.NeonGreen.copy(alpha = 0.6f) else CyberBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = if (uiState.useCustomBackend) com.example.ui.theme.NeonGreen else TextSecondaryDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Use Express Backend (/api/chat)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }
                        Text(
                            text = "Routes chat transmissions through your local/remote Express + Dialogflow ES server.",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                    Switch(
                        checked = uiState.useCustomBackend,
                        onCheckedChange = { onToggleCustomBackend() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBlack,
                            checkedTrackColor = com.example.ui.theme.NeonGreen,
                            uncheckedThumbColor = TextMutedDark,
                            uncheckedTrackColor = CyberSurfaceElevated
                        )
                    )
                }

                if (uiState.useCustomBackend) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.customBackendUrl,
                        onValueChange = onCustomBackendUrlChanged,
                        label = { Text("Server Base URL", fontSize = 11.sp) },
                        placeholder = { Text("http://10.0.2.2:3000 or http://localhost:3000", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedBorderColor = com.example.ui.theme.NeonGreen,
                            unfocusedBorderColor = CyberBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = onTestBackendConnection,
                            enabled = !uiState.isCheckingBackend,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberSurfaceElevated,
                                contentColor = com.example.ui.theme.NeonGreen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, com.example.ui.theme.NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            if (uiState.isCheckingBackend) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = com.example.ui.theme.NeonGreen
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Checking /health...", fontSize = 11.sp)
                            } else {
                                Text("Test Connection (/health)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (uiState.backendStatus != null) {
                            val isOnline = uiState.backendStatus.startsWith("Online")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (isOnline) com.example.ui.theme.NeonGreen else com.example.ui.theme.NeonRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.backendStatus,
                                    fontSize = 10.sp,
                                    color = if (isOnline) com.example.ui.theme.NeonGreen else com.example.ui.theme.NeonRed,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
