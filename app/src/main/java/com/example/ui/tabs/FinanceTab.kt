package com.example.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun FinanceTab(
    state: GameState,
    onTakeLoan: (amount: Double, days: Int, dailyPayment: Double, lender: String) -> Unit,
    onRepayLoanEarly: (ActiveLoan) -> Unit,
    onStartResearch: (TechUpgrade) -> Unit,
    onAcquireCompetitor: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSubtab by remember { mutableIntStateOf(0) } // 0: Financials, 1: Market & Rivals, 2: Tech Tree
    var competitorToAcquire by remember { mutableStateOf<CompetitorCompany?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
    ) {
        TabRow(
            selectedTabIndex = selectedSubtab,
            containerColor = Color(0xFF0F172A),
            contentColor = Color(0xFF00E5FF),
            modifier = Modifier.fillMaxWidth().testTag("finance_subtabs")
        ) {
            Tab(
                selected = selectedSubtab == 0,
                onClick = { selectedSubtab = 0 },
                text = { Text("Financials & Debt", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                modifier = Modifier.testTag("subtab_finance_pnl")
            )
            Tab(
                selected = selectedSubtab == 1,
                onClick = { selectedSubtab = 1 },
                text = { Text("Market & Rivals", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                modifier = Modifier.testTag("subtab_market_rivals")
            )
            Tab(
                selected = selectedSubtab == 2,
                onClick = { selectedSubtab = 2 },
                text = { Text("Tech Tree", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                modifier = Modifier.testTag("subtab_tech_tree")
            )
        }

        when (selectedSubtab) {
            0 -> {
                // === FINANCIALS & DEBT ===
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Valuation & Equity Card
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
                                        Text("Calculated Market Valuation", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        Text(
                                            text = "$%,.0f".format(state.valuation),
                                            color = Color(0xFFFBBF24),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF1E293B)
                                    ) {
                                        Text(
                                            text = "%.0f%% Equity Held".format(state.equityOwnedPercent),
                                            color = Color(0xFF38BDF8),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Daily Profit & Loss Statement (P&L)
                    item {
                        val payroll = state.employees.sumOf { it.dailySalary }
                        val rent = state.officeTier.monthlyRent / 30.0
                        val serverCost = state.server.calculateDailyCost(state.totalActiveUsers)
                        val marketing = state.products.sumOf { it.dailyMarketingSpend }
                        val loanPayments = state.activeLoans.sumOf { it.dailyPayment }
                        val dailyRev = state.products.filter { it.stage == ProductStage.LIVE }.sumOf { it.dailyRevenue }
                        val totalExpenses = payroll + rent + serverCost + marketing + loanPayments
                        val netProfit = dailyRev - totalExpenses

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Daily Operating Statement (P&L)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))

                                LedgerRow(label = "Product Recurring Revenue", value = dailyRev, isPositive = true)
                                HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 4.dp))

                                LedgerRow(label = "Employee Salaries", value = -payroll, isPositive = false)
                                LedgerRow(label = "Office Rent", value = -rent, isPositive = false)
                                LedgerRow(label = "Cloud & Server Hosting", value = -serverCost, isPositive = false)
                                LedgerRow(label = "Marketing Campaigns", value = -marketing, isPositive = false)
                                if (loanPayments > 0) {
                                    LedgerRow(label = "Debt Amortization", value = -loanPayments, isPositive = false)
                                }

                                HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Net Daily Profit / Burn", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${if (netProfit >= 0) "+$" else "-$"}$%,.0f/day".format(kotlin.math.abs(netProfit)),
                                        color = if (netProfit >= 0) Color(0xFF34D399) else Color(0xFFEF4444),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    // Loans & Debt Facilities
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Commercial Credit Lines & Loans", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                if (state.activeLoans.isEmpty()) {
                                    Text("No active liabilities. Excellent debt rating.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                } else {
                                    state.activeLoans.forEach { loan ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF1E293B),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(loan.lender, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("${loan.daysRemaining} days left | $%,.0f/day".format(loan.dailyPayment), color = Color(0xFF94A3B8), fontSize = 10.sp)
                                                }
                                                Button(
                                                    onClick = { onRepayLoanEarly(loan) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text("Pay Off Early", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Available Venture Loans:", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))

                                val loanOffers = listOf(
                                    Triple("Angel Bridge Loan", 15000.0, 30),
                                    Triple("Growth Capital Loan", 40000.0, 45),
                                    Triple("Series Credit Line", 100000.0, 60)
                                )

                                loanOffers.forEach { (name, amount, days) ->
                                    val daily = (amount * 1.12) / days
                                    val canBorrow = state.activeLoans.none { it.lender == name }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("$%,.0f over %d days ($%,.0f/day)".format(amount, days, daily), color = Color(0xFF94A3B8), fontSize = 10.sp)
                                            }
                                            Button(
                                                onClick = { onTakeLoan(amount, days, daily, name) },
                                                enabled = canBorrow,
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("Borrow", color = Color(0xFF090D16), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // === MARKET LEADERBOARD & RIVALS ===
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Valuation Formula Breakdown Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Audited Company Valuation Formula", color = Color(0xFF00E5FF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Valuation = Cash + Physical Assets - Total Debt + (Annualized Revenue × 4.5 × Satisfaction) + (Active Users × $15) + Tech Tree Bonus + Brand Equity",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Your Global Valuation", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text("$%,.0f".format(state.valuation), color = Color(0xFFFBBF24), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Market Share", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text("%.1f%%".format(state.marketShare), color = Color(0xFF00E5FF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Monthly Run Rate", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text("$%,.0f/mo".format(state.monthlyRevenue), color = Color(0xFF34D399), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Tech Tycoon Global Leaderboard", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    items(state.leaderboard, key = { it.companyId }) { entry ->
                        val isPlayer = entry.isPlayer
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPlayer) Color(0xFF1E293B) else Color(0xFF131B2E)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isPlayer) Color(0xFF00E5FF) else if (entry.isAcquired) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (entry.rank == 1) Color(0xFFFBBF24) else Color(0xFF334155),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "#${entry.rank}",
                                                    color = if (entry.rank == 1) Color(0xFF090D16) else Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = entry.companyName,
                                                    color = if (isPlayer) Color(0xFF00E5FF) else Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (isPlayer) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0xFF00E5FF).copy(alpha = 0.2f)
                                                    ) {
                                                        Text("YOU", color = Color(0xFF00E5FF), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${entry.statusText} | Market Share: %.1f%%".format(entry.marketShare),
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$%,.0f".format(entry.valuation),
                                            color = Color(0xFFFBBF24),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "$%,.0f/mo".format(entry.monthlyRevenue),
                                            color = Color(0xFF34D399),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                if (!isPlayer) {
                                    val competitor = state.competitors.find { it.id == entry.companyId }
                                    if (competitor != null && !competitor.isAcquired) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Users: %,d | Staff: %d".format(competitor.activeUsers, competitor.employeeCount), color = Color(0xFF94A3B8), fontSize = 10.sp)

                                            val canAfford = state.cash >= competitor.acquisitionCost
                                            Button(
                                                onClick = { competitorToAcquire = competitor },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (canAfford) Color(0xFFF59E0B) else Color(0xFF334155)
                                                ),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.testTag("btn_acquire_${competitor.id}")
                                            ) {
                                                Text(
                                                    text = "Acquire ($%,.0f)".format(competitor.acquisitionCost),
                                                    color = if (canAfford) Color(0xFF090D16) else Color(0xFF94A3B8),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    } else if (competitor != null && competitor.isAcquired) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("✓ Wholly-owned subsidiary of ${state.companyName}", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // === TECH RESEARCH TREE ===
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        state.activeResearch?.let { research ->
                            val tech = TechUpgrade.entries.find { it.id == research.techId }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                                border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Active R&D Investigation", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(tech?.title ?: "Technology", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { research.progress },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = Color(0xFF00E5FF),
                                        trackColor = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${research.daysInvested} of ${research.daysRequired} days investigated", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    items(TechUpgrade.entries) { tech ->
                        val isUnlocked = state.unlockedTechIds.contains(tech.id)
                        val isCurrent = state.activeResearch?.techId == tech.id
                        val canAfford = state.cash >= tech.cost
                        val prereqsMet = tech.prereqId == null || state.unlockedTechIds.contains(tech.prereqId)

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUnlocked) Color(0xFF131B2E) else Color(0xFF0D1322)
                            ),
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isUnlocked -> Color(0xFF10B981)
                                    isCurrent -> Color(0xFF00E5FF)
                                    else -> Color(0xFF1E293B)
                                }
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(tech.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        if (isUnlocked) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Text(tech.description, color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    Text("Cost: $%,.0f | Duration: %d days".format(tech.cost, tech.requiredResearchDays), color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                }

                                if (!isUnlocked && !isCurrent) {
                                    Button(
                                        onClick = { onStartResearch(tech) },
                                        enabled = canAfford && prereqsMet && state.activeResearch == null,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Research", color = Color(0xFF090D16), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal for Acquisition Buyout
    competitorToAcquire?.let { comp ->
        AlertDialog(
            onDismissRequest = { competitorToAcquire = null },
            title = { Text("Corporate Acquisition: ${comp.name}", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = Color(0xFF131B2E),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Buyout Terms & Financial Impact:", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Acquisition Price:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("$%,.0f Cash".format(comp.acquisitionCost), color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Absorbed User Base:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("+%,d Active Users".format((comp.activeUsers * 0.40).toInt()), color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Engineering Talent:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("+2 Senior Tech Leads", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Brand Reputation:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("+45 Reputation Points", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = "Are you sure you want to execute this takeover? Cash will be deducted immediately.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAcquireCompetitor(comp.id)
                        competitorToAcquire = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) {
                    Text("Confirm Buyout", color = Color(0xFF090D16), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { competitorToAcquire = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
private fun LedgerRow(label: String, value: Double, isPositive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFFCBD5E1), fontSize = 12.sp)
        Text(
            text = "${if (value >= 0) "+$" else "-$"}$%,.0f".format(kotlin.math.abs(value)),
            color = if (isPositive) Color(0xFF34D399) else Color(0xFFF87171),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
