package com.example.engine

import com.example.model.*
import kotlin.random.Random

object SimulationEngine {

    fun tick(currentState: GameState, hoursToAdvance: Int = 1): GameState {
        if (currentState.clock.speed == GameSpeed.PAUSED || currentState.isBankrupt) {
            return currentState
        }

        var state = currentState
        for (i in 0 until hoursToAdvance) {
            state = advanceOneHour(state)
        }
        return state
    }

    private fun advanceOneHour(state: GameState): GameState {
        val oldClock = state.clock
        val nextHour = (oldClock.hour + 1) % 24
        val isNewDay = nextHour == 0
        val nextDay = if (isNewDay) oldClock.day + 1 else oldClock.day
        val newClock = oldClock.copy(
            day = nextDay,
            hour = nextHour,
            totalHoursElapsed = oldClock.totalHoursElapsed + 1
        )

        // 1. Calculate bonuses from tech tree and office amenities
        val devBonus = calculateDevSpeedBonus(state)
        val designBonus = calculateDesignSpeedBonus(state)
        val bugReduction = calculateBugReduction(state)
        val hasEspresso = state.furniture.any { it.type == FurnitureType.ESPRESSO_MACHINE }
        val hasArcade = state.furniture.any { it.type == FurnitureType.ARCADE_CABINET }

        // 2. Process Employees and Work Output
        val isWorkHour = oldClock.hour in 9..19
        val updatedEmployees = state.employees.map { emp ->
            processEmployeeHour(emp, isWorkHour, hasEspresso, hasArcade)
        }

        // 3. Process Product Development and Live Operations
        val (updatedProducts, hourlyProductRevenue, hourlyMarketingSpend) = processProductsHour(
            products = state.products,
            employees = updatedEmployees,
            isWorkHour = isWorkHour,
            devBonus = devBonus,
            designBonus = designBonus,
            bugReduction = bugReduction,
            serverLoad = state.serverLoad
        )

        // 4. Process Client Contracts Progress and Milestones
        val (updatedContracts, milestonePayouts) = processContractsHour(
            contracts = state.contracts,
            employees = updatedEmployees,
            isWorkHour = isWorkHour
        )

        // 5. Server health & Outages
        val totalActiveUsers = updatedProducts.filter { it.stage == ProductStage.LIVE }.sumOf { it.activeUsers }
        val updatedServer = processServerHour(state.server, totalActiveUsers)

        // 6. Financial tracking for current day
        val netHourlyCashDelta = hourlyProductRevenue + milestonePayouts - (hourlyMarketingSpend / 24.0)
        val currentCash = state.cash + netHourlyCashDelta

        var ledger = state.todayLedger.copy(
            productRevenue = state.todayLedger.productRevenue + hourlyProductRevenue,
            contractRevenue = state.todayLedger.contractRevenue + milestonePayouts,
            marketingExpenses = state.todayLedger.marketingExpenses + (hourlyMarketingSpend / 24.0)
        )

        var finalCash = currentCash
        var finalReputation = state.reputation
        var finalHistory = state.financialHistory
        var finalLoans = state.activeLoans
        var finalCandidates = state.availableCandidates
        var finalResearch = state.activeResearch
        var finalUnlockedTech = state.unlockedTechIds
        var finalProducts = updatedProducts
        var finalContracts = updatedContracts
        var finalCompetitors = state.competitors
        var newNotifications = state.notifications.toMutableList()
        var newEvent = state.pendingEvent
        var graceDays = state.bankruptcyGraceDays
        var isBankrupt = state.isBankrupt
        var newLevel = state.companyLevel

        if (milestonePayouts > 0) {
            newNotifications.add(0, "Contract Milestone Reached! Received +$${milestonePayouts.toInt()} milestone payment.")
        }

        // 7. Day rollover actions
        if (isNewDay) {
            // Deduct Daily Salaries
            val dailyPayroll = updatedEmployees.sumOf { it.dailySalary }
            // Deduct Daily Rent
            val dailyRent = state.officeTier.monthlyRent / 30.0
            // Deduct Server Hosting
            val dailyServerCost = updatedServer.calculateDailyCost(totalActiveUsers)
            // Loan repayments
            var loanDeductions = 0.0
            finalLoans = state.activeLoans.mapNotNull { loan ->
                val nextDays = loan.daysRemaining - 1
                loanDeductions += loan.dailyPayment
                if (nextDays <= 0) null else loan.copy(daysRemaining = nextDays)
            }

            finalCash -= (dailyPayroll + dailyRent + dailyServerCost + loanDeductions)

            ledger = ledger.copy(
                payrollExpenses = dailyPayroll,
                rentExpenses = dailyRent,
                serverExpenses = dailyServerCost,
                loanRepaymentExpenses = loanDeductions
            )

            // Record into financial history (keep up to 14 days)
            val dayRecord = FinancialRecord(
                day = oldClock.day,
                revenue = ledger.totalRevenue,
                expenses = ledger.totalExpenses,
                activeUsers = totalActiveUsers,
                cashAtEndOfDay = finalCash
            )
            finalHistory = (finalHistory + dayRecord).takeLast(14)

            // Reset ledger for the fresh day
            ledger = DailyLedger()

            // Evaluate Client Contracts Status, Deadlines, and Deliveries
            val processedContracts = mutableListOf<ClientContract>()
            val completedOrFailedContractIds = mutableSetOf<String>()

            for (contract in finalContracts) {
                if (contract.status == ContractStatus.IN_PROGRESS) {
                    val daysLeft = contract.daysRemaining - 1
                    if (contract.isDeliverable) {
                        // Completed!
                        finalCash += contract.completionReward
                        finalReputation = (finalReputation + contract.reputationGain).coerceAtMost(500)
                        newNotifications.add(
                            0,
                            "Contract Completed: '${contract.title}' delivered to ${contract.clientName}! Payout: +$${contract.completionReward.toInt()}, +${contract.reputationGain} Rep!"
                        )
                        completedOrFailedContractIds.add(contract.id)
                        processedContracts.add(contract.copy(status = ContractStatus.COMPLETED, daysRemaining = daysLeft))
                    } else if (daysLeft <= 0) {
                        // Failed!
                        finalCash -= contract.penaltyCost
                        finalReputation = (finalReputation - 15).coerceAtLeast(10)
                        newNotifications.add(
                            0,
                            "Contract Failed: Deadline expired on '${contract.title}'. Client penalty assessed: -$${contract.penaltyCost.toInt()}!"
                        )
                        completedOrFailedContractIds.add(contract.id)
                        processedContracts.add(contract.copy(status = ContractStatus.FAILED, daysRemaining = 0))
                    } else {
                        processedContracts.add(contract.copy(daysRemaining = daysLeft))
                    }
                } else {
                    processedContracts.add(contract)
                }
            }

            finalContracts = processedContracts

            // Unassign staff from completed or failed contracts
            if (completedOrFailedContractIds.isNotEmpty()) {
                state.employees.forEach { emp ->
                    if (emp.assignedContractId in completedOrFailedContractIds) {
                        // Cleared in updatedEmployees below
                    }
                }
            }

            // Refresh available client contracts if low
            val availableCount = finalContracts.count { it.status == ContractStatus.AVAILABLE }
            if (availableCount < 2 && nextDay % 4 == 0) {
                finalContracts = finalContracts + generateFreshContracts(finalReputation)
                newNotifications.add(0, "New enterprise client contracts are available for bidding!")
            }

            // Simulate AI Competitor Companies
            finalCompetitors = simulateCompetitorsDay(finalCompetitors, totalActiveUsers)

            // Active marketing campaigns day decrement
            finalProducts = updatedProducts.map { prod ->
                val nextCampaigns = prod.activeCampaigns.mapNotNull { camp ->
                    val rem = camp.daysRemaining - 1
                    if (rem <= 0) {
                        newNotifications.add(0, "Campaign Complete: '${camp.type.title}' brought +${camp.usersGenerated} active users to '${prod.name}'!")
                        null
                    } else {
                        camp.copy(daysRemaining = rem)
                    }
                }

                // Tech debt accumulation on live products every few days if not in maintenance
                val nextTechDebt = if (prod.stage == ProductStage.LIVE && nextDay % 3 == 0) {
                    (prod.techDebt + 1).coerceAtMost(30)
                } else {
                    prod.techDebt
                }

                prod.copy(activeCampaigns = nextCampaigns, techDebt = nextTechDebt)
            }

            // Refresh candidate pool every 3 days
            if (nextDay % 3 == 0) {
                finalCandidates = generateFreshCandidates(state.companyLevel)
                newNotifications.add(0, "New job applicants have applied to your recruitment board!")
            }

            // Research progression
            if (finalResearch != null) {
                val nextDaysInvested = finalResearch.daysInvested + 1
                if (nextDaysInvested >= finalResearch.daysRequired) {
                    finalUnlockedTech = finalUnlockedTech + finalResearch.techId
                    val techName = TechUpgrade.entries.find { it.id == finalResearch.techId }?.title ?: "Technology"
                    newNotifications.add(0, "Research Complete: $techName is now active!")
                    finalResearch = null
                } else {
                    finalResearch = finalResearch.copy(daysInvested = nextDaysInvested)
                }
            }

            // Company level check
            newLevel = calculateCompanyLevel(finalCash, totalActiveUsers, state.officeTier)
            if (newLevel > state.companyLevel) {
                newNotifications.add(0, "Company Level Up! You reached Level $newLevel: ${getCompanyLevelTitle(newLevel)}!")
            }

            // Bankruptcy check
            if (finalCash < 0) {
                graceDays++
                if (graceDays >= 7) {
                    isBankrupt = true
                    newNotifications.add(0, "CRITICAL: The bank has foreclosed due to sustained debt. Company is bankrupt!")
                } else {
                    newNotifications.add(0, "WARNING: Cash is negative! You have ${7 - graceDays} days before bankruptcy.")
                }
            } else {
                graceDays = 0
            }

            // Random Events trigger check (~25% chance each new day if no event pending)
            if (newEvent == null && !isBankrupt && Random.nextFloat() < 0.25f) {
                newEvent = generateRandomEvent(state, totalActiveUsers)
            }
        }

        // Clean up employee assignments if their contract was resolved
        val activeContractIds = finalContracts.filter { it.status == ContractStatus.IN_PROGRESS }.map { it.id }.toSet()
        val cleanedEmployees = updatedEmployees.map { emp ->
            if (emp.assignedContractId != null && emp.assignedContractId !in activeContractIds) {
                emp.copy(assignedContractId = null)
            } else {
                emp
            }
        }

        // Limit notifications count
        if (newNotifications.size > 25) {
            newNotifications = newNotifications.take(25).toMutableList()
        }

        return state.copy(
            clock = newClock,
            cash = finalCash,
            reputation = finalReputation,
            companyLevel = newLevel,
            employees = cleanedEmployees,
            products = finalProducts,
            contracts = finalContracts,
            competitors = finalCompetitors,
            server = updatedServer,
            todayLedger = ledger,
            financialHistory = finalHistory,
            activeLoans = finalLoans,
            availableCandidates = finalCandidates,
            activeResearch = finalResearch,
            unlockedTechIds = finalUnlockedTech,
            notifications = newNotifications,
            pendingEvent = newEvent,
            bankruptcyGraceDays = graceDays,
            isBankrupt = isBankrupt
        )
    }

