package com.copilotovirtual.adas.tsr

import com.copilotovirtual.data.model.BoundingBox

/**
 * Interface para detectar objetos en un frame y notificar al listener
 * @see BoundingBox
 * @author Alvaro Zambrana Sejas
 * @since 0.4
 */
interface DetectorListener {
    /**
     * Notifica al listener que no se detectaron objetos
     */
    fun onEmptyDetect()

    /**
     * Notifica al listener que se detectaron objetos en el frame y el tiempo de inferencia
     * @param boundingBoxes Lista de objetos detectados
     * @param inferenceTime Tiempo de inferencia
     * @see BoundingBox
     */
    fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long)
}