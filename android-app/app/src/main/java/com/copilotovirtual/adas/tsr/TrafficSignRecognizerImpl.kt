package com.copilotovirtual.adas.tsr

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.copilotovirtual.adas.tsr.yolo.ObjectDetectorResults
import com.copilotovirtual.data.model.BoundingBox
import com.copilotovirtual.data.model.TrafficSign

private const val TAG = "TrafficSignRecognizerImpl"

/**
 * Clase que implementa la interfaz [TrafficSignRecognizer].
 * @param detectorType Tipo de detector a utilizar.
 * @param context Contexto de la aplicación.
 *
 * @see TrafficSignRecognizer
 * @see DetectorType
 * @see ObjectDetector
 * @see ObjectDetectorFactory
 * @see DetectorListener
 *
 * @author Alvaro Zambrana Sejas
 * @since 0.4
 */
class TrafficSignRecognizerImpl(
    private val detectorType: DetectorType,
    private val context: Context,
    private val trafficSignListener: TrafficSignListener
) : TrafficSignRecognizer, DetectorListener {
    private lateinit var results: ObjectDetectorResults
    private lateinit var trafficSignRecognizerResults: TrafficSignRecognizerResults
    var lastDetectedSign: TrafficSign? = null
    var objectDetector: ObjectDetector? = null

    init {
        // TODO refactor to support multiple DetectorListener
        objectDetector = ObjectDetectorFactory.create(detectorType, context, this)
    }

    override fun detectTrafficSigns(bitmap: Bitmap): TrafficSignRecognizerResults {
        results = objectDetector!!.detect(bitmap)
        trafficSignRecognizerResults = TrafficSignRecognizerResults(
            trafficSigns = results.boundingBoxes.map {
                TrafficSign(
                    it.clsName,
                    it.cnf,
                    it
                )
            },
            inferenceTime = results.inferenceTime
        )

        trafficSignListener.onTrafficSignsDetected(trafficSignRecognizerResults)
        return trafficSignRecognizerResults
    }

    override fun fetchLastDetectedSign(): TrafficSign? {
        results.boundingBoxes.last().let {
            lastDetectedSign = TrafficSign(
                it.clsName,
                it.cnf,
                it
            )
        }

        return lastDetectedSign
    }

    override fun resetDetection() {
        lastDetectedSign = null
    }

    override fun onEmptyDetect() {
        Log.d(TAG, "detectorType: $detectorType, onEmptyDetect")
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        Log.d(TAG, "detectorType: $detectorType, onDetect: $boundingBoxes, inferenceTime: $inferenceTime")
    }
}