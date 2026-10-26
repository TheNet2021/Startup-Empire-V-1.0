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
    ),
    SALES(
        title = "Enterprise Sales",
        primarySkill = "Sales",
        description = "Closes high-value corporate deals and enterprise contracts.",
        baseSalary = 115.0
    )
}

enum class EmployeeTrait(
    val title: String,
    val iconEmoji: String,
    val description: String,
    val productivityMultiplier: Float = 1.0f
) {
    TEN_X_CODER(
        title = "10x Developer",
        iconEmoji = "⚡",
        description = "+35% feature coding throughput and velocity.",
        productivityMultiplier = 1.35f
    ),
    PIXEL_PERFECT(
        title = "Craftsperson",
        iconEmoji = "✨",
        description = "+25% product design quality; reduces bug generation.",
        productivityMultiplier = 1.25f
    ),
    CAFFEINE_SAVANT(
        title = "Espresso Fueled",
        iconEmoji = "☕",
        description = "Energy recharges 2x faster; +20% sprint speed.",
        productivityMultiplier = 1.20f
    ),
    TEAM_CATALYST(
        title = "Team Cheerleader",
        iconEmoji = "🎉",
        description = "Radiates +15% morale across company staff.",
        productivityMultiplier = 1.15f
    ),
    NIGHT_OWL(
        title = "Midnight Hacker",
        iconEmoji = "🌙",
        description = "Gains +30% productivity boost during evening and night.",
        productivityMultiplier = 1.25f
    ),
    DEAL_CLOSER(
        title = "Rainmaker",
        iconEmoji = "💼",
        description = "Boosts enterprise contract completion rewards by +25%.",
        productivityMultiplier = 1.25f
    ),
    BUG_HUNTER(
        title = "Eagle Eyes",
        iconEmoji = "🔍",
        description = "Discovers and resolves software bugs 50% faster.",
        productivityMultiplier = 1.30f
    ),
    RAPID_LEARNER(
        title = "Quick Study",
        iconEmoji = "📚",
        description = "Gains double skill and speed benefits from training courses.",
        productivityMultiplier = 1.20f
    )
}

enum class HairStyle(val title: String) {
    SHORT_POMPADOUR("Pompadour"),
    TEXTURED_CROP("Crop Fade"),
    SLEEK_BOB("Layered Bob"),
    CURLY_AFRO("Curly Afro"),
    PONYTAIL_BUN("High Bun"),
    SPIKY_MODERN("Textured Spikes"),
    LONG_WAVY("Flowing Waves"),
    BUZZ_FADE("Buzz Fade"),
    SIDE_BRAIDS("Side Braids"),
    MESSY_SHAG("Messy Shag")
}

enum class ClothingStyle(val title: String) {
    TECH_HOODIE("Zip Hoodie"),
    BLAZER_CREW("Blazer & Tee"),
    OXFORD_BUTTON_DOWN("Oxford Shirt"),
    COZY_TURTLENECK("Roll Turtleneck"),
    CASUAL_GRAPHIC_TEE("Graphic Tee"),
    FLANNEL_PLAID("Flannel Plaid"),
    LAYERED_CARDIGAN("Cardigan"),
    DEV_POLO("Tech Polo")
}

enum class EmployeeAccessory(val title: String) {
    NONE("None"),
    BLACK_ACETATE_GLASSES("Acetate Glasses"),
    GOLD_WIRE_GLASSES("Wireframe Glasses"),
    STUDIO_HEADPHONES("Studio Headphones"),
    AIRPOD_PROS("Wireless Earbuds"),
    STREETWEAR_CAP("Streetwear Cap"),
    HIPSTER_BEANIE("Knit Beanie"),
    SMART_WATCH("Smart Tracker")
}

enum class FacialHairStyle(val title: String) {
    NONE("Clean Shaven"),
    FIVE_O_CLOCK_SHADOW("Designer Stubble"),
    TRIMMED_BEARD("Trimmed Beard"),
    CHIN_GOATEE("Sculpted Goatee"),
    MOUSTACHE("Classic Moustache")
}

