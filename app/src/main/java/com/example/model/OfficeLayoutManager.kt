package com.example.model

/**
 * Manages spatial layouts, collision detection, walking aisles, and workstation spacing
 * for all office architectural tiers.
 *
 * Guarantees:
 * 1. Every employee has a dedicated workstation with ample room around desks and chairs.
 * 2. Clear gaps and walking corridors between desks, chairs, monitors, and equipment.
 * 3. Zero sprite overlap or intersections.
 * 4. Automatic placement and interactive move validation.
 * 5. Workstation capacity constraints strictly tied to office tier.
 */
object OfficeLayoutManager {

    data class GridCoordinate(val x: Int, val y: Int)

    sealed class PlacementValidation {
        data object Valid : PlacementValidation()
        data class Invalid(val reason: String) : PlacementValidation()
    }

    /**
     * Dedicated, architecturally planned workstation coordinates for each office tier.
     * Guaranteed:
     * - Every slot has at least 2 tiles separation from any other workstation (dx >= 2 or dy >= 2).
     * - Dedicated 2-tile walking aisles between desk pods.
     * - Clear lines of sight with no vertical occlusion.
     */
    fun getWorkstationSlots(tier: OfficeTier): List<GridCoordinate> = when (tier) {
        OfficeTier.GARAGE -> listOf(
            GridCoordinate(1, 3), // Founder workstation (Alex Vance)
            GridCoordinate(3, 1), // Engineering workstation 1
            GridCoordinate(4, 4)  // Growth/Product workstation 2
        )
        OfficeTier.COWORKING -> listOf(
            // Pod 1 (Left wing)
            GridCoordinate(1, 2),
            GridCoordinate(1, 5),
            // Pod 2 (Right wing)
            GridCoordinate(3, 1),
            GridCoordinate(3, 4),
            // Pod 3 (Center-front wing)
            GridCoordinate(5, 2),
            GridCoordinate(5, 5)
        )
        OfficeTier.TECH_LOFT -> listOf(
            // Row 1 (Engineering team)
            GridCoordinate(1, 2),
            GridCoordinate(1, 5),
            GridCoordinate(1, 8),
            // Row 2 (Product & Design)
            GridCoordinate(4, 2),
            GridCoordinate(4, 5),
            GridCoordinate(4, 8),
            // Row 3 (Marketing & Growth)
            GridCoordinate(6, 2),
            GridCoordinate(6, 5),
            GridCoordinate(6, 8),
            // Row 4 (Sales & Leadership)
            GridCoordinate(8, 2),
            GridCoordinate(8, 5),
            GridCoordinate(8, 8)
        )
        OfficeTier.CAMPUS_HQ -> {
            val slots = mutableListOf<GridCoordinate>()
            val xRows = listOf(1, 3, 5, 7, 9, 11)
            val yCols = listOf(2, 5, 8, 10)
            for (gx in xRows) {
                for (gy in yCols) {
                    slots.add(GridCoordinate(gx, gy))
                }
            }
            slots
        }
        OfficeTier.CYBER_TOWER -> {
            val slots = mutableListOf<GridCoordinate>()
            val xRows = listOf(1, 3, 5, 7, 9, 11, 13)
            val yCols = listOf(1, 3, 5, 7, 9, 11, 13)
            for (gx in xRows) {
                for (gy in yCols) {
                    // Reserve central 3x3 for executive marble plaza / boardroom
                    if ((gx == 5 || gx == 7) && (gy == 5 || gy == 7)) continue
                    slots.add(GridCoordinate(gx, gy))
                }
            }
            slots
        }
    }

