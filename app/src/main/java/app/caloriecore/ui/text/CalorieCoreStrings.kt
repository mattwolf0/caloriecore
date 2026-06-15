package app.caloriecore.ui.text

import app.caloriecore.ui.model.BmiCategory
import app.caloriecore.ui.model.Sex
import app.caloriecore.ui.model.SleepQuality
import app.caloriecore.ui.model.UiLanguage
import app.caloriecore.ui.model.UiThemeMode
import app.caloriecore.ui.navigation.CalorieCoreTab
import java.util.Locale

data class CalorieCoreStrings(
    val appName: String,
    val today: String,
    val food: String,
    val train: String,
    val progress: String,
    val settings: String,
    val todayTitle: String,
    val dailyBurn: String,
    val bmr: String,
    val watch: String,
    val training: String,
    val intake: String,
    val profileData: String,
    val saved: String,
    val breakdown: String,
    val age: String,
    val height: String,
    val weight: String,
    val bodyFat: String,
    val sleep: String,
    val activeCalories: String,
    val steps: String,
    val trainingCalories: String,
    val restingHeartRate: String,
    val baseMetabolism: String,
    val watchActivity: String,
    val plannedWorkout: String,
    val sleepCorrection: String,
    val bmrFormula: String,
    val bodyFatFormula: String,
    val mifflinFormula: String,
    val foodTitle: String,
    val calories: String,
    val protein: String,
    val carbs: String,
    val fat: String,
    val dailyMacros: String,
    val newMeal: String,
    val barcode: String,
    val findProductByBarcode: String,
    val lookingUp: String,
    val productLoaded: String,
    val invalidBarcode: String,
    val invalidSearch: String,
    val productNotFound: String,
    val productLoadFailed: String,
    val foodName: String,
    val serving: String,
    val addMeal: String,
    val updateMeal: String,
    val todaysMeals: String,
    val scanBarcode: String,
    val scanCanceled: String,
    val scannerUnavailable: String,
    val barcodeScanned: String,
    val searchFood: String,
    val foodSearchText: String,
    val foodSearchResults: String,
    val noFoodSearchResults: String,
    val selectProduct: String,
    val trainTitle: String,
    val volume: String,
    val sets: String,
    val reps: String,
    val load: String,
    val todayWorkout: String,
    val workSets: String,
    val trainingPlans: String,
    val planName: String,
    val addPlan: String,
    val updatePlan: String,
    val editPlan: String,
    val startPlan: String,
    val noPlans: String,
    val planExerciseName: String,
    val addPlanExercise: String,
    val trainingSession: String,
    val sessionTitle: String,
    val saveWorkout: String,
    val updateWorkout: String,
    val editWorkout: String,
    val addSet: String,
    val noSets: String,
    val progressTitle: String,
    val bodyWeight: String,
    val noEntries: String,
    val settingsTitle: String,
    val language: String,
    val theme: String,
    val system: String,
    val data: String,
    val privacyText: String,
    val resetData: String,
    val light: String,
    val dark: String,
    val male: String,
    val female: String,
    val low: String,
    val normal: String,
    val elevated: String,
    val high: String,
    val veryLowSleep: String,
    val lowSleep: String,
    val stableSleep: String,
    val highSleep: String,
    val bmi: String,
    val macroTarget: String,
    val logged: String,
    val items: String,
    val exercises: String,
    val delete: String,
    val edit: String,
    val clear: String,
    val duplicate: String,
    val date: String,
    val time: String,
    val selectedMoment: String,
    val entryTime: String,
    val todayButton: String = "Today",
    val planned: String = "Plan",
    val completedToday: String = "Completed today",
    val doneToday: String = "Done today",
    val trendWindow: String = "Last 7 days",
    val weightTrend: String = "Body weight trend",
    val calorieTrend: String = "Energy balance",
    val trainingTrend: String = "Training volume",
    val average: String = "Average",
    val change: String = "Change",
    val createPlan: String = "Create plan",
    val close: String = "Close",
    val planPreview: String = "Plan preview",
    val addThisExercise: String = "Add this exercise"
) {
    fun dockLabel(tab: CalorieCoreTab): String = when (tab) {
        CalorieCoreTab.Today -> today
        CalorieCoreTab.Food -> food
        CalorieCoreTab.Train -> train
        CalorieCoreTab.Progress -> progress
        CalorieCoreTab.Settings -> settings
    }

    fun languageDisplayName(language: UiLanguage): String = when (language) {
        UiLanguage.System -> system
        UiLanguage.Hungarian -> "Magyar"
        UiLanguage.English -> "English"
        UiLanguage.German -> "Deutsch"
    }

    fun themeDisplayName(themeMode: UiThemeMode): String = when (themeMode) {
        UiThemeMode.System -> system
        UiThemeMode.Light -> light
        UiThemeMode.Dark -> dark
    }

    fun sexDisplayName(sex: Sex): String = when (sex) {
        Sex.Male -> male
        Sex.Female -> female
    }

    fun bmiCategoryDisplayName(category: BmiCategory): String = when (category) {
        BmiCategory.Low -> low
        BmiCategory.Normal -> normal
        BmiCategory.Elevated -> elevated
        BmiCategory.High -> high
    }

    fun sleepQualityDisplayName(quality: SleepQuality): String = when (quality) {
        SleepQuality.VeryLow -> veryLowSleep
        SleepQuality.Low -> lowSleep
        SleepQuality.Stable -> stableSleep
        SleepQuality.High -> highSleep
    }
}

fun resolveCalorieCoreLanguage(language: UiLanguage, locale: Locale = Locale.getDefault()): UiLanguage =
    if (language != UiLanguage.System) {
        language
    } else {
        when (locale.language.lowercase(Locale.ROOT)) {
            "hu" -> UiLanguage.Hungarian
            "de" -> UiLanguage.German
            else -> UiLanguage.English
        }
    }

fun calorieCoreStrings(language: UiLanguage, locale: Locale = Locale.getDefault()): CalorieCoreStrings =
    when (resolveCalorieCoreLanguage(language, locale)) {
        UiLanguage.System -> englishStrings
        UiLanguage.Hungarian -> hungarianStrings
        UiLanguage.English -> englishStrings
        UiLanguage.German -> germanStrings
    }
