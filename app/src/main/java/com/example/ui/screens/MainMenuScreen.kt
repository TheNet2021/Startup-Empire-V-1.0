package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.model.SaveSummary
import com.example.ui.components.CompanyLogoBadge
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MainMenuScreen(
    saveSummary: SaveSummary?,
    onStartNewGame: () -> Unit,
    onContinueGame: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCredits: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showOverwriteConfirmDialog by remember { mutableStateOf(false) }

    // Ambient floating lights animation
    val infiniteTransition = rememberInfiniteTransition(label = "main_menu_bg")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070B14),
                        Color(0xFF0F172A),
                        Color(0xFF090D16)
                    )
                )
            )
    ) {
        // Subtle ambient neon particle canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 3f

            // Glow orbs
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(cx + sin(pulse) * 60f, cy + cos(pulse) * 40f),
                    radius = 280f
                ),
                radius = 280f,
                center = Offset(cx + sin(pulse) * 60f, cy + cos(pulse) * 40f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(cx - cos(pulse) * 80f, cy + sin(pulse) * 60f),
                    radius = 320f
                ),
                radius = 320f,
                center = Offset(cx - cos(pulse) * 80f, cy + sin(pulse) * 60f)
            )
        }

        // Main Menu Content Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .windowInsetsPadding(WindowInsets.systemBars),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Game Brand & Crest Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // High-tech logo badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(12.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF00E5FF).copy(alpha = 0.25f),
                                    Color(0xFF1E293B)
                                )
                            )
                        )
                        .border(2.dp, Color(0xFF00E5FF).copy(alpha = 0.8f), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = "Game Crest",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "STARTUP EMPIRE",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "3D TECH BUSINESS SIMULATOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    letterSpacing = 3.sp
                )
            }

            // Save Resume Card if save exists
            if (saveSummary != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                    border = BorderStroke(1.5.dp, Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CompanyLogoBadge(
                            iconKey = saveSummary.logoIcon,
                            colorLong = saveSummary.logoColor,
                            sizeDp = 48.dp
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = saveSummary.companyName,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${saveSummary.levelTitle}  •  ${saveSummary.dateString}",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Treasury: $%,.0f".format(saveSummary.cash),
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                // Pitch snippet for new players
                Text(
                    text = "Build digital products, hire engineering teams, customize your tech headquarters, and lead your startup to global dominance.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Menu Buttons Stack
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CONTINUE GAME (If save available)
                if (saveSummary != null) {
                    Button(
                        onClick = {
                            SoundManager.playConfirm()
                            onContinueGame()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("btn_continue_game"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONTINUE CAREER",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // START NEW GAME
                Button(
                    onClick = {
                        SoundManager.playClick()
                        if (saveSummary != null) {
                            showOverwriteConfirmDialog = true
                        } else {
                            onStartNewGame()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_start_new_game"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (saveSummary == null) Color(0xFF00E5FF) else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = if (saveSummary != null) BorderStroke(1.dp, Color(0xFF334155)) else null
                ) {
                    Icon(
                        Icons.Default.AddBusiness,
                        contentDescription = null,
                        tint = if (saveSummary == null) Color(0xFF090D16) else Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START NEW GAME",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (saveSummary == null) Color(0xFF090D16) else Color.White,
                        letterSpacing = 1.sp
                    )
                }

                // SETTINGS & CREDITS ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            SoundManager.playClick()
                            onOpenSettings()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_main_menu_settings"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Settings", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = {
                            SoundManager.playClick()
                            onOpenCredits()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_main_menu_credits"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Credits", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // Overwrite Confirmation Dialog
    if (showOverwriteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showOverwriteConfirmDialog = false },
            containerColor = Color(0xFF0F172A),
            titleContentColor = Color.White,
            textContentColor = Color(0xFF94A3B8),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("Start New Company?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "You have an ongoing career for '${saveSummary?.companyName}'. Starting a new game will overwrite your existing save progress.",
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOverwriteConfirmDialog = false
                        SoundManager.playConfirm()
                        onStartNewGame()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Overwrite & Start", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverwriteConfirmDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}
