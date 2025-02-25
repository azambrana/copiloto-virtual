package com.copilotovirtual.adas.tsr

import com.copilotovirtual.data.model.TrafficSign

/**
 * Clase que representa el resultado del reconocimiento de señales de tráfico.
 * @param trafficSigns Lista de señales de tráfico detectadas.
 * @param inferenceTime Tiempo de inferencia en milisegundos.
 *
 * @author Alvaro Zambrana Sejas
 * @since 0.4
 */
data class TrafficSignRecognizerResults(
    val trafficSigns: List<TrafficSign> = emptyList(),
    val inferenceTime: Long = 0
)
