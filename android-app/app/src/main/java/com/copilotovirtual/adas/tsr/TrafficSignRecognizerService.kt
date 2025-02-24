package com.copilotovirtual.adas.tsr

import android.content.Context
import android.graphics.Bitmap
import com.copilotovirtual.data.model.TrafficSign

/**
 * Interface para reconocer señales de tráfico en un frame
 * @see TrafficSign
 * @see Bitmap
 *
 * @author Alvaro Zambrana Sejas
 * @since 0.4
 */
abstract class TrafficSignRecognizerService (
    private val context: Context,
    private val detectorType: DetectorType,
    private val trafficSignListener: TrafficSignListener
) {

    /**
     * Procesa un fotograma y devuelve una lista de señales de tráfico detectadas.
     * @param bitmap El fotograma a procesar.
     * @return Una lista de señales de tráfico detectadas.
     */
    abstract fun processFrame(bitmap: Bitmap): TrafficSignRecognizerResults
}