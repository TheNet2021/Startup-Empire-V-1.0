package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*

@Composable
fun EmployeeDossierDialog(
    employee: Employee,
    desk: PlacedFurniture?,
    assignedProduct: Product?,
    cash: Double,
    onDismiss: () -> Unit,
    onTrain: (Employee) -> Unit,
    onRaiseSalary: (Employee) -> Unit,
    onSendRetreat: (Employee) -> Unit,
    onFire: (Employee) -> Unit
) {
    var showConfirmFire by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        titleContentColor = Color.White,
        textContentColor = Color(0xFF94A3B8),
        shape = RoundedCornerShape(18.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Character Avatar & Role Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Stylized Avatar Box
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(employee.hairColor),
                                        Color(employee.shirtColor)
                                    )
                                )
                            )
                            .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (employee.role) {
                                EmployeeRole.DEVELOPER -> Icons.Default.Terminal
                                EmployeeRole.DESIGNER -> Icons.Default.Palette
                                EmployeeRole.QA_TESTER -> Icons.Default.BugReport
                                EmployeeRole.MARKETER -> Icons.Default.Campaign
                                EmployeeRole.SYSADMIN -> Icons.Default.Dns
                                EmployeeRole.PROJECT_MANAGER -> Icons.Default.Groups
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = employee.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Text(
                                    text = employee.role.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "•  ${employee.levelTitle}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                // Stats Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBarRow(
                        label = "Skill Proficiency",
                        value = "${employee.skill}/100",
                        progress = employee.skill / 100f,
                        barColor = Color(0xFF00E5FF)
                    )
                    StatBarRow(
                        label = "Execution Speed",
                        value = "${employee.speed}/100",
                        progress = employee.speed / 100f,
                        barColor = Color(0xFF38BDF8)
                    )
                    StatBarRow(
                        label = "Morale",
                        value = "${employee.morale}%",
                        progress = employee.morale / 100f,
                        barColor = if (employee.morale > 70) Color(0xFF10B981) else if (employee.morale > 40) Color(0xFFF59E0B) else Color(0xFFEF4444)
                    )
                    StatBarRow(
                        label = "Energy",
                        value = "${employee.energy}%",
                        progress = employee.energy / 100f,
                        barColor = Color(0xFFA855F7)
                    )
                }

                // Workstation & Project assignment summary card
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Assigned Station:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = desk?.type?.title ?: "No Desk Assigned",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (desk != null) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Project:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = assignedProduct?.name ?: "Brainstorming",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Daily Compensation:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "$%,.0f / day".format(employee.dailySalary),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onTrain(employee) },
                        enabled = cash >= 500.0,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_train_employee"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Train ($500)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onRaiseSalary(employee) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_raise_salary"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+15% Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSendRetreat(employee) },
                        enabled = cash >= 300.0,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_retreat_employee"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA855F7)),
                        border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Spa, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retreat ($300)", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { showConfirmFire = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_fire_employee"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.PersonRemove, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dismiss", fontSize = 11.sp)
                    }
                }

                if (showConfirmFire) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B151E)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Confirm Dismissal?",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Text(
                                "Terminate ${employee.name}'s employment? Their desk will be freed.",
                                fontSize = 11.sp,
                                color = Color(0xFFFCA5A5)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { showConfirmFire = false }) {
                                    Text("Cancel", color = Color.White, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        showConfirmFire = false
                                        onFire(employee)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Confirm Terminate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun StatBarRow(
    label: String,
    value: String,
    progress: Float,
    barColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 11.sp, color = Color(0xFF94A3B8))
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = Color(0xFF1E293B)
        )
    }
}
