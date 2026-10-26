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
}
