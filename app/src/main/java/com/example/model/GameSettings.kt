package com.example.model

data class GameSettings(
    val musicVolume: Float = 0.65f,
    val sfxVolume: Float = 0.85f,
    val isMusicEnabled: Boolean = true,
    val isSfxEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val selectedMusicTrack: Int = 0
)

data class SaveSummary(
    val companyName: String,
    val companyLevel: Int,
    val levelTitle: String,
    val cash: Double,
    val dateString: String,
    val logoIcon: String,
    val logoColor: Long,
    val savedAtTimestamp: Long
)
