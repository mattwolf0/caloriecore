package app.caloriecore.ui.screens

import androidx.compose.runtime.mutableStateOf
import app.caloriecore.ui.model.Sex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileSetupStateTest {
    @Test
    fun buildsProfileFromValidInputs() {
        val setup = profileSetupForTest()
        setup.sex = Sex.Female
        setup.updateAge("31")
        setup.updateHeight("168")
        setup.updateWeight("64,5")

        assertTrue(setup.canSave)
        val profile = setup.buildProfile(loggedAt = 123L)

        assertEquals(123L, profile.loggedAt)
        assertEquals(Sex.Female, profile.sex)
        assertEquals(31, profile.age)
        assertEquals(168, profile.heightCm)
        assertEquals(64.5, profile.weightKg, 0.0)
        assertNull(profile.bodyFatPercent)
    }

    @Test
    fun rejectsValuesOutsideProfileLimits() {
        val setup = profileSetupForTest()
        setup.sex = Sex.Male
        setup.updateAge("12")
        setup.updateHeight("99")
        setup.updateWeight("24")
        setup.updateBodyFat("90")

        assertFalse(setup.canSave)
        assertTrue(setup.ageInvalid)
        assertTrue(setup.heightInvalid)
        assertTrue(setup.weightInvalid)
        assertTrue(setup.bodyFatInvalid)
        assertTrue(setup.hasInvalidValues)
    }

    @Test
    fun requiresSexAndMainBodyValues() {
        val setup = profileSetupForTest()

        assertFalse(setup.canSave)
        assertFalse(setup.hasInvalidValues)

        setup.sex = Sex.Male
        setup.updateAge("29")
        setup.updateHeight("178")
        setup.updateWeight("82")
        setup.updateBodyFat("18.5")

        assertTrue(setup.canSave)
        assertEquals(18.5, setup.buildProfile(50L).bodyFatPercent ?: 0.0, 0.0)
    }

    private fun profileSetupForTest(): ProfileSetupState = ProfileSetupState(
        sexState = mutableStateOf<Sex?>(null),
        ageInputState = mutableStateOf(""),
        heightInputState = mutableStateOf(""),
        weightInputState = mutableStateOf(""),
        bodyFatInputState = mutableStateOf("")
    )
}
