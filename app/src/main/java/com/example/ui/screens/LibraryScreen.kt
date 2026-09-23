package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CreationItem
import com.example.data.model.GitConfig
import com.example.ui.components.RenderedBase64Image
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LibraryScreen(
    creations: List<CreationItem>,
    currentFilter: String,
    gitConfig: GitConfig = GitConfig(),
    onOpenGitConfig: () -> Unit = {},
    onFilterChanged: (String) -> Unit,
    onItemClick: (CreationItem) -> Unit,
    onToggleBookmark: (CreationItem) -> Unit,
    onDeleteItem: (CreationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val filteredCreations = remember(creations, currentFilter) {
        when (currentFilter) {
            "INTELLIGENCE" -> creations.filter { it.category == "INTELLIGENCE" }
            "IMAGE" -> creations.filter { it.category == "IMAGE" }
            "VIDEO" -> creations.filter { it.category == "VIDEO" }
            "SAVED" -> creations.filter { it.isBookmarked }
            else -> creations
        }
    }

    val filters = listOf(
        "ALL" to "All (${creations.size})",
        "INTELLIGENCE" to "Intelligence",
        "IMAGE" to "Images",
        "VIDEO" to "Veo Videos",
        "SAVED" to "Bookmarks"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Git Author Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (gitConfig.username.isNotEmpty()) AccentEmerald.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .clickable { onOpenGitConfig() }
                .testTag("git_config_card")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (gitConfig.username.isNotEmpty()) AccentEmerald.copy(alpha = 0.15f)
                            else StudioPrimary.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Code,
                        contentDescription = "Git Config",
                        tint = if (gitConfig.username.isNotEmpty()) AccentEmerald else StudioPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (gitConfig.username.isNotEmpty()) "Git: ${gitConfig.username}" else "Git Configuration",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val summary = buildString {
                        if (gitConfig.email.isNotEmpty()) append(gitConfig.email)
                        if (gitConfig.aliasesEnabled) append(" • Aliases")
                        if (gitConfig.isVsCodeEditor) append(" • VS Code")
                        if (gitConfig.commandLogs.isNotEmpty()) append(" • ${gitConfig.commandLogs.size} logs")
                    }.ifEmpty { "Tap to configure author, aliases, editor & view logs" }
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                FilledTonalButton(
                    onClick = onOpenGitConfig,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("configure_git_button")
                ) {
                    Text(
                        text = if (gitConfig.username.isNotEmpty()) "Edit" else "Configure",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // Filter Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { (key, label) ->
                val isSelected = currentFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterChanged(key) },
                    label = { Text(label) },
                    leadingIcon = if (key == "SAVED") {
                        { Icon(Icons.Filled.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StudioPrimary.copy(alpha = 0.18f),
                        selectedLabelColor = StudioPrimary
                    )
                )
            }
        }

        if (filteredCreations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = "No creations found in this view",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Your queries, images & Veo videos will be synced to Firestore here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredCreations, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(item) }
                            .testTag("creation_item_${item.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = when (item.category) {
                                            "IMAGE" -> Icons.Filled.Image
                                            "VIDEO" -> Icons.Filled.Movie
                                            else -> Icons.Filled.Psychology
                                        },
                                        contentDescription = null,
                                        tint = when (item.category) {
                                            "IMAGE" -> StudioPrimary
                                            "VIDEO" -> StudioSecondary
                                            else -> AccentEmerald
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = item.modelUsed,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (item.aspectRatio != null) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = item.aspectRatio,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onToggleBookmark(item) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (item.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Bookmark",
                                            tint = if (item.isBookmarked) AccentAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteItem(item) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.prompt,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (!item.imageBase64.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                RenderedBase64Image(
                                    base64String = item.imageBase64,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                )
                            } else if (!item.resultText.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.resultText,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(item.timestamp)),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
