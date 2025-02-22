package com.copilotovirtual

import com.copilotovirtual.data.model.BoundingBox

interface DetectorListener {
    fun onEmptyDetect()
    fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long)
}