package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GangstreamDatabase
import com.example.data.model.BotPersona
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.VisualCreation
import com.example.data.remote.AudioPlayerManager
import com.example.data.remote.SpeechToTextManager
import com.example.data.repository.GangstreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WorkspaceUiState(
    val currentSessionId: String? = null,
    val currentSession: ChatSession? = null,
    val selectedPersona: BotPersona = BotPersona.SOUL,
    val selectedModel: String = "gemini-3.5-flash",
    val isThinkingMode: Boolean = false,
    val isSearchGrounding: Boolean = false,
    val isGenerating: Boolean = false,
    val streamingChunk: String = "",
    val inputText: String = "",
    val selectedImageBitmap: Bitmap? = null,
    val activeTab: WorkspaceTab = WorkspaceTab.CHAT,
    val errorMessage: String? = null,
    // Speech-to-text
    val isRecordingVoice: Boolean = false,
    val voiceTranscriptionDraft: String = "",
    // Visual studio
    val visualPrompt: String = "",
    val visualType: String = "IMAGE", // IMAGE or VIDEO
    val visualAspectRatio: String = "1:1",
    val isGeneratingVisual: Boolean = false,
    val visualError: String? = null,
    // Voice studio
    val isListening: Boolean = false,
    val voiceSpeed: Float = 1.0f,
    val selectedVoiceName: String = "Kore"
)

enum class WorkspaceTab {
    CHAT,
    VISUAL_STUDIO,
    VOICE_LAB,
    SESSIONS
}

class GangstreamViewModel(application: Application) : AndroidViewModel(application) {
    private val database = GangstreamDatabase.getInstance(application)
    private val repository = GangstreamRepository(database.chatDao(), database.visualDao())
    val audioPlayerManager = AudioPlayerManager(application)
    val speechToTextManager = SpeechToTextManager(application)

