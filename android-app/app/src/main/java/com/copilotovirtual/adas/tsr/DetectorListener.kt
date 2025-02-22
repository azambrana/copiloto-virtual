package com.copilotovirtual.adas.tsr

import com.copilotovirtual.data.model.BaseBoundingBox

interface DetectorListener {
    fun onEmptyDetect()
    fun onDetect(BoundingBoxYOLOv10es: List<BaseBoundingBox>, inferenceTime: Long)
}