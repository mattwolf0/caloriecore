package app.caloriecore.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.caloriecore.data.steps.PhoneStepCounter
import app.caloriecore.data.steps.StepCounter
import app.caloriecore.ui.model.PhoneStepState
import app.caloriecore.ui.model.PhoneStepStatus
import kotlinx.coroutines.delay

internal data class PhoneStepsControl(
    val state: PhoneStepState
)

internal class PhoneStepsController(
    private val phoneStepCounter: StepCounter
) {
    private var startInProgress = false

    var state by mutableStateOf(
        PhoneStepState(
            steps = phoneStepCounter.cachedStepsToday(),
            status = if (phoneStepCounter.isAvailable) {
                PhoneStepStatus.Starting
            } else {
                PhoneStepStatus.NotAvailable
            }
        )
    )
        private set

    fun start(hasPermission: Boolean) {
        when {
            !phoneStepCounter.isAvailable -> {
                startInProgress = false
                state = state.copy(status = PhoneStepStatus.NotAvailable)
            }
            !hasPermission -> {
                startInProgress = false
                state = state.copy(status = PhoneStepStatus.PermissionNeeded)
            }
            state.status == PhoneStepStatus.Active -> {
                refreshSteps()
            }
            !startInProgress -> {
                startRecording()
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            startRecording()
        } else {
            startInProgress = false
            state = state.copy(status = PhoneStepStatus.PermissionNeeded)
        }
    }

    fun refreshSteps() {
        if (state.status != PhoneStepStatus.Active) return
        phoneStepCounter.readToday(::handleStepResult)
    }

    private fun startRecording() {
        startInProgress = true
        state = state.copy(status = PhoneStepStatus.Starting)
        phoneStepCounter.enable(::handleStepResult)
    }

    private fun handleStepResult(result: Result<Int>) {
        startInProgress = false
        state = result.fold(
            onSuccess = { steps ->
                PhoneStepState(steps = steps, status = PhoneStepStatus.Active)
            },
            onFailure = {
                state.copy(status = PhoneStepStatus.Error)
            }
        )
    }
}

@SuppressLint("InlinedApi")
@Composable
internal fun rememberPhoneSteps(): PhoneStepsControl {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    val phoneStepCounter = remember { PhoneStepCounter(context) }
    val controller = remember { PhoneStepsController(phoneStepCounter) }
    var appStarted by remember { mutableStateOf(false) }
    var permissionAsked by rememberSaveable { mutableStateOf(false) }

    val stepPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        controller.onPermissionResult(granted)
    }

    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, _ ->
            appStarted = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        }
        lifecycle.addObserver(observer)
        appStarted = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(appStarted) {
        if (appStarted) {
            val hasPermission = context.canReadPhoneSteps()
            controller.start(hasPermission)
            if (
                !hasPermission &&
                controller.state.status != PhoneStepStatus.NotAvailable &&
                !permissionAsked
            ) {
                permissionAsked = true
                stepPermissionRequest.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    }

    LaunchedEffect(appStarted, controller.state.status) {
        while (appStarted) {
            when (controller.state.status) {
                PhoneStepStatus.Active -> {
                    delay(StepRefreshMillis)
                    controller.refreshSteps()
                }
                PhoneStepStatus.Error -> {
                    delay(StepRetryMillis)
                    controller.start(context.canReadPhoneSteps())
                }
                else -> break
            }
        }
    }

    return PhoneStepsControl(
        state = controller.state
    )
}

private const val StepRefreshMillis = 15_000L
private const val StepRetryMillis = 30_000L

private fun Context.canReadPhoneSteps(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
