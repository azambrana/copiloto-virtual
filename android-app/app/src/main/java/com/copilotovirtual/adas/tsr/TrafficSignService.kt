package com.copilotovirtual.adas.tsr

import com.copilotovirtual.adas.ITrafficSignService
import com.copilotovirtual.data.model.TrafficSign

class TrafficSignService(private val recognizer: TrafficSignRecognizer) : ITrafficSignService {

    fun processFrame(imageData: ByteArray): List<TrafficSign> {
        return recognizer.detectTrafficSigns(imageData)
    }
}