package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatioOption
import com.example.data.model.ImageStudioModel
import com.example.ui.components.AspectRatioSelector
import com.example.ui.components.RenderedBase64Image
import com.example.ui.theme.*
import com.example.ui.viewmodel.ImageStudioUiState

@Composable
fun ImageStudioScreen(
    state: ImageStudioUiState,
    onPromptChanged: (String) -> Unit,
    onModelSelected: (ImageStudioModel) -> Unit,
    onRatioSelected: (AspectRatioOption) -> Unit,
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

    val creativePrompts = listOf(
        "Bioluminescent neon cybernetic lotus blooming in obsidian water, iridescent cinematic lighting",
        "Mid-century modern architectural villa hanging over a tranquil alpine lake at golden sunset",
        "Macro crystal prism reflecting vibrant prismatic light rays, photorealistic 8K render",
        "Futuristic astronaut wandering through an ancient moss-covered alien temple, ethereal haze"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Model Selector
        Column {
            Text(
                text = "Image Generation Model",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ImageStudioModel.values().forEach { model ->
                    val isSelected = model == state.selectedModel
                    Surface(
                        onClick = { onModelSelected(model) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) StudioPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) StudioPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("model_${model.name.lowercase()}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (model == ImageStudioModel.PRO_IMAGE) Icons.Filled.Diamond else Icons.Filled.FlashOn,
                                    contentDescription = null,
                                    tint = if (isSelected) StudioPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (model == ImageStudioModel.PRO_IMAGE) "Pro Image" else "Flash Image",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) StudioPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = model.description,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Aspect Ratio Selector (Mandatory 1:1, 2:3, 3:2, 3:4, 4:3, 9:16, 16:9, 21:9)
        AspectRatioSelector(
            selectedRatio = state.selectedRatio,
            onRatioSelected = onRatioSelected
        )

        // Preset Inspiration
        Column {
            Text(
                text = "Creative Inspiration",
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
                creativePrompts.forEach { promptText ->
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
                .testTag("image_prompt_input"),
            label = { Text("Describe the visual scene in detail...") },
            placeholder = { Text("E.g. A serene Japanese zen garden in autumn mist, glowing lantern reflections, 4K...") },
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
                .testTag("image_generate_button"),
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
                    text = "Generating ${state.selectedRatio.ratioString} Visual with ${state.selectedModel.displayName}...",
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Generate in ${state.selectedRatio.ratioString} Ratio",
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

        // Rendered Output
        state.currentResult?.let { result ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.3f)),
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
                            result.aspectRatio?.let { ratio ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = StudioSecondary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "📐 $ratio",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(result.prompt))
                                Toast.makeText(context, "Prompt copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy Prompt")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!result.imageBase64.isNullOrEmpty()) {
                        RenderedBase64Image(
                            base64String = result.imageBase64,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

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
