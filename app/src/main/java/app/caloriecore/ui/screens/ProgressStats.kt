package app.caloriecore.ui.screens

internal class ProgressStats(days: List<ProgressDay>) {
    val foodDays = days.count { it.intakeKcal != null }
    val bodyDays = days.count { it.weightKg != null }
    val stepDays = days.count { it.steps != null }
    val averageIntake = loggedAverage(days.map { it.intakeKcal?.toDouble() })
    val averageBalance = loggedAverage(days.map { it.balanceKcal?.toDouble() })
    val averageSleep = loggedAverage(days.map { it.sleepHours })
    val averageSteps = loggedAverage(days.map { it.steps?.toDouble() })
    val activityCalories = days.sumOf { it.activityKcal ?: 0 }
    val workouts = days.sumOf { it.workoutCount }
    val sets = days.sumOf { it.setCount }
    val weightChange = days.mapNotNull { it.weightKg }.let { weights ->
        if (weights.size < 2) null else weights.last() - weights.first()
    }
}

private fun loggedAverage(values: List<Double?>): Double? = values.filterNotNull()
    .filter { it.isFinite() }
    .takeIf { it.isNotEmpty() }
    ?.average()
