package com.copilotovirtual.adas.tsr.yolo

import android.content.Context
import com.copilotovirtual.adas.tsr.DetectorListener
import com.copilotovirtual.data.model.BoundingBox
import com.copilotovirtual.data.model.FullBoundingBox

/**
 * Clase Detector para realizar la detección de objetos en un frame de video utilizando un modelo de
 * TensorFlow Lite generado con el framework YOLOv18.
 */
open class YOLOv8Detector(
    context: Context,
    modelPath: String,
    labelPath: String,
    detectorListener: DetectorListener
) : YOLODetector(context, modelPath, labelPath, detectorListener) {

    /**
     * Obtiene las mejores bounding boxes de la salida del modelo
     */
    override fun bestBox(array: FloatArray) : List<BoundingBox> {

        val boundingBoxes = mutableListOf<FullBoundingBox>()

        for (c in 0 until numElements) {
            var maxConf = CONFIDENCE_THRESHOLD
            var maxIdx = -1
            var j = 4
            var arrayIdx = c + numElements * j
            while (j < numChannel){
                if (array[arrayIdx] > maxConf) {
                    maxConf = array[arrayIdx]
                    maxIdx = j - 4
                }
                j++
                arrayIdx += numElements
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                val clsName = labels[maxIdx]
                val cx = array[c] // 0
                val cy = array[c + numElements] // 1
                val w = array[c + numElements * 2]
                val h = array[c + numElements * 3]
                val x1 = cx - (w/2F)
                val y1 = cy - (h/2F)
                val x2 = cx + (w/2F)
                val y2 = cy + (h/2F)
                if (x1 < 0F || x1 > 1F) continue
                if (y1 < 0F || y1 > 1F) continue
                if (x2 < 0F || x2 > 1F) continue
                if (y2 < 0F || y2 > 1F) continue

                boundingBoxes.add(
                    FullBoundingBox(
                        x1 = x1, y1 = y1, x2 = x2, y2 = y2,
                        cx = cx, cy = cy, w = w, h = h,
                        cnf = maxConf, cls = maxIdx, clsName = clsName
                    )
                )
            }
        }

        if (boundingBoxes.isEmpty()) return listOf()

        return applyNMS(boundingBoxes)
    }

    /**
     * Aplica el algoritmo de Non-Maximum Suppression para eliminar bounding boxes duplicados
     * @param boxes Lista de bounding boxes
     * @return Lista de bounding boxes sin duplicados
     */
    private fun applyNMS(boxes: List<FullBoundingBox>) : MutableList<FullBoundingBox> {
        val sortedBoxes = boxes.sortedByDescending { it.cnf }.toMutableList()
        val selectedBoxes = mutableListOf<FullBoundingBox>()

        while(sortedBoxes.isNotEmpty()) {
            val first = sortedBoxes.first()
            selectedBoxes.add(first)
            sortedBoxes.remove(first)

            val iterator = sortedBoxes.iterator()
            while (iterator.hasNext()) {
                val nextBox = iterator.next()
                val iou = calculateIoU(first, nextBox)
                if (iou >= IOU_THRESHOLD) {
                    iterator.remove()
                }
            }
        }

        return selectedBoxes
    }

    /**
     * Calcula la intersección sobre unión entre dos bounding boxes
     * @param box1 Bounding box 1
     * @param box2 Bounding box 2
     * @return Valor de la intersección sobre unión
     */
    private fun calculateIoU(box1: FullBoundingBox, box2: FullBoundingBox): Float {
        val x1 = maxOf(box1.x1, box2.x1)
        val y1 = maxOf(box1.y1, box2.y1)
        val x2 = minOf(box1.x2, box2.x2)
        val y2 = minOf(box1.y2, box2.y2)
        val intersectionArea = maxOf(0F, x2 - x1) * maxOf(0F, y2 - y1)
        val box1Area = box1.w * box1.h
        val box2Area = box2.w * box2.h
        return intersectionArea / (box1Area + box2Area - intersectionArea)
    }

    companion object {
        private const val CONFIDENCE_THRESHOLD = 0.5F
        private const val IOU_THRESHOLD = 0.5F
    }
}