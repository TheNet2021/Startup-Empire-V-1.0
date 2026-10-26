package com.example.model

data class CompanyMilestone(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val rewardCash: Double,
    val rewardReputation: Int,
    val isAchieved: Boolean = false,
    val isClaimed: Boolean = false
) {
    companion object {
        fun defaultMilestones(): List<CompanyMilestone> = listOf(
            CompanyMilestone(
                id = "ms_first_product",
                title = "Ship First App",
                description = "Successfully plan, code, test, and launch your first digital product to the live market.",
                category = "Product",
                rewardCash = 5000.0,
                rewardReputation = 20
            ),
            CompanyMilestone(
                id = "ms_ten_thousand_users",
                title = "10k Active Users",
                description = "Grow total active users across your digital portfolio to 10,000 or more.",
                category = "Audience",
                rewardCash = 12000.0,
                rewardReputation = 35
            ),
            CompanyMilestone(
                id = "ms_deliver_contract",
                title = "First Enterprise Delivery",
                description = "Meet all dev, design, and QA specifications to deliver a client project contract.",
                category = "Contracts",
                rewardCash = 8000.0,
                rewardReputation = 25
            ),
            CompanyMilestone(
                id = "ms_office_loft",
                title = "Downtown Headquarters",
                description = "Upgrade your company headquarters to the Downtown Tech Loft or higher.",
                category = "Operations",
                rewardCash = 15000.0,
                rewardReputation = 30
            ),
            CompanyMilestone(
                id = "ms_research_innovator",
                title = "Tech Innovator",
                description = "Research and unlock 4 or more advanced technologies in the Tech Tree.",
                category = "R&D",
                rewardCash = 20000.0,
                rewardReputation = 40
            ),
            CompanyMilestone(
                id = "ms_market_leader",
                title = "Market Dominance",
                description = "Achieve at least 25% total market share against AI industry competitors.",
                category = "Market",
                rewardCash = 35000.0,
                rewardReputation = 50
            ),
            CompanyMilestone(
                id = "ms_unicorn_status",
                title = "Tech Unicorn ($1M+)",
                description = "Reach a calculated company valuation of $1,000,000 or more.",
                category = "Valuation",
                rewardCash = 50000.0,
                rewardReputation = 75
            ),
            CompanyMilestone(
                id = "ms_strategic_takeover",
                title = "Corporate Titan",
                description = "Acquire a competing tech rival company and absorb their market assets.",
                category = "M&A",
                rewardCash = 40000.0,
                rewardReputation = 60
            )
        )
    }
}
