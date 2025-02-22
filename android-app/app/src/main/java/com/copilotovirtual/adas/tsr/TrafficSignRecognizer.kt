package com.copilotovirtual.adas.tsr

import com.copilotovirtual.data.model.TrafficSign

interface TrafficSignRecognizer {
    /**
     * Processes a given frame (image) and returns detected traffic signs.
     * @param imageData The image frame in a format suitable for processing.
     * @return A list of detected traffic signs with their types and positions.
     */
    fun detectTrafficSigns(imageData: ByteArray): List<TrafficSign>

    /**
     * Retrieves the last detected traffic sign.
     * @return The last detected traffic sign or null if no sign was detected.
     */
    fun getLastDetectedSign(): TrafficSign?

    /**
     * Clears the stored detection results.
     */
    fun resetDetection()
}