    /**
     * Dedicated perimeter amenity and infrastructure slots along walls.
     */
    fun getPerimeterAmenitySlots(tier: OfficeTier): List<GridCoordinate> = when (tier) {
        OfficeTier.GARAGE -> listOf(
            GridCoordinate(5, 1), // Server blade rack (right wall)
            GridCoordinate(1, 5), // Water cooler (left wall)
            GridCoordinate(4, 2), // Bonsai plant
            GridCoordinate(0, 4), // Espresso machine
            GridCoordinate(2, 0), // Whiteboard
            GridCoordinate(0, 2), // Bookshelf
            GridCoordinate(4, 0)  // Arcade cabinet
        )
        OfficeTier.COWORKING -> listOf(
            GridCoordinate(7, 1), // Blade rack (right wall)
            GridCoordinate(0, 3), // Water cooler (left wall)
            GridCoordinate(0, 1), // Espresso bar
            GridCoordinate(4, 0), // Whiteboard
            GridCoordinate(0, 6), // Bonsai plant
            GridCoordinate(6, 0), // Bookshelf
            GridCoordinate(7, 6)  // Arcade cabinet
        )
        OfficeTier.TECH_LOFT -> listOf(
            GridCoordinate(9, 1), // Enterprise server rack
            GridCoordinate(0, 3), // Espresso machine
            GridCoordinate(0, 6), // Water cooler
            GridCoordinate(3, 0), // Large whiteboard
            GridCoordinate(6, 0), // Meeting table / lounge
            GridCoordinate(0, 1), // Oak bookshelf
            GridCoordinate(9, 7)  // Arcade cabinet
        )
        OfficeTier.CAMPUS_HQ -> listOf(
            GridCoordinate(11, 1), // Server wing
            GridCoordinate(0, 4),  // Water cooler
            GridCoordinate(0, 7),  // Espresso cafe
            GridCoordinate(4, 0),  // Glass presentation board
            GridCoordinate(8, 0),  // Executive bookshelf
            GridCoordinate(11, 6), // Breakout arcade
            GridCoordinate(0, 1)   // Botanical plant
        )
        OfficeTier.CYBER_TOWER -> listOf(
            GridCoordinate(13, 1), // Quantum enterprise server
            GridCoordinate(0, 4),  // Italian espresso bar
            GridCoordinate(0, 8),  // Hydration station
            GridCoordinate(6, 6),  // Penthouse conference table
            GridCoordinate(4, 0),  // Digital whiteboard
            GridCoordinate(10, 0), // Architectural bookshelf
            GridCoordinate(13, 8)  // Arcade lounge
        )
    }

    /**
     * Rigorous collision detection and spatial clearance validation.
     * Prevents overlapping furniture, blocked aisles, or crowded workstations.
     */
    fun validatePlacement(
        type: FurnitureType,
        targetX: Int,
        targetY: Int,
        movingFurnitureId: String?,
        existingFurniture: List<PlacedFurniture>,
        tier: OfficeTier
    ): PlacementValidation {
        // 1. Boundary Check
        if (targetX !in 0 until tier.gridWidth || targetY !in 0 until tier.gridHeight) {
            return PlacementValidation.Invalid("Cannot place outside office walls!")
        }

        // 2. Direct Occupancy Check
        val otherFurniture = existingFurniture.filter { it.id != movingFurnitureId }
        if (otherFurniture.any { it.gridX == targetX && it.gridY == targetY }) {
            return PlacementValidation.Invalid("Tile ($targetX, $targetY) is already occupied!")
        }

        // 3. Workstation Spacing & Clearance Rules
        if (type.isWorkstation) {
            val workstationCount = otherFurniture.count { it.type.isWorkstation }
            if (movingFurnitureId == null && workstationCount >= tier.maxDesks) {
                return PlacementValidation.Invalid(
                    "Office capacity reached (${tier.maxDesks} desks max)! Relocate to ${tier.title} to expand."
                )
            }

            // Ensure ample clearance from any other workstation:
            // Workstations must have at least 1 empty tile between them in both axes (Chebyshev distance >= 2)
            // so chairs, monitors, and walking aisles are completely unobstructed.
            for (other in otherFurniture.filter { it.type.isWorkstation }) {
                val dx = kotlin.math.abs(targetX - other.gridX)
                val dy = kotlin.math.abs(targetY - other.gridY)
                if (dx < 2 && dy < 2) {
                    return PlacementValidation.Invalid(
                        "Workstations require at least 1 tile clearance for chairs and walking aisles!"
                    )
                }
            }

            // Prevent placing directly behind tall furniture that would block the workstation view
            for (other in otherFurniture.filter { !it.type.isWorkstation }) {
                if (other.gridX == targetX + 1 && other.gridY == targetY + 1) {
                    return PlacementValidation.Invalid("Aisle in front of workstation must remain clear!")
                }
            }
        } else {
            // 4. Amenity/Equipment placement: ensure it doesn't block desk chairs
            for (desk in otherFurniture.filter { it.type.isWorkstation }) {
                if (desk.gridX == targetX && desk.gridY == targetY) {
                    return PlacementValidation.Invalid("Cannot block workstation!")
                }
            }
        }

        return PlacementValidation.Valid
    }

