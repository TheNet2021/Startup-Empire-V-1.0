package com.example.model

data class ServerInfrastructure(
    val baseCloudCapacity: Int = 5000,
    val rackCapacity: Int = 0,
    val isCloudAutoScaleEnabled: Boolean = true,
    val cloudUnitDailyCost: Double = 0.04, // per 1k users auto-scaled
    val uptimePercent: Float = 99.95f,
    val isOutageActive: Boolean = false,
    val outageHoursRemaining: Int = 0,
    val incidentReason: String? = null
) {
    val totalPhysicalCapacity: Int get() = baseCloudCapacity + rackCapacity

    fun calculateLoad(totalActiveUsers: Int): Float {
        if (totalPhysicalCapacity <= 0) return 1.0f
        return (totalActiveUsers.toFloat() / totalPhysicalCapacity)
    }

    fun calculateDailyCost(totalActiveUsers: Int): Double {
        val baseHosting = 15.0 // basic network & domain
        val rackMaintenance = (rackCapacity / 10000) * 8.0
        val excessUsers = (totalActiveUsers - totalPhysicalCapacity).coerceAtLeast(0)
        val cloudBurst = if (isCloudAutoScaleEnabled) (excessUsers / 1000.0) * cloudUnitDailyCost else 0.0
        return baseHosting + rackMaintenance + cloudBurst
    }
}
