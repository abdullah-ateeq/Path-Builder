package com.example.pathbuilder.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode(val label: String) { LIGHT("Light"), DARK("Dark"), SYSTEM("System") }

enum class BoardTheme(val label: String) { CLASSIC("Classic"), MIDNIGHT("Midnight") }

data class GameSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val boardTheme: BoardTheme = BoardTheme.CLASSIC,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val showConnectedPath: Boolean = true,
    val showTimer: Boolean = true,
)

data class GameStats(
    val gamesPlayed: Int = 0,
    val routesCompleted: Int = 0,
    val bestMoves: Int? = null,
    val bestTimeMillis: Long? = null,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "path_builder")

class SettingsRepository(context: Context) {

    private val store = context.applicationContext.dataStore

    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val boardTheme = stringPreferencesKey("board_theme")
        val sound = booleanPreferencesKey("sound")
        val haptics = booleanPreferencesKey("haptics")
        val showConnected = booleanPreferencesKey("show_connected")
        val showTimer = booleanPreferencesKey("show_timer")
        val gamesPlayed = intPreferencesKey("games_played")
        val routesCompleted = intPreferencesKey("routes_completed")
        val bestMoves = intPreferencesKey("best_moves")
        val bestTime = longPreferencesKey("best_time")
    }

    val settings: Flow<GameSettings> = store.data.map { p ->
        GameSettings(
            themeMode = p[Keys.themeMode].toEnum(ThemeMode.SYSTEM),
            boardTheme = p[Keys.boardTheme].toEnum(BoardTheme.CLASSIC),
            soundEnabled = p[Keys.sound] ?: true,
            hapticsEnabled = p[Keys.haptics] ?: true,
            showConnectedPath = p[Keys.showConnected] ?: true,
            showTimer = p[Keys.showTimer] ?: true,
        )
    }

    val stats: Flow<GameStats> = store.data.map { p ->
        GameStats(
            gamesPlayed = p[Keys.gamesPlayed] ?: 0,
            routesCompleted = p[Keys.routesCompleted] ?: 0,
            bestMoves = p[Keys.bestMoves],
            bestTimeMillis = p[Keys.bestTime],
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = store.edit { it[Keys.themeMode] = mode.name }
    suspend fun setBoardTheme(theme: BoardTheme) = store.edit { it[Keys.boardTheme] = theme.name }
    suspend fun setSound(enabled: Boolean) = store.edit { it[Keys.sound] = enabled }
    suspend fun setHaptics(enabled: Boolean) = store.edit { it[Keys.haptics] = enabled }
    suspend fun setShowConnectedPath(enabled: Boolean) = store.edit { it[Keys.showConnected] = enabled }
    suspend fun setShowTimer(enabled: Boolean) = store.edit { it[Keys.showTimer] = enabled }

    suspend fun recordGame(won: Boolean, moves: Int, timeMillis: Long) {
        store.edit { p ->
            p[Keys.gamesPlayed] = (p[Keys.gamesPlayed] ?: 0) + 1
            if (won) {
                p[Keys.routesCompleted] = (p[Keys.routesCompleted] ?: 0) + 1
                p[Keys.bestMoves] = minOf(p[Keys.bestMoves] ?: Int.MAX_VALUE, moves)
                p[Keys.bestTime] = minOf(p[Keys.bestTime] ?: Long.MAX_VALUE, timeMillis)
            }
        }
    }

    suspend fun resetStats() {
        store.edit { p ->
            p.remove(Keys.gamesPlayed)
            p.remove(Keys.routesCompleted)
            p.remove(Keys.bestMoves)
            p.remove(Keys.bestTime)
        }
    }
}

private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
    this?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default
