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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.GangstreamViewModel
import com.example.ui.WorkspaceUiState
import com.example.ui.components.CyberMessageBubble
import com.example.ui.components.NeonGLogo
import com.example.ui.components.QuickPromptChips
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassCardBackground
import com.example.ui.theme.GlassInputBackground
import com.example.ui.theme.GlassMessagesBackground
import com.example.ui.theme.GradientEndPurple
import com.example.ui.theme.GradientStartPurple
import com.example.ui.theme.SendButtonOrange
import com.example.ui.theme.SendButtonOrangeHover

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
                // Ignore failure
            }
        }
    }

    // Scroll to bottom on new message
    LaunchedEffect(messages.size, uiState.isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Full screen outer layout with linear gradient 135deg (#667eea -> #764ba2)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(GradientStartPurple, GradientEndPurple)
                )
            )
            .imePadding()
            .testTag("chat_workspace"),
        contentAlignment = Alignment.Center
    ) {
        // Chat container with glassmorphic styling matching .chat-container in HTML
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(GlassCardBackground)
                .border(1.dp, GlassBorder, RoundedCornerShape(15.dp))
                .padding(16.dp)
                .testTag("chat_container")
        ) {
            // Chat Header matching .chat-header h1 in HTML
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonGLogo(
                    size = 28.dp,
                    showGlow = false,
                    animated = false
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gangstream AI",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            // Chat Messages area matching .chat-messages in HTML
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(GlassMessagesBackground)
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .padding(8.dp)
                    .testTag("chat_messages_area")
            ) {
                if (messages.isEmpty() && !uiState.isGenerating) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        NeonGLogo(
                            size = 54.dp,
                            showGlow = false,
                            animated = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Hello! How can I assist you today?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
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

                        // Bot typing / thinking status
                        if (uiState.isGenerating) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (uiState.isThinkingMode) "Gangstream is thinking deeply..." else "Gangstream AI is typing...",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Voice Recording Banner if recording
            AnimatedVisibility(
                visible = uiState.isRecordingVoice,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5252))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.inputText.isNotEmpty()) "Transcribing: \"${uiState.inputText}\"" else "Listening... Speak now",
                            fontSize = 12.sp,
                            color = Color.White,
                            maxLines = 1
                        )
                    }

                    IconButton(
                        onClick = { viewModel.stopVoiceRecording() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop recording",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Attached Image Thumbnail Preview
            if (uiState.selectedImageBitmap != null) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Image(
                        bitmap = uiState.selectedImageBitmap.asImageBitmap(),
                        contentDescription = "Attached visual",
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = { viewModel.setSelectedImage(null) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(18.dp)
                            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Image",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            // Quick Prompt Chips
            QuickPromptChips(
                onPromptSelected = { prompt ->
                    viewModel.setInputText(prompt)
                    viewModel.sendMessage(prompt)
                }
            )

            // Chat Input Row matching .chat-input in HTML: gap 10px, pill input, orange Send button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Media attachment (Android Photo Picker)
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .testTag("attach_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Attach image",
                        tint = if (uiState.selectedImageBitmap != null) SendButtonOrange else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

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
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isRecordingVoice) SendButtonOrange.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.15f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .testTag("voice_record_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isRecordingVoice) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (uiState.isRecordingVoice) Color(0xFFFF5252) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Input Pill matching input: flex 1, padding 10px, border-radius 20px, background rgba(255,255,255,0.2)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassInputBackground)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (uiState.inputText.isEmpty()) {
                        Text(
                            text = "Type your message...",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                    BasicTextField(
                        value = uiState.inputText,
                        onValueChange = { viewModel.setInputText(it) },
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = Color.White
                        ),
                        cursorBrush = SolidColor(Color.White),
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
                            .testTag("user_input")
                    )
                }

                // Send Button matching button: padding 10px 20px, border-radius 20px, background #FF5722, hover #E64A19
                val canSend = uiState.inputText.isNotBlank() || uiState.selectedImageBitmap != null
                val sendInteractionSource = remember { MutableInteractionSource() }
                val isSendPressed by sendInteractionSource.collectIsPressedAsState()

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (!canSend) SendButtonOrange.copy(alpha = 0.5f)
                            else if (isSendPressed) SendButtonOrangeHover
                            else SendButtonOrange
                        )
                        .clickable(
                            interactionSource = sendInteractionSource,
                            indication = null,
                            enabled = canSend && !uiState.isGenerating
                        ) {
                            if (canSend && !uiState.isGenerating) {
                                focusManager.clearFocus()
                                viewModel.sendMessage()
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Send",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
