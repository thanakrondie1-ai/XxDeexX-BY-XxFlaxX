package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GitConfigValidator
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.StudioPrimary

/**
 * Reusable Compose UI form providing text input fields for the user to enter
 * their Git username and email address, along with a 'Save' button.
 */
@Composable
fun GitUserConfigForm(
    initialUsername: String = "",
    initialEmail: String = "",
    onSave: (username: String, email: String) -> Boolean,
    onAutoDetect: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    val context = LocalContext.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    
    DisposableEffect(Unit) {
        onDispose {
            focusManager.clearFocus()
        }
    }

    var username by remember(initialUsername) { mutableStateOf(initialUsername) }
    var email by remember(initialEmail) { mutableStateOf(initialEmail) }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var hasAttemptedSave by remember { mutableStateOf(false) }
    var isUsernameModified by remember { mutableStateOf(false) }
    var isEmailModified by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("git_user_config_form"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (title != null || onAutoDetect != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (onAutoDetect != null) {
                    TextButton(
                        onClick = onAutoDetect,
                        modifier = Modifier.testTag("git_auto_detect_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Auto-Detect",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                        contentDescription = "Username Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                } else if (username.trim().isNotEmpty() && GitConfigValidator.validateUsername(username) == null) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Valid Username",
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

        // Git Email Address text field
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
                    contentDescription = "Git Email Icon",
                    tint = if (emailError != null && (hasAttemptedSave || isEmailModified)) MaterialTheme.colorScheme.error else StudioPrimary
                )
            },
            trailingIcon = {
                val showEmailError = emailError != null && (hasAttemptedSave || isEmailModified)
                if (showEmailError) {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = "Email Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                } else if (email.trim().isNotEmpty() && GitConfigValidator.validateEmail(email) == null) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Valid Email",
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
                    Text("Author email in valid email format (e.g. name@example.com)")
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("git_email_input")
        )

        // Command Live Preview
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF1E1E24),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
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
            }
        }

        // Action Buttons: Clear and Save
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
                    val uError = GitConfigValidator.validateUsername(username)
                    val eError = GitConfigValidator.validateEmail(email)

                    usernameError = uError
                    emailError = eError

                    if (uError == null && eError == null) {
                        val success = onSave(username.trim(), email.trim())
                        if (success) {
                            Toast.makeText(
                                context,
                                "Git configuration saved successfully",
                                Toast.LENGTH_SHORT
                            ).show()
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
    }
}
