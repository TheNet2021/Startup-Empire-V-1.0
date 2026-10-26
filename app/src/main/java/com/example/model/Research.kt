package com.example.model

enum class TechUpgrade(
    val id: String,
    val title: String,
    val category: String,
    val cost: Double,
    val requiredResearchDays: Int,
    val prereqId: String? = null,
    val description: String,
    val devSpeedBonus: Float = 0f,
    val designSpeedBonus: Float = 0f,
    val bugReductionFactor: Float = 0f,
    val serverCapacityBonus: Float = 0f,
    val marketingBoost: Float = 0f,
    val overallEfficiency: Float = 0f
) {
    AGILE_SPRINTS(
        id = "agile_sprints",
        title = "Agile Sprints",
        category = "Process",
        cost = 1200.0,
        requiredResearchDays = 2,
        description = "Modern scrum workflow boosting software development speed by 20%.",
        devSpeedBonus = 0.20f
    ),
    DESIGN_SYSTEM(
        id = "design_system",
        title = "Atomic Design System",
        category = "Design",
        cost = 2000.0,
        requiredResearchDays = 3,
        prereqId = "agile_sprints",
        description = "Standardized component library speeding up design output by 35%.",
        designSpeedBonus = 0.35f
    ),
    CI_CD_PIPELINE(
        id = "ci_cd",
        title = "Automated CI/CD",
        category = "DevOps",
        cost = 3500.0,
        requiredResearchDays = 4,
        prereqId = "agile_sprints",
        description = "Automated integration testing catching 50% of software bugs before launch.",
        bugReductionFactor = 0.50f
    ),
    CONTAINER_K8S(
        id = "container_k8s",
        title = "Microservices & K8s",
        category = "Infrastructure",
        cost = 6500.0,
        requiredResearchDays = 5,
        prereqId = "ci_cd",
        description = "Container orchestration expanding server user capacity by 35%.",
        serverCapacityBonus = 0.35f
    ),
    AI_CODE_COPILOT(
        id = "ai_copilot",
        title = "AI Neural Copilot",
        category = "Engineering",
        cost = 12000.0,
        requiredResearchDays = 6,
        prereqId = "container_k8s",
        description = "Synthesizes boilerplate instantly, accelerating coding output by 40%.",
        devSpeedBonus = 0.40f
    ),
    VIRAL_GROWTH_ENGINE(
        id = "viral_growth",
        title = "Viral Growth Funnels",
        category = "Marketing",
        cost = 16000.0,
        requiredResearchDays = 5,
        prereqId = "design_system",
        description = "Engineered referral loops that boost marketing user acquisition by 50%.",
        marketingBoost = 0.50f
    ),
    GLOBAL_EDGE_CDN(
        id = "global_edge_cdn",
        title = "Global Anycast Edge",
        category = "Infrastructure",
        cost = 32000.0,
        requiredResearchDays = 8,
        prereqId = "container_k8s",
        description = "Sub-10ms global edge network reducing latency and doubling server throughput.",
        serverCapacityBonus = 0.50f
    ),
    QUANTUM_ARCH(
        id = "quantum_arch",
        title = "Autonomous AI Empire",
        category = "Futurism",
        cost = 80000.0,
        requiredResearchDays = 12,
        prereqId = "ai_copilot",
        description = "Next-gen autonomous software architecture boosting total company efficiency by 75%.",
        overallEfficiency = 0.75f
    )
}

data class ActiveResearch(
    val techId: String,
    val daysInvested: Int = 0,
    val daysRequired: Int
) {
    val progress: Float get() = (daysInvested.toFloat() / daysRequired.coerceAtLeast(1)).coerceIn(0f, 1f)
}
