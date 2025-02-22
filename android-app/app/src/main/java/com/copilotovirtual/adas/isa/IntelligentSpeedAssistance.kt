package com.copilotovirtual.adas.isa

import com.copilotovirtual.data.model.TrafficSign

/**
 * Interface para el Asistente de Velocidad Inteligente (ISA).
 *
 * El ISA procesa las señales de límite de velocidad detectadas y las compara con la velocidad actual estimada del vehículo.
 *
 * @author Alvaro Zambrana Sejas
 * @version 0.4
 */
interface IntelligentSpeedAssistance {
    /**
     * Procesa la señal de límite de velocidad detectada y la compara con la velocidad actual del vehículo.
     * @param detectedSign Señal de límite de velocidad detectada, o null si no hay ninguna.
     * @param currentSpeed Velocidad actual del vehículo en km/h.
     */
    fun processSpeedLimit(detectedSign: TrafficSign?, currentSpeed: Float)

    /**
     * Retrieves the last speed limit detected by the system.
     * @return The last detected speed limit in km/h, or null if none detected.
     */
    fun getLastSpeedLimit(): Int?

    /**
     * Clears the last detected speed limit.
     */
    fun resetSpeedLimit()
}
