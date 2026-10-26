package com.example.model

data class CompetitorCompany(
    val id: String,
    val name: String,
    val category: ProductCategory,
    val logoIcon: String = "business",
    val logoColor: Long = 0xFF6366F1,
    val valuation: Double,
    val monthlyRevenue: Double,
    val cash: Double,
    val activeUsers: Int,
    val marketShare: Float, // percentage e.g. 24.5f
    val reputation: Int,
    val employeeCount: Int,
    val productNames: List<String>,
    val isAcquired: Boolean = false,
    val acquisitionCost: Double,
    val status: String = "Growing" // "Dominant", "Growing", "Struggling", "Acquired"
) {
    val isEligibleForAcquisition: Boolean get() = !isAcquired

    companion object {
        fun defaultCompetitors(): List<CompetitorCompany> = listOf(
            CompetitorCompany(
                id = "comp_nexus",
                name = "Nexus Dynamics",
                category = ProductCategory.SOCIAL_APP,
                logoIcon = "share",
                logoColor = 0xFF6366F1,
                valuation = 480000.0,
                monthlyRevenue = 38000.0,
                cash = 85000.0,
                activeUsers = 125000,
                marketShare = 32.5f,
                reputation = 220,
                employeeCount = 18,
                productNames = listOf("Nexus Feed", "Vibe Chat"),
                acquisitionCost = 420000.0,
                status = "Dominant"
            ),
            CompetitorCompany(
                id = "comp_quantum",
                name = "Quantum Byte",
                category = ProductCategory.SAAS_TOOL,
                logoIcon = "work",
                logoColor = 0xFF10B981,
                valuation = 290000.0,
                monthlyRevenue = 24000.0,
                cash = 45000.0,
                activeUsers = 48000,
                marketShare = 21.0f,
                reputation = 175,
                employeeCount = 12,
                productNames = listOf("TaskFlow Enterprise", "DataSync"),
                acquisitionCost = 260000.0,
                status = "Growing"
            ),
            CompetitorCompany(
                id = "comp_veloce",
                name = "Veloce Interactive",
                category = ProductCategory.MOBILE_GAME,
                logoIcon = "sports_esports",
                logoColor = 0xFFF59E0B,
                valuation = 185000.0,
                monthlyRevenue = 15000.0,
                cash = 22000.0,
                activeUsers = 72000,
                marketShare = 16.5f,
                reputation = 140,
                employeeCount = 9,
                productNames = listOf("Neon Racer 3D", "Cyber Blitz"),
                acquisitionCost = 165000.0,
                status = "Growing"
            ),
            CompetitorCompany(
                id = "comp_orbit",
                name = "Orbit Commerce",
                category = ProductCategory.ECOMMERCE,
                logoIcon = "shopping_cart",
                logoColor = 0xFFEC4899,
                valuation = 120000.0,
                monthlyRevenue = 9500.0,
                cash = 8000.0,
                activeUsers = 31000,
                marketShare = 11.2f,
                reputation = 115,
                employeeCount = 6,
                productNames = listOf("DropShip Direct"),
                acquisitionCost = 98000.0,
                status = "Struggling" // Struggling -> cheaper buyout target!
            ),
            CompetitorCompany(
                id = "comp_synapse",
                name = "Synapse Cognitive AI",
                category = ProductCategory.AI_ASSISTANT,
                logoIcon = "psychology",
                logoColor = 0xFF8B5CF6,
                valuation = 360000.0,
                monthlyRevenue = 29000.0,
                cash = 52000.0,
                activeUsers = 88000,
                marketShare = 18.8f,
                reputation = 190,
                employeeCount = 14,
                productNames = listOf("Synapse Brain", "PromptGenius"),
                acquisitionCost = 325000.0,
                status = "Growing"
            )
        )
    }
}

data class LeaderboardEntry(
    val rank: Int,
    val companyId: String,
    val companyName: String,
    val logoIcon: String,
    val logoColor: Long,
    val valuation: Double,
    val monthlyRevenue: Double,
    val activeUsers: Int,
    val marketShare: Float,
    val reputation: Int,
    val isPlayer: Boolean,
    val isAcquired: Boolean = false,
    val statusText: String = ""
)
