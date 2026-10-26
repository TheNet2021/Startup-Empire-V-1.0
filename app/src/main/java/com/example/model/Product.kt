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
    val yesterdayRevenue: Double = 0.0,
    val dailyMarketingSpend: Double = 0.0,
    val userSatisfaction: Float = 0.85f, // 0.0 to 1.0
    val daysLive: Int = 0,
    val sprintMode: SprintMode = SprintMode.NORMAL,
    val dailyDevCost: Double = 35.0,
    val activeCampaigns: List<ActiveCampaign> = emptyList(),
    val marketResearch: MarketResearchReport? = null,
    val features: List<ProductFeature> = emptyList(),
    val customerReviews: List<CustomerReview> = emptyList()
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

    val effectiveARPU: Double get() {
        val featureBoost = features.filter { it.isImplemented }.sumOf { it.arpuBoost }
        return monetization.revenuePerUser + featureBoost
    }

    val rating: Float get() {
        val base = userSatisfaction * 5f
        val penalty = (bugsCount * 0.15f + techDebt * 0.05f).coerceAtMost(2.5f)
        return (base - penalty).coerceIn(1.0f, 5.0f)
    }
}

data class MarketResearchReport(
    val isCompleted: Boolean = false,
    val marketSaturation: String = "Moderate",
    val demandScore: Float = 0.90f,
    val recommendedPricing: MonetizationModel = MonetizationModel.FREEMIUM,
    val targetAudience: String = "Tech Early Adopters",
    val estimatedMaxUsers: Int = 150000,
    val competitorDensity: String = "2 Active Rivals"
)

data class ProductIdea(
    val id: String,
    val name: String,
    val category: ProductCategory,
    val tagLine: String,
    val description: String,
    val demandScore: Float,
    val targetAudience: String,
    val suggestedMonetization: MonetizationModel
)

fun defaultIdeasForCategory(category: ProductCategory): List<ProductIdea> {
    return when (category) {
        ProductCategory.SOCIAL_APP -> listOf(
            ProductIdea("idea_social_1", "Sphere Social", ProductCategory.SOCIAL_APP, "Next-gen interest graph", "Ephemeral multimedia feeds with high viral loops.", 1.25f, "Gen-Z & Creators", MonetizationModel.FREE_WITH_ADS),
            ProductIdea("idea_social_2", "AudioClub", ProductCategory.SOCIAL_APP, "Live voice lounges", "Drop-in synchronous audio discussions for tech communities.", 1.10f, "Tech Professionals", MonetizationModel.FREEMIUM),
            ProductIdea("idea_social_3", "DevCircle", ProductCategory.SOCIAL_APP, "Code portfolio & peer reviews", "Developer network with interactive snippet showcases.", 1.30f, "Software Engineers", MonetizationModel.FREEMIUM)
        )
        ProductCategory.SAAS_TOOL -> listOf(
            ProductIdea("idea_saas_1", "WorkFlow Pro", ProductCategory.SAAS_TOOL, "Sprint & issue tracker", "Agile kanban with automated git branch syncing.", 1.15f, "SMB Tech Teams", MonetizationModel.SUBSCRIPTION),
            ProductIdea("idea_saas_2", "CloudLedger", ProductCategory.SAAS_TOOL, "Automated startup accounting", "One-click corporate tax filings & burn rate analytics.", 1.20f, "Founders & CFOs", MonetizationModel.SUBSCRIPTION),
            ProductIdea("idea_saas_3", "API Gateway 360", ProductCategory.SAAS_TOOL, "Enterprise microservice routing", "Low-latency edge caching with SOC2 compliance.", 1.35f, "Enterprise IT Admins", MonetizationModel.ENTERPRISE)
        )
        ProductCategory.MOBILE_GAME -> listOf(
            ProductIdea("idea_game_1", "PixelQuest RPG", ProductCategory.MOBILE_GAME, "Retro isometric crawler", "Dungeon looter with battle pass cosmetics & weekly boss raids.", 1.30f, "Casual & Core Gamers", MonetizationModel.FREEMIUM),
            ProductIdea("idea_game_2", "CyberCity 2099", ProductCategory.MOBILE_GAME, "Sci-fi idle builder", "Incremental resource tycoon with cybernetic enhancements.", 1.15f, "Mobile Gamers", MonetizationModel.FREEMIUM),
            ProductIdea("idea_game_3", "SpeedDrift Legends", ProductCategory.MOBILE_GAME, "Synchronous multiplayer racer", "Real-time physics drift battles with car customization.", 1.25f, "Competitive Mobile Players", MonetizationModel.FREE_WITH_ADS)
        )
        ProductCategory.ECOMMERCE -> listOf(
            ProductIdea("idea_ecom_1", "CartWave Direct", ProductCategory.ECOMMERCE, "Curated creator marketplace", "Fast social commerce with 1-click video checkout.", 1.10f, "Online Shoppers", MonetizationModel.FREEMIUM),
            ProductIdea("idea_ecom_2", "B2B Wholesale Hub", ProductCategory.ECOMMERCE, "Bulk vendor procurement", "Supply chain ordering platform with invoice credit terms.", 1.25f, "Commercial Retailers", MonetizationModel.ENTERPRISE),
            ProductIdea("idea_ecom_3", "ArtisanCraft", ProductCategory.ECOMMERCE, "Handmade goods exchange", "Direct-from-maker boutique platform with buyer protection.", 1.05f, "Craft Enthusiasts", MonetizationModel.FREEMIUM)
        )
        ProductCategory.AI_ASSISTANT -> listOf(
            ProductIdea("idea_ai_1", "OmniMind AI", ProductCategory.AI_ASSISTANT, "Autonomous task orchestrator", "Executes multi-step browser workflows and code generation.", 1.45f, "Knowledge Workers", MonetizationModel.SUBSCRIPTION),
            ProductIdea("idea_ai_2", "NeuralScribe", ProductCategory.AI_ASSISTANT, "Meeting intelligence assistant", "Real-time multilingual transcription with actionable Jira summaries.", 1.35f, "Corporate Executives", MonetizationModel.ENTERPRISE),
            ProductIdea("idea_ai_3", "CodeCompanion", ProductCategory.AI_ASSISTANT, "Local IDE neural copilot", "Privacy-first on-device code autocomplete & refactoring.", 1.40f, "Software Developers", MonetizationModel.SUBSCRIPTION)
        )
    }
}

