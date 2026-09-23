package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AspectRatioOption
import com.example.data.model.CreationItem
import com.example.data.model.GitCommandLog
import com.example.data.model.GitConfig
import com.example.data.model.GitConfigValidator
import com.example.data.model.ImageStudioModel
import com.example.data.model.IntelligenceMode
import com.example.data.model.UserProfile
import com.example.data.model.VeoAspectRatio
import com.example.data.repository.StudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

enum class StudioTab(val label: String) {
    INTELLIGENCE("Intelligence"),
    IMAGE_STUDIO("Image Studio"),
    VIDEO_STUDIO("Veo 3 Video"),
    LIBRARY("Library")
}

data class IntelligenceUiState(
    val selectedMode: IntelligenceMode = IntelligenceMode.HIGH_THINKING,
    val prompt: String = "",
    val isLoading: Boolean = false,
    val currentResult: CreationItem? = null,
    val errorMessage: String? = null
)

data class ImageStudioUiState(
    val prompt: String = "",
    val selectedModel: ImageStudioModel = ImageStudioModel.FLASH_IMAGE,
    val selectedRatio: AspectRatioOption = AspectRatioOption.RATIO_1_1,
    val isLoading: Boolean = false,
    val currentResult: CreationItem? = null,
    val errorMessage: String? = null
)

