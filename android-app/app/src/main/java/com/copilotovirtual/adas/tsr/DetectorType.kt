package com.copilotovirtual.adas.tsr

/**
 * Clase que define los tipos de detectores de objetos.
 * @see ObjectDetector
 * @see TrafficSignRecognizerFactory
 * @see TrafficSignRecognizerService
 * @author Alvaro Zambrana Sejas
 * @since 0.4
 */
enum class DetectorType {
    YOLOv8,
    YOLOv10,
    YOLOv11
}