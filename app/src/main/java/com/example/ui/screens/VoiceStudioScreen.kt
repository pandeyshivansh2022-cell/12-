package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GangstreamViewModel
import com.example.ui.WorkspaceUiState
import com.example.ui.components.NeonGLogo
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlowing
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
import kotlin.math.sin

val availableVoices = listOf(
    "Kore" to "Balanced, articulate cybernetic voice",
    "Fenrir" to "Deep, resonant authoritative tone",
    "Puck" to "Agile, enthusiastic rapid speech",
    "Aoede" to "Smooth, melodious calm cadence"
)

@Composable
fun VoiceStudioScreen(
    viewModel: GangstreamViewModel,
    uiState: WorkspaceUiState,
    modifier: Modifier = Modifier
) {
    val isPlaying by viewModel.audioPlayerManager.isPlaying.collectAsState()
    var speechInputText by remember {
        mutableStateOf("Gangstream AI is fully initialized and operational. Neural audio channels synchronized.")
    }
    var isSynthesizing by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            NeonGLogo(size = 36.dp, showGlow = true)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "GANGSTREAM VOICE LAB",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    color = TextPrimaryDark
                )
                Text(
                    text = "Neural Text-To-Speech (gemini-3.1-flash-tts-preview) & Voice AI",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Futuristic Audio Visualizer Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CyberSurfaceDark)
                .border(1.dp, if (isPlaying) NeonCyan else CyberBorder, RoundedCornerShape(16.dp))
                .testTag("audio_visualizer_box"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val w = size.width
                val h = size.height
                val midY = h / 2f
                val barCount = 32
                val barWidth = w / (barCount * 1.5f)

                for (i in 0 until barCount) {
                    val x = i * (barWidth * 1.5f)
                    val amp = if (isPlaying) {
                        (sin(i * 0.4f + wavePhase) * 0.5f + 0.5f) * (h * 0.4f) + 10f
                    } else {
                        10f + (sin(i * 0.3f) * 4f)
                    }

                    val color = if (isPlaying) {
                        if (i % 2 == 0) NeonCyan else NeonElectricBlue
                    } else {
                        CyberBorderGlowing.copy(alpha = 0.3f)
                    }

                    drawLine(
                        color = color,
                        start = Offset(x, midY - amp),
                        end = Offset(x, midY + amp),
                        strokeWidth = barWidth
                    )
                }
            }

            if (!isPlaying) {
                Text(
                    text = "VOICE ENGINE READY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextMutedDark
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Voice Persona Chooser
        Text(
            text = "SELECT NEURAL VOICE MODEL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            letterSpacing = 0.8.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableVoices.forEach { (voice, desc) ->
                val isSelected = uiState.selectedVoiceName == voice
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CyberSurfaceElevated)
                        .border(1.dp, if (isSelected) NeonCyan else CyberBorder, RoundedCornerShape(10.dp))
                        .clickable { viewModel.selectVoice(voice) }
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = voice,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NeonCyan else TextPrimaryDark
                        )
                        Text(
                            text = desc.take(15) + "...",
                            fontSize = 9.sp,
                            color = TextSecondaryDark,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Text input to speak
        OutlinedTextField(
            value = speechInputText,
            onValueChange = { speechInputText = it },
            label = { Text("Enter script for speech synthesis", fontSize = 12.sp, color = TextMutedDark) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tts_text_input"),
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

        Spacer(modifier = Modifier.height(16.dp))

        // Speak / Stop Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isPlaying) Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF991B1B)))
                    else Brush.linearGradient(listOf(NeonCyan, NeonElectricBlue))
                )
                .border(
                    1.dp,
                    if (isPlaying) Color(0xFFEF4444) else NeonCyan,
                    RoundedCornerShape(12.dp)
                )
                .clickable {
                    if (isPlaying) {
                        viewModel.stopAudio()
                    } else {
                        viewModel.audioPlayerManager.speakWithAndroidTts("studio_tts", speechInputText)
                    }
                }
                .testTag("synthesize_voice_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = ObsidianBlack,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPlaying) "STOP AUDIO STREAM" else "SYNTHESIZE SPEECH (TTS)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = ObsidianBlack
                )
            }
        }
    }
}