    val sessions: StateFlow<List<ChatSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visualCreations: StateFlow<List<VisualCreation>> = repository.allVisualCreations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(WorkspaceUiState())
    val uiState: StateFlow<WorkspaceUiState> = _uiState.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentMessages: StateFlow<List<ChatMessage>> = _uiState
        .flatMapLatest { state ->
            val sessionId = state.currentSessionId
            if (sessionId != null) {
                repository.getMessagesForSession(sessionId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Observe speech recognition state
        viewModelScope.launch {
            speechToTextManager.isListening.collect { listening ->
                _uiState.value = _uiState.value.copy(isRecordingVoice = listening)
            }
        }
        viewModelScope.launch {
            speechToTextManager.partialText.collect { partial ->
                if (partial.isNotEmpty() && _uiState.value.isRecordingVoice) {
                    _uiState.value = _uiState.value.copy(
                        inputText = partial,
                        voiceTranscriptionDraft = partial
                    )
                }
            }
        }

        // Automatically open or create initial session
        viewModelScope.launch {
            repository.allSessions.collect { sessionList ->
                if (_uiState.value.currentSessionId == null) {
                    if (sessionList.isNotEmpty()) {
                        selectSession(sessionList.first())
                    } else {
                        createNewSession()
                    }
                }
            }
        }
    }

    fun selectTab(tab: WorkspaceTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun setInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun setSelectedImage(bitmap: Bitmap?) {
        _uiState.value = _uiState.value.copy(selectedImageBitmap = bitmap)
    }

    fun toggleThinkingMode() {
        val newState = !_uiState.value.isThinkingMode
        _uiState.value = _uiState.value.copy(
            isThinkingMode = newState,
            selectedModel = if (newState) "gemini-3.1-pro-preview" else "gemini-3.5-flash"
        )
        updateCurrentSessionSettings()
    }

    fun toggleSearchGrounding() {
        val newState = !_uiState.value.isSearchGrounding
        _uiState.value = _uiState.value.copy(isSearchGrounding = newState)
        updateCurrentSessionSettings()
    }

    fun selectPersona(persona: BotPersona) {
        _uiState.value = _uiState.value.copy(
            selectedPersona = persona,
            selectedModel = if (_uiState.value.isThinkingMode) "gemini-3.1-pro-preview" else persona.defaultModel
        )
    }

    fun selectModel(modelName: String) {
        val isThinking = modelName.contains("pro") && _uiState.value.isThinkingMode
        _uiState.value = _uiState.value.copy(
            selectedModel = modelName,
            isThinkingMode = isThinking
        )
        updateCurrentSessionSettings()
    }

    fun startVoiceRecording() {
        if (_uiState.value.isRecordingVoice) {
            speechToTextManager.stopListening()
        } else {
            speechToTextManager.startListening(
                onFinalResult = { recognized ->
                    _uiState.value = _uiState.value.copy(
                        inputText = recognized,
                        isRecordingVoice = false
                    )
                },
                onError = { err ->
                    _uiState.value = _uiState.value.copy(
                        isRecordingVoice = false,
                        errorMessage = err
                    )
                }
            )
        }
    }

    fun stopVoiceRecording() {
        speechToTextManager.stopListening()
        _uiState.value = _uiState.value.copy(isRecordingVoice = false)
    }

    fun reactToMessage(messageId: String, reaction: String) {
        viewModelScope.launch {
            val message = currentMessages.value.find { it.id == messageId }
            val newReaction = if (message?.userReaction == reaction) null else reaction
            repository.updateMessageReaction(messageId, newReaction)
        }
    }

    fun requestToneVariation(originalMessageText: String, instruction: String) {
        val prompt = "Could you $instruction for this: \"$originalMessageText\""
        sendMessage(prompt)
    }

    fun createNewSession() {
        viewModelScope.launch {
            val defaultPersona = BotPersona.SOUL
            val newId = repository.createNewSession(
                title = "Connected Gangstream Flow",
                personaId = defaultPersona.id,
                modelName = defaultPersona.defaultModel,
                isThinkingEnabled = false,
                isSearchGroundingEnabled = false
            )
            val session = repository.getSessionById(newId)
            _uiState.value = _uiState.value.copy(
                currentSessionId = newId,
                currentSession = session,
                selectedPersona = defaultPersona,
                selectedModel = defaultPersona.defaultModel,
                isThinkingMode = false,
                isSearchGrounding = false,
                activeTab = WorkspaceTab.CHAT
            )
        }
    }

    fun selectSession(session: ChatSession) {
        val persona = BotPersona.values().find { it.id == session.personaId } ?: BotPersona.SOUL
        _uiState.value = _uiState.value.copy(
            currentSessionId = session.id,
            currentSession = session,
            selectedPersona = persona,
            selectedModel = session.modelName,
            isThinkingMode = session.isThinkingEnabled,
            isSearchGrounding = session.isSearchGroundingEnabled,
            activeTab = WorkspaceTab.CHAT
        )
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_uiState.value.currentSessionId == sessionId) {
                _uiState.value = _uiState.value.copy(currentSessionId = null, currentSession = null)
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    private fun updateCurrentSessionSettings() {
        val session = _uiState.value.currentSession ?: return
        viewModelScope.launch {
            val updated = session.copy(
                personaId = _uiState.value.selectedPersona.id,
                modelName = _uiState.value.selectedModel,
                isThinkingEnabled = _uiState.value.isThinkingMode,
                isSearchGroundingEnabled = _uiState.value.isSearchGrounding
            )
            repository.updateSession(updated)
            _uiState.value = _uiState.value.copy(currentSession = updated)
        }
    }

    fun sendMessage(customPrompt: String? = null) {
        val prompt = (customPrompt ?: _uiState.value.inputText).trim()
        val sessionId = _uiState.value.currentSessionId ?: return
        if (prompt.isEmpty() && _uiState.value.selectedImageBitmap == null) return

        val bitmap = _uiState.value.selectedImageBitmap
        val persona = _uiState.value.selectedPersona
        val model = _uiState.value.selectedModel
        val thinking = _uiState.value.isThinkingMode
        val search = _uiState.value.isSearchGrounding
        val pastMsgs = currentMessages.value

        _uiState.value = _uiState.value.copy(
            inputText = "",
            selectedImageBitmap = null,
            isGenerating = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                repository.sendMessage(
                    sessionId = sessionId,
                    userPrompt = prompt,
                    imageBitmap = bitmap,
                    personaInstruction = persona.systemInstruction,
                    modelName = model,
                    enableThinking = thinking,
                    enableSearchGrounding = search,
                    pastMessages = pastMsgs
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isGenerating = false)
            }
        }
    }

    fun playMessageAudio(message: ChatMessage) {
        viewModelScope.launch {
            if (message.audioBase64 != null) {
                audioPlayerManager.playAudio(message.id, message.audioBase64)
            } else {
                try {
                    val speechBase64 = repository.generateSpeechForText(
                        text = message.text.take(300),
                        voiceName = _uiState.value.selectedVoiceName
                    )
                    audioPlayerManager.playAudio(message.id, speechBase64)
                } catch (e: Exception) {
                    audioPlayerManager.speakWithAndroidTts(message.id, message.text)
                }
            }
        }
    }

    fun stopAudio() {
        audioPlayerManager.stopAudio()
    }

    // Visual Studio methods
    fun setVisualPrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(visualPrompt = prompt)
    }

    fun setVisualType(type: String) {
        _uiState.value = _uiState.value.copy(
            visualType = type,
            visualAspectRatio = if (type == "VIDEO") "16:9" else "1:1"
        )
    }

    fun setVisualAspectRatio(ratio: String) {
        _uiState.value = _uiState.value.copy(visualAspectRatio = ratio)
    }

    fun generateVisual() {
        val prompt = _uiState.value.visualPrompt.trim()
        if (prompt.isEmpty()) return
        val type = _uiState.value.visualType
        val ratio = _uiState.value.visualAspectRatio

        _uiState.value = _uiState.value.copy(isGeneratingVisual = true, visualError = null)

        viewModelScope.launch {
            try {
                if (type == "IMAGE") {
                    repository.generateImage(prompt, ratio)
                } else {
                    repository.generateVideoPrompt(prompt, ratio)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(visualError = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isGeneratingVisual = false)
            }
        }
    }

    fun deleteVisual(id: String) {
        viewModelScope.launch {
            repository.deleteVisualCreation(id)
        }
    }

    fun selectVoice(name: String) {
        _uiState.value = _uiState.value.copy(selectedVoiceName = name)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.release()
        speechToTextManager.release()
    }
}