    private fun processEmployeeHour(
        emp: Employee,
        isWorkHour: Boolean,
        hasEspresso: Boolean,
        hasArcade: Boolean
    ): Employee {
        if (!isWorkHour) {
            val newEnergy = (emp.energy + 8).coerceAtMost(100)
            val newMorale = (emp.morale + (if (hasArcade) 4 else 2)).coerceAtMost(100)
            return emp.copy(
                energy = newEnergy,
                morale = newMorale,
                state = EmployeeState.RESTING
            )
        }

        if (emp.energy < 20) {
            val recoverBoost = if (hasEspresso) 18 else 10
            return emp.copy(
                energy = (emp.energy + recoverBoost).coerceAtMost(100),
                morale = (emp.morale + 3).coerceAtMost(100),
                state = EmployeeState.COFFEE_BREAK
            )
        }

        val energyCost = if (hasEspresso) 2 else 4
        val newEnergy = (emp.energy - energyCost).coerceAtLeast(5)
        val stateEnum = if (emp.assignedProductId != null || emp.assignedContractId != null) {
            EmployeeState.WORKING
        } else {
            EmployeeState.BRAINSTORMING
        }

        return emp.copy(
            energy = newEnergy,
            state = stateEnum
        )
    }

    private fun processProductsHour(
        products: List<Product>,
        employees: List<Employee>,
        isWorkHour: Boolean,
        devBonus: Float,
        designBonus: Float,
        bugReduction: Float,
        serverLoad: Float
    ): Triple<List<Product>, Double, Double> {
        var totalHourlyRevenue = 0.0
        var totalDailyMarketingBudget = 0.0

        val updatedProducts = products.map { prod ->
            totalDailyMarketingBudget += prod.dailyMarketingSpend

            when (prod.stage) {
                ProductStage.PLANNING -> {
                    if (!isWorkHour) return@map prod
                    val assignedStaff = employees.filter { it.assignedProductId == prod.id && it.state == EmployeeState.WORKING }
                    val sprintSpeed = prod.sprintMode.speedMultiplier

                    var addedPlanning = 0
                    for (staff in assignedStaff) {
                        val score = staff.effectiveProductivity
                        when (staff.role) {
                            EmployeeRole.DESIGNER -> addedPlanning += ((score * 4f) * (1f + designBonus) * sprintSpeed).toInt().coerceAtLeast(1)
                            EmployeeRole.PROJECT_MANAGER -> addedPlanning += ((score * 5f) * sprintSpeed).toInt().coerceAtLeast(1)
                            else -> {} // Developers cannot code during pure planning
                        }
                    }

                    val nextPlan = (prod.planningPointsCurrent + addedPlanning).coerceAtMost(prod.planningPointsNeeded)
                    val nextStage = if (nextPlan >= prod.planningPointsNeeded) ProductStage.DEVELOPMENT else ProductStage.PLANNING

                    prod.copy(
                        stage = nextStage,
                        planningPointsCurrent = nextPlan
                    )
                }

                ProductStage.DEVELOPMENT -> {
                    if (!isWorkHour) return@map prod
                    val assignedStaff = employees.filter { it.assignedProductId == prod.id && it.state == EmployeeState.WORKING }
                    val sprintSpeed = prod.sprintMode.speedMultiplier

                    var addedDesign = 0
                    var addedCode = 0
                    var newBugs = 0

                    for (staff in assignedStaff) {
                        val score = staff.effectiveProductivity
                        when (staff.role) {
                            EmployeeRole.DESIGNER -> {
                                val pts = ((score * 4f) * (1f + designBonus) * sprintSpeed).toInt().coerceAtLeast(1)
                                addedDesign += pts
                            }
                            EmployeeRole.DEVELOPER -> {
                                val pts = ((score * 5f) * (1f + devBonus) * sprintSpeed).toInt().coerceAtLeast(1)
                                addedCode += pts
                                if (Random.nextFloat() < (0.22f * (1f - bugReduction))) {
                                    newBugs += 1
                                }
                            }
                            EmployeeRole.PROJECT_MANAGER -> {
                                addedDesign += (1 * sprintSpeed).toInt()
                                addedCode += (2 * sprintSpeed).toInt()
                            }
                            else -> {}
                        }
                    }

                    val nextDesign = (prod.designPointsCurrent + addedDesign).coerceAtMost(prod.designPointsNeeded)
                    val nextCode = (prod.codePointsCurrent + addedCode).coerceAtMost(prod.codePointsNeeded)
                    val nextBugs = prod.bugsCount + newBugs

                    val isDevDone = nextDesign >= prod.designPointsNeeded && nextCode >= prod.codePointsNeeded
                    val nextStage = if (isDevDone) ProductStage.TESTING else ProductStage.DEVELOPMENT

                    prod.copy(
                        stage = nextStage,
                        designPointsCurrent = nextDesign,
                        codePointsCurrent = nextCode,
                        bugsCount = nextBugs
                    )
                }

                ProductStage.TESTING -> {
                    if (!isWorkHour) return@map prod
                    val assignedStaff = employees.filter { it.assignedProductId == prod.id && it.state == EmployeeState.WORKING }
                    val sprintSpeed = prod.sprintMode.speedMultiplier

                    var addedTest = 0
                    var squashedBugs = 0

                    for (staff in assignedStaff) {
                        val score = staff.effectiveProductivity
                        when (staff.role) {
                            EmployeeRole.QA_TESTER -> {
                                val pts = (score * 5f * sprintSpeed).toInt().coerceAtLeast(1)
                                addedTest += pts
                                if (prod.bugsCount > 0 && Random.nextFloat() < (score * 0.75f)) {
                                    squashedBugs += 1
                                }
                            }
                            EmployeeRole.DEVELOPER -> {
                                // Developers assist in squashing bugs during QA test phase
                                if (prod.bugsCount > 0 && Random.nextFloat() < (score * 0.40f)) {
                                    squashedBugs += 1
                                }
                            }
                            EmployeeRole.PROJECT_MANAGER -> {
                                addedTest += (1 * sprintSpeed).toInt()
                            }
                            else -> {}
                        }
                    }

                    val nextTest = (prod.testPointsCurrent + addedTest).coerceAtMost(prod.testPointsNeeded)
                    val nextBugs = (prod.bugsCount - squashedBugs).coerceAtLeast(0)

                    val isReady = nextTest >= prod.testPointsNeeded && nextBugs <= 3
                    val nextStage = if (isReady) ProductStage.READY_TO_LAUNCH else ProductStage.TESTING

                    prod.copy(
                        stage = nextStage,
                        testPointsCurrent = nextTest,
                        bugsCount = nextBugs,
                        bugsSquashed = prod.bugsSquashed + squashedBugs
                    )
                }

                ProductStage.READY_TO_LAUNCH -> prod

                ProductStage.LIVE -> {
                    val assignedStaff = employees.filter { it.assignedProductId == prod.id && it.state == EmployeeState.WORKING }
                    val qaStaff = assignedStaff.filter { it.role == EmployeeRole.QA_TESTER }
                    var squashedBugs = 0
                    for (qa in qaStaff) {
                        if (prod.bugsCount > 0 && Random.nextFloat() < (qa.effectiveProductivity * 0.5f)) {
                            squashedBugs++
                        }
                    }

                    val marketers = assignedStaff.filter { it.role == EmployeeRole.MARKETER }
                    val marketerBonusUsers = marketers.sumOf { (it.effectiveProductivity * 15f).toInt() }

                    var campaignUsersHourly = 0
                    val updatedCampaigns = prod.activeCampaigns.map { camp ->
                        val dailyRate = (camp.type.expectedUsersMin + (camp.type.expectedUsersMax - camp.type.expectedUsersMin) / 2) / camp.type.durationDays
                        val hourlyShare = (dailyRate / 24).coerceAtLeast(1)
                        campaignUsersHourly += hourlyShare
                        camp.copy(usersGenerated = camp.usersGenerated + hourlyShare)
                    }

                    val virality = prod.category.viralityFactor
                    val ratingScore = (prod.rating / 5.0f).coerceIn(0.2f, 1.0f)
                    val paidUsersHourly = ((prod.dailyMarketingSpend / 10.0) * virality / 24.0).toInt()
                    val organicGrowthHourly = (prod.activeUsers * 0.003f * virality * ratingScore).toInt().coerceAtLeast(1)

                    var churnMultiplier = 1.0f
                    if (serverLoad > 0.90f) churnMultiplier += 1.8f
                    if (serverLoad > 1.0f) churnMultiplier += 4.5f
                    val bugsChurn = (prod.bugsCount * 0.0015f) + (prod.techDebt * 0.001f)
                    val baseChurn = (prod.monetization.churnPenalty / 24f) * churnMultiplier + bugsChurn
                    val churnedUsers = (prod.activeUsers * baseChurn).toInt()

                    val netUserDelta = (organicGrowthHourly + paidUsersHourly + marketerBonusUsers + campaignUsersHourly) - churnedUsers
                    val nextUsers = (prod.activeUsers + netUserDelta).coerceAtLeast(10)

                    val satisfactionDelta = when {
                        serverLoad > 1.0f -> -0.003f
                        serverLoad > 0.90f -> -0.001f
                        prod.bugsCount == 0 && prod.techDebt == 0 && serverLoad < 0.70f -> +0.0005f
                        else -> 0f
                    }
                    val nextSatisfaction = (prod.userSatisfaction + satisfactionDelta).coerceIn(0.20f, 0.98f)

                    val revenueMultiplier = if (serverLoad > 1.05f) 0.5f else 1.0f
                    val hourlyRev = (nextUsers * (prod.monetization.revenuePerUser / 24.0) * revenueMultiplier)
                    totalHourlyRevenue += hourlyRev

                    prod.copy(
                        activeUsers = nextUsers,
                        dailyRevenue = prod.dailyRevenue + hourlyRev,
                        totalRevenue = prod.totalRevenue + hourlyRev,
                        userSatisfaction = nextSatisfaction,
                        bugsCount = (prod.bugsCount - squashedBugs).coerceAtLeast(0),
                        bugsSquashed = prod.bugsSquashed + squashedBugs,
                        activeCampaigns = updatedCampaigns
                    )
                }

                ProductStage.MAINTENANCE -> {
                    // Refactoring and overhaul phase
                    val assignedStaff = employees.filter { it.assignedProductId == prod.id && it.state == EmployeeState.WORKING }
                    var squashedBugs = 0
                    var reducedTechDebt = 0

                    if (isWorkHour) {
                        for (staff in assignedStaff) {
                            val score = staff.effectiveProductivity
                            if (staff.role == EmployeeRole.DEVELOPER || staff.role == EmployeeRole.QA_TESTER) {
                                if (prod.bugsCount > 0 && Random.nextFloat() < (score * 0.8f)) squashedBugs++
                                if (prod.techDebt > 0 && Random.nextFloat() < (score * 0.9f)) reducedTechDebt++
                            }
                        }
                    }

                    val nextBugs = (prod.bugsCount - squashedBugs).coerceAtLeast(0)
                    val nextDebt = (prod.techDebt - reducedTechDebt).coerceAtLeast(0)
                    val nextSatisfaction = (prod.userSatisfaction + 0.001f).coerceAtMost(0.98f)

                    // Retains 75% baseline revenue during scheduled maintenance
                    val hourlyRev = (prod.activeUsers * (prod.monetization.revenuePerUser / 24.0) * 0.75f)
                    totalHourlyRevenue += hourlyRev

                    val isCleaned = nextBugs == 0 && nextDebt == 0
                    val nextStage = if (isCleaned) ProductStage.LIVE else ProductStage.MAINTENANCE

                    prod.copy(
                        stage = nextStage,
                        bugsCount = nextBugs,
                        techDebt = nextDebt,
                        userSatisfaction = nextSatisfaction,
                        dailyRevenue = prod.dailyRevenue + hourlyRev,
                        totalRevenue = prod.totalRevenue + hourlyRev
                    )
                }
            }
        }

        return Triple(updatedProducts, totalHourlyRevenue, totalDailyMarketingBudget)
    }

