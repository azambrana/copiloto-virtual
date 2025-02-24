package com.copilotovirtual.adas.isa

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.copilotovirtual.MainActivity
import com.copilotovirtual.R
import com.copilotovirtual.data.model.DistanceState
import com.copilotovirtual.data.model.DistanceState.totalDistance
import com.copilotovirtual.data.model.LocationState
import com.copilotovirtual.data.model.SpeedLimitState
import com.copilotovirtual.data.model.SpeedState
import com.copilotovirtual.utils.GPSManager
import com.copilotovirtual.utils.SoundPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val EXCESO_VELOCIDAD = "exceso-velocidad"

class IntelligentSpeedAssistanceServiceImpl(
    context: Context,
    speedLimitListener: SpeedLimitListener
) : IntelligentSpeedAssistanceService(context, speedLimitListener) {
    private var gpsManager: GPSManager
    private val loopInterval: Long = 2000L
    private var soundPlayer: SoundPlayer
    private var isLoopRunning: Boolean = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        speedLimit = DEFAULT_SPEED_LIMIT
        soundPlayer = SoundPlayer(context)
        // Initialize utilities
        gpsManager = GPSManager(context, ::onLocationReceived)

        // Check permissions
        if (gpsManager.checkPermissions()) {
            gpsManager.startLocationUpdates()
        } else {
            val mainActivity = context as MainActivity
            gpsManager.requestPermissions(mainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        }
        startAsyncLoop()
    }

    private fun startAsyncLoop() {
        isLoopRunning = true
        var speedLimitExceeded = false
        var distanceLimitExceeded = 0f
        coroutineScope.launch {
            while (isLoopRunning) {
                Log.d("ISA Loop", "Timestamp: ${System.currentTimeMillis()}, Speed: ${SpeedState.currentSpeed}, SpeedLimit: ${SpeedLimitState.currentSpeedLimit}, Distance: ${DistanceState.totalDistance}")

                val deltaSpeed = SpeedState.currentSpeed - SpeedLimitState.currentSpeedLimit

                if (!speedLimitExceeded && SpeedLimitState.currentSpeedLimit > 0 && deltaSpeed > 0) {
                    speedLimitExceeded = true
                    distanceLimitExceeded = DistanceState.totalDistance
                    speedLimitListener.onSpeedLimitExceeded()

                    val message = "Exceso de velocidad: ${SpeedState.currentSpeed} km/h, Límite: ${SpeedLimitState.currentSpeedLimit} km/h"
                    // toast(message)
                    Log.d("ISA Loop", message)

                    soundPlayer.playSound(EXCESO_VELOCIDAD)

                    resetSpeedLimit()
                    SpeedLimitState.currentSpeedLimit = speedLimit
                }

                // mantener el límite dentro de los siguientes 100 metros
                if (speedLimitExceeded && DistanceState.totalDistance - distanceLimitExceeded > 100) {
                    speedLimitExceeded = false
                }

                delay(loopInterval)
            }
        }
    }

    override fun checkSpeedLimit() {
        if (currentSpeed > speedLimit) {
            speedLimitListener.onSpeedLimitExceeded()
        }
    }

    private fun onLocationReceived(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val speed = location.speed * 3.6f // Convert to km/h

        var distance = 0f
        lastLocation?.let {
            distance = it.distanceTo(location)
            totalDistance += distance
        }
        lastLocation = location

        SpeedState.currentSpeed = speed.toInt()
        DistanceState.totalDistance += distance
        LocationState.latitude = latitude
        LocationState.longitude = longitude

        speedLimitListener.onSpeedChanged(speed.toInt())

        Log.d("Location", "Lat: $latitude, Lon: $longitude, Speed: $speed, Distance: $distance")
    }
}