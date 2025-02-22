package com.copilotovirtual.data.model

/**
 * Interface de BoundingBox que define los atributos de una caja delimitadora.
 *
 * @author Alvaro Zambrana Sejas
 * @version 0.4
 */
interface BoundingBox {
    /**
     * Coordenada x1 de la caja delimitadora.
     */
    val x1: Float

    /**
     * Coordenada y1 de la caja delimitadora.
     */
    val y1: Float

    /**
     * Coordenada x2 de la caja delimitadora.
     */
    val x2: Float

    /**
     * Coordenada y2 de la caja delimitadora.
     */
    val y2: Float

    val cx: Float
    val cy: Float
    val w: Float
    val h: Float

    /**
     * Confianza de la detección, un valor entre 0 y 1.
     */
    val cnf: Float

    /**
     * Clase de la detección.
     */
    val cls: Int

    /**
     * Nombre de la clase de la detección.
     */
    val clsName: String
}