    private fun processContractsHour(
        contracts: List<ClientContract>,
        employees: List<Employee>,
        isWorkHour: Boolean
    ): Pair<List<ClientContract>, Double> {
        var milestonePayouts = 0.0

        val updatedContracts = contracts.map { contract ->
            if (contract.status != ContractStatus.IN_PROGRESS || !isWorkHour) return@map contract

            val assignedStaff = employees.filter { it.assignedContractId == contract.id && it.state == EmployeeState.WORKING }
            var addedDev = 0
            var addedDesign = 0
            var addedQA = 0

            for (staff in assignedStaff) {
                val score = staff.effectiveProductivity
                when (staff.role) {
                    EmployeeRole.DEVELOPER -> addedDev += (score * 4.5f).toInt().coerceAtLeast(1)
                    EmployeeRole.DESIGNER -> addedDesign += (score * 4.0f).toInt().coerceAtLeast(1)
                    EmployeeRole.QA_TESTER -> addedQA += (score * 4.0f).toInt().coerceAtLeast(1)
                    EmployeeRole.PROJECT_MANAGER -> {
                        addedDev += 1
                        addedDesign += 1
                        addedQA += 1
                    }
                    else -> {}
                }
            }

            val nextDev = (contract.currentDevPoints + addedDev).coerceAtMost(contract.requiredDevPoints)
            val nextDesign = (contract.currentDesignPoints + addedDesign).coerceAtMost(contract.requiredDesignPoints)
            val nextQA = (contract.currentQAPoints + addedQA).coerceAtMost(contract.requiredQAPoints)

            val devRatio = if (contract.requiredDevPoints > 0) (nextDev.toFloat() / contract.requiredDevPoints).coerceIn(0f, 1f) else 1f
            val designRatio = if (contract.requiredDesignPoints > 0) (nextDesign.toFloat() / contract.requiredDesignPoints).coerceIn(0f, 1f) else 1f
            val qaRatio = if (contract.requiredQAPoints > 0) (nextQA.toFloat() / contract.requiredQAPoints).coerceIn(0f, 1f) else 1f
            val currentOverallProgress = (devRatio * 0.5f) + (designRatio * 0.25f) + (qaRatio * 0.25f)

            val updatedMilestones = contract.milestones.map { ms ->
                if (!ms.isCompleted && currentOverallProgress >= ms.targetPercent) {
                    milestonePayouts += ms.rewardPayout
                    ms.copy(isCompleted = true)
                } else {
                    ms
                }
            }

            contract.copy(
                currentDevPoints = nextDev,
                currentDesignPoints = nextDesign,
                currentQAPoints = nextQA,
                milestones = updatedMilestones
            )
        }

        return Pair(updatedContracts, milestonePayouts)
    }

