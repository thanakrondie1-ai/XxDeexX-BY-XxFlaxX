package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.GitCommandLog
import com.example.data.model.GitConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Service responsible for persisting and retrieving user's Git configuration 
 * (username, email, aliases, editor settings) using Jetpack DataStore.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "git_settings")

class GitDataStoreService(private val context: Context) {

    private object PreferencesKeys {
        val GIT_USERNAME = stringPreferencesKey("git_username")
        val GIT_EMAIL = stringPreferencesKey("git_email")
        val GIT_ALIASES_ENABLED = booleanPreferencesKey("git_aliases_enabled")
        val GIT_VSCODE_EDITOR = booleanPreferencesKey("git_vscode_editor")
        val GIT_COMMAND_LOGS = stringPreferencesKey("git_command_logs")
    }

    /**
     * Flow of the current [GitConfig] state.
     */
    val gitConfigFlow: Flow<GitConfig> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val username = preferences[PreferencesKeys.GIT_USERNAME] ?: ""
            val email = preferences[PreferencesKeys.GIT_EMAIL] ?: ""
            val aliasesEnabled = preferences[PreferencesKeys.GIT_ALIASES_ENABLED] ?: false
            val isVsCodeEditor = preferences[PreferencesKeys.GIT_VSCODE_EDITOR] ?: false
            val logsJson = preferences[PreferencesKeys.GIT_COMMAND_LOGS] ?: "[]"
            
            val logs = mutableListOf<GitCommandLog>()
            try {
                val jsonArray = JSONArray(logsJson)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    logs.add(
                        GitCommandLog(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            command = obj.optString("command", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            isSuccess = obj.optBoolean("isSuccess", true)
                        )
                    )
                }
            } catch (e: Exception) {
                // Fail gracefully if JSON parsing fails
            }

            GitConfig(
                username = username,
                email = email,
                aliasesEnabled = aliasesEnabled,
                isVsCodeEditor = isVsCodeEditor,
                commandLogs = logs
            )
        }

    /**
     * Persists a new [GitConfig] to the DataStore.
     */
    suspend fun saveGitConfig(config: GitConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GIT_USERNAME] = config.username.trim()
            preferences[PreferencesKeys.GIT_EMAIL] = config.email.trim()
            preferences[PreferencesKeys.GIT_ALIASES_ENABLED] = config.aliasesEnabled
            preferences[PreferencesKeys.GIT_VSCODE_EDITOR] = config.isVsCodeEditor
            
            // Serialize command logs to JSON for simple string persistence in Preferences DataStore
            val jsonArray = JSONArray()
            config.commandLogs.takeLast(50).forEach { log ->
                val obj = JSONObject().apply {
                    put("id", log.id)
                    put("command", log.command)
                    put("timestamp", log.timestamp)
                    put("isSuccess", log.isSuccess)
                }
                jsonArray.put(obj)
            }
            preferences[PreferencesKeys.GIT_COMMAND_LOGS] = jsonArray.toString()
        }
    }

    /**
     * Resets the persisted Git configuration to default values.
     */
    suspend fun clearGitConfig() {
        context.dataStore.edit { it.clear() }
    }
}
