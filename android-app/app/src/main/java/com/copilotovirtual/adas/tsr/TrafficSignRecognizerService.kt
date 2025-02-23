package com.copilotovirtual.adas.tsr

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
interface TrafficSignRecognizerService {

    /**
     * Procesa un fotograma y devuelve una lista de señales de tráfico detectadas.
     * @param bitmap El fotograma a procesar.
     * @return Una lista de señales de tráfico detectadas.
     */
    fun processFrame(bitmap: Bitmap): List<TrafficSign> {
        return emptyList()
    }
}