    private fun simulateCompetitorsDay(
        competitors: List<CompetitorCompany>,
        playerTotalUsers: Int
    ): List<CompetitorCompany> {
        val updated = competitors.map { comp ->
            if (comp.isAcquired) return@map comp

            val growthMultiplier = when (comp.status) {
                "Dominant" -> Random.nextDouble(1.008, 1.025)
                "Growing" -> Random.nextDouble(1.015, 1.040)
                "Struggling" -> Random.nextDouble(0.985, 1.010)
                else -> 1.01
            }

            val nextUsers = (comp.activeUsers * growthMultiplier).toInt().coerceAtLeast(3000)
            val nextMonthlyRev = (nextUsers * 0.28).coerceAtLeast(2500.0)
            val nextValuation = (nextMonthlyRev * 12.0 * 3.4 + nextUsers * 7.5 + comp.cash).coerceAtLeast(25000.0)

            val nextStatus = when {
                growthMultiplier < 1.0 -> "Struggling"
                nextValuation > 420000.0 -> "Dominant"
                else -> "Growing"
            }

            val discount = if (nextStatus == "Struggling") 0.70 else 0.90
            val nextAcquisitionCost = nextValuation * discount

            comp.copy(
                activeUsers = nextUsers,
                monthlyRevenue = nextMonthlyRev,
                valuation = nextValuation,
                acquisitionCost = nextAcquisitionCost,
                status = nextStatus
            )
        }

        val totalMarketUsers = playerTotalUsers + updated.filter { !it.isAcquired }.sumOf { it.activeUsers }
        return updated.map { comp ->
            val share = if (totalMarketUsers > 0 && !comp.isAcquired) {
                ((comp.activeUsers.toFloat() / totalMarketUsers) * 100f).coerceIn(0.1f, 99f)
            } else 0f
            comp.copy(marketShare = share)
        }
    }

