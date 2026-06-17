package app.caloriecore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
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
import app.caloriecore.ui.components.LogCard
import app.caloriecore.ui.components.LogMomentPicker
import app.caloriecore.ui.components.ScreenName
import app.caloriecore.ui.components.ShelfHeader
import app.caloriecore.ui.model.Logbook
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.foodRowsForPickedDay
import app.caloriecore.ui.model.pickedDayReport
import app.caloriecore.ui.text.CalorieCoreStrings
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

    var lookupBusy by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(logbook.selectedDateTime) {
        editor.followSelectedMoment(logbook.selectedDateTime)
    }

    LaunchedEffect(
        editor.draft.grams,
        editor.draft.keepsOffPer100Base,
        editor.draft.productKcalPer100g,
        editor.draft.productProteinPer100g,
        editor.draft.productCarbsPer100g,
        editor.draft.productFatPer100g
    ) {
        editor.refreshProductTotals()
    }

    fun fetchScannedFood(code: String) {
        foodScreenScope.launch {
            lookupBusy = true
            editor.lookupNote = null
            foodFacts.lookupBarcode(code)
                .onSuccess { editor.useOffProduct(it, strings.productLoaded) }
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
        val typedFood = editor.draft.offSearchText.trim()
        if (typedFood.length < 2) {
            editor.offShelf = emptyList()
            editor.hasSearchedOff = typedFood.isNotEmpty()
            editor.lookupNote = if (typedFood.isEmpty()) null else strings.invalidSearch
            return
        }
        foodScreenScope.launch {
            lookupBusy = true
            editor.hasSearchedOff = true
            editor.lookupNote = null
            foodFacts.searchFoods(typedFood)
                .onSuccess { shelf ->
                    editor.offShelf = shelf
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
                editor.updateBarcode(scannedCode)
                editor.lookupNote = strings.barcodeScanned
                fetchScannedFood(scannedCode)
            },
            onUserCanceled = { editor.lookupNote = strings.scanCanceled },
            onScannerFailure = { editor.lookupNote = strings.scannerUnavailable }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { ScreenName(title = strings.foodTitle) }
        item {
            LogCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShelfHeader(strings.selectedMoment)
                    LogMomentPicker(
                        pickedMillis = logbook.selectedDateTime,
                        onValueChange = onSelectedDateTimeChange,
                        dateLabel = strings.date,
                        timeLabel = strings.time,
                        todayText = strings.todayButton
                    )
                }
            }
        }
        item {
            FoodMacroSummary(
                nutrition = pickedDayNutrition,
                strings = strings
            )
        }
        item {
            FoodEntryEditor(
                editor = editor,
                strings = strings,
                lookupBusy = lookupBusy,
                selectedDateTime = logbook.selectedDateTime,
                onScanBarcode = ::scanBarcode,
                onLookupBarcode = { fetchScannedFood(editor.draft.barcode) },
                onSearchFood = ::searchOffShelf,
                onSaveFoodEntry = onSaveFoodEntry
            )
        }
        mealRows(
            meals = mealsForPickedDay,
            strings = strings,
            onEdit = editor::edit,
            onRemove = { entry -> onRemoveFoodEntry(entry.id) }
        )
        item { Spacer(modifier = Modifier.height(54.dp)) }
    }
}
