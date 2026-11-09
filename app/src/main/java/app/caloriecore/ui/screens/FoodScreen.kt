package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.caloriecore.data.FoodBarcodeReader
import app.caloriecore.data.FoodFactsClient
import app.caloriecore.ui.components.ConfirmActionDialog
import app.caloriecore.ui.components.DayPicker
import app.caloriecore.ui.components.FormSheet
import app.caloriecore.ui.components.PagePadding
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.SolidActionButton
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.foodRowsForPickedDay
import app.caloriecore.ui.model.newEntryMoment
import app.caloriecore.ui.model.pickedDayReport
import app.caloriecore.ui.text.CalorieCoreStrings
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun FoodScreen(
    logbook: Logbook,
    strings: CalorieCoreStrings,
    onSelectedDateTimeChange: (Long) -> Unit,
    onSaveFoodEntry: (FoodEntry) -> Unit,
    onRemoveFoodEntry: (Long) -> Unit
) {
    val mealsForPickedDay = logbook.foodRowsForPickedDay()
    val pickedDayNutrition = logbook.pickedDayReport().nutrition
    val editor = rememberFoodEditorState(logbook.selectedDateTime)
    val context = LocalContext.current
    val barcodeReader = remember { FoodBarcodeReader(context) }
    val foodFacts = remember { FoodFactsClient() }
    val foodScreenScope = rememberCoroutineScope()

    var lookupBusy by remember { mutableStateOf(false) }
    var lookupJob by remember { mutableStateOf<Job?>(null) }
    var isEditorOpen by rememberSaveable { mutableStateOf(false) }
    var foodPage by rememberSaveable { mutableStateOf(FoodAddPage.Options) }
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
                    foodPage = FoodAddPage.Custom
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
                if (isEditorOpen && foodPage == FoodAddPage.Barcode) {
                    editor.updateBarcode(scannedCode)
                    editor.lookupNote = strings.barcodeScanned
                    fetchScannedFood(scannedCode)
                }
            },
            onUserCanceled = {
                if (isEditorOpen && foodPage == FoodAddPage.Barcode) editor.lookupNote = strings.scanCanceled
            },
            onScannerFailure = {
                if (isEditorOpen && foodPage == FoodAddPage.Barcode) editor.lookupNote = strings.scannerUnavailable
            }
        )
    }

    if (isEditorOpen) {
        FormSheet(
            title = when (foodPage) {
                FoodAddPage.Options -> strings.addMeal
                FoodAddPage.Custom -> if (editor.isEditing) strings.updateMeal else strings.newMeal
                FoodAddPage.Barcode -> strings.barcode
                FoodAddPage.Image -> strings.imageRecognition
                FoodAddPage.Search -> strings.searchFood
            },
            closeText = strings.close,
            contentKey = foodPage,
            onDismiss = {
                lookupJob?.cancel()
                lookupBusy = false
                editor.clear(logbook.selectedDateTime)
                isEditorOpen = false
                foodPage = FoodAddPage.Options
            }
        ) {
            if (foodPage != FoodAddPage.Options) {
                TextButton(onClick = {
                    lookupJob?.cancel()
                    lookupBusy = false
                    editor.lookupNote = null
                    foodPage = FoodAddPage.Options
                }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.back)
                }
            }
            when (foodPage) {
                FoodAddPage.Options -> FoodAddOptions(strings = strings, onPick = { foodPage = it })
                FoodAddPage.Custom -> FoodEntryEditor(
                    editor = editor,
                    strings = strings,
                    selectedDateTime = logbook.selectedDateTime,
                    onSaveFoodEntry = onSaveFoodEntry,
                    onClose = { isEditorOpen = false }
                )
                FoodAddPage.Image -> FoodPhotoSoon(strings)
                FoodAddPage.Barcode, FoodAddPage.Search -> FoodLookupBox(
                    editor = editor,
                    strings = strings,
                    page = foodPage,
                    lookupBusy = lookupBusy,
                    onScanBarcode = ::scanBarcode,
                    onLookupBarcode = { fetchScannedFood(editor.draft.barcode) },
                    onSearchFood = ::searchOffShelf,
                    onSelectProduct = {
                        editor.useLookupProduct(it, strings.productLoaded)
                        foodPage = FoodAddPage.Custom
                    }
                )
            }
        }
    }

    LazyColumn(
        contentPadding = PagePadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenName(title = strings.foodTitle) }
        item {
            DayPicker(
                pickedMillis = logbook.selectedDateTime,
                onValueChange = onSelectedDateTimeChange,
                dateLabel = strings.date,
                todayText = strings.todayButton,
                previousDayText = strings.previousDay,
                nextDayText = strings.nextDay
            )
        }
        item {
            FoodMacroSummary(
                nutrition = pickedDayNutrition,
                hasMissingMacros = mealsForPickedDay.any {
                    !it.proteinKnown || !it.carbsKnown || !it.fatKnown
                },
                strings = strings
            )
        }
        item {
            SolidActionButton(
                text = strings.addMeal,
                icon = Icons.Rounded.Add,
                onClick = {
                    editor.clear(newEntryMoment(logbook.selectedDateTime))
                    foodPage = FoodAddPage.Options
                    isEditorOpen = true
                }
            )
        }
        mealRows(
            meals = mealsForPickedDay,
            strings = strings,
            onEdit = { entry ->
                editor.edit(entry)
                foodPage = FoodAddPage.Custom
                isEditorOpen = true
            },
            onRemove = { entry -> deleteMealId = entry.id }
        )
    }
}