data class ProductFeature(
    val id: String,
    val name: String,
    val description: String,
    val devPointsReq: Int,
    val designPointsReq: Int,
    val qaPointsReq: Int,
    val satisfactionBoost: Float,
    val arpuBoost: Double,
    val devPointsCurrent: Int = 0,
    val designPointsCurrent: Int = 0,
    val qaPointsCurrent: Int = 0,
    val isImplemented: Boolean = false,
    val isUnderDevelopment: Boolean = false
) {
    val progress: Float get() {
        val total = devPointsReq + designPointsReq + qaPointsReq
        if (total <= 0) return 1f
        val current = devPointsCurrent + designPointsCurrent + qaPointsCurrent
        return (current.toFloat() / total).coerceIn(0f, 1f)
    }

    val isReadyToDeploy: Boolean get() =
        devPointsCurrent >= devPointsReq &&
        designPointsCurrent >= designPointsReq &&
        qaPointsCurrent >= qaPointsReq
}

data class CustomerReview(
    val id: String,
    val author: String,
    val rating: Int, // 1 to 5 stars
    val comment: String,
    val daySubmitted: Int
)

fun defaultFeaturesForCategory(category: ProductCategory): List<ProductFeature> {
    return when (category) {
        ProductCategory.SOCIAL_APP -> listOf(
            ProductFeature("feat_dark_mode", "Dark Mode & Theming", "Improves nighttime usage and user comfort.", 40, 30, 20, 0.05f, 0.005),
            ProductFeature("feat_stories", "Ephemeral Stories", "24-hour video clips boosting daily active sessions.", 70, 50, 40, 0.08f, 0.012),
            ProductFeature("feat_algorithm", "Neural Recommendation Feed", "Personalized machine-learning content feed.", 110, 40, 60, 0.12f, 0.025),
            ProductFeature("feat_chat", "Encrypted Realtime Messaging", "P2P instant messaging with rich reactions.", 80, 45, 50, 0.09f, 0.018)
        )
        ProductCategory.SAAS_TOOL -> listOf(
            ProductFeature("feat_audit_log", "Enterprise Audit Logs", "SOC2-compliant security logs for IT admins.", 60, 25, 45, 0.06f, 0.040),
            ProductFeature("feat_api_webhooks", "Public REST API & Webhooks", "Enables third-party ERP and CRM integrations.", 95, 30, 55, 0.10f, 0.065),
            ProductFeature("feat_sso", "SAML Single Sign-On (SSO)", "One-click login with Okta & Azure AD.", 75, 20, 50, 0.08f, 0.050),
            ProductFeature("feat_collab", "Real-Time Multiplayer Canvas", "CRDT-based live synchronous document editing.", 120, 65, 80, 0.15f, 0.080)
        )
        ProductCategory.MOBILE_GAME -> listOf(
            ProductFeature("feat_battlepass", "Seasonal Battlepass", "Tiered cosmetic rewards driving monetization.", 65, 80, 35, 0.07f, 0.035),
            ProductFeature("feat_leaderboards", "Global Ranked Tournaments", "Competitive matchmaking with seasonal badges.", 80, 50, 45, 0.09f, 0.020),
            ProductFeature("feat_guilds", "Guild Warfare & Clans", "Social clan alliances and cooperative raids.", 100, 70, 60, 0.11f, 0.030),
            ProductFeature("feat_cloudsave", "Seamless Cross-Platform Cloud Save", "Sync progress across Android, iOS and Web.", 50, 20, 40, 0.06f, 0.015)
        )
        ProductCategory.ECOMMERCE -> listOf(
            ProductFeature("feat_oneclick", "One-Click Instant Checkout", "Stripe & Apple Pay integration for low cart abandonment.", 70, 45, 50, 0.08f, 0.030),
            ProductFeature("feat_tracking", "Live Map Delivery Tracking", "Real-time courier GPS route tracking.", 90, 55, 60, 0.10f, 0.040),
            ProductFeature("feat_reviews", "Verified Customer Photo Reviews", "Social proof and buyer Q&A section.", 45, 40, 30, 0.05f, 0.020),
            ProductFeature("feat_loyalty", "VIP Rewards & Cashback Program", "Tiered points for repeat order frequency.", 60, 35, 40, 0.07f, 0.035)
        )
        ProductCategory.AI_ASSISTANT -> listOf(
            ProductFeature("feat_vision", "Multimodal Vision Input", "Analyze user camera photos, charts, and diagrams.", 110, 40, 70, 0.12f, 0.060),
            ProductFeature("feat_voice", "Low-Latency Neural Speech", "Human-like natural duplex voice conversation.", 130, 30, 80, 0.14f, 0.075),
            ProductFeature("feat_plugins", "Autonomous Agent Plugins", "Executes web searches, calendar bookings, and emails.", 140, 50, 90, 0.16f, 0.090),
            ProductFeature("feat_memory", "Long-Term Personalized Memory", "Remembers user preferences and past context forever.", 85, 30, 55, 0.09f, 0.045)
        )
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