    private fun processServerHour(server: ServerInfrastructure, totalActiveUsers: Int): ServerInfrastructure {
        val load = server.calculateLoad(totalActiveUsers)
        val isOverloaded = load > 1.0f && !server.isCloudAutoScaleEnabled

        if (isOverloaded && !server.isOutageActive) {
            return server.copy(
                isOutageActive = true,
                outageHoursRemaining = Random.nextInt(2, 6),
                incidentReason = "Traffic overload! Server CPU throttled at ${(load * 100).toInt()}% load."
            )
        } else if (server.isOutageActive) {
            val remaining = server.outageHoursRemaining - 1
            if (remaining <= 0) {
                return server.copy(
                    isOutageActive = false,
                    outageHoursRemaining = 0,
                    incidentReason = null
                )
            } else {
                return server.copy(outageHoursRemaining = remaining)
            }
        }

        return server
    }

    private fun calculateDevSpeedBonus(state: GameState): Float {
        var bonus = 0f
        if (state.unlockedTechIds.contains("agile_sprints")) bonus += 0.20f
        if (state.unlockedTechIds.contains("ai_copilot")) bonus += 0.40f
        if (state.unlockedTechIds.contains("quantum_arch")) bonus += 0.75f
        val executiveDesks = state.furniture.count { it.type == FurnitureType.DESK_EXECUTIVE }
        val dualDesks = state.furniture.count { it.type == FurnitureType.DESK_DUAL_SCREEN }
        bonus += (executiveDesks * 0.08f) + (dualDesks * 0.04f)
        return bonus
    }

