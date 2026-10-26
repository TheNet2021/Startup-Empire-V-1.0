package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    onPromote: (Employee) -> Unit = {},
    onBonus: (Employee) -> Unit = {},
    onCoffeeBreak: (Employee) -> Unit = {},
    onFire: (Employee) -> Unit
) {
    var showConfirmFire by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        titleContentColor = Color.White,
        textContentColor = Color(0xFF94A3B8),
        shape = RoundedCornerShape(20.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Header with Avatar, Role, Specialization & Seniority
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val roleColor = when (employee.role) {
                        EmployeeRole.DEVELOPER -> Color(0xFF00E5FF)
                        EmployeeRole.DESIGNER -> Color(0xFFEC4899)
                        EmployeeRole.QA_TESTER -> Color(0xFF10B981)
                        EmployeeRole.MARKETER -> Color(0xFFF59E0B)
                        EmployeeRole.SYSADMIN -> Color(0xFF38BDF8)
                        EmployeeRole.PROJECT_MANAGER -> Color(0xFFA855F7)
                        EmployeeRole.SALES -> Color(0xFFEAB308)
                    }

                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(employee.hairColor),
                                        Color(employee.shirtColor)
                                    )
                                )
                            )
                            .border(2.dp, roleColor, RoundedCornerShape(16.dp)),
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
                                EmployeeRole.SALES -> Icons.Default.TrendingUp
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = employee.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = employee.specializationTitle,
                            fontSize = 12.sp,
                            color = roleColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(11.dp))
                                    Text(
                                        text = "Level ${employee.level} • ${employee.levelTitle}",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF1F5F9)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Dynamic Live Thought / Status Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                        Text(
                            text = "\"${employee.currentThought}\"",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 3. Employee Unique Trait Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = employee.trait.iconEmoji,
                            fontSize = 24.sp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Trait: ${employee.trait.title}",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = employee.trait.description,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // 4. Performance & Vital Stats
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
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
                        label = "Morale Level",
                        value = "${employee.morale}%",
                        progress = employee.morale / 100f,
                        barColor = if (employee.morale > 70) Color(0xFF10B981) else if (employee.morale > 40) Color(0xFFF59E0B) else Color(0xFFEF4444)
                    )
                    StatBarRow(
                        label = "Energy Reserves",
                        value = "${employee.energy}%",
                        progress = employee.energy / 100f,
                        barColor = Color(0xFFA855F7)
                    )
                }

                // 5. Workstation, Project & Payroll Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Workstation:", fontSize = 11.5.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = desk?.type?.title ?: "No Desk Assigned",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (desk != null) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Current Project:", fontSize = 11.5.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = assignedProduct?.name ?: "Brainstorming",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Daily Compensation:", fontSize = 11.5.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "$%,.0f / day".format(employee.dailySalary),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Productivity Multiplier:", fontSize = 11.5.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "%.2fx output".format(employee.effectiveProductivity),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                // 6. Executive Care & Enhancement Actions
                Text("Management Actions", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))

                // Row 1: Spot Bonus ($250) & Espresso Break ($40)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onBonus(employee) },
                        enabled = cash >= 250.0,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 7.dp)
                    ) {
                        Text("🎁 Bonus ($250)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    }

                    FilledTonalButton(
                        onClick = { onCoffeeBreak(employee) },
                        enabled = cash >= 40.0,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 7.dp)
                    ) {
                        Text("☕ Coffee ($40)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    }
                }

                // Row 2: Training ($500) & Pay Raise (+15%)
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
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(15.dp))
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
                        Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+15% Salary", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Row 3: Promotion (if promotable)
                if (employee.isPromotable) {
                    Button(
                        onClick = { onPromote(employee) },
                        enabled = cash >= employee.promotionCost,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_promote_employee"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Promote to Level ${employee.level + 1} ($%,.0f Bonus)".format(employee.promotionCost),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                // Row 4: Wellness Retreat ($300)
                OutlinedButton(
                    onClick = { onSendRetreat(employee) },
                    enabled = cash >= 300.0,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEC4899)),
                    border = BorderStroke(1.dp, Color(0xFFEC4899)),
                    contentPadding = PaddingValues(vertical = 7.dp)
                ) {
                    Icon(Icons.Default.Spa, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send to Wellness Retreat ($300)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Danger zone: Fire Employee
                if (!showConfirmFire) {
                    TextButton(
                        onClick = { showConfirmFire = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Terminate Contract", color = Color(0xFFEF4444), fontSize = 11.sp)
                    }
                } else {
                    Surface(
                        color = Color(0xFF450A0A),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Confirm termination of ${employee.name}?", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { onFire(employee) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Yes, Terminate", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { showConfirmFire = false },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Text("Cancel", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
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
            Text(label, fontSize = 11.5.sp, color = Color(0xFF94A3B8))
            Text(value, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = Color(0xFF1E293B),
        )
    }
}
