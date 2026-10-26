package com.example

import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `starting furniture has zero overlap and all workstations have at least 1 tile walking gap`() {
    val startingFurniture = GameState.defaultStartingFurniture()
    val occupied = startingFurniture.map { it.gridX to it.gridY }
    
    // Check no duplicate tiles
    assertEquals("Every furniture item must have a unique coordinate", occupied.size, occupied.toSet().size)

    val workstations = startingFurniture.filter { it.type.isWorkstation }
    for (i in workstations.indices) {
      for (j in i + 1 until workstations.size) {
        val w1 = workstations[i]
        val w2 = workstations[j]
        val dx = kotlin.math.abs(w1.gridX - w2.gridX)
        val dy = kotlin.math.abs(w1.gridY - w2.gridY)
        assertTrue("Workstations must have at least 1 empty tile between them (dx >= 2 or dy >= 2)", dx >= 2 || dy >= 2)
      }
    }
  }

  @Test
  fun `office layout manager rejects placing workstation too close to another desk`() {
    val existing = listOf(
      PlacedFurniture(id = "desk_0", type = FurnitureType.DESK_STANDARD, gridX = 1, gridY = 3, rotation = 0)
    )

    // (1, 2) is adjacent (dx = 0, dy = 1) -> must be rejected
    val resultAdjacent = OfficeLayoutManager.validatePlacement(
      type = FurnitureType.DESK_DUAL_SCREEN,
      targetX = 1,
      targetY = 2,
      movingFurnitureId = null,
      existingFurniture = existing,
      tier = OfficeTier.GARAGE
    )
    assertTrue("Adjacent workstation must be rejected", resultAdjacent is OfficeLayoutManager.PlacementValidation.Invalid)

    // (2, 3) is adjacent (dx = 1, dy = 0) -> must be rejected
    val resultAdjacentX = OfficeLayoutManager.validatePlacement(
      type = FurnitureType.DESK_DUAL_SCREEN,
      targetX = 2,
      targetY = 3,
      movingFurnitureId = null,
      existingFurniture = existing,
      tier = OfficeTier.GARAGE
    )
    assertTrue("Adjacent workstation on X must be rejected", resultAdjacentX is OfficeLayoutManager.PlacementValidation.Invalid)

    // (3, 1) has dx = 2, dy = 2 -> must be valid!
    val resultSpaced = OfficeLayoutManager.validatePlacement(
      type = FurnitureType.DESK_DUAL_SCREEN,
      targetX = 3,
      targetY = 1,
      movingFurnitureId = null,
      existingFurniture = existing,
      tier = OfficeTier.GARAGE
    )
    assertTrue("Spaced workstation must be accepted", resultSpaced is OfficeLayoutManager.PlacementValidation.Valid)
  }

  @Test
  fun `workstation capacity limit is strictly enforced per office tier`() {
    val garageDesks = listOf(
      PlacedFurniture(id = "d1", type = FurnitureType.DESK_STANDARD, gridX = 1, gridY = 3, rotation = 0),
      PlacedFurniture(id = "d2", type = FurnitureType.DESK_STANDARD, gridX = 3, gridY = 1, rotation = 0),
      PlacedFurniture(id = "d3", type = FurnitureType.DESK_STANDARD, gridX = 4, gridY = 4, rotation = 0)
    )

    // Garage maxDesks is 3. Attempting to place a 4th desk must be rejected
    val resultExceed = OfficeLayoutManager.validatePlacement(
      type = FurnitureType.DESK_STANDARD,
      targetX = 0,
      targetY = 0,
      movingFurnitureId = null,
      existingFurniture = garageDesks,
      tier = OfficeTier.GARAGE
    )
    assertTrue("Exceeding max desks must be rejected", resultExceed is OfficeLayoutManager.PlacementValidation.Invalid)
  }

  @Test
  fun `tier migration preserves furniture and guarantees non-overlapping layout in new office`() {
    val garageFurniture = GameState.defaultStartingFurniture()
    val migrated = OfficeLayoutManager.migrateFurnitureForTier(garageFurniture, OfficeTier.COWORKING)

    assertEquals("Furniture count must be preserved after relocation", garageFurniture.size, migrated.size)
    val occupied = migrated.map { it.gridX to it.gridY }
    assertEquals("Migrated items must have unique coordinates", occupied.size, occupied.toSet().size)

    val workstations = migrated.filter { it.type.isWorkstation }
    for (i in workstations.indices) {
      for (j in i + 1 until workstations.size) {
        val dx = kotlin.math.abs(workstations[i].gridX - workstations[j].gridX)
        val dy = kotlin.math.abs(workstations[i].gridY - workstations[j].gridY)
        assertTrue("Migrated workstations must have proper clearance", dx >= 2 || dy >= 2)
      }
    }
  }
}