    private fun calculateDesignSpeedBonus(state: GameState): Float {
        var bonus = 0f
        if (state.unlockedTechIds.contains("design_system")) bonus += 0.35f
        val whiteboards = state.furniture.count { it.type == FurnitureType.WHITEBOARD }
        bonus += (whiteboards * 0.05f)
        return bonus
    }

    private fun calculateBugReduction(state: GameState): Float {
        var factor = 0f
        if (state.unlockedTechIds.contains("ci_cd")) factor += 0.50f
        return factor
    }

    private fun generateFreshCandidates(level: Int): List<Employee> {
        val names = listOf(
            "Elena Rostova", "Marcus Chen", "Chloe Bennett", "Liam Gallagher",
            "Aisha Patel", "Noah Thorne", "Sophia Rossi", "Lucas Meyer",
            "Kenji Sato", "Hannah Morales", "Zack Anderson", "Grace Hopper Jr."
        ).shuffled()

        val roles = EmployeeRole.entries.toTypedArray()
        val candidateList = mutableListOf<Employee>()

        for (i in 0 until 4) {
            val role = roles[Random.nextInt(roles.size)]
            val candidateLevel = Random.nextInt(1, (level + 1).coerceAtMost(4))
            val baseSkill = 40 + (candidateLevel * 12) + Random.nextInt(-5, 10)
            val baseSpeed = 40 + (candidateLevel * 10) + Random.nextInt(-5, 10)
            val salary = role.baseSalary * (1.0 + (candidateLevel - 1) * 0.35)

            val hairColors = listOf(0xFF1E293B, 0xFF4A3728, 0xFF78350F, 0xFF0F172A)
            val shirtColors = listOf(0xFF2563EB, 0xFF10B981, 0xFF8B5CF6, 0xFFEC4899, 0xFFF59E0B)
            val skinTones = listOf(0xFFFFDBAC, 0xFFE0AC69, 0xFFFCD34D, 0xFF8D5524)

            candidateList.add(
                Employee(
                    id = "cand_${System.currentTimeMillis()}_$i",
                    name = names.getOrElse(i) { "Talented Candidate" },
                    role = role,
                    level = candidateLevel,
                    skill = baseSkill.coerceIn(20, 99),
                    speed = baseSpeed.coerceIn(20, 99),
                    dailySalary = salary,
                    morale = 85 + Random.nextInt(-10, 10),
                    energy = 90 + Random.nextInt(-10, 10),
                    hairColor = hairColors.random(),
                    shirtColor = shirtColors.random(),
                    skinTone = skinTones.random()
                )
            )
        }
        return candidateList
    }

