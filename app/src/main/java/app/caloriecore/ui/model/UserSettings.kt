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

data class UserPreferences(
    val language: UiLanguage = UiLanguage.System,
    val themeMode: UiThemeMode = UiThemeMode.System
)
