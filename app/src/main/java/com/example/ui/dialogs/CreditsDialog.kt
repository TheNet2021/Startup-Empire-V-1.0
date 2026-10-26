package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager

@Composable
fun CreditsDialog(onDismiss: () -> Unit) {
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
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF00E5FF))
                Text("About Startup Empire 3D", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Startup Empire 3D is a full-featured tech company management simulation built natively with Kotlin and Jetpack Compose.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFFCBD5E1)
                )

                HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Game Design & Engineering:", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    Text("Senior Simulation & Mobile UX Architecture", fontSize = 12.sp, color = Color.White)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Audio System:", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    Text("Procedural Real-Time Audio Synthesizer (Zero Dependencies)", fontSize = 12.sp, color = Color.White)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Inspiration:", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    Text("Classic PC Tycoon & Office Management Simulators", fontSize = 12.sp, color = Color.White)
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
                Text("Close", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
            }
        }
    )
}
