package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.model.GitCommandLog
import com.example.data.model.GitConfig
import com.example.ui.components.GitConfigForm
import com.example.ui.components.GitUserConfigForm
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GitConfigFormTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gitConfigForm_rendersTextFields_andSavesDetails() {
        var savedUsername = ""
        var savedEmail = ""
        var saveCalled = false

        composeTestRule.setContent {
            MyApplicationTheme {
                GitConfigForm(
                    currentConfig = GitConfig(username = "initial_user", email = "init@example.com"),
                    onSave = { u, e ->
                        savedUsername = u
                        savedEmail = e
                        saveCalled = true
                        true
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Verify form and text fields exist with appropriate tags
        composeTestRule.onNodeWithTag("git_config_form").assertExists()
        composeTestRule.onNodeWithTag("git_username_input").assertExists()
        composeTestRule.onNodeWithTag("git_email_input").assertExists()
        composeTestRule.onNodeWithTag("git_save_button").assertExists()

        // Verify text field contents and update with new details
        composeTestRule.onNodeWithTag("git_username_input").performScrollTo().performTextClearance()
        composeTestRule.onNodeWithTag("git_username_input").performTextInput("ThanakronDie1")

        composeTestRule.onNodeWithTag("git_email_input").performScrollTo().performTextClearance()
        composeTestRule.onNodeWithTag("git_email_input").performTextInput("ThanakronDie1@gmail.com")

        composeTestRule.waitForIdle()

        // Perform save
        composeTestRule.onNodeWithTag("git_save_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertTrue(saveCalled)
        assertEquals("ThanakronDie1", savedUsername)
        assertEquals("ThanakronDie1@gmail.com", savedEmail)
    }

    @Test
    fun gitConfigForm_displaysValidationErrors_onInvalidInput() {
        var saveCalled = false

        composeTestRule.setContent {
            MyApplicationTheme {
                GitConfigForm(
                    currentConfig = GitConfig(username = "", email = ""),
                    onSave = { _, _ ->
                        saveCalled = true
                        true
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Clear and type invalid email
        composeTestRule.onNodeWithTag("git_username_input").performScrollTo().performTextInput("ValidUser")
        composeTestRule.onNodeWithTag("git_email_input").performScrollTo().performTextInput("invalid-email-address")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("git_save_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Save should not be called due to invalid email format
        assertFalse(saveCalled)
        composeTestRule.onNodeWithTag("git_email_error", useUnmergedTree = true).performScrollTo().assertExists()
    }

    @Test
    fun gitConfigForm_showsRealTimeValidationFeedback_onTypingInvalidEmail() {
        composeTestRule.setContent {
            MyApplicationTheme {
                GitConfigForm(
                    currentConfig = GitConfig(username = "", email = ""),
                    onSave = { _, _ -> true }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Type invalid email in real-time without clicking save
        composeTestRule.onNodeWithTag("git_email_input").performScrollTo().performTextInput("not-an-email")
        composeTestRule.waitForIdle()

        // Verify error appears immediately before clicking save
        composeTestRule.onNodeWithTag("git_email_error", useUnmergedTree = true).performScrollTo().assertExists()
    }

    @Test
    fun gitConfigForm_rendersAliases_andEditorSections() {
        composeTestRule.setContent {
            MyApplicationTheme {
                GitConfigForm(
                    currentConfig = GitConfig(
                        username = "Thanakron",
                        email = "thanakron@example.com",
                        aliasesEnabled = true,
                        isVsCodeEditor = true
                    ),
                    onSave = { _, _ -> true }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Verify Aliases section and VS Code editor card exist
        composeTestRule.onNodeWithTag("git_aliases_card").assertExists()
        composeTestRule.onNodeWithTag("git_aliases_switch").assertExists()
        composeTestRule.onNodeWithTag("copy_aliases_button").assertExists()
        composeTestRule.onNodeWithTag("git_editor_card").assertExists()
        composeTestRule.onNodeWithTag("git_editor_switch").assertExists()
    }

    @Test
    fun gitConfigForm_displaysCommandHistoryLog() {
        val sampleLogs = listOf(
            GitCommandLog(command = "git config --global user.name \"Thanakron\""),
            GitCommandLog(command = "git config --global user.email \"thanakron@example.com\""),
            GitCommandLog(command = "git config --global core.editor \"code --wait\"")
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                GitConfigForm(
                    currentConfig = GitConfig(
                        username = "Thanakron",
                        email = "thanakron@example.com",
                        commandLogs = sampleLogs
                    ),
                    onSave = { _, _ -> true }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Verify history section and log container exist
        composeTestRule.onNodeWithTag("git_command_history_section").assertExists()
        composeTestRule.onNodeWithTag("git_command_log_container").assertExists()
    }

    @Test
    fun gitUserConfigForm_entersUsernameAndEmail_andClicksSave() {
        var savedUsername = ""
        var savedEmail = ""
        var saveCalled = false

        composeTestRule.setContent {
            MyApplicationTheme {
                GitUserConfigForm(
                    initialUsername = "",
                    initialEmail = "",
                    onSave = { u, e ->
                        savedUsername = u
                        savedEmail = e
                        saveCalled = true
                        true
                    }
                )
            }
        }

        composeTestRule.waitForIdle()

        // Verify form and text fields exist with appropriate tags
        composeTestRule.onNodeWithTag("git_user_config_form").assertExists()
        composeTestRule.onNodeWithTag("git_username_input").assertExists()
        composeTestRule.onNodeWithTag("git_email_input").assertExists()
        composeTestRule.onNodeWithTag("git_save_button").assertExists()

        // Enter Git Username and Email
        composeTestRule.onNodeWithTag("git_username_input").performTextInput("octocat")
        composeTestRule.onNodeWithTag("git_email_input").performTextInput("octocat@github.com")

        composeTestRule.waitForIdle()

        // Click Save button
        composeTestRule.onNodeWithTag("git_save_button").performClick()
        composeTestRule.waitForIdle()

        assertTrue(saveCalled)
        assertEquals("octocat", savedUsername)
        assertEquals("octocat@github.com", savedEmail)
    }
}
