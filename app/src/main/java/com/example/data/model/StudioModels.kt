package com.example.data.model

import java.util.UUID

enum class IntelligenceMode(
    val title: String,
    val description: String,
    val modelName: String,
    val badge: String
) {
    HIGH_THINKING(
        title = "High Thinking",
        description = "Deep multi-step reasoning with high thinking level",
        modelName = "gemini-1.5-pro",
        badge = "Deep Reasoning"
    ),
    SEARCH_GROUNDING(
        title = "Google Search Grounding",
        description = "Real-time web discovery with live citations and sources",
        modelName = "gemini-1.5-flash",
        badge = "Live Web"
    ),
    MAPS_GROUNDING(
        title = "Google Maps Grounding",
        description = "Location discovery, places, directions and maps data",
        modelName = "gemini-1.5-flash",
        badge = "Google Maps"
    ),
    LOW_LATENCY(
        title = "Low-Latency Quick Response",
        description = "Instant lightning-fast generation for rapid ideation",
        modelName = "gemini-1.5-flash",
        badge = "Instant"
    ),
    CONTENT_ANALYSIS(
        title = "Content Intelligence",
        description = "Analyze documents, code, translate, summarize & rewrite",
        modelName = "gemini-1.5-pro",
        badge = "Pro Analyzer"
    )
}

enum class ImageStudioModel(
    val id: String,
    val displayName: String,
    val description: String
) {
    FLASH_IMAGE(
        id = "gemini-1.5-flash",
        displayName = "Gemini Flash Image",
        description = "Fast, creative generation for everyday visuals"
    ),
    PRO_IMAGE(
        id = "gemini-1.5-pro",
        displayName = "Gemini Pro Image",
        description = "Studio-quality rendering, rich textures & fine detail"
    )
}

enum class AspectRatioOption(
    val ratioString: String,
    val label: String,
    val description: String,
    val widthFactor: Float,
    val heightFactor: Float
) {
    RATIO_1_1("1:1", "1:1 Square", "Social & Avatars", 1f, 1f),
    RATIO_2_3("2:3", "2:3 Portrait", "Classic Portrait", 2f, 3f),
    RATIO_3_2("3:2", "3:2 Landscape", "Standard Photo", 3f, 2f),
    RATIO_3_4("3:4", "3:4 Vertical", "Medium Portrait", 3f, 4f),
    RATIO_4_3("4:3", "4:3 Standard", "Display & Tablet", 4f, 3f),
    RATIO_9_16("9:16", "9:16 Story", "Full Screen Mobile", 9f, 16f),
    RATIO_16_9("16:9", "16:9 Cinema", "Widescreen / Video", 16f, 9f),
    RATIO_21_9("21:9", "21:9 Ultrawide", "Cinematic Panoramic", 21f, 9f)
}

enum class VeoAspectRatio(
    val ratioString: String,
    val label: String,
    val description: String,
    val widthFactor: Float,
    val heightFactor: Float
) {
    LANDSCAPE("16:9", "16:9 Landscape", "Cinematic widescreen", 16f, 9f),
    PORTRAIT("9:16", "9:16 Portrait", "Vertical mobile video", 9f, 16f)
}

data class GroundingSource(
    val title: String,
    val uri: String,
    val snippet: String? = null
)

data class GroundingMetadata(
    val webSearchQueries: List<String> = emptyList(),
    val sources: List<GroundingSource> = emptyList(),
    val searchEntryPointRenderedContent: String? = null
)

data class CreationItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val prompt: String,
    val category: String, // "INTELLIGENCE", "IMAGE", "VIDEO"
    val modelUsed: String,
    val resultText: String? = null,
    val imageBase64: String? = null,
    val videoUri: String? = null,
    val videoOperationName: String? = null,
    val videoStatus: String? = null,
    val groundingMetadata: GroundingMetadata? = null,
    val executionDurationMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val aspectRatio: String? = null,
    val isBookmarked: Boolean = false
) {
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "prompt" to prompt,
            "category" to category,
            "modelUsed" to modelUsed,
            "resultText" to resultText,
            "videoOperationName" to videoOperationName,
            "videoStatus" to videoStatus,
            "executionDurationMs" to executionDurationMs,
            "timestamp" to timestamp,
            "aspectRatio" to aspectRatio,
            "isBookmarked" to isBookmarked
        )
    }
}

data class UserProfile(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean
)

data class GitCommandLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val command: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true
)

data class GitConfig(
    val username: String = "",
    val email: String = "",
    val aliasesEnabled: Boolean = false,
    val isVsCodeEditor: Boolean = false,
    val commandLogs: List<GitCommandLog> = emptyList()
) {
    companion object {
        val COMMON_ALIASES = listOf(
            "st" to "status",
            "co" to "checkout",
            "br" to "branch",
            "ci" to "commit"
        )

        const val VS_CODE_EDITOR_CMD = "git config --global core.editor \"code --wait\""

        fun aliasCommands(): List<String> = COMMON_ALIASES.map { (alias, target) ->
            "git config --global alias.$alias $target"
        }
    }
}

object GitConfigValidator {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateUsername(username: String): String? {
        val trimmed = username.trim()
        if (trimmed.isEmpty()) {
            return "Username cannot be empty"
        }
        return null
    }

    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) {
            return "Email cannot be empty"
        }
        if (!EMAIL_REGEX.matches(trimmed)) {
            return "Please enter a valid email address (e.g. thanakrondie1@gmail.com)"
        }
        return null
    }

    fun isValid(username: String, email: String): Boolean {
        return validateUsername(username) == null && validateEmail(email) == null
    }
}
