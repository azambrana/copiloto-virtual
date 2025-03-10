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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.copilotovirtual.adas.isa.DEFAULT_SPEED_LIMIT
import com.copilotovirtual.adas.isa.IntelligentSpeedAssistanceService
import com.copilotovirtual.adas.isa.IntelligentSpeedAssistanceServiceImpl
import com.copilotovirtual.adas.isa.SPEED_LIMIT_PREFFIX
import com.copilotovirtual.adas.isa.SpeedLimitListener
import com.copilotovirtual.adas.tsr.TrafficSignRecognizerService
import com.copilotovirtual.adas.tsr.DetectorType
import com.copilotovirtual.adas.tsr.TrafficSignRecognizerResults
import com.copilotovirtual.adas.tsr.TrafficSignListener
import com.copilotovirtual.adas.tsr.TrafficSignRecognizerServiceImpl
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
import java.util.concurrent.Executors
import com.copilotovirtual.utils.CSVLogger
import com.copilotovirtual.utils.SoundPlayer

private const val DELAY_DETECTION_SECONDS = 5000
private const val ACCEPTABLE_CONFIDENCE = 0.7

/**
 * Fragmento que muestra la cámara y detecta objetos en tiempo real.
 *
 * @author Alvaro Zambrana Sejas
 * @version 0.4
 */
class CameraFragment : Fragment(), TrafficSignListener, SpeedLimitListener {
    private val showOverlay: Boolean = true
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraExecutor = Executors.newSingleThreadExecutor()
    private var previousClassName = ""
    private var previousTimestamp = 0L
    private lateinit var csvLogger: CSVLogger
    private lateinit var soundPlayer: SoundPlayer
    private val detectorType: DetectorType = DetectorType.YOLOv11

    private lateinit var trafficSignRecognizerService: TrafficSignRecognizerService
    private lateinit var intelligentSpeedAssistanceService: IntelligentSpeedAssistanceService

    val trafficSignViewModel: TrafficSignViewModel by activityViewModels()
    val speedLimitViewModel: SpeedLimitViewModel by activityViewModels()
    val currentSpeedViewModel: CurrentSpeedViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.currentSpeedViewModel = currentSpeedViewModel
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkCameraPermissionAndStart()

        soundPlayer = SoundPlayer(requireContext())

        createCSVFile()

        resetSpeedLimit()
    }

    private fun checkCameraPermissionAndStart() {
        when {
            allPermissionsGranted() -> {
                startCamera()
                cameraExecutor.execute {
                    trafficSignRecognizerService = TrafficSignRecognizerServiceImpl(context = requireContext(), detectorType = detectorType, this)
                    intelligentSpeedAssistanceService  = IntelligentSpeedAssistanceServiceImpl(requireContext(), this)
                }
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(requireContext(), "Camera permission is needed to use the camera.", Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
            }
            else -> {
                requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
            }
        }
    }

    private fun resetSpeedLimit() {
        onResetSpeedLimit(DEFAULT_SPEED_LIMIT)
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

        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(rotation)
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

            trafficSignRecognizerService.processFrame(rotatedBitmap)
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
        if (it[Manifest.permission.CAMERA] == true) {
            startCamera()
        } else {
            toast("Permiso para la cámara es requerido.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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

    private fun logDetectedBoundingBoxes(boundingBoxes: List<BoundingBox>, timestamp: Long, inferenceTime: Long) {
        try {
            for (bbox in boundingBoxes) {
                val isAcceptable = acceptableConfidence(bbox.cnf)
                val sound = if (isAcceptable) "1" else "0"
                val clsName = bbox.clsName
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

    override fun onTrafficSignsDetected(trafficSignRecognizerResults: TrafficSignRecognizerResults) {
        trafficSignRecognizerResults.trafficSigns.forEach { Log.d(TAG, it.toString()) }
        val trafficSigns: List<TrafficSign> = trafficSignRecognizerResults.trafficSigns
        val boundingBoxes = trafficSigns.map { it.position }.filterNotNull()

        val currentTimestamp = System.currentTimeMillis()

        if (showOverlay) updateOverlay(emptyList())

        if (trafficSigns.isEmpty()) return

        val bestTrafficSign = trafficSigns.maxByOrNull { it.position?.cnf?: -1f }

        if (bestTrafficSign == null) return

        val best = bestTrafficSign.position

        if (best != null && acceptableConfidence(best.cnf) && currentTimestamp - previousTimestamp > DELAY_DETECTION_SECONDS) {
            if (showOverlay) updateOverlay(listOf(best))

            previousClassName = best.clsName
            previousTimestamp = currentTimestamp
            soundPlayer.playSound(best.clsName)

            toast("${best.clsName} [${best.cnf}]")

            this.trafficSignViewModel.updateTrafficSign(bestTrafficSign)

            intelligentSpeedAssistanceService.processSpeedLimitTrafficSign(bestTrafficSign)

            logDetectedBoundingBoxes(boundingBoxes, currentTimestamp, trafficSignRecognizerResults.inferenceTime)
        }
    }

    private fun updateOverlay(boundingBoxes: List<BoundingBox>) {
        requireActivity().runOnUiThread {
            binding.overlay.apply {
                setResults(boundingBoxes)
                invalidate()
            }
        }
    }

    override fun onSpeedLimitDetected(detectedSpeedLimitSign: TrafficSign) {
        speedLimitViewModel.updateSpeedLimitSign(detectedSpeedLimitSign)
        SpeedLimitState.currentSpeedLimit = detectedSpeedLimitSign.type.substringAfter(SPEED_LIMIT_PREFFIX).toInt()
    }

    override fun onSpeedLimitExceeded() {
        toast("¡Exceso de velocidad! ${SpeedState.currentSpeed} km/h, Límite: ${SpeedLimitState.currentSpeedLimit} km/h")
    }

    override fun onSpeedChanged(speed: Int) {
        SpeedState.currentSpeed = speed
        currentSpeedViewModel.updateCurrentSpeed(speed)
    }

    override fun onResetSpeedLimit(speed: Int) {
        val speedLimitTrafficSign = TrafficSign(
            type = SPEED_LIMIT_PREFFIX + speed,
            confidence = 1f,
            position = null
        )
        onSpeedLimitDetected(speedLimitTrafficSign)
    }
}