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

        // 1. Calculate bonuses from tech tree and office furniture
        val devBonus = calculateDevSpeedBonus(state)
        val designBonus = calculateDesignSpeedBonus(state)
        val bugReduction = calculateBugReduction(state)
        val hasEspresso = state.furniture.any { it.type == FurnitureType.ESPRESSO_MACHINE }
        val hasArcade = state.furniture.any { it.type == FurnitureType.ARCADE_CABINET }

        // 2. Process Employees and Work Output
        val isWorkHour = oldClock.hour in 9..18 // 9:00 AM to 6:59 PM (10 hours workday)
        val updatedEmployees = state.employees.map { emp ->
            processEmployeeHour(emp, isWorkHour, hasEspresso, hasArcade)
        }

        // 3. Process Product Development and Live Operations
        val sysadminCount = updatedEmployees.count { it.role == EmployeeRole.SYSADMIN && it.state == EmployeeState.WORKING }
        val (updatedProducts, hourlyProductRevenue, hourlyMarketingSpend) = processProductsHour(
            products = state.products,
            employees = updatedEmployees,
            isWorkHour = isWorkHour,
            devBonus = devBonus,
            designBonus = designBonus,
            bugReduction = bugReduction,
            serverLoad = state.serverLoad,
            isOutage = state.server.isOutageActive
        )

        // 4. Process Client Contracts Progress, Milestones & Auto-Deliveries
        val (updatedContracts, milestonePayouts, contractRepGain, contractCompletedNotes) = processContractsHour(
            contracts = state.contracts,
            employees = updatedEmployees,
            isWorkHour = isWorkHour
        )

        // 5. Server health & Outages
        val totalActiveUsers = updatedProducts.filter { it.stage == ProductStage.LIVE }.sumOf { it.activeUsers }
        val serverTechBonus = calculateServerCapacityBonus(state)
        val serverWithTech = state.server.copy(techCapacityBonus = serverTechBonus)
        val updatedServer = processServerHour(serverWithTech, totalActiveUsers, sysadminCount)

        // 6. Financial tracking for current hour
        val currentCash = state.cash + hourlyProductRevenue + milestonePayouts - (hourlyMarketingSpend / 24.0)

        var ledger = state.todayLedger.copy(
            productRevenue = state.todayLedger.productRevenue + hourlyProductRevenue,
            contractRevenue = state.todayLedger.contractRevenue + milestonePayouts,
            marketingExpenses = state.todayLedger.marketingExpenses + (hourlyMarketingSpend / 24.0)
        )

        var finalCash = currentCash
        var finalReputation = (state.reputation + contractRepGain).coerceIn(10, 500)
        var finalHistory = state.financialHistory
        var finalLoans = state.activeLoans
        var finalCandidates = state.availableCandidates
        var finalResearch = state.activeResearch
        var finalUnlockedTech = state.unlockedTechIds
        var finalProducts = updatedProducts
        var finalContracts = updatedContracts
        var finalCompetitors = state.competitors
        var finalMilestones = state.milestones
        var newNotifications = state.notifications.toMutableList()
        var newEvent = state.pendingEvent
        var graceDays = state.bankruptcyGraceDays
        var isBankrupt = state.isBankrupt
        var newLevel = state.companyLevel

        contractCompletedNotes.forEach { note ->
            newNotifications.add(0, note)
        }

        // 7. Day rollover actions (Midnight)
        if (isNewDay) {
            // Deduct Daily Payroll
            val dailyPayroll = updatedEmployees.sumOf { it.dailySalary }
            // Deduct Daily Rent
            val dailyRent = state.officeTier.monthlyRent / 30.0
            // Deduct Daily Server Hosting
            val dailyServerCost = updatedServer.calculateDailyCost(totalActiveUsers)
            // Loan Repayments
            var loanDeductions = 0.0
            finalLoans = state.activeLoans.mapNotNull { loan ->
                val nextDays = loan.daysRemaining - 1
                loanDeductions += loan.dailyPayment
                if (nextDays <= 0) null else loan.copy(daysRemaining = nextDays)
            }

            val dailyOperatingProfit = (ledger.productRevenue + ledger.contractRevenue) - (dailyPayroll + dailyRent + dailyServerCost + loanDeductions)
            val corporateTax = if (dailyOperatingProfit > 2000.0) (dailyOperatingProfit * 0.12).coerceAtLeast(0.0) else 0.0

            finalCash -= (dailyPayroll + dailyRent + dailyServerCost + loanDeductions + corporateTax)

            ledger = ledger.copy(
                payrollExpenses = dailyPayroll,
                rentExpenses = dailyRent,
                serverExpenses = dailyServerCost,
                loanRepaymentExpenses = loanDeductions,
                taxExpenses = corporateTax
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

            // Reset each product's dailyRevenue, saving it to yesterdayRevenue
            finalProducts = finalProducts.map { prod ->
                prod.copy(
                    yesterdayRevenue = prod.dailyRevenue,
                    dailyRevenue = 0.0
                )
            }

            // Evaluate In-Progress Contracts Deadlines
            val processedContracts = mutableListOf<ClientContract>()
            val expiredFailedContractIds = mutableSetOf<String>()

            for (contract in finalContracts) {
                if (contract.status == ContractStatus.IN_PROGRESS) {
                    val daysLeft = contract.daysRemaining - 1
                    if (daysLeft <= 0 && !contract.isDeliverable) {
                        // Failed!
                        finalCash -= contract.penaltyCost
                        finalReputation = (finalReputation - 20).coerceAtLeast(10)
                        newNotifications.add(
                            0,
                            "Contract Failed: Deadline expired on '${contract.title}'. Client penalty assessed: -$${contract.penaltyCost.toInt()}!"
                        )
                        expiredFailedContractIds.add(contract.id)
                        processedContracts.add(contract.copy(status = ContractStatus.FAILED, daysRemaining = 0))
                    } else if (contract.isDeliverable && !contract.isDelivered) {
                        // Deliver fully completed contract on day rollover
                        val payout = if (contract.isRewardPaid) 0.0 else contract.completionReward
                        finalCash += payout
                        finalReputation = (finalReputation + contract.reputationGain).coerceAtMost(500)
                        newNotifications.add(
                            0,
                            "✓ CONTRACT DELIVERED: Successfully completed '${contract.title}' for ${contract.clientName}! Payout: +$${payout.toInt()}, +${contract.reputationGain} Rep!"
                        )
                        processedContracts.add(
                            contract.copy(
                                status = ContractStatus.COMPLETED,
                                isDelivered = true,
                                isRewardPaid = true,
                                milestones = contract.milestones.map { it.copy(isCompleted = true) }
                            )
                        )
                    } else {
                        processedContracts.add(contract.copy(daysRemaining = daysLeft.coerceAtLeast(0)))
                    }
                } else {
                    processedContracts.add(contract)
                }
            }

            finalContracts = processedContracts

            // Refresh available client contracts if pool is low
            val availableCount = finalContracts.count { it.status == ContractStatus.AVAILABLE }
            if (availableCount < 3 && nextDay % 3 == 0) {
                finalContracts = finalContracts + generateFreshContracts(finalReputation)
                newNotifications.add(0, "New enterprise client contracts posted on the bidding board!")
            }

            // Simulate AI Competitor Companies with realistic market competition
            val playerLiveCategories = finalProducts.filter { it.stage == ProductStage.LIVE }.map { it.category }.toSet()
            finalCompetitors = simulateCompetitorsDay(finalCompetitors, totalActiveUsers, playerLiveCategories)

            // Active marketing campaigns day decrement
            finalProducts = finalProducts.map { prod ->
                val nextCampaigns = prod.activeCampaigns.mapNotNull { camp ->
                    val rem = camp.daysRemaining - 1
                    if (rem <= 0) {
                        newNotifications.add(0, "Campaign Complete: '${camp.type.title}' brought +${camp.usersGenerated} active users to '${prod.name}'!")
                        null
                    } else {
                        camp.copy(daysRemaining = rem)
                    }
                }

                // Tech debt accumulation on live products every 3 days if not in maintenance
                val nextTechDebt = if (prod.stage == ProductStage.LIVE && nextDay % 3 == 0 && prod.activeUsers > 1500) {
                    (prod.techDebt + 1).coerceAtMost(35)
                } else {
                    prod.techDebt
                }

                // Generate authentic customer reviews based on actual stability and performance
                val currentReviews = prod.customerReviews.toMutableList()
                if (prod.stage == ProductStage.LIVE && nextDay % 2 == 0) {
                    val review = when {
                        prod.bugsCount > 8 -> CustomerReview(
                            id = "rev_${nextDay}_${Random.nextInt(1000)}",
                            author = listOf("CodeTester_99", "SarahDev", "MarcusT", "ElenaR").random(),
                            rating = 1,
                            comment = "Frustrating experience. Lots of glitches and bugs in v${prod.version}.",
                            daySubmitted = nextDay
                        )
                        updatedServer.isOutageActive || updatedServer.calculateLoad(totalActiveUsers) > 1.0f -> CustomerReview(
                            id = "rev_${nextDay}_${Random.nextInt(1000)}",
                            author = listOf("CloudUser", "DevOpsDan", "AlexK", "Sammy").random(),
                            rating = 2,
                            comment = "Slow loading times and occasional downtime. Please upgrade infrastructure!",
                            daySubmitted = nextDay
                        )
                        prod.rating >= 4.2f -> CustomerReview(
                            id = "rev_${nextDay}_${Random.nextInt(1000)}",
                            author = listOf("FounderJack", "TechLead_Mia", "PowerUser_Z", "DavidHQ").random(),
                            rating = 5,
                            comment = "Incredible tool! Our entire team relies on it daily. Smooth and fast.",
                            daySubmitted = nextDay
                        )
                        else -> CustomerReview(
                            id = "rev_${nextDay}_${Random.nextInt(1000)}",
                            author = listOf("User404", "JessicaM", "Brian_K", "LisaT").random(),
                            rating = 3,
                            comment = "Good core idea, but could use more features and UI polish.",
                            daySubmitted = nextDay
                        )
                    }
                    currentReviews.add(0, review)
                }

                // Feature progression: if a feature is ready to deploy
                val updatedFeatures = prod.features.map { feat ->
                    if (feat.isReadyToDeploy && feat.isUnderDevelopment && !feat.isImplemented) {
                        newNotifications.add(0, "✓ Feature Deployed: '${feat.name}' on '${prod.name}' is now active (+${(feat.satisfactionBoost * 100).toInt()}% satisfaction, +$${feat.arpuBoost} ARPU)!")
                        feat.copy(isImplemented = true, isUnderDevelopment = false)
                    } else feat
                }

                prod.copy(
                    activeCampaigns = nextCampaigns,
                    techDebt = nextTechDebt,
                    customerReviews = currentReviews.take(6),
                    features = updatedFeatures
                )
            }

            // Evaluate company long-term milestones
            finalMilestones = finalMilestones.map { ms ->
                if (ms.isAchieved) ms else {
                    val achieved = when (ms.id) {
                        "ms_first_product" -> finalProducts.any { it.stage == ProductStage.LIVE }
                        "ms_ten_thousand_users" -> totalActiveUsers >= 10000
                        "ms_deliver_contract" -> finalContracts.any { it.status == ContractStatus.COMPLETED }
                        "ms_office_loft" -> state.officeTier.ordinal >= OfficeTier.TECH_LOFT.ordinal
                        "ms_research_innovator" -> finalUnlockedTech.size >= 4
                        "ms_market_leader" -> state.marketShare >= 25.0f
                        "ms_unicorn_status" -> state.valuation >= 1000000.0
                        "ms_strategic_takeover" -> finalCompetitors.any { it.isAcquired }
                        else -> false
                    }
                    if (achieved) {
                        newNotifications.add(0, "🏆 MILESTONE UNLOCKED: '${ms.title}' achieved! Claim bonus reward in Tech & Milestones.")
                        ms.copy(isAchieved = true)
                    } else ms
                }
            }

            // Refresh candidate recruitment pool every 3 days
            if (nextDay % 3 == 0) {
                finalCandidates = generateFreshCandidates(state.companyLevel)
                newNotifications.add(0, "New qualified candidates submitted applications to your hiring board!")
            }

            // Research progression
            if (finalResearch != null) {
                val nextDaysInvested = finalResearch.daysInvested + 1
                if (nextDaysInvested >= finalResearch.daysRequired) {
                    finalUnlockedTech = finalUnlockedTech + finalResearch.techId
                    val techName = TechUpgrade.entries.find { it.id == finalResearch.techId }?.title ?: "Technology"
                    newNotifications.add(0, "R&D Complete: $techName is now unlocked and active!")
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

            // Solvency and Bankruptcy check
            if (finalCash < 0) {
                graceDays++
                if (graceDays >= 7) {
                    isBankrupt = true
                    newNotifications.add(0, "CRITICAL: The bankruptcy court has foreclosed on all operations due to sustained insolvency.")
                } else {
                    newNotifications.add(0, "WARNING: Cash is negative ($%,.0f)! You have ${7 - graceDays} days to restore positive cash flow or face foreclosure.".format(finalCash))
                }
            } else {
                graceDays = 0
            }

            // Random Events trigger check (~25% chance each new day if no event pending)
            if (newEvent == null && !isBankrupt && Random.nextFloat() < 0.25f) {
                newEvent = generateRandomEvent(state, totalActiveUsers)
            }
        }

        // Clean up employee assignments if their contract was resolved (completed or failed)
        val activeContractIds = finalContracts.filter { it.status == ContractStatus.IN_PROGRESS }.map { it.id }.toSet()
        val cleanedEmployees = updatedEmployees.map { emp ->
            if (emp.assignedContractId != null && emp.assignedContractId !in activeContractIds) {
                emp.copy(assignedContractId = null)
            } else {
                emp
            }
        }

        // Limit notifications count to latest 30
        if (newNotifications.size > 30) {
            newNotifications = newNotifications.take(30).toMutableList()
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
            milestones = finalMilestones,
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

        // Work hours (9 AM to 7 PM)
        if (emp.energy < 15) {
            val recoverBoost = if (hasEspresso) 18 else 12
            return emp.copy(
                energy = (emp.energy + recoverBoost).coerceAtMost(100),
                morale = (emp.morale + 3).coerceAtMost(100),
                state = EmployeeState.COFFEE_BREAK
            )
        }

        val energyCost = if (hasEspresso) 2 else 4
        val newEnergy = (emp.energy - energyCost).coerceAtLeast(5)
        val isAssigned = emp.assignedProductId != null || emp.assignedContractId != null
        val stateEnum = if (isAssigned) EmployeeState.WORKING else EmployeeState.BRAINSTORMING

        return emp.copy(
            energy = newEnergy,
            state = stateEnum,
            totalWorkOutput = emp.totalWorkOutput + (if (isAssigned) 1 else 0)
        )
    }

    private fun processProductsHour(
        products: List<Product>,
        employees: List<Employee>,
        isWorkHour: Boolean,
        devBonus: Float,
        designBonus: Float,
        bugReduction: Float,
        serverLoad: Float,
        isOutage: Boolean = false
    ): Triple<List<Product>, Double, Double> {
        var totalHourlyRevenue = 0.0
        var totalDailyMarketingBudget = 0.0

        val updatedProducts = products.map { prod ->
            totalDailyMarketingBudget += prod.dailyMarketingSpend

            when (prod.stage) {
                ProductStage.PLANNING -> {
                    if (!isWorkHour) return@map prod
                    val assignedStaff = employees.filter { it.assignedProductId == prod.id && it.state == EmployeeState.WORKING }
                    if (assignedStaff.isEmpty()) return@map prod

                    val sprintSpeed = prod.sprintMode.speedMultiplier
                    var addedPlanning = 0

                    for (staff in assignedStaff) {
                        val score = staff.effectiveProductivity
                        when (staff.role) {
                            EmployeeRole.DESIGNER -> addedPlanning += ((score * 4.5f) * (1f + designBonus) * sprintSpeed).toInt().coerceAtLeast(1)
                            EmployeeRole.PROJECT_MANAGER -> addedPlanning += ((score * 5.0f) * sprintSpeed).toInt().coerceAtLeast(1)
                            // Developer can assist at 40% speed if no designer is assigned, preventing blockage
                            EmployeeRole.DEVELOPER -> addedPlanning += ((score * 1.8f) * sprintSpeed).toInt().coerceAtLeast(1)
                            else -> {}
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
                    if (assignedStaff.isEmpty()) return@map prod

                    val sprintSpeed = prod.sprintMode.speedMultiplier
                    var addedDesign = 0
                    var addedCode = 0
                    var newBugs = 0

                    val hasDev = assignedStaff.any { it.role == EmployeeRole.DEVELOPER || it.role == EmployeeRole.PROJECT_MANAGER }
                    val hasDesigner = assignedStaff.any { it.role == EmployeeRole.DESIGNER || it.role == EmployeeRole.PROJECT_MANAGER }

                    for (staff in assignedStaff) {
                        val score = staff.effectiveProductivity
                        when (staff.role) {
                            EmployeeRole.DESIGNER -> {
                                val pts = ((score * 4.5f) * (1f + designBonus) * sprintSpeed).toInt().coerceAtLeast(1)
                                addedDesign += pts
                            }
                            EmployeeRole.DEVELOPER -> {
                                val pts = ((score * 5.0f) * (1f + devBonus) * sprintSpeed).toInt().coerceAtLeast(1)
                                addedCode += pts
                                // Chance to introduce bugs: high skill reduces bugs
                                val bugChance = (0.20f * (1f - bugReduction) * (1.1f - (staff.skill / 150f))).coerceIn(0.04f, 0.25f)
                                if (Random.nextFloat() < bugChance) {
                                    newBugs += 1
                                }
                            }
                            EmployeeRole.PROJECT_MANAGER -> {
                                addedDesign += (2 * sprintSpeed).toInt()
                                addedCode += (3 * sprintSpeed).toInt()
                            }
                            else -> {}
                        }
                    }

                    // If no designer assigned, let developer assist design at 30% speed so solo founder isn't completely stuck
                    if (!hasDesigner && prod.designPointsCurrent < prod.designPointsNeeded) {
                        val devStaff = assignedStaff.filter { it.role == EmployeeRole.DEVELOPER }
                        addedDesign += devStaff.sumOf { (it.effectiveProductivity * 1.5f).toInt().coerceAtLeast(1) }
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
                    if (assignedStaff.isEmpty()) return@map prod

                    val sprintSpeed = prod.sprintMode.speedMultiplier
                    var addedTest = 0
                    var squashedBugs = 0

                    for (staff in assignedStaff) {
                        val score = staff.effectiveProductivity
                        when (staff.role) {
                            EmployeeRole.QA_TESTER -> {
                                val pts = (score * 5.5f * sprintSpeed).toInt().coerceAtLeast(1)
                                addedTest += pts
                                if (prod.bugsCount > 0 && Random.nextFloat() < (score * 0.80f)) {
                                    squashedBugs += 1
                                }
                            }
                            EmployeeRole.DEVELOPER -> {
                                // Developers assist in QA and squashing bugs
                                val pts = (score * 2.2f * sprintSpeed).toInt().coerceAtLeast(1)
                                addedTest += pts
                                if (prod.bugsCount > 0 && Random.nextFloat() < (score * 0.55f)) {
                                    squashedBugs += 1
                                }
                            }
                            EmployeeRole.PROJECT_MANAGER -> {
                                addedTest += (2 * sprintSpeed).toInt()
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
                        if (prod.bugsCount > 0 && Random.nextFloat() < (qa.effectiveProductivity * 0.50f)) {
                            squashedBugs++
                        }
                    }

                    val marketers = assignedStaff.filter { it.role == EmployeeRole.MARKETER }
                    val marketerBonusUsers = marketers.sumOf { (it.effectiveProductivity * 18f).toInt() }

                    var campaignUsersHourly = 0
                    val updatedCampaigns = prod.activeCampaigns.map { camp ->
                        val dailyRate = (camp.type.expectedUsersMin + (camp.type.expectedUsersMax - camp.type.expectedUsersMin) / 2) / camp.type.durationDays
                        val hourlyShare = (dailyRate / 24).coerceAtLeast(1)
                        campaignUsersHourly += hourlyShare
                        camp.copy(usersGenerated = camp.usersGenerated + hourlyShare)
                    }

                    val virality = prod.category.viralityFactor
                    val ratingScore = (prod.rating / 5.0f).coerceIn(0.2f, 1.0f)
                    val demandFactor = prod.marketResearch?.demandScore ?: 1.0f
                    val paidUsersHourly = ((prod.dailyMarketingSpend / 12.0) * virality * demandFactor / 24.0).toInt()
                    val organicGrowthHourly = (prod.activeUsers * 0.0028f * virality * ratingScore * demandFactor).toInt().coerceAtLeast(1)

                    var churnMultiplier = 1.0f
                    if (isOutage) churnMultiplier += 3.5f
                    if (serverLoad > 0.90f) churnMultiplier += 1.8f
                    if (serverLoad > 1.0f) churnMultiplier += 4.5f
                    val bugsChurn = (prod.bugsCount * 0.0012f) + (prod.techDebt * 0.0008f)
                    val baseChurn = (prod.monetization.churnPenalty / 24f) * churnMultiplier + bugsChurn
                    val churnedUsers = (prod.activeUsers * baseChurn).toInt()

                    val netUserDelta = (organicGrowthHourly + paidUsersHourly + marketerBonusUsers + campaignUsersHourly) - churnedUsers
                    val nextUsers = (prod.activeUsers + netUserDelta).coerceAtLeast(15)

                    val satisfactionDelta = when {
                        isOutage -> -0.005f
                        serverLoad > 1.0f -> -0.003f
                        serverLoad > 0.90f -> -0.001f
                        prod.bugsCount == 0 && prod.techDebt == 0 && serverLoad < 0.70f -> +0.0005f
                        else -> 0f
                    }
                    val nextSatisfaction = (prod.userSatisfaction + satisfactionDelta).coerceIn(0.20f, 0.98f)

                    val revenueMultiplier = when {
                        isOutage -> 0.10f
                        serverLoad > 1.05f -> 0.5f
                        else -> 1.0f
                    }
                    val hourlyRev = (nextUsers * (prod.effectiveARPU / 24.0) * revenueMultiplier)
                    totalHourlyRevenue += hourlyRev

                    val activeFeature = prod.features.find { it.isUnderDevelopment && !it.isImplemented }
                    val featuresAfterWork = if (activeFeature != null && isWorkHour && assignedStaff.isNotEmpty()) {
                        var addDev = 0
                        var addDesign = 0
                        var addQA = 0
                        for (staff in assignedStaff) {
                            val score = staff.effectiveProductivity
                            when (staff.role) {
                                EmployeeRole.DESIGNER -> addDesign += (score * 4.0f * (1f + designBonus)).toInt().coerceAtLeast(1)
                                EmployeeRole.DEVELOPER -> {
                                    addDev += (score * 4.5f * (1f + devBonus)).toInt().coerceAtLeast(1)
                                    if (activeFeature.designPointsCurrent < activeFeature.designPointsReq) {
                                        addDesign += (score * 1.5f).toInt().coerceAtLeast(1)
                                    }
                                    if (activeFeature.qaPointsCurrent < activeFeature.qaPointsReq) {
                                        addQA += (score * 1.5f).toInt().coerceAtLeast(1)
                                    }
                                }
                                EmployeeRole.QA_TESTER -> addQA += (score * 4.5f).toInt().coerceAtLeast(1)
                                EmployeeRole.PROJECT_MANAGER -> {
                                    addDev += 1
                                    addDesign += 1
                                    addQA += 1
                                }
                                else -> {}
                            }
                        }
                        prod.features.map { feat ->
                            if (feat.id == activeFeature.id) {
                                val nd = (feat.devPointsCurrent + addDev).coerceAtMost(feat.devPointsReq)
                                val ng = (feat.designPointsCurrent + addDesign).coerceAtMost(feat.designPointsReq)
                                val nq = (feat.qaPointsCurrent + addQA).coerceAtMost(feat.qaPointsReq)
                                val ready = nd >= feat.devPointsReq && ng >= feat.designPointsReq && nq >= feat.qaPointsReq
                                feat.copy(
                                    devPointsCurrent = nd,
                                    designPointsCurrent = ng,
                                    qaPointsCurrent = nq,
                                    isImplemented = ready,
                                    isUnderDevelopment = !ready
                                )
                            } else feat
                        }
                    } else prod.features

                    prod.copy(
                        activeUsers = nextUsers,
                        dailyRevenue = prod.dailyRevenue + hourlyRev,
                        totalRevenue = prod.totalRevenue + hourlyRev,
                        userSatisfaction = nextSatisfaction,
                        bugsCount = (prod.bugsCount - squashedBugs).coerceAtLeast(0),
                        bugsSquashed = prod.bugsSquashed + squashedBugs,
                        activeCampaigns = updatedCampaigns,
                        features = featuresAfterWork
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
                                if (prod.bugsCount > 0 && Random.nextFloat() < (score * 0.85f)) squashedBugs++
                                if (prod.techDebt > 0 && Random.nextFloat() < (score * 0.90f)) reducedTechDebt++
                            }
                        }
                    }

                    val nextBugs = (prod.bugsCount - squashedBugs).coerceAtLeast(0)
                    val nextDebt = (prod.techDebt - reducedTechDebt).coerceAtLeast(0)
                    val nextSatisfaction = (prod.userSatisfaction + 0.0015f).coerceAtMost(0.98f)

                    // Retains 80% baseline revenue during maintenance
                    val hourlyRev = (prod.activeUsers * (prod.monetization.revenuePerUser / 24.0) * 0.80f)
                    totalHourlyRevenue += hourlyRev

                    val isCleaned = nextBugs == 0 && nextDebt == 0
                    val nextStage = if (isCleaned) ProductStage.LIVE else ProductStage.MAINTENANCE

                    val activeFeature = prod.features.find { it.isUnderDevelopment && !it.isImplemented }
                    val featuresAfterWork = if (activeFeature != null && isWorkHour && assignedStaff.isNotEmpty()) {
                        var addDev = 0
                        var addDesign = 0
                        var addQA = 0
                        for (staff in assignedStaff) {
                            val score = staff.effectiveProductivity
                            when (staff.role) {
                                EmployeeRole.DESIGNER -> addDesign += (score * 3.5f).toInt().coerceAtLeast(1)
                                EmployeeRole.DEVELOPER -> addDev += (score * 4.0f).toInt().coerceAtLeast(1)
                                EmployeeRole.QA_TESTER -> addQA += (score * 4.0f).toInt().coerceAtLeast(1)
                                else -> {}
                            }
                        }
                        prod.features.map { feat ->
                            if (feat.id == activeFeature.id) {
                                val nd = (feat.devPointsCurrent + addDev).coerceAtMost(feat.devPointsReq)
                                val ng = (feat.designPointsCurrent + addDesign).coerceAtMost(feat.designPointsReq)
                                val nq = (feat.qaPointsCurrent + addQA).coerceAtMost(feat.qaPointsReq)
                                val ready = nd >= feat.devPointsReq && ng >= feat.designPointsReq && nq >= feat.qaPointsReq
                                feat.copy(
                                    devPointsCurrent = nd,
                                    designPointsCurrent = ng,
                                    qaPointsCurrent = nq,
                                    isImplemented = ready,
                                    isUnderDevelopment = !ready
                                )
                            } else feat
                        }
                    } else prod.features

                    prod.copy(
                        stage = nextStage,
                        bugsCount = nextBugs,
                        techDebt = nextDebt,
                        userSatisfaction = nextSatisfaction,
                        dailyRevenue = prod.dailyRevenue + hourlyRev,
                        totalRevenue = prod.totalRevenue + hourlyRev,
                        features = featuresAfterWork
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
    ): Tuple4<List<ClientContract>, Double, Int, List<String>> {
        var milestonePayouts = 0.0
        var totalRepGain = 0
        val notifications = mutableListOf<String>()

        val updatedContracts = contracts.map { contract ->
            if (contract.status != ContractStatus.IN_PROGRESS || !isWorkHour) return@map contract

            val assignedStaff = employees.filter { it.assignedContractId == contract.id && it.state == EmployeeState.WORKING }
            if (assignedStaff.isEmpty()) return@map contract // No progress without assigned staff!

            var addedDev = 0
            var addedDesign = 0
            var addedQA = 0

            val hasDev = assignedStaff.any { it.role == EmployeeRole.DEVELOPER }
            val hasDesigner = assignedStaff.any { it.role == EmployeeRole.DESIGNER }
            val hasQA = assignedStaff.any { it.role == EmployeeRole.QA_TESTER }

            for (staff in assignedStaff) {
                val score = staff.effectiveProductivity
                when (staff.role) {
                    EmployeeRole.DEVELOPER -> addedDev += (score * 4.5f).toInt().coerceAtLeast(1)
                    EmployeeRole.DESIGNER -> addedDesign += (score * 4.2f).toInt().coerceAtLeast(1)
                    EmployeeRole.QA_TESTER -> addedQA += (score * 4.5f).toInt().coerceAtLeast(1)
                    EmployeeRole.PROJECT_MANAGER -> {
                        addedDev += 1
                        addedDesign += 1
                        addedQA += 1
                    }
                    else -> {}
                }
            }

            // Cross-functional assistance if team is small:
            // Developer assists QA if no QA is assigned (at 35% speed)
            if (!hasQA && contract.currentQAPoints < contract.requiredQAPoints) {
                val devStaff = assignedStaff.filter { it.role == EmployeeRole.DEVELOPER }
                addedQA += devStaff.sumOf { (it.effectiveProductivity * 1.5f).toInt().coerceAtLeast(1) }
            }
            // Developer assists Design if no Designer is assigned (at 30% speed)
            if (!hasDesigner && contract.currentDesignPoints < contract.requiredDesignPoints) {
                val devStaff = assignedStaff.filter { it.role == EmployeeRole.DEVELOPER }
                addedDesign += devStaff.sumOf { (it.effectiveProductivity * 1.2f).toInt().coerceAtLeast(1) }
            }

            val salesStaff = assignedStaff.filter { it.role == EmployeeRole.SALES }
            val salesMultiplier = 1.0 + salesStaff.sumOf { (it.effectiveProductivity * 0.20).toDouble() }

            val nextDev = (contract.currentDevPoints + addedDev).coerceAtMost(contract.requiredDevPoints)
            val nextDesign = (contract.currentDesignPoints + addedDesign).coerceAtMost(contract.requiredDesignPoints)
            val nextQA = (contract.currentQAPoints + addedQA).coerceAtMost(contract.requiredQAPoints)

            val devRatio = if (contract.requiredDevPoints > 0) (nextDev.toFloat() / contract.requiredDevPoints).coerceIn(0f, 1f) else 1f
            val designRatio = if (contract.requiredDesignPoints > 0) (nextDesign.toFloat() / contract.requiredDesignPoints).coerceIn(0f, 1f) else 1f
            val qaRatio = if (contract.requiredQAPoints > 0) (nextQA.toFloat() / contract.requiredQAPoints).coerceIn(0f, 1f) else 1f
            val currentOverallProgress = (devRatio * 0.5f) + (designRatio * 0.25f) + (qaRatio * 0.25f)

            var isContractFinished = false
            val updatedMilestones = contract.milestones.map { ms ->
                if (!ms.isCompleted && currentOverallProgress >= ms.targetPercent) {
                    val reward = ms.rewardPayout * salesMultiplier
                    milestonePayouts += reward
                    if (ms.id == 3 || ms.targetPercent >= 1.0f) {
                        isContractFinished = true
                    } else {
                        notifications.add("Contract Milestone '${ms.title}' reached on '${contract.title}'! +$${reward.toInt()}")
                    }
                    ms.copy(isCompleted = true)
                } else {
                    ms
                }
            }

            val isFullyDone = isContractFinished || (nextDev >= contract.requiredDevPoints && nextDesign >= contract.requiredDesignPoints && nextQA >= contract.requiredQAPoints)
            if (isFullyDone) {
                val repGain = (contract.reputationGain * (if (salesStaff.isNotEmpty()) 1.25f else 1.0f)).toInt()
                val isFinalMilestonePaid = updatedMilestones.any { it.targetPercent >= 1.0f && it.isCompleted }
                val finalPayout = if (contract.isRewardPaid || isFinalMilestonePaid) 0.0 else (contract.completionReward * salesMultiplier)
                if (finalPayout > 0) {
                    milestonePayouts += finalPayout
                }
                totalRepGain += repGain
                val totalDeliveryAmount = (if (isFinalMilestonePaid) updatedMilestones.find { it.targetPercent >= 1.0f }?.rewardPayout ?: 0.0 else finalPayout).toInt()
                notifications.add("✓ CONTRACT COMPLETE: Delivered '${contract.title}' to ${contract.clientName}! Payout: +$${totalDeliveryAmount}, +${repGain} Rep!")
                contract.copy(
                    status = ContractStatus.COMPLETED,
                    isDelivered = true,
                    isRewardPaid = true,
                    currentDevPoints = nextDev,
                    currentDesignPoints = nextDesign,
                    currentQAPoints = nextQA,
                    milestones = updatedMilestones
                )
            } else {
                contract.copy(
                    currentDevPoints = nextDev,
                    currentDesignPoints = nextDesign,
                    currentQAPoints = nextQA,
                    milestones = updatedMilestones
                )
            }
        }

        return Tuple4(updatedContracts, milestonePayouts, totalRepGain, notifications)
    }

    private fun simulateCompetitorsDay(
        competitors: List<CompetitorCompany>,
        playerTotalUsers: Int,
        playerLiveCategories: Set<ProductCategory>
    ): List<CompetitorCompany> {
        val updated = competitors.map { comp ->
            if (comp.isAcquired) return@map comp

            // If player has a live product in the same category, market competition intensifies!
            val isDirectlyCompeting = playerLiveCategories.contains(comp.category)
            val competitionPenalty = if (isDirectlyCompeting) Random.nextDouble(0.965, 0.995) else 1.0

            val baseGrowth = when (comp.status) {
                "Dominant" -> Random.nextDouble(1.006, 1.018)
                "Growing" -> Random.nextDouble(1.012, 1.030)
                "Struggling" -> Random.nextDouble(0.975, 1.005)
                else -> 1.008
            }

            val finalGrowth = baseGrowth * competitionPenalty
            val nextUsers = (comp.activeUsers * finalGrowth).toInt().coerceAtLeast(1500)
            val nextMonthlyRev = (nextUsers * 0.28).coerceAtLeast(2000.0)
            val nextExpenses = (comp.employeeCount * 130.0 * 30.0) + (nextMonthlyRev * 0.35)
            val nextCash = (comp.cash + (nextMonthlyRev - nextExpenses) / 30.0).coerceAtLeast(1000.0)

            // Valuation uses consistent formula
            val nextValuation = (nextCash + nextMonthlyRev * 12.0 * 3.8 + nextUsers * 12.0 + (comp.reputation * 300.0)).coerceAtLeast(20000.0)

            val nextStatus = when {
                finalGrowth < 0.99 || nextMonthlyRev < nextExpenses -> "Struggling"
                nextValuation > 450000.0 -> "Dominant"
                else -> "Growing"
            }

            val discount = when (nextStatus) {
                "Struggling" -> 0.70 // 30% distressed buyout discount
                "Growing" -> 0.92
                else -> 1.05
            }
            val nextAcquisitionCost = nextValuation * discount

            val activity = when {
                isDirectlyCompeting && finalGrowth < 1.0 -> "Losing market share to your competing product in ${comp.category.title}"
                nextStatus == "Struggling" -> "Cash flow burning; board receptive to strategic buyout"
                nextStatus == "Dominant" -> "Expanded enterprise enterprise server infrastructure"
                else -> "Active marketing and user acquisition sprint"
            }

            comp.copy(
                activeUsers = nextUsers,
                monthlyRevenue = nextMonthlyRev,
                monthlyExpenses = nextExpenses,
                cash = nextCash,
                valuation = nextValuation,
                acquisitionCost = nextAcquisitionCost,
                status = nextStatus,
                latestActivity = activity
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

    private fun processServerHour(
        server: ServerInfrastructure,
        totalActiveUsers: Int,
        sysadminCount: Int = 0
    ): ServerInfrastructure {
        val load = server.calculateLoad(totalActiveUsers)
        val threshold = if (sysadminCount > 0) 1.15f else 1.0f
        val isOverloaded = load > threshold && !server.isCloudAutoScaleEnabled

        if (isOverloaded && !server.isOutageActive) {
            val duration = if (sysadminCount > 0) Random.nextInt(1, 3) else Random.nextInt(2, 5)
            return server.copy(
                isOutageActive = true,
                outageHoursRemaining = duration,
                incidentReason = "Traffic overload! Server CPU throttled at ${(load * 100).toInt()}% load."
            )
        } else if (server.isOutageActive) {
            val recoveryStep = if (sysadminCount > 0) 2 else 1
            val remaining = (server.outageHoursRemaining - recoveryStep).coerceAtLeast(0)
            if (remaining <= 0) {
                return server.copy(
                    isOutageActive = false,
                    outageHoursRemaining = 0,
                    incidentReason = null,
                    uptimePercent = ((server.uptimePercent * 0.98f) + 2.0f).coerceIn(95.0f, 99.99f)
                )
            } else {
                return server.copy(
                    outageHoursRemaining = remaining,
                    uptimePercent = (server.uptimePercent - 0.05f).coerceAtLeast(90.0f)
                )
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
        if (state.unlockedTechIds.contains("enterprise_security")) factor += 0.30f
        return factor
    }

    private fun calculateServerCapacityBonus(state: GameState): Float {
        var bonus = 0f
        if (state.unlockedTechIds.contains("container_k8s")) bonus += 0.35f
        if (state.unlockedTechIds.contains("global_edge_cdn")) bonus += 0.50f
        if (state.unlockedTechIds.contains("multi_region_cloud")) bonus += 0.70f
        return bonus
    }

    private fun generateFreshCandidates(level: Int): List<Employee> {
        val names = listOf(
            "Elena Rostova", "Marcus Chen", "Chloe Bennett", "Liam Gallagher",
            "Aisha Patel", "Noah Thorne", "Sophia Rossi", "Lucas Meyer",
            "Kenji Sato", "Hannah Morales", "Zack Anderson", "Grace Hopper Jr.",
            "Vikram Singh", "Nadia Al-Mansoor", "Oliver Wright", "Zoe Castiglione"
        ).shuffled()

        val roles = EmployeeRole.entries.toTypedArray()
        val candidateList = mutableListOf<Employee>()

        for (i in 0 until 4) {
            val role = roles[Random.nextInt(roles.size)]
            val candidateLevel = Random.nextInt(1, (level + 1).coerceAtMost(4))
            val baseSkill = 45 + (candidateLevel * 12) + Random.nextInt(-5, 8)
            val baseSpeed = 45 + (candidateLevel * 10) + Random.nextInt(-5, 8)
            val salary = role.baseSalary * (1.0 + (candidateLevel - 1) * 0.35)

            val hairColors = listOf(0xFF1E293B, 0xFF4A3728, 0xFF78350F, 0xFF0F172A)
            val shirtColors = listOf(0xFF2563EB, 0xFF10B981, 0xFF8B5CF6, 0xFFEC4899, 0xFFF59E0B)
            val skinTones = listOf(0xFFFFDBAC, 0xFFE0AC69, 0xFFFCD34D, 0xFF8D5524)

            candidateList.add(
                Employee(
                    id = "cand_${System.currentTimeMillis()}_$i",
                    name = names.getOrElse(i) { "Talented Specialist" },
                    role = role,
                    level = candidateLevel,
                    skill = baseSkill.coerceIn(25, 99),
                    speed = baseSpeed.coerceIn(25, 99),
                    dailySalary = salary,
                    morale = 85 + Random.nextInt(-5, 10),
                    energy = 90 + Random.nextInt(-5, 10),
                    hairColor = hairColors.random(),
                    shirtColor = shirtColors.random(),
                    skinTone = skinTones.random()
                )
            )
        }
        return candidateList
    }

    fun generateFreshContracts(reputation: Int): List<ClientContract> {
        val clientTemplates = listOf(
            Triple("OmniPay Microservice", "Fintech Corp", ProductCategory.SAAS_TOOL),
            Triple("Cloud Telemetry Suite", "Vanguard Aerospace", ProductCategory.SAAS_TOOL),
            Triple("Interactive Battlepass", "Nova Studios", ProductCategory.MOBILE_GAME),
            Triple("AI Customer Co-Pilot", "Beacon Intelligence", ProductCategory.AI_ASSISTANT),
            Triple("Logistics Dispatch UI", "Atlas Global Express", ProductCategory.SAAS_TOOL),
            Triple("Social Commerce Gateway", "Horizon Media", ProductCategory.ECOMMERCE),
            Triple("High-Frequency Order Router", "Apex Capital", ProductCategory.SAAS_TOOL)
        ).shuffled()

        val item = clientTemplates.first()
        val tier = when {
            reputation >= 195 -> ContractTier.PLATINUM
            reputation >= 135 -> ContractTier.GOLD
            reputation >= 85 -> ContractTier.SILVER
            else -> ContractTier.BRONZE
        }

        val multiplier = when (tier) {
            ContractTier.BRONZE -> 1.0
            ContractTier.SILVER -> 1.7
            ContractTier.GOLD -> 2.9
            ContractTier.PLATINUM -> 4.8
        }

        val devReq = (65 * multiplier).toInt()
        val designReq = (35 * multiplier).toInt()
        val qaReq = (30 * multiplier).toInt()
        val upfront = (3200.0 * multiplier)
        val m1 = (2000.0 * multiplier)
        val m2 = (3500.0 * multiplier)
        val finalReward = (5500.0 * multiplier)
        val penalty = (2500.0 * multiplier)
        val deadline = (12 * (multiplier * 0.75)).toInt().coerceIn(10, 28)

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
                deadlineDays = deadline,
                daysRemaining = deadline,
                upfrontDeposit = upfront,
                completionReward = finalReward,
                penaltyCost = penalty,
                minReputation = tier.minReputation,
                reputationGain = (15 * multiplier).toInt().coerceIn(15, 50),
                milestones = listOf(
                    ContractMilestone(1, "Milestone 1: Architectural Draft & UI Specs", 0.33f, m1),
                    ContractMilestone(2, "Milestone 2: Core Implementation & API", 0.66f, m2),
                    ContractMilestone(3, "Milestone 3: Acceptance Testing & Delivery", 1.0f, finalReward)
                )
            )
        )
    }

    private fun generateRandomEvent(state: GameState, totalUsers: Int): GameEvent? {
        val eventType = Random.nextInt(7)
        return when (eventType) {
            0 -> {
                val investmentAmount = (state.valuation * 0.12).coerceIn(20000.0, 300000.0)
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
                        description = "A renowned tech journalist wrote a rave review about '${bestProduct.name}'. Over 12,000 enthusiastic users are signing up right now! Check your server capacity!",
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
            5 -> {
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
            else -> {
                GameEvent(
                    id = "event_security_audit",
                    title = "Critical Security CVE Disclosed",
                    description = "An upstream open-source dependency has a remote code execution vulnerability. Deploy rapid emergency hotfix for $1,200 or patch internally with delayed sprint velocity?",
                    iconName = "Security",
                    options = listOf(
                        GameEventOption(
                            label = "Pay $1,200 Rapid Hotfix",
                            description = "Deploys patch immediately without downtime or bugs.",
                            actionType = EventActionType.PAY_FIX,
                            amount = 1200.0
                        ),
                        GameEventOption(
                            label = "Patch with In-House Staff",
                            description = "Saves cash but incurs technical debt.",
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
            totalUsers >= 800 || cash >= 35000.0 -> 2
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

data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
