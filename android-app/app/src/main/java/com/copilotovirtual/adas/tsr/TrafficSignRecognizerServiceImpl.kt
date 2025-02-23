package com.copilotovirtual.adas.tsr

import android.content.Context
import android.graphics.Bitmap
import com.copilotovirtual.data.model.TrafficSign

/**
 * Implementación de [TrafficSignRecognizerService] que utiliza un [TrafficSignRecognizer] para reconocer señales de tráfico.
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
class TrafficSignRecognizerServiceImpl(context: Context, detectorType: DetectorType) :
    TrafficSignRecognizerService {

    private var  recognizer : TrafficSignRecognizer? = null

    init {
        recognizer = TrafficSignRecognizerFactory.create(context, detectorType)
    }

    override fun processFrame(bitmap: Bitmap): List<TrafficSign> {
        return recognizer?.detectTrafficSigns(bitmap) ?: emptyList()
    }
}