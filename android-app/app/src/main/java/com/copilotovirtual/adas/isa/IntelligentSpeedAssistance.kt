package com.copilotovirtual.adas.isa

import android.content.Context
import com.copilotovirtual.data.model.TrafficSign

/**
 * Interface para el Asistente de Velocidad Inteligente (ISA).
 *
 * El ISA procesa las señales de límite de velocidad detectadas y las compara con la velocidad
 * actual estimada del vehículo.
 *
 * @author Alvaro Zambrana Sejas
 * @version 0.4
 */
abstract class IntelligentSpeedAssistance (
    protected val context: Context,
    protected val speedLimitListener: SpeedLimitListener
) {
    /**
     * Inicia el ISA.
     */
    abstract fun start()

    /**
     * Retrieves the last speed limit detected by the system.
     * @return The last detected speed limit in km/h, or null if none detected.
     */
    abstract fun getLastSpeedLimit(): Int?

    /**
     * Clears the last detected speed limit.
     */
    abstract fun resetSpeedLimit()

    /**
     * Procesa la señal de límite de velocidad detectada y la compara con la velocidad actual del vehículo.
     * @param currentSpeed  Velocidad actual del vehículo en km/h.
     * @param speedLimit Velocidad límite detectada en km/h.
     */
    abstract fun isSpeedLimitExceeded(currentSpeed: Int, speedLimit: Int): Boolean

}
