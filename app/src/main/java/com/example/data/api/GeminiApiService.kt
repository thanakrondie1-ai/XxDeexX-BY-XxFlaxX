package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.GroundingMetadata
import com.example.data.model.GroundingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApiService {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"

    private fun getApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        return if (key.isNotEmpty() && key != "MY_GEMINI_API_KEY") key else ""
    }

    suspend fun generateContent(
        model: String,
        prompt: String,
        tools: JSONArray? = null,
        thinkingLevel: String? = null,
        systemInstruction: String? = null
    ): Result<IntelligenceResponse> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val url = "$baseUrl/models/$model:generateContent?key=$apiKey"

            val payload = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                }
                put("contents", contents)

                if (tools != null && tools.length() > 0) {
                    put("tools", tools)
                }

                if (!thinkingLevel.isNullOrEmpty() && model.contains("thinking", ignoreCase = true)) {
                    val genConfig = JSONObject().apply {
                        val thinkingConfig = JSONObject().apply {
                            put("thinkingLevel", thinkingLevel)
                        }
                        put("thinkingConfig", thinkingConfig)
                    }
                    put("generationConfig", genConfig)
                }

                if (!systemInstruction.isNullOrEmpty()) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", systemInstruction)
                            })
                        })
                    })
                }
            }

            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiApiService", "API Error: ${response.code} $responseBody")
                return@withContext Result.failure(
                    Exception("Gemini API Error (${response.code}): ${parseErrorMessage(responseBody)}")
                )
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)

            var extractedText = ""
            var thinkingText: String? = null

            val parts = firstCandidate?.optJSONObject("content")?.optJSONArray("parts")
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i)
                    if (part != null) {
                        if (part.optBoolean("thought", false)) {
                            thinkingText = part.optString("text")
                        } else if (part.has("text")) {
                            extractedText += part.optString("text")
                        }
                    }
                }
            }

            if (extractedText.isEmpty() && thinkingText != null) {
                extractedText = thinkingText
            }

            // Extract Grounding metadata
            val groundingMetadataJson = firstCandidate?.optJSONObject("groundingMetadata")
            val groundingMetadata = parseGroundingMetadata(groundingMetadataJson)

            Result.success(
                IntelligenceResponse(
                    text = extractedText,
                    thinking = thinkingText,
                    groundingMetadata = groundingMetadata
                )
            )
        } catch (e: Exception) {
            Log.e("GeminiApiService", "Exception calling Gemini API", e)
            Result.failure(e)
        }
    }

    suspend fun generateImage(
        model: String,
        prompt: String,
        aspectRatio: String
    ): Result<ImageResponse> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val url = "$baseUrl/models/$model:generateContent?key=$apiKey"

            val payload = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                }
                put("contents", contents)

                val genConfig = JSONObject().apply {
                    val imageConfig = JSONObject().apply {
                        put("aspectRatio", aspectRatio)
                        put("imageSize", "1K")
                    }
                    put("imageConfig", imageConfig)
                    put("responseModalities", JSONArray().apply {
                        put("TEXT")
                        put("IMAGE")
                    })
                }
                put("generationConfig", genConfig)
            }

            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiApiService", "Image API Error: ${response.code} $responseBody")
                return@withContext Result.failure(
                    Exception("Image API Error (${response.code}): ${parseErrorMessage(responseBody)}")
                )
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val parts = firstCandidate?.optJSONObject("content")?.optJSONArray("parts")

            var imageBase64: String? = null
            var mimeType: String? = null
            var textDescription: String? = null

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i)
                    if (part != null) {
                        if (part.has("inlineData")) {
                            val inlineData = part.getJSONObject("inlineData")
                            imageBase64 = inlineData.optString("data")
                            mimeType = inlineData.optString("mimeType", "image/png")
                        } else if (part.has("text")) {
                            textDescription = part.optString("text")
                        }
                    }
                }
            }

            if (imageBase64 != null) {
                Result.success(ImageResponse(base64Data = imageBase64, mimeType = mimeType ?: "image/png", caption = textDescription))
            } else {
                Result.failure(Exception("No image returned from Gemini model. Output was text: $textDescription"))
            }
        } catch (e: Exception) {
            Log.e("GeminiApiService", "Exception calling Image API", e)
            Result.failure(e)
        }
    }

    suspend fun generateVideo(
        prompt: String,
        aspectRatio: String
    ): Result<VideoResponse> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            val modelsToTry = listOf(
                "veo-3.1-fast-generate-preview",
                "veo-3.1-generate-preview"
            )

            var lastError = "Failed to initiate video generation"

            for (model in modelsToTry) {
                val url = "$baseUrl/models/$model:predictLongRunning?key=$apiKey"

                val payload = JSONObject().apply {
                    val instances = JSONArray().apply {
                        put(JSONObject().apply {
                            put("prompt", prompt)
                        })
                    }
                    put("instances", instances)
                    put("parameters", JSONObject().apply {
                        put("sampleCount", 1)
                        put("aspectRatio", aspectRatio)
                    })
                }

                val request = Request.Builder()
                    .url(url)
                    .post(payload.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val json = JSONObject(responseBody)
                    val operationName = json.optString("name", "")

                    return@withContext Result.success(
                        VideoResponse(
                            operationName = operationName,
                            prompt = prompt,
                            aspectRatio = aspectRatio,
                            message = "Veo 3 generation initiated! Operation: $operationName"
                        )
                    )
                }

                val parsedError = parseErrorMessage(responseBody)
                Log.w("GeminiApiService", "Veo Video API ($model): ${response.code} $parsedError")

                if (response.code == 429) {
                    return@withContext Result.failure(
                        Exception("Veo 3 quota limit reached for this API key. Note: Veo preview requires active Gemini project quota.")
                    )
                }

                lastError = "Veo Video Error (${response.code}): $parsedError"

                // If not 404, don't try other models
                if (response.code != 404) {
                    break
                }
            }

            Result.failure(Exception(lastError))
        } catch (e: Exception) {
            Log.e("GeminiApiService", "Exception calling Veo API", e)
            Result.failure(e)
        }
    }

    private fun parseGroundingMetadata(json: JSONObject?): GroundingMetadata? {
        if (json == null) return null
        val queries = mutableListOf<String>()
        val webQueriesArray = json.optJSONArray("webSearchQueries")
        if (webQueriesArray != null) {
            for (i in 0 until webQueriesArray.length()) {
                queries.add(webQueriesArray.optString(i))
            }
        }

        val sources = mutableListOf<GroundingSource>()
        val chunksArray = json.optJSONArray("groundingChunks")
        if (chunksArray != null) {
            for (i in 0 until chunksArray.length()) {
                val chunk = chunksArray.optJSONObject(i)
                val web = chunk?.optJSONObject("web")
                if (web != null) {
                    sources.add(
                        GroundingSource(
                            title = web.optString("title", "Web Source"),
                            uri = web.optString("uri", ""),
                            snippet = null
                        )
                    )
                }
            }
        }

        val searchEntryPoint = json.optJSONObject("searchEntryPoint")?.optString("renderedContent")

        return GroundingMetadata(
            webSearchQueries = queries,
            sources = sources,
            searchEntryPointRenderedContent = searchEntryPoint
        )
    }

    private fun parseErrorMessage(body: String): String {
        return try {
            val json = JSONObject(body)
            val error = json.optJSONObject("error")
            error?.optString("message") ?: body
        } catch (_: Exception) {
            body
        }
    }
}

data class IntelligenceResponse(
    val text: String,
    val thinking: String?,
    val groundingMetadata: GroundingMetadata?
)

data class ImageResponse(
    val base64Data: String,
    val mimeType: String,
    val caption: String?
)

data class VideoResponse(
    val operationName: String,
    val prompt: String,
    val aspectRatio: String,
    val message: String
)
