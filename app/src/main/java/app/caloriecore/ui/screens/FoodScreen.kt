package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import app.caloriecore.data.FoodBarcodeReader
import app.caloriecore.data.FoodFactsClient
import app.caloriecore.ui.components.ConfirmActionDialog
import app.caloriecore.ui.components.DayPicker
import app.caloriecore.ui.components.EditorPage
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.macroTargetFor
import app.caloriecore.ui.model.pickedDayReport
import app.caloriecore.ui.text.CalorieCoreStrings
import app.caloriecore.ui.theme.UiSpacing
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun FoodScreen(
    logbook: Logbook,
    strings: CalorieCoreStrings,
    onSelectedDateTimeChange: (Long) -> Unit,
    onSaveFoodEntry: (FoodEntry) -> Unit,
    onRemoveFoodEntry: (Long) -> Unit,
    openEditorOnStart: Boolean = false,
    onEditorOpenChange: (Boolean) -> Unit = {}
) {
    val report = remember(logbook) { logbook.pickedDayReport() }
    val mealsForPickedDay = report.foodEntries
    val pickedDayNutrition = report.nutrition
    val target = remember(report.profile, report.activities, logbook.settings.nutritionGoal) {
        macroTargetFor(report.profile, report.activities, logbook.settings.nutritionGoal)
    }
    val editor = rememberFoodEditorState(logbook.selectedDateTime)
    val context = LocalContext.current
    val barcodeReader = remember { FoodBarcodeReader(context) }
    val foodFacts = remember { FoodFactsClient() }
    val foodScreenScope = rememberCoroutineScope()

    var lookupBusy by remember { mutableStateOf(false) }
    var lookupJob by remember { mutableStateOf<Job?>(null) }
    var isEditorOpen by rememberSaveable { mutableStateOf(openEditorOnStart) }
    var foodMethod by rememberSaveable { mutableStateOf<FoodAddMethod?>(null) }
    var deleteMealId by rememberSaveable { mutableStateOf<Long?>(null) }

    if (deleteMealId != null) {
        ConfirmActionDialog(
            title = strings.deleteConfirmTitle,
            message = strings.deleteConfirmText,
            confirmText = strings.delete,
            cancelText = strings.cancel,
            onConfirm = {
                deleteMealId?.let(onRemoveFoodEntry)
                deleteMealId = null
            },
            onDismiss = { deleteMealId = null }
        )
    }

    LaunchedEffect(logbook.selectedDateTime) {
        editor.followSelectedMoment(logbook.selectedDateTime)
    }

    LaunchedEffect(
        editor.draft.grams,
        editor.draft.keepsProductPer100Base,
        editor.draft.productKcalPer100g,
        editor.draft.productProteinPer100g,
        editor.draft.productCarbsPer100g,
        editor.draft.productFatPer100g
    ) {
        editor.refreshProductTotals()
    }

    fun fetchScannedFood(code: String) {
        if (lookupBusy) return
        lookupJob = foodScreenScope.launch {
            lookupBusy = true
            editor.lookupNote = null
            foodFacts.lookupBarcode(code)
                .onSuccess { food ->
                    val loadedMessage = if ("calorieapi" in food.source) {
                        "${strings.productLoaded} · ${strings.calorieApiAttribution}"
                    } else {
                        strings.productLoaded
                    }
                    editor.useLookupProduct(food, loadedMessage)
                    foodMethod = FoodAddMethod.Custom
                }
                .onFailure { error ->
                    editor.lookupNote = when (error.message) {
                        FoodFactsClient.InvalidBarcode -> strings.invalidBarcode
                        FoodFactsClient.NotFound -> strings.productNotFound
                        else -> strings.productLoadFailed
                    }
                }
            lookupBusy = false
        }
    }

    fun searchOffShelf() {
        if (lookupBusy) return
        val typedFood = editor.draft.offSearchText.trim()
        if (typedFood.length < 2) {
            editor.offShelf = emptyList()
            editor.hasSearchedOff = typedFood.isNotEmpty()
            editor.lookupNote = if (typedFood.isEmpty()) null else strings.invalidSearch
            return
        }
        lookupJob = foodScreenScope.launch {
            lookupBusy = true
            editor.offShelf = emptyList()
            editor.hasSearchedOff = true
            editor.lookupNote = null
            foodFacts.searchFoods(typedFood)
                .onSuccess { shelf ->
                    editor.offShelf = shelf.sortedBy { it.name.lowercase() }
                    editor.lookupNote = if (shelf.isEmpty()) strings.productNotFound else null
                }
                .onFailure { error ->
                    editor.offShelf = emptyList()
                    editor.lookupNote = when (error.message) {
                        FoodFactsClient.SearchTooShort -> strings.invalidSearch
                        else -> strings.productLoadFailed
                    }
                }
            lookupBusy = false
        }
    }

    fun scanBarcode() {
        barcodeReader.scan(
            onBarcodeDetected = { scannedCode ->
                if (isEditorOpen && foodMethod == FoodAddMethod.Barcode) {
                    editor.updateBarcode(scannedCode)
                    editor.lookupNote = strings.barcodeScanned
                    fetchScannedFood(scannedCode)
                }
            },
            onUserCanceled = {
                if (isEditorOpen && foodMethod == FoodAddMethod.Barcode) editor.lookupNote = strings.scanCanceled
            },
            onScannerFailure = {
                if (isEditorOpen && foodMethod == FoodAddMethod.Barcode) editor.lookupNote = strings.scannerUnavailable
            }
        )
    }

    if (isEditorOpen) {
        EditorPage(
            strings = strings,
            hasChanges = editor.draft != FoodDraft(mealLoggedAt = logbook.selectedDateTime),
            onClose = {
                lookupJob?.cancel()
                lookupBusy = false
                editor.clear(logbook.selectedDateTime)
                isEditorOpen = false
            },
            onEditorOpenChange = onEditorOpenChange,
            scrollKey = foodMethod
        ) { cancel ->
            FoodEntryEditor(
                editor = editor, strings = strings, lookupBusy = lookupBusy,
                method = foodMethod,
                onMethodChange = {
                    lookupJob?.cancel()
                    lookupBusy = false
                    editor.lookupNote = null
                    foodMethod = it
                },
                onScanBarcode = ::scanBarcode,
                onLookupBarcode = { fetchScannedFood(editor.draft.barcode) },
                onSearchFood = ::searchOffShelf,
                onSelectProduct = {
                    lookupJob?.cancel()
                    lookupBusy = false
                    editor.useLookupProduct(it, strings.productLoaded)
                    foodMethod = FoodAddMethod.Custom
                },
                onSaveFoodEntry = { entry ->
                    onSaveFoodEntry(entry)
                    editor.clear(logbook.selectedDateTime)
                    isEditorOpen = false
                },
                onClose = cancel
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(UiSpacing.page),
        verticalArrangement = Arrangement.spacedBy(UiSpacing.section)
    ) {
        item { ScreenName(title = strings.foodTitle) }
        item {
            LogCard {
                DayPicker(
                    pickedMillis = logbook.selectedDateTime,
                    onValueChange = onSelectedDateTimeChange,
                    dateLabel = strings.date,
                    todayText = strings.todayButton,
                    previousDayText = strings.previousDay,
                    nextDayText = strings.nextDay
                )
            }
        }
        item {
            FoodMacroSummary(
                nutrition = pickedDayNutrition,
                target = target,
                hasMissingMacros = mealsForPickedDay.any {
                    !it.proteinKnown || !it.carbsKnown || !it.fatKnown
                },
                strings = strings
            )
        }
        item {
            SolidActionButton(
                text = strings.addMeal,
                onClick = {
                    editor.clear(logbook.selectedDateTime)
                    foodMethod = null
                    isEditorOpen = true
                }
            )
        }
        mealRows(
            meals = mealsForPickedDay,
            strings = strings,
            onEdit = { entry ->
                editor.edit(entry)
                foodMethod = FoodAddMethod.Custom
                isEditorOpen = true
            },
            onRemove = { entry -> deleteMealId = entry.id }
        )
    }
}
