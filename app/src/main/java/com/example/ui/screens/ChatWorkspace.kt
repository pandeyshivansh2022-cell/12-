package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.ChatMessage
import com.example.ui.GangstreamViewModel
import com.example.ui.WorkspaceUiState
import com.example.ui.components.CyberMessageBubble
import com.example.ui.components.NeonGLogo
import com.example.ui.components.QuickPromptChips
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderGlowing
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonElectricBlue
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonSkyBlue
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun ChatWorkspace(
    viewModel: GangstreamViewModel,
    uiState: WorkspaceUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val messages by viewModel.currentMessages.collectAsState()
    val isPlayingAudio by viewModel.audioPlayerManager.isPlaying.collectAsState()
    val playingMessageId by viewModel.audioPlayerManager.currentlyPlayingMessageId.collectAsState()
    val listState = rememberLazyListState()

    // Permission launcher for speech recognition
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceRecording()
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                }
                viewModel.setSelectedImage(bitmap)
            } catch (e: Exception) {
                // Handle bitmap decode failure
            }
        }
    }

    // Scroll to bottom on new message
    LaunchedEffect(messages.size, uiState.isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .imePadding()
            .testTag("chat_workspace")
    ) {
        // Connected Persona Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurfaceDark.copy(alpha = 0.7f))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Companion: ${uiState.selectedPersona.displayName}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonCyan
                )
            }

            Text(
                text = if (uiState.isThinkingMode) "3.1 Pro Thinking" else uiState.selectedModel.replace("-preview", ""),
                fontSize = 10.sp,
                color = TextSecondaryDark
            )
        }

        // Chat Thread Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty() && !uiState.isGenerating) {
                // Empty state Hero Graphic
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    NeonGLogo(
                        size = 72.dp,
                        showGlow = true,
                        animated = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "GANGSTREAM AI BOT",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = TextPrimaryDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "A connected, emotionally perceptive intelligence designed for deep conversations, lightning solutions, and creative synergy.",
                        fontSize = 13.sp,
                        color = TextSecondaryDark,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mode Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonIndigo.copy(alpha = 0.15f))
                                .border(1.dp, NeonIndigo.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = NeonIndigo,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "3.1 Pro Thinking",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonIndigo
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Live Search Grounding",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        CyberMessageBubble(
                            message = message,
                            isPlayingAudio = isPlayingAudio && playingMessageId == message.id,
                            onPlayAudioClick = { viewModel.playMessageAudio(message) },
                            onStopAudioClick = { viewModel.stopAudio() },
                            onDeleteClick = { viewModel.deleteMessage(message.id) },
                            onReactClick = { emoji -> viewModel.reactToMessage(message.id, emoji) },
                            onPromptClick = { prompt -> viewModel.sendMessage(prompt) }
                        )
                    }

                    // Neural Generating Wave indicator
                    if (uiState.isGenerating) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NeonGLogo(
                                    size = 28.dp,
                                    showGlow = true,
                                    animated = true
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = NeonCyan,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (uiState.isThinkingMode) "Gangstream is synthesizing high cognition thoughts..." else "Gangstream is reflecting...",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = NeonCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Voice Recording Banner
        AnimatedVisibility(
            visible = uiState.isRecordingVoice,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonCyan.copy(alpha = 0.15f))
                    .border(1.dp, NeonCyan, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF43F5E))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.inputText.isNotEmpty()) "Transcribing: \"${uiState.inputText}\"" else "Listening... Speak naturally to Gangstream",
                        fontSize = 12.sp,
                        color = TextPrimaryDark,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = { viewModel.stopVoiceRecording() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop voice recording",
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Quick Prompt Suggestions
        QuickPromptChips(
            onPromptSelected = { prompt ->
                viewModel.setInputText(prompt)
                viewModel.sendMessage(prompt)
            }
        )

        // Bottom Input Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurfaceDark)
                .border(1.dp, CyberBorder.copy(alpha = 0.5f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Selected Image Preview if attached
            if (uiState.selectedImageBitmap != null) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, NeonCyan, RoundedCornerShape(8.dp))
                ) {
                    Image(
                        bitmap = uiState.selectedImageBitmap.asImageBitmap(),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = { viewModel.setSelectedImage(null) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .background(ObsidianBlack.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach Media Button (Android Photo Picker)
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberSurfaceElevated)
                        .border(1.dp, CyberBorder, CircleShape)
                        .testTag("attach_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Attach image for vision",
                        tint = if (uiState.selectedImageBitmap != null) NeonCyan else TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Voice Recording Button
                IconButton(
                    onClick = {
                        val hasAudioPerm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasAudioPerm) {
                            viewModel.startVoiceRecording()
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isRecordingVoice) NeonCyan.copy(alpha = 0.2f) else CyberSurfaceElevated)
                        .border(1.dp, if (uiState.isRecordingVoice) NeonCyan else CyberBorder, CircleShape)
                        .testTag("voice_record_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isRecordingVoice) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (uiState.isRecordingVoice) Color(0xFFF43F5E) else TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Text Field with neon cyber border
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(CyberSurfaceElevated)
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    if (uiState.isThinkingMode) NeonIndigo.copy(alpha = 0.5f) else NeonCyan.copy(alpha = 0.3f),
                                    CyberBorder
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    TextField(
                        value = uiState.inputText,
                        onValueChange = { viewModel.setInputText(it) },
                        placeholder = {
                            Text(
                                text = if (uiState.isThinkingMode) "Ask deep reasoning query..." else "Ask or speak to Gangstream...",
                                fontSize = 13.sp,
                                color = TextMutedDark
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            cursorColor = NeonCyan
                        ),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (uiState.inputText.isNotBlank() || uiState.selectedImageBitmap != null) {
                                    focusManager.clearFocus()
                                    viewModel.sendMessage()
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_input_textfield")
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send Button with neon gradient
                val canSend = uiState.inputText.isNotBlank() || uiState.selectedImageBitmap != null
                IconButton(
                    onClick = {
                        if (canSend && !uiState.isGenerating) {
                            focusManager.clearFocus()
                            viewModel.sendMessage()
                        }
                    },
                    enabled = canSend && !uiState.isGenerating,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (canSend) Brush.linearGradient(listOf(NeonCyan, NeonElectricBlue))
                            else Brush.linearGradient(listOf(CyberSurfaceElevated, CyberSurfaceDark))
                        )
                        .border(
                            1.dp,
                            if (canSend) NeonCyan else CyberBorder,
                            CircleShape
                        )
                        .testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send transmission",
                        tint = if (canSend) ObsidianBlack else TextMutedDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
