package com.example.model

enum class ProductCategory(
    val title: String,
    val iconName: String,
    val defaultName: String,
    val baseDesignReq: Int,
    val baseCodeReq: Int,
    val baseTestReq: Int,
    val viralityFactor: Float,
    val serverLoadPerUser: Float,
    val description: String
) {
    SOCIAL_APP(
        title = "Social Network",
        iconName = "Share",
        defaultName = "Sphere",
        baseDesignReq = 120,
        baseCodeReq = 200,
        baseTestReq = 80,
        viralityFactor = 1.6f,
        serverLoadPerUser = 0.8f,
        description = "Viral community platform with high traffic & advertising revenue."
    ),
    SAAS_TOOL(
        title = "B2B SaaS Suite",
        iconName = "Work",
        defaultName = "WorkFlow Pro",
        baseDesignReq = 100,
        baseCodeReq = 260,
        baseTestReq = 140,
        viralityFactor = 0.9f,
        serverLoadPerUser = 0.5f,
        description = "High subscription revenue and corporate contract retention."
    ),
    MOBILE_GAME(
        title = "Mobile Game",
        iconName = "SportsEsports",
        defaultName = "PixelQuest",
        baseDesignReq = 220,
        baseCodeReq = 180,
        baseTestReq = 100,
        viralityFactor = 1.4f,
        serverLoadPerUser = 0.6f,
        description = "Fast monetization with in-app purchases and ad rewards."
    ),
    ECOMMERCE(
        title = "E-Commerce Market",
        iconName = "ShoppingCart",
        defaultName = "CartWave",
        baseDesignReq = 140,
        baseCodeReq = 240,
        baseTestReq = 120,
        viralityFactor = 1.1f,
        serverLoadPerUser = 0.7f,
        description = "Transactional marketplace taking revenue cuts on orders."
    ),
    AI_ASSISTANT(
        title = "AI Engine",
        iconName = "Psychology",
        defaultName = "OmniMind AI",
        baseDesignReq = 160,
        baseCodeReq = 360,
        baseTestReq = 180,
        viralityFactor = 1.8f,
        serverLoadPerUser = 1.5f,
        description = "Cutting-edge neural assistant with viral hype and high ARPU."
    )
}

enum class ProductStage {
    PLANNING,
    DEVELOPMENT,
    TESTING,
    READY_TO_LAUNCH,
    LIVE,
    MAINTENANCE
}

enum class MonetizationModel(
    val title: String,
    val revenuePerUser: Double,
    val churnPenalty: Float,
    val description: String
) {
    FREE_WITH_ADS(
        title = "Free + Ads",
        revenuePerUser = 0.015,
        churnPenalty = 0.02f,
        description = "Low barrier to entry, earns through programmatic impressions."
    ),
    FREEMIUM(
        title = "Freemium",
        revenuePerUser = 0.045,
        churnPenalty = 0.01f,
        description = "Free core features with premium tier upgrades."
    ),
    SUBSCRIPTION(
        title = "Pro Subscription",
        revenuePerUser = 0.12,
        churnPenalty = 0.03f,
        description = "Predictable recurring revenue from committed users."
    ),
    ENTERPRISE(
        title = "Enterprise Contracts",
        revenuePerUser = 0.35,
        churnPenalty = 0.005f,
        description = "High value B2B enterprise tier requiring top stability."
    )
}

