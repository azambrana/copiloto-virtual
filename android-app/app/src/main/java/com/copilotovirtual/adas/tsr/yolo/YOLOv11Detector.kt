package com.copilotovirtual.adas.tsr.yolo

import android.content.Context
import com.copilotovirtual.adas.tsr.DetectorListener
import com.copilotovirtual.data.model.BaseBoundingBox
import com.copilotovirtual.data.model.BoundingBox

/**
 * Clase Detector para realizar la detección de objetos en un frame de video utilizando un modelo de
 * TensorFlow Lite generado con el framework YOLOv11.
 */
class YOLOv11Detector(
    context: Context,
    modelPath: String,
    labelPath: String,
    detectorListener: DetectorListener
): YOLOv8Detector(context, modelPath, labelPath, detectorListener) {

}