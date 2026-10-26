package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import com.example.audio.SoundManager
import com.example.ui.components.CompanyLogoBadge
import com.example.ui.components.getLogoIconVector
import kotlin.random.Random

private val RANDOM_COMPANY_NAMES = listOf(
    "Apex Dynamics",
    "Quantum Flow",
    "Nexus AI",
    "NovaByte Labs",
    "OmniSoft Systems",
    "Aether Interactive",
    "Pulse Cloud",
    "Hyperion Tech",
    "Vanguard Digital",
    "Cipher Logic"
)

private val LOGO_ICONS = listOf(
    "rocket", "terminal", "bolt", "brain",
    "globe", "building", "diamond", "cpu",
    "code", "layers", "cloud", "shield"
)

private val BRAND_COLORS = listOf(
    0xFF00E5FF, // Electric Cyan
    0xFF10B981, // Emerald Green
    0xFF8B5CF6, // Royal Violet
    0xFFF59E0B, // Amber Gold
    0xFFF43F5E, // Crimson Rose
    0xFF0284C7  // Azure Sky
)

private val LOGO_SHAPES = listOf(
    "rounded" to "Rounded",
    "circle" to "Circle",
    "diamond" to "Diamond",
    "shield" to "Shield"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyCreationScreen(
    onCancel: () -> Unit,
    onCreateCompany: (name: String, logoIcon: String, logoColor: Long, logoShape: String, difficulty: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var companyName by remember { mutableStateOf("NovaByte Labs") }
    var selectedIcon by remember { mutableStateOf("rocket") }
    var selectedColor by remember { mutableLongStateOf(0xFF00E5FF) }
    var selectedShape by remember { mutableStateOf("rounded") }
    var selectedDifficulty by remember { mutableStateOf("NORMAL") } // EASY, NORMAL, HARD

    val scrollState = rememberScrollState()

    val startingCash = when (selectedDifficulty) {
        "EASY" -> 60000.0
        "HARD" -> 15000.0
        else -> 30000.0
    }

    val startingRent = when (selectedDifficulty) {
        "EASY" -> 125.0
        "HARD" -> 180.0
        else -> 150.0
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16)),
        containerColor = Color(0xFF090D16),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Found New Company",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        SoundManager.playClick()
                        onCancel()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color(0xFF0F172A),
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            SoundManager.playClick()
                            onCancel()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8))
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val nameToUse = companyName.trim().ifEmpty { "NovaByte Labs" }
                            SoundManager.playLaunch()
                            onCreateCompany(nameToUse, selectedIcon, selectedColor, selectedShape, selectedDifficulty)
                        },
                        modifier = Modifier
                            .weight(2f)
                            .height(50.dp)
                            .testTag("btn_confirm_create_company"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color(0xFF090D16))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "LAUNCH STARTUP",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF090D16),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Live Crest Preview Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CompanyLogoBadge(
                        iconKey = selectedIcon,
                        colorLong = selectedColor,
                        shapeKey = selectedShape,
                        sizeDp = 64.dp
                    )

                    Column {
                        Text(
                            text = companyName.ifEmpty { "Enter Company Name" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Level 1 • Garage Startup",
                            fontSize = 12.sp,
                            color = Color(0xFF38BDF8)
                        )
                        Text(
                            text = "Mode: $selectedDifficulty • $%,.0f Starting Capital".format(startingCash),
                            fontSize = 11.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }

            // Step 1: Company Name
            SectionCard(title = "1. Company Name") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { if (it.length <= 26) companyName = it },
                        placeholder = { Text("e.g. Nexus Software") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_company_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    FilledTonalIconButton(
                        onClick = {
                            SoundManager.playClick()
                            companyName = RANDOM_COMPANY_NAMES.random()
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("btn_random_name"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Randomize", tint = Color(0xFF00E5FF))
                    }
                }
            }

            // Step 2: Company Logo Editor
            SectionCard(title = "2. Design Company Crest") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Icon Picker
                    Text("Select Emblem:", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LOGO_ICONS.take(6).forEach { iconKey ->
                            val isSelected = selectedIcon == iconKey
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(selectedColor).copy(alpha = 0.25f) else Color(0xFF0F172A))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(selectedColor) else Color(0xFF1E293B),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        SoundManager.playClick()
                                        selectedIcon = iconKey
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getLogoIconVector(iconKey),
                                    contentDescription = iconKey,
                                    tint = if (isSelected) Color(selectedColor) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LOGO_ICONS.drop(6).take(6).forEach { iconKey ->
                            val isSelected = selectedIcon == iconKey
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(selectedColor).copy(alpha = 0.25f) else Color(0xFF0F172A))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(selectedColor) else Color(0xFF1E293B),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        SoundManager.playClick()
                                        selectedIcon = iconKey
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getLogoIconVector(iconKey),
                                    contentDescription = iconKey,
                                    tint = if (isSelected) Color(selectedColor) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Brand Color Palette
                    Text("Brand Color:", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BRAND_COLORS.forEach { colorLong ->
                            val isSelected = selectedColor == colorLong
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorLong))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        SoundManager.playClick()
                                        selectedColor = colorLong
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    // Badge Shape Selector
                    Text("Crest Shape:", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LOGO_SHAPES.forEach { (key, label) ->
                            val isSelected = selectedShape == key
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        SoundManager.playClick()
                                        selectedShape = key
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.3f) else Color(0xFF0F172A),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B)
                                )
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Step 3: Difficulty Selection
            SectionCard(title = "3. Simulation Difficulty") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DifficultyCard(
                        title = "EASY",
                        subtitle = "$60,000 Start • +25% Revenue • -15% Rent",
                        desc = "Relaxed pace with generous market adoption and safety buffer.",
                        isSelected = selectedDifficulty == "EASY",
                        color = Color(0xFF10B981),
                        onClick = {
                            SoundManager.playClick()
                            selectedDifficulty = "EASY"
                        }
                    )
                    DifficultyCard(
                        title = "NORMAL",
                        subtitle = "$30,000 Start • Standard Market Economy",
                        desc = "Balanced tech tycoon simulation. Strategic hiring and growth required.",
                        isSelected = selectedDifficulty == "NORMAL",
                        color = Color(0xFF00E5FF),
                        onClick = {
                            SoundManager.playClick()
                            selectedDifficulty = "NORMAL"
                        }
                    )
                    DifficultyCard(
                        title = "HARD",
                        subtitle = "$15,000 Start • -15% Revenue • +20% Rent & Salaries",
                        desc = "Strict cashflow constraints, fast server churn, and bankruptcy risk.",
                        isSelected = selectedDifficulty == "HARD",
                        color = Color(0xFFEF4444),
                        onClick = {
                            SoundManager.playClick()
                            selectedDifficulty = "HARD"
                        }
                    )
                }
            }

            // Step 4: Starting Scenario Summary
            SectionCard(title = "4. Starting Headquarters Summary") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SummaryRow("Initial Capital", "$%,.0f".format(startingCash), Color(0xFF10B981))
                    SummaryRow("Initial Office", "Garage Startup (6x6 Grid)", Color.White)
                    SummaryRow("Monthly Rent", "$%,.0f / month".format(startingRent), Color(0xFFF59E0B))
                    SummaryRow("Workstation Cap", "3 Desks (Expandable)", Color(0xFF38BDF8))
                    SummaryRow("Founding Staff", "Alex Vance (Lead Engineer)", Color.White)
                    SummaryRow("Debut Product", "Sphere Social (In Development)", Color(0xFF00E5FF))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            content()
        }
    }
}

@Composable
private fun DifficultyCard(
    title: String,
    subtitle: String,
    desc: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFF131B2E),
        border = BorderStroke(
            1.5.dp,
            if (isSelected) color else Color(0xFF1E293B)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = color, unselectedColor = Color(0xFF475569))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = if (isSelected) color else Color.White, fontSize = 13.sp)
                Text(subtitle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8))
                Text(desc, fontSize = 10.sp, color = Color(0xFF64748B), lineHeight = 14.sp)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFF94A3B8))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
