package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.SoundManager
import com.example.model.*
import com.example.ui.components.TopStatusBar
import com.example.ui.dialogs.*
import com.example.ui.office3d.OfficeIsometricCanvas
import com.example.ui.screens.CompanyCreationScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.tabs.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

enum class GameTab(val title: String, val icon: ImageVector, val tag: String) {
    OFFICE("Office", Icons.Default.Apartment, "tab_office"),
    EMPLOYEES("Staff", Icons.Default.People, "tab_employees"),
    PRODUCTS("Products", Icons.Default.Apps, "tab_products"),
    SERVERS("Servers", Icons.Default.Dns, "tab_servers"),
    FINANCE("Finance", Icons.Default.AccountBalance, "tab_finance")
}

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val appScreen by viewModel.appScreen.collectAsStateWithLifecycle()
                val saveSummary by viewModel.saveSummary.collectAsStateWithLifecycle()
                val settings by viewModel.settings.collectAsStateWithLifecycle()
                val state by viewModel.gameState.collectAsStateWithLifecycle()
                val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()

                var showSettingsDialogGlobal by remember { mutableStateOf(false) }
                var showCreditsDialogGlobal by remember { mutableStateOf(false) }

                BackHandler(enabled = appScreen != AppScreen.MAIN_MENU) {
                    when (appScreen) {
                        AppScreen.COMPANY_CREATION -> viewModel.navigateTo(AppScreen.MAIN_MENU)
                        AppScreen.GAMEPLAY -> showSettingsDialogGlobal = true
                        else -> {}
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (appScreen) {
                        AppScreen.MAIN_MENU -> {
                            MainMenuScreen(
                                saveSummary = saveSummary,
                                onStartNewGame = { viewModel.navigateTo(AppScreen.COMPANY_CREATION) },
                                onContinueGame = { viewModel.continueGame() },
                                onOpenSettings = { showSettingsDialogGlobal = true },
                                onOpenCredits = { showCreditsDialogGlobal = true }
                            )
                        }
                        AppScreen.COMPANY_CREATION -> {
                            CompanyCreationScreen(
                                onCancel = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                                onCreateCompany = { name, icon, color, shape, difficulty ->
                                    viewModel.startNewGame(name, icon, color, shape, difficulty)
                                }
                            )
                        }
                        AppScreen.GAMEPLAY -> {
                            StartupEmpireGameScreen(
                                state = state,
                                toastMessage = toastMsg,
                                viewModel = viewModel,
                                onOpenSettings = { showSettingsDialogGlobal = true }
                            )
                        }
                    }

                    // Global Settings Modal
                    if (showSettingsDialogGlobal) {
                        SettingsDialog(
                            settings = settings,
                            companyName = if (appScreen == AppScreen.GAMEPLAY) state.companyName else saveSummary?.companyName,
                            cash = if (appScreen == AppScreen.GAMEPLAY) state.cash else saveSummary?.cash,
                            onUpdateSettings = { viewModel.updateSettings(it) },
                            onSaveGameNow = { viewModel.saveGame() },
                            onReturnToMainMenu = if (appScreen == AppScreen.GAMEPLAY) {
                                {
                                    showSettingsDialogGlobal = false
                                    viewModel.returnToMainMenu()
                                }
                            } else null,
                            onDismiss = { showSettingsDialogGlobal = false }
                        )
                    }

                    // Global Credits Modal
                    if (showCreditsDialogGlobal) {
                        CreditsDialog(onDismiss = { showCreditsDialogGlobal = false })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SoundManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        SoundManager.onPause()
        viewModel.saveGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.release()
    }
}

@Composable
fun StartupEmpireGameScreen(
    state: GameState,
    toastMessage: String?,
    viewModel: GameViewModel,
    onOpenSettings: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(GameTab.OFFICE) }
    var inspectedEmployee by remember { mutableStateOf<Employee?>(null) }
    var inspectedFurniture by remember { mutableStateOf<PlacedFurniture?>(null) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var isOfficeViewExpanded by remember { mutableStateOf(false) }

    // Auto-clear toast after 2.5 seconds
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2500)
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16)),
        containerColor = Color(0xFF090D16),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // Material 3 Navigation Bar with system insets respect
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                contentColor = Color(0xFF00E5FF),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                GameTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            SoundManager.playClick()
                            selectedTab = tab
                            isOfficeViewExpanded = false
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (tab == GameTab.PRODUCTS && state.products.any { it.stage == ProductStage.READY_TO_LAUNCH }) {
                                        Badge(containerColor = Color(0xFF10B981)) { Text("!") }
                                    } else if (tab == GameTab.SERVERS && (state.server.isOutageActive || state.serverLoad > 0.90f)) {
                                        Badge(containerColor = Color(0xFFEF4444)) { Text("!") }
                                    }
                                }
                            ) {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) Color(0xFF00E5FF) else Color(0xFF94A3B8)
                                )
                            }
                        },
                        label = {
                            Text(
                                tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // 1. Top HUD Status Bar
            TopStatusBar(
                state = state,
                onSpeedChange = { speed ->
                    SoundManager.playClick()
                    viewModel.setSpeed(speed)
                },
                onOpenNotifications = {
                    SoundManager.playClick()
                    showNotificationsDialog = true
                },
                onOpenSettings = {
                    SoundManager.playClick()
                    onOpenSettings()
                }
            )

            // Dynamic Toast Alert Banner
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = Color(0xFF0284C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = toastMessage ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearToast() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // 2. Central 3D/Isometric Office Scene & Management Pane
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Elevated Isometric Office Canvas (takes upper portion or full screen when expanded)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(if (isOfficeViewExpanded || selectedTab == GameTab.OFFICE) 1.25f else 0.85f)
                            .testTag("office_3d_viewport")
                    ) {
                        OfficeIsometricCanvas(
                            state = state,
                            onSelectFurniture = { furn ->
                                SoundManager.playClick()
                                inspectedFurniture = furn
                            },
                            onSelectEmployee = { emp ->
                                SoundManager.playClick()
                                inspectedEmployee = emp
                            },
                            onOpenServerDetails = {
                                SoundManager.playClick()
                                selectedTab = GameTab.SERVERS
                            },
                            onMoveFurniture = { fId, nx, ny ->
                                viewModel.moveFurniture(fId, nx, ny)
                            },
                            onRotateFurniture = { fId ->
                                SoundManager.playClick()
                                viewModel.rotateFurniture(fId)
                            },
                            onSellFurniture = { fId ->
                                viewModel.sellFurniture(fId)
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Floating Expand/Collapse Viewport Button
                        FilledTonalIconButton(
                            onClick = {
                                SoundManager.playClick()
                                isOfficeViewExpanded = !isOfficeViewExpanded
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                                .size(36.dp)
                                .testTag("btn_expand_office_view"),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFF1E293B).copy(alpha = 0.85f)
                            )
                        ) {
                            Icon(
                                imageVector = if (isOfficeViewExpanded) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "Toggle Fullscreen Office View",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Lower Management Tabs Section (Collapsible when expanded)
                    if (!isOfficeViewExpanded) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1.0f)
                                .testTag("management_tabs_card"),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
                            border = BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            when (selectedTab) {
                                GameTab.OFFICE -> OfficeTab(
                                    state = state,
                                    onUpgradeOffice = { tier -> viewModel.upgradeOffice(tier) },
                                    onBuyFurniture = { type -> viewModel.buyFurniture(type) },
                                    onSellFurniture = { fId -> viewModel.sellFurniture(fId) }
                                )
                                GameTab.EMPLOYEES -> EmployeesTab(
                                    state = state,
                                    onHireCandidate = { cand -> viewModel.hireCandidate(cand) },
                                    onFireEmployee = { emp -> viewModel.fireEmployee(emp) },
                                    onRaiseSalary = { emp -> viewModel.raiseSalary(emp) },
                                    onTrainEmployee = { emp -> viewModel.trainEmployee(emp) },
                                    onPromoteEmployee = { emp -> viewModel.promoteEmployee(emp) },
                                    onSendOnRetreat = { emp -> viewModel.sendOnRetreat(emp) },
                                    onAssignProduct = { emp, pId -> viewModel.assignProduct(emp, pId) },
                                    onAssignContract = { emp, cId -> viewModel.assignStaffToContract(emp.id, cId) },
                                    onSelectEmployee = { emp -> inspectedEmployee = emp }
                                )
                                GameTab.PRODUCTS -> ProductsTab(
                                    state = state,
                                    onCreateProduct = { name, cat, mon -> viewModel.createProduct(name, cat, mon) },
                                    onLaunchProduct = { prod -> viewModel.launchProduct(prod) },
                                    onChangeMonetization = { prod, mon -> viewModel.changeMonetization(prod, mon) },
                                    onChangeMarketingBudget = { prod, budget -> viewModel.changeMarketingBudget(prod, budget) },
                                    onLaunchCampaign = { prod, campType -> viewModel.launchCampaign(prod, campType) },
                                    onSetSprintMode = { prod, mode -> viewModel.setSprintMode(prod, mode) },
                                    onStartProductUpdate = { prod -> viewModel.startProductUpdate(prod) },
                                    onToggleMaintenance = { prod -> viewModel.toggleProductMaintenance(prod.id) },
                                    onConductMarketResearch = { prod -> viewModel.conductMarketResearch(prod) },
                                    onStartFeatureDevelopment = { pId, fId -> viewModel.startFeatureDevelopment(pId, fId) },
                                    onAcceptContract = { cId -> viewModel.acceptContract(cId) },
                                    onCancelContract = { cId -> viewModel.cancelContract(cId) },
                                    onDeliverContract = { cId -> viewModel.deliverContract(cId) }
                                )
                                GameTab.SERVERS -> ServersTab(
                                    state = state,
                                    onToggleAutoScale = { enabled -> viewModel.toggleCloudAutoScale(enabled) },
                                    onEmergencyRestart = { viewModel.emergencyServerRestart() },
                                    onOptimizeDatabase = { viewModel.optimizeDatabaseServer() },
                                    onNavigateToOfficeCatalog = { selectedTab = GameTab.OFFICE }
                                )
                                GameTab.FINANCE -> FinanceTab(
                                    state = state,
                                    onTakeLoan = { amount, days, dailyPayment, lender -> viewModel.takeLoan(amount, days, dailyPayment, lender) },
                                    onRepayLoanEarly = { loan -> viewModel.repayLoanEarly(loan) },
                                    onStartResearch = { tech -> viewModel.startResearch(tech) },
                                    onAcquireCompetitor = { compId -> viewModel.acquireCompetitor(compId) },
                                    onSellEquity = { pct -> viewModel.sellEquityForCapital(pct) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // MODALS & DIALOGS
    // -------------------------------------------------------------

    // 1. Random Event Decision Modal
    state.pendingEvent?.let { event ->
        EventDialog(
            event = event,
            onSelectOption = { option ->
                SoundManager.playConfirm()
                viewModel.handleEventChoice(option)
            }
        )
    }

    // 2. Employee Dossier Modal
    inspectedEmployee?.let { emp ->
        val desk = state.furniture.find { it.id == emp.assignedDeskId }
        val prod = state.products.find { it.id == emp.assignedProductId }
        EmployeeDossierDialog(
            employee = emp,
            desk = desk,
            assignedProduct = prod,
            cash = state.cash,
            onDismiss = { inspectedEmployee = null },
            onTrain = {
                viewModel.trainEmployee(it)
                inspectedEmployee = viewModel.gameState.value.employees.find { e -> e.id == it.id }
            },
            onRaiseSalary = {
                viewModel.raiseSalary(it)
                inspectedEmployee = viewModel.gameState.value.employees.find { e -> e.id == it.id }
            },
            onSendRetreat = {
                viewModel.sendOnRetreat(it)
                inspectedEmployee = viewModel.gameState.value.employees.find { e -> e.id == it.id }
            },
            onPromote = {
                viewModel.promoteEmployee(it)
                inspectedEmployee = viewModel.gameState.value.employees.find { e -> e.id == it.id }
            },
            onFire = {
                viewModel.fireEmployee(it)
                inspectedEmployee = null
            }
        )
    }

    // 3. Furniture Inspection Modal
    inspectedFurniture?.let { furn ->
        val refund = furn.type.cost * 0.60
        val assignedEmp = state.employees.find { it.assignedDeskId == furn.id }
        AlertDialog(
            onDismissRequest = { inspectedFurniture = null },
            containerColor = Color(0xFF131B2E),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                    Text(furn.type.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(furn.type.description, color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    Text("Category: ${furn.type.category.name}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text("Office Grid Position: (${furn.gridX}, ${furn.gridY})", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    if (furn.type.isWorkstation) {
                        Text(
                            text = if (assignedEmp != null) "Assigned Staff: ${assignedEmp.name} (${assignedEmp.role.title})" else "Status: Unoccupied Workstation",
                            color = if (assignedEmp != null) Color(0xFF38BDF8) else Color(0xFFFBBF24),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (furn.type.productivityBonus > 0f) {
                        Text("+${(furn.type.productivityBonus * 100).toInt()}% Velocity Boost", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    if (furn.type.userCapacityBoost > 0) {
                        Text("+%,d Concurrent User Capacity".format(furn.type.userCapacityBoost), color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sellFurniture(furn.id)
                        inspectedFurniture = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sell Item (+$%,.0f)".format(refund), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectedFurniture = null }) {
                    Text("Close", color = Color(0xFF00E5FF))
                }
            }
        )
    }

    // 4. Notifications Feed Modal
    if (showNotificationsDialog) {
        NotificationsDialog(
            notifications = state.notifications,
            onDismiss = { showNotificationsDialog = false }
        )
    }

    // 5. Bankruptcy Foreclosure Modal
    if (state.isBankrupt) {
        GameOverDialog(
            debtAmount = -state.cash,
            onRestructureBailout = { viewModel.restructureBailout() },
            onRestartNewGame = { viewModel.resetGame() }
        )
    }
}
