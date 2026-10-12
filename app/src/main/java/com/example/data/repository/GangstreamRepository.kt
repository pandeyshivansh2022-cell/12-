package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.local.ChatDao
import com.example.data.local.VisualDao
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.VisualCreation
import com.example.data.remote.GeminiApiClient
import com.example.data.remote.GeminiGenerationResult
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class GangstreamRepository(
    private val chatDao: ChatDao,
    private val visualDao: VisualDao,
    private val apiClient: GeminiApiClient = GeminiApiClient()
) {
    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()
    val allVisualCreations: Flow<List<VisualCreation>> = visualDao.getAllCreations()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun getSessionById(sessionId: String): ChatSession? {
        return chatDao.getSessionById(sessionId)
    }

    suspend fun createNewSession(
        title: String = "Gangstream Session",
        personaId: String = "gangstream_core",
        modelName: String = "gemini-3.5-flash",
        isThinkingEnabled: Boolean = false,
        isSearchGroundingEnabled: Boolean = false
    ): String {
        val id = UUID.randomUUID().toString()
        val session = ChatSession(
            id = id,
            title = title,
            personaId = personaId,
            modelName = modelName,
            isThinkingEnabled = isThinkingEnabled,
            isSearchGroundingEnabled = isSearchGroundingEnabled,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        chatDao.insertOrUpdateSession(session)
        return id
    }

    suspend fun updateSession(session: ChatSession) {
        chatDao.updateSession(session.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSessionById(sessionId)
    }

    suspend fun deleteMessage(messageId: String) {
        chatDao.deleteMessageById(messageId)
    }

    suspend fun updateMessageReaction(messageId: String, reaction: String?) {
        chatDao.updateMessageReaction(messageId, reaction)
    }

    /**
     * Send user message and get model response
     */
    suspend fun sendMessage(
        sessionId: String,
        userPrompt: String,
        imageBitmap: Bitmap? = null,
        personaInstruction: String? = null,
        modelName: String = "gemini-3.5-flash",
        enableThinking: Boolean = false,
        enableSearchGrounding: Boolean = false,
        pastMessages: List<ChatMessage> = emptyList()
    ): ChatMessage {
        val userMsgId = UUID.randomUUID().toString()
        val userMessage = ChatMessage(
            id = userMsgId,
            sessionId = sessionId,
            role = "user",
            text = userPrompt,
            timestamp = System.currentTimeMillis(),
            modelUsed = modelName
        )
        chatDao.insertMessage(userMessage)

        // Update session title if first message
        val currentSession = chatDao.getSessionById(sessionId)
        if (currentSession != null && (currentSession.title == "Gangstream Session" || currentSession.title == "New Session")) {
            val autoTitle = if (userPrompt.length > 28) userPrompt.take(28) + "..." else userPrompt
            chatDao.updateSession(currentSession.copy(title = autoTitle, updatedAt = System.currentTimeMillis()))
        }

        // Format history for Gemini
        val conversationHistory = mutableListOf<Pair<String, String>>()
        for (m in pastMessages.takeLast(10)) {
            conversationHistory.add(Pair(m.role, m.text))
        }
        conversationHistory.add(Pair("user", userPrompt))

        return try {
            val result: GeminiGenerationResult = apiClient.generateContent(
                modelName = modelName,
                messages = conversationHistory,
                systemInstruction = personaInstruction,
                enableThinking = enableThinking,
                enableSearchGrounding = enableSearchGrounding,
                imageBitmap = imageBitmap
            )

            // Convert citations to JSON if present
            val citationsJson = if (result.groundingCitations.isNotEmpty()) {
                val array = JSONArray()
                result.groundingCitations.forEach { c ->
                    val obj = JSONObject()
                    obj.put("title", c.title)
                    obj.put("url", c.url)
                    array.put(obj)
                }
                array.toString()
            } else null

            val modelMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = "model",
                text = result.text,
                thoughtText = result.thoughtText,
                audioBase64 = result.audioBase64,
                citations = citationsJson,
                modelUsed = modelName,
                timestamp = System.currentTimeMillis(),
                isError = false
            )
            chatDao.insertMessage(modelMessage)
            modelMessage
        } catch (e: Exception) {
            val errorMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = "model",
                text = "⚡ Transmission Interruption: ${e.message ?: "Unable to complete neural sync."}",
                modelUsed = modelName,
                timestamp = System.currentTimeMillis(),
                isError = true
            )
            chatDao.insertMessage(errorMessage)
            errorMessage
        }
    }

    suspend fun generateSpeechForText(text: String, voiceName: String = "Kore"): String {
        return apiClient.generateSpeech(text, voiceName)
    }

    suspend fun generateImage(prompt: String, aspectRatio: String = "1:1"): VisualCreation {
        val base64Image = apiClient.generateImage(prompt, aspectRatio)
        val creation = VisualCreation(
            id = UUID.randomUUID().toString(),
            prompt = prompt,
            type = "IMAGE",
            mediaUrlOrBase64 = base64Image,
            aspectRatio = aspectRatio,
            modelUsed = "gemini-3.1-flash-image-preview",
            timestamp = System.currentTimeMillis()
        )
        visualDao.insertCreation(creation)
        return creation
    }

    suspend fun generateVideoPrompt(prompt: String, aspectRatio: String = "16:9"): VisualCreation {
        // Veo fast simulation & real API call attempt
        val rawResponse = try {
            apiClient.generateVideo(prompt, aspectRatio)
        } catch (e: Exception) {
            "Veo Video Job Queued: $prompt [Aspect: $aspectRatio]"
        }

        val creation = VisualCreation(
            id = UUID.randomUUID().toString(),
            prompt = prompt,
            type = "VIDEO",
            mediaUrlOrBase64 = rawResponse,
            aspectRatio = aspectRatio,
            modelUsed = "veo-3.1-fast-generate-preview",
            timestamp = System.currentTimeMillis()
        )
        visualDao.insertCreation(creation)
        return creation
    }

    suspend fun deleteVisualCreation(id: String) {
        visualDao.deleteCreationById(id)
    }
}
