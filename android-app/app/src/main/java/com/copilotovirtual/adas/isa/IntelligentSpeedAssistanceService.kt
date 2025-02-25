package com.copilotovirtual.adas.isa

import android.content.Context
import com.copilotovirtual.data.model.TrafficSign

const val DEFAULT_SPEED_LIMIT = 40
const val SPEED_LIMIT_PREFFIX = "limite-velocidad-"

abstract class IntelligentSpeedAssistanceService (
    protected var context: Context,
    protected val speedLimitListener: SpeedLimitListener
) {
    protected var speedLimit: Int = DEFAULT_SPEED_LIMIT

    /**
     * Reinicia el límite de velocidad detectado.
     */
    fun resetSpeedLimit() {
        speedLimit = DEFAULT_SPEED_LIMIT
        speedLimitListener.onResetSpeedLimit(speedLimit)
    }

    /**
     * Procesa la señal de límite de velocidad detectada y la compara con la velocidad actual del vehículo.
     * @param trafficSign Señal de límite de velocidad detectada, o null si no hay ninguna.
     */
    abstract fun processSpeedLimitTrafficSign(trafficSign: TrafficSign)
}