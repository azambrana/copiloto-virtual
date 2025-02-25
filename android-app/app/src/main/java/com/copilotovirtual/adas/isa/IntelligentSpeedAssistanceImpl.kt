package com.copilotovirtual.adas.isa

import android.content.Context
import android.location.Location
import android.util.Log
import com.copilotovirtual.MainActivity
import com.copilotovirtual.data.model.DistanceState
import com.copilotovirtual.data.model.LocationState
import com.copilotovirtual.data.model.SpeedLimitState
import com.copilotovirtual.data.model.SpeedState
import com.copilotovirtual.utils.GPSManager
import com.copilotovirtual.utils.SoundPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IntelligentSpeedAssistanceImpl
    (
    context: Context,
    speedLimitListener: SpeedLimitListener
) : IntelligentSpeedAssistance(context, speedLimitListener) {
    private var lastLocation: Location = Location("")
    private var gpsManager: GPSManager
    private val loopInterval: Long = 2000L
    private var soundPlayer: SoundPlayer
    private var isLoopRunning: Boolean = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var currentSpeedLimit = DEFAULT_SPEED_LIMIT

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        soundPlayer = SoundPlayer(context)
        gpsManager = GPSManager(context, ::onLocationReceived)
    }

    private fun startAsyncLoop() {
        isLoopRunning = true
        var checkSpeedLimitExceeded = true
        var distanceWhenSpeedLimitExceeded = 0f

        coroutineScope.launch {
            while (isLoopRunning) {
                Log.d(
                    "ISA Loop",
                    "Timestamp: ${System.currentTimeMillis()}, Speed: ${SpeedState.currentSpeed}, " +
                            "SpeedLimit: ${SpeedLimitState.currentSpeedLimit}, Distance: ${DistanceState.totalDistance}"
                )

                if (checkSpeedLimitExceeded &&
                    isSpeedLimitExceeded(SpeedState.currentSpeed, SpeedLimitState.currentSpeedLimit)) {
                    checkSpeedLimitExceeded = false
                    distanceWhenSpeedLimitExceeded = DistanceState.totalDistance
                    speedLimitListener.onSpeedLimitExceeded()

                    val message =
                        "Exceso de velocidad: ${SpeedState.currentSpeed} km/h, " +
                                "Límite: ${SpeedLimitState.currentSpeedLimit} km/h"
                    Log.d("ISA Loop", message)

                    soundPlayer.playSound(EXCESO_VELOCIDAD)
                }

                // mantener el límite dentro de los siguientes 100 metros (1 cuadra approx.)
                if (!checkSpeedLimitExceeded && DistanceState.totalDistance - distanceWhenSpeedLimitExceeded > 100) {
                    checkSpeedLimitExceeded = true
                    resetSpeedLimit()
                }

                delay(loopInterval)
            }
        }
    }

    override fun isSpeedLimitExceeded(currentSpeed: Int, speedLimit: Int): Boolean {
        return currentSpeed > speedLimit
    }

    private fun onLocationReceived(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val speed = location.speed * 3.6f // Convert to km/h

        var distance = 0f
        lastLocation?.let {
            distance = it.distanceTo(location)
            DistanceState.totalDistance += distance
        }
        lastLocation = location

        SpeedState.currentSpeed = speed.toInt()
        LocationState.latitude = latitude
        LocationState.longitude = longitude

        speedLimitListener.onSpeedChanged(speed.toInt())

        Log.d("Location", "Lat: $latitude, Lon: $longitude, Speed: $speed, Distance: $distance")
    }

    override fun start() {
        if (gpsManager.checkPermissions()) {
            gpsManager.startLocationUpdates()
        } else {
            val mainActivity = context as MainActivity
            gpsManager.requestPermissions(mainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        }
        startAsyncLoop()
    }

    override fun resetSpeedLimit() {
        speedLimitListener.onResetSpeedLimit(DEFAULT_SPEED_LIMIT)
    }
}