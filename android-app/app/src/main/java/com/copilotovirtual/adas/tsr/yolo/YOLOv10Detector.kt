package com.copilotovirtual.adas.tsr.yolo

import android.content.Context
import com.copilotovirtual.adas.tsr.DetectorListener
import com.copilotovirtual.data.model.BaseBoundingBox
import com.copilotovirtual.data.model.BoundingBox

class YOLOv10Detector(
    context: Context,
    modelPath: String,
    labelPath: String,
    detectorListener: DetectorListener,
) : YOLODetector(context, modelPath, labelPath, detectorListener) {

    override fun bestBox(array: FloatArray) : List<BoundingBox> {
        val boundingBoxes = mutableListOf<BoundingBox>()
        for (r in 0 until numElements) {
            val cnf = array[r * numChannel + 4]
            if (cnf > CONFIDENCE_THRESHOLD) {
                val x1 = array[r * numChannel]
                val y1 = array[r * numChannel + 1]
                val x2 = array[r * numChannel + 2]
                val y2 = array[r * numChannel + 3]
                val cls = array[r * numChannel + 5].toInt()
                val clsName = labels[cls]
                boundingBoxes.add(
                    BaseBoundingBox(
                        x1 = x1, y1 = y1, x2 = x2, y2 = y2,
                        cnf = cnf, cls = cls, clsName = clsName
                    )
                )
            }
        }
        return boundingBoxes
    }

    companion object {
        private const val CONFIDENCE_THRESHOLD = 0.3F
    }
}