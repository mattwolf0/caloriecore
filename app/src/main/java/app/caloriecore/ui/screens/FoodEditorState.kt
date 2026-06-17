package app.caloriecore.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.caloriecore.ui.components.keepLogNumberText
import app.caloriecore.ui.format.CalorieCoreFormatter
import app.caloriecore.ui.model.FoodEntry
import app.caloriecore.ui.model.FoodProduct
import app.caloriecore.ui.model.newLogId
import kotlin.math.roundToInt

internal data class FoodDraft(
    val mealBeingEditedId: Long? = null,
    val mealLoggedAt: Long,
    val name: String = "",
    val barcode: String = "",
    val grams: String = "100",
    val kcal: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val keepsOffPer100Base: Boolean = false,
    val productKcalPer100g: Double = 0.0,
    val productProteinPer100g: Double = 0.0,
    val productCarbsPer100g: Double = 0.0,
    val productFatPer100g: Double = 0.0,
    val offSearchText: String = ""
)

internal class FoodEditorState internal constructor(
    private val draftState: MutableState<FoodDraft>,
    private val lookupNoteState: MutableState<String?>,
    private val hasSearchedOffState: MutableState<Boolean>,
    private val offShelfState: MutableState<List<FoodProduct>>
) {
    var draft by draftState
        private set

    var lookupNote by lookupNoteState
    var hasSearchedOff by hasSearchedOffState
    var offShelf by offShelfState

    val isEditing: Boolean
        get() = draft.mealBeingEditedId != null

    val canSave: Boolean
        get() = draft.name.isNotBlank() && draft.kcal.isNotBlank()

    fun followSelectedMoment(selectedDateTime: Long) {
        if (draft.mealBeingEditedId == null) changeLoggedAt(selectedDateTime)
    }

    fun changeLoggedAt(nextLoggedAt: Long) {
        draft = draft.copy(mealLoggedAt = nextLoggedAt)
    }

    fun hideOffShelf() {
        offShelf = emptyList()
        hasSearchedOff = false
        draft = draft.copy(offSearchText = "")
    }

    fun clear(selectedDateTime: Long) {
        draft = FoodDraft(mealLoggedAt = selectedDateTime)
        lookupNote = null
        hideOffShelf()
    }

    fun edit(entry: FoodEntry) {
        draft = FoodDraft(
            mealBeingEditedId = entry.id,
            mealLoggedAt = entry.loggedAt,
            name = entry.name,
            barcode = entry.barcode,
            grams = entry.servingGrams.toString(),
            kcal = entry.calories.toString(),
            protein = CalorieCoreFormatter.logDecimal(entry.proteinGrams),
            carbs = CalorieCoreFormatter.logDecimal(entry.carbGrams),
            fat = CalorieCoreFormatter.logDecimal(entry.fatGrams)
        )
        lookupNote = null
        hideOffShelf()
    }

    fun useOffProduct(offFood: FoodProduct, loadedMessage: String) {
        draft = FoodDraft(
            mealBeingEditedId = draft.mealBeingEditedId,
            mealLoggedAt = draft.mealLoggedAt,
            name = offFood.name,
            barcode = offFood.code,
            grams = "100",
            kcal = offFood.kcalPer100g.toString(),
            protein = CalorieCoreFormatter.logDecimal(offFood.proteinPer100g),
            carbs = CalorieCoreFormatter.logDecimal(offFood.carbsPer100g),
            fat = CalorieCoreFormatter.logDecimal(offFood.fatPer100g),
            keepsOffPer100Base = true,
            productKcalPer100g = offFood.kcalPer100g.toDouble(),
            productProteinPer100g = offFood.proteinPer100g,
            productCarbsPer100g = offFood.carbsPer100g,
            productFatPer100g = offFood.fatPer100g
        )
        lookupNote = loadedMessage
        hideOffShelf()
    }

    fun updateFoodName(nextName: String) {
        draft = draft.copy(name = nextName)
        if (nextName.isBlank()) {
            lookupNote = null
            draft = draft.copy(keepsOffPer100Base = false)
            hideOffShelf()
        }
    }

    fun updateBarcode(nextBarcode: String) {
        draft = draft.copy(barcode = keepLogNumberText(nextBarcode))
    }

    fun updateSearchText(nextSearchText: String) {
        draft = draft.copy(offSearchText = nextSearchText)
        if (nextSearchText.isBlank()) {
            offShelf = emptyList()
            hasSearchedOff = false
        }
    }

    fun updateServing(nextServing: String) {
        draft = draft.copy(grams = keepLogNumberText(nextServing))
    }

    fun updateCalories(nextCalories: String) {
        draft = draft.copy(
            keepsOffPer100Base = false,
            kcal = keepLogNumberText(nextCalories)
        )
    }

    fun updateProtein(nextProtein: String) {
        draft = draft.copy(
            keepsOffPer100Base = false,
            protein = keepLogNumberText(nextProtein, allowDecimal = true)
        )
    }

    fun updateCarbs(nextCarbs: String) {
        draft = draft.copy(
            keepsOffPer100Base = false,
            carbs = keepLogNumberText(nextCarbs, allowDecimal = true)
        )
    }

    fun updateFat(nextFat: String) {
        draft = draft.copy(
            keepsOffPer100Base = false,
            fat = keepLogNumberText(nextFat, allowDecimal = true)
        )
    }

    fun refreshProductTotals() {
        if (!draft.keepsOffPer100Base) return
        draft.grams.toIntOrNull()?.takeIf { it >= 0 }?.let { grams ->
            // OFF values are per 100 g, so scale them by grams.
            val scale = grams / 100.0
            draft = draft.copy(
                kcal = (draft.productKcalPer100g * scale).roundToInt().toString(),
                protein = CalorieCoreFormatter.logDecimal(draft.productProteinPer100g * scale),
                carbs = CalorieCoreFormatter.logDecimal(draft.productCarbsPer100g * scale),
                fat = CalorieCoreFormatter.logDecimal(draft.productFatPer100g * scale)
            )
        }
    }

    fun buildEntry(): FoodEntry = FoodEntry(
        id = draft.mealBeingEditedId ?: newLogId(),
        loggedAt = draft.mealLoggedAt,
        name = draft.name.trim(),
        barcode = draft.barcode,
        servingGrams = draft.grams.toIntOrNull() ?: 100,
        calories = draft.kcal.toIntOrNull() ?: 0,
        proteinGrams = draft.protein.toDoubleOrNull() ?: 0.0,
        carbGrams = draft.carbs.toDoubleOrNull() ?: 0.0,
        fatGrams = draft.fat.toDoubleOrNull() ?: 0.0
    )
}

