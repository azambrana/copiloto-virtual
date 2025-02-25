package com.copilotovirtual.adas.tsr.yolo

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import com.copilotovirtual.adas.tsr.DetectorListener
import com.copilotovirtual.adas.tsr.ObjectDetector
import com.copilotovirtual.data.model.BoundingBox
import com.copilotovirtual.utils.LabelsLoader
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

abstract class YOLODetector (
    val context: Context,
    val modelPath: String,
    val labelPath: String,
    val detectorListener: DetectorListener
): ObjectDetector {
    var interpreter: Interpreter
    var labels = mutableListOf<String>()

    var tensorWidth = 0
    var tensorHeight = 0
    var numChannel = 0
    var numElements = 0

    val imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(INPUT_MEAN, INPUT_STANDARD_DEVIATION))
        .add(CastOp(INPUT_IMAGE_TYPE))
        .build()

    init {
        val options = Interpreter.Options().apply{
            this.setNumThreads(4)
        }

        val model = FileUtil.loadMappedFile(context, modelPath)
        interpreter = Interpreter(model, options)

        val inputShape = interpreter.getInputTensor(0)?.shape()
        val outputShape = interpreter.getOutputTensor(0)?.shape()

        labels = LabelsLoader.loadLabelsFromAssets(context, labelPath).toMutableList()

        if (inputShape != null) {
            tensorWidth = inputShape[1]
            tensorHeight = inputShape[2]

            // If in case input shape is in format of [1, 3, ..., ...]
            if (inputShape[1] == 3) {
                tensorWidth = inputShape[2]
                tensorHeight = inputShape[3]
            }
        }

        if (outputShape != null) {
            numChannel = outputShape[1]
            numElements = outputShape[2]
        }
    }

    override fun detect(frame: Bitmap): ObjectDetectorResults {
        if (tensorWidth == 0
            || tensorHeight == 0
            || numChannel == 0
            || numElements == 0) return ObjectDetectorResults(emptyList(), 0)

        var inferenceTime = SystemClock.uptimeMillis()

        val resizedBitmap = Bitmap.createScaledBitmap(frame, tensorWidth, tensorHeight, false)

        val tensorImage = TensorImage(INPUT_IMAGE_TYPE)
        tensorImage.load(resizedBitmap)
        val processedImage = imageProcessor.process(tensorImage)
        val imageBuffer = processedImage.buffer

        val output = TensorBuffer.createFixedSize(intArrayOf(1, numChannel, numElements), OUTPUT_IMAGE_TYPE)
        interpreter.run(imageBuffer, output.buffer)

        val bestBoxes = bestBox(output.floatArray)
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime

        if (bestBoxes.isEmpty()) {
            detectorListener.onEmptyDetect()
            return ObjectDetectorResults(emptyList(), 0)
        }

        detectorListener.onDetect(bestBoxes, inferenceTime)

        return return ObjectDetectorResults(bestBoxes, inferenceTime)
    }

    fun close() {
        interpreter.close()
    }

    abstract fun bestBox(array: FloatArray): List<BoundingBox>

    companion object {
        private const val INPUT_MEAN = 0f
        private const val INPUT_STANDARD_DEVIATION = 255f
        private val INPUT_IMAGE_TYPE = DataType.FLOAT32
        private val OUTPUT_IMAGE_TYPE = DataType.FLOAT32
    }
}
