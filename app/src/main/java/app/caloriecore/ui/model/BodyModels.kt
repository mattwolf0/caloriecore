package app.caloriecore.ui.model

private const val SoloProfileId = "local-user"

enum class Sex {
    Male,
    Female
}

data class BodySnapshot(
    val id: Long = newLogId(),
    val loggedAt: Long = phoneNowMillis(),
    val userId: String = SoloProfileId,
    val sex: Sex = Sex.Male,
    val age: Int = 29,
    val heightCm: Int = 178,
    val weightKg: Double = 82.0,
    val bodyFatPercent: Double? = null,
    val sleepHours: Double = 7.2,
    val watchActiveCalories: Int = 0,
    val steps: Int = 0,
    val plannedWorkoutCalories: Int = 0,
    val restingHeartRate: Int = 60
)
