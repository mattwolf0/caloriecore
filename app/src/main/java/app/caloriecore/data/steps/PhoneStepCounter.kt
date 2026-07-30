package app.caloriecore.data.steps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.edit
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.fitness.FitnessLocal
import com.google.android.gms.fitness.LocalRecordingClient
import com.google.android.gms.fitness.data.LocalDataType
import com.google.android.gms.fitness.request.LocalDataReadRequest
import app.caloriecore.ui.model.logDateText
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

internal interface StepCounter {
    val isAvailable: Boolean

    fun cachedStepsToday(): Int

    fun enable(onFinished: (Result<Int>) -> Unit)

    fun readToday(onFinished: (Result<Int>) -> Unit)
}

internal class PhoneStepCounter(context: Context) : StepCounter {
    private val appContext = context.applicationContext
    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val recordingClient = FitnessLocal.getLocalRecordingClient(appContext)
    private val settings = appContext.getSharedPreferences(SettingsName, Context.MODE_PRIVATE)

    override val isAvailable: Boolean
        get() {
            val hasStepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
            val hasRecordingApi = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                appContext,
                LocalRecordingClient.LOCAL_RECORDING_CLIENT_STEPS_MIN_VERSION_CODE
            ) == ConnectionResult.SUCCESS
            return hasStepSensor && hasRecordingApi
        }

    override fun cachedStepsToday(): Int {
        val today = logDateText(System.currentTimeMillis())
        return if (settings.getString(CacheDayKey, null) == today) {
            settings.getInt(CacheStepsKey, 0)
        } else {
            0
        }
    }

    @SuppressLint("MissingPermission")
    override fun enable(onFinished: (Result<Int>) -> Unit) {
        if (!isAvailable) {
            onFinished(Result.failure(IllegalStateException("Step recording is not available.")))
            return
        }
        if (!hasActivityPermission()) {
            onFinished(Result.failure(SecurityException("Activity permission is missing.")))
            return
        }
        try {
            recordingClient.subscribe(LocalDataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener {
                    readToday(onFinished)
                }
                .addOnFailureListener { error ->
                    onFinished(Result.failure(error))
                }
        } catch (error: SecurityException) {
            onFinished(Result.failure(error))
        }
    }

    @SuppressLint("MissingPermission")
    override fun readToday(onFinished: (Result<Int>) -> Unit) {
        if (!isAvailable) {
            onFinished(Result.failure(IllegalStateException("Step recording is not available.")))
            return
        }
        if (!hasActivityPermission()) {
            onFinished(Result.failure(SecurityException("Activity permission is missing.")))
            return
        }
        val now = ZonedDateTime.now()
        val dayStart = now.toLocalDate().atStartOfDay(now.zone)
        val request = LocalDataReadRequest.Builder()
            .aggregate(LocalDataType.TYPE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(dayStart.toEpochSecond(), now.toEpochSecond(), TimeUnit.SECONDS)
            .build()

        try {
            recordingClient.readData(request)
                .addOnSuccessListener { response ->
                    val stepValues = response.buckets
                        .flatMap { it.dataSets }
                        .flatMap { it.dataPoints }
                        .mapNotNull { point ->
                            point.dataType.fields.firstOrNull()?.let { field ->
                                point.getValue(field).asInt()
                            }
                        }
                    val steps = totalStepCount(stepValues)
                    saveCache(steps)
                    onFinished(Result.success(steps))
                }
                .addOnFailureListener { error ->
                    onFinished(Result.failure(error))
                }
        } catch (error: SecurityException) {
            onFinished(Result.failure(error))
        }
    }

    private fun saveCache(steps: Int) {
        settings.edit {
            putString(CacheDayKey, logDateText(System.currentTimeMillis()))
            putInt(CacheStepsKey, steps)
        }
    }

    private fun hasActivityPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED

    private companion object {
        const val SettingsName = "phone_step_settings"
        const val CacheDayKey = "cache_day"
        const val CacheStepsKey = "cache_steps"
    }
}

internal fun totalStepCount(stepValues: List<Int>): Int = stepValues
    .fold(0L) { total, value -> total + value.coerceAtLeast(0) }
    .coerceAtMost(Int.MAX_VALUE.toLong())
    .toInt()
