package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VeoAspectRatio
import com.example.ui.components.VeoAspectRatioSelector
import com.example.ui.theme.*
import com.example.ui.viewmodel.VideoStudioUiState

@Composable
fun VideoStudioScreen(
    state: VideoStudioUiState,
    onPromptChanged: (String) -> Unit,
    onRatioSelected: (VeoAspectRatio) -> Unit,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val scrollState = rememberScrollState()

    DisposableEffect(Unit) {
        onDispose {
            focusManager.clearFocus()
        }
    }

    val cinematicPrompts = listOf(
        "Hyper-lapse of aurora borealis shimmering emerald across snow-capped Nordic peaks",
        "Cinematic slow-motion water droplet creating iridescent concentric ripple waves",
        "FPV drone swoop racing down through a neon cybernetic skyscraper canyon at midnight",
        "Golden hour cinematic camera tracking shot of an autumn forest canopy in breeze"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Veo 3 Header
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioSecondary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(StudioSecondary, StudioPrimary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MovieCreation,
                        contentDescription = "Veo 3",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Veo 3 Video Generation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = StudioSecondary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "veo-3.1-fast-generate-preview",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = StudioSecondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Next-generation video synthesis with 16:9 landscape & 9:16 portrait composition",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Mandatory Aspect Ratio Selector: 16:9 or 9:16
        VeoAspectRatioSelector(
            selectedRatio = state.selectedRatio,
            onRatioSelected = onRatioSelected
        )

        // Cinematic Presets
        Column {
            Text(
                text = "Cinematic Direction Ideas",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cinematicPrompts.forEach { promptText ->
                    SuggestionChip(
                        onClick = { onPromptChanged(promptText) },
                        label = {
                            Text(
                                text = promptText,
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )
                }
            }
        }

        // Prompt Input
        OutlinedTextField(
            value = state.prompt,
            onValueChange = onPromptChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("video_prompt_input"),
            label = { Text("Describe the cinematic video scene and motion...") },
            placeholder = { Text("E.g. Dynamic camera tracking an astronaut running on alien dune at sunset, 1080p cinematic lighting...") },
            trailingIcon = {
                if (state.prompt.isNotEmpty()) {
                    IconButton(onClick = { onPromptChanged("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                    }
                }
            },
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(16.dp)
        )

        // Generate Button
        Button(
            onClick = onGenerate,
            enabled = state.prompt.isNotBlank() && !state.isLoading,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("video_generate_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = StudioSecondary
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Initiating Veo 3 Video Synthesis...",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            } else {
                Icon(Icons.Filled.Videocam, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Synthesize ${state.selectedRatio.ratioString} Video",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Error message
        if (state.errorMessage != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Filled.ErrorOutline,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Video Output / Operation Tracker Card
        state.currentResult?.let { result ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioSecondary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StudioSecondary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Veo 3.1 Fast",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            result.aspectRatio?.let { ratio ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "📐 $ratio",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(result.prompt))
                                Toast.makeText(context, "Copied prompt to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy Prompt")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Video Frame Simulated Player with Exact Aspect Ratio
                    val isPortrait = result.aspectRatio == "9:16"
                    val aspectRatioValue = if (isPortrait) 9f / 16f else 16f / 9f

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspectRatioValue)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E2135), Color(0xFF0F101A))
                                )
                            )
                            .border(1.dp, StudioSecondary.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(StudioSecondary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Play Video",
                                    tint = StudioSecondary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Veo 3 Video Preview",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "${result.aspectRatio ?: "16:9"} High-Fidelity Rendering",
                                style = MaterialTheme.typography.bodySmall,
                                color = StudioSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!result.videoOperationName.isNullOrEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Sync,
                                    contentDescription = null,
                                    tint = StudioSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Operation: ${result.videoOperationName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = result.prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
