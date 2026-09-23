package com.example.data.repository

import android.content.Context
import com.example.data.datastore.GitDataStoreService
import com.example.data.api.GeminiApiService
import com.example.data.firebase.FirebaseService
import com.example.data.system.SystemShellService
import com.example.data.model.AspectRatioOption
import com.example.data.model.CreationItem
import com.example.data.model.GitCommandLog
import com.example.data.model.GitConfig
import com.example.data.model.ImageStudioModel
import com.example.data.model.IntelligenceMode
import com.example.data.model.UserProfile
import com.example.data.model.VeoAspectRatio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

class StudioRepository(context: Context) {

    private val apiService = GeminiApiService()
    private val firebaseService = FirebaseService(context)
    private val gitDataStoreService = GitDataStoreService(context)
    private val systemShellService = SystemShellService()

    val currentUserProfile: StateFlow<UserProfile?> = firebaseService.currentUserProfile

    fun observeCreations(): Flow<List<CreationItem>> = firebaseService.observeCreations()

    fun observeGitConfig(): Flow<GitConfig> = gitDataStoreService.gitConfigFlow

    suspend fun saveGitConfig(config: GitConfig) {
        gitDataStoreService.saveGitConfig(config)
    }

    suspend fun signInAnonymously(): Result<UserProfile> = firebaseService.signInAnonymously()

    suspend fun signInWithGoogle(activityContext: Context, webClientId: String = ""): Result<UserProfile> =
        firebaseService.signInWithGoogleCredential(activityContext, webClientId)

    fun signOut() = firebaseService.signOut()

    suspend fun deleteCreation(id: String) = firebaseService.deleteCreation(id)

    suspend fun toggleBookmark(id: String, isBookmarked: Boolean) =
        firebaseService.toggleBookmark(id, isBookmarked)

    suspend fun detectSystemGitConfig(): Result<Pair<String, String>> = systemShellService.detectGitConfig()

    suspend fun executeIntelligence(
        prompt: String,
        mode: IntelligenceMode,
        systemInstruction: String? = null
    ): Result<CreationItem> {
        val startTime = System.currentTimeMillis()

        val tools = when (mode) {
            IntelligenceMode.SEARCH_GROUNDING -> JSONArray().apply {
                put(JSONObject().apply {
                    put("googleSearch", JSONObject())
                })
            }
            IntelligenceMode.MAPS_GROUNDING -> JSONArray().apply {
                put(JSONObject().apply {
                    put("googleMaps", JSONObject())
                })
            }
            else -> null
        }

        val thinkingLevel = if (mode == IntelligenceMode.HIGH_THINKING) "HIGH" else null

        val result = apiService.generateContent(
            model = mode.modelName,
            prompt = prompt,
            tools = tools,
            thinkingLevel = thinkingLevel,
            systemInstruction = systemInstruction
        )

        val duration = System.currentTimeMillis() - startTime

        return result.map { response ->
            val creation = CreationItem(
                title = prompt.take(45) + if (prompt.length > 45) "..." else "",
                prompt = prompt,
                category = "INTELLIGENCE",
                modelUsed = mode.modelName,
                resultText = response.text,
                groundingMetadata = response.groundingMetadata,
                executionDurationMs = duration,
                timestamp = System.currentTimeMillis()
            )
            // Persist to Firestore
            firebaseService.saveCreation(creation)
            creation
        }
    }

    suspend fun executeImageGeneration(
        prompt: String,
        model: ImageStudioModel,
        aspectRatio: AspectRatioOption
    ): Result<CreationItem> {
        val startTime = System.currentTimeMillis()

        val result = apiService.generateImage(
            model = model.id,
            prompt = prompt,
            aspectRatio = aspectRatio.ratioString
        )

        val duration = System.currentTimeMillis() - startTime

        return result.map { imageResponse ->
            val creation = CreationItem(
                title = prompt.take(45) + if (prompt.length > 45) "..." else "",
                prompt = prompt,
                category = "IMAGE",
                modelUsed = model.id,
                resultText = imageResponse.caption ?: "Generated in ${aspectRatio.ratioString} ratio",
                imageBase64 = imageResponse.base64Data,
                aspectRatio = aspectRatio.ratioString,
                executionDurationMs = duration,
                timestamp = System.currentTimeMillis()
            )
            firebaseService.saveCreation(creation)
            creation
        }
    }

    suspend fun executeVideoGeneration(
        prompt: String,
        aspectRatio: VeoAspectRatio
    ): Result<CreationItem> {
        val startTime = System.currentTimeMillis()

        val result = apiService.generateVideo(
            prompt = prompt,
            aspectRatio = aspectRatio.ratioString
        )

        val duration = System.currentTimeMillis() - startTime

        return result.map { videoResponse ->
            val creation = CreationItem(
                title = prompt.take(45) + if (prompt.length > 45) "..." else "",
                prompt = prompt,
                category = "VIDEO",
                modelUsed = "veo-3.1-fast-generate-preview",
                resultText = videoResponse.message,
                videoOperationName = videoResponse.operationName,
                videoStatus = "Initiated",
                aspectRatio = aspectRatio.ratioString,
                executionDurationMs = duration,
                timestamp = System.currentTimeMillis()
            )
            firebaseService.saveCreation(creation)
            creation
        }
    }
}
