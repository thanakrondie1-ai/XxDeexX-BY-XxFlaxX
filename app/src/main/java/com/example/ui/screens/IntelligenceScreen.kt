package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IntelligenceMode
import com.example.ui.components.GroundingMetadataCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.IntelligenceUiState

@Composable
fun IntelligenceScreen(
    state: IntelligenceUiState,
    onModeSelected: (IntelligenceMode) -> Unit,
    onPromptChanged: (String) -> Unit,
    onSubmit: () -> Unit,
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

    val promptSuggestions = remember(state.selectedMode) {
        when (state.selectedMode) {
            IntelligenceMode.HIGH_THINKING -> listOf(
                "Solve step-by-step: P vs NP complexity implications in quantum computing",
                "Prove whether all even numbers greater than 2 can be written as the sum of two primes",
                "Analyze game-theoretic equilibrium in autonomous driving multi-lane mergers"
            )
            IntelligenceMode.SEARCH_GROUNDING -> listOf(
                "What are the latest breakthroughs from the James Webb Space Telescope this month?",
                "Current status of global quantum supremacy benchmarks and newest research",
                "What were today's major global tech events and AI model announcements?"
            )
            IntelligenceMode.MAPS_GROUNDING -> listOf(
                "Find top-rated artisan bakeries and pour-over coffee spots in Shibuya, Tokyo",
                "Best scenic coastal viewpoints and hiking paths along the Big Sur California coast",
                "Recommend family-friendly historic landmarks and walking tour in Florence, Italy"
            )
            IntelligenceMode.LOW_LATENCY -> listOf(
                "Explain the difference between coroutines and threads in two punchy sentences",
                "Quick brainstorm 5 catchy names for a futuristic aerospace startup",
                "Fast syntax cheat-sheet for Kotlin Flow vs StateFlow"
            )
            IntelligenceMode.CONTENT_ANALYSIS -> listOf(
                "Analyze tone, identify logical fallacies, and suggest 3 persuasive improvements",
                "Summarize key takeaways, action items, and risk factors from project specs",
                "Refactor this Kotlin Composable logic for maximum state efficiency and cleanliness"
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector Carousel
        Column {
            Text(
                text = "XxFlaxX Intelligence Core",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IntelligenceMode.values().forEach { mode ->
                    val isSelected = mode == state.selectedMode
                    Surface(
                        onClick = { onModeSelected(mode) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) StudioPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) StudioPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("mode_tab_${mode.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = when (mode) {
                                    IntelligenceMode.HIGH_THINKING -> Icons.Filled.Psychology
                                    IntelligenceMode.SEARCH_GROUNDING -> Icons.Filled.Search
                                    IntelligenceMode.MAPS_GROUNDING -> Icons.Filled.Place
                                    IntelligenceMode.LOW_LATENCY -> Icons.Filled.Bolt
                                    IntelligenceMode.CONTENT_ANALYSIS -> Icons.Filled.Analytics
                                },
                                contentDescription = null,
                                tint = if (isSelected) StudioPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) StudioPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = mode.modelName,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Mode Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when (state.selectedMode) {
                                IntelligenceMode.HIGH_THINKING -> StudioPrimary
                                IntelligenceMode.SEARCH_GROUNDING -> StudioSecondary
                                IntelligenceMode.MAPS_GROUNDING -> AccentEmerald
                                IntelligenceMode.LOW_LATENCY -> AccentAmber
                                IntelligenceMode.CONTENT_ANALYSIS -> StudioTertiary
                            }.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (state.selectedMode) {
                            IntelligenceMode.HIGH_THINKING -> Icons.Filled.Psychology
                            IntelligenceMode.SEARCH_GROUNDING -> Icons.Filled.TravelExplore
                            IntelligenceMode.MAPS_GROUNDING -> Icons.Filled.Map
                            IntelligenceMode.LOW_LATENCY -> Icons.Filled.Speed
                            IntelligenceMode.CONTENT_ANALYSIS -> Icons.Filled.AutoFixHigh
                        },
                        contentDescription = null,
                        tint = when (state.selectedMode) {
                            IntelligenceMode.HIGH_THINKING -> StudioPrimary
                            IntelligenceMode.SEARCH_GROUNDING -> StudioSecondary
                            IntelligenceMode.MAPS_GROUNDING -> AccentEmerald
                            IntelligenceMode.LOW_LATENCY -> AccentAmber
                            IntelligenceMode.CONTENT_ANALYSIS -> StudioTertiary
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = state.selectedMode.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = StudioPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = state.selectedMode.badge,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = StudioPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = state.selectedMode.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Suggestions Row
        Column {
            Text(
                text = "Preset Prompts",
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
                promptSuggestions.forEach { suggestion ->
                    SuggestionChip(
                        onClick = { onPromptChanged(suggestion) },
                        label = {
                            Text(
                                text = suggestion,
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )
                }
            }
        }

        // Input Box
        OutlinedTextField(
            value = state.prompt,
            onValueChange = onPromptChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("intelligence_prompt_input"),
            label = { Text("Enter prompt for ${state.selectedMode.title}...") },
            placeholder = { Text("Ask a question, enter topics to research or verify...") },
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

        // Submit Button
        Button(
            onClick = onSubmit,
            enabled = state.prompt.isNotBlank() && !state.isLoading,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("intelligence_submit_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = StudioPrimary
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
                    text = when (state.selectedMode) {
                        IntelligenceMode.HIGH_THINKING -> "Reasoning deeply with Gemini 3.1 Pro..."
                        IntelligenceMode.SEARCH_GROUNDING -> "Searching live web citations..."
                        IntelligenceMode.MAPS_GROUNDING -> "Querying Google Maps grounding..."
                        IntelligenceMode.LOW_LATENCY -> "Generating instant response..."
                        IntelligenceMode.CONTENT_ANALYSIS -> "Analyzing content with Pro..."
                    },
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Icon(
                    imageVector = when (state.selectedMode) {
                        IntelligenceMode.HIGH_THINKING -> Icons.Filled.Psychology
                        IntelligenceMode.SEARCH_GROUNDING -> Icons.Filled.TravelExplore
                        IntelligenceMode.MAPS_GROUNDING -> Icons.Filled.LocationSearching
                        IntelligenceMode.LOW_LATENCY -> Icons.Filled.ElectricBolt
                        IntelligenceMode.CONTENT_ANALYSIS -> Icons.Filled.Analytics
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate with ${state.selectedMode.modelName}",
                    fontWeight = FontWeight.Bold
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

        // Results Section
        state.currentResult?.let { result ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                color = StudioPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = result.modelUsed,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            if (result.executionDurationMs > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "⏱ ${result.executionDurationMs}ms",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                result.resultText?.let {
                                    clipboardManager.setText(AnnotatedString(it))
                                    Toast.makeText(context, "Copied response to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy Text")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = result.resultText ?: "No response generated.",
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Grounding sources card if present
                    result.groundingMetadata?.let { metadata ->
                        if (metadata.sources.isNotEmpty() || metadata.webSearchQueries.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            GroundingMetadataCard(metadata = metadata)
                        }
                    }
                }
            }
        }
    }
}
