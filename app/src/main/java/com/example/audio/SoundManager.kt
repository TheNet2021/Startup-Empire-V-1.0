package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import android.util.Log
import com.example.model.GameSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

object SoundManager {
    private const val TAG = "SoundManager"
    private const val SAMPLE_RATE = 22050

    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isInitialized = false

    private var soundClickId: Int = 0
    private var soundConfirmId: Int = 0
    private var soundCashId: Int = 0
    private var soundErrorId: Int = 0
    private var soundHireId: Int = 0
    private var soundLaunchId: Int = 0

    private var musicVolume: Float = 0.65f
    private var sfxVolume: Float = 0.85f
    private var isMusicEnabled: Boolean = true
    private var isSfxEnabled: Boolean = true

    fun initialize(context: Context, settings: GameSettings) {
        if (isInitialized) {
            updateSettings(settings)
            return
        }

        musicVolume = settings.musicVolume
        sfxVolume = settings.sfxVolume
        isMusicEnabled = settings.isMusicEnabled
        isSfxEnabled = settings.isSfxEnabled

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cacheDir = context.cacheDir
                val audioDir = File(cacheDir, "audio_assets").apply { mkdirs() }

                // 1. Synthesize SFX WAV files
                val clickFile = File(audioDir, "sfx_click.wav")
                if (!clickFile.exists()) clickFile.writeBytes(generateClickWav())

                val confirmFile = File(audioDir, "sfx_confirm.wav")
                if (!confirmFile.exists()) confirmFile.writeBytes(generateConfirmWav())

                val cashFile = File(audioDir, "sfx_cash.wav")
                if (!cashFile.exists()) cashFile.writeBytes(generateCashWav())

                val errorFile = File(audioDir, "sfx_error.wav")
                if (!errorFile.exists()) errorFile.writeBytes(generateErrorWav())

                val hireFile = File(audioDir, "sfx_hire.wav")
                if (!hireFile.exists()) hireFile.writeBytes(generateHireWav())

                val launchFile = File(audioDir, "sfx_launch.wav")
                if (!launchFile.exists()) launchFile.writeBytes(generateLaunchWav())

                // 2. Synthesize ambient Lo-Fi BGM WAV file
                val bgmFile = File(audioDir, "bgm_ambient.wav")
                if (!bgmFile.exists()) bgmFile.writeBytes(generateAmbientBgmWav())

                // 3. Setup SoundPool
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                soundPool = SoundPool.Builder()
                    .setMaxStreams(8)
                    .setAudioAttributes(attrs)
                    .build().apply {
                        soundClickId = load(clickFile.absolutePath, 1)
                        soundConfirmId = load(confirmFile.absolutePath, 1)
                        soundCashId = load(cashFile.absolutePath, 1)
                        soundErrorId = load(errorFile.absolutePath, 1)
                        soundHireId = load(hireFile.absolutePath, 1)
                        soundLaunchId = load(launchFile.absolutePath, 1)
                    }

                // 4. Setup MediaPlayer for background music
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setDataSource(context, Uri.fromFile(bgmFile))
                    isLooping = true
                    prepare()
                    val actualVol = if (isMusicEnabled) musicVolume else 0f
                    setVolume(actualVol, actualVol)
                    if (isMusicEnabled) {
                        start()
                    }
                }

