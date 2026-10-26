package com.example.model

enum class GameSpeed(val multiplier: Float, val label: String) {
    PAUSED(0f, "Paused"),
    NORMAL(1f, "1x"),
    FAST(2.5f, "2.5x"),
    ULTRA(5f, "5x")
}

data class GameClock(
    val day: Int = 1,
    val hour: Int = 9,
    val totalHoursElapsed: Long = 0,
    val speed: GameSpeed = GameSpeed.NORMAL
) {
    val month: Int get() = ((day - 1) / 30) + 1
    val dayOfMonth: Int get() = ((day - 1) % 30) + 1
    val formattedDate: String get() = "Month $month, Day $dayOfMonth - %02d:00".format(hour)
    val shortTime: String get() = "D$day %02d:00".format(hour)
}
