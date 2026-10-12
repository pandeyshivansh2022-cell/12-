package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.ChatMessage
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlowing
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonElectricBlue
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonRed
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CyberMessageBubble(
    message: ChatMessage,
    isPlayingAudio: Boolean,
    onPlayAudioClick: () -> Unit,
    onStopAudioClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReactClick: (String) -> Unit = {},
    onPromptClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isUser = message.role == "user"
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag(if (isUser) "user_message_bubble" else "gangstream_message_bubble"),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // Gangstream Avatar for model
        if (!isUser) {
            NeonGLogo(
                size = 32.dp,
                showGlow = false,
                animated = false,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 330.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Main Message Card
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (isUser) {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F2B48),
                                    Color(0xFF081C33)
                                )
                            )
                        } else if (message.isError) {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF3B1111),
                                    Color(0xFF220A0A)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    CyberSurfaceElevated,
                                    CyberSurfaceDark
                                )
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        brush = if (isUser) {
                            Brush.linearGradient(
                                colors = listOf(NeonSkyBlue.copy(alpha = 0.4f), Color.Transparent)
                            )
                        } else if (message.isError) {
                            Brush.linearGradient(
                                colors = listOf(NeonRed.copy(alpha = 0.6f), Color.Transparent)
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(NeonCyan.copy(alpha = 0.3f), NeonIndigo.copy(alpha = 0.2f))
                            )
                        },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column {
                    // Role header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isUser) "YOU" else "GANGSTREAM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = if (isUser) NeonSkyBlue else NeonCyan
                        )

                        if (!isUser && message.modelUsed != null) {
                            Text(
                                text = message.modelUsed.replace("-preview", ""),
                                fontSize = 9.sp,
                                color = TextMutedDark,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Attached Image if present
                    if (message.imageUri != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Image(
                            painter = rememberAsyncImagePainter(model = message.imageUri),
                            contentDescription = "Attached Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    // Thinking trace collapsible card if present
                    if (!message.thoughtText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ThinkingProcessCard(
                            thoughtText = message.thoughtText,
                            initialExpanded = false
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Text Body
                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = if (message.isError) NeonRed else TextPrimaryDark
                    )

                    // Search Grounding Citations if present
                    if (!message.citations.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SearchGroundingCard(citationsJson = message.citations)
                    }
                }
            }

            // Interactive Micro-Reactions & Natural Actions for Assistant Messages
            if (!isUser && !message.isError) {
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp, start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val reactions = listOf("❤️", "💡", "⚡", "🤝")
                    reactions.forEach { emoji ->
                        val isSelected = message.userReaction == emoji
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.25f) else CyberSurfaceDark)
                                .border(
                                    width = if (isSelected) 1.dp else 0.5.dp,
                                    color = if (isSelected) NeonCyan else CyberBorder.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable { onReactClick(emoji) }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Bottom Action Bar: TTS Audio Playback, Copy, Timestamp, Delete
            Row(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = TextMutedDark
                )

                // Voice TTS button for Gangstream model messages
                if (!isUser && !message.isError) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isPlayingAudio) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable {
                                if (isPlayingAudio) onStopAudioClick() else onPlayAudioClick()
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPlayingAudio) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isPlayingAudio) "Stop Audio" else "Play Voice TTS",
                            tint = if (isPlayingAudio) NeonCyan else TextSecondaryDark,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isPlayingAudio) "PLAYING" else "TTS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlayingAudio) NeonCyan else TextSecondaryDark
                        )
                    }
                }

                // Copy Text Button
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Gangstream Message", message.text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy message",
                        tint = TextMutedDark,
                        modifier = Modifier.size(12.dp)
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete message",
                        tint = TextMutedDark,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // User Avatar
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0C243B))
                    .border(1.dp, NeonSkyBlue.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = NeonSkyBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

val NeonSkyBlue = Color(0xFF38BDF8)
