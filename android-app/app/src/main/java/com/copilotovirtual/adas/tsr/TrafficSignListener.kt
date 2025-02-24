package com.copilotovirtual.adas.tsr

interface TrafficSignListener {
    /**
     * Notifica que se ha detectado una señal de tráfico.
     * @param trafficSignRecognizerResults Los resultados de la detección de señales de tráfico.
     */
    fun onTrafficSignsDetected(trafficSignRecognizerResults: TrafficSignRecognizerResults)
}