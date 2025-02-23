package com.copilotovirtual.adas.tsr

import android.content.Context
import com.copilotovirtual.adas.Constants
import com.copilotovirtual.adas.tsr.yolo.YOLOv10Detector
import com.copilotovirtual.adas.tsr.yolo.YOLOv8Detector

/**
 * Clase que crea instancias de detectores de objetos.
 *
 * @see ObjectDetector
 * @see DetectorType
 * @see TrafficSignRecognizerFactory
 * @see TrafficSignRecognizerService
 *
 * @author Alvaro Zambrana Sejas
 * @since 0.4
 */
object ObjectDetectorFactory {

    /**
     * Crea un detector de objetos.
     *
     * @param detectorType Tipo de detector a crear.
     * @param context Contexto de la aplicación.
     * @param detectorListener Listener para notificar los resultados de la detección.
     * @return Un detector de objetos.
     */
    fun create(detectorType: DetectorType, context: Context, detectorListener: DetectorListener): ObjectDetector {
        return when (detectorType) {
            DetectorType.YOLOv8 -> createYOLOv8Detector(context, detectorListener)
            DetectorType.YOLOv10 -> createYOLOv10Detector(context, detectorListener)
            DetectorType.YOLOv11 -> TODO("Not yet implemented")
        }
    }

    /**
     * Crea un detector YOLOv10.
     *
     * @param context Contexto de la aplicación.
     * @param detectorListener Listener para notificar los resultados de la detección.
     * @return Un detector YOLOv10.
     */
    private fun createYOLOv10Detector(
        context: Context,
        detectorListener: DetectorListener
    ): ObjectDetector {
        return YOLOv10Detector(
            modelPath = Constants.MODEL_PATH_YOLOv10,
            labelPath = Constants.LABELS_PATH,
            context = context,
            detectorListener = detectorListener
        )
    }

    /**
     * Crea un detector YOLOv8.
     *
     * @param context Contexto de la aplicación.
     * @param detectorListener Listener para notificar los resultados de la detección.
     * @return Un detector YOLOv8.
     */
    private fun createYOLOv8Detector(
        context: Context,
        detectorListener: DetectorListener
    ): ObjectDetector {
        return YOLOv8Detector(
            modelPath = Constants.MODEL_PATH_YOLOv8,
            labelPath = Constants.LABELS_PATH,
            context = context,
            detectorListener = detectorListener
        )
    }
}
