package com.example.model

data class GameState(
    val companyName: String = "Aether Tech",
    val companyLogoIcon: String = "rocket",
    val companyLogoColor: Long = 0xFF00E5FF,
    val companyLogoShape: String = "rounded",
    val difficulty: String = "NORMAL",
    val companyLevel: Int = 1,
    val reputation: Int = 100,
    val cash: Double = 30000.0,
    val equityOwnedPercent: Float = 100.0f,
    val clock: GameClock = GameClock(),
    val officeTier: OfficeTier = OfficeTier.GARAGE,
    val furniture: List<PlacedFurniture> = defaultStartingFurniture(),
    val employees: List<Employee> = defaultStartingEmployees(),
    val availableCandidates: List<Employee> = defaultInitialCandidates(),
    val products: List<Product> = defaultStartingProducts(),
    val server: ServerInfrastructure = ServerInfrastructure(),
    val contracts: List<ClientContract> = ClientContract.defaultStartingContracts(),
    val competitors: List<CompetitorCompany> = CompetitorCompany.defaultCompetitors(),
    val unlockedTechIds: Set<String> = emptySet(),
    val activeResearch: ActiveResearch? = null,
    val activeLoans: List<ActiveLoan> = emptyList(),
    val financialHistory: List<FinancialRecord> = emptyList(),
    val todayLedger: DailyLedger = DailyLedger(),
    val milestones: List<CompanyMilestone> = CompanyMilestone.defaultMilestones(),
    val pendingEvent: GameEvent? = null,
    val notifications: List<String> = listOf("Welcome to Startup Empire 3D! Hire staff, set up desks, and launch your first digital product."),
    val isBankrupt: Boolean = false,
    val bankruptcyGraceDays: Int = 0
) {
    val totalWorkstations: Int get() = furniture.count { it.type.isWorkstation }
    val availableDesks: Int get() = (totalWorkstations - employees.size).coerceAtLeast(0)

    val dailyNetProfit: Double get() = effectiveDailyRevenue - dailyExpenses
    val burnRate: Double get() = if (dailyNetProfit < 0) -dailyNetProfit else 0.0
    val cashRunwayDays: Int get() = if (burnRate > 0 && cash > 0) (cash / burnRate).toInt() else if (cash <= 0) 0 else 999

    val avgEmployeeMorale: Int get() = if (employees.isNotEmpty()) employees.map { it.morale }.average().toInt() else 0
    val avgEmployeeSkill: Int get() = if (employees.isNotEmpty()) employees.map { it.skill }.average().toInt() else 0
    val avgEmployeeEnergy: Int get() = if (employees.isNotEmpty()) employees.map { it.energy }.average().toInt() else 0

    val topProduct: Product? get() = products.filter { it.stage == ProductStage.LIVE }.maxByOrNull { it.activeUsers }
    val topEmployee: Employee? get() = employees.maxByOrNull { it.skill }

    val totalActiveUsers: Int get() = products.filter { it.stage == ProductStage.LIVE }.sumOf { it.activeUsers }

    val effectiveDailyRevenue: Double get() = products.filter { it.stage == ProductStage.LIVE }
        .sumOf { prod ->
            if (prod.yesterdayRevenue > 0) {
                prod.yesterdayRevenue.coerceAtLeast(prod.dailyRevenue)
            } else {
                val hourFactor = (24.0 / clock.hour.coerceAtLeast(1)).coerceIn(1.0, 24.0)
                prod.dailyRevenue * hourFactor
            }
        }

    val monthlyRevenue: Double get() = effectiveDailyRevenue * 30.0

    val dailyExpenses: Double get() =
        employees.sumOf { it.dailySalary } +
        (officeTier.monthlyRent / 30.0) +
        server.calculateDailyCost(totalActiveUsers) +
        products.sumOf { it.dailyMarketingSpend } +
        activeLoans.sumOf { it.dailyPayment }

    val monthlyExpenses: Double get() = dailyExpenses * 30.0
    val monthlyProfit: Double get() = monthlyRevenue - monthlyExpenses

    val serverLoad: Float get() = server.calculateLoad(totalActiveUsers)

    val marketShare: Float get() {
        val playerUsers = totalActiveUsers
        val competitorUsers = competitors.filter { !it.isAcquired }.sumOf { it.activeUsers }
        val totalMarketUsers = playerUsers + competitorUsers
        return if (totalMarketUsers > 0) {
            ((playerUsers.toFloat() / totalMarketUsers) * 100f).coerceIn(0.1f, 100f)
        } else 1.0f
    }

    val levelTitle: String get() = when (companyLevel) {
        1 -> "Garage Hacker"
        2 -> "Seed Startup"
        3 -> "Series A Growth"
        4 -> "Tech Scale-Up"
        5 -> "Unicorn Leader"
        else -> "Global Tech Empire"
    }

    val valuation: Double get() {
        val annualRevenue = monthlyRevenue * 12.0
        val liveProducts = products.filter { it.stage == ProductStage.LIVE }
        val avgSatisfaction = if (liveProducts.isNotEmpty()) {
            liveProducts.map { it.userSatisfaction }.average().toFloat()
        } else 0.85f
        val satisfactionFactor = (avgSatisfaction / 0.85f).coerceIn(0.7f, 1.35f)

        val profitMargin = if (monthlyRevenue > 0) (monthlyProfit / monthlyRevenue).coerceIn(-1.0, 1.0) else -0.5
        val earningsMultiple = when {
            profitMargin >= 0.35 -> 5.5
            profitMargin >= 0.15 -> 4.5
            profitMargin >= 0.0 -> 3.8
            profitMargin >= -0.25 -> 2.6
            else -> 1.8
        }

        val revenueValuation = annualRevenue * earningsMultiple * satisfactionFactor
        val usersValue = totalActiveUsers * when {
            totalActiveUsers > 150000 -> 15.0
            totalActiveUsers > 25000 -> 12.0
            else -> 10.0
        }
        val physicalAssets = furniture.sumOf { it.type.cost * 0.70 } + (officeTier.ordinal * 14000.0)
        val techPatentsValue = unlockedTechIds.size * 8000.0
        val totalDebt = activeLoans.sumOf { it.remainingBalance }
        val brandEquity = reputation * 300.0
        val liquidCash = cash.coerceAtLeast(0.0)

        val totalBase = liquidCash + physicalAssets - totalDebt + revenueValuation + usersValue + techPatentsValue + brandEquity
        return (totalBase * (equityOwnedPercent / 100.0)).coerceAtLeast(15000.0)
    }

    val leaderboard: List<LeaderboardEntry> get() {
        val playerEntry = LeaderboardEntry(
            rank = 1,
            companyId = "player_company",
            companyName = companyName,
            logoIcon = companyLogoIcon,
            logoColor = companyLogoColor,
            valuation = valuation,
            monthlyRevenue = monthlyRevenue,
            monthlyProfit = monthlyProfit,
            activeUsers = totalActiveUsers,
            marketShare = marketShare,
            reputation = reputation,
            isPlayer = true,
            statusText = levelTitle,
            latestActivity = "Organic growth & pipeline delivery"
        )
        val competitorEntries = competitors.map { comp ->
            LeaderboardEntry(
                rank = 0,
                rankDelta = comp.rankDelta,
                companyId = comp.id,
                companyName = comp.name,
                logoIcon = comp.logoIcon,
                logoColor = comp.logoColor,
                valuation = comp.valuation,
                monthlyRevenue = comp.monthlyRevenue,
                monthlyProfit = comp.monthlyProfit,
                activeUsers = comp.activeUsers,
                marketShare = comp.marketShare,
                reputation = comp.reputation,
                isPlayer = false,
                isAcquired = comp.isAcquired,
                statusText = comp.status,
                latestActivity = comp.latestActivity
            )
        }
        return (listOf(playerEntry) + competitorEntries)
            .sortedByDescending { it.valuation }
            .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
    }

    companion object {
        fun defaultStartingFurniture(): List<PlacedFurniture> = listOf(
            PlacedFurniture(
                id = "desk_0",
                type = FurnitureType.DESK_STANDARD,
                gridX = 1,
                gridY = 3,
                rotation = 0,
                assignedEmployeeId = "emp_founder"
            ),
            PlacedFurniture(
                id = "desk_1",
                type = FurnitureType.DESK_DUAL_SCREEN,
                gridX = 3,
                gridY = 1,
                rotation = 0
            ),
            PlacedFurniture(
                id = "server_0",
                type = FurnitureType.SERVER_RACK_BLADE,
                gridX = 5,
                gridY = 1,
                rotation = 0
            ),
            PlacedFurniture(
                id = "plant_0",
                type = FurnitureType.BONSAI_PLANT,
                gridX = 4,
                gridY = 2,
                rotation = 0
            ),
            PlacedFurniture(
                id = "water_0",
                type = FurnitureType.WATER_COOLER,
                gridX = 1,
                gridY = 5,
                rotation = 0
            )
        )

        fun defaultStartingEmployees(): List<Employee> = listOf(
            Employee(
                id = "emp_founder",
                name = "Alex Vance (Founder)",
                role = EmployeeRole.DEVELOPER,
                level = 2,
                skill = 65,
                speed = 60,
                dailySalary = 80.0, // Reduced founder salary
                morale = 95,
                energy = 95,
                trait = EmployeeTrait.TEN_X_CODER,
                assignedProductId = "prod_initial",
                assignedDeskId = "desk_0",
                hairColor = 0xFF2D3748,
                shirtColor = 0xFF0284C7,
                skinTone = 0xFFFFDBAC
            )
        )

        fun defaultInitialCandidates(): List<Employee> = listOf(
            Employee(
                id = "cand_1",
                name = "Maya Lin",
                role = EmployeeRole.DESIGNER,
                level = 1,
                skill = 55,
                speed = 50,
                dailySalary = 100.0,
                morale = 85,
                energy = 90,
                trait = EmployeeTrait.PIXEL_PERFECT,
                hairColor = 0xFF1E293B,
                shirtColor = 0xFFEC4899,
                skinTone = 0xFFE0AC69
            ),
            Employee(
                id = "cand_2",
                name = "Jordan Reed",
                role = EmployeeRole.DEVELOPER,
                level = 1,
                skill = 50,
                speed = 55,
                dailySalary = 120.0,
                morale = 80,
                energy = 85,
                trait = EmployeeTrait.RAPID_LEARNER,
                hairColor = 0xFF78350F,
                shirtColor = 0xFF10B981,
                skinTone = 0xFFFCD34D
            ),
            Employee(
                id = "cand_3",
                name = "Samantha Cruz",
                role = EmployeeRole.MARKETER,
                level = 2,
                skill = 65,
                speed = 60,
                dailySalary = 135.0,
                morale = 90,
                energy = 90,
                trait = EmployeeTrait.DEAL_CLOSER,
                hairColor = 0xFF4B5563,
                shirtColor = 0xFF8B5CF6,
                skinTone = 0xFFFBBF24
            ),
            Employee(
                id = "cand_4",
                name = "David Kim",
                role = EmployeeRole.SYSADMIN,
                level = 1,
                skill = 58,
                speed = 52,
                dailySalary = 130.0,
                morale = 85,
                energy = 90,
                trait = EmployeeTrait.CAFFEINE_SAVANT,
                hairColor = 0xFF0F172A,
                shirtColor = 0xFFF59E0B,
                skinTone = 0xFFFFDFBA
            )
        )

        fun defaultStartingProducts(): List<Product> = listOf(
            Product(
                id = "prod_initial",
                name = "Sphere Social",
                category = ProductCategory.SOCIAL_APP,
                stage = ProductStage.DEVELOPMENT,
                designPointsNeeded = 120,
                designPointsCurrent = 50,
                codePointsNeeded = 200,
                codePointsCurrent = 75,
                testPointsNeeded = 80,
                testPointsCurrent = 20,
                bugsCount = 3,
                monetization = MonetizationModel.FREEMIUM,
                features = defaultFeaturesForCategory(ProductCategory.SOCIAL_APP)
            )
        )
    }
}
