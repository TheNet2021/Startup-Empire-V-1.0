package com.example.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.*

@Composable
fun ServersTab(
    state: GameState,
    onToggleAutoScale: (Boolean) -> Unit,
    onEmergencyRestart: () -> Unit,
    onOptimizeDatabase: () -> Unit,
    onNavigateToOfficeCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalUsers = state.totalActiveUsers
    val server = state.server
    val load = state.serverLoad
    val isOutage = server.isOutageActive
    val totalRacks = state.furniture.count { it.type.category == FurnitureCategory.INFRASTRUCTURE }
    val physicalCapacity = server.totalPhysicalCapacity

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Outage Alert Banner if active
        if (isOutage) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFCA5A5))
                            Text(
                                text = "CRITICAL SERVER OUTAGE!",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = server.incidentReason ?: "Servers are unresponsive due to heavy traffic load.",
                            color = Color(0xFFFEE2E2),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Estimated downtime remaining: ${server.outageHoursRemaining} hours. Active users are churning!",
                            color = Color(0xFFFCA5A5),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onEmergencyRestart,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("emergency_restart_button")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Perform Emergency Cluster Reboot", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live Load & Capacity Gauge Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Traffic & Computing Load", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            Text(
                                text = "%,d / %,d Capacity".format(totalUsers, physicalCapacity),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                isOutage -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                load > 0.85f -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                else -> Color(0xFF10B981).copy(alpha = 0.2f)
                            }
                        ) {
                            Text(
                                text = "${(load * 100).toInt()}% Load",
                                color = when {
                                    isOutage -> Color(0xFFEF4444)
                                    load > 0.85f -> Color(0xFFF59E0B)
                                    else -> Color(0xFF10B981)
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { load.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        color = when {
                            load > 0.95f -> Color(0xFFEF4444)
                            load > 0.75f -> Color(0xFFF59E0B)
                            else -> Color(0xFF00E5FF)
                        },
                        trackColor = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Global SLA", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text("%.2f%%".format(server.uptimePercent), color = Color(0xFF34D399), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Server Racks", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text("$totalRacks Active", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hosting Upkeep", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text("$%,.0f/day".format(server.calculateDailyCost(totalUsers)), color = Color(0xFFF87171), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Maintenance & Database Optimization Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Database Indexing & Cache Optimization", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Defragments database shards, flushes stale cache, and boosts system uptime reliability.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = onOptimizeDatabase,
                        enabled = state.cash >= 250.0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("optimize_database_button")
                    ) {
                        Text("Tune ($250)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Cloud Auto-Scale Switch Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                            Text("Cloud Elastic Burst Scaling", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dynamically spins up cloud VMs when traffic exceeds rack limits. Prevents outages at $0.04 per 1,000 excess users/day.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Switch(
                        checked = server.isCloudAutoScaleEnabled,
                        onCheckedChange = onToggleAutoScale,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00E5FF),
                            checkedTrackColor = Color(0xFF0891B2)
                        ),
                        modifier = Modifier.testTag("toggle_auto_scale_switch")
                    )
                }
            }
        }

        // Hardware Rack Upgrades Action Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Expand Physical Infrastructure", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Deploy server blade racks in your office to increase fixed user capacity permanently and reduce cloud variable fees.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onNavigateToOfficeCatalog,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("buy_server_racks_button")
                    ) {
                        Icon(Icons.Default.Dns, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Server Equipment Shop", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