    private fun generateFreshContracts(reputation: Int): List<ClientContract> {
        val clientTemplates = listOf(
            Triple("OmniPay Microservice", "Fintech Corp", ProductCategory.SAAS_TOOL),
            Triple("Cloud Telemetry Suite", "Vanguard Aerospace", ProductCategory.SAAS_TOOL),
            Triple("Interactive Battlepass", "Nova Studios", ProductCategory.MOBILE_GAME),
            Triple("AI Customer Co-Pilot", "Beacon Intelligence", ProductCategory.AI_ASSISTANT),
            Triple("Logistics Dispatch UI", "Atlas Global Express", ProductCategory.SAAS_TOOL)
        ).shuffled()

        val item = clientTemplates.first()
        val tier = when {
            reputation >= 180 -> ContractTier.PLATINUM
            reputation >= 110 -> ContractTier.GOLD
            reputation >= 60 -> ContractTier.SILVER
            else -> ContractTier.BRONZE
        }

        val multiplier = when (tier) {
            ContractTier.BRONZE -> 1.0
            ContractTier.SILVER -> 1.8
            ContractTier.GOLD -> 3.2
            ContractTier.PLATINUM -> 5.5
        }

        val devReq = (70 * multiplier).toInt()
        val designReq = (40 * multiplier).toInt()
        val qaReq = (35 * multiplier).toInt()
        val upfront = (3000.0 * multiplier)
        val reward = (8500.0 * multiplier)
        val penalty = (2200.0 * multiplier)

        return listOf(
            ClientContract(
                id = "contract_${System.currentTimeMillis()}",
                title = item.first,
                clientName = item.second,
                category = item.third,
                tier = tier,
                requiredDevPoints = devReq,
                requiredDesignPoints = designReq,
                requiredQAPoints = qaReq,
                deadlineDays = (12 * (multiplier * 0.75)).toInt().coerceIn(10, 30),
                daysRemaining = (12 * (multiplier * 0.75)).toInt().coerceIn(10, 30),
                upfrontDeposit = upfront,
                completionReward = reward,
                penaltyCost = penalty,
                minReputation = (30 * multiplier).toInt(),
                reputationGain = (12 * multiplier).toInt(),
                milestones = listOf(
                    ContractMilestone(1, "Milestone 1: Architectural Draft", 0.33f, upfront * 0.4),
                    ContractMilestone(2, "Milestone 2: Core Implementation", 0.66f, upfront * 0.8),
                    ContractMilestone(3, "Milestone 3: Final Acceptance", 1.0f, reward)
                )
            )
        )
    }

