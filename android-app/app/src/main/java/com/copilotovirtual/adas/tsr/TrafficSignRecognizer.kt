package com.copilotovirtual.adas.tsr

import android.graphics.Bitmap
import com.copilotovirtual.data.model.TrafficSign

interface TrafficSignRecognizer {
    /**
     * Procesa un fotograma y devuelve una lista de señales de tráfico detectadas.
     * @param bitmap El fotograma a procesar.
     * @return Una lista de señales de tráfico detectadas.
     */
    fun detectTrafficSigns(bitmap: Bitmap): List<TrafficSign>

    /**
     * Retorna la última señal de tráfico detectada.
     * @return La última señal de tráfico detectada.
     */
    fun fetchLastDetectedSign(): TrafficSign?

    /**
     * Reinicia la detección.
     */
    fun resetDetection()
}
