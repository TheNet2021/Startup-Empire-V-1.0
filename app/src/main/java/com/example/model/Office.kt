package com.example.model

enum class OfficeTier(
    val title: String,
    val maxDesks: Int,
    val monthlyRent: Double,
    val upgradeCost: Double,
    val gridWidth: Int,
    val gridHeight: Int,
    val description: String,
    val requiredCompanyLevel: Int = 1,
    val requiredActiveUsers: Int = 0,
    val requiredLiveProducts: Int = 0,
    val unlockRequirementText: String = "Available initially"
) {
    GARAGE(
        title = "Garage Startup",
        maxDesks = 3,
        monthlyRent = 150.0,
        upgradeCost = 0.0,
        gridWidth = 6,
        gridHeight = 6,
        description = "Humble beginnings. Bare concrete, flickering lights, and unlimited hustle.",
        requiredCompanyLevel = 1,
        requiredActiveUsers = 0,
        requiredLiveProducts = 0,
        unlockRequirementText = "Starting headquarters"
    ),
    COWORKING(
        title = "Incubator Space",
        maxDesks = 6,
        monthlyRent = 650.0,
        upgradeCost = 5000.0,
        gridWidth = 8,
        gridHeight = 8,
        description = "Shared tech incubator with beanbags, espresso, and high-speed fiber.",
        requiredCompanyLevel = 2,
        requiredActiveUsers = 0,
        requiredLiveProducts = 0,
        unlockRequirementText = "Requires Company Level 2 (Seed Startup)"
    ),
    TECH_LOFT(
        title = "Downtown Tech Loft",
        maxDesks = 12,
        monthlyRent = 2400.0,
        upgradeCost = 20000.0,
        gridWidth = 10,
        gridHeight = 10,
        description = "Exposed brick, glass partitions, and room for a serious engineering squad.",
        requiredCompanyLevel = 3,
        requiredActiveUsers = 1000,
        requiredLiveProducts = 1,
        unlockRequirementText = "Requires Level 3, 1,000 Users & 1 Live Product"
    ),
    CAMPUS_HQ(
        title = "Silicon Park Campus",
        maxDesks = 24,
        monthlyRent = 7500.0,
        upgradeCost = 75000.0,
        gridWidth = 12,
        gridHeight = 12,
        description = "Modern corporate campus with dedicated server wing and meeting hubs.",
        requiredCompanyLevel = 4,
        requiredActiveUsers = 25000,
        requiredLiveProducts = 2,
        unlockRequirementText = "Requires Level 4 & 25,000 Active Users"
    ),
    CYBER_TOWER(
        title = "Skyline Cyber Tower",
        maxDesks = 48,
        monthlyRent = 22000.0,
        upgradeCost = 250000.0,
        gridWidth = 14,
        gridHeight = 14,
        description = "Penthouse headquarters of an international tech conglomerate.",
        requiredCompanyLevel = 5,
        requiredActiveUsers = 100000,
        requiredLiveProducts = 3,
        unlockRequirementText = "Requires Level 5 & 100,000 Active Users"
    )
}

enum class FurnitureCategory {
    WORKSTATION,
    INFRASTRUCTURE,
    AMENITY,
    DECORATION
}

enum class FurnitureType(
    val title: String,
    val category: FurnitureCategory,
    val cost: Double,
    val isWorkstation: Boolean,
    val userCapacityBoost: Int = 0,
    val productivityBonus: Float = 0f,
    val moraleBonus: Int = 0,
    val description: String
) {
    DESK_STANDARD(
        title = "Basic Desk",
        category = FurnitureCategory.WORKSTATION,
        cost = 250.0,
        isWorkstation = true,
        description = "Simple wooden workstation with single monitor."
    ),
    DESK_DUAL_SCREEN(
        title = "Dual-Monitor Station",
        category = FurnitureCategory.WORKSTATION,
        cost = 650.0,
        isWorkstation = true,
        productivityBonus = 0.15f,
        description = "Twin ultrawide displays. Increases developer & designer velocity."
    ),
    DESK_EXECUTIVE(
        title = "Executive Battle Station",
        category = FurnitureCategory.WORKSTATION,
        cost = 1600.0,
        isWorkstation = true,
        productivityBonus = 0.30f,
        moraleBonus = 15,
        description = "Ergonomic motorized standing desk with high-end mechanical gear."
    ),
    SERVER_RACK_BLADE(
        title = "Blade Server Rack",
        category = FurnitureCategory.INFRASTRUCTURE,
        cost = 1200.0,
        isWorkstation = false,
        userCapacityBoost = 15000,
        description = "High-density computing rack handling up to 15,000 active users."
    ),
    SERVER_RACK_ENTERPRISE(
        title = "Quantum Server Rack",
        category = FurnitureCategory.INFRASTRUCTURE,
        cost = 3800.0,
        isWorkstation = false,
        userCapacityBoost = 60000,
        description = "Liquid-cooled server stack for massive concurrent traffic."
    ),
    ESPRESSO_MACHINE(
        title = "Italian Espresso Bar",
        category = FurnitureCategory.AMENITY,
        cost = 500.0,
        isWorkstation = false,
        moraleBonus = 10,
        description = "Gourmet caffeine station. Halves employee energy fatigue."
    ),
    WATER_COOLER(
        title = "Water Cooler",
        category = FurnitureCategory.AMENITY,
        cost = 180.0,
        isWorkstation = false,
        moraleBonus = 5,
        description = "Social hub where employees share quick gossip and boost spirits."
    ),
    ARCADE_CABINET(
        title = "Retro Arcade Machine",
        category = FurnitureCategory.AMENITY,
        cost = 950.0,
        isWorkstation = false,
        moraleBonus = 20,
        description = "Classic arcade cabinet that restores team morale during breaks."
    ),
    BONSAI_PLANT(
        title = "Potted Ficus Plant",
        category = FurnitureCategory.DECORATION,
        cost = 90.0,
        isWorkstation = false,
        moraleBonus = 4,
        description = "Air-purifying greenery that makes the office feel inviting."
    ),
    WHITEBOARD(
        title = "Kanban Whiteboard",
        category = FurnitureCategory.DECORATION,
        cost = 220.0,
        isWorkstation = false,
        productivityBonus = 0.10f,
        description = "Visual sprint planner. Boosts design and project speed by 10%."
    ),
    RECEPTION_DESK(
        title = "Reception Front Desk",
        category = FurnitureCategory.DECORATION,
        cost = 850.0,
        isWorkstation = false,
        moraleBonus = 12,
        description = "Modern wood-paneled reception desk welcoming visitors and investors."
    ),
    MEETING_TABLE(
        title = "Executive Conference Table",
        category = FurnitureCategory.AMENITY,
        cost = 1400.0,
        isWorkstation = false,
        productivityBonus = 0.20f,
        description = "Spacious meeting table with 4 ergonomic chairs for team standups."
    ),
    BOOKSHELF(
        title = "Tech Bookshelf & Storage",
        category = FurnitureCategory.DECORATION,
        cost = 320.0,
        isWorkstation = false,
        productivityBonus = 0.05f,
        description = "Engineering reference library and documentation archive cabinet."
    )
}

data class PlacedFurniture(
    val id: String,
    val type: FurnitureType,
    val gridX: Int,
    val gridY: Int,
    val rotation: Int = 0, // 0, 90, 180, 270
    val assignedEmployeeId: String? = null
)