private val FoodDraftStateSaver = listSaver<MutableState<FoodDraft>, Any>(
    save = { state ->
        val draft = state.value
        listOf(
            draft.mealBeingEditedId != null,
            draft.mealBeingEditedId ?: 0L,
            draft.mealLoggedAt,
            draft.name,
            draft.barcode,
            draft.grams,
            draft.kcal,
            draft.protein,
            draft.carbs,
            draft.fat,
            draft.keepsOffPer100Base,
            draft.productKcalPer100g,
            draft.productProteinPer100g,
            draft.productCarbsPer100g,
            draft.productFatPer100g,
            draft.offSearchText
        )
    },
    restore = { values ->
        mutableStateOf(
            FoodDraft(
                mealBeingEditedId = if (values[0] as Boolean) values[1] as Long else null,
                mealLoggedAt = values[2] as Long,
                name = values[3] as String,
                barcode = values[4] as String,
                grams = values[5] as String,
                kcal = values[6] as String,
                protein = values[7] as String,
                carbs = values[8] as String,
                fat = values[9] as String,
                keepsOffPer100Base = values[10] as Boolean,
                productKcalPer100g = values[11] as Double,
                productProteinPer100g = values[12] as Double,
                productCarbsPer100g = values[13] as Double,
                productFatPer100g = values[14] as Double,
                offSearchText = values[15] as String
            )
        )
    }
)

@Composable
internal fun rememberFoodEditorState(selectedDateTime: Long): FoodEditorState {
    val draftState = rememberSaveable(saver = FoodDraftStateSaver) {
        mutableStateOf(FoodDraft(mealLoggedAt = selectedDateTime))
    }
    val lookupNote = rememberSaveable { mutableStateOf<String?>(null) }
    val hasSearchedOff = rememberSaveable { mutableStateOf(false) }
    val offShelf = remember { mutableStateOf(emptyList<FoodProduct>()) }

    return remember {
        FoodEditorState(
            draftState = draftState,
            lookupNoteState = lookupNote,
            hasSearchedOffState = hasSearchedOff,
            offShelfState = offShelf
        )
    }
}
