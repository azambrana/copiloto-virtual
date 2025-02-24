package com.copilotovirtual.adas.isa

import com.copilotovirtual.data.model.TrafficSign

/**
 * Interfaz que define los métodos que deben implementar los listeners de señales de límite de velocidad.
 * @see TrafficSign
 *
 * @author Alvaro Zambrana Sejas
 * @since 0.4
 */
interface SpeedLimitListener {
    /**
     * Notifica que se ha detectado una señal de límite de velocidad.
     * @param trafficSign La señal de límite de velocidad detectada.
     */
    fun onSpeedLimitDetected(trafficSign: TrafficSign)
    fun onSpeedLimitExceeded()
    fun onSpeedChanged(speed: Int)
}