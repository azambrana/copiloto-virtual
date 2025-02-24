package com.copilotovirtual.adas.isa

import android.content.Context
import android.location.Location
const val DEFAULT_SPEED_LIMIT = 40

abstract class IntelligentSpeedAssistanceService (
    protected var context: Context,
    protected val speedLimitListener: SpeedLimitListener
) {
    protected var lastLocation: Location = Location("")
    protected var speedLimit: Int = DEFAULT_SPEED_LIMIT
    protected var currentSpeed: Int = 0
    protected var distance: Int = 0

    fun resetDistance() {
        distance = 0
    }

    fun resetSpeedLimit() {
        speedLimit = DEFAULT_SPEED_LIMIT
    }

    abstract fun checkSpeedLimit()
}