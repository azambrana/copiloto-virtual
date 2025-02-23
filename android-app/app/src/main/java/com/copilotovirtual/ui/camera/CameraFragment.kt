package com.copilotovirtual.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.copilotovirtual.adas.Constants.LABELS_PATH
import com.copilotovirtual.adas.Constants.MODEL_PATH_YOLOv10
import com.copilotovirtual.adas.tsr.TrafficSignRecognizerService
import com.copilotovirtual.adas.tsr.DetectorListener
import com.copilotovirtual.adas.tsr.DetectorType
import com.copilotovirtual.adas.tsr.TrafficSignRecognizerServiceImpl
import com.copilotovirtual.adas.tsr.yolo.YOLODetector
import com.copilotovirtual.adas.tsr.yolo.YOLOv10Detector
import com.copilotovirtual.data.model.BaseBoundingBox
import com.copilotovirtual.databinding.FragmentCameraBinding
import com.copilotovirtual.data.model.BoundingBox
import com.copilotovirtual.data.model.LocationState
import com.copilotovirtual.data.model.SpeedLimitState
import com.copilotovirtual.data.model.SpeedState
import com.copilotovirtual.data.model.TrafficSign
import com.copilotovirtual.ui.viewmodel.CurrentSpeedViewModel
import com.copilotovirtual.ui.viewmodel.SpeedLimitViewModel
import com.copilotovirtual.ui.viewmodel.TrafficSignViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.copilotovirtual.utils.CSVLogger
import com.copilotovirtual.utils.SoundPlayer

private const val NINE_SECONDS = 9000

private const val SPEED_LIMIT_PREFFIX = "limite-velocidad-"

private const val MIN_SPEED_LIMIT = 10f

private const val ACCEPTABLE_CONFIDENCE = 0.5

/**
 * Fragmento que muestra la cámara y detecta objetos en tiempo real.
 *
 * @author Alvaro Zambrana Sejas
 * @version 0.4
 */
class CameraFragment : Fragment(), DetectorListener {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var yoloDetector: YOLODetector? = null
    private lateinit var cameraExecutor: ExecutorService
    private var previousClassName = ""
    private var previousTimestamp = 0L
    private lateinit var csvLogger: CSVLogger
    private lateinit var soundPlayer: SoundPlayer

    private lateinit var trafficSignRecognizerService: TrafficSignRecognizerService

    val trafficSignViewModel: TrafficSignViewModel by activityViewModels()
    val speedLimitViewModel: SpeedLimitViewModel by activityViewModels()
    val currentSpeedViewModel: CurrentSpeedViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        cameraExecutor.execute {
            yoloDetector = YOLOv10Detector(requireContext(), MODEL_PATH_YOLOv10, LABELS_PATH, this)
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        trafficSignRecognizerService = TrafficSignRecognizerServiceImpl(context = requireContext(), detectorType = DetectorType.YOLOv8)
        soundPlayer = SoundPlayer(requireContext())

        createCSVFile()

        val defaultSpeedLimitDetectedSign = TrafficSign(
            type = "limite-velocidad-40",
            confidence = 1f,
            position = null
        )

        speedLimitViewModel.updateSpeedLimitSign(defaultSpeedLimitDetectedSign)
    }

    /**
     * Inicia la cámara.
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider  = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     * Configura las opciones de la cámara y los casos de uso.
     */
    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val rotation = binding.viewFinder.display.rotation