enum class EmployeeGender(val title: String) {
    MALE("Male"),
    FEMALE("Female"),
    NON_BINARY("Non-Binary")
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
    val trait: EmployeeTrait = EmployeeTrait.TEN_X_CODER,
    val assignedProductId: String? = null,
    val assignedContractId: String? = null,
    val assignedDeskId: String? = null,
    val state: EmployeeState = EmployeeState.WORKING,
    val hairColor: Long = 0xFF4A3728,
    val shirtColor: Long = 0xFF2563EB,
    val skinTone: Long = 0xFFFFDBAC,
    val hairStyle: HairStyle = HairStyle.SHORT_POMPADOUR,
    val clothingStyle: ClothingStyle = ClothingStyle.TECH_HOODIE,
    val accessory: EmployeeAccessory = EmployeeAccessory.NONE,
    val facialHair: FacialHairStyle = FacialHairStyle.NONE,
    val gender: EmployeeGender = EmployeeGender.NON_BINARY,
    val trouserColor: Long = 0xFF1E293B,
    val shoeColor: Long = 0xFFE2E8F0,
    val totalWorkOutput: Long = 0
) {
    val levelTitle: String get() = when (level) {
        1 -> "Junior"
        2 -> "Mid-level"
        3 -> "Senior"
        4 -> "Staff"
        else -> "Principal"
    }

    val specializationTitle: String get() = when (role) {
        EmployeeRole.DEVELOPER -> when (level) {
            1 -> "Junior Full-Stack"
            2 -> "Full-Stack Engineer"
            3 -> "Senior Systems Architect"
            4 -> "Staff AI Engineer"
            else -> "Principal Architect"
        }
        EmployeeRole.DESIGNER -> when (level) {
            1 -> "Junior Visual Designer"
            2 -> "UI/UX Specialist"
            3 -> "Senior Product Designer"
            4 -> "Lead Design Systems"
            else -> "VP of Product Experience"
        }
        EmployeeRole.QA_TESTER -> when (level) {
            1 -> "QA Analyst"
            2 -> "Automation Specialist"
            3 -> "Senior QA Architect"
            4 -> "Lead Security QA"
            else -> "Director of Quality"
        }
        EmployeeRole.MARKETER -> when (level) {
            1 -> "Content Associate"
            2 -> "Growth Marketer"
            3 -> "Senior Acquisition Lead"
            4 -> "Head of Performance"
            else -> "Chief Marketing Officer"
        }
        EmployeeRole.SYSADMIN -> when (level) {
            1 -> "DevOps Associate"
            2 -> "Cloud Infrastructure Eng"
            3 -> "Senior Site Reliability Eng"
            4 -> "Principal Cloud Architect"
            else -> "VP of Infrastructure"
        }
        EmployeeRole.PROJECT_MANAGER -> when (level) {
            1 -> "Agile Coordinator"
            2 -> "Scrum Master"
            3 -> "Senior Technical PM"
            4 -> "Director of Delivery"
            else -> "VP of Product Ops"
        }
        EmployeeRole.SALES -> when (level) {
            1 -> "Sales Associate"
            2 -> "Account Executive"
            3 -> "Senior Commercial Lead"
            4 -> "Enterprise Sales Director"
            else -> "Chief Revenue Officer"
        }
    }

    val currentThought: String get() = when (state) {
        EmployeeState.WORKING -> when (role) {
            EmployeeRole.DEVELOPER -> listOf("Shipping clean commits 🚀", "Optimizing DB queries ⚡", "Writing unit tests 🧪", "Refactoring architecture 🛠️", "Solving concurrency bugs 🧠").let { it[(kotlin.math.abs(id.hashCode()) + level) % it.size] }
            EmployeeRole.DESIGNER -> listOf("Perfecting layout typography ✨", "Drafting dark mode tokens 🎨", "Creating interactive prototypes 📱", "Conducting usability tests 🔍").let { it[(kotlin.math.abs(id.hashCode()) + level) % it.size] }
            EmployeeRole.QA_TESTER -> listOf("Squashing critical edge cases 🐛", "Running automated test suites 🎯", "Verifying release candidate 🛡️").let { it[(kotlin.math.abs(id.hashCode()) + level) % it.size] }
            EmployeeRole.MARKETER -> listOf("Analyzing conversion funnels 📈", "Optimizing viral ad hooks 🎯", "Engaging community feedback 💬").let { it[(kotlin.math.abs(id.hashCode()) + level) % it.size] }
            EmployeeRole.SYSADMIN -> listOf("Keeping 99.99% uptime 🟢", "Scaling Kubernetes clusters ☁️", "Tuning server throughput 🛡️").let { it[(kotlin.math.abs(id.hashCode()) + level) % it.size] }
            EmployeeRole.PROJECT_MANAGER -> listOf("Unblocking sprint deliverables 📋", "Aligning roadmap priorities 🎯", "Celebrating team wins 🎉").let { it[(kotlin.math.abs(id.hashCode()) + level) % it.size] }
            EmployeeRole.SALES -> listOf("Closing $50k enterprise deal 💼", "Demoing enterprise suite 🤝", "Nurturing corporate leads 📞").let { it[(kotlin.math.abs(id.hashCode()) + level) % it.size] }
        }
        EmployeeState.COFFEE_BREAK -> "Sipping freshly brewed espresso ☕"
        EmployeeState.BRAINSTORMING -> "Sketching out the next killer feature 💡"
        EmployeeState.RESTING -> "Recharging energy for the next sprint 🔋"
        EmployeeState.EXHAUSTED -> "Running on fumes; needs coffee or rest! 😴"
    }

    val speechBubbleIcon: String get() = when (state) {
        EmployeeState.WORKING -> when (role) {
            EmployeeRole.DEVELOPER -> "{ ; }"
            EmployeeRole.DESIGNER -> "🎨"
            EmployeeRole.QA_TESTER -> "🐛"
            EmployeeRole.MARKETER -> "📈"
            EmployeeRole.SYSADMIN -> "☁️"
            EmployeeRole.PROJECT_MANAGER -> "📋"
            EmployeeRole.SALES -> "💼"
        }
        EmployeeState.COFFEE_BREAK -> "☕"
        EmployeeState.BRAINSTORMING -> "💡"
        EmployeeState.RESTING -> "💤"
        EmployeeState.EXHAUSTED -> "⚠️"
    }

    val hiringCost: Double get() = (dailySalary * 2.5 + (level * 150.0)).coerceAtLeast(250.0)

    val promotionCost: Double get() = (dailySalary * 3.0 + (level * 250.0)).coerceAtLeast(450.0)

    val isPromotable: Boolean get() = level < 5

    val effectiveProductivity: Float get() {
        val moraleFactor = (morale / 100f).coerceIn(0.2f, 1.3f)
        val energyFactor = (energy / 100f).coerceIn(0.1f, 1.2f)
        val levelFactor = 1.0f + (level - 1) * 0.35f
        val traitBonus = trait.productivityMultiplier
        return (skill / 100f) * (speed / 100f) * moraleFactor * energyFactor * levelFactor * traitBonus
    }
}

