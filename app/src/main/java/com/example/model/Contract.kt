package com.example.model

enum class ContractTier(val title: String, val badgeColor: Long, val minReputation: Int) {
    BRONZE("Startup Tier", 0xFFCD7F32, 40),
    SILVER("Corporate Tier", 0xFF94A3B8, 85),
    GOLD("Enterprise Tier", 0xFFF59E0B, 135),
    PLATINUM("Global Tech Tier", 0xFF00E5FF, 195)
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
    val minReputation: Int = 40,
    val reputationGain: Int = 15,
    val status: ContractStatus = ContractStatus.AVAILABLE,
    val assignedEmployeeIds: List<String> = emptyList(),
    val milestones: List<ContractMilestone> = emptyList(),
    val isDelivered: Boolean = false,
    val isRewardPaid: Boolean = false
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
        currentQAPoints >= requiredQAPoints &&
        status == ContractStatus.IN_PROGRESS

    companion object {
        fun defaultStartingContracts(): List<ClientContract> = listOf(
            ClientContract(
                id = "contract_fintech_portal",
                title = "Fintech Mobile Checkout",
                clientName = "Apex Global Pay",
                category = ProductCategory.SAAS_TOOL,
                tier = ContractTier.BRONZE,
                requiredDevPoints = 65,
                currentDevPoints = 0,
                requiredDesignPoints = 35,
                currentDesignPoints = 0,
                requiredQAPoints = 25,
                currentQAPoints = 0,
                deadlineDays = 12,
                daysRemaining = 12,
                upfrontDeposit = 3000.0,
                completionReward = 4500.0, // Remaining final payout on delivery
                penaltyCost = 2200.0,
                minReputation = 40,
                reputationGain = 15,
                milestones = listOf(
                    ContractMilestone(1, "Milestone 1: UI Wireframes & Architecture", 0.33f, 1500.0),
                    ContractMilestone(2, "Milestone 2: Checkout Engine & API Integration", 0.66f, 2500.0),
                    ContractMilestone(3, "Milestone 3: Security QA & Final Delivery", 1.0f, 4500.0)
                )
            ),
            ClientContract(
                id = "contract_health_dashboard",
                title = "Healthcare Patient Portal",
                clientName = "Zenith Care Systems",
                category = ProductCategory.SAAS_TOOL,
                tier = ContractTier.SILVER,
                requiredDevPoints = 120,
                currentDevPoints = 0,
                requiredDesignPoints = 60,
                currentDesignPoints = 0,
                requiredQAPoints = 50,
                currentQAPoints = 0,
                deadlineDays = 18,
                daysRemaining = 18,
                upfrontDeposit = 6500.0,
                completionReward = 9500.0,
                penaltyCost = 4500.0,
                minReputation = 85,
                reputationGain = 25,
                milestones = listOf(
                    ContractMilestone(1, "Milestone 1: HIPAA Compliance & UX Draft", 0.33f, 3000.0),
                    ContractMilestone(2, "Milestone 2: Secure Patient DB & Telehealth", 0.66f, 5000.0),
                    ContractMilestone(3, "Milestone 3: Load Testing & Enterprise Delivery", 1.0f, 9500.0)
                )
            ),
            ClientContract(
                id = "contract_game_multiplayer",
                title = "Matchmaking Microservice",
                clientName = "Hyperion Interactive",
                category = ProductCategory.MOBILE_GAME,
                tier = ContractTier.GOLD,
                requiredDevPoints = 200,
                currentDevPoints = 0,
                requiredDesignPoints = 50,
                currentDesignPoints = 0,
                requiredQAPoints = 80,
                currentQAPoints = 0,
                deadlineDays = 24,
                daysRemaining = 24,
                upfrontDeposit = 11000.0,
                completionReward = 16000.0,
                penaltyCost = 8000.0,
                minReputation = 135,
                reputationGain = 35,
                milestones = listOf(
                    ContractMilestone(1, "Milestone 1: Low-Latency Protocol Specs", 0.33f, 5500.0),
                    ContractMilestone(2, "Milestone 2: Redis Cluster & Tick Engine", 0.66f, 9500.0),
                    ContractMilestone(3, "Milestone 3: Stress Testing & Cluster Sign-off", 1.0f, 16000.0)
                )
            )
        )
    }
}
