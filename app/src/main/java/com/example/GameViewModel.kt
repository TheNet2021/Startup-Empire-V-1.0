package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.SimulationEngine
import com.example.model.*
import com.example.storage.GameStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppScreen {
    MAIN_MENU,
    COMPANY_CREATION,
    GAMEPLAY
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _appScreen = MutableStateFlow(AppScreen.MAIN_MENU)
    val appScreen: StateFlow<AppScreen> = _appScreen.asStateFlow()

    private val _settings = MutableStateFlow(GameStorage.loadSettings(application))
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    private val _saveSummary = MutableStateFlow(GameStorage.getSaveSummary(application))
    val saveSummary: StateFlow<SaveSummary?> = _saveSummary.asStateFlow()

    private val _gameState = MutableStateFlow(GameStorage.loadGame(application))
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var hoursSinceLastSave = 0

    init {
        com.example.audio.SoundManager.initialize(application, _settings.value)
        // Start simulation loop
        viewModelScope.launch {
            while (isActive) {
                val currentSpeed = _gameState.value.clock.speed
                if (currentSpeed == GameSpeed.PAUSED || _gameState.value.isBankrupt) {
                    delay(300)
                    continue
                }

                val tickDelay = when (currentSpeed) {
                    GameSpeed.NORMAL -> 750L
                    GameSpeed.FAST -> 300L
                    GameSpeed.ULTRA -> 120L
                    GameSpeed.PAUSED -> 1000L
                }

                delay(tickDelay)

                _gameState.value = SimulationEngine.tick(_gameState.value, 1)

                hoursSinceLastSave++
                if (hoursSinceLastSave >= 24) {
                    hoursSinceLastSave = 0
                    saveGameInternal()
                }
            }
        }
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun navigateTo(screen: AppScreen) {
        _appScreen.value = screen
    }

    fun startNewGame(
        companyName: String,
        logoIcon: String,
        logoColor: Long,
        logoShape: String,
        difficulty: String
    ) {
        val startingCash = when (difficulty) {
            "EASY" -> 60000.0
            "HARD" -> 15000.0
            else -> 30000.0
        }

        val freshState = GameState(
            companyName = companyName,
            companyLogoIcon = logoIcon,
            companyLogoColor = logoColor,
            companyLogoShape = logoShape,
            difficulty = difficulty,
            companyLevel = 1,
            reputation = 100,
            cash = startingCash,
            clock = GameClock(),
            officeTier = OfficeTier.GARAGE,
            furniture = GameState.defaultStartingFurniture(),
            employees = GameState.defaultStartingEmployees(),
            availableCandidates = GameState.defaultInitialCandidates(),
            products = GameState.defaultStartingProducts(),
            server = ServerInfrastructure(),
            notifications = listOf("Welcome to $companyName! Set up workstations, hire specialists, and build digital hits.")
        )

        _gameState.value = freshState
        GameStorage.saveGame(getApplication(), freshState)
        _saveSummary.value = GameStorage.getSaveSummary(getApplication())
        _appScreen.value = AppScreen.GAMEPLAY
        com.example.audio.SoundManager.playLaunch()
        showToast("Welcome to $companyName!")
    }

    fun continueGame() {
        val loaded = GameStorage.loadGame(getApplication())
        val summary = GameStorage.getSaveSummary(getApplication())
        val lastTimestamp = summary?.savedAtTimestamp ?: System.currentTimeMillis()
        val elapsedMillis = (System.currentTimeMillis() - lastTimestamp).coerceAtLeast(0)
        val elapsedHours = (elapsedMillis / (1000 * 3600)).toInt().coerceIn(0, 24)

        val finalLoadedState = if (elapsedHours > 0) {
            val progressed = SimulationEngine.tick(loaded, elapsedHours)
            val revenueDelta = progressed.cash - loaded.cash
            val returnNote = if (revenueDelta >= 0) {
                "Welcome back! While you were offline for $elapsedHours hours, your team generated +$%,.0f net cash.".format(revenueDelta)
            } else {
                "Welcome back! Offline for $elapsedHours hours. Overhead costs -$%,.0f deducted.".format(-revenueDelta)
            }
            progressed.copy(notifications = listOf(returnNote) + progressed.notifications.take(29))
        } else {
            loaded
        }

        _gameState.value = finalLoadedState
        _saveSummary.value = summary
        _appScreen.value = AppScreen.GAMEPLAY
        com.example.audio.SoundManager.playConfirm()
    }

    fun returnToMainMenu() {
        saveGameInternal()
        _saveSummary.value = GameStorage.getSaveSummary(getApplication())
        _appScreen.value = AppScreen.MAIN_MENU
        com.example.audio.SoundManager.playConfirm()
    }

    fun updateSettings(newSettings: GameSettings) {
        _settings.value = newSettings
        GameStorage.saveSettings(getApplication(), newSettings)
        com.example.audio.SoundManager.updateSettings(newSettings)
    }

    fun setSpeed(speed: GameSpeed) {
        _gameState.value = _gameState.value.copy(
            clock = _gameState.value.clock.copy(speed = speed)
        )
        showToast("Game Speed: ${speed.label}")
    }

    fun hireCandidate(candidate: Employee) {
        val state = _gameState.value
        val freeDesk = state.furniture.find { it.type.isWorkstation && state.employees.none { emp -> emp.assignedDeskId == it.id } }
        if (freeDesk == null) {
            showToast("Cannot hire: No free desks! Purchase a desk in the Office tab.")
            return
        }

        if (state.cash < candidate.hiringCost) {
            showToast("Insufficient funds! Need $%,.0f to hire %s.".format(candidate.hiringCost, candidate.name))
            return
        }

        val assignedEmployee = candidate.copy(assignedDeskId = freeDesk.id)
        val updatedCandidates = state.availableCandidates.filter { it.id != candidate.id }
        val updatedStaff = state.employees + assignedEmployee
        val updatedFurniture = state.furniture.map {
            if (it.id == freeDesk.id) it.copy(assignedEmployeeId = assignedEmployee.id) else it
        }

        _gameState.value = state.copy(
            cash = state.cash - candidate.hiringCost,
            employees = updatedStaff,
            furniture = updatedFurniture,
            availableCandidates = updatedCandidates,
            notifications = listOf("Hired ${candidate.name} as ${candidate.role.title} ($%,.0f signing fee)!".format(candidate.hiringCost)) + state.notifications
        )
        showToast("Hired ${candidate.name} ($%,.0f fee)".format(candidate.hiringCost))
        saveGameInternal()
    }

    fun raiseSalary(employee: Employee) {
        val state = _gameState.value
        val updatedEmployees = state.employees.map {
            if (it.id == employee.id) {
                it.copy(
                    dailySalary = it.dailySalary * 1.15,
                    morale = (it.morale + 15).coerceAtMost(100)
                )
            } else it
        }
        _gameState.value = state.copy(employees = updatedEmployees)
        showToast("Raised salary for ${employee.name} (+15% pay, +15 morale)")
    }

    fun trainEmployee(employee: Employee) {
        val state = _gameState.value
        if (state.cash < 500.0) {
            showToast("Insufficient funds! Training costs $500.")
            return
        }

        val updatedEmployees = state.employees.map {
            if (it.id == employee.id) {
                it.copy(
                    skill = (it.skill + 8).coerceAtMost(99),
                    speed = (it.speed + 6).coerceAtMost(99),
                    morale = (it.morale + 10).coerceAtMost(100)
                )
            } else it
        }
        _gameState.value = state.copy(
            cash = state.cash - 500.0,
            employees = updatedEmployees,
            notifications = listOf("Trained ${employee.name}: Skill +8, Speed +6!") + state.notifications
        )
        showToast("Trained ${employee.name}! Skill: ${employee.skill + 8}")
        saveGameInternal()
    }

    fun promoteEmployee(employee: Employee) {
        val state = _gameState.value
        if (!employee.isPromotable) {
            showToast("${employee.name} has already reached the maximum tier (${employee.levelTitle})!")
            return
        }
        if (state.cash < employee.promotionCost) {
            showToast("Insufficient funds! Promotion bonus costs $%,.0f.".format(employee.promotionCost))
            return
        }
        val nextLevel = employee.level + 1
        val updatedEmployees = state.employees.map {
            if (it.id == employee.id) {
                it.copy(
                    level = nextLevel,
                    skill = (it.skill + 12).coerceAtMost(99),
                    speed = (it.speed + 10).coerceAtMost(99),
                    dailySalary = it.dailySalary * 1.30,
                    morale = 100
                )
            } else it
        }
        val promotedTitle = updatedEmployees.find { it.id == employee.id }?.levelTitle ?: "Senior"
        _gameState.value = state.copy(
            cash = state.cash - employee.promotionCost,
            employees = updatedEmployees,
            notifications = listOf("★ PROMOTION: ${employee.name} promoted to Level $nextLevel $promotedTitle! Skill +12, Speed +10.") + state.notifications
        )
        showToast("Promoted ${employee.name} to $promotedTitle!")
        com.example.audio.SoundManager.playLaunch()
        saveGameInternal()
    }

    fun sendOnRetreat(employee: Employee) {
        val state = _gameState.value
        if (state.cash < 300.0) {
            showToast("Insufficient funds! Wellness retreat costs $300.")
            return
        }

        val updatedEmployees = state.employees.map {
            if (it.id == employee.id) {
                it.copy(
                    morale = 100,
                    energy = 100,
                    state = EmployeeState.RESTING
                )
            } else it
        }
        _gameState.value = state.copy(
            cash = state.cash - 300.0,
            employees = updatedEmployees,
            notifications = listOf("${employee.name} returned from retreat with 100% morale!") + state.notifications
        )
        showToast("${employee.name} sent on retreat! Morale & Energy fully restored.")
        saveGameInternal()
    }

    fun fireEmployee(employee: Employee) {
        val state = _gameState.value
        val updatedStaff = state.employees.filter { it.id != employee.id }
        val updatedFurniture = state.furniture.map {
            if (it.assignedEmployeeId == employee.id) it.copy(assignedEmployeeId = null) else it
        }
        _gameState.value = state.copy(
            employees = updatedStaff,
            furniture = updatedFurniture,
            notifications = listOf("Terminated contract for ${employee.name}.") + state.notifications
        )
        showToast("Terminated employment contract for ${employee.name}.")
        saveGameInternal()
    }

    fun assignProduct(employee: Employee, productId: String?) {
        val state = _gameState.value
        val updatedEmployees = state.employees.map {
            if (it.id == employee.id) {
                it.copy(
                    assignedProductId = productId,
                    assignedContractId = if (productId != null) null else it.assignedContractId
                )
            } else it
        }
        val prodName = state.products.find { it.id == productId }?.name ?: "Brainstorming"
        _gameState.value = state.copy(employees = updatedEmployees)
        showToast("Assigned ${employee.name} to $prodName.")
    }

    fun upgradeOffice(tier: OfficeTier) {
        val state = _gameState.value
        val meetsLevel = state.companyLevel >= tier.requiredCompanyLevel
        val meetsUsers = state.totalActiveUsers >= tier.requiredActiveUsers
        val meetsProducts = state.products.count { it.stage == ProductStage.LIVE } >= tier.requiredLiveProducts

        if (!meetsLevel || !meetsUsers || !meetsProducts) {
            showToast("Cannot relocate: ${tier.unlockRequirementText}")
            com.example.audio.SoundManager.playError()
            return
        }

        if (state.cash < tier.upgradeCost) {
            showToast("Insufficient funds! Need $%,.0f to relocate.".format(tier.upgradeCost))
            com.example.audio.SoundManager.playError()
            return
        }

        // Clamp furniture coordinates within new grid boundaries
        val adjustedFurniture = state.furniture.map { furn ->
            furn.copy(
                gridX = furn.gridX.coerceIn(0, tier.gridWidth - 1),
                gridY = furn.gridY.coerceIn(0, tier.gridHeight - 1)
            )
        }

        _gameState.value = state.copy(
            cash = state.cash - tier.upgradeCost,
            officeTier = tier,
            furniture = adjustedFurniture,
            notifications = listOf("Relocated headquarters to ${tier.title}! Capacity expanded to ${tier.maxDesks} desks.") + state.notifications
        )
        showToast("Relocated to ${tier.title}!")
        com.example.audio.SoundManager.playLaunch()
        saveGameInternal()
    }

    fun buyFurniture(type: FurnitureType) {
        val state = _gameState.value
        if (type.isWorkstation && state.totalWorkstations >= state.officeTier.maxDesks) {
            showToast("Workstation limit reached for current office (${state.officeTier.maxDesks} max)! Upgrade office first.")
            return
        }

        if (state.cash < type.cost) {
            showToast("Insufficient funds! Need $%,.0f for ${type.title}.".format(type.cost))
            return
        }

        // Place on next open grid tile
        val gridW = state.officeTier.gridWidth
        val gridH = state.officeTier.gridHeight
        val occupiedTiles = state.furniture.map { it.gridX to it.gridY }.toSet()

        var chosenTile: Pair<Int, Int>? = null
        for (gx in 1 until gridW - 1) {
            for (gy in 1 until gridH - 1) {
                if (!occupiedTiles.contains(gx to gy)) {
                    chosenTile = gx to gy
                    break
                }
            }
            if (chosenTile != null) break
        }

        val (tx, ty) = chosenTile ?: (1 to 1)
        val newFurniture = PlacedFurniture(
            id = "furn_${System.currentTimeMillis()}",
            type = type,
            gridX = tx,
            gridY = ty,
            rotation = 0
        )

        // If server rack, expand rack capacity
        val updatedServer = if (type.category == FurnitureCategory.INFRASTRUCTURE) {
            state.server.copy(rackCapacity = state.server.rackCapacity + type.userCapacityBoost)
        } else state.server

        _gameState.value = state.copy(
            cash = state.cash - type.cost,
            furniture = state.furniture + newFurniture,
            server = updatedServer,
            notifications = listOf("Installed ${type.title} in the office.") + state.notifications
        )
        showToast("Installed ${type.title} (-$%,.0f)".format(type.cost))
        saveGameInternal()
    }

    fun buyFurnitureAt(type: FurnitureType, x: Int, y: Int) {
        val state = _gameState.value
        if (type.isWorkstation && state.totalWorkstations >= state.officeTier.maxDesks) {
            showToast("Workstation limit reached for current office (${state.officeTier.maxDesks} max)! Upgrade office first.")
            return
        }

        if (state.cash < type.cost) {
            showToast("Insufficient funds! Need $%,.0f for ${type.title}.".format(type.cost))
            return
        }

        val occupiedTiles = state.furniture.map { it.gridX to it.gridY }.toSet()
        if (occupiedTiles.contains(x to y)) {
            showToast("Tile ($x, $y) is already occupied!")
            return
        }

        val newFurniture = PlacedFurniture(
            id = "furn_${System.currentTimeMillis()}",
            type = type,
            gridX = x,
            gridY = y,
            rotation = 0
        )

        val updatedServer = if (type.category == FurnitureCategory.INFRASTRUCTURE) {
            state.server.copy(rackCapacity = state.server.rackCapacity + type.userCapacityBoost)
        } else state.server

        _gameState.value = state.copy(
            cash = state.cash - type.cost,
            furniture = state.furniture + newFurniture,
            server = updatedServer,
            notifications = listOf("Placed ${type.title} at ($x, $y).") + state.notifications
        )
        showToast("Placed ${type.title} at ($x, $y) (-$%,.0f)".format(type.cost))
        saveGameInternal()
    }

    fun moveFurniture(furnitureId: String, newX: Int, newY: Int) {
        val state = _gameState.value
        val furn = state.furniture.find { it.id == furnitureId } ?: return

        val gridW = state.officeTier.gridWidth
        val gridH = state.officeTier.gridHeight
        if (newX !in 0 until gridW || newY !in 0 until gridH) {
            showToast("Cannot move outside office walls!")
            return
        }

        val otherTiles = state.furniture.filter { it.id != furnitureId }.map { it.gridX to it.gridY }.toSet()
        if (otherTiles.contains(newX to newY)) {
            showToast("Tile ($newX, $newY) is already occupied!")
            return
        }

        val updatedFurniture = state.furniture.map {
            if (it.id == furnitureId) it.copy(gridX = newX, gridY = newY) else it
        }

        _gameState.value = state.copy(furniture = updatedFurniture)
        showToast("Moved ${furn.type.title} to ($newX, $newY).")
        saveGameInternal()
    }

    fun rotateFurniture(furnitureId: String) {
        val state = _gameState.value
        val furn = state.furniture.find { it.id == furnitureId } ?: return
        val newRot = (furn.rotation + 90) % 360

        val updatedFurniture = state.furniture.map {
            if (it.id == furnitureId) it.copy(rotation = newRot) else it
        }

        _gameState.value = state.copy(furniture = updatedFurniture)
        showToast("Rotated ${furn.type.title} to ${newRot}°.")
        saveGameInternal()
    }

    fun sellFurniture(furnitureId: String) {
        val state = _gameState.value
        val furn = state.furniture.find { it.id == furnitureId } ?: return
        val refund = furn.type.cost * 0.60

        // If had assigned employee, unassign desk
        val updatedEmployees = state.employees.map {
            if (it.assignedDeskId == furnitureId) it.copy(assignedDeskId = null) else it
        }

        // If was server rack, reduce capacity
        val updatedServer = if (furn.type.category == FurnitureCategory.INFRASTRUCTURE) {
            state.server.copy(rackCapacity = (state.server.rackCapacity - furn.type.userCapacityBoost).coerceAtLeast(0))
        } else state.server

        _gameState.value = state.copy(
            cash = state.cash + refund,
            furniture = state.furniture.filter { it.id != furnitureId },
            employees = updatedEmployees,
            server = updatedServer,
            notifications = listOf("Sold ${furn.type.title} for $%,.0f refund.".format(refund)) + state.notifications
        )
        showToast("Sold ${furn.type.title} for +$%,.0f refund.".format(refund))
        saveGameInternal()
    }

    fun createProduct(name: String, category: ProductCategory, monetization: MonetizationModel) {
        val state = _gameState.value
        val newProduct = Product(
            id = "prod_${System.currentTimeMillis()}",
            name = name,
            category = category,
            stage = ProductStage.PLANNING,
            monetization = monetization,
            planningPointsNeeded = (category.baseDesignReq * 0.4f).toInt().coerceAtLeast(30),
            planningPointsCurrent = 0,
            designPointsNeeded = category.baseDesignReq,
            codePointsNeeded = category.baseCodeReq,
            testPointsNeeded = category.baseTestReq,
            features = defaultFeaturesForCategory(category)
        )
        _gameState.value = state.copy(
            products = state.products + newProduct,
            notifications = listOf("Started planning & design phase for new product '$name'!") + state.notifications
        )
        showToast("Started planning on '$name' (Designers needed)!")
        saveGameInternal()
    }

    fun conductMarketResearch(product: Product) {
        val state = _gameState.value
        val cost = 800.0
        if (state.cash < cost) {
            showToast("Insufficient funds! Market research costs $800.")
            return
        }
        val rivalsInCategory = state.competitors.count { it.category == product.category && !it.isAcquired }
        val saturation = when {
            rivalsInCategory >= 3 -> "High Saturation"
            rivalsInCategory >= 1 -> "Moderate Competition"
            else -> "Underserved Niche (High Potential)"
        }
        val demand = when (product.category) {
            ProductCategory.AI_ASSISTANT -> 1.35f
            ProductCategory.SOCIAL_APP -> 1.15f
            ProductCategory.SAAS_TOOL -> 1.05f
            ProductCategory.MOBILE_GAME -> 1.20f
            ProductCategory.ECOMMERCE -> 0.95f
        }
        val report = MarketResearchReport(
            isCompleted = true,
            marketSaturation = saturation,
            demandScore = demand,
            recommendedPricing = when (product.category) {
                ProductCategory.SAAS_TOOL -> MonetizationModel.SUBSCRIPTION
                ProductCategory.AI_ASSISTANT -> MonetizationModel.ENTERPRISE
                ProductCategory.MOBILE_GAME -> MonetizationModel.FREEMIUM
                ProductCategory.SOCIAL_APP -> MonetizationModel.FREE_WITH_ADS
                ProductCategory.ECOMMERCE -> MonetizationModel.FREEMIUM
            },
            targetAudience = when (product.category) {
                ProductCategory.SAAS_TOOL -> "B2B SMBs & Enterprises"
                ProductCategory.AI_ASSISTANT -> "Developers & Power Users"
                ProductCategory.MOBILE_GAME -> "Gen-Z & Mobile Gamers"
                ProductCategory.SOCIAL_APP -> "Global Creators & Communities"
                ProductCategory.ECOMMERCE -> "Digital Shoppers & Merchants"
            },
            estimatedMaxUsers = (120000 * demand).toInt(),
            competitorDensity = "$rivalsInCategory Active Rivals"
        )
        val updatedProducts = state.products.map {
            if (it.id == product.id) it.copy(marketResearch = report) else it
        }
        _gameState.value = state.copy(
            cash = state.cash - cost,
            products = updatedProducts,
            notifications = listOf("Completed Market Feasibility Study for '${product.name}'! Demand: ${(demand * 100).toInt()}%. Saturation: $saturation.") + state.notifications
        )
        showToast("Market research complete! Demand: ${(demand * 100).toInt()}%")
        com.example.audio.SoundManager.playConfirm()
        saveGameInternal()
    }

    fun startFeatureDevelopment(productId: String, featureId: String) {
        val state = _gameState.value
        val cost = 450.0 // Tooling, licenses & sprint setup
        if (state.cash < cost) {
            showToast("Insufficient funds! Feature sprint setup costs $450.")
            return
        }
        val updatedProducts = state.products.map { prod ->
            if (prod.id == productId) {
                val updatedFeatures = prod.features.map { feat ->
                    if (feat.id == featureId) feat.copy(isUnderDevelopment = true) else feat
                }
                prod.copy(features = updatedFeatures)
            } else prod
        }
        _gameState.value = state.copy(
            cash = state.cash - cost,
            products = updatedProducts,
            notifications = listOf("Queued feature sprint for $450 tooling setup.") + state.notifications
        )
        showToast("Feature queued (-$450)! Assigned engineers will build it.")
        com.example.audio.SoundManager.playClick()
        saveGameInternal()
    }

    fun claimMilestone(milestoneId: String) {
        val state = _gameState.value
        val milestone = state.milestones.find { it.id == milestoneId } ?: return
        if (!milestone.isAchieved) {
            showToast("Milestone objectives not met yet!")
            return
        }
        if (milestone.isClaimed) {
            showToast("Milestone reward already claimed!")
            return
        }

        val updatedMilestones = state.milestones.map {
            if (it.id == milestoneId) it.copy(isClaimed = true) else it
        }

        _gameState.value = state.copy(
            cash = state.cash + milestone.rewardCash,
            reputation = (state.reputation + milestone.rewardReputation).coerceAtMost(500),
            milestones = updatedMilestones,
            notifications = listOf("CLAIMED MILESTONE: '${milestone.title}'! +$%,.0f, +%d Rep.".format(milestone.rewardCash, milestone.rewardReputation)) + state.notifications
        )
        showToast("Claimed '${milestone.title}'! +$%,.0f, +%d Rep.".format(milestone.rewardCash, milestone.rewardReputation))
        com.example.audio.SoundManager.playCash()
        saveGameInternal()
    }

    fun sellEquityForCapital(percent: Float) {
        val state = _gameState.value
        if (state.equityOwnedPercent <= 25.0f) {
            showToast("Cannot sell more equity! Founders must maintain at least 25% control.")
            return
        }
        val capitalRaised = state.valuation * (percent / 100.0)
        _gameState.value = state.copy(
            cash = state.cash + capitalRaised,
            equityOwnedPercent = (state.equityOwnedPercent - percent).coerceAtLeast(0f),
            notifications = listOf("Equity Investment: Sold ${percent.toInt()}% equity for $%,.0f in funding!".format(capitalRaised)) + state.notifications
        )
        showToast("Raised $%,.0f for ${percent.toInt()}% equity!".format(capitalRaised))
        com.example.audio.SoundManager.playCash()
        saveGameInternal()
    }

    fun toggleProductMaintenance(productId: String) {
        val state = _gameState.value
        val prod = state.products.find { it.id == productId } ?: return
        val newStage = if (prod.stage == ProductStage.MAINTENANCE) ProductStage.LIVE else ProductStage.MAINTENANCE
        val updatedProducts = state.products.map {
            if (it.id == productId) it.copy(stage = newStage) else it
        }
        _gameState.value = state.copy(products = updatedProducts)
        val modeDesc = if (newStage == ProductStage.MAINTENANCE) "in MAINTENANCE (bug fixing & code refactoring)" else "LIVE on market"
        showToast("${prod.name} is now $modeDesc.")
        saveGameInternal()
    }

    fun acceptContract(contractId: String) {
        val state = _gameState.value
        val contract = state.contracts.find { it.id == contractId } ?: return
        if (contract.status != ContractStatus.AVAILABLE) {
            showToast("Contract is not available.")
            return
        }
        if (state.reputation < contract.minReputation) {
            showToast("Requires ${contract.minReputation} Reputation (current: ${state.reputation})")
            com.example.audio.SoundManager.playError()
            return
        }

        val updatedContracts = state.contracts.map {
            if (it.id == contractId) it.copy(status = ContractStatus.IN_PROGRESS) else it
        }

        _gameState.value = state.copy(
            cash = state.cash + contract.upfrontDeposit,
            contracts = updatedContracts,
            notifications = listOf("Accepted client contract '${contract.title}'! Received +$%,.0f upfront deposit.".format(contract.upfrontDeposit)) + state.notifications
        )
        showToast("Accepted '${contract.title}' (+$%,.0f deposit)".format(contract.upfrontDeposit))
        com.example.audio.SoundManager.playConfirm()
        saveGameInternal()
    }

    fun assignStaffToContract(employeeId: String, contractId: String?) {
        val state = _gameState.value
        val emp = state.employees.find { it.id == employeeId } ?: return
        val updatedEmployees = state.employees.map {
            if (it.id == employeeId) {
                it.copy(
                    assignedContractId = contractId,
                    assignedProductId = if (contractId != null) null else it.assignedProductId
                )
            } else it
        }
        val updatedContracts = state.contracts.map { c ->
            if (c.id == contractId) {
                c.copy(assignedEmployeeIds = (c.assignedEmployeeIds + employeeId).distinct())
            } else {
                c.copy(assignedEmployeeIds = c.assignedEmployeeIds.filter { it != employeeId })
            }
        }
        _gameState.value = state.copy(employees = updatedEmployees, contracts = updatedContracts)
        val targetName = if (contractId != null) {
            state.contracts.find { it.id == contractId }?.title ?: "Contract"
        } else "Idle"
        showToast("Assigned ${emp.name} to $targetName.")
    }

    fun deliverContract(contractId: String) {
        val state = _gameState.value
        val contract = state.contracts.find { it.id == contractId } ?: return
        if (!contract.isDeliverable || contract.status != ContractStatus.IN_PROGRESS) {
            showToast("Contract requirements not met yet!")
            return
        }

        val updatedContracts = state.contracts.map {
            if (it.id == contractId) {
                it.copy(
                    status = ContractStatus.COMPLETED,
                    isDelivered = true,
                    isRewardPaid = true,
                    milestones = it.milestones.map { ms -> ms.copy(isCompleted = true) }
                )
            } else it
        }

        val updatedEmployees = state.employees.map {
            if (it.assignedContractId == contractId) it.copy(assignedContractId = null) else it
        }

        // Spawn a new higher-tier or fresh contract
        val freshContracts = SimulationEngine.generateFreshContracts(state.reputation + contract.reputationGain)
        val finalPayout = if (contract.isRewardPaid) 0.0 else contract.completionReward

        _gameState.value = state.copy(
            cash = state.cash + finalPayout,
            reputation = (state.reputation + contract.reputationGain).coerceAtMost(500),
            contracts = updatedContracts + freshContracts,
            employees = updatedEmployees,
            notifications = listOf("✓ DELIVERED: '${contract.title}' to ${contract.clientName}! Received final reward +$%,.0f, +%d Rep.".format(finalPayout, contract.reputationGain)) + state.notifications
        )
        showToast("Delivered '${contract.title}'! +$%,.0f".format(finalPayout))
        com.example.audio.SoundManager.playLaunch()
        saveGameInternal()
    }

    fun cancelContract(contractId: String) {
        val state = _gameState.value
        val contract = state.contracts.find { it.id == contractId } ?: return
        val penalty = contract.penaltyCost * 0.5
        val updatedContracts = state.contracts.filter { it.id != contractId }
        val updatedEmployees = state.employees.map {
            if (it.assignedContractId == contractId) it.copy(assignedContractId = null) else it
        }
        _gameState.value = state.copy(
            cash = state.cash - penalty,
            reputation = (state.reputation - 10).coerceAtLeast(10),
            contracts = updatedContracts,
            employees = updatedEmployees,
            notifications = listOf("Cancelled contract '${contract.title}'. Paid -$%,.0f cancellation penalty.".format(penalty)) + state.notifications
        )
        showToast("Cancelled contract (-$%,.0f penalty)".format(penalty))
        saveGameInternal()
    }

    fun acquireCompetitor(competitorId: String) {
        val state = _gameState.value
        val competitor = state.competitors.find { it.id == competitorId } ?: return
        if (competitor.isAcquired) {
            showToast("Already acquired!")
            return
        }
        if (state.cash < competitor.acquisitionCost) {
            showToast("Insufficient cash! Need $%,.0f to acquire ${competitor.name}.".format(competitor.acquisitionCost))
            com.example.audio.SoundManager.playError()
            return
        }

        val updatedCompetitors = state.competitors.map {
            if (it.id == competitorId) it.copy(
                isAcquired = true,
                status = "Acquired Subsidiary",
                latestActivity = "Operating as wholly-owned subsidiary of ${state.companyName}"
            ) else it
        }

        val matchingProduct = state.products.find { it.category == competitor.category && it.stage == ProductStage.LIVE }
        val updatedProducts = if (matchingProduct != null) {
            state.products.map {
                if (it.id == matchingProduct.id) {
                    it.copy(
                        activeUsers = it.activeUsers + (competitor.activeUsers * 0.45).toInt(),
                        yesterdayRevenue = it.yesterdayRevenue + (competitor.monthlyRevenue / 30.0 * 0.50)
                    )
                } else it
            }
        } else {
            val adopted = Product(
                id = "prod_acquired_${competitor.id}",
                name = competitor.productNames.firstOrNull() ?: "${competitor.name} Suite",
                category = competitor.category,
                stage = ProductStage.LIVE,
                activeUsers = (competitor.activeUsers * 0.45).toInt(),
                yesterdayRevenue = competitor.monthlyRevenue / 30.0 * 0.70,
                dailyRevenue = 0.0,
                monetization = MonetizationModel.SUBSCRIPTION
            )
            state.products + adopted
        }

        // Find available workstations for acquired staff
        val availableWorkstations = state.furniture.filter { it.type.isWorkstation && state.employees.none { emp -> emp.assignedDeskId == it.id } }
        val desk1 = availableWorkstations.getOrNull(0)?.id
        val desk2 = availableWorkstations.getOrNull(1)?.id

        val staff1Id = "emp_acq_${competitor.id}_1"
        val staff2Id = "emp_acq_${competitor.id}_2"

        val acquiredStaff = listOf(
            Employee(
                id = staff1Id,
                name = "${competitor.name.split(" ").first()} Lead Dev",
                role = EmployeeRole.DEVELOPER,
                level = 3,
                skill = 85,
                speed = 78,
                dailySalary = 160.0,
                morale = 95,
                energy = 95,
                assignedDeskId = desk1
            ),
            Employee(
                id = staff2Id,
                name = "${competitor.name.split(" ").first()} Senior PM",
                role = EmployeeRole.PROJECT_MANAGER,
                level = 3,
                skill = 82,
                speed = 80,
                dailySalary = 170.0,
                morale = 95,
                energy = 95,
                assignedDeskId = desk2
            )
        )

        val updatedFurniture = state.furniture.map { furn ->
            when (furn.id) {
                desk1 -> furn.copy(assignedEmployeeId = staff1Id)
                desk2 -> furn.copy(assignedEmployeeId = staff2Id)
                else -> furn
            }
        }

        _gameState.value = state.copy(
            cash = state.cash - competitor.acquisitionCost,
            reputation = (state.reputation + 45).coerceAtMost(500),
            competitors = updatedCompetitors,
            products = updatedProducts,
            employees = state.employees + acquiredStaff,
            furniture = updatedFurniture,
            notifications = listOf("STRATEGIC BUYOUT: Acquired ${competitor.name} for $%,.0f! Absorbed %,d users and senior engineering leadership.".format(competitor.acquisitionCost, (competitor.activeUsers * 0.45).toInt())) + state.notifications
        )
        showToast("Strategic Buyout: Acquired ${competitor.name}!")
        com.example.audio.SoundManager.playLaunch()
        saveGameInternal()
    }

    fun launchProduct(product: Product) {
        val state = _gameState.value
        val updatedProducts = state.products.map {
            if (it.id == product.id) {
                it.copy(
                    stage = ProductStage.LIVE,
                    activeUsers = 150, // initial launch cohort
                    dailyRevenue = 30.0
                )
            } else it
        }
        _gameState.value = state.copy(
            products = updatedProducts,
            notifications = listOf("PRODUCT LAUNCH! '${product.name}' is now live on the global market!") + state.notifications
        )
        showToast("🚀 '${product.name}' is now LIVE on the market!")
        saveGameInternal()
    }

    fun changeMonetization(product: Product, model: MonetizationModel) {
        val state = _gameState.value
        val updatedProducts = state.products.map {
            if (it.id == product.id) it.copy(monetization = model) else it
        }
        _gameState.value = state.copy(products = updatedProducts)
        showToast("Updated monetization to ${model.title}.")
    }

    fun changeMarketingBudget(product: Product, amount: Double) {
        val state = _gameState.value
        val updatedProducts = state.products.map {
            if (it.id == product.id) it.copy(dailyMarketingSpend = amount) else it
        }
        _gameState.value = state.copy(products = updatedProducts)
        showToast("Set daily marketing to $%,.0f/day.".format(amount))
    }

    fun launchCampaign(product: Product, type: CampaignType) {
        val state = _gameState.value
        if (state.cash < type.upfrontCost) {
            showToast("Insufficient funds! Need $%,.0f for ${type.title}.".format(type.upfrontCost))
            return
        }

        val campaign = ActiveCampaign(
            id = "camp_${System.currentTimeMillis()}",
            productId = product.id,
            type = type,
            daysRemaining = type.durationDays
        )

        val updatedProducts = state.products.map {
            if (it.id == product.id) {
                it.copy(activeCampaigns = it.activeCampaigns + campaign)
            } else it
        }

        _gameState.value = state.copy(
            cash = state.cash - type.upfrontCost,
            products = updatedProducts,
            notifications = listOf("Launched campaign '${type.title}' for '${product.name}' ($%,.0f)!".format(type.upfrontCost)) + state.notifications
        )
        showToast("Launched ${type.title} (-$%,.0f)".format(type.upfrontCost))
        saveGameInternal()
    }

    fun setSprintMode(product: Product, mode: SprintMode) {
        val state = _gameState.value
        val updatedProducts = state.products.map {
            if (it.id == product.id) it.copy(sprintMode = mode) else it
        }
        _gameState.value = state.copy(products = updatedProducts)
        showToast("Sprint pace set to ${mode.title}.")
    }

    fun startProductUpdate(product: Product) {
        val state = _gameState.value
        val nextVer = product.version.toDoubleOrNull()?.plus(0.1)?.let { "%.1f".format(it) } ?: "2.0"
        val updatedProducts = state.products.map {
            if (it.id == product.id) {
                it.copy(
                    version = nextVer,
                    stage = ProductStage.DEVELOPMENT,
                    designPointsNeeded = product.category.baseDesignReq / 2,
                    designPointsCurrent = 0,
                    codePointsNeeded = product.category.baseCodeReq / 2,
                    codePointsCurrent = 0,
                    testPointsNeeded = product.category.baseTestReq / 2,
                    testPointsCurrent = 0
                )
            } else it
        }
        _gameState.value = state.copy(
            products = updatedProducts,
            notifications = listOf("Started v$nextVer sprint update for '${product.name}'.") + state.notifications
        )
        showToast("Started v$nextVer update sprint for '${product.name}'!")
    }

    fun toggleCloudAutoScale(enabled: Boolean) {
        val state = _gameState.value
        _gameState.value = state.copy(
            server = state.server.copy(isCloudAutoScaleEnabled = enabled)
        )
        showToast("Cloud Elastic Auto-Scale: ${if (enabled) "ENABLED" else "DISABLED"}")
    }

    fun emergencyServerRestart() {
        val state = _gameState.value
        _gameState.value = state.copy(
            server = state.server.copy(
                isOutageActive = false,
                outageHoursRemaining = 0,
                incidentReason = null
            ),
            notifications = listOf("Emergency server cluster restart succeeded. Systems restored online.") + state.notifications
        )
        showToast("Emergency server reboot succeeded! Systems online.")
    }

    fun optimizeDatabaseServer() {
        val state = _gameState.value
        if (state.cash < 250.0) {
            showToast("Insufficient funds! Database optimization costs $250.")
            return
        }

        _gameState.value = state.copy(
            cash = state.cash - 250.0,
            server = state.server.copy(
                uptimePercent = (state.server.uptimePercent + 0.05f).coerceAtMost(99.99f)
            ),
            notifications = listOf("Optimized database indexing and garbage collection (-$250).") + state.notifications
        )
        showToast("Optimized database! Server uptime & latency improved.")
        saveGameInternal()
    }

    fun takeLoan(amount: Double, days: Int, dailyPayment: Double, lender: String) {
        val state = _gameState.value
        val currentDebt = state.activeLoans.sumOf { it.remainingBalance }
        val maxDebt = (state.valuation * 0.55).coerceIn(40000.0, 300000.0)
        if (currentDebt + amount > maxDebt) {
            showToast("Credit limit reached! Max borrowing limit is $%,.0f.".format(maxDebt))
            return
        }
        val newLoan = ActiveLoan(
            id = "loan_${System.currentTimeMillis()}",
            lender = lender,
            principalBorrowed = amount,
            dailyPayment = dailyPayment,
            daysRemaining = days,
            interestRate = 0.08f
        )
        _gameState.value = state.copy(
            cash = state.cash + amount,
            activeLoans = state.activeLoans + newLoan,
            notifications = listOf("Secured $%,.0f credit from $lender.".format(amount)) + state.notifications
        )
        showToast("Borrowed $%,.0f from $lender!".format(amount))
        saveGameInternal()
    }

    fun repayLoanEarly(loan: ActiveLoan) {
        val state = _gameState.value
        val totalDue = loan.dailyPayment * loan.daysRemaining
        if (state.cash < totalDue) {
            showToast("Insufficient cash to pay off loan early ($%,.0f needed)!".format(totalDue))
            return
        }

        _gameState.value = state.copy(
            cash = state.cash - totalDue,
            activeLoans = state.activeLoans.filter { it.id != loan.id },
            notifications = listOf("Repaid ${loan.lender} credit facility in full ($%,.0f)!".format(totalDue)) + state.notifications
        )
        showToast("Paid off ${loan.lender} loan in full! Daily payments eliminated.")
        saveGameInternal()
    }

    fun startResearch(tech: TechUpgrade) {
        val state = _gameState.value
        if (state.activeResearch != null) {
            showToast("Research already in progress: ${state.activeResearch?.techId}")
            return
        }
        if (state.cash < tech.cost) {
            showToast("Insufficient funds! Need $%,.0f to research ${tech.title}.".format(tech.cost))
            return
        }

        val research = ActiveResearch(
            techId = tech.id,
            daysInvested = 0,
            daysRequired = tech.requiredResearchDays
        )
        _gameState.value = state.copy(
            cash = state.cash - tech.cost,
            activeResearch = research,
            notifications = listOf("R&D department initiated research on '${tech.title}'.") + state.notifications
        )
        showToast("Started R&D on ${tech.title} (-$%,.0f)".format(tech.cost))
        saveGameInternal()
    }

    fun handleEventChoice(option: GameEventOption) {
        val state = _gameState.value
        var cashDelta = 0.0
        var newEquity = state.equityOwnedPercent
        var newStaff = state.employees
        var newProducts = state.products

        when (option.actionType) {
            EventActionType.ACCEPT_INVESTMENT -> {
                cashDelta += option.amount
                newEquity = (newEquity - 12f).coerceAtLeast(10f)
                showToast("Accepted investment! Added +$%,.0f cash.".format(option.amount))
            }
            EventActionType.REJECT_INVESTMENT -> {
                showToast("Declined investment. Retained 100% equity.")
            }
            EventActionType.PAY_FIX -> {
                cashDelta -= option.amount
                showToast("Paid $%,.0f for emergency repairs.".format(option.amount))
            }
            EventActionType.RISK_DOWNTIME -> {
                if (Random.nextFloat() < 0.6f) {
                    _gameState.value = _gameState.value.copy(
                        server = _gameState.value.server.copy(
                            isOutageActive = true,
                            outageHoursRemaining = 4,
                            incidentReason = "Thermal overload! Servers overheating."
                        )
                    )
                    showToast("Risk failed! Server room suffered thermal shutdown.")
                } else {
                    showToast("Lucky! Temperatures stabilized without downtime.")
                }
            }
            EventActionType.MATCH_SALARY -> {
                val empId = state.pendingEvent?.relatedEntityId
                if (empId != null) {
                    newStaff = state.employees.map {
                        if (it.id == empId) it.copy(dailySalary = it.dailySalary + option.amount, morale = 100) else it
                    }
                }
                showToast("Matched salary! Employee stays with 100% morale.")
            }
            EventActionType.LET_EMPLOYEE_GO -> {
                val empId = state.pendingEvent?.relatedEntityId
                if (empId != null) {
                    newStaff = state.employees.filter { it.id != empId }
                }
                showToast("Employee left to join Big Tech competitor.")
            }
            EventActionType.SIGN_ENTERPRISE_DEAL -> {
                cashDelta += option.amount
                showToast("Signed Fortune 500 Enterprise Deal! +$%,.0f".format(option.amount))
            }
            EventActionType.BUY_FIREWALL -> {
                cashDelta -= option.amount
                showToast("Activated Anti-DDoS protection shield.")
            }
            EventActionType.DISMISS -> {}
        }

        _gameState.value = _gameState.value.copy(
            cash = state.cash + cashDelta,
            equityOwnedPercent = newEquity,
            employees = newStaff,
            products = newProducts,
            pendingEvent = null
        )
        saveGameInternal()
    }

    fun restructureBailout() {
        val state = _gameState.value
        _gameState.value = state.copy(
            cash = 30000.0,
            equityOwnedPercent = (state.equityOwnedPercent - 35f).coerceAtLeast(10f),
            isBankrupt = false,
            bankruptcyGraceDays = 0,
            notifications = listOf("Chapter 11 restructuring approved. Injected $30,000 cash; 35% equity diluted.") + state.notifications
        )
        showToast("Chapter 11 Restructuring complete! Injected $30,000.")
        saveGameInternal()
    }

    fun resetGame() {
        val fresh = GameState()
        _gameState.value = fresh
        GameStorage.saveGame(getApplication(), fresh)
        showToast("Game reset! Welcome to your new startup.")
    }

    fun saveGame() {
        saveGameInternal()
        showToast("Game progress successfully saved to device!")
    }

    private fun saveGameInternal() {
        GameStorage.saveGame(getApplication(), _gameState.value)
    }
}
