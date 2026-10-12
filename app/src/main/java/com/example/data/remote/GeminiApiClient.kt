package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class GeminiGenerationResult(
    val text: String,
    val thoughtText: String? = null,
    val groundingCitations: List<com.example.data.model.GroundingCitation> = emptyList(),
    val audioBase64: String? = null,
    val imageBase64: String? = null,
    val rawJson: String? = null
)

class GeminiApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"

    private fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY.ifEmpty { "" }
    }

    /**
     * Generate content with multi-turn history, optional thinking mode, optional search grounding, optional system instruction.
     */
    suspend fun generateContent(
        modelName: String,
        messages: List<Pair<String, String>>, // (role, text)
        systemInstruction: String? = null,
        enableThinking: Boolean = false,
        enableSearchGrounding: Boolean = false,
        imageBitmap: Bitmap? = null
    ): GeminiGenerationResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val url = "$baseUrl/$modelName:generateContent?key=$apiKey"

        val rootJson = JSONObject()

        // Contents
        val contentsArray = JSONArray()
        for (i in messages.indices) {
            val (role, text) = messages[i]
            val contentObj = JSONObject()
            contentObj.put("role", if (role == "user") "user" else "model")

            val partsArray = JSONArray()
            val textPart = JSONObject()
            textPart.put("text", text)
            partsArray.put(textPart)

            // If last message and image is attached
            if (i == messages.lastIndex && imageBitmap != null) {
                val imagePart = JSONObject()
                val inlineData = JSONObject()
                inlineData.put("mimeType", "image/jpeg")
                inlineData.put("data", bitmapToBase64(imageBitmap))
                imagePart.put("inlineData", inlineData)
                partsArray.put(imagePart)
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
        }
        rootJson.put("contents", contentsArray)

        // System Instruction
        if (!systemInstruction.isNullOrBlank()) {
            val sysInstObj = JSONObject()
            val parts = JSONArray().put(JSONObject().put("text", systemInstruction))
            sysInstObj.put("parts", parts)
            rootJson.put("systemInstruction", sysInstObj)
        }

        // Generation Config
        val genConfig = JSONObject()
        if (enableThinking && modelName.contains("pro")) {
            val thinkingConfig = JSONObject()
            thinkingConfig.put("thinkingLevel", "HIGH")
            genConfig.put("thinkingConfig", thinkingConfig)
        }
        rootJson.put("generationConfig", genConfig)

        // Tools (Search Grounding)
        if (enableSearchGrounding) {
            val toolsArray = JSONArray()
            val searchTool = JSONObject()
            searchTool.put("googleSearch", JSONObject())
            toolsArray.put(searchTool)
            rootJson.put("tools", toolsArray)
        }

        val requestBody = rootJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response from Gemini API")

        if (!response.isSuccessful) {
            val errorMsg = try {
                val errObj = JSONObject(responseBody).optJSONObject("error")
                errObj?.optString("message") ?: responseBody
            } catch (e: Exception) {
                responseBody
            }
            throw Exception("Gemini Error (${response.code}): $errorMsg")
        }

        parseGeminiResponse(responseBody)
    }

    /**
     * Stream content generation chunk by chunk
     */
    suspend fun streamGenerateContent(
        modelName: String,
        messages: List<Pair<String, String>>,
        systemInstruction: String? = null,
        enableThinking: Boolean = false,
        enableSearchGrounding: Boolean = false,
        onChunk: (textChunk: String, thoughtChunk: String?) -> Unit
    ) = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val url = "$baseUrl/$modelName:streamGenerateContent?alt=sse&key=$apiKey"

        val rootJson = JSONObject()
        val contentsArray = JSONArray()
        for ((role, text) in messages) {
            val contentObj = JSONObject()
            contentObj.put("role", if (role == "user") "user" else "model")
            val partsArray = JSONArray().put(JSONObject().put("text", text))
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
        }
        rootJson.put("contents", contentsArray)

        if (!systemInstruction.isNullOrBlank()) {
            val sysInstObj = JSONObject()
            sysInstObj.put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
            rootJson.put("systemInstruction", sysInstObj)
        }

        val genConfig = JSONObject()
        if (enableThinking && modelName.contains("pro")) {
            genConfig.put("thinkingConfig", JSONObject().put("thinkingLevel", "HIGH"))
        }
        rootJson.put("generationConfig", genConfig)

        if (enableSearchGrounding) {
            rootJson.put("tools", JSONArray().put(JSONObject().put("googleSearch", JSONObject())))
        }

        val request = Request.Builder()
            .url(url)
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val err = response.body?.string() ?: "Stream error"
            throw Exception("Stream Error (${response.code}): $err")
        }

        response.body?.byteStream()?.bufferedReader()?.use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line?.trim() ?: continue
                if (!currentLine.startsWith("data:")) continue
                val payload = currentLine.removePrefix("data:").trim()
                if (payload.isEmpty() || payload == "[DONE]") continue

                try {
                    val jsonObj = JSONObject(payload)
                    val candidates = jsonObj.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null) {
                            for (p in 0 until parts.length()) {
                                val partObj = parts.getJSONObject(p)
                                val text = partObj.optString("text", "")
                                val thought = partObj.optBoolean("thought", false)
                                if (thought) {
                                    onChunk("", text)
                                } else if (text.isNotEmpty()) {
                                    onChunk(text, null)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore transient chunk parse errors
                }
            }
        }
    }

    /**
     * Text to Speech using Gemini Audio
     */
    suspend fun generateSpeech(
        text: String,
        voiceName: String = "Kore",
        modelName: String = "gemini-2.5-flash-preview-tts"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val url = "$baseUrl/$modelName:generateContent?key=$apiKey"

        val rootJson = JSONObject()
        val contentsArray = JSONArray().put(
            JSONObject().put(
                "parts", JSONArray().put(
                    JSONObject().put("text", "Say with clear, engaging, futuristic cybernetic charisma: $text")
                )
            )
        )
        rootJson.put("contents", contentsArray)

        val genConfig = JSONObject()
        genConfig.put("responseModalities", JSONArray().put("AUDIO"))
        val speechConfig = JSONObject()
        val voiceConfig = JSONObject()
        val prebuiltVoiceConfig = JSONObject().put("voiceName", voiceName)
        voiceConfig.put("prebuiltVoiceConfig", prebuiltVoiceConfig)
        speechConfig.put("voiceConfig", voiceConfig)
        genConfig.put("speechConfig", speechConfig)

        rootJson.put("generationConfig", genConfig)

        val response = client.newCall(
            Request.Builder().url(url).post(rootJson.toString().toRequestBody(jsonMediaType)).build()
        ).execute()

        val responseBody = response.body?.string() ?: throw Exception("Empty speech response")
        if (!response.isSuccessful) {
            throw Exception("Speech generation error: $responseBody")
        }

        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates") ?: throw Exception("No audio candidates")
        val parts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            val inlineData = part.optJSONObject("inlineData")
            if (inlineData != null) {
                return@withContext inlineData.getString("data")
            }
        }
        throw Exception("No audio data in response")
    }

    /**
     * Image generation using gemini-3.1-flash-image-preview / gemini-2.5-flash-image
     */
    suspend fun generateImage(
        prompt: String,
        aspectRatio: String = "1:1",
        modelName: String = "gemini-3.1-flash-image-preview"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val url = "$baseUrl/$modelName:generateContent?key=$apiKey"

        val rootJson = JSONObject()
        val contentsArray = JSONArray().put(
            JSONObject().put(
                "parts", JSONArray().put(
                    JSONObject().put("text", prompt)
                )
            )
        )
        rootJson.put("contents", contentsArray)

        val genConfig = JSONObject()
        genConfig.put("responseModalities", JSONArray().put("IMAGE").put("TEXT"))
        val imageConfig = JSONObject()
        imageConfig.put("aspectRatio", aspectRatio)
        imageConfig.put("imageSize", "1K")
        genConfig.put("imageConfig", imageConfig)
        rootJson.put("generationConfig", genConfig)

        val response = client.newCall(
            Request.Builder().url(url).post(rootJson.toString().toRequestBody(jsonMediaType)).build()
        ).execute()

        val responseBody = response.body?.string() ?: throw Exception("Empty image response")
        if (!response.isSuccessful) {
            // If 3.1-flash-image-preview fails, fallback to 2.5-flash-image
            if (modelName != "gemini-2.5-flash-image") {
                return@withContext generateImage(prompt, aspectRatio, "gemini-2.5-flash-image")
            }
            throw Exception("Image generation failed: $responseBody")
        }

        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates") ?: throw Exception("No candidates")
        val parts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            val inlineData = part.optJSONObject("inlineData")
            if (inlineData != null) {
                return@withContext inlineData.getString("data")
            }
        }
        throw Exception("No image binary returned in response")
    }

    /**
     * Veo Video Generation (veo-3.1-fast-generate-preview)
     */
    suspend fun generateVideo(
        prompt: String,
        aspectRatio: String = "16:9",
        modelName: String = "veo-3.1-fast-generate-preview"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val url = "$baseUrl/$modelName:generateVideos?key=$apiKey"

        val rootJson = JSONObject()
        rootJson.put("prompt", prompt)
        val config = JSONObject()
        config.put("numberOfVideos", 1)
        config.put("resolution", "720p")
        config.put("aspectRatio", aspectRatio)
        rootJson.put("config", config)

        val response = client.newCall(
            Request.Builder().url(url).post(rootJson.toString().toRequestBody(jsonMediaType)).build()
        ).execute()

        val responseBody = response.body?.string() ?: throw Exception("Empty video response")
        if (!response.isSuccessful) {
            throw Exception("Veo generation error: $responseBody")
        }

        // Returns operation name or video JSON
        responseBody
    }

    private fun parseGeminiResponse(jsonString: String): GeminiGenerationResult {
        val json = JSONObject(jsonString)
        val candidates = json.optJSONArray("candidates") ?: return GeminiGenerationResult("No candidate responses received.")

        val firstCandidate = candidates.optJSONObject(0) ?: return GeminiGenerationResult("Empty candidate received.")
        val content = firstCandidate.optJSONObject("content")
        val parts = content?.optJSONArray("parts")

        val textBuilder = StringBuilder()
        val thoughtBuilder = StringBuilder()
        var audioBase64: String? = null
        var imageBase64: String? = null

        if (parts != null) {
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                val text = part.optString("text", "")
                val isThought = part.optBoolean("thought", false)

                if (isThought) {
                    thoughtBuilder.append(text)
                } else if (text.isNotEmpty()) {
                    textBuilder.append(text)
                }

                val inlineData = part.optJSONObject("inlineData")
                if (inlineData != null) {
                    val mime = inlineData.optString("mimeType", "")
                    val data = inlineData.optString("data", "")
                    if (mime.startsWith("audio/")) {
                        audioBase64 = data
                    } else if (mime.startsWith("image/")) {
                        imageBase64 = data
                    }
                }
            }
        }

        // Parse Grounding Metadata
        val groundingCitations = mutableListOf<com.example.data.model.GroundingCitation>()
        val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
        if (groundingMetadata != null) {
            val chunks = groundingMetadata.optJSONArray("groundingChunks")
            if (chunks != null) {
                for (c in 0 until chunks.length()) {
                    val chunk = chunks.getJSONObject(c)
                    val web = chunk.optJSONObject("web")
                    if (web != null) {
                        val title = web.optString("title", "Web Source")
                        val uri = web.optString("uri", "")
                        if (uri.isNotEmpty()) {
                            groundingCitations.add(
                                com.example.data.model.GroundingCitation(title = title, url = uri)
                            )
                        }
                    }
                }
            }
        }

        return GeminiGenerationResult(
            text = textBuilder.toString().ifEmpty { "Response received." },
            thoughtText = thoughtBuilder.toString().ifEmpty { null },
            groundingCitations = groundingCitations,
            audioBase64 = audioBase64,
            imageBase64 = imageBase64,
            rawJson = jsonString
        )
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
