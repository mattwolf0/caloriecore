package app.caloriecore.ui.text

import app.caloriecore.ui.model.BmiCategory
import app.caloriecore.ui.model.NutritionGoal
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
    val gender: String,
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
    val customFood: String,
    val foodBarcode: String,
    val foodPhoto: String,
    val foodSearch: String,
    val imageRecognition: String,
    val comingSoon: String,
    val imageFoodSoon: String,
    val barcode: String,
    val findProductByBarcode: String,
    val lookingUp: String,
    val productLoaded: String,
    val calorieApiAttribution: String,
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
    val goal: String = "Goal",
    val buildMuscle: String = "Build muscle",
    val loseFat: String = "Lose fat",
    val maintainWeight: String = "Maintain weight",
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
    val monthWindow: String = "Last 30 days",
    val chartEmpty: String = "No entries in this period.",
    val chartHint: String = "Tap the chart or use the arrows to pick a day.",
    val bodyStats: String = "Body",
    val periodSummary: String = "Period summary",
    val workouts: String = "Workouts",
    val daysLogged: String = "days logged",
    val progressDataHint: String = "Averages use days with entries. Missing values stay blank.",
    val openDay: String = "Open day",
    val energyTrend: String = "Intake and burn",
    val balanceHint: String = "Intake − burn",
    val weightTrend: String = "Body weight trend",
    val trainingTrend: String = "Training volume",
    val average: String = "Average",
    val change: String = "Change",
    val createPlan: String = "Create plan",
    val close: String = "Close",
    val planPreview: String = "Plan preview",
    val addThisExercise: String = "Add this exercise",
    val cancel: String = "Cancel",
    val deleteConfirmTitle: String = "Delete entry?",
    val deleteConfirmText: String = "This cannot be undone.",
    val clearDataConfirmTitle: String = "Clear all local data?",
    val clearDataConfirmText: String = "Meals, activities, workouts, plans and profile data will be deleted. Language and theme will stay.",
    val missingMacroData: String = "Some macro values are missing.",
    val showDetails: String = "Show details",
    val hideDetails: String = "Hide details",
    val dailyBalance: String = "Balance",
    val previousDay: String = "Previous day",
    val nextDay: String = "Next day",
    val phoneStepsStarting: String = "Starting...",
    val stepPermissionNeeded: String = "Allow activity access to track steps.",
    val stepCounterUnavailable: String = "Step tracking is not available.",
    val stepCounterError: String = "Could not start. Try again.",
    val activity: String,
    val loggedActivity: String,
    val addActivity: String,
    val updateActivity: String,
    val searchActivity: String,
    val activityList: String,
    val otherActivity: String,
    val otherActivityName: String,
    val minutes: String,
    val estimatedBurn: String,
    val weightUsed: String,
    val noActivities: String,
    val activityDoubleCountWarning: String,
    val activityCatalogSource: String,
    val legalAndDataSources: String,
    val legalInfoText: String,
    val dataSources: String,
    val softwareLicenses: String,
    val serviceTerms: String,
    val back: String,
    val view: String,
    val openLink: String,
    val compendiumLegalText: String,
    val openFoodFactsLegalText: String,
    val calorieApiLegalText: String,
    val androidLibrariesLegalText: String,
    val kotlinLegalText: String,
    val googleServicesLegalText: String,
    val profileSetupTitle: String = "Set up your profile",
    val profileSetupIntro: String = "We use these details to estimate your daily calorie burn. You can change them later.",
    val profileSetupRequired: String = "Fields marked with * are required.",
    val profileSetupBodyFatHint: String = "Body fat is optional. Add it if you know it.",
    val profileSetupInvalid: String = "Check the entered values.",
    val profileSetupContinue: String = "Save and continue",
    val save: String = "Save",
    val discardChanges: String = "Discard changes?",
    val discardChangesText: String = "Your changes have not been saved.",
    val discard: String = "Discard",
    val keepEditing: String = "Keep editing",
    val noMealsLogged: String = "No food logged yet.",
    val burnShare: String = "Intake / burn",
    val foodDigestion: String = "Food digestion",
    val startWorkout: String = "Log workout",
    val planSaveHint: String = "Enter a plan name and add at least one exercise.",
    val exerciseInputHint: String = "Enter a name, sets and reps. Leave weight empty for bodyweight exercises.",
    val workoutSaveHint: String = "Enter a title and add at least one set.",
    val periodEnding: String = "Period ending",
    val selectedDay: String = "Selected day",
    val loggedDaysAverage: String = "Average of logged days",
    val noActivityResults: String = "No matching activities.",
    val activityMinutesHint: String = "Enter 1–1440 minutes.",
    val changeActivity: String = "Change activity",
    val chartZero: String = "All logged values are 0.",
    val foodSaveHint: String = "Enter a name, a serving above 0 g and calories. Macros are optional."
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

    fun goalDisplayName(goal: NutritionGoal): String = when (goal) {
        NutritionGoal.BuildMuscle -> buildMuscle
        NutritionGoal.LoseFat -> loseFat
        NutritionGoal.MaintainWeight -> maintainWeight
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