data class Product(
    val id: String,
    val name: String,
    val category: ProductCategory,
    val stage: ProductStage = ProductStage.PLANNING,
    val version: String = "1.0",
    val planningPointsNeeded: Int = (category.baseDesignReq * 0.4f).toInt().coerceAtLeast(30),
    val planningPointsCurrent: Int = 0,
    val designPointsNeeded: Int = category.baseDesignReq,
    val designPointsCurrent: Int = 0,
    val codePointsNeeded: Int = category.baseCodeReq,
    val codePointsCurrent: Int = 0,
    val testPointsNeeded: Int = category.baseTestReq,
    val testPointsCurrent: Int = 0,
    val bugsCount: Int = 5,
    val bugsSquashed: Int = 0,
    val techDebt: Int = 0,
    val monetization: MonetizationModel = MonetizationModel.FREEMIUM,
    val activeUsers: Int = 0,
    val totalRevenue: Double = 0.0,
    val dailyRevenue: Double = 0.0,
    val dailyMarketingSpend: Double = 0.0,
    val userSatisfaction: Float = 0.85f, // 0.0 to 1.0
    val daysLive: Int = 0,
    val sprintMode: SprintMode = SprintMode.NORMAL,
    val dailyDevCost: Double = 35.0,
    val activeCampaigns: List<ActiveCampaign> = emptyList()
) {
    val overallDevProgress: Float get() {
        val planRatio = if (planningPointsNeeded > 0) (planningPointsCurrent.toFloat() / planningPointsNeeded).coerceIn(0f, 1f) else 1f
        val designRatio = (designPointsCurrent.toFloat() / designPointsNeeded).coerceIn(0f, 1f)
        val codeRatio = (codePointsCurrent.toFloat() / codePointsNeeded).coerceIn(0f, 1f)
        val testRatio = (testPointsCurrent.toFloat() / testPointsNeeded).coerceIn(0f, 1f)
        return (planRatio * 0.15f) + (designRatio * 0.25f) + (codeRatio * 0.40f) + (testRatio * 0.20f)
    }

    val isDevComplete: Boolean get() =
        planningPointsCurrent >= planningPointsNeeded &&
        designPointsCurrent >= designPointsNeeded &&
        codePointsCurrent >= codePointsNeeded &&
        testPointsCurrent >= testPointsNeeded

    val rating: Float get() {
        val base = userSatisfaction * 5f
        val penalty = (bugsCount * 0.15f + techDebt * 0.05f).coerceAtMost(2.5f)
        return (base - penalty).coerceIn(1.0f, 5.0f)
    }
}

enum class SprintMode(val title: String, val speedMultiplier: Float, val costMultiplier: Float, val fatigueMultiplier: Float) {
    NORMAL("Normal Pace", 1.0f, 1.0f, 1.0f),
    CRUNCH("Sprint Crunch (2x)", 2.0f, 2.5f, 2.0f)
}

enum class CampaignType(
    val title: String,
    val upfrontCost: Double,
    val durationDays: Int,
    val expectedUsersMin: Int,
    val expectedUsersMax: Int,
    val description: String
) {
    SOCIAL_MEDIA_BLITZ(
        title = "Social Media Blitz",
        upfrontCost = 600.0,
        durationDays = 5,
        expectedUsersMin = 1500,
        expectedUsersMax = 3500,
        description = "Targeted ads on Twitter, TikTok & Instagram for fast viral reach."
    ),
    INFLUENCER_SPONSORSHIP(
        title = "Tech Influencer Sponsorship",
        upfrontCost = 2500.0,
        durationDays = 7,
        expectedUsersMin = 7000,
        expectedUsersMax = 15000,
        description = "Sponsored review video from a top tech creator with high trust."
    ),
    APP_STORE_FEATURE(
        title = "App Store Feature Campaign",
        upfrontCost = 6000.0,
        durationDays = 10,
        expectedUsersMin = 20000,
        expectedUsersMax = 45000,
        description = "Top-tier search ads and editorial spotlight boosting conversion."
    ),
    GLOBAL_PR_LAUNCH(
        title = "Global Press & PR Tour",
        upfrontCost = 18000.0,
        durationDays = 14,
        expectedUsersMin = 75000,
        expectedUsersMax = 160000,
        description = "International news coverage across major tech and business outlets."
    )
}

data class ActiveCampaign(
    val id: String,
    val productId: String,
    val type: CampaignType,
    val daysRemaining: Int,
    val usersGenerated: Int = 0
)

