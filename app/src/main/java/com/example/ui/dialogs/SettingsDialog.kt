package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.model.GameSettings

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp

@Composable
fun SettingsDialog(
    settings: GameSettings,
    companyName: String?,
    cash: Double?,
    onUpdateSettings: (GameSettings) -> Unit,
    onSaveGameNow: () -> Unit,
    onReturnToMainMenu: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    var musicVol by remember { mutableFloatStateOf(settings.musicVolume) }
    var sfxVol by remember { mutableFloatStateOf(settings.sfxVolume) }
    var isMusicOn by remember { mutableStateOf(settings.isMusicEnabled) }
    var isSfxOn by remember { mutableStateOf(settings.isSfxEnabled) }
    var isHapticsOn by remember { mutableStateOf(settings.isHapticsEnabled) }
    var selectedTrack by remember { mutableIntStateOf(settings.selectedMusicTrack) }
    var showMainMenuConfirm by remember { mutableStateOf(false) }

    fun commitSettings(newTrack: Int = selectedTrack) {
        val updated = settings.copy(
            musicVolume = musicVol,
            sfxVolume = sfxVol,
            isMusicEnabled = isMusicOn,
            isSfxEnabled = isSfxOn,
            isHapticsEnabled = isHapticsOn,
            selectedMusicTrack = newTrack
        )
        onUpdateSettings(updated)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        titleContentColor = Color.White,
        textContentColor = Color(0xFF94A3B8),
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF00E5FF))
                Text("Game Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Audio Section
                Text("AUDIO & SOUNDTRACK", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF38BDF8), letterSpacing = 1.sp)

                // Background Music
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Background Music", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text("Synthesized Lo-Fi & Synthwave", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = isMusicOn,
                            onCheckedChange = {
                                isMusicOn = it
                                SoundManager.playClick()
                                commitSettings()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E5FF),
                                checkedTrackColor = Color(0xFF0284C7)
                            ),
                            modifier = Modifier.testTag("switch_music_toggle")
                        )
                    }

                    if (isMusicOn) {
                        // Track volume slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            Slider(
                                value = musicVol,
                                onValueChange = {
                                    musicVol = it
                                    commitSettings()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("slider_music_volume"),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF00E5FF),
                                    activeTrackColor = Color(0xFF00E5FF)
                                )
                            )
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }

                        // Music Track Selector Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                                        Text("Active Track:", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                                        Text(
                                            SoundManager.TRACK_NAMES.getOrElse(selectedTrack) { "Track" },
                                            fontSize = 11.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    SoundManager.TRACK_NAMES.forEachIndexed { index, name ->
                                        val isSelected = selectedTrack == index
                                        val shortLabel = when (index) {
                                            0 -> "Lo-Fi Chill"
                                            1 -> "Synthwave"
                                            else -> "Acoustic"
                                        }
                                        FilledTonalButton(
                                            onClick = {
                                                selectedTrack = index
                                                SoundManager.playClick()
                                                SoundManager.switchTrack(index)
                                                commitSettings(index)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_track_$index"),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                                                contentColor = if (isSelected) Color.White else Color(0xFF94A3B8)
                                            )
                                        ) {
                                            Text(shortLabel, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Sound Effects
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sound Effects (SFX)", fontSize = 13.sp, color = Color.White)
                        Switch(
                            checked = isSfxOn,
                            onCheckedChange = {
                                isSfxOn = it
                                SoundManager.playClick()
                                commitSettings()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E5FF),
                                checkedTrackColor = Color(0xFF0284C7)
                            ),
                            modifier = Modifier.testTag("switch_sfx_toggle")
                        )
                    }

                    if (isSfxOn) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            Slider(
                                value = sfxVol,
                                onValueChange = {
                                    sfxVol = it
                                    commitSettings()
                                },
                                onValueChangeFinished = {
                                    SoundManager.playConfirm()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("slider_sfx_volume"),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF00E5FF),
                                    activeTrackColor = Color(0xFF00E5FF)
                                )
                            )
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                // Haptics & Feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Haptic Feedback", fontSize = 13.sp, color = Color.White)
                        Text("Subtle vibration on interactions", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                    Switch(
                        checked = isHapticsOn,
                        onCheckedChange = {
                            isHapticsOn = it
                            SoundManager.playClick()
                            commitSettings()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00E5FF),
                            checkedTrackColor = Color(0xFF0284C7)
                        ),
                        modifier = Modifier.testTag("switch_haptics_toggle")
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                // Save game manager card
                if (companyName != null && cash != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("ACTIVE SAVE FILE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            Text("$companyName • Treasury: $%,.0f".format(cash), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Button(
                                onClick = {
                                    SoundManager.playConfirm()
                                    onSaveGameNow()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_save_now"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Game Now", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Return to Main Menu (if in gameplay)
                if (onReturnToMainMenu != null) {
                    OutlinedButton(
                        onClick = {
                            SoundManager.playClick()
                            showMainMenuConfirm = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_return_main_menu"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exit to Title Menu", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    SoundManager.playClick()
                    onDismiss()
                }
            ) {
                Text("Done", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
            }
        }
    )

    if (showMainMenuConfirm && onReturnToMainMenu != null) {
        AlertDialog(
            onDismissRequest = { showMainMenuConfirm = false },
            containerColor = Color(0xFF0F172A),
            titleContentColor = Color.White,
            textContentColor = Color(0xFF94A3B8),
            shape = RoundedCornerShape(16.dp),
            title = { Text("Exit to Main Menu?") },
            text = { Text("Your company progress will be automatically saved before returning to the title screen.") },
            confirmButton = {
                Button(
                    onClick = {
                        showMainMenuConfirm = false
                        onSaveGameNow()
                        SoundManager.playConfirm()
                        onDismiss()
                        onReturnToMainMenu()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Save & Exit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMainMenuConfirm = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}