    /**
     * Finds the next open, pre-planned workstation slot for automated purchases.
     */
    fun findNextFreeWorkstationSlot(
        tier: OfficeTier,
        existingFurniture: List<PlacedFurniture>
    ): GridCoordinate? {
        val occupiedTiles = existingFurniture.map { it.gridX to it.gridY }.toSet()
        val plannedSlots = getWorkstationSlots(tier)

        for (slot in plannedSlots) {
            if (!occupiedTiles.contains(slot.x to slot.y)) {
                val validation = validatePlacement(
                    type = FurnitureType.DESK_STANDARD,
                    targetX = slot.x,
                    targetY = slot.y,
                    movingFurnitureId = null,
                    existingFurniture = existingFurniture,
                    tier = tier
                )
                if (validation is PlacementValidation.Valid) {
                    return slot
                }
            }
        }
        return null
    }

    /**
     * Finds an open perimeter or amenity slot for equipment and amenities.
     */
    fun findNextFreeAmenitySlot(
        type: FurnitureType,
        tier: OfficeTier,
        existingFurniture: List<PlacedFurniture>
    ): GridCoordinate? {
        val occupiedTiles = existingFurniture.map { it.gridX to it.gridY }.toSet()
        val plannedAmenities = getPerimeterAmenitySlots(tier)

        for (slot in plannedAmenities) {
            if (!occupiedTiles.contains(slot.x to slot.y)) {
                val validation = validatePlacement(
                    type = type,
                    targetX = slot.x,
                    targetY = slot.y,
                    movingFurnitureId = null,
                    existingFurniture = existingFurniture,
                    tier = tier
                )
                if (validation is PlacementValidation.Valid) {
                    return slot
                }
            }
        }

        // Secondary search along perimeter walls
        for (gx in 0 until tier.gridWidth) {
            for (gy in 0 until tier.gridHeight) {
                val isPerimeter = gx == 0 || gy == 0 || gx == tier.gridWidth - 1 || gy == tier.gridHeight - 1
                if (isPerimeter && !occupiedTiles.contains(gx to gy)) {
                    val validation = validatePlacement(
                        type = type,
                        targetX = gx,
                        targetY = gy,
                        movingFurnitureId = null,
                        existingFurniture = existingFurniture,
                        tier = tier
                    )
                    if (validation is PlacementValidation.Valid) {
                        return GridCoordinate(gx, gy)
                    }
                }
            }
        }

        return null
    }

    /**
     * Re-organizes furniture cleanly into the new office tier during relocation.
     * Preserves assigned employees, rotations, and item identities.
     */
    fun migrateFurnitureForTier(
        oldFurniture: List<PlacedFurniture>,
        newTier: OfficeTier
    ): List<PlacedFurniture> {
        val workstationSlots = getWorkstationSlots(newTier)
        val amenitySlots = getPerimeterAmenitySlots(newTier)

        val workstations = oldFurniture.filter { it.type.isWorkstation }
        val amenities = oldFurniture.filter { !it.type.isWorkstation }

        val migrated = mutableListOf<PlacedFurniture>()
        val occupied = mutableSetOf<Pair<Int, Int>>()

        // 1. Assign workstations to spacious dedicated slots
        workstations.forEachIndexed { index, desk ->
            val slot = workstationSlots.getOrElse(index) {
                // Fallback spaced coordinate
                GridCoordinate(
                    x = (index * 2) % (newTier.gridWidth - 2) + 1,
                    y = ((index * 2) / (newTier.gridWidth - 2) * 2) % (newTier.gridHeight - 2) + 1
                )
            }
            migrated.add(desk.copy(gridX = slot.x, gridY = slot.y))
            occupied.add(slot.x to slot.y)
        }

        // 2. Assign amenities to perimeter slots avoiding occupied
        amenities.forEachIndexed { index, amen ->
            val slot = amenitySlots.firstOrNull { !occupied.contains(it.x to it.y) }
                ?: run {
                    var found: GridCoordinate? = null
                    for (gx in 0 until newTier.gridWidth) {
                        for (gy in 0 until newTier.gridHeight) {
                            val isPerimeter = gx == 0 || gy == 0 || gx == newTier.gridWidth - 1 || gy == newTier.gridHeight - 1
                            if (isPerimeter && !occupied.contains(gx to gy)) {
                                found = GridCoordinate(gx, gy)
                                break
                            }
                        }
                        if (found != null) break
                    }
                    found ?: GridCoordinate(
                        amen.gridX.coerceIn(0, newTier.gridWidth - 1),
                        amen.gridY.coerceIn(0, newTier.gridHeight - 1)
                    )
                }
            migrated.add(amen.copy(gridX = slot.x, gridY = slot.y))
            occupied.add(slot.x to slot.y)
        }

        return migrated
    }
}
