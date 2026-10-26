package com.example.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun EmployeesTab(
    state: GameState,
    onHireCandidate: (Employee) -> Unit,
    onRaiseSalary: (Employee) -> Unit,
    onSendOnRetreat: (Employee) -> Unit,
    onTrainEmployee: (Employee) -> Unit,
    onPromoteEmployee: (Employee) -> Unit = {},
    onFireEmployee: (Employee) -> Unit,
    onAssignProduct: (Employee, String?) -> Unit,
    onAssignContract: (Employee, String?) -> Unit = { _, _ -> },
    onSelectEmployee: (Employee) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSubtab by remember { mutableIntStateOf(0) } // 0: Team Roster, 1: Recruitment
    var employeeToAssign by remember { mutableStateOf<Employee?>(null) }
    var employeeToFire by remember { mutableStateOf<Employee?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
    ) {
        // Tab Header Switcher
        TabRow(
            selectedTabIndex = selectedSubtab,
            containerColor = Color(0xFF0F172A),
            contentColor = Color(0xFF00E5FF),
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedSubtab == 0,
                onClick = { selectedSubtab = 0 },
                text = { Text("Team Roster (${state.employees.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("subtab_team_roster")
            )
            Tab(
                selected = selectedSubtab == 1,
                onClick = { selectedSubtab = 1 },
                text = { Text("Recruitment (${state.availableCandidates.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("subtab_recruitment")
            )
        }

        if (selectedSubtab == 0) {
            // Team Roster View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Team overview summary bar
                item {
                    val totalPayroll = state.employees.sumOf { it.dailySalary }
                    val avgMorale = if (state.employees.isNotEmpty()) state.employees.map { it.morale }.average().toInt() else 0

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Staff", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("${state.employees.size} / ${state.totalWorkstations}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Daily Payroll", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("$%,.0f/day".format(totalPayroll), color = Color(0xFFF87171), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Avg Morale", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("$avgMorale%", color = if (avgMorale >= 75) Color(0xFF34D399) else Color(0xFFFBBF24), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                items(state.employees, key = { it.id }) { emp ->
                    val assignedProd = state.products.find { it.id == emp.assignedProductId }

                    Card(
                        onClick = { onSelectEmployee(emp) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar circle with shirt color
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(emp.shirtColor))
                                        .border(2.dp, Color(emp.skinTone), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (emp.role) {
                                            EmployeeRole.DEVELOPER -> Icons.Default.Code
                                            EmployeeRole.DESIGNER -> Icons.Default.Palette
                                            EmployeeRole.QA_TESTER -> Icons.Default.BugReport
                                            EmployeeRole.MARKETER -> Icons.Default.Campaign
                                            EmployeeRole.SYSADMIN -> Icons.Default.Dns
                                            EmployeeRole.PROJECT_MANAGER -> Icons.Default.Engineering
                                            EmployeeRole.SALES -> Icons.Default.TrendingUp
                                        },
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = emp.name,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF1E293B)
                                        ) {
                                            Text(
                                                text = "${emp.levelTitle} ${emp.role.title}",
                                                color = Color(0xFF38BDF8),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Skill: ${emp.skill} | Spd: ${emp.speed}",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "$%,.0f/day".format(emp.dailySalary),
                                            color = Color(0xFFFBBF24),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // State badge
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (emp.state) {
                                        EmployeeState.WORKING -> Color(0xFF00E5FF).copy(alpha = 0.2f)
                                        EmployeeState.COFFEE_BREAK -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                        EmployeeState.BRAINSTORMING -> Color(0xFFA855F7).copy(alpha = 0.2f)
                                        EmployeeState.RESTING -> Color(0xFF10B981).copy(alpha = 0.2f)
                                        EmployeeState.EXHAUSTED -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                    }
                                ) {
                                    Text(
                                        text = emp.state.name.replace("_", " "),
                                        color = when (emp.state) {
                                            EmployeeState.WORKING -> Color(0xFF00E5FF)
                                            EmployeeState.COFFEE_BREAK -> Color(0xFFF59E0B)
                                            EmployeeState.BRAINSTORMING -> Color(0xFFA855F7)
                                            EmployeeState.RESTING -> Color(0xFF10B981)
                                            EmployeeState.EXHAUSTED -> Color(0xFFEF4444)
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Morale and Energy Gauges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Morale", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text("${emp.morale}%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { emp.morale / 100f },
                                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                                        color = if (emp.morale > 60) Color(0xFF10B981) else Color(0xFFF59E0B),
                                        trackColor = Color(0xFF1E293B),
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Energy", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text("${emp.energy}%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { emp.energy / 100f },
                                        modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                                        color = if (emp.energy > 40) Color(0xFF38BDF8) else Color(0xFFEF4444),
                                        trackColor = Color(0xFF1E293B),
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Assigned Product Indicator
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val assignedContract = state.contracts.find { it.id == emp.assignedContractId }
                                val assignmentLabel = when {
                                    assignedProd != null -> "Product: ${assignedProd.name}"
                                    assignedContract != null -> "Contract: ${assignedContract.title}"
                                    else -> "Idle (Brainstorming)"
                                }
                                val hasTask = assignedProd != null || assignedContract != null

                                Text(
                                    text = assignmentLabel,
                                    color = if (hasTask) Color(0xFF34D399) else Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                TextButton(
                                    onClick = { employeeToAssign = emp },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Assign Task", fontSize = 11.sp, color = Color(0xFF00E5FF))
                                }
                            }

                            // Action buttons row (Raise Salary, Train, Retreat, Fire)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onRaiseSalary(emp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    Text("+Raise", fontSize = 10.sp, color = Color(0xFFFBBF24))
                                }

                                OutlinedButton(
                                    onClick = { onTrainEmployee(emp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    Text("Train ($500)", fontSize = 10.sp, color = Color(0xFF38BDF8))
                                }

                                OutlinedButton(
                                    onClick = { onSendOnRetreat(emp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                ) {
                                    Text("Retreat ($300)", fontSize = 10.sp, color = Color(0xFFEC4899))
                                }

                                if (emp.id != "emp_founder") {
                                    IconButton(
                                        onClick = { employeeToFire = emp },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Dismiss Employee",
                                            tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            if (emp.isPromotable) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { onPromoteEmployee(emp) },
                                    enabled = state.cash >= emp.promotionCost,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("promote_employee_${emp.id}")
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Promote to Level ${emp.level + 1} ($%,.0f Bonus)".format(emp.promotionCost),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Recruitment Marketplace View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    val availableDesks = state.availableDesks
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Available Workstations", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text(
                                    text = if (availableDesks > 0) "$availableDesks Free Desks" else "No Free Desks (Buy desks in Office)",
                                    color = if (availableDesks > 0) Color(0xFF34D399) else Color(0xFFEF4444),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = "Pool refreshes every 3 days",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                items(state.availableCandidates, key = { it.id }) { cand ->
                    val canAfford = state.cash >= cand.hiringCost
                    val hasDesk = state.availableDesks > 0
                    val canHire = hasDesk && canAfford

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = cand.name,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF1E293B)
                                    ) {
                                        Text(
                                            text = "${cand.levelTitle} ${cand.role.title}",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cand.role.description,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Skill: ${cand.skill}", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Speed: ${cand.speed}", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Salary: $%,.0f/day".format(cand.dailySalary), color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Recruiting Fee: $%,.0f".format(cand.hiringCost),
                                    color = Color(0xFFFBBF24),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { onHireCandidate(cand) },
                                enabled = canHire,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E5FF),
                                    disabledContainerColor = Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("hire_candidate_${cand.id}")
                            ) {
                                Text(
                                    text = if (!hasDesk) "No Desk" else if (!canAfford) "Need Cash" else "Hire ($%,.0f)".format(cand.hiringCost),
                                    color = if (canHire) Color(0xFF090D16) else Color(0xFF64748B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet to assign employee to task (Product or Contract)
    employeeToAssign?.let { emp ->
        val activeContracts = state.contracts.filter { it.status == ContractStatus.IN_PROGRESS }

        AlertDialog(
            onDismissRequest = { employeeToAssign = null },
            title = {
                Column {
                    Text("Assign Task: ${emp.name}", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("${emp.levelTitle} ${emp.role.title} (${emp.role.primarySkill}: ${emp.skill}, Spd: ${emp.speed})", color = Color(0xFF00E5FF), fontSize = 11.sp)
                }
            },
            containerColor = Color(0xFF131B2E),
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Digital Products:", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    state.products.forEach { prod ->
                        val isAssigned = emp.assignedProductId == prod.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isAssigned) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1E293B),
                            border = BorderStroke(1.dp, if (isAssigned) Color(0xFF00E5FF) else Color(0xFF334155)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAssignProduct(emp, prod.id)
                                    employeeToAssign = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(prod.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("${prod.stage.name.replace("_", " ")} | ${prod.category.title}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                }
                                if (isAssigned) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    if (activeContracts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Active Client Contracts:", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        activeContracts.forEach { contract ->
                            val isAssignedContract = emp.assignedContractId == contract.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isAssignedContract) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (isAssignedContract) Color(0xFF00E5FF) else Color(0xFF334155)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAssignContract(emp, contract.id)
                                        employeeToAssign = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(contract.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("${contract.clientName} | ${contract.daysRemaining}d left", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    }
                                    if (isAssignedContract) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    val isIdle = emp.assignedProductId == null && emp.assignedContractId == null
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isIdle) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (isIdle) Color(0xFF00E5FF) else Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAssignProduct(emp, null)
                                onAssignContract(emp, null)
                                employeeToAssign = null
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Unassigned (Brainstorming)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Rests or generates research ideas", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                            if (isIdle) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { employeeToAssign = null }) {
                    Text("Close", color = Color(0xFF00E5FF))
                }
            }
        )
    }

    // Confirmation dialog before firing employee
    employeeToFire?.let { emp ->
        AlertDialog(
            onDismissRequest = { employeeToFire = null },
            containerColor = Color(0xFF131B2E),
            title = { Text("Fire ${emp.name}?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to dismiss ${emp.name}? Their workstation will become available and their daily salary ($%,.0f/day) will be removed from payroll.".format(emp.dailySalary),
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onFireEmployee(emp)
                        employeeToFire = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Confirm Termination", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { employeeToFire = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}
