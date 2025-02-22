package com.copilotovirtual.data.model

import kotlin.math.abs

/**
 * Clase que implementa la interfaz BoundingBox y define los atributos de una caja delimitadora.
 *
 * @param x1 Coordenada x1 de la caja delimitadora.
 * @param y1 Coordenada y1 de la caja delimitadora.
 * @param x2 Coordenada x2 de la caja delimitadora.
 * @param y2 Coordenada y2 de la caja delimitadora.
 * @param cnf Confianza de la detección, un valor entre 0 y 1.
 * @param cls Clase de la detección.
 * @param clsName Nombre de la clase de la detección.
 * @constructor Crea una caja delimitadora con los atributos especificados.
 *
 * @author Alvaro Zambrana Sejas
 * @version 0.4
 */
data class BaseBoundingBox (
    override val x1: Float,
    override val y1: Float,
    override val x2: Float,
    override val y2: Float,
    override val cx: Float = (x1 + x2) / 2,
    override val cy: Float = (y1 + y2) / 2,
    override val w: Float = abs(x2 - x1),
    override val h: Float = abs(y2 - y1),
    override val cnf: Float,
    override val cls: Int,
    override val clsName: String
): BoundingBox