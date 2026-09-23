package com.example

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainStudioScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request 120Hz Refresh Rate (Screen 120 Hz Update)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = this.display
            if (display != null) {
                window.attributes.preferredDisplayModeId = display.supportedModes
                    .filter { mode -> mode.refreshRate >= 119f }
                    .maxByOrNull { mode -> mode.refreshRate }?.modeId ?: 0
            }
        }

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val studioViewModel: StudioViewModel = viewModel()
                    MainStudioScreen(viewModel = studioViewModel)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
