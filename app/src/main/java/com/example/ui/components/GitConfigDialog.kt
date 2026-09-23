package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.GitCommandLog
import com.example.data.model.GitConfig
import com.example.data.model.GitConfigValidator
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import java.text.SimpleDateFormat
import java.util.*

/**
 * Reusable UI form with text fields for 'Git Username' and 'Git User Email',
 * Git alias configuration (`st`, `co`, `br`, `ci`), VS Code default editor setting,
 * and a transparent history log of executed Git setup commands.
 */
@Composable
fun GitConfigForm(
    currentConfig: GitConfig,
    onSave: (username: String, email: String) -> Boolean,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    showHeader: Boolean = true,
    onSaveExtended: ((username: String, email: String, aliases: Boolean, editor: Boolean) -> Boolean)? = null,
    onApplyAliases: (() -> Unit)? = null,
    onApplyEditor: (() -> Unit)? = null,
    onClearLogs: (() -> Unit)? = null,
    onAutoDetect: ((onResult: (String, String) -> Unit) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var username by remember(currentConfig) { mutableStateOf(currentConfig.username) }
    var email by remember(currentConfig) { mutableStateOf(currentConfig.email) }
    var enableAliases by remember(currentConfig) { mutableStateOf(currentConfig.aliasesEnabled) }
    var enableVsCodeEditor by remember(currentConfig) { mutableStateOf(currentConfig.isVsCodeEditor) }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var hasAttemptedSave by remember { mutableStateOf(false) }
    var isUsernameModified by remember { mutableStateOf(false) }
    var isEmailModified by remember { mutableStateOf(false) }
    var isDetecting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .testTag("git_config_form"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showHeader) {
            // Header Icon & Title
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(StudioPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Code,
                    contentDescription = "Git Icon",
                    tint = StudioPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Git Configuration & Setup",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configure author credentials, workflow aliases, default editor, and track executed commands.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Currently Saved Config preview banner if available
        if (currentConfig.username.isNotEmpty() || currentConfig.email.isNotEmpty() || currentConfig.aliasesEnabled || currentConfig.isVsCodeEditor) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = AccentEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (currentConfig.username.isNotEmpty()) "Active Author: ${currentConfig.username} <${currentConfig.email}>" else "Author not yet configured",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentConfig.aliasesEnabled) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Aliases Active (`st`, `co`, `br`, `ci`)", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = AccentEmerald)
                                }
                            )
                        }
                        if (currentConfig.isVsCodeEditor) {
                            AssistChip(
                                onClick = {},
                                label = { Text("VS Code Editor", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = AccentEmerald)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Section: Author Details
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "AUTHOR CREDENTIALS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = StudioPrimary
            )
            
            if (onAutoDetect != null) {
                TextButton(
                    onClick = {
                        isDetecting = true
                        onAutoDetect { detectedName, detectedEmail ->
                            isDetecting = false
                            if (detectedName.isNotEmpty()) {
                                username = detectedName
                                isUsernameModified = true
                                usernameError = GitConfigValidator.validateUsername(detectedName)
                            }
                            if (detectedEmail.isNotEmpty()) {
                                email = detectedEmail
                                isEmailModified = true
                                emailError = GitConfigValidator.validateEmail(detectedEmail)
                            }
                        }
                    },
                    enabled = !isDetecting,
                    modifier = Modifier.testTag("git_auto_detect_button")
                ) {
                    if (isDetecting) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Auto-Detect", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Git Username text field
        OutlinedTextField(
            value = username,
            onValueChange = { input ->
                username = input
                isUsernameModified = true
                usernameError = GitConfigValidator.validateUsername(input)
            },
            label = { Text("Git Username") },
            placeholder = { Text("e.g. Thanakron or octocat") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Git Username Icon",
                    tint = if (usernameError != null && (hasAttemptedSave || isUsernameModified)) MaterialTheme.colorScheme.error else StudioPrimary
                )
            },
            trailingIcon = {
                val showUsernameError = usernameError != null && (hasAttemptedSave || isUsernameModified)
                if (showUsernameError) {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                } else if (username.trim().isNotEmpty() && GitConfigValidator.validateUsername(username) == null) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Valid",
                        tint = AccentEmerald
                    )
                }
            },
            isError = usernameError != null && (hasAttemptedSave || isUsernameModified),
            supportingText = {
                val showUsernameError = usernameError != null && (hasAttemptedSave || isUsernameModified)
                if (showUsernameError) {
                    Text(
                        text = usernameError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("git_username_error")
                    )
                } else {
                    Text("Author name used for git commits (non-empty)")
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("git_username_input")
        )

        // Git User Email text field
        OutlinedTextField(
            value = email,
            onValueChange = { input ->
                email = input
                isEmailModified = true
                emailError = GitConfigValidator.validateEmail(input)
            },
            label = { Text("Git Email Address") },
            placeholder = { Text("e.g. thanakrondie1@gmail.com") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = "Git User Email Icon",
                    tint = if (emailError != null && (hasAttemptedSave || isEmailModified)) MaterialTheme.colorScheme.error else StudioPrimary
                )
            },
            trailingIcon = {
                val showEmailError = emailError != null && (hasAttemptedSave || isEmailModified)
                if (showEmailError) {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                } else if (email.trim().isNotEmpty() && GitConfigValidator.validateEmail(email) == null) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Valid",
                        tint = AccentEmerald
                    )
                }
            },
            isError = emailError != null && (hasAttemptedSave || isEmailModified),
            supportingText = {
                val showEmailError = emailError != null && (hasAttemptedSave || isEmailModified)
                if (showEmailError) {
                    Text(
                        text = emailError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("git_email_error")
                    )
                } else {
                    Text("Author email in valid email format (e.g. thanakrondie1@gmail.com)")
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("git_email_input")
        )

        // Section: Git Aliases Configuration
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("git_aliases_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(StudioSecondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Terminal,
                                contentDescription = null,
                                tint = StudioSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Git Common Aliases",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "st (status), co (checkout), br (branch), ci (commit)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = enableAliases,
                        onCheckedChange = { checked ->
                            enableAliases = checked
                            if (checked && onApplyAliases != null) {
                                onApplyAliases()
                            }
                        },
                        modifier = Modifier.testTag("git_aliases_switch")
                    )
                }

                // Aliases commands list preview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E1E24))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GitConfig.COMMON_ALIASES.forEach { (alias, target) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$ git config --global alias.$alias $target",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFFA0C0FF)
                                )
                            )
                            Icon(
                                Icons.Filled.ContentCopy,
                                contentDescription = "Copy alias $alias",
                                tint = Color(0xFF8090A0),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString("git config --global alias.$alias $target"))
                                        Toast.makeText(context, "Copied alias command", Toast.LENGTH_SHORT).show()
                                    }
                            )
                        }
                    }
                }

                // Copy All Aliases Button
                OutlinedButton(
                    onClick = {
                        val allCommands = GitConfig.aliasCommands().joinToString("\n")
                        clipboardManager.setText(AnnotatedString(allCommands))
                        Toast.makeText(context, "All 4 alias commands copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("copy_aliases_button")
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy All 4 Alias Commands", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Section: Visual Studio Code Default Editor
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("git_editor_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(StudioPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null,
                                tint = StudioPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Default Editor: VS Code",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Configure 'code --wait' for commits & interactive rebases",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = enableVsCodeEditor,
                        onCheckedChange = { checked ->
                            enableVsCodeEditor = checked
                            if (checked && onApplyEditor != null) {
                                onApplyEditor()
                            }
                        },
                        modifier = Modifier.testTag("git_editor_switch")
                    )
                }

                // Command Preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E1E24),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$ ${GitConfig.VS_CODE_EDITOR_CMD}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFFA0C0FF)
                            )
                        )
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "Copy editor command",
                            tint = Color(0xFF8090A0),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(GitConfig.VS_CODE_EDITOR_CMD))
                                    Toast.makeText(context, "Copied VS Code editor command", Toast.LENGTH_SHORT).show()
                                }
                        )
                    }
                }
            }
        }

        // Live Preview of Current Setup Commands
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E1E24),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "# Active configuration command preview",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFF8090A0)
                    )
                )
                Text(
                    text = "$ git config --global user.name \"${username.trim().ifEmpty { "your_username" }}\"",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFA0C0FF)
                    )
                )
                Text(
                    text = "$ git config --global user.email \"${email.trim().ifEmpty { "your_email" }}\"",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFA0C0FF)
                    )
                )
                if (enableAliases) {
                    Text(
                        text = "$ git config --global alias.st status (+ co, br, ci)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFA0C0FF)
                        )
                    )
                }
                if (enableVsCodeEditor) {
                    Text(
                        text = "$ ${GitConfig.VS_CODE_EDITOR_CMD}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFA0C0FF)
                        )
                    )
                }
            }
        }

        // Primary Action Buttons: Clear and Save Configuration
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Clear Button
            OutlinedButton(
                onClick = {
                    username = ""
                    email = ""
                    usernameError = null
                    emailError = null
                    hasAttemptedSave = false
                    enableAliases = false
                    enableVsCodeEditor = false
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(0.4f)
                    .height(50.dp)
                    .testTag("git_clear_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear")
            }

            // Save Button
            Button(
                onClick = {
                    hasAttemptedSave = true
                    isUsernameModified = true
                    isEmailModified = true
                    val uError = GitConfigValidator.validateUsername(username)
                    val eError = GitConfigValidator.validateEmail(email)

                    usernameError = uError
                    emailError = eError

                    if (uError == null && eError == null) {
                        val success = if (onSaveExtended != null) {
                            onSaveExtended(username.trim(), email.trim(), enableAliases, enableVsCodeEditor)
                        } else {
                            onSave(username.trim(), email.trim())
                        }

                        if (success) {
                            onDismiss?.invoke()
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                modifier = Modifier
                    .weight(0.6f)
                    .height(50.dp)
                    .testTag("git_save_button")
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save", fontWeight = FontWeight.Bold)
            }
        }

        // Section: Executed Git Commands History / Log
        GitCommandHistorySection(
            commandLogs = currentConfig.commandLogs,
            onClearLogs = onClearLogs,
            onCopyCommand = { cmd ->
                clipboardManager.setText(AnnotatedString(cmd))
                Toast.makeText(context, "Command copied to clipboard", Toast.LENGTH_SHORT).show()
            },
            onCopyAllLogs = {
                val fullLog = currentConfig.commandLogs.joinToString("\n") { it.command }
                clipboardManager.setText(AnnotatedString(fullLog))
                Toast.makeText(context, "Full command log copied", Toast.LENGTH_SHORT).show()
            }
        )

        if (onDismiss != null) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("git_cancel_button")
            ) {
                Text("Close")
            }
        }
    }
}

