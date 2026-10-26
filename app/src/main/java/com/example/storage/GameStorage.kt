package com.example.storage

import android.content.Context
import com.example.model.GameSettings
import com.example.model.GameState
import com.example.model.SaveSummary
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

object GameStorage {
    private const val SAVE_FILE_NAME = "startup_empire_save_v1.json"
    private const val BACKUP_FILE_NAME = "startup_empire_save_backup.json"
    private const val PREFS_NAME = "startup_empire_prefs"
    private const val KEY_MUSIC_VOL = "music_volume"
    private const val KEY_SFX_VOL = "sfx_volume"
    private const val KEY_MUSIC_ENABLED = "music_enabled"
    private const val KEY_SFX_ENABLED = "sfx_enabled"
    private const val KEY_HAPTICS_ENABLED = "haptics_enabled"

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val adapter by lazy {
        moshi.adapter(GameState::class.java)
    }

    fun hasSaveGame(context: Context): Boolean {
        val file = File(context.filesDir, SAVE_FILE_NAME)
        val backupFile = File(context.filesDir, BACKUP_FILE_NAME)
        return (file.exists() && file.length() > 20) || (backupFile.exists() && backupFile.length() > 20)
    }

    fun getSaveSummary(context: Context): SaveSummary? {
        val file = File(context.filesDir, SAVE_FILE_NAME)
        val target = if (file.exists() && file.length() > 20) file else File(context.filesDir, BACKUP_FILE_NAME)
        if (!target.exists() || target.length() <= 20) return null

        return try {
            val json = target.readText()
            val state = adapter.fromJson(json) ?: return null
            SaveSummary(
                companyName = state.companyName,
                companyLevel = state.companyLevel,
                levelTitle = state.levelTitle,
                cash = state.cash,
                dateString = state.clock.formattedDate,
                logoIcon = state.companyLogoIcon,
                logoColor = state.companyLogoColor,
                savedAtTimestamp = target.lastModified()
            )
        } catch (e: Exception) {
            null
        }
    }

    fun saveGame(context: Context, state: GameState): Boolean {
        return try {
            val json = adapter.toJson(state)
            val file = File(context.filesDir, SAVE_FILE_NAME)
            val backupFile = File(context.filesDir, BACKUP_FILE_NAME)

            // If existing file is valid, backup first
            if (file.exists() && file.length() > 20) {
                file.copyTo(backupFile, overwrite = true)
            }

            // Write to temp file then rename for atomic safety
            val tempFile = File(context.filesDir, "$SAVE_FILE_NAME.tmp")
            tempFile.writeText(json)
            tempFile.renameTo(file)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun loadGame(context: Context): GameState {
        val file = File(context.filesDir, SAVE_FILE_NAME)
        val backupFile = File(context.filesDir, BACKUP_FILE_NAME)

        if (file.exists()) {
            try {
                val json = file.readText()
                val loaded = adapter.fromJson(json)
                if (loaded != null) return loaded
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Try backup file if main file failed
        if (backupFile.exists()) {
            try {
                val json = backupFile.readText()
                val loaded = adapter.fromJson(json)
                if (loaded != null) return loaded
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fresh default state
        return GameState()
    }

    fun deleteSave(context: Context): Boolean {
        return try {
            val file = File(context.filesDir, SAVE_FILE_NAME)
            val backup = File(context.filesDir, BACKUP_FILE_NAME)
            if (file.exists()) file.delete()
            if (backup.exists()) backup.delete()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun saveSettings(context: Context, settings: GameSettings) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(KEY_MUSIC_VOL, settings.musicVolume)
            .putFloat(KEY_SFX_VOL, settings.sfxVolume)
            .putBoolean(KEY_MUSIC_ENABLED, settings.isMusicEnabled)
            .putBoolean(KEY_SFX_ENABLED, settings.isSfxEnabled)
            .putBoolean(KEY_HAPTICS_ENABLED, settings.isHapticsEnabled)
            .apply()
    }

    fun loadSettings(context: Context): GameSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return GameSettings(
            musicVolume = prefs.getFloat(KEY_MUSIC_VOL, 0.65f),
            sfxVolume = prefs.getFloat(KEY_SFX_VOL, 0.85f),
            isMusicEnabled = prefs.getBoolean(KEY_MUSIC_ENABLED, true),
            isSfxEnabled = prefs.getBoolean(KEY_SFX_ENABLED, true),
            isHapticsEnabled = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        )
    }
}
