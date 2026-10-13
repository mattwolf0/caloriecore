package app.caloriecore.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
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
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.caloriecore.data.steps.PhoneStepCounter
import app.caloriecore.data.steps.StepCounter
import app.caloriecore.ui.model.PhoneStepState
import app.caloriecore.ui.model.PhoneStepStatus
import app.caloriecore.ui.model.logDateText
import app.caloriecore.ui.model.phoneNowMillis
import kotlinx.coroutines.delay

internal data class PhoneStepsControl(
    val state: PhoneStepState,
    val requestPermission: () -> Unit,
    val openSettings: () -> Unit,
    val retry: () -> Unit
)

internal class PhoneStepsController(
    private val phoneStepCounter: StepCounter,
    private val today: () -> String = { logDateText(phoneNowMillis()) }
) {
    private var startInProgress = false

    var state by mutableStateOf(
        PhoneStepState(
            steps = phoneStepCounter.cachedStepsToday(),
            day = today(),
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
        val day = today()
        phoneStepCounter.readToday { handleStepResult(it, day) }
    }

    private fun startRecording() {
        if (startInProgress) return
        startInProgress = true
        state = state.copy(status = PhoneStepStatus.Starting)
        val day = today()
        phoneStepCounter.enable { handleStepResult(it, day) }
    }

    private fun handleStepResult(result: Result<Int>, day: String) {
        startInProgress = false
        if (day != today()) {
            state = PhoneStepState(day = today())
            startRecording()
            return
        }
        state = result.fold(
            onSuccess = { steps ->
                PhoneStepState(steps = steps, status = PhoneStepStatus.Active, day = day)
            },
            onFailure = {
                state.copy(status = PhoneStepStatus.Error)
            }
        )
    }
}

@SuppressLint("InlinedApi")
@Composable
internal fun rememberPhoneSteps(enabled: Boolean = true): PhoneStepsControl {
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

    LaunchedEffect(appStarted, enabled) {
        if (appStarted && enabled) {
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

    LaunchedEffect(appStarted, enabled, controller.state.status) {
        while (appStarted && enabled) {
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
        state = controller.state,
        requestPermission = {
            if (context.canReadPhoneSteps()) {
                controller.start(true)
            } else {
                stepPermissionRequest.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        },
        openSettings = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${context.packageName}".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        },
        retry = { controller.start(context.canReadPhoneSteps()) }
    )
}

private const val StepRefreshMillis = 15_000L
private const val StepRetryMillis = 30_000L

private fun Context.canReadPhoneSteps(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACTIVITY_RECOGNITION
    ) == PackageManager.PERMISSION_GRANTED