/**
 * Section displaying a transparent audit log and history of executed Git setup commands.
 */
@Composable
fun GitCommandHistorySection(
    commandLogs: List<GitCommandLog>,
    onClearLogs: (() -> Unit)?,
    onCopyCommand: (String) -> Unit,
    onCopyAllLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("git_command_history_section")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AccentEmerald.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.History,
                            contentDescription = null,
                            tint = AccentEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Executed Command History",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${commandLogs.size} command(s) logged for transparency",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (commandLogs.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onCopyAllLogs,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Filled.ContentCopy,
                                contentDescription = "Copy all logs",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (onClearLogs != null) {
                            IconButton(
                                onClick = onClearLogs,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Clear logs",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (commandLogs.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No executed commands logged yet.\nSave author credentials, aliases, or editor config to record execution history.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF141418))
                        .padding(10.dp)
                        .testTag("git_command_log_container"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    commandLogs.reversed().forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "[${dateFormat.format(Date(log.timestamp))}]",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = Color(0xFF8090A0)
                                    )
                                )
                                Text(
                                    text = "$ ${log.command}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = if (log.isSuccess) Color(0xFFA0E0B0) else Color(0xFFFF9090)
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { onCopyCommand(log.command) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Filled.ContentCopy,
                                    contentDescription = "Copy command",
                                    tint = Color(0xFF8090A0),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modal dialog for inputting Git configuration details and viewing setup command history.
 */
@Composable
fun GitConfigDialog(
    currentConfig: GitConfig,
    onDismiss: () -> Unit,
    onSave: (username: String, email: String) -> Boolean,
    modifier: Modifier = Modifier,
    onSaveExtended: ((username: String, email: String, aliases: Boolean, editor: Boolean) -> Boolean)? = null,
    onApplyAliases: (() -> Unit)? = null,
    onApplyEditor: (() -> Unit)? = null,
    onClearLogs: (() -> Unit)? = null,
    onAutoDetect: ((onResult: (String, String) -> Unit) -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.3f)),
            modifier = modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.9f)
                .padding(8.dp)
                .testTag("git_config_dialog")
        ) {
            GitConfigForm(
                currentConfig = currentConfig,
                onSave = onSave,
                onDismiss = onDismiss,
                showHeader = true,
                onSaveExtended = onSaveExtended,
                onApplyAliases = onApplyAliases,
                onApplyEditor = onApplyEditor,
                onClearLogs = onClearLogs,
                onAutoDetect = onAutoDetect,
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}
