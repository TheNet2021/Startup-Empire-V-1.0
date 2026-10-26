package com.example.model

enum class EmployeeRole(
    val title: String,
    val primarySkill: String,
    val description: String,
    val baseSalary: Double
) {
    DEVELOPER(
        title = "Software Engineer",
        primarySkill = "Coding",
        description = "Builds frontend & backend product features.",
        baseSalary = 120.0
    ),
    DESIGNER(
        title = "UI/UX Designer",
        primarySkill = "Design",
        description = "Crafts intuitive layouts and user experiences.",
        baseSalary = 100.0
    ),
    QA_TESTER(
        title = "QA Tester",
        primarySkill = "Testing",
        description = "Discovers and squashes product bugs.",
        baseSalary = 80.0
    ),
    MARKETER(
        title = "Growth Marketer",
        primarySkill = "Marketing",
        description = "Acquires new active users and launches campaigns.",
        baseSalary = 110.0
    ),
    SYSADMIN(
        title = "DevOps Sysadmin",
        primarySkill = "Infrastructure",
        description = "Keeps servers online and prevents outages.",
        baseSalary = 130.0
    ),
    PROJECT_MANAGER(
        title = "Project Manager",
        primarySkill = "Management",
        description = "Coordinates sprints and boosts team productivity.",
        baseSalary = 140.0
    )
}

enum class EmployeeState {
    WORKING,
    COFFEE_BREAK,
    BRAINSTORMING,
    RESTING,
    EXHAUSTED
}

data class Employee(
    val id: String,
    val name: String,
    val role: EmployeeRole,
    val level: Int = 1,
    val skill: Int = 30, // 10 to 100
    val speed: Int = 40, // 10 to 100
    val dailySalary: Double = role.baseSalary,
    val morale: Int = 85, // 0 to 100
    val energy: Int = 90, // 0 to 100
    val assignedProductId: String? = null,
    val assignedContractId: String? = null,
    val assignedDeskId: String? = null,
    val state: EmployeeState = EmployeeState.WORKING,
    val hairColor: Long = 0xFF4A3728,
    val shirtColor: Long = 0xFF2563EB,
    val skinTone: Long = 0xFFFFDBAC,
    val totalWorkOutput: Long = 0
) {
    val levelTitle: String get() = when (level) {
        1 -> "Junior"
        2 -> "Mid-level"
        3 -> "Senior"
        4 -> "Staff"
        else -> "Principal"
    }

    val hiringCost: Double get() = (dailySalary * 2.5 + (level * 150.0)).coerceAtLeast(250.0)

    val effectiveProductivity: Float get() {
        val moraleFactor = (morale / 100f).coerceIn(0.2f, 1.3f)
        val energyFactor = (energy / 100f).coerceIn(0.1f, 1.2f)
        val levelFactor = 1.0f + (level - 1) * 0.35f
        return (skill / 100f) * (speed / 100f) * moraleFactor * energyFactor * levelFactor
    }
}
