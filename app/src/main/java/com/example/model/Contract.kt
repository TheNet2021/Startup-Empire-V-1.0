package com.example.model

enum class ContractTier(val title: String, val badgeColor: Long) {
    BRONZE("Startup", 0xFFCD7F32),
    SILVER("Corporate", 0xFF94A3B8),
    GOLD("Enterprise", 0xFFF59E0B),
    PLATINUM("Global Tech", 0xFF00E5FF)
}

enum class ContractStatus {
    AVAILABLE,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

data class ContractMilestone(
    val id: Int,
    val title: String,
    val targetPercent: Float,
    val rewardPayout: Double,
    val isCompleted: Boolean = false
)

data class ClientContract(
    val id: String,
    val title: String,
    val clientName: String,
    val category: ProductCategory,
    val tier: ContractTier,
    val requiredDevPoints: Int,
    val currentDevPoints: Int = 0,
    val requiredDesignPoints: Int,
    val currentDesignPoints: Int = 0,
    val requiredQAPoints: Int,
    val currentQAPoints: Int = 0,
    val deadlineDays: Int,
    val daysRemaining: Int,
    val upfrontDeposit: Double,
    val completionReward: Double,
    val penaltyCost: Double,
    val minReputation: Int = 50,
    val reputationGain: Int = 15,
    val status: ContractStatus = ContractStatus.AVAILABLE,
    val assignedEmployeeIds: List<String> = emptyList(),
    val milestones: List<ContractMilestone> = emptyList()
) {
    val overallProgress: Float get() {
        val devRatio = if (requiredDevPoints > 0) (currentDevPoints.toFloat() / requiredDevPoints).coerceIn(0f, 1f) else 1f
        val designRatio = if (requiredDesignPoints > 0) (currentDesignPoints.toFloat() / requiredDesignPoints).coerceIn(0f, 1f) else 1f
        val qaRatio = if (requiredQAPoints > 0) (currentQAPoints.toFloat() / requiredQAPoints).coerceIn(0f, 1f) else 1f
        return (designRatio * 0.25f) + (devRatio * 0.50f) + (qaRatio * 0.25f)
    }

    val isDeliverable: Boolean get() =
        currentDevPoints >= requiredDevPoints &&
        currentDesignPoints >= requiredDesignPoints &&
        currentQAPoints >= requiredQAPoints

    companion object {
        fun defaultStartingContracts(): List<ClientContract> = listOf(
            ClientContract(
                id = "contract_fintech_portal",
                title = "Fintech Mobile Checkout",
                clientName = "Apex Global Pay",
                category = ProductCategory.SAAS_TOOL,
                tier = ContractTier.BRONZE,
                requiredDevPoints = 80,
                currentDevPoints = 0,
                requiredDesignPoints = 40,
                currentDesignPoints = 0,
                requiredQAPoints = 30,
                currentQAPoints = 0,
                deadlineDays = 12,
                daysRemaining = 12,
                upfrontDeposit = 3500.0,
                completionReward = 9500.0,
                penaltyCost = 2000.0,
                minReputation = 50,
                reputationGain = 15,
                milestones = listOf(
                    ContractMilestone(1, "UI Design & Specs", 0.33f, 1500.0),
                    ContractMilestone(2, "Core API Integration", 0.66f, 3000.0),
                    ContractMilestone(3, "Security Audit & Delivery", 1.0f, 5000.0)
                )
            ),
            ClientContract(
                id = "contract_health_dashboard",
                title = "Healthcare Patient Portal",
                clientName = "Zenith Care Systems",
                category = ProductCategory.SAAS_TOOL,
                tier = ContractTier.SILVER,
                requiredDevPoints = 140,
                currentDevPoints = 0,
                requiredDesignPoints = 70,
                currentDesignPoints = 0,
                requiredQAPoints = 60,
                currentQAPoints = 0,
                deadlineDays = 18,
                daysRemaining = 18,
                upfrontDeposit = 7000.0,
                completionReward = 18500.0,
                penaltyCost = 4500.0,
                minReputation = 90,
                reputationGain = 25,
                milestones = listOf(
                    ContractMilestone(1, "Design Prototype", 0.33f, 3000.0),
                    ContractMilestone(2, "HIPAA Compliant Backend", 0.66f, 6000.0),
                    ContractMilestone(3, "Final Acceptance Testing", 1.0f, 9500.0)
                )
            ),
            ClientContract(
                id = "contract_game_multiplayer",
                title = "Matchmaking Microservice",
                clientName = "Hyperion Interactive",
                category = ProductCategory.MOBILE_GAME,
                tier = ContractTier.GOLD,
                requiredDevPoints = 220,
                currentDevPoints = 0,
                requiredDesignPoints = 60,
                currentDesignPoints = 0,
                requiredQAPoints = 90,
                currentQAPoints = 0,
                deadlineDays = 24,
                daysRemaining = 24,
                upfrontDeposit = 12000.0,
                completionReward = 32000.0,
                penaltyCost = 8000.0,
                minReputation = 140,
                reputationGain = 35,
                milestones = listOf(
                    ContractMilestone(1, "Network Architecture", 0.33f, 6000.0),
                    ContractMilestone(2, "Low Latency Engine", 0.66f, 11000.0),
                    ContractMilestone(3, "Load Testing & Release", 1.0f, 15000.0)
                )
            )
        )
    }
}
