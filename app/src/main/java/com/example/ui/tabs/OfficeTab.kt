package com.example.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.SoundManager
import com.example.model.*

@Composable
fun OfficeTab(
    state: GameState,
    onUpgradeOffice: (OfficeTier) -> Unit,
    onBuyFurniture: (FurnitureType) -> Unit,
    onSellFurniture: (String) -> Unit,
    initialSubtab: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedOfficeSubtab by remember(initialSubtab) { mutableIntStateOf(initialSubtab) } // 0: Shop Catalog, 1: Placed Items, 2: Relocate
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
                onClick = {
                    SoundManager.playClick()
                    selectedOfficeSubtab = 0
                },
                text = { Text("Shop Catalog", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("subtab_office_shop")
            )
            Tab(
                selected = selectedOfficeSubtab == 1,
                onClick = {
                    SoundManager.playClick()
                    selectedOfficeSubtab = 1
                },
                text = { Text("Placed Items (${state.furniture.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("subtab_office_placed")
            )
            Tab(
                selected = selectedOfficeSubtab == 2,
                onClick = {
                    SoundManager.playClick()
                    selectedOfficeSubtab = 2
                },
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
                    // Commercial Visual Hero Banner
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(115.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_office_shop_banner),
                                    contentDescription = "Office Showroom Banner",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0x55090D16),
                                                    Color(0xDD090D16),
                                                    Color(0xFF090D16)
                                                )
                                            )
                                        )
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column {
                                            Text(
                                                text = "OFFICE STORE & DEPOT",
                                                color = Color(0xFF00E5FF),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 1.sp
                                            )
                                            Text(
                                                text = "Premium Tech Furniture & Gear",
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.25f),
                                            border = BorderStroke(1.dp, Color(0xFF10B981))
                                        ) {
                                            Text(
                                                text = "$%,.0f Cash".format(state.cash),
                                                color = Color(0xFF34D399),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "HQ: ${state.officeTier.title}",
                                            color = Color(0xFFCBD5E1),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "•",
                                            color = Color(0xFF64748B),
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = "Desks: ${state.totalWorkstations}/${state.officeTier.maxDesks}",
                                            color = if (state.totalWorkstations >= state.officeTier.maxDesks) Color(0xFFFBBF24) else Color(0xFF38BDF8),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "•",
                                            color = Color(0xFF64748B),
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = "Rent: $%,.0f/mo".format(state.officeTier.monthlyRent),
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
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
                                onClick = {
                                    SoundManager.playClick()
                                    selectedCategory = null
                                },
                                label = { Text("All (${FurnitureType.entries.size})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00E5FF),
                                    selectedLabelColor = Color(0xFF090D16)
                                )
                            )
                            FurnitureCategory.entries.forEach { cat ->
                                val count = FurnitureType.entries.count { it.category == cat }
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = {
                                        SoundManager.playClick()
                                        selectedCategory = cat
                                    },
                                    label = { Text("${cat.name.lowercase().replaceFirstChar { it.uppercase() }} ($count)", fontSize = 11.sp) },
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
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // High-resolution 3D asset preview card
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0F172A),
                                    border = BorderStroke(1.dp, Color(0xFF334155)),
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = getFurnitureDrawable(furn)),
                                            contentDescription = furn.title,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                // Details Column
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
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
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = furn.description,
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )

                                    // Strategic perks
                                    if (furn.productivityBonus > 0f || furn.moraleBonus > 0 || furn.userCapacityBoost > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (furn.productivityBonus > 0f) {
                                                Text(
                                                    text = "+${(furn.productivityBonus * 100).toInt()}% Velocity",
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (furn.moraleBonus > 0) {
                                                Text(
                                                    text = "+${furn.moraleBonus} Morale",
                                                    color = Color(0xFFF472B6),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (furn.userCapacityBoost > 0) {
                                                Text(
                                                    text = "+$%,d Users".format(furn.userCapacityBoost),
                                                    color = Color(0xFF34D399),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                // Purchase Action Button
                                Button(
                                    onClick = {
                                        SoundManager.playClick()
                                        onBuyFurniture(furn)
                                    },
                                    enabled = canAfford && !deskLimitReached,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF10B981),
                                        disabledContainerColor = Color(0xFF1E293B)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("buy_furniture_${furn.name.lowercase()}")
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (deskLimitReached) "Max Desks" else "$%,.0f".format(furn.cost),
                                            color = if (canAfford && !deskLimitReached) Color.White else Color(0xFF64748B),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (!deskLimitReached && canAfford) {
                                            Text(
                                                text = "Purchase",
                                                color = Color.White.copy(alpha = 0.8f),
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
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
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A),
                                    border = BorderStroke(1.dp, Color(0xFF334155)),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(3.dp)) {
                                        Image(
                                            painter = painterResource(id = getFurnitureDrawable(item.type)),
                                            contentDescription = item.type.title,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.type.title,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tile (${item.gridX}, ${item.gridY}) • ${item.type.category.name}",
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
                // RELOCATION - ENHANCED WITH 3D HEADQUARTERS RENDERS
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
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 3D Rendered HQ Environment Preview Image
                                val previewDrawable = getTierPreviewDrawable(tier)
                                if (previewDrawable != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = previewDrawable),
                                            contentDescription = tier.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            Color(0x990F172A),
                                                            Color(0xFF0F172A)
                                                        )
                                                    )
                                                )
                                        )
                                        if (isCurrent) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF00E5FF),
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(10.dp)
                                            ) {
                                                Text(
                                                    "ACTIVE HEADQUARTERS",
                                                    color = Color(0xFF090D16),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(tier.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "${tier.gridWidth}x${tier.gridHeight} Floor Grid • Max ${tier.maxDesks} Desks",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp
                                            )
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
                                                color = Color(0xFF1E1B4B).copy(alpha = 0.5f),
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
                                                onClick = {
                                                    SoundManager.playClick()
                                                    onUpgradeOffice(tier)
                                                },
                                                enabled = canAfford,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF00E5FF),
                                                    disabledContainerColor = Color(0xFF1E293B)
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("upgrade_office_${tier.name.lowercase()}")
                                            ) {
                                                Text(
                                                    text = if (canAfford) "Relocate & Expand HQ ($%,.0f)".format(tier.upgradeCost) else "Insufficient Funds ($%,.0f)".format(tier.upgradeCost),
                                                    color = if (canAfford) Color(0xFF090D16) else Color(0xFF64748B),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
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

    // Sell Confirmation Dialog
    if (itemToSell != null) {
        val target = itemToSell!!
        val refund = target.type.cost * 0.60
        AlertDialog(
            onDismissRequest = { itemToSell = null },
            title = { Text("Sell ${target.type.title}?") },
            text = {
                Text("This will remove the item from your office floor and refund $%,.0f (60%% of purchase price).".format(refund))
            },
            confirmButton = {
                Button(
                    onClick = {
                        SoundManager.playClick()
                        onSellFurniture(target.id)
                        itemToSell = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Sell for $%,.0f".format(refund), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToSell = null }) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF0F172A),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCBD5E1)
        )
    }
}

private fun getFurnitureIcon(furn: FurnitureType): ImageVector = when (furn) {
    FurnitureType.DESK_STANDARD -> Icons.Default.Computer
    FurnitureType.DESK_DUAL_SCREEN -> Icons.Default.DesktopWindows
    FurnitureType.DESK_EXECUTIVE -> Icons.Default.Monitor
    FurnitureType.SERVER_RACK_BLADE, FurnitureType.SERVER_RACK_ENTERPRISE -> Icons.Default.Dns
    FurnitureType.ESPRESSO_MACHINE -> Icons.Default.Coffee
    FurnitureType.WATER_COOLER -> Icons.Default.WaterDrop
    FurnitureType.BONSAI_PLANT -> Icons.Default.Yard
    FurnitureType.WHITEBOARD -> Icons.Default.Dashboard
    FurnitureType.BOOKSHELF -> Icons.AutoMirrored.Filled.MenuBook
    FurnitureType.ARCADE_CABINET -> Icons.Default.SportsEsports
    FurnitureType.RECEPTION_DESK -> Icons.Default.MeetingRoom
    FurnitureType.MEETING_TABLE -> Icons.Default.Groups
}

private fun getFurnitureDrawable(furn: FurnitureType): Int = when (furn) {
    FurnitureType.DESK_STANDARD,
    FurnitureType.DESK_DUAL_SCREEN,
    FurnitureType.DESK_EXECUTIVE -> R.drawable.iso_desk_empty
    FurnitureType.SERVER_RACK_BLADE,
    FurnitureType.SERVER_RACK_ENTERPRISE -> R.drawable.iso_server_rack
    FurnitureType.BONSAI_PLANT -> R.drawable.iso_plant
    FurnitureType.WATER_COOLER -> R.drawable.iso_water_cooler
    FurnitureType.ARCADE_CABINET -> R.drawable.iso_arcade
    FurnitureType.WHITEBOARD -> R.drawable.iso_whiteboard
    FurnitureType.ESPRESSO_MACHINE -> R.drawable.iso_espresso
    FurnitureType.MEETING_TABLE,
    FurnitureType.RECEPTION_DESK -> R.drawable.iso_meeting_table
    FurnitureType.BOOKSHELF -> R.drawable.iso_bookshelf
}

private fun getTierPreviewDrawable(tier: OfficeTier): Int? = when (tier) {
    OfficeTier.GARAGE -> R.drawable.img_tier_garage
    OfficeTier.COWORKING -> R.drawable.img_tier_loft // Shared tech incubator
    OfficeTier.TECH_LOFT -> R.drawable.img_tier_loft
    OfficeTier.CAMPUS_HQ -> R.drawable.img_tier_campus
    OfficeTier.CYBER_TOWER -> R.drawable.img_tier_penthouse
}
