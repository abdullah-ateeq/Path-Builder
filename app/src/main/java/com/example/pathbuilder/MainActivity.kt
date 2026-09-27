package com.example.pathbuilder

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pathbuilder.data.GameSettings
import com.example.pathbuilder.data.SettingsRepository
import com.example.pathbuilder.feedback.LocalGameFeedback
import com.example.pathbuilder.feedback.rememberGameFeedback
import com.example.pathbuilder.navigation.PathBuilderNavHost
import com.example.pathbuilder.ui.theme.PathBuilderTheme
import com.example.pathbuilder.ui.theme.isAppInDarkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = SettingsRepository(applicationContext)
        setContent {
            val settings by repository.settings.collectAsStateWithLifecycle(GameSettings())
            val dark = isAppInDarkTheme(settings.themeMode)
            DisposableEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }
            PathBuilderTheme(themeMode = settings.themeMode, boardTheme = settings.boardTheme) {
                val feedback = rememberGameFeedback(settings)
                CompositionLocalProvider(LocalGameFeedback provides feedback) {
                    PathBuilderNavHost(settings = settings, repository = repository)
                }
            }
        }
    }
}