data class VideoStudioUiState(
    val prompt: String = "",
    val selectedRatio: VeoAspectRatio = VeoAspectRatio.LANDSCAPE,
    val isLoading: Boolean = false,
    val currentResult: CreationItem? = null,
    val errorMessage: String? = null
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudioRepository(application.applicationContext)

    val currentUserProfile: StateFlow<UserProfile?> = repository.currentUserProfile

    val creationsList: StateFlow<List<CreationItem>> = repository.observeCreations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(StudioTab.INTELLIGENCE)
    val currentTab: StateFlow<StudioTab> = _currentTab.asStateFlow()

    private val _intelligenceState = MutableStateFlow(IntelligenceUiState())
    val intelligenceState: StateFlow<IntelligenceUiState> = _intelligenceState.asStateFlow()

    private val _imageState = MutableStateFlow(ImageStudioUiState())
    val imageState: StateFlow<ImageStudioUiState> = _imageState.asStateFlow()

    private val _videoState = MutableStateFlow(VideoStudioUiState())
    val videoState: StateFlow<VideoStudioUiState> = _videoState.asStateFlow()

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    val gitConfig: StateFlow<GitConfig> = repository.observeGitConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GitConfig())

    private val _showGitConfigDialog = MutableStateFlow(false)
    val showGitConfigDialog: StateFlow<Boolean> = _showGitConfigDialog.asStateFlow()

    private val _selectedCreationDetail = MutableStateFlow<CreationItem?>(null)
    val selectedCreationDetail: StateFlow<CreationItem?> = _selectedCreationDetail.asStateFlow()

    private val _historyFilter = MutableStateFlow("ALL")
    val historyFilter: StateFlow<String> = _historyFilter.asStateFlow()

    init {
        viewModelScope.launch {
            // Attempt to auto-fill from system if DataStore is currently empty on first launch
            repository.observeGitConfig().take(1).collect { config ->
                if (config.username.isEmpty() && config.email.isEmpty()) {
                    val result = repository.detectSystemGitConfig()
                    result.onSuccess { (name, email) ->
                        if (name.isNotEmpty() || email.isNotEmpty()) {
                            repository.saveGitConfig(config.copy(username = name, email = email))
                        }
                    }
                }
            }
        }
    }

    fun selectTab(tab: StudioTab) {
        _currentTab.value = tab
    }

    fun setAuthDialogVisible(visible: Boolean) {
        _showAuthDialog.value = visible
    }

    fun setGitConfigDialogVisible(visible: Boolean) {
        _showGitConfigDialog.value = visible
    }

    fun saveGitConfig(
        username: String,
        email: String,
        aliasesEnabled: Boolean = false,
        isVsCodeEditor: Boolean = false
    ): Result<Unit> {
        val usernameError = GitConfigValidator.validateUsername(username)
        if (usernameError != null) {
            return Result.failure(IllegalArgumentException(usernameError))
        }

        val emailError = GitConfigValidator.validateEmail(email)
        if (emailError != null) {
            return Result.failure(IllegalArgumentException(emailError))
        }

        val now = System.currentTimeMillis()
        val newLogs = mutableListOf<GitCommandLog>()

        newLogs.add(GitCommandLog(command = "git config --global user.name \"${username.trim()}\"", timestamp = now))
        newLogs.add(GitCommandLog(command = "git config --global user.email \"${email.trim()}\"", timestamp = now + 1))

        if (aliasesEnabled) {
            GitConfig.aliasCommands().forEachIndexed { index, cmd ->
                newLogs.add(GitCommandLog(command = cmd, timestamp = now + 2 + index))
            }
        }

        if (isVsCodeEditor) {
            newLogs.add(GitCommandLog(command = GitConfig.VS_CODE_EDITOR_CMD, timestamp = now + 10))
        }

        val combinedLogs = (gitConfig.value.commandLogs + newLogs)

        val config = GitConfig(
            username = username.trim(),
            email = email.trim(),
            aliasesEnabled = aliasesEnabled,
            isVsCodeEditor = isVsCodeEditor,
            commandLogs = combinedLogs
        )
        viewModelScope.launch {
            repository.saveGitConfig(config)
        }
        return Result.success(Unit)
    }

    fun applyGitAliases() {
        val now = System.currentTimeMillis()
        val newLogs = GitConfig.aliasCommands().mapIndexed { index, cmd ->
            GitCommandLog(command = cmd, timestamp = now + index)
        }
        val current = gitConfig.value
        val updated = current.copy(
            aliasesEnabled = true,
            commandLogs = current.commandLogs + newLogs
        )
        viewModelScope.launch {
            repository.saveGitConfig(updated)
        }
    }

    fun applyVsCodeEditor() {
        val current = gitConfig.value
        val newLog = GitCommandLog(
            command = GitConfig.VS_CODE_EDITOR_CMD,
            timestamp = System.currentTimeMillis()
        )
        val updated = current.copy(
            isVsCodeEditor = true,
            commandLogs = current.commandLogs + newLog
        )
        viewModelScope.launch {
            repository.saveGitConfig(updated)
        }
    }

    fun clearCommandLogs() {
        val updated = gitConfig.value.copy(commandLogs = emptyList())
        viewModelScope.launch {
            repository.saveGitConfig(updated)
        }
    }

    fun detectSystemGitConfig(onResult: (Pair<String, String>?, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.detectSystemGitConfig()
            result.onSuccess { pair ->
                onResult(pair, null)
            }.onFailure { error ->
                onResult(null, error.localizedMessage ?: "Failed to detect system git config")
            }
        }
    }

    fun selectCreationDetail(item: CreationItem?) {
        _selectedCreationDetail.value = item
    }

    fun setHistoryFilter(filter: String) {
        _historyFilter.value = filter
    }

    // --- Intelligence Actions ---

    fun setIntelligenceMode(mode: IntelligenceMode) {
        _intelligenceState.value = _intelligenceState.value.copy(
            selectedMode = mode,
            errorMessage = null
        )
    }

    fun setIntelligencePrompt(prompt: String) {
        _intelligenceState.value = _intelligenceState.value.copy(prompt = prompt)
    }

    fun runIntelligenceQuery() {
        val state = _intelligenceState.value
        val prompt = state.prompt.trim()
        if (prompt.isEmpty() || state.isLoading) return

        _intelligenceState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = repository.executeIntelligence(
                prompt = prompt,
                mode = state.selectedMode
            )
            result.onSuccess { creation ->
                _intelligenceState.value = _intelligenceState.value.copy(
                    isLoading = false,
                    currentResult = creation,
                    errorMessage = null
                )
            }.onFailure { error ->
                _intelligenceState.value = _intelligenceState.value.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Failed to generate response"
                )
            }
        }
    }

    // --- Image Studio Actions ---

    fun setImagePrompt(prompt: String) {
        _imageState.value = _imageState.value.copy(prompt = prompt)
    }

    fun setImageModel(model: ImageStudioModel) {
        _imageState.value = _imageState.value.copy(selectedModel = model)
    }

    fun setImageAspectRatio(ratio: AspectRatioOption) {
        _imageState.value = _imageState.value.copy(selectedRatio = ratio)
    }

    fun generateImage() {
        val state = _imageState.value
        val prompt = state.prompt.trim()
        if (prompt.isEmpty() || state.isLoading) return

        _imageState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = repository.executeImageGeneration(
                prompt = prompt,
                model = state.selectedModel,
                aspectRatio = state.selectedRatio
            )
            result.onSuccess { creation ->
                _imageState.value = _imageState.value.copy(
                    isLoading = false,
                    currentResult = creation,
                    errorMessage = null
                )
            }.onFailure { error ->
                _imageState.value = _imageState.value.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Failed to generate image"
                )
            }
        }
    }

    // --- Video Studio Actions (Veo 3) ---

    fun setVideoPrompt(prompt: String) {
        _videoState.value = _videoState.value.copy(prompt = prompt)
    }

    fun setVideoAspectRatio(ratio: VeoAspectRatio) {
        _videoState.value = _videoState.value.copy(selectedRatio = ratio)
    }

    fun generateVideo() {
        val state = _videoState.value
        val prompt = state.prompt.trim()
        if (prompt.isEmpty() || state.isLoading) return

        _videoState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val result = repository.executeVideoGeneration(
                prompt = prompt,
                aspectRatio = state.selectedRatio
            )
            result.onSuccess { creation ->
                _videoState.value = _videoState.value.copy(
                    isLoading = false,
                    currentResult = creation,
                    errorMessage = null
                )
            }.onFailure { error ->
                _videoState.value = _videoState.value.copy(
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Failed to generate video with Veo 3"
                )
            }
        }
    }

    // --- Auth Actions ---

    fun signInWithGoogle(context: Context, webClientId: String = "") {
        viewModelScope.launch {
            repository.signInWithGoogle(context, webClientId)
            _showAuthDialog.value = false
        }
    }

    fun signInAsGuest() {
        viewModelScope.launch {
            repository.signInAnonymously()
            _showAuthDialog.value = false
        }
    }

    fun signOut() {
        repository.signOut()
    }

    // --- History / Persistence Actions ---

    fun toggleBookmark(item: CreationItem) {
        viewModelScope.launch {
            repository.toggleBookmark(item.id, !item.isBookmarked)
            if (_selectedCreationDetail.value?.id == item.id) {
                _selectedCreationDetail.value = item.copy(isBookmarked = !item.isBookmarked)
            }
        }
    }

    fun deleteCreation(item: CreationItem) {
        viewModelScope.launch {
            repository.deleteCreation(item.id)
            if (_selectedCreationDetail.value?.id == item.id) {
                _selectedCreationDetail.value = null
            }
        }
    }
}
