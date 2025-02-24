package com.copilotovirtual.adas.tsr.yolo

import com.copilotovirtual.data.model.BoundingBox

/**
 * Clase que representa el resultado de la detección de objetos.
 * @param boundingBoxes Lista de cajas delimitadoras de los objetos detectados.
 * @param inferenceTime Tiempo de inferencia en milisegundos.
 *
 * @see BoundingBox
 *
 * @author Alvaro Zambrana Sejas
 * @since 0.4
 */
data class ObjectDetectorResults(
    val boundingBoxes: List<BoundingBox>,
    val inferenceTime: Long
)
