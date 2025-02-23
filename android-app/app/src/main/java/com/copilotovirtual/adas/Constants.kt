package com.copilotovirtual.adas

/**
 * Clase que contiene las constantes de la aplicación.
 * Modelos exportados en formato float16 para mejor rendimiento en dispositivos móviles.
 *
 * @since 0.4
 * @author Alvaro Zambrana Sejas
 */
object Constants {
    /**
     * Ruta del modelo de detección de señales de tráfico YOLOv8.
     */
    const val MODEL_PATH_YOLOv8 = "YOLOv8_cbba_best_float16.tflite"

    /**
     * Ruta del modelo de detección de señales de tráfico YOLOv10..
     */
    const val MODEL_PATH_YOLOv10 = "YOLOv10_cbba_best_float16.tflite"

    /**
     * Ruta del archivo de etiquetas.
     */
    val LABELS_PATH: String = "labels.txt"
}
