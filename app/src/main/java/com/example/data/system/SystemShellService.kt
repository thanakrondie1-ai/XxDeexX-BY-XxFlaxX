package com.example.data.system

import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service to execute system shell commands on the underlying device/container.
 */
class SystemShellService {

    /**
     * Executes a command and returns the output as a string.
     */
    suspend fun executeCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            process.waitFor()
            
            if (process.exitValue() == 0) {
                Result.success(output.toString().trim())
            } else {
                val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                val errorOutput = StringBuilder()
                while (errorReader.readLine().also { line = it } != null) {
                    errorOutput.append(line).append("\n")
                }
                Result.failure(Exception("Command exited with ${process.exitValue()}: ${errorOutput.toString().trim()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Specifically attempts to detect global Git configuration.
     */
    suspend fun detectGitConfig(): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val nameResult = executeCommand("git config --global user.name")
        val emailResult = executeCommand("git config --global user.email")
        
        if (nameResult.isSuccess && emailResult.isSuccess) {
            Result.success(Pair(nameResult.getOrDefault(""), emailResult.getOrDefault("")))
        } else {
            // Even if one fails, we might get partial info, but we report failure if we can't get both reliably
            Result.failure(Exception("Failed to detect git config. Ensure git is installed and configured."))
        }
    }
}
