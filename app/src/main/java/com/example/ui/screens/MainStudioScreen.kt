package com.example.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.ui.components.AuthDialog
import com.example.ui.components.CreationDetailDialog
import com.example.ui.components.GitConfigDialog
import com.example.ui.components.StudioTopBar
import com.example.ui.theme.StudioPrimary
import com.example.ui.viewmodel.StudioTab
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun MainStudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val userProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val showAuthDialog by viewModel.showAuthDialog.collectAsStateWithLifecycle()
    val gitConfig by viewModel.gitConfig.collectAsStateWithLifecycle()
    val showGitConfigDialog by viewModel.showGitConfigDialog.collectAsStateWithLifecycle()
    val selectedDetail by viewModel.selectedCreationDetail.collectAsStateWithLifecycle()

    val intelligenceState by viewModel.intelligenceState.collectAsStateWithLifecycle()
    val imageState by viewModel.imageState.collectAsStateWithLifecycle()
    val videoState by viewModel.videoState.collectAsStateWithLifecycle()
    val creationsList by viewModel.creationsList.collectAsStateWithLifecycle()
    val historyFilter by viewModel.historyFilter.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StudioTopBar(
                userProfile = userProfile,
                gitConfig = gitConfig,
                onAuthClick = { viewModel.setAuthDialogVisible(true) },
                onGitConfigClick = { viewModel.setGitConfigDialogVisible(true) }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == StudioTab.INTELLIGENCE,
                    onClick = { viewModel.selectTab(StudioTab.INTELLIGENCE) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == StudioTab.INTELLIGENCE) Icons.Filled.Psychology else Icons.Outlined.Psychology,
                            contentDescription = "Intelligence"
                        )
                    },
                    label = { Text("Intelligence") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = StudioPrimary,
                        indicatorColor = StudioPrimary.copy(alpha = 0.18f)
                    ),
                    modifier = Modifier.testTag("nav_intelligence")
                )

                NavigationBarItem(
                    selected = currentTab == StudioTab.IMAGE_STUDIO,
                    onClick = { viewModel.selectTab(StudioTab.IMAGE_STUDIO) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == StudioTab.IMAGE_STUDIO) Icons.Filled.Image else Icons.Outlined.Image,
                            contentDescription = "Image Studio"
                        )
                    },
                    label = { Text("Image Studio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = StudioPrimary,
                        indicatorColor = StudioPrimary.copy(alpha = 0.18f)
                    ),
                    modifier = Modifier.testTag("nav_image_studio")
                )

                NavigationBarItem(
                    selected = currentTab == StudioTab.VIDEO_STUDIO,
                    onClick = { viewModel.selectTab(StudioTab.VIDEO_STUDIO) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == StudioTab.VIDEO_STUDIO) Icons.Filled.MovieCreation else Icons.Outlined.MovieCreation,
                            contentDescription = "Veo 3 Video"
                        )
                    },
                    label = { Text("Veo 3 Video") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = StudioPrimary,
                        indicatorColor = StudioPrimary.copy(alpha = 0.18f)
                    ),
                    modifier = Modifier.testTag("nav_video_studio")
                )

                NavigationBarItem(
                    selected = currentTab == StudioTab.LIBRARY,
                    onClick = { viewModel.selectTab(StudioTab.LIBRARY) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (creationsList.isNotEmpty()) {
                                    Badge { Text("${creationsList.size}") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == StudioTab.LIBRARY) Icons.Filled.FolderSpecial else Icons.Outlined.FolderSpecial,
                                contentDescription = "Library"
                            )
                        }
                    },
                    label = { Text("Library") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = StudioPrimary,
                        indicatorColor = StudioPrimary.copy(alpha = 0.18f)
                    ),
                    modifier = Modifier.testTag("nav_library")
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            StudioTab.INTELLIGENCE -> {
                IntelligenceScreen(
                    state = intelligenceState,
                    onModeSelected = { viewModel.setIntelligenceMode(it) },
                    onPromptChanged = { viewModel.setIntelligencePrompt(it) },
                    onSubmit = { viewModel.runIntelligenceQuery() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            StudioTab.IMAGE_STUDIO -> {
                ImageStudioScreen(
                    state = imageState,
                    onPromptChanged = { viewModel.setImagePrompt(it) },
                    onModelSelected = { viewModel.setImageModel(it) },
                    onRatioSelected = { viewModel.setImageAspectRatio(it) },
                    onGenerate = { viewModel.generateImage() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            StudioTab.VIDEO_STUDIO -> {
                VideoStudioScreen(
                    state = videoState,
                    onPromptChanged = { viewModel.setVideoPrompt(it) },
                    onRatioSelected = { viewModel.setVideoAspectRatio(it) },
                    onGenerate = { viewModel.generateVideo() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            StudioTab.LIBRARY -> {
                LibraryScreen(
                    creations = creationsList,
                    currentFilter = historyFilter,
                    gitConfig = gitConfig,
                    onOpenGitConfig = { viewModel.setGitConfigDialogVisible(true) },
                    onFilterChanged = { viewModel.setHistoryFilter(it) },
                    onItemClick = { viewModel.selectCreationDetail(it) },
                    onToggleBookmark = { viewModel.toggleBookmark(it) },
                    onDeleteItem = { viewModel.deleteCreation(it) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        // Dialogs
        if (showAuthDialog) {
            AuthDialog(
                currentUser = userProfile,
                onDismiss = { viewModel.setAuthDialogVisible(false) },
                onSignInWithGoogle = { viewModel.signInWithGoogle(context) },
                onSignInAsGuest = { viewModel.signInAsGuest() },
                onSignOut = { viewModel.signOut() },
                onOpenGitConfig = { viewModel.setGitConfigDialogVisible(true) }
            )
        }

        if (showGitConfigDialog) {
            GitConfigDialog(
                currentConfig = gitConfig,
                onDismiss = { viewModel.setGitConfigDialogVisible(false) },
                onSave = { username, email ->
                    val result = viewModel.saveGitConfig(username, email)
                    if (result.isSuccess) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Git configuration saved successfully")
                        }
                    }
                    result.isSuccess
                },
                onSaveExtended = { username, email, aliases, editor ->
                    val result = viewModel.saveGitConfig(username, email, aliases, editor)
                    if (result.isSuccess) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Git configuration and extended options saved")
                        }
                    }
                    result.isSuccess
                },
                onApplyAliases = {
                    viewModel.applyGitAliases()
                    scope.launch {
                        snackbarHostState.showSnackbar("Git aliases configured and logged")
                    }
                },
                onApplyEditor = {
                    viewModel.applyVsCodeEditor()
                    scope.launch {
                        snackbarHostState.showSnackbar("VS Code editor configured and logged")
                    }
                },
                onClearLogs = {
                    viewModel.clearCommandLogs()
                },
                onAutoDetect = { onResult ->
                    viewModel.detectSystemGitConfig { pair, error ->
                        if (error != null) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Detection failed: $error")
                            }
                            onResult("", "")
                        } else if (pair != null) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Detected credentials from system")
                            }
                            onResult(pair.first, pair.second)
                        }
                    }
                }
            )
        }

        selectedDetail?.let { item ->
            CreationDetailDialog(
                item = item,
                onDismiss = { viewModel.selectCreationDetail(null) },
                onToggleBookmark = { viewModel.toggleBookmark(item) },
                onDelete = { viewModel.deleteCreation(item) }
            )
        }
    }
}
