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
fun ProductsTab(
    state: GameState,
    onCreateProduct: (name: String, category: ProductCategory, monetization: MonetizationModel) -> Unit,
    onLaunchProduct: (Product) -> Unit,
    onChangeMonetization: (Product, MonetizationModel) -> Unit,
    onChangeMarketingBudget: (Product, Double) -> Unit,
    onLaunchCampaign: (Product, CampaignType) -> Unit,
    onSetSprintMode: (Product, SprintMode) -> Unit,
    onStartProductUpdate: (Product) -> Unit,
    onToggleMaintenance: (Product) -> Unit = {},
    onConductMarketResearch: (Product) -> Unit = {},
    onStartFeatureDevelopment: (String, String) -> Unit = { _, _ -> },
    onAcceptContract: (String) -> Unit = {},
    onCancelContract: (String) -> Unit = {},
    onDeliverContract: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Products, 1: Client Contracts
    var showCreateDialog by remember { mutableStateOf(false) }
    var productForCampaign by remember { mutableStateOf<Product?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
    ) {
        // Section Switcher: Products vs Client Contracts
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = Color(0xFF0F172A),
            contentColor = Color(0xFF00E5FF),
            modifier = Modifier.fillMaxWidth().testTag("products_contracts_subtabs")
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = { Text("My Products (${state.products.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("subtab_my_products")
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = {
                    val activeContractsCount = state.contracts.count { it.status == ContractStatus.IN_PROGRESS }
                    Text(
                        text = if (activeContractsCount > 0) "Client Contracts ($activeContractsCount Active)" else "Client Contracts (${state.contracts.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("subtab_client_contracts")
            )
        }

        if (selectedSection == 0) {
            // === MY PRODUCTS SECTION ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Digital Products Portfolio",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.products.count { it.stage == ProductStage.LIVE }} Live | ${state.products.count { it.stage != ProductStage.LIVE }} In Pipeline",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("create_new_product_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF090D16), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Product", color = Color(0xFF090D16), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.products, key = { it.id }) { prod ->
                    val assignedCount = state.employees.count { it.assignedProductId == prod.id }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                        border = BorderStroke(
                            1.dp,
                            when (prod.stage) {
                                ProductStage.LIVE -> Color(0xFF10B981).copy(alpha = 0.5f)
                                ProductStage.MAINTENANCE -> Color(0xFFF59E0B).copy(alpha = 0.6f)
                                else -> Color(0xFF1E293B)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header: Name, Category, Stage
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = when (prod.category) {
                                            ProductCategory.SOCIAL_APP -> Icons.Default.Share
                                            ProductCategory.SAAS_TOOL -> Icons.Default.Work
                                            ProductCategory.MOBILE_GAME -> Icons.Default.SportsEsports
                                            ProductCategory.ECOMMERCE -> Icons.Default.ShoppingCart
                                            ProductCategory.AI_ASSISTANT -> Icons.Default.Psychology
                                        },
                                        contentDescription = null,
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = prod.name,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${prod.category.title} (v${prod.version})",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (prod.stage) {
                                        ProductStage.PLANNING -> Color(0xFF64748B).copy(alpha = 0.25f)
                                        ProductStage.DEVELOPMENT -> Color(0xFF00E5FF).copy(alpha = 0.25f)
                                        ProductStage.TESTING -> Color(0xFFF59E0B).copy(alpha = 0.25f)
                                        ProductStage.READY_TO_LAUNCH -> Color(0xFF10B981).copy(alpha = 0.25f)
                                        ProductStage.LIVE -> Color(0xFF10B981).copy(alpha = 0.25f)
                                        ProductStage.MAINTENANCE -> Color(0xFFEAB308).copy(alpha = 0.25f)
                                    }
                                ) {
                                    Text(
                                        text = prod.stage.name.replace("_", " "),
                                        color = when (prod.stage) {
                                            ProductStage.PLANNING -> Color(0xFFCBD5E1)
                                            ProductStage.DEVELOPMENT -> Color(0xFF00E5FF)
                                            ProductStage.TESTING -> Color(0xFFF59E0B)
                                            ProductStage.READY_TO_LAUNCH -> Color(0xFF10B981)
                                            ProductStage.LIVE -> Color(0xFF34D399)
                                            ProductStage.MAINTENANCE -> Color(0xFFFACC15)
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            when (prod.stage) {
                                ProductStage.PLANNING -> {
                                    Text("Planning & Specs Phase (Designers & PMs required)", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    DevProgressBar(
                                        label = "Architecture & Wireframes",
                                        current = prod.planningPointsCurrent,
                                        needed = prod.planningPointsNeeded,
                                        color = Color(0xFFA855F7)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "$assignedCount Staff Assigned | Progress: ${(prod.overallDevProgress * 100).toInt()}%",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }

                                ProductStage.DEVELOPMENT, ProductStage.TESTING -> {
                                    DevProgressBar(
                                        label = "Design & UI Assets",
                                        current = prod.designPointsCurrent,
                                        needed = prod.designPointsNeeded,
                                        color = Color(0xFFEC4899)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    DevProgressBar(
                                        label = "Code & Engine",
                                        current = prod.codePointsCurrent,
                                        needed = prod.codePointsNeeded,
                                        color = Color(0xFF00E5FF)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    DevProgressBar(
                                        label = "QA Test Suite",
                                        current = prod.testPointsCurrent,
                                        needed = prod.testPointsNeeded,
                                        color = Color(0xFF10B981)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text("$assignedCount Staff Assigned", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                            if (prod.bugsCount > 0) {
                                                Text("${prod.bugsCount} Bugs", color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            SprintMode.entries.forEach { mode ->
                                                val isSelected = prod.sprintMode == mode
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF334155)),
                                                    modifier = Modifier.clickable { onSetSprintMode(prod, mode) }
                                                ) {
                                                    Text(
                                                        text = mode.title,
                                                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                ProductStage.READY_TO_LAUNCH -> {
                                    Text("Development & Testing 100% Complete! Ready for market.", color = Color(0xFF10B981), fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { onLaunchProduct(prod) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("launch_product_${prod.id}")
                                    ) {
                                        Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Launch to Global Market!", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                ProductStage.LIVE -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Active Users", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                            Text("%,d DAU".format(prod.activeUsers), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Daily Revenue", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                            Text("$%,.0f/day".format(prod.dailyRevenue), color = Color(0xFF34D399), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Rating", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                            Text("★ %.1f".format(prod.rating), color = Color(0xFFFBBF24), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Tech Debt", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                            Text("${prod.techDebt}", color = if (prod.techDebt > 10) Color(0xFFF87171) else Color(0xFF94A3B8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action buttons for Live Product
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { productForCampaign = prod },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color(0xFF00E5FF))
                                        ) {
                                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Campaign", fontSize = 11.sp, color = Color(0xFF00E5FF))
                                        }

                                        OutlinedButton(
                                            onClick = { onToggleMaintenance(prod) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color(0xFFF59E0B))
                                        ) {
                                            Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Maintenance", fontSize = 11.sp, color = Color(0xFFF59E0B))
                                        }

                                        OutlinedButton(
                                            onClick = { onStartProductUpdate(prod) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color(0xFF38BDF8))
                                        ) {
                                            Icon(Icons.Default.Upgrade, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("vNext", fontSize = 11.sp, color = Color(0xFF38BDF8))
                                        }
                                    }
                                }

                                ProductStage.MAINTENANCE -> {
                                    Text("Refactoring Codebase & Bug Eradication Phase", color = Color(0xFFFACC15), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Remaining Bugs: ${prod.bugsCount}", color = Color(0xFFF87171), fontSize = 11.sp)
                                        Text("Tech Debt Points: ${prod.techDebt}", color = Color(0xFFFBBF24), fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { onToggleMaintenance(prod) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Return to LIVE Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // --- MARKET FEASIBILITY STUDY ---
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.height(8.dp))

                            if (prod.marketResearch == null) {
                                OutlinedButton(
                                    onClick = { onConductMarketResearch(prod) },
                                    enabled = state.cash >= 800.0,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFA855F7)),
                                    modifier = Modifier.fillMaxWidth().testTag("research_button_${prod.id}")
                                ) {
                                    Icon(Icons.Default.Analytics, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Conduct Market Feasibility Study ($800)", fontSize = 11.sp, color = Color(0xFFA855F7), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                val report = prod.marketResearch
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1E293B).copy(alpha = 0.6f),
                                    border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(14.dp))
                                                Text("Market Study: ${report.marketSaturation}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text("Demand: ${(report.demandScore * 100).toInt()}%", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Target: ${report.targetAudience}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                            Text("Est. Cap: %,d users".format(report.estimatedMaxUsers), color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                        }
                                    }
                                }
                            }

                            // --- FEATURE ROADMAP EXPANDER ---
                            if (prod.features.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                var isRoadmapExpanded by remember { mutableStateOf(false) }
                                val implementedCount = prod.features.count { it.isImplemented }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1E293B).copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, Color(0xFF334155)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { isRoadmapExpanded = !isRoadmapExpanded },
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.Route, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(15.dp))
                                                Text("Feature Roadmap ($implementedCount/${prod.features.size} Unlocked)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Icon(
                                                imageVector = if (isRoadmapExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        if (isRoadmapExpanded) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            prod.features.forEach { feat ->
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFF0F172A),
                                                    border = BorderStroke(1.dp, if (feat.isImplemented) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFF1E293B)),
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(8.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                                            Text(feat.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                            Text(feat.description, color = Color(0xFF94A3B8), fontSize = 9.sp, maxLines = 2)
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(
                                                                text = "+$%.3f ARPU | +${(feat.satisfactionBoost * 100).toInt()}% Sat".format(feat.arpuBoost),
                                                                color = Color(0xFF34D399),
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }

                                                        when {
                                                            feat.isImplemented -> {
                                                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF10B981).copy(alpha = 0.2f)) {
                                                                    Text("Active", color = Color(0xFF10B981), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                                }
                                                            }
                                                            feat.isUnderDevelopment -> {
                                                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF00E5FF).copy(alpha = 0.2f)) {
                                                                    Text("Building...", color = Color(0xFF00E5FF), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                                }
                                                            }
                                                            else -> {
                                                                Button(
                                                                    onClick = { onStartFeatureDevelopment(prod.id, feat.id) },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                                                    shape = RoundedCornerShape(4.dp),
                                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                                ) {
                                                                    Text("Queue", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // --- CUSTOMER REVIEWS (FOR LIVE PRODUCTS) ---
                            if (prod.customerReviews.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                var isReviewsExpanded by remember { mutableStateOf(false) }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1E293B).copy(alpha = 0.3f),
                                    border = BorderStroke(1.dp, Color(0xFF334155)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { isReviewsExpanded = !isReviewsExpanded },
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(15.dp))
                                                Text("User Reviews (${prod.customerReviews.size})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Icon(
                                                imageVector = if (isReviewsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = Color(0xFF94A3B8),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        if (isReviewsExpanded) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            prod.customerReviews.takeLast(3).reversed().forEach { review ->
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFF0F172A),
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp)) {
                                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                            Text(review.author, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                            Text("★".repeat(review.rating), color = Color(0xFFFBBF24), fontSize = 10.sp)
                                                        }
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(review.comment, color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // === CLIENT CONTRACTS SECTION ===
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Company Reputation", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                Text("★ ${state.reputation}", color = Color(0xFFFBBF24), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Active Contracts", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                Text("${state.contracts.count { it.status == ContractStatus.IN_PROGRESS }}", color = Color(0xFF00E5FF), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Available Bids", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                Text("${state.contracts.count { it.status == ContractStatus.AVAILABLE }}", color = Color(0xFF34D399), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                items(state.contracts, key = { it.id }) { contract ->
                    val isReputationSufficient = state.reputation >= contract.minReputation
                    val assignedStaffCount = state.employees.count { it.assignedContractId == contract.id }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                        border = BorderStroke(
                            1.dp,
                            when (contract.status) {
                                ContractStatus.IN_PROGRESS -> Color(0xFF00E5FF).copy(alpha = 0.6f)
                                ContractStatus.COMPLETED -> Color(0xFF10B981).copy(alpha = 0.5f)
                                ContractStatus.FAILED -> Color(0xFFEF4444).copy(alpha = 0.5f)
                                else -> Color(0xFF1E293B)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(contract.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text("Client: ${contract.clientName}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(contract.tier.badgeColor).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = contract.tier.title,
                                        color = Color(contract.tier.badgeColor),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Financials row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Upfront: +$%,.0f".format(contract.upfrontDeposit), color = Color(0xFF34D399), fontSize = 11.sp)
                                Text("Final: +$%,.0f".format(contract.completionReward), color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Penalty: -$%,.0f".format(contract.penaltyCost), color = Color(0xFFF87171), fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Requirements / Progress
                            if (contract.status == ContractStatus.IN_PROGRESS) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Deadline: ${contract.daysRemaining} days remaining", color = if (contract.daysRemaining <= 3) Color(0xFFEF4444) else Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("$assignedStaffCount Staff Assigned", color = Color(0xFF00E5FF), fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                DevProgressBar("Dev Coding", contract.currentDevPoints, contract.requiredDevPoints, Color(0xFF00E5FF))
                                Spacer(modifier = Modifier.height(4.dp))
                                DevProgressBar("UI Design", contract.currentDesignPoints, contract.requiredDesignPoints, Color(0xFFEC4899))
                                Spacer(modifier = Modifier.height(4.dp))
                                DevProgressBar("QA Testing", contract.currentQAPoints, contract.requiredQAPoints, Color(0xFF10B981))

                                Spacer(modifier = Modifier.height(8.dp))

                                // Milestones list
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    contract.milestones.forEach { ms ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(
                                                    imageVector = if (ms.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                    contentDescription = null,
                                                    tint = if (ms.isCompleted) Color(0xFF10B981) else Color(0xFF64748B),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(ms.title, color = if (ms.isCompleted) Color(0xFFCBD5E1) else Color(0xFF94A3B8), fontSize = 10.sp)
                                            }
                                            Text(
                                                text = if (ms.isCompleted) "PAID +$%,.0f".format(ms.rewardPayout) else "+$%,.0f".format(ms.rewardPayout),
                                                color = if (ms.isCompleted) Color(0xFF10B981) else Color(0xFFFBBF24),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (contract.isDeliverable) {
                                    Button(
                                        onClick = { onDeliverContract(contract.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("deliver_contract_${contract.id}")
                                    ) {
                                        Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Deliver Contract (+$%,.0f)".format(contract.completionReward), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                OutlinedButton(
                                    onClick = { onCancelContract(contract.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Cancel Contract (Pay 50% Penalty)", fontSize = 11.sp)
                                }
                            } else if (contract.status == ContractStatus.AVAILABLE) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Scope: ${contract.requiredDevPoints} Dev | ${contract.requiredDesignPoints} Design | ${contract.requiredQAPoints} QA", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                    Text("Limit: ${contract.deadlineDays}d", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = { onAcceptContract(contract.id) },
                                    enabled = isReputationSufficient,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("accept_contract_${contract.id}")
                                ) {
                                    Text(
                                        text = if (isReputationSufficient) "Accept Contract (+$%,.0f Advance)".format(contract.upfrontDeposit) else "Requires ${contract.minReputation} Rep",
                                        color = if (isReputationSufficient) Color(0xFF090D16) else Color(0xFF94A3B8),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (contract.status == ContractStatus.COMPLETED) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (contract.status == ContractStatus.COMPLETED) "✓ DELIVERED & PAID IN FULL" else "✗ FAILED DEADLINE - PENALTY APPLIED",
                                        color = if (contract.status == ContractStatus.COMPLETED) Color(0xFF10B981) else Color(0xFFEF4444),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal for Campaign selection
    productForCampaign?.let { prod ->
        AlertDialog(
            onDismissRequest = { productForCampaign = null },
            title = { Text("Launch Marketing Campaign", color = Color.White) },
            containerColor = Color(0xFF131B2E),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select campaign for '${prod.name}':", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    CampaignType.entries.forEach { camp ->
                        val canAfford = state.cash >= camp.upfrontCost
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, if (canAfford) Color(0xFF00E5FF) else Color(0xFF334155)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = canAfford) {
                                    onLaunchCampaign(prod, camp)
                                    productForCampaign = null
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(camp.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("$%,.0f".format(camp.upfrontCost), color = if (canAfford) Color(0xFF34D399) else Color(0xFFF87171), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("${camp.durationDays} days | Expected: %,d - %,d users".format(camp.expectedUsersMin, camp.expectedUsersMax), color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { productForCampaign = null }) {
                    Text("Close", color = Color(0xFF00E5FF))
                }
            }
        )
    }

    // Modal for creating product
    if (showCreateDialog) {
        var nameInput by remember { mutableStateOf("") }
        var selectedCat by remember { mutableStateOf(ProductCategory.SOCIAL_APP) }
        var selectedMon by remember { mutableStateOf(MonetizationModel.FREEMIUM) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Product Initiation", color = Color.White) },
            containerColor = Color(0xFF131B2E),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Product Name") },
                        placeholder = { Text(selectedCat.defaultName) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_product_name")
                    )

                    Text("Product Category:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ProductCategory.entries.take(3).forEach { cat ->
                            val isSel = selectedCat == cat
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFF00E5FF) else Color(0xFF334155)),
                                modifier = Modifier.weight(1f).clickable { selectedCat = cat }
                            ) {
                                Text(
                                    text = cat.title,
                                    color = if (isSel) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }

                    Text("Monetization Model:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        MonetizationModel.entries.forEach { mon ->
                            val isSel = selectedMon == mon
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFF00E5FF) else Color(0xFF334155)),
                                modifier = Modifier.fillMaxWidth().clickable { selectedMon = mon }
                            ) {
                                Row(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(mon.title, color = if (isSel) Color(0xFF00E5FF) else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("$%.3f/user".format(mon.revenuePerUser), color = Color(0xFF34D399), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = nameInput.trim().ifEmpty { selectedCat.defaultName }
                        onCreateProduct(finalName, selectedCat, selectedMon)
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    modifier = Modifier.testTag("submit_create_product_button")
                ) {
                    Text("Start Planning Phase", color = Color(0xFF090D16), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
private fun DevProgressBar(
    label: String,
    current: Int,
    needed: Int,
    color: Color
) {
    val progress = (current.toFloat() / needed.coerceAtLeast(1)).coerceIn(0f, 1f)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color(0xFFCBD5E1), fontSize = 10.sp)
            Text("$current / $needed (${(progress * 100).toInt()}%)", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF1E293B)
        )
    }
}
