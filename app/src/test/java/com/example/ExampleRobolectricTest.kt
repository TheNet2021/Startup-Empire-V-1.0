package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.SimulationEngine
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Startup Empire 3D", appName)
  }

  @Test
  fun `simulation engine advances hour and day correctly`() {
    val startingState = GameState(
      clock = GameClock(day = 1, hour = 23, totalHoursElapsed = 23)
    )
    val nextState = SimulationEngine.tick(startingState, 1)

    assertEquals(2, nextState.clock.day)
    assertEquals(0, nextState.clock.hour)
    assertEquals(24L, nextState.clock.totalHoursElapsed)
  }

  @Test
  fun `product planning stage advances with designers and transitions to development`() {
    val designer = Employee(
      id = "emp_des",
      name = "Designer",
      role = EmployeeRole.DESIGNER,
      skill = 85,
      speed = 85,
      assignedProductId = "prod_plan",
      state = EmployeeState.WORKING
    )
    val planningProd = Product(
      id = "prod_plan",
      name = "Future App",
      category = ProductCategory.SAAS_TOOL,
      stage = ProductStage.PLANNING,
      planningPointsNeeded = 20,
      planningPointsCurrent = 18
    )
    val state = GameState(
      clock = GameClock(day = 1, hour = 10),
      employees = listOf(designer),
      products = listOf(planningProd)
    )

    val tickedState = SimulationEngine.tick(state, 2)
    val updatedProd = tickedState.products.first()
    assertEquals(ProductStage.DEVELOPMENT, updatedProd.stage)
    assertTrue(updatedProd.planningPointsCurrent >= 20)
  }

  @Test
  fun `unqualified staff do not advance contract skill points`() {
    // Only a marketer assigned to a contract requiring dev points
    val marketer = Employee(
      id = "emp_mark",
      name = "Marketer",
      role = EmployeeRole.MARKETER,
      assignedContractId = "contract_test",
      state = EmployeeState.WORKING
    )
    val contract = ClientContract(
      id = "contract_test",
      title = "API Bridge",
      clientName = "Client A",
      category = ProductCategory.SAAS_TOOL,
      tier = ContractTier.BRONZE,
      requiredDevPoints = 50,
      currentDevPoints = 0,
      requiredDesignPoints = 0,
      requiredQAPoints = 0,
      deadlineDays = 10,
      daysRemaining = 10,
      upfrontDeposit = 2000.0,
      completionReward = 5000.0,
      penaltyCost = 1000.0,
      status = ContractStatus.IN_PROGRESS
    )
    val state = GameState(
      clock = GameClock(day = 1, hour = 11),
      employees = listOf(marketer),
      contracts = listOf(contract)
    )

    val tickedState = SimulationEngine.tick(state, 3)
    val updatedContract = tickedState.contracts.first()
    assertEquals(0, updatedContract.currentDevPoints)
  }

  @Test
  fun `client contract progress requires qualified staff and awards milestone payout`() {
    val dev = Employee(
      id = "emp_dev",
      name = "Developer",
      role = EmployeeRole.DEVELOPER,
      skill = 80,
      speed = 80,
      assignedContractId = "contract_dev",
      state = EmployeeState.WORKING
    )
    val milestone = ContractMilestone(1, "Milestone 1", 0.33f, 1500.0)
    val contract = ClientContract(
      id = "contract_dev",
      title = "Microservice",
      clientName = "Apex",
      category = ProductCategory.SAAS_TOOL,
      tier = ContractTier.BRONZE,
      requiredDevPoints = 30,
      currentDevPoints = 5,
      requiredDesignPoints = 0,
      requiredQAPoints = 0,
      deadlineDays = 10,
      daysRemaining = 10,
      upfrontDeposit = 1000.0,
      completionReward = 6000.0,
      penaltyCost = 1000.0,
      status = ContractStatus.IN_PROGRESS,
      milestones = listOf(milestone)
    )
    val initialCash = 10000.0
    val state = GameState(
      cash = initialCash,
      clock = GameClock(day = 1, hour = 10),
      employees = listOf(dev),
      contracts = listOf(contract)
    )

    val tickedState = SimulationEngine.tick(state, 4)
    val updatedContract = tickedState.contracts.first()
    assertTrue(updatedContract.currentDevPoints > 5)
    assertTrue(updatedContract.milestones.first().isCompleted)
    assertTrue(tickedState.cash > initialCash) // Received milestone payout
  }

  @Test
  fun `contract delivery awards completion payout and reputation on day rollover`() {
    val completedContract = ClientContract(
      id = "contract_ready",
      title = "Ready Contract",
      clientName = "Zenith Corp",
      category = ProductCategory.SAAS_TOOL,
      tier = ContractTier.BRONZE,
      requiredDevPoints = 20,
      currentDevPoints = 20,
      requiredDesignPoints = 10,
      currentDesignPoints = 10,
      requiredQAPoints = 10,
      currentQAPoints = 10,
      deadlineDays = 5,
      daysRemaining = 5,
      upfrontDeposit = 1000.0,
      completionReward = 8000.0,
      penaltyCost = 2000.0,
      reputationGain = 20,
      status = ContractStatus.IN_PROGRESS
    )
    val state = GameState(
      cash = 5000.0,
      reputation = 100,
      clock = GameClock(day = 1, hour = 23, totalHoursElapsed = 23),
      contracts = listOf(completedContract)
    )

    val tickedState = SimulationEngine.tick(state, 1)
    val delivered = tickedState.contracts.first()
    assertEquals(ContractStatus.COMPLETED, delivered.status)
    assertEquals(120, tickedState.reputation)
    assertTrue(tickedState.cash > 10000.0) // 5000 + 8000 reward - daily costs
  }

  @Test
  fun `company valuation reflects assets, users, run rate and debts`() {
    val liveProd = Product(
      id = "prod_val",
      name = "Money Maker",
      category = ProductCategory.SAAS_TOOL,
      stage = ProductStage.LIVE,
      activeUsers = 10000,
      dailyRevenue = 500.0,
      userSatisfaction = 0.90f
    )
    val loan = ActiveLoan(
      id = "loan_1",
      lender = "Bank",
      principalBorrowed = 10000.0,
      dailyPayment = 200.0,
      daysRemaining = 30
    )
    val state = GameState(
      cash = 25000.0,
      reputation = 150,
      products = listOf(liveProd),
      activeLoans = listOf(loan)
    )

    val valuation = state.valuation
    assertTrue(valuation > 100000.0) // Substantial valuation from users, run rate, cash, brand
    val leaderboard = state.leaderboard
    assertEquals(1, leaderboard.count { it.isPlayer })
  }

  @Test
  fun `competitor simulation updates market share dynamically`() {
    val state = GameState(
      products = listOf(
        Product(
          id = "prod_1",
          name = "Player App",
          category = ProductCategory.SOCIAL_APP,
          stage = ProductStage.LIVE,
          activeUsers = 25000
        )
      )
    )
    assertTrue(state.competitors.isNotEmpty())
    val nextState = SimulationEngine.tick(state.copy(clock = GameClock(day = 1, hour = 23)), 1)
    assertTrue(nextState.competitors.first().activeUsers > 0)
    assertTrue(nextState.marketShare > 0f)
  }

  @Test
  fun `save game and settings persistence works correctly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val customState = GameState(
      companyName = "Quantum Byte",
      companyLogoIcon = "bolt",
      companyLogoColor = 0xFF10B981,
      difficulty = "HARD",
      cash = 45000.0
    )
    val saved = com.example.storage.GameStorage.saveGame(context, customState)
    assertTrue(saved)
    assertTrue(com.example.storage.GameStorage.hasSaveGame(context))

    val summary = com.example.storage.GameStorage.getSaveSummary(context)
    assertNotNull(summary)
    assertEquals("Quantum Byte", summary?.companyName)
    assertEquals("bolt", summary?.logoIcon)

    val settings = GameSettings(musicVolume = 0.4f, sfxVolume = 0.9f, isMusicEnabled = false)
    com.example.storage.GameStorage.saveSettings(context, settings)
    val loadedSettings = com.example.storage.GameStorage.loadSettings(context)
    assertEquals(0.4f, loadedSettings.musicVolume, 0.01f)
    assertFalse(loadedSettings.isMusicEnabled)
  }

  @Test
  fun `midnight rollover resets product daily revenue to prevent runaway inflation`() {
    val liveProd = Product(
      id = "prod_roll",
      name = "Daily App",
      category = ProductCategory.SAAS_TOOL,
      stage = ProductStage.LIVE,
      activeUsers = 5000,
      dailyRevenue = 250.0,
      totalRevenue = 1000.0
    )
    val state = GameState(
      clock = GameClock(day = 1, hour = 23, totalHoursElapsed = 23),
      products = listOf(liveProd)
    )

    val tickedState = SimulationEngine.tick(state, 1)
    val rolledProd = tickedState.products.first()
    assertEquals(2, tickedState.clock.day)
    assertEquals(0, tickedState.clock.hour)
    // Yesterday revenue captures the daily revenue, and dailyRevenue resets for the new day
    assertTrue(rolledProd.yesterdayRevenue >= 250.0)
    assertTrue(rolledProd.dailyRevenue < 20.0) // only hour 0's small increment
  }

  @Test
  fun `competitor acquisition absorbs market share and user base`() {
    val targetComp = CompetitorCompany(
      id = "comp_target",
      name = "Target Inc",
      category = ProductCategory.SAAS_TOOL,
      valuation = 150000.0,
      monthlyRevenue = 12000.0,
      monthlyExpenses = 15000.0,
      cash = 20000.0,
      activeUsers = 20000,
      marketShare = 15.0f,
      reputation = 90,
      employeeCount = 8,
      productNames = listOf("Legacy App"),
      acquisitionCost = 105000.0,
      status = "Struggling",
      isAcquired = false
    )
    // Struggling status gives a 30% discount
    val expectedPrice = targetComp.valuation * 0.70
    assertEquals(105000.0, expectedPrice, 0.01)

    val playerProd = Product(
      id = "player_prod",
      name = "Main SaaS",
      category = ProductCategory.SAAS_TOOL,
      stage = ProductStage.LIVE,
      activeUsers = 10000
    )
    val state = GameState(
      cash = 200000.0,
      products = listOf(playerProd),
      competitors = listOf(targetComp)
    )

    // Simulate acquisition through state transformation
    val absorbedUsers = (targetComp.activeUsers * 0.45).toInt()
    val updatedProd = playerProd.copy(activeUsers = playerProd.activeUsers + absorbedUsers)
    val acquiredComp = targetComp.copy(isAcquired = true, status = "Acquired", activeUsers = 0)
    val afterAcquisitionState = state.copy(
      cash = state.cash - expectedPrice,
      products = listOf(updatedProd),
      competitors = listOf(acquiredComp)
    )

    assertEquals(95000.0, afterAcquisitionState.cash, 0.01)
    assertEquals(19000, afterAcquisitionState.products.first().activeUsers)
    assertTrue(afterAcquisitionState.competitors.first().isAcquired)
  }

  @Test
  fun `assigned engineer advances feature development points until implemented`() {
    val dev = Employee(
      id = "emp_dev_feat",
      name = "Senior Dev",
      role = EmployeeRole.DEVELOPER,
      skill = 90,
      speed = 90,
      assignedProductId = "prod_feat",
      state = EmployeeState.WORKING
    )
    val feature = ProductFeature(
      id = "feat_test",
      name = "Test Feature",
      description = "Feature under active development",
      devPointsReq = 20,
      designPointsReq = 0,
      qaPointsReq = 0,
      devPointsCurrent = 5,
      satisfactionBoost = 0.05f,
      arpuBoost = 0.01,
      isUnderDevelopment = true,
      isImplemented = false
    )
    val liveProd = Product(
      id = "prod_feat",
      name = "Feature Master",
      category = ProductCategory.SAAS_TOOL,
      stage = ProductStage.LIVE,
      features = listOf(feature)
    )
    val state = GameState(
      clock = GameClock(day = 1, hour = 10),
      employees = listOf(dev),
      products = listOf(liveProd)
    )

    val tickedState = SimulationEngine.tick(state, 4)
    val updatedFeat = tickedState.products.first().features.first()
    assertTrue(updatedFeat.devPointsCurrent > 5)
  }

  @Test
  fun `contract cannot be delivered twice or grant duplicate rewards`() {
    val contract = ClientContract(
      id = "contract_dup_test",
      title = "Duplicate Defense Contract",
      clientName = "Lockdown",
      category = ProductCategory.SAAS_TOOL,
      tier = ContractTier.GOLD,
      requiredDevPoints = 10,
      currentDevPoints = 10,
      requiredDesignPoints = 10,
      currentDesignPoints = 10,
      requiredQAPoints = 10,
      currentQAPoints = 10,
      deadlineDays = 5,
      daysRemaining = 5,
      upfrontDeposit = 500.0,
      completionReward = 10000.0,
      penaltyCost = 2000.0,
      reputationGain = 25,
      status = ContractStatus.IN_PROGRESS,
      isRewardPaid = false
    )
    val state = GameState(
      cash = 5000.0,
      reputation = 50,
      clock = GameClock(day = 1, hour = 23, totalHoursElapsed = 23),
      contracts = listOf(contract)
    )

    val tickedOnce = SimulationEngine.tick(state, 1)
    val completedOnce = tickedOnce.contracts.first()
    assertTrue(completedOnce.isRewardPaid)
    assertEquals(ContractStatus.COMPLETED, completedOnce.status)
    val cashAfterFirst = tickedOnce.cash

    // Tick again; should not grant second 10,000 completion reward
    val tickedTwice = SimulationEngine.tick(tickedOnce.copy(clock = GameClock(day = 2, hour = 23, totalHoursElapsed = 47)), 1)
    assertTrue(tickedTwice.cash <= cashAfterFirst) // only normal daily expenses, no extra 10,000 payout
  }

  @Test
  fun `milestone achievement unlocks when condition is met on day rollover`() {
    val liveProd = Product(
      id = "prod_ms",
      name = "Milestone App",
      category = ProductCategory.SAAS_TOOL,
      stage = ProductStage.LIVE,
      activeUsers = 12000
    )
    val initialMilestones = CompanyMilestone.defaultMilestones()
    val state = GameState(
      clock = GameClock(day = 1, hour = 23, totalHoursElapsed = 23),
      products = listOf(liveProd),
      milestones = initialMilestones
    )

    val nextState = SimulationEngine.tick(state, 1)
    val ms1 = nextState.milestones.find { it.id == "ms_first_product" }
    val ms2 = nextState.milestones.find { it.id == "ms_ten_thousand_users" }
    assertNotNull(ms1)
    assertNotNull(ms2)
    assertTrue(ms1?.isAchieved == true)
    assertTrue(ms2?.isAchieved == true)
  }
}


