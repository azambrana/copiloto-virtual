package com.copilotovirtual.data.model

/**
 * Representa una señal de tráfico detectada en una imagen.
 */
data class TrafficSign(
    /**
     * Tipo de señal detectada, los valores deben emparejar con las etiquetas del modelo.
     *
     * Ejemplo: 'zona-escolar', 'pare', 'paso-peatonal', 'ceda-el-paso', 'limite-velocidad-10', 'limite-velocidad-20', etc.
     *
     * @see LabelsLoader
     */
    val type: String,

    /**
     * Confianza de la detección, un valor entre 0 y 1.
     */
    val confidence: Float,

    /**
     * Posición de la señal en la imagen.
     */
    val position: BoundingBox?
)
