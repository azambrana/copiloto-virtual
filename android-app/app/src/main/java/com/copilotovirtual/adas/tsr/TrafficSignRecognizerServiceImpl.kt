package com.copilotovirtual.adas.tsr

import android.content.Context
import android.graphics.Bitmap
import com.copilotovirtual.data.model.TrafficSign

/**
 * Implementación de [TrafficSignRecognizerService] que utiliza un [TrafficSignRecognizer] para
 * detectar señales de tránsito.
 *
 * @see TrafficSignRecognizer
 * @see TrafficSignRecognizerService
 * @see DetectorType
 * @see TrafficSignRecognizerFactory
 * @see TrafficSign
 * @see Bitmap
 *
 * @param context Contexto de la aplicación.
 * @param detectorType Tipo de detector a utilizar.
 *
 * @author Alvaro Zambrana Sejas
 * @since 0.4
 */
class TrafficSignRecognizerServiceImpl(
    context: Context,
    detectorType: DetectorType,
    trafficSignListener: TrafficSignListener
) :
    TrafficSignRecognizerService(context, detectorType, trafficSignListener) {

    private var recognizer: TrafficSignRecognizer? = null

    init {
        recognizer = TrafficSignRecognizerFactory.create(context, detectorType, trafficSignListener)
    }

    override fun processFrame(bitmap: Bitmap): TrafficSignRecognizerResults {
        return recognizer?.detectTrafficSigns(bitmap) ?: TrafficSignRecognizerResults()
    }
}