                isInitialized = true
                Log.d(TAG, "SoundManager initialized successfully with synthesized audio assets.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize SoundManager", e)
            }
        }
    }

    fun updateSettings(settings: GameSettings) {
        musicVolume = settings.musicVolume
        sfxVolume = settings.sfxVolume
        isMusicEnabled = settings.isMusicEnabled
        isSfxEnabled = settings.isSfxEnabled

        try {
            mediaPlayer?.let { player ->
                val actualVol = if (isMusicEnabled) musicVolume else 0f
                player.setVolume(actualVol, actualVol)
                if (isMusicEnabled && !player.isPlaying) {
                    player.start()
                } else if (!isMusicEnabled && player.isPlaying) {
                    player.pause()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating music settings", e)
        }
    }

    fun playClick() {
        if (!isSfxEnabled) return
        soundPool?.play(soundClickId, sfxVolume, sfxVolume, 1, 0, 1.0f)
    }

    fun playConfirm() {
        if (!isSfxEnabled) return
        soundPool?.play(soundConfirmId, sfxVolume, sfxVolume, 2, 0, 1.0f)
    }

    fun playCash() {
        if (!isSfxEnabled) return
        soundPool?.play(soundCashId, sfxVolume, sfxVolume, 3, 0, 1.0f)
    }

    fun playError() {
        if (!isSfxEnabled) return
        soundPool?.play(soundErrorId, sfxVolume, sfxVolume, 3, 0, 1.0f)
    }

    fun playHire() {
        if (!isSfxEnabled) return
        soundPool?.play(soundHireId, sfxVolume, sfxVolume, 4, 0, 1.0f)
    }

    fun playLaunch() {
        if (!isSfxEnabled) return
        soundPool?.play(soundLaunchId, sfxVolume, sfxVolume, 5, 0, 1.0f)
    }

    fun onResume() {
        try {
            if (isMusicEnabled && mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming MediaPlayer", e)
        }
    }

    fun onPause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing MediaPlayer", e)
        }
    }

    fun release() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            soundPool?.release()
            soundPool = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio resources", e)
        }
    }

    // =========================================================================
    // PROCEDURAL AUDIO SYNTHESIZERS (Pure Math, 100% Offline & Original)
    // =========================================================================

    private fun createWavBytes(samples: ShortArray): ByteArray {
        val numBytes = samples.size * 2
        val totalSize = 36 + numBytes
        val buffer = ByteBuffer.allocate(44 + numBytes).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put("RIFF".toByteArray())
        buffer.putInt(totalSize)
        buffer.put("WAVE".toByteArray())

        buffer.put("fmt ".toByteArray())
        buffer.putInt(16) // Subchunk1Size
        buffer.putShort(1) // PCM format
        buffer.putShort(1) // Mono
        buffer.putInt(SAMPLE_RATE)
        buffer.putInt(SAMPLE_RATE * 2) // Byte rate
        buffer.putShort(2) // Block align
        buffer.putShort(16) // Bits per sample

        buffer.put("data".toByteArray())
        buffer.putInt(numBytes)
        for (s in samples) {
            buffer.putShort(s)
        }
        return buffer.array()
    }

    // Short crisp UI tick blip (30ms)
    private fun generateClickWav(): ByteArray {
        val durationMs = 30
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 1200.0
            val env = exp(-t * 120.0)
            val v = sin(2.0 * Math.PI * freq * t) * env
            samples[i] = (v * 20000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return createWavBytes(samples)
    }

    // Two-tone soft confirmation chime (140ms)
    private fun generateConfirmWav(): ByteArray {
        val durationMs = 140
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = if (t < 0.07) 523.25 else 659.25 // C5 -> E5
            val localT = if (t < 0.07) t else t - 0.07
            val env = exp(-localT * 35.0)
            val v = (sin(2.0 * Math.PI * freq * t) + 0.3 * sin(4.0 * Math.PI * freq * t)) * env
            samples[i] = (v * 18000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return createWavBytes(samples)
    }

    // Coins / Cash Register bell arpeggio (260ms)
    private fun generateCashWav(): ByteArray {
        val durationMs = 260
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val samples = ShortArray(numSamples)
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        val step = 0.065

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val noteIdx = (t / step).toInt().coerceIn(0, 3)
            val localT = t - (noteIdx * step)
            val freq = notes[noteIdx]
            val env = exp(-localT * 40.0)
            val v = (sin(2.0 * Math.PI * freq * t) + 0.4 * sin(4.0 * Math.PI * freq * t)) * env
            samples[i] = (v * 22000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return createWavBytes(samples)
    }

    // Low buzz / Error bump (150ms)
    private fun generateErrorWav(): ByteArray {
        val durationMs = 150
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val samples = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 160.0 - (t * 200.0)
            val env = exp(-t * 25.0)
            val v = (sin(2.0 * Math.PI * freq * t) + 0.5 * sin(6.0 * Math.PI * freq * t)) * env
            samples[i] = (v * 19000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return createWavBytes(samples)
    }

    // Welcoming hire chime triad (320ms)
    private fun generateHireWav(): ByteArray {
        val durationMs = 320
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val samples = ShortArray(numSamples)
        val freqs = doubleArrayOf(349.23, 440.0, 523.25) // F4, A4, C5

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 10.0) * (1.0 - exp(-t * 60.0))
            var v = 0.0
            for (f in freqs) {
                v += sin(2.0 * Math.PI * f * t)
            }
            samples[i] = ((v / 3.0) * env * 22000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return createWavBytes(samples)
    }

    // Product Launch / Level Up celebratory flourish (600ms)
    private fun generateLaunchWav(): ByteArray {
        val durationMs = 600
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val samples = ShortArray(numSamples)
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 987.77, 1046.50, 1318.51)
        val step = 0.09

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val noteIdx = (t / step).toInt().coerceIn(0, 5)
            val localT = t - (noteIdx * step)
            val freq = notes[noteIdx]
            val env = exp(-localT * 22.0)
            val v = (sin(2.0 * Math.PI * freq * t) + 0.35 * sin(2.0 * Math.PI * freq * 2.0 * t)) * env
            samples[i] = (v * 24000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return createWavBytes(samples)
    }

    // 12-second peaceful ambient Lo-Fi tech loop (Cmaj7 -> Am7 -> Fmaj7 -> Gsus4)
    private fun generateAmbientBgmWav(): ByteArray {
        val durationSec = 12.0
        val numSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = ShortArray(numSamples)

        val chords = listOf(
            doubleArrayOf(261.63, 329.63, 392.00, 493.88), // C4, E4, G4, B4 (Cmaj7)
            doubleArrayOf(220.00, 261.63, 329.63, 392.00), // A3, C4, E4, G4 (Am7)
            doubleArrayOf(174.61, 220.00, 261.63, 329.63), // F3, A3, C4, E4 (Fmaj7)
            doubleArrayOf(196.00, 261.63, 293.66, 392.00)  // G3, C4, D4, G4 (Gsus4)
        )
        val bassNotes = doubleArrayOf(65.41, 55.0, 43.65, 49.00) // C2, A1, F1, G1
        val chordDuration = 3.0 // 3 seconds per chord

        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val chordIdx = ((t / chordDuration).toInt()) % chords.size
            val localT = t % chordDuration
            val currentChord = chords[chordIdx]
            val bassFreq = bassNotes[chordIdx]

            // Soft electric piano / warm sine synth with gentle attack & decay
            val chordEnv = (1.0 - exp(-localT * 6.0)) * exp(-localT * 0.9)
            var chordWave = 0.0
            for ((idx, freq) in currentChord.withIndex()) {
                // gentle arpeggio offset
                val arpOffset = idx * 0.12
                val noteEnv = if (localT >= arpOffset) {
                    val nt = localT - arpOffset
                    (1.0 - exp(-nt * 12.0)) * exp(-nt * 1.4)
                } else 0.0
                chordWave += sin(2.0 * Math.PI * freq * t) * noteEnv
            }

            // Warm subtle sub-bass
            val bassEnv = (1.0 - exp(-localT * 8.0)) * exp(-localT * 0.7)
            val bassWave = sin(2.0 * Math.PI * bassFreq * t) * bassEnv * 0.65

            // High bell shimmer
            val bellFreq = currentChord[3] * 2.0
            val bellEnv = if (localT > 1.2) {
                val bt = localT - 1.2
                exp(-bt * 4.0) * (1.0 - exp(-bt * 20.0)) * 0.3
            } else 0.0
            val bellWave = sin(2.0 * Math.PI * bellFreq * t) * bellEnv

            val mix = (chordWave * 0.4 + bassWave + bellWave) * 0.55
            // Soft master volume ceiling
            samples[i] = (mix * 18000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
        return createWavBytes(samples)
    }
}
