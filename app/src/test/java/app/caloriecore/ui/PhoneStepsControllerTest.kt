package app.caloriecore.ui

import app.caloriecore.data.steps.StepCounter
import app.caloriecore.ui.model.PhoneStepStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneStepsControllerTest {
    @Test
    fun startsWhenPermissionIsReady() {
        val stepCounter = FakeStepCounter(cachedSteps = 600)
        stepCounter.enableResult = Result.success(720)
        val controller = PhoneStepsController(stepCounter)

        controller.start(hasPermission = true)

        assertEquals(1, stepCounter.enableCalls)
        assertEquals(720, controller.state.steps)
        assertEquals(PhoneStepStatus.Active, controller.state.status)
    }

    @Test
    fun waitsForPermission() {
        val stepCounter = FakeStepCounter()
        val controller = PhoneStepsController(stepCounter)

        controller.start(hasPermission = false)

        assertEquals(0, stepCounter.enableCalls)
        assertEquals(PhoneStepStatus.PermissionNeeded, controller.state.status)

        controller.onPermissionResult(granted = true)

        assertEquals(1, stepCounter.enableCalls)
        assertEquals(PhoneStepStatus.Active, controller.state.status)
    }

    @Test
    fun showsWhenStepTrackingIsNotAvailable() {
        val stepCounter = FakeStepCounter(isAvailable = false)
        val controller = PhoneStepsController(stepCounter)

        controller.start(hasPermission = true)

        assertEquals(0, stepCounter.enableCalls)
        assertEquals(PhoneStepStatus.NotAvailable, controller.state.status)
    }

    @Test
    fun canRetryAfterStartError() {
        val stepCounter = FakeStepCounter()
        stepCounter.enableResult = Result.failure(IllegalStateException("Not ready"))
        val controller = PhoneStepsController(stepCounter)

        controller.start(hasPermission = true)

        assertEquals(PhoneStepStatus.Error, controller.state.status)

        stepCounter.enableResult = Result.success(900)
        controller.start(hasPermission = true)

        assertEquals(2, stepCounter.enableCalls)
        assertEquals(900, controller.state.steps)
        assertEquals(PhoneStepStatus.Active, controller.state.status)
    }

    @Test
    fun refreshesTheStepCount() {
        val stepCounter = FakeStepCounter()
        val controller = PhoneStepsController(stepCounter)
        controller.start(hasPermission = true)
        stepCounter.readResult = Result.success(1_050)

        controller.refreshSteps()

        assertEquals(1, stepCounter.readCalls)
        assertEquals(1_050, controller.state.steps)
        assertEquals(PhoneStepStatus.Active, controller.state.status)
    }
}

private class FakeStepCounter(
    override val isAvailable: Boolean = true,
    private val cachedSteps: Int = 0
) : StepCounter {
    var enableCalls = 0
    var readCalls = 0
    var enableResult: Result<Int> = Result.success(cachedSteps)
    var readResult: Result<Int> = Result.success(cachedSteps)

    override fun cachedStepsToday(): Int = cachedSteps

    override fun enable(onFinished: (Result<Int>) -> Unit) {
        enableCalls += 1
        onFinished(enableResult)
    }

    override fun readToday(onFinished: (Result<Int>) -> Unit) {
        readCalls += 1
        onFinished(readResult)
    }
}