object EmployeeAppearance {
    val SKIN_TONES = listOf(
        0xFFFFE0BDL, // Fair porcelain
        0xFFFFD1AAL, // Warm ivory
        0xFFFFDBACL, // Peach
        0xFFF1C27DL, // Golden beige
        0xFFE0AC69L, // Warm honey
        0xFFC68642L, // Olive tan
        0xFF8D5524L, // Caramel bronze
        0xFF6A3B18L, // Deep espresso
        0xFF3D2314L  // Dark ebony
    )

    val HAIR_COLORS = listOf(
        0xFF0F172AL, // Onyx jet black
        0xFF2D3748L, // Charcoal black
        0xFF4A3728L, // Deep espresso brown
        0xFF78350FL, // Chestnut brown
        0xFFB45309L, // Amber auburn
        0xFFDC2626L, // Copper red
        0xFFF59E0BL, // Honey blonde
        0xFFFEF08AL, // Platinum blonde
        0xFF94A3B8L, // Silver grey
        0xFF06B6D4L, // Neon cyan
        0xFFA855F7L, // Purple
        0xFFEC4899L  // Pastel pink
    )

    val SHIRT_COLORS = listOf(
        0xFF2563EBL, // Cobalt blue
        0xFF0284C7L, // Electric cyan
        0xFF0D9488L, // Deep teal
        0xFF10B981L, // Emerald green
        0xFF16A34AL, // Forest green
        0xFF84CC16L, // Bright lime
        0xFFEAB308L, // Mustard gold
        0xFFEA580CL, // Warm rust orange
        0xFFDC2626L, // Crimson red
        0xFFDB2777L, // Magenta rose
        0xFF8B5CF6L, // Purple iris
        0xFF475569L, // Slate tech grey
        0xFF0F172AL, // Stealth black
        0xFFF8FAFCL  // Crisp white
    )

    val TROUSER_COLORS = listOf(
        0xFF1E293BL, // Dark indigo denim
        0xFF0F172AL, // Pitch black chinos
        0xFF334155L, // Charcoal slacks
        0xFF78350FL, // Khaki chinos
        0xFF365314L, // Olive cargos
        0xFF1E3A8AL  // Classic blue jeans
    )

    val SHOE_COLORS = listOf(
        0xFFFFFFFFL, // White sneakers
        0xFF0F172AL, // Black leather
        0xFFE2E8F0L, // Slate trainers
        0xFFDC2626L, // Red accents
        0xFFD97706L  // Gum sole
    )
}
