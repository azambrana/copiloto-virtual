package com.copilotovirtual.adas.tsr

import android.content.Context

/**
 * Clase que crea instancias de reconocedores de señales de tráfico.
 * @see TrafficSignRecognizer
 * @see DetectorType
 *
 * @author Alvaro Zambrana Sejas
 * @since 0.4
 */
object TrafficSignRecognizerFactory {

    /**
     * Crea un reconocedor de señales de tráfico.
     * @param context Contexto de la aplicación.
     * @param detectorType Tipo de detector a utilizar.
     * @return Un reconocedor de señales de tráfico.
     */
    fun create(context: Context, detectorType: DetectorType): TrafficSignRecognizer {
        return TrafficSignRecognizerImpl(context = context, detectorType = detectorType)
    }
}
