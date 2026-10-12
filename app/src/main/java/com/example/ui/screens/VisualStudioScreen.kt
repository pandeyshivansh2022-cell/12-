package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VisualCreation
import com.example.ui.GangstreamViewModel
import com.example.ui.WorkspaceUiState
import com.example.ui.components.NeonGLogo
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlowing
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonElectricBlue
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

val stylePresets = listOf(
    "Neon Cyberpunk Cybercity" to "Futuristic cyberpunk megacity with intense electric blue and neon cyan holographic lights, rain reflections, cinematic 8k unreal engine render",
    "Holographic Blueprint" to "Glowing cyan holographic wireframe blueprint of an advanced cybernetic AI neural core, floating in dark void, high detail technical HUD",
    "Studio Dark Mech" to "Hyper-detailed sleek obsidian battle mech with glowing blue reactor lines, studio lighting, dramatic octane render",
    "Synthwave Grid" to "Retro-futuristic synthwave landscape with glowing neon cyan vector grid, digital mountains, and chrome horizons"
)

@Composable
fun VisualStudioScreen(
    viewModel: GangstreamViewModel,
    uiState: WorkspaceUiState,
    modifier: Modifier = Modifier
) {
    val creations by viewModel.visualCreations.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Visual Studio Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                NeonGLogo(size = 36.dp, showGlow = true)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "CYBER VISUAL STUDIO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Image generation (Gemini 3.1 Flash Image) & Veo Video Studio",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Selector: Image vs Veo Video
            TabRow(
                selectedTabIndex = if (uiState.visualType == "IMAGE") 0 else 1,
                containerColor = CyberSurfaceDark,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[if (uiState.visualType == "IMAGE") 0 else 1]),
                        color = NeonCyan
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = uiState.visualType == "IMAGE",
                    onClick = { viewModel.setVisualType("IMAGE") },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gemini 3.1 Image", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
                Tab(
                    selected = uiState.visualType == "VIDEO",
                    onClick = { viewModel.setVisualType("VIDEO") },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Veo 3.1 Video", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Aspect Ratio Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ASPECT RATIO:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryDark
                )

                val ratios = if (uiState.visualType == "VIDEO") listOf("16:9", "9:16") else listOf("1:1", "16:9", "9:16", "4:3")
                ratios.forEach { ratio ->
                    val isSelected = uiState.visualAspectRatio == ratio
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CyberSurfaceDark)
                            .border(1.dp, if (isSelected) NeonCyan else CyberBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.setVisualAspectRatio(ratio) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = ratio,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NeonCyan else TextSecondaryDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Style Prompt Presets
            Text(
                text = "CYBER PROMPT PRESETS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NeonElectricBlue,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stylePresets) { (name, prompt) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberSurfaceElevated)
                            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                            .clickable { viewModel.setVisualPrompt(prompt) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prompt Input Field
            OutlinedTextField(
                value = uiState.visualPrompt,
                onValueChange = { viewModel.setVisualPrompt(it) },
                label = { Text("Prompt description", fontSize = 12.sp, color = TextMutedDark) },
                placeholder = {
                    Text(
                        if (uiState.visualType == "IMAGE") "Describe image to generate..."
                        else "Describe video animation for Veo 3.1...",
                        fontSize = 13.sp,
                        color = TextMutedDark
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("visual_prompt_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CyberBorder,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark,
                    focusedContainerColor = CyberSurfaceDark,
                    unfocusedContainerColor = CyberSurfaceDark
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Generate Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (uiState.visualPrompt.isNotBlank() && !uiState.isGeneratingVisual)
                            Brush.linearGradient(listOf(NeonCyan, NeonElectricBlue))
                        else Brush.linearGradient(listOf(CyberSurfaceElevated, CyberSurfaceDark))
                    )
                    .border(
                        1.dp,
                        if (uiState.visualPrompt.isNotBlank()) NeonCyan else CyberBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = uiState.visualPrompt.isNotBlank() && !uiState.isGeneratingVisual) {
                        viewModel.generateVisual()
                    }
                    .testTag("generate_visual_button"),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isGeneratingVisual) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ObsidianBlack, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SYNTHESIZING VISUAL MATRIX...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = ObsidianBlack
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (uiState.visualPrompt.isNotBlank()) ObsidianBlack else TextMutedDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.visualType == "IMAGE") "GENERATE IMAGE" else "RENDER VEO VIDEO",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = if (uiState.visualPrompt.isNotBlank()) ObsidianBlack else TextMutedDark
                        )
                    }
                }
            }

            if (uiState.visualError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ ${uiState.visualError}",
                    fontSize = 11.sp,
                    color = Color(0xFFEF4444)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "GENERATED GALLERY (${creations.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = NeonCyan
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Gallery Items
        if (creations.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberSurfaceDark)
                        .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = TextMutedDark,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No visual assets generated yet.",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                    }
                }
            }
        } else {
            items(creations, key = { it.id }) { item ->
                VisualCreationCard(
                    item = item,
                    onDelete = { viewModel.deleteVisual(item.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun VisualCreationCard(
    item: VisualCreation,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberSurfaceDark)
            .border(1.dp, CyberBorderGlowing.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            // Header Row: Type Badge + Model + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (item.type == "IMAGE") NeonCyan.copy(alpha = 0.15f) else NeonIndigo.copy(alpha = 0.2f))
                            .border(1.dp, if (item.type == "IMAGE") NeonCyan else NeonIndigo, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${item.type} [${item.aspectRatio}]",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.type == "IMAGE") NeonCyan else NeonIndigo
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.modelUsed.replace("-preview", ""),
                        fontSize = 10.sp,
                        color = TextMutedDark
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete item",
                        tint = TextMutedDark,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Display Image Bitmap or Video Placeholder
            if (item.type == "IMAGE" && item.mediaUrlOrBase64.isNotEmpty()) {
                val bitmap = remember(item.mediaUrlOrBase64) {
                    try {
                        val decodedBytes = Base64.decode(item.mediaUrlOrBase64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    } catch (e: Exception) {
                        null
                    }
                }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = item.prompt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            } else {
                // Video simulation preview card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF1E1B4B))
                            )
                        )
                        .border(1.dp, NeonIndigo.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Veo 3.1 Video Stream Ready",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Aspect: ${item.aspectRatio} // 720p",
                            fontSize = 10.sp,
                            color = TextSecondaryDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.prompt,
                fontSize = 12.sp,
                color = TextSecondaryDark,
                lineHeight = 16.sp
            )
        }
    }
}