        val cameraSelector = CameraSelector
            .Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
            )
            .build()

        preview =  Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy ->
            val bitmapBuffer =
                Bitmap.createBitmap(
                    imageProxy.width,
                    imageProxy.height,
                    Bitmap.Config.ARGB_8888
                )
            imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
            imageProxy.close()

            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
                matrix, true
            )

            yoloDetector?.detect(rotatedBitmap)
            val detectedTrafficSignList = trafficSignRecognizerService.processFrame(rotatedBitmap)
            detectedTrafficSignList.forEach { Log.d(TAG, it.toString()) }
        }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer
            )

            preview?.surfaceProvider = binding.viewFinder.surfaceProvider
        } catch(exc: Exception ) {
            when (exc) {
                is IllegalStateException,
                is UnsupportedOperationException,
                is IllegalArgumentException -> {
                    Log.e(TAG, "Use case binding failed", exc)
                }
                else -> throw exc
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {
        if (it[Manifest.permission.CAMERA] == true) { startCamera() }
    }

    override fun onDestroy() {
        super.onDestroy()
        yoloDetector?.close()
        cameraExecutor.shutdown()
        soundPlayer.release()
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()){
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    companion object {
        private const val TAG = "Camera"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf (
            Manifest.permission.CAMERA
        ).toTypedArray()
    }

    override fun onEmptyDetect() {
        requireActivity().runOnUiThread {
            binding.overlay.clear()
        }
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        val currentTimestamp = System.currentTimeMillis()
        requireActivity().runOnUiThread {
            binding.overlay.apply {
                setResults(boundingBoxes)
                invalidate()
            }

            if (boundingBoxes.isNotEmpty()) {
                val best = boundingBoxes.maxByOrNull { it.cnf }

                if (best != null) {
                    if (currentTimestamp - previousTimestamp > NINE_SECONDS) {
                        if (acceptableConfidence(best.cnf)) {
                            previousClassName = best.clsName
                            previousTimestamp = currentTimestamp
                            soundPlayer.playSound(best.clsName)
                            toast("Detectado: ${best.clsName} [${best.cnf}]")

                            val detectedTrafficSign = TrafficSign(
                                type = best.clsName,
                                confidence = best.cnf,
                                position = BaseBoundingBox(
                                    best.x1, best.y1, best.x2, best.y2,
                                    best.cx, best.cy, best.w, best.h,
                                    best.cnf, best.cls, best.clsName
                                )
                            )

                            this.trafficSignViewModel.updateTrafficSign(detectedTrafficSign)

                            if (best.clsName.startsWith(SPEED_LIMIT_PREFFIX)) {
                                val detectedSpeedLimitSign = TrafficSign(
                                    type = best.clsName,
                                    confidence = best.cnf,
                                    position = BaseBoundingBox(
                                        best.x1, best.y1, best.x2, best.y2,
                                        best.cx, best.cy, best.w, best.h,
                                        best.cnf, best.cls, best.clsName
                                    )
                                )
                                this.speedLimitViewModel.updateSpeedLimitSign(detectedSpeedLimitSign)
                            }

                            if (best.clsName.startsWith("zona-escolar")) {
                                val detectedSpeedLimitSign = TrafficSign(
                                    type = "limite-velocidad-10",
                                    confidence = best.cnf,
                                    position = BaseBoundingBox(
                                        best.x1, best.y1, best.x2, best.y2,
                                        best.cx, best.cy, best.w, best.h,
                                        best.cnf, best.cls, best.clsName
                                    )
                                )
                                this.speedLimitViewModel.updateSpeedLimitSign(detectedSpeedLimitSign)
                            }
                        }
                    }
                }
                logBestDetectedBoundingBoxes(boundingBoxes, currentTimestamp, best, inferenceTime)
            }
        }
    }

    private fun toast(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Crea un archivo CSV para almacenar los datos de las detecciones.
     */
    private fun createCSVFile() {
        csvLogger = CSVLogger(
            context = requireContext(),
            firstTimestamp = System.currentTimeMillis(),
            headers =  "timestamp,latitud,longitud,velocidad,clase,probabilidad,sonido,tiempoInferencia",
            suffix = "yolo_data"
        )
    }

    /**
     * Registra los datos de las detecciones en el archivo CSV.
     */
    private fun logBestDetectedBoundingBoxes(boundingBoxes: List<BoundingBox>, timestamp: Long, best: BoundingBox?, inferenceTime: Long) {
        try {
            for (bbox in boundingBoxes) {
                val isAcceptable = acceptableConfidence(bbox.cnf)
                val sound = if (isAcceptable) "1" else "0"
                val clsName = bbox.clsName

                if (isAcceptable && clsName.startsWith(SPEED_LIMIT_PREFFIX)) {
                    SpeedLimitState.currentSpeedLimit = clsName.substringAfter(SPEED_LIMIT_PREFFIX).toFloat()
                } else if (isAcceptable && clsName.startsWith("zona-escolar")) {
                    SpeedLimitState.currentSpeedLimit = MIN_SPEED_LIMIT
                }

                csvLogger.logRowData(timestamp, LocationState.latitude, LocationState.longitude, SpeedState.currentSpeed, clsName, bbox.cnf, sound, inferenceTime)
            }
        } catch (e: IOException) {
            Log.e("CSVError", "Error de escritura en el archivo CSV ${csvLogger.getCSVFileName()}", e)
        }
    }

    /**
     * Verifica si la confianza de la detección es aceptable.
     */
    private fun acceptableConfidence(cnf: Float): Boolean {
        return cnf >= ACCEPTABLE_CONFIDENCE
    }
}