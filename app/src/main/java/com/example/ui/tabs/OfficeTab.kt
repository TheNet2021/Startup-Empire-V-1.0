package com.example.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun OfficeTab(
    state: GameState,
    onUpgradeOffice: (OfficeTier) -> Unit,
    onBuyFurniture: (FurnitureType) -> Unit,
    onSellFurniture: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOfficeSubtab by remember { mutableIntStateOf(0) } // 0: Shop Catalog, 1: Placed Items, 2: Relocate
    var selectedCategory by remember { mutableStateOf<FurnitureCategory?>(null) }
    var itemToSell by remember { mutableStateOf<PlacedFurniture?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
    ) {
        // Subtab switcher
        TabRow(
            selectedTabIndex = selectedOfficeSubtab,
            containerColor = Color(0xFF0F172A),
            contentColor = Color(0xFF00E5FF),
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedOfficeSubtab == 0,
                onClick = { selectedOfficeSubtab = 0 },
                text = { Text("Shop Catalog", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("subtab_office_shop")
            )
            Tab(
                selected = selectedOfficeSubtab == 1,
                onClick = { selectedOfficeSubtab = 1 },
                text = { Text("Placed Items (${state.furniture.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("subtab_office_placed")
            )
            Tab(
                selected = selectedOfficeSubtab == 2,
                onClick = { selectedOfficeSubtab = 2 },
                text = { Text("Relocation", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("subtab_office_relocate")
            )
        }

        when (selectedOfficeSubtab) {
            0 -> {
                // SHOP CATALOG
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quick Capacity Bar
                    item {
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
                                OfficeStatPill("Office Tier", state.officeTier.title)
                                OfficeStatPill("Workstations", "${state.employees.size} / ${state.totalWorkstations} (${state.officeTier.maxDesks} max)")
                                OfficeStatPill("Monthly Rent", "$%,.0f".format(state.officeTier.monthlyRent))
                            }
                        }
                    }

                    // Category Filter Chips
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("All", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00E5FF),
                                    selectedLabelColor = Color(0xFF090D16)
                                )
                            )
                            FurnitureCategory.entries.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF00E5FF),
                                        selectedLabelColor = Color(0xFF090D16)
                                    )
                                )
                            }
                        }
                    }

                    val displayedItems = FurnitureType.entries.filter {
                        selectedCategory == null || it.category == selectedCategory
                    }

                    items(displayedItems) { furn ->
                        val canAfford = state.cash >= furn.cost
                        val isDesk = furn.isWorkstation
                        val deskLimitReached = isDesk && state.totalWorkstations >= state.officeTier.maxDesks

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
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = furn.title,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF1E293B)
                                        ) {
                                            Text(
                                                text = furn.category.name,
                                                color = Color(0xFF94A3B8),
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = furn.description,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                    if (furn.productivityBonus > 0f || furn.moraleBonus > 0 || furn.userCapacityBoost > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (furn.productivityBonus > 0f) {
                                                Text(
                                                    text = "+${(furn.productivityBonus * 100).toInt()}% Dev Speed",
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            if (furn.moraleBonus > 0) {
                                                Text(
                                                    text = "+${furn.moraleBonus} Morale",
                                                    color = Color(0xFFF472B6),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            if (furn.userCapacityBoost > 0) {
                                                Text(
                                                    text = "+$%,d Users".format(furn.userCapacityBoost),
                                                    color = Color(0xFF34D399),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Button(
                                    onClick = { onBuyFurniture(furn) },
                                    enabled = canAfford && !deskLimitReached,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF10B981),
                                        disabledContainerColor = Color(0xFF1E293B)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("buy_furniture_${furn.name.lowercase()}")
                                ) {
                                    Text(
                                        text = if (deskLimitReached) "Max Desks" else "$%,.0f".format(furn.cost),
                                        color = if (canAfford && !deskLimitReached) Color.White else Color(0xFF64748B),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // PLACED FURNITURE MANAGEMENT
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Installed Office Equipment & Assets",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "You can sell unwanted furniture for a 60% cash refund to free up grid space.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    items(state.furniture, key = { it.id }) { item ->
                        val assignedEmp = state.employees.find { it.assignedDeskId == item.id }
                        val refundAmount = item.type.cost * 0.60

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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.type.title,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tile (${item.gridX}, ${item.gridY}) - ${item.type.category.name}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                    if (item.type.isWorkstation) {
                                        Text(
                                            text = if (assignedEmp != null) "Assigned: ${assignedEmp.name}" else "Unoccupied",
                                            color = if (assignedEmp != null) Color(0xFF38BDF8) else Color(0xFFFBBF24),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Button(
                                    onClick = { itemToSell = item },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.2f)),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("sell_furniture_${item.id}")
                                ) {
                                    Text(
                                        text = "Sell (+$%,.0f)".format(refundAmount),
                                        color = Color(0xFFFCA5A5),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // RELOCATION
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = "Commercial Real Estate & HQ Tiers",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Relocate to larger office buildings to expand employee workstation limits and host bigger server wings.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    items(OfficeTier.entries) { tier ->
                        val isCurrent = state.officeTier == tier
                        val isPast = tier.ordinal < state.officeTier.ordinal
                        val meetsLevel = state.companyLevel >= tier.requiredCompanyLevel
                        val meetsUsers = state.totalActiveUsers >= tier.requiredActiveUsers
                        val meetsProducts = state.products.count { it.stage == ProductStage.LIVE } >= tier.requiredLiveProducts
                        val isUnlocked = meetsLevel && meetsUsers && meetsProducts
                        val canAfford = state.cash >= tier.upgradeCost

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isCurrent -> Color(0xFF0F2B36)
                                    !isUnlocked -> Color(0xFF0B101D)
                                    canAfford -> Color(0xFF131B2E)
                                    else -> Color(0xFF131B2E)
                                }
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                when {
                                    isCurrent -> Color(0xFF00E5FF)
                                    !isUnlocked -> Color(0xFF1E293B)
                                    canAfford -> Color(0xFF0284C7)
                                    else -> Color(0xFF334155)
                                }
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                isCurrent -> Icons.Default.Apartment
                                                !isUnlocked -> Icons.Default.Lock
                                                else -> Icons.Default.Business
                                            },
                                            contentDescription = null,
                                            tint = when {
                                                isCurrent -> Color(0xFF00E5FF)
                                                !isUnlocked -> Color(0xFF64748B)
                                                else -> Color(0xFF38BDF8)
                                            },
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Column {
                                            Text(tier.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "${tier.gridWidth}x${tier.gridHeight} Floor Grid • Max ${tier.maxDesks} Desks",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    if (isCurrent) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, Color(0xFF00E5FF))
                                        ) {
                                            Text(
                                                "CURRENT HQ",
                                                color = Color(0xFF00E5FF),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Text(tier.description, color = Color(0xFFCBD5E1), fontSize = 11.sp, lineHeight = 15.sp)

                                // Financial Specs
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Moving Cost: $%,.0f".format(tier.upgradeCost), fontSize = 11.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                                    Text("Monthly Rent: $%,.0f/mo".format(tier.monthlyRent), fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold)
                                }

                                // Unlock requirements or Action Button
                                if (!isCurrent) {
                                    if (!isUnlocked) {
                                        Surface(
                                            color = Color(0xFF1E1B4B).copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color(0xFF4338CA).copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFA5B4FC), modifier = Modifier.size(14.dp))
                                                Text(
                                                    text = tier.unlockRequirementText,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFA5B4FC)
                                                )
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = { onUpgradeOffice(tier) },
                                            enabled = canAfford,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (canAfford) Color(0xFF0284C7) else Color(0xFF1E293B)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("relocate_${tier.name.lowercase()}")
                                        ) {
                                            if (canAfford) {
                                                Text("Relocate to ${tier.title} ($%,.0f)".format(tier.upgradeCost), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            } else {
                                                Text("Insufficient Capital ($%,.0f needed)".format(tier.upgradeCost - state.cash), color = Color(0xFFEF4444), fontSize = 11.sp)
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

    // Confirmation dialog for selling furniture
    itemToSell?.let { item ->
        val refund = item.type.cost * 0.60
        AlertDialog(
            onDismissRequest = { itemToSell = null },
            containerColor = Color(0xFF131B2E),
            title = { Text("Sell ${item.type.title}?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to sell this ${item.type.title}? You will receive a 60% cash refund of $%,.0f and free up tile (${item.gridX}, ${item.gridY}).".format(refund),
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSellFurniture(item.id)
                        itemToSell = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Confirm Sell", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToSell = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
private fun OfficeStatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 10.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
