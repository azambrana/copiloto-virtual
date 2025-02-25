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

    /**
     * Notifica que se ha excedido el límite de velocidad.
     */
    fun onSpeedLimitExceeded()

    /**
     * Notifica que se ha cambiado la velocidad del vehículo.
     */
    fun onSpeedChanged(speed: Int)

    /**
     * Notifica que se ha restablecido el límite de velocidad.
     */
    fun onResetSpeedLimit(speed: Int)
}