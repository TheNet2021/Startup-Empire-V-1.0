package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameClock
import com.example.model.GameSpeed
import com.example.model.GameState

@Composable
fun TopStatusBar(
    state: GameState,
    onSpeedChange: (GameSpeed) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_status_bar"),
        color = Color(0xFF0F172A),
        tonalElevation = 6.dp,
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Row 1: Company Name, Level Badge, and Settings/Notifications
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompanyLogoBadge(
                        iconKey = state.companyLogoIcon,
                        colorLong = state.companyLogoColor,
                        shapeKey = state.companyLogoShape,
                        sizeDp = 26.dp
                    )
                    Text(
                        text = state.companyName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, Color(0xFF0284C7))
                    ) {
                        Text(
                            text = "Lv ${state.companyLevel}",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Notifications icon with badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onOpenNotifications() }
                            .testTag("notifications_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = if (state.notifications.isNotEmpty()) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                        if (state.notifications.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                                    .background(Color(0xFFEF4444), CircleShape)
                            )
                        }
                    }

                    // Settings icon
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Cash Balance, Daily Net Profit, and Time + Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Financial Status
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isNegativeCash = state.cash < 0
                        Text(
                            text = "$%,.0f".format(state.cash),
                            color = if (isNegativeCash) Color(0xFFEF4444) else Color(0xFF10B981),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    val yesterdayProfit = state.financialHistory.lastOrNull()?.profit ?: state.todayLedger.netProfit
                    val profitColor = if (yesterdayProfit >= 0) Color(0xFF34D399) else Color(0xFFF87171)
                    val sign = if (yesterdayProfit >= 0) "+" else ""
                    Text(
                        text = "$sign$%,.0f /day".format(yesterdayProfit),
                        color = profitColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Clock & Speed Controls
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = state.clock.formattedDate,
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Speed toggle pill buttons
                    Row(
                        modifier = Modifier
                            .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        SpeedButton(
                            label = "⏸",
                            isSelected = state.clock.speed == GameSpeed.PAUSED,
                            onClick = { onSpeedChange(GameSpeed.PAUSED) },
                            testTag = "speed_pause"
                        )
                        SpeedButton(
                            label = "1x",
                            isSelected = state.clock.speed == GameSpeed.NORMAL,
                            onClick = { onSpeedChange(GameSpeed.NORMAL) },
                            testTag = "speed_1x"
                        )
                        SpeedButton(
                            label = "2x",
                            isSelected = state.clock.speed == GameSpeed.FAST,
                            onClick = { onSpeedChange(GameSpeed.FAST) },
                            testTag = "speed_2x"
                        )
                        SpeedButton(
                            label = "5x",
                            isSelected = state.clock.speed == GameSpeed.ULTRA,
                            onClick = { onSpeedChange(GameSpeed.ULTRA) },
                            testTag = "speed_5x"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) Color(0xFF00E5FF) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color(0xFF090D16) else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