    private fun generateRandomEvent(state: GameState, totalUsers: Int): GameEvent? {
        val eventType = Random.nextInt(6)
        return when (eventType) {
            0 -> {
                val investmentAmount = (state.valuation * 0.15).coerceIn(25000.0, 350000.0)
                GameEvent(
                    id = "event_vc_offer",
                    title = "Venture Capitalist Term Sheet",
                    description = "Venture firm 'Apex Horizons' is impressed by your growth and offers to invest $%,.0f for a 12%% equity stake in your company.".format(investmentAmount),
                    iconName = "AccountBalance",
                    options = listOf(
                        GameEventOption(
                            label = "Accept Term Sheet",
                            description = "+$%,.0f Cash, -12%% Equity".format(investmentAmount),
                            actionType = EventActionType.ACCEPT_INVESTMENT,
                            amount = investmentAmount
                        ),
                        GameEventOption(
                            label = "Reject Offer",
                            description = "Retain 100% founder control & equity.",
                            actionType = EventActionType.REJECT_INVESTMENT
                        )
                    )
                )
            }
            1 -> {
                val bestProduct = state.products.filter { it.stage == ProductStage.LIVE }.maxByOrNull { it.activeUsers }
                if (bestProduct != null) {
                    GameEvent(
                        id = "event_tech_viral",
                        title = "Featured on TechCrunch Frontpage!",
                        description = "A renowned tech journalist wrote a rave review about '${bestProduct.name}'. Over 15,000 enthusiastic users are signing up right now! Check your server capacity!",
                        iconName = "Whatshot",
                        options = listOf(
                            GameEventOption(
                                label = "Ride the Viral Wave",
                                description = "Welcome incoming traffic & celebrate.",
                                actionType = EventActionType.DISMISS
                            )
                        ),
                        relatedEntityId = bestProduct.id
                    )
                } else null
            }
            2 -> {
                GameEvent(
                    id = "event_server_heat",
                    title = "Server Room Thermal Warning",
                    description = "Summer temperatures have spiked and the server rack cooling fans are failing. Will you hire emergency liquid cooling contractors for $850, or risk intermittent server crashes?",
                    iconName = "Warning",
                    options = listOf(
                        GameEventOption(
                            label = "Pay $850 for Emergency Cooling",
                            description = "Guarantees 100% server uptime.",
                            actionType = EventActionType.PAY_FIX,
                            amount = 850.0
                        ),
                        GameEventOption(
                            label = "Risk It (Open Windows)",
                            description = "Save cash, but 60% risk of server downtime.",
                            actionType = EventActionType.RISK_DOWNTIME
                        )
                    )
                )
            }
            3 -> {
                val bestEmp = state.employees.maxByOrNull { it.skill }
                if (bestEmp != null && bestEmp.id != "emp_founder") {
                    val salaryIncrease = bestEmp.dailySalary * 0.25
                    GameEvent(
                        id = "event_poached_dev",
                        title = "Headhunter Alert!",
                        description = "A competing Big Tech giant just offered ${bestEmp.name} a massive signing bonus. To keep them on your team, you must match a 25% salary increase (+$${salaryIncrease.toInt()}/day).",
                        iconName = "PersonSearch",
                        options = listOf(
                            GameEventOption(
                                label = "Match Salary (+$$salaryIncrease/day)",
                                description = "${bestEmp.name} stays and gains +15% morale.",
                                actionType = EventActionType.MATCH_SALARY,
                                amount = salaryIncrease
                            ),
                            GameEventOption(
                                label = "Wish Them Well",
                                description = "Let ${bestEmp.name} leave your company.",
                                actionType = EventActionType.LET_EMPLOYEE_GO
                            )
                        ),
                        relatedEntityId = bestEmp.id
                    )
                } else null
            }
            4 -> {
                GameEvent(
                    id = "event_enterprise_deal",
                    title = "Fortune 500 Enterprise Deal",
                    description = "A multinational retail conglomerate wants to license your enterprise software suite. They offer $12,500 upfront license fee plus dedicated support expectations.",
                    iconName = "Handshake",
                    options = listOf(
                        GameEventOption(
                            label = "Sign Enterprise Contract",
                            description = "+$12,500 upfront cash revenue.",
                            actionType = EventActionType.SIGN_ENTERPRISE_DEAL,
                            amount = 12500.0
                        ),
                        GameEventOption(
                            label = "Decline (Focus on Core Product)",
                            description = "Keep roadmap distraction-free.",
                            actionType = EventActionType.DISMISS
                        )
                    )
                )
            }
            else -> {
                GameEvent(
                    id = "event_ddos_attack",
                    title = "Suspicious Cyber Attack Detected",
                    description = "Bots are flooding your API endpoints. Activate Cloudflare Edge Anti-DDoS protection for $400 or let your DevOps sysadmins mitigate manually?",
                    iconName = "Security",
                    options = listOf(
                        GameEventOption(
                            label = "Activate Anti-DDoS ($400)",
                            description = "Instantly clears malicious traffic.",
                            actionType = EventActionType.BUY_FIREWALL,
                            amount = 400.0
                        ),
                        GameEventOption(
                            label = "Let DevOps Handle It",
                            description = "Sysadmins attempt mitigation.",
                            actionType = EventActionType.DISMISS
                        )
                    )
                )
            }
        }
    }

    private fun calculateCompanyLevel(cash: Double, totalUsers: Int, officeTier: OfficeTier): Int {
        return when {
            totalUsers >= 250000 || officeTier == OfficeTier.CYBER_TOWER -> 6
            totalUsers >= 80000 || officeTier == OfficeTier.CAMPUS_HQ -> 5
            totalUsers >= 25000 || officeTier == OfficeTier.TECH_LOFT -> 4
            totalUsers >= 5000 || officeTier == OfficeTier.COWORKING -> 3
            totalUsers >= 800 || cash >= 40000.0 -> 2
            else -> 1
        }
    }

    fun getCompanyLevelTitle(level: Int): String = when (level) {
        1 -> "Garage Hacker"
        2 -> "Seed Startup"
        3 -> "Series A Growth"
        4 -> "Tech Scale-Up"
        5 -> "Unicorn Leader"
        else -> "Global Tech Empire"
    }
}
