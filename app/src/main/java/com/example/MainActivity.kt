package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CashflowApp
import com.example.ui.theme.KasFlowTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePreferences
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by ThemePreferences.observeThemeMode(applicationContext)
                .collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            val scope = rememberCoroutineScope()

            DisposableEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
                onDispose {}
            }

            KasFlowTheme(darkTheme = isDark) {
                CashflowApp(
                    themeMode = themeMode,
                    onToggleTheme = {
                        val nextMode = if (isDark) ThemeMode.LIGHT else ThemeMode.DARK
                        scope.launch {
                            ThemePreferences.setThemeMode(applicationContext, nextMode)
                        }
                    },
                    onSelectThemeMode = { selectedMode ->
                        scope.launch {
                            ThemePreferences.setThemeMode(applicationContext, selectedMode)
                        }
                    }
                )
            }
        }
    }
}
