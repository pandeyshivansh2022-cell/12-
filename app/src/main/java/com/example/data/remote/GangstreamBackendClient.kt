package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class GangstreamBackendClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Send message to Express /api/chat endpoint
     * Expects: POST /api/chat with {"message": "..."}
     * Returns: response text from {"response": "..."}
     */
    suspend fun sendChatMessage(backendBaseUrl: String, message: String): String = withContext(Dispatchers.IO) {
        val cleanUrl = backendBaseUrl.trim().removeSuffix("/")
        val url = "$cleanUrl/api/chat"

        val jsonBody = JSONObject().apply {
            put("message", message)
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response from backend server")

        if (!response.isSuccessful) {
            val errorMsg = try {
                val obj = JSONObject(responseBody)
                obj.optString("error", responseBody)
            } catch (e: Exception) {
                responseBody
            }
            throw Exception("Backend (${response.code}): $errorMsg")
        }

        val json = JSONObject(responseBody)
        val reply = json.optString("response", "")
        if (reply.isNotEmpty()) {
            reply
        } else {
            "Response received from Gangstream backend."
        }
    }

    /**
     * Generate image via Express /api/generate-image (OpenAI DALL·E 3)
     * Expects: POST /api/generate-image with {"prompt": "..."}
     * Returns: image URL string
     */
    suspend fun generateImage(backendBaseUrl: String, prompt: String): String = withContext(Dispatchers.IO) {
        val cleanUrl = backendBaseUrl.trim().removeSuffix("/")
        val url = "$cleanUrl/api/generate-image"

        val jsonBody = JSONObject().apply {
            put("prompt", prompt)
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response from media generator")

        if (!response.isSuccessful) {
            val errorMsg = try {
                val obj = JSONObject(responseBody)
                obj.optString("error", responseBody)
            } catch (e: Exception) {
                responseBody
            }
            throw Exception("Media Generator (${response.code}): $errorMsg")
        }

        val json = JSONObject(responseBody)
        val imageUrl = json.optString("imageUrl", "")
        if (imageUrl.isNotEmpty()) {
            imageUrl
        } else {
            throw Exception("No imageUrl returned by DALL·E 3 endpoint")
        }
    }

    /**
     * Generate video via Express /api/generate-video (FFmpeg slideshow)
     * Expects: POST /api/generate-video with {"prompt": "...", "duration": 5}
     * Streams the generated MP4 file to local cache and returns the file path.
     */
    suspend fun generateVideo(
        backendBaseUrl: String,
        prompt: String,
        duration: Int = 5,
        cacheDir: File
    ): String = withContext(Dispatchers.IO) {
        val cleanUrl = backendBaseUrl.trim().removeSuffix("/")
        val url = "$cleanUrl/api/generate-video"

        val jsonBody = JSONObject().apply {
            put("prompt", prompt)
            put("duration", duration)
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errBody = response.body?.string() ?: "Video generation failed"
            val errorMsg = try {
                JSONObject(errBody).optString("error", errBody)
            } catch (e: Exception) {
                errBody
            }
            throw Exception("Video Generator (${response.code}): $errorMsg")
        }

        val body = response.body ?: throw Exception("Empty video payload received")
        val outputFile = File(cacheDir, "generated_video_${System.currentTimeMillis()}.mp4")
        outputFile.outputStream().use { fileOut ->
            body.byteStream().use { byteIn ->
                byteIn.copyTo(fileOut)
            }
        }
        outputFile.absolutePath
    }

    /**
     * Check health status of Express server
     * Expects: GET /health
     * Returns: Pair of (isHealthy, statusMessage)
     */
    suspend fun checkHealth(backendBaseUrl: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val cleanUrl = backendBaseUrl.trim().removeSuffix("/")
            val url = "$cleanUrl/health"

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext Pair(false, "No body returned")

            if (response.isSuccessful) {
                val json = JSONObject(body)
                val status = json.optString("status", "OK")
                Pair(status == "OK", "Server is active ($status)")
            } else {
                Pair(false, "Server returned HTTP ${response.code}")
            }
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Unable to connect to server")
        }
    }
}
