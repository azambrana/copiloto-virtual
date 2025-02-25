package com.copilotovirtual.data.model

/**
 * Clase que implementa la interfaz BoundingBox y define los atributos de una caja delimitadora.
 *
 * @param x1 Coordenada x1 de la caja delimitadora.
 * @param y1 Coordenada y1 de la caja delimitadora.
 * @param x2 Coordenada x2 de la caja delimitadora.
 * @param y2 Coordenada y2 de la caja delimitadora.
 * @param cx Coordenada x del centro de la caja delimitadora.
 * @param cy Coordenada y del centro de la caja delimitadora.
 * @param w Ancho de la caja delimitadora.
 * @param h Alto de la caja delimitadora.
 * @param cnf Confianza de la detección, un valor entre 0 y 1.
 * @param cls Clase de la detección.
 * @param clsName Nombre de la clase de la detección.
 *
 * @author Alvaro Zambrana Sejas
 * @version 0.4
 */
data class FullBoundingBox(
    override val x1: Float,
    override val y1: Float,
    override val x2: Float,
    override val y2: Float,
    override val cx: Float,
    override val cy: Float,
    override val w: Float,
    override val h: Float,
    override val cnf: Float,
    override val cls: Int,
    override val clsName: String
): BoundingBox