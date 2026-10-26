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
import androidx.compose.ui.text.style.TextAlign
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
    onSellEquity: (Float) -> Unit = {},
    onClaimMilestone: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSubtab by remember { mutableIntStateOf(0) } // 0: Financials & P&L, 1: Analytics & Reports, 2: Market & Rivals, 3: Tech & Milestones
    var competitorToAcquire by remember { mutableStateOf<CompetitorCompany?>(null) }
    var leaderboardSortBy by remember { mutableStateOf("Valuation") }

    val sortedLeaderboard = remember(state.leaderboard, leaderboardSortBy) {
        when (leaderboardSortBy) {
            "Revenue" -> state.leaderboard.sortedByDescending { it.monthlyRevenue }
            "Market Share" -> state.leaderboard.sortedByDescending { it.marketShare }
            "Reputation" -> state.leaderboard.sortedByDescending { it.reputation }
            else -> state.leaderboard.sortedByDescending { it.valuation }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
    ) {
        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedSubtab,
            containerColor = Color(0xFF0F172A),
            contentColor = Color(0xFF00E5FF),
            modifier = Modifier.fillMaxWidth().testTag("finance_subtabs")
        ) {
            Tab(
                selected = selectedSubtab == 0,
                onClick = { selectedSubtab = 0 },
                text = { Text("P&L & Debt", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                modifier = Modifier.testTag("subtab_finance_pnl")
            )
            Tab(
                selected = selectedSubtab == 1,
                onClick = { selectedSubtab = 1 },
                text = { Text("Reports", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                modifier = Modifier.testTag("subtab_analytics_reports")
            )
            Tab(
                selected = selectedSubtab == 2,
                onClick = { selectedSubtab = 2 },
                text = { Text("Market", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                modifier = Modifier.testTag("subtab_market_rivals")
            )
            Tab(
                selected = selectedSubtab == 3,
                onClick = { selectedSubtab = 3 },
                text = {
                    val claimableCount = state.milestones.count { it.isAchieved && !it.isClaimed }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Tech & Goals", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        if (claimableCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF10B981)
                            ) {
                                Text("$claimableCount", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                            }
                        }
                    }
                },
                modifier = Modifier.testTag("subtab_tech_milestones")
            )
        }

        when (selectedSubtab) {
            0 -> {
                // =========================================================================
                // SUBTAB 0: FINANCIALS, P&L, DEBT & CAPITAL
                // =========================================================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Valuation & Equity Summary Card
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
                                        Text("Market Valuation", color = Color(0xFF94A3B8), fontSize = 11.sp)
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

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Daily Net Cashflow", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text(
                                            text = "${if (state.dailyNetProfit >= 0) "+$" else "-$"}$%,.0f/day".format(kotlin.math.abs(state.dailyNetProfit)),
                                            color = if (state.dailyNetProfit >= 0) Color(0xFF34D399) else Color(0xFFF87171),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Column {
                                        Text("Daily Burn Rate", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text(
                                            text = "$%,.0f/day".format(state.burnRate),
                                            color = Color(0xFFF87171),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Column {
                                        Text("Cash Runway", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text(
                                            text = if (state.cashRunwayDays >= 999) "Profitable" else "${state.cashRunwayDays} days",
                                            color = if (state.cashRunwayDays > 30) Color(0xFF34D399) else Color(0xFFF87171),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Daily P&L Statement Card
                    item {
                        val ledger = state.todayLedger
                        val estProductRev = if (ledger.productRevenue > 0) ledger.productRevenue else state.effectiveDailyRevenue
                        val estSalaries = if (ledger.payrollExpenses > 0) ledger.payrollExpenses else state.employees.sumOf { it.dailySalary }
                        val estRent = if (ledger.rentExpenses > 0) ledger.rentExpenses else (state.officeTier.monthlyRent / 30.0)
                        val estServer = if (ledger.serverExpenses > 0) ledger.serverExpenses else state.server.calculateDailyCost(state.totalActiveUsers)
                        val estMarketing = if (ledger.marketingExpenses > 0) ledger.marketingExpenses else state.products.sumOf { it.dailyMarketingSpend }
                        val estLoans = if (ledger.loanRepaymentExpenses > 0) ledger.loanRepaymentExpenses else state.activeLoans.sumOf { it.dailyPayment }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Operating Profit & Loss Statement (Today)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))

                                LedgerRow("Product Gross Revenue", estProductRev, isPositive = true)
                                Spacer(modifier = Modifier.height(4.dp))
                                LedgerRow("Contract Milestones & Rewards", ledger.contractRevenue, isPositive = true)
                                Spacer(modifier = Modifier.height(4.dp))
                                LedgerRow("Payroll & Employee Salaries", -estSalaries, isPositive = false)
                                Spacer(modifier = Modifier.height(4.dp))
                                LedgerRow("Office Space Lease & Facilities", -estRent, isPositive = false)
                                Spacer(modifier = Modifier.height(4.dp))
                                LedgerRow("Server Hosting & Cloud Bandwidth", -estServer, isPositive = false)
                                Spacer(modifier = Modifier.height(4.dp))
                                LedgerRow("Marketing & User Acquisition", -estMarketing, isPositive = false)
                                Spacer(modifier = Modifier.height(4.dp))
                                LedgerRow("Active Debt Service & Interest", -estLoans, isPositive = false)
                                Spacer(modifier = Modifier.height(4.dp))
                                LedgerRow("Corporate Taxes & Filings", -ledger.taxExpenses, isPositive = false)

                                HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Net Daily Earnings", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${if (state.dailyNetProfit >= 0) "+$" else "-$"}$%,.0f".format(kotlin.math.abs(state.dailyNetProfit)),
                                        color = if (state.dailyNetProfit >= 0) Color(0xFF34D399) else Color(0xFFF87171),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Debt & Credit Management Card
                    item {
                        val totalDebt = state.activeLoans.sumOf { it.remainingBalance }
                        val maxCreditLimit = (state.valuation * 0.40).coerceAtLeast(15000.0)
                        val remainingCredit = (maxCreditLimit - totalDebt).coerceAtLeast(0.0)

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
                                    Text("Debt Facility & Commercial Loans", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Credit Limit: $%,.0f".format(maxCreditLimit), color = Color(0xFF94A3B8), fontSize = 10.sp)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (state.activeLoans.isEmpty()) {
                                    Text("Company is debt-free. Available borrowing limit: $%,.0f".format(remainingCredit), color = Color(0xFF34D399), fontSize = 11.sp)
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        state.activeLoans.forEach { loan ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF1E293B),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(loan.lender, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        Text("Balance: $%,.0f | $%,.0f/day (%d days left)".format(loan.remainingBalance, loan.dailyPayment, loan.daysRemaining), color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                                    }
                                                    Button(
                                                        onClick = { onRepayLoanEarly(loan) },
                                                        enabled = state.cash >= loan.remainingBalance,
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                        shape = RoundedCornerShape(6.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text("Pay Off", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Available Loan Products:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))

                                val loanOffers = listOf(
                                    Triple(5000.0, 15, "Silicon Microfinance (Quick Cash)"),
                                    Triple(25000.0, 30, "Bay Area Commercial Bank (Growth Term)"),
                                    Triple(75000.0, 45, "Tech Syndicate Credit Facility")
                                )

                                loanOffers.forEach { (amount, termDays, lender) ->
                                    val interestRate = 1.15
                                    val totalRepayment = amount * interestRate
                                    val dailyPay = totalRepayment / termDays
                                    val isEligible = remainingCredit >= amount

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0F172A),
                                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(lender, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("$%,.0f Principal | $%,.0f/day for %d days".format(amount, dailyPay, termDays), color = Color(0xFF94A3B8), fontSize = 10.sp)
                                            }
                                            Button(
                                                onClick = { onTakeLoan(amount, termDays, dailyPay, lender) },
                                                enabled = isEligible,
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(if (isEligible) "Borrow" else "Max Limit", color = Color(0xFF090D16), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Equity Financing & VC Investment Card
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
                                    Text("Equity Financing & Venture Capital", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Retained: %.0f%%".format(state.equityOwnedPercent), color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("Trade equity for non-dilutable cash without recurring debt service. You must retain at least 25% voting control.", color = Color(0xFF94A3B8), fontSize = 10.sp)

                                Spacer(modifier = Modifier.height(10.dp))

                                val equityRounds = listOf(
                                    Pair("Angel Syndicate Pitch (5% Equity)", 5.0f),
                                    Pair("Seed Venture Round (10% Equity)", 10.0f),
                                    Pair("Series A Growth Round (20% Equity)", 20.0f)
                                )

                                equityRounds.forEach { (roundTitle, pct) ->
                                    val raiseAmount = state.valuation * (pct / 100.0)
                                    val canSell = (state.equityOwnedPercent - pct) >= 25.0f

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
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(roundTitle, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "Capital Injection: +$%,.0f".format(raiseAmount),
                                                    color = Color(0xFF34D399),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Button(
                                                onClick = { onSellEquity(pct) },
                                                enabled = canSell,
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(if (canSell) "Sell ${pct.toInt()}%" else "Limit Reached", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                // =========================================================================
                // SUBTAB 1: ANALYTICS, DECISIONS & REPORTS DASHBOARD
                // =========================================================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Executive KPI Summary
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Executive KPI Summary", color = Color(0xFF00E5FF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Operating Days", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text("Day ${state.clock.day}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Market Share", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text("%.1f%%".format(state.marketShare), color = Color(0xFF00E5FF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Monthly Run Rate", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text("$%,.0f".format(state.monthlyRevenue), color = Color(0xFF34D399), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Brand Trust", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text("★ ${state.reputation}", color = Color(0xFFFBBF24), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Product Portfolio Performance Table
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
                                    Text("Product Portfolio Performance", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("${state.products.size} Products", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                if (state.products.isEmpty()) {
                                    Text("No products in portfolio yet. Launch one in the Products tab!", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                } else {
                                    state.products.forEach { prod ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF1E293B),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(prod.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                        Text("${prod.category.title} • v${prod.version}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                                    }
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = if (prod.stage == ProductStage.LIVE) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF38BDF8).copy(alpha = 0.2f)
                                                    ) {
                                                        Text(
                                                            prod.stage.name,
                                                            color = if (prod.stage == ProductStage.LIVE) Color(0xFF34D399) else Color(0xFF38BDF8),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text("Active Users", color = Color(0xFF94A3B8), fontSize = 9.sp)
                                                        Text("%,d".format(prod.activeUsers), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Column {
                                                        Text("Daily Rev", color = Color(0xFF94A3B8), fontSize = 9.sp)
                                                        Text("$%,.0f".format(prod.dailyRevenue), color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Column {
                                                        Text("Rating", color = Color(0xFF94A3B8), fontSize = 9.sp)
                                                        Text("★ %.1f".format(prod.rating), color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Column {
                                                        Text("Bugs / Debt", color = Color(0xFF94A3B8), fontSize = 9.sp)
                                                        Text("${prod.bugsCount} / ${prod.techDebt}", color = if (prod.bugsCount > 5) Color(0xFFF87171) else Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Workforce & Productivity Analytics Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Workforce & Productivity Diagnostics", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Staff Count", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text("${state.employees.size} / ${state.totalWorkstations}", color = Color(0xFF00E5FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Avg Skill", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text("${state.avgEmployeeSkill}", color = Color(0xFFFBBF24), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Avg Morale", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text("${state.avgEmployeeMorale}%", color = Color(0xFF34D399), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Avg Energy", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        Text("${state.avgEmployeeEnergy}%", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                state.topEmployee?.let { star ->
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                                            Column {
                                                Text("Top Star Talent: ${star.name}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("${star.role.title} • ${star.levelTitle} • Skill ${star.skill}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Transparent Valuation Breakdown Card
                    item {
                        val activeUsersTotal = state.products.sumOf { it.activeUsers }
                        val annualRevenue = state.monthlyRevenue * 12.0
                        val revenueMultipleVal = annualRevenue * 4.5 * (state.reputation / 100.0).coerceIn(0.5, 2.0)
                        val userEquityVal = activeUsersTotal * 15.0
                        val physicalAssetsVal = state.furniture.sumOf { it.type.cost * 0.70 } + (state.officeTier.ordinal * 14000.0)
                        val techBonusVal = state.unlockedTechIds.size * 12000.0
                        val totalDebtLiab = state.activeLoans.sumOf { it.remainingBalance }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Audited Valuation Formula Breakdown", color = Color(0xFFFBBF24), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                LedgerRow("1. Liquid Cash Reserves", state.cash, isPositive = true)
                                Spacer(modifier = Modifier.height(4.dp))
                                LedgerRow("2. Physical Assets (Office & Furniture)", physicalAssetsVal, isPositive = true)
                                Spacer(modifier = Modifier.height(4.dp))
                                LedgerRow("3. Outstanding Debt Liabilities", -totalDebtLiab, isPositive = false)
                                Spacer(modifier = Modifier.height(4.dp))
                                LedgerRow("4. Annualized Revenue Multiple (4.5x)", revenueMultipleVal, isPositive = true)
                                Spacer(modifier = Modifier.height(4.dp))
                                LedgerRow("5. Digital User Base Asset ($15/DAU)", userEquityVal, isPositive = true)
                                Spacer(modifier = Modifier.height(4.dp))
                                LedgerRow("6. R&D Intellectual Property Bonus", techBonusVal, isPositive = true)

                                HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Certified Enterprise Valuation", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("$%,.0f".format(state.valuation), color = Color(0xFFFBBF24), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // =========================================================================
                // SUBTAB 2: MARKET LEADERBOARD & AI COMPETITOR TAKEOVERS
                // =========================================================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tech Tycoon Global Leaderboard", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("Valuation", "Revenue", "Market Share", "Reputation").forEach { sortKey ->
                                    val isSelected = leaderboardSortBy == sortKey
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF334155)),
                                        modifier = Modifier.weight(1f).clickable { leaderboardSortBy = sortKey }
                                    ) {
                                        Text(
                                            text = sortKey,
                                            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(vertical = 5.dp).fillMaxWidth(),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(sortedLeaderboard, key = { it.companyId }) { entry ->
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
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(entry.companyName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                if (isPlayer) {
                                                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF00E5FF).copy(alpha = 0.2f)) {
                                                        Text("YOU", color = Color(0xFF00E5FF), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                    }
                                                }
                                                if (entry.isAcquired) {
                                                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF10B981).copy(alpha = 0.2f)) {
                                                        Text("SUBSIDIARY", color = Color(0xFF34D399), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                    }
                                                }
                                            }
                                            Text(entry.statusText.ifEmpty { entry.latestActivity }, color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("$%,.0f".format(entry.valuation), color = Color(0xFFFBBF24), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("%.1f%% Mkt Share".format(entry.marketShare), color = Color(0xFF38BDF8), fontSize = 10.sp)
                                    }
                                }

                                if (!isPlayer && !entry.isAcquired) {
                                    val competitor = state.competitors.find { it.id == entry.companyId }
                                    competitor?.let { comp ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${comp.status} • ${comp.employeeCount} Staff • %,d Users".format(comp.activeUsers),
                                                color = when (comp.status) {
                                                    "Dominant" -> Color(0xFFFBBF24)
                                                    "Growing" -> Color(0xFF34D399)
                                                    else -> Color(0xFFF87171)
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            Button(
                                                onClick = { competitorToAcquire = comp },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("Acquisition Bid", color = Color(0xFF090D16), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // =========================================================================
                // SUBTAB 3: TECH RESEARCH TREE & COMPANY MILESTONES
                // =========================================================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Active R&D Progress
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

                    // Company Milestones & Achievements Section
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(18.dp))
                                    Text("Company Milestones & Goals", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                val completedCount = state.milestones.count { it.isAchieved }
                                Text("$completedCount / ${state.milestones.size} Unlocked", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }

                    items(state.milestones, key = { it.id }) { milestone ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (milestone.isClaimed) Color(0xFF0F172A) else if (milestone.isAchieved) Color(0xFF1E293B) else Color(0xFF131B2E)
                            ),
                            border = BorderStroke(
                                1.dp,
                                when {
                                    milestone.isClaimed -> Color(0xFF334155)
                                    milestone.isAchieved -> Color(0xFF10B981)
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
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(milestone.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        if (milestone.isClaimed) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF334155)) {
                                                Text("CLAIMED", color = Color(0xFF94A3B8), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                            }
                                        } else if (milestone.isAchieved) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF10B981).copy(alpha = 0.2f)) {
                                                Text("COMPLETED!", color = Color(0xFF34D399), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                            }
                                        }
                                    }
                                    Text(milestone.description, color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Reward: +$%,.0f Cash • +%d Reputation".format(milestone.rewardCash, milestone.rewardReputation), color = Color(0xFFFBBF24), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }

                                if (milestone.isAchieved && !milestone.isClaimed) {
                                    Button(
                                        onClick = { onClaimMilestone(milestone.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("claim_milestone_${milestone.id}")
                                    ) {
                                        Text("Claim", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (!milestone.isAchieved) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1E293B)) {
                                        Text("In Progress", color = Color(0xFF64748B), fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Tech Tree Section Header
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AccountTree, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                            Text("R&D Tech Tree Innovations", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(tech.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        if (isUnlocked) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(15.dp))
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
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("research_btn_${tech.id}")
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
        val canAfford = state.cash >= comp.acquisitionCost
        val discountLabel = when (comp.status) {
            "Struggling" -> "Distressed Buyout (30% Discount)"
            "Growing" -> "Fair Market Valuation (8% Discount)"
            else -> "Strategic Control Premium (+5%)"
        }

        AlertDialog(
            onDismissRequest = { competitorToAcquire = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Acquire ${comp.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (comp.status == "Struggling") Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f)
                    ) {
                        Text(comp.status, color = if (comp.status == "Struggling") Color(0xFFF87171) else Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            },
            containerColor = Color(0xFF131B2E),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Negotiation Terms: $discountLabel", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Acquisition Price:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("$%,.0f Cash".format(comp.acquisitionCost), color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Your Available Cash:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("$%,.0f".format(state.cash), color = if (canAfford) Color(0xFF34D399) else Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 2.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Absorbed Active Users:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("+%,d Active Users".format((comp.activeUsers * 0.45).toInt()), color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Acquired Senior Talent:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("+2 Specialists (Lead Dev & Senior PM)", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Digital Product Rights:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text(comp.productNames.firstOrNull() ?: "Subsidiary Suite", color = Color(0xFFA855F7), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Brand Prestige Gain:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("+45 Reputation Points", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Risk Advisory
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("⚠ Post-Merger Operational Advisory:", color = Color(0xFFFBBF24), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Absorbed staff will add +$330/day to company payroll. Ensure server capacity in the Servers tab can support %,d absorbed users.".format((comp.activeUsers * 0.45).toInt()), color = Color(0xFFCBD5E1), fontSize = 10.sp)
                        }
                    }

                    if (!canAfford) {
                        Text(
                            text = "Insufficient funds! You need $%,.0f more cash to execute this takeover.".format(comp.acquisitionCost - state.cash),
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAcquireCompetitor(comp.id)
                        competitorToAcquire = null
                    },
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        disabledContainerColor = Color(0xFF334155)
                    )
                ) {
                    Text(
                        text = if (canAfford) "Execute Strategic Buyout" else "Insufficient Funds",
                        color = if (canAfford) Color(0xFF090D16) else Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold
                    )
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
