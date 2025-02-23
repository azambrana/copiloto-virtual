package com.copilotovirtual.adas.tsr

import android.graphics.Bitmap
import com.copilotovirtual.data.model.BoundingBox

/**
 * Interface para detectar objetos en un frame
 *
 * @see BoundingBox
 * @author Alvaro Zambrana Sejas
 * @since 0.4
 */
interface ObjectDetector {
    /**
     * Procesa un fotograma y devuelve una lista de objetos detectados.
     * @param frame El fotograma a procesar.
     * @return Una lista de objetos detectados.
     */
    fun detect(frame: Bitmap): List<BoundingBox>
}