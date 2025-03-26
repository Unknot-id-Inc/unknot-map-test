package com.example.mapboxtest

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.unknot.android_sdk.ForwardLocation
import org.unknot.android_sdk.SdkArgs
import org.unknot.android_sdk.ServiceState
import org.unknot.android_sdk.UnknotServiceCallback
import org.unknot.android_sdk.UnknotServiceConnection
import org.unknot.android_sdk.UnknotServiceController
import org.unknot.android_sdk.rest_api.UnknotRest
import java.io.File
import java.io.FileOutputStream
import kotlin.time.Duration.Companion.minutes

interface DeviceIdState {
    val deviceId: String
}

sealed interface MainUiState {
    data object Init : MainUiState
    data object Registering : MainUiState
    data class Ready(
        override val deviceId: String
    ) : MainUiState, DeviceIdState {
        companion object {
            fun stopFromTracking(tracking: Tracking) = Ready(
                deviceId = tracking.deviceId
            )
        }
    }
    data class Tracking(
        override val deviceId: String,
        val sessionId: String?,
        val testing: Boolean = false
    ) : MainUiState, DeviceIdState {
        companion object {
            fun startFromReady(ready: Ready, testing: Boolean = false) = Tracking(
                deviceId = ready.deviceId,
                sessionId = null,
                testing = testing
            )
        }
    }
    data class Error(val message: String, val cause: Throwable?) : MainUiState
}

class MainActivityViewModel(application: Application) : AndroidViewModel(application), UnknotServiceCallback {
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Init)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val serviceConnection = UnknotServiceConnection(this)
    private var serviceState: ServiceState? by mutableStateOf(null)
    private var serviceBound by mutableStateOf(false)
    private var batchCount by mutableIntStateOf(0)

    private val notification = ExampleNotification(getApplication())

    private lateinit var registeredDeviceId: String

    val locationProvider = UnknotLocationProvider()


    private val ctx: Context
        get() = getApplication<Application>().applicationContext

    init {
        serviceConnection.bind(getApplication())
        notification.registerChannel()

        viewModelScope.launch {
            ctx.dataStore.data.collect { prefs ->
                val deviceId: String? = prefs[DEVICE_ID_PREF]

                if (deviceId == null) {
                    registerDevice()
                } else {
                    registeredDeviceId = deviceId
                    _uiState.value = MainUiState.Ready(deviceId)
                }
            }
        }
    }

    override fun onCleared() {
        serviceConnection.unbind(getApplication())
    }

    private suspend fun registerDevice() {
        _uiState.value = MainUiState.Registering
        try {
            val restApi = UnknotRest(BuildConfig.AUTH_TARGET, BuildConfig.API_KEY)
            val deviceId = withContext(Dispatchers.IO) {
                restApi.registerDevice(ctx)
            }
            ctx.dataStore.edit { prefs ->
                prefs[DEVICE_ID_PREF] = deviceId
            }
            registeredDeviceId = deviceId
            _uiState.value = MainUiState.Ready(deviceId)
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.value = MainUiState.Error("Failed to register device", e)
        }
    }

    fun startSession() {
        val state = _uiState.value
        require(state is MainUiState.Ready) { "UI must be in Ready state to start a session" }

        val sdkArgs = SdkArgs(
            apiKey = BuildConfig.API_KEY,
            deviceId = state.deviceId,
            locationId = "",
            authTarget = BuildConfig.AUTH_TARGET,
            ingesterTarget = BuildConfig.INGESTER_TARGET,
            streamerTarget = BuildConfig.STREAM_TARGET
        )

        UnknotServiceController.startDataCollection(
            ctx = getApplication(),
            args = sdkArgs,
            notification = notification.getNotification("Session running"),
            forwardPredictions = true
        )

        //_uiState.value = MainUiState.Tracking.startFromReady(state)
    }

    fun stopSession() {
        val state = _uiState.value
        require(state is MainUiState.Tracking) { "UI must be in Tracking state to stop session" }

        UnknotServiceController.stopDataCollection(
            ctx = ctx,
            notification = notification.getNotification("Session stopped")
        )

        //_uiState.value = MainUiState.Ready.stopFromTracking(state)
    }

    fun stopTestTracking() {
        val state = _uiState.value
        require(state is MainUiState.Tracking) { "UI must be in Tracking state to stop test tracking" }
        require(state.testing) { "UI must be in testing state to stop test tracking" }

        UnknotServiceController.stopTestTrackExternalSession(getApplication(), null)

        _uiState.value = MainUiState.Ready(
            deviceId = registeredDeviceId,
        )
    }

    fun startMockTrajectory() {
        val ctx: Context = getApplication()

        // Write asset data into real file so it can be accessed by the service
        val tfile = File(ctx.cacheDir, "mock-traj.json")
        ctx.resources.assets.open(mockDataAsset).use { input ->
            FileOutputStream(tfile).use { output ->
                input.copyTo(output)
            }
        }

        UnknotServiceController.startMockTrackSession(
            ctx = ctx,
            file = tfile,
            speed = 10,
            repeat = true,
            start = 5.minutes.inWholeMilliseconds,
            end = 15.minutes.inWholeMilliseconds,
            notification = notification.getNotification("Mock session running")
        )

        _uiState.value = MainUiState.Tracking(
            deviceId = registeredDeviceId,
            sessionId = "MOCK",
            testing = true,
        )
    }


    fun stop() {
        val state = _uiState.value
        require(state is MainUiState.Tracking) { "UI must be in Tracking state to call stop" }

        if (state.testing) stopTestTracking()
        else stopSession()
    }

    override fun onUpdateServiceState(state: ServiceState) {
        serviceState = state
        val uiState = _uiState.value
        when (state) {
            is ServiceState.Running ->
                when (uiState) {
                    is MainUiState.Tracking ->
                        _uiState.value = uiState.copy(sessionId = state.sessionId)
                    is MainUiState.Ready ->
                        _uiState.value = MainUiState.Tracking.startFromReady(uiState)
                    else -> {}
                }

            is ServiceState.Error -> _uiState.value =
                MainUiState.Error(state.message ?: "Unknown error", null)
            ServiceState.Idle, ServiceState.Syncing, ServiceState.Unspecified ->
                if (uiState is MainUiState.Tracking)
                    _uiState.value = MainUiState.Ready.stopFromTracking(uiState)
        }
    }

    override fun onBound() {
        serviceBound = true
    }

    override fun onUnbound() {
        serviceBound = false
    }

    override fun onBatchUpdate(count: Int, total: Int) {
        batchCount = count
    }

    override fun onLocation(location: ForwardLocation) {
        locationProvider.updateLocation(location)
    }
}
