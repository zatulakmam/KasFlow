package com.example.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode(val label: String) {
    SYSTEM("Sistem"),
    LIGHT("Terang"),
    DARK("Gelap");

    companion object {
        fun fromRaw(raw: String?): ThemeMode =
            entries.firstOrNull { it.name == raw } ?: SYSTEM
    }
}

private val Context.themeDataStore by preferencesDataStore(name = "kasflow_theme_prefs")

object ThemePreferences {
    private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")

    fun observeThemeMode(context: Context): Flow<ThemeMode> =
        context.applicationContext.themeDataStore.data.map { prefs ->
            ThemeMode.fromRaw(prefs[KEY_THEME_MODE])
        }

    suspend fun setThemeMode(context: Context, mode: ThemeMode) {
        context.applicationContext.themeDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }
}
