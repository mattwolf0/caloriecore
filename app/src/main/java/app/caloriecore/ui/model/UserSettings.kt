package app.caloriecore.ui.model

enum class UiLanguage {
    System,
    Hungarian,
    English,
    German
}

enum class UiThemeMode {
    System,
    Light,
    Dark
}

enum class NutritionGoal {
    BuildMuscle,
    LoseFat,
    MaintainWeight
}

data class UserPreferences(
    val language: UiLanguage = UiLanguage.System,
    val themeMode: UiThemeMode = UiThemeMode.System,
    val nutritionGoal: NutritionGoal = NutritionGoal.MaintainWeight,
    val profileSetupComplete: Boolean = false
)
