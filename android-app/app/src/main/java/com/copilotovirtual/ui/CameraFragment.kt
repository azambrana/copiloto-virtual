package com.copilotovirtual.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.copilotovirtual.Constants.LABELS_PATH
import com.copilotovirtual.Constants.MODEL_PATH_YOLOv10
import com.copilotovirtual.YOLOv10Detector
import com.copilotovirtual.databinding.FragmentCameraBinding
import com.copilotovirtual.model.BoundingBoxYOLOv10
import com.copilotovirtual.model.LocationState
import com.copilotovirtual.model.SpeedLimitState
import com.copilotovirtual.model.SpeedState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.copilotovirtual.utils.CSVLogger
import com.copilotovirtual.utils.SoundPlayer

/**
 * Fragmento que muestra la cámara y detecta objetos en tiempo real.
 */
class CameraFragment : Fragment(), YOLOv10Detector.DetectorListener {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val isFrontCamera = false
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var YOLOv10Detector: YOLOv10Detector? = null
    private lateinit var cameraExecutor: ExecutorService
    private val mediaPlayer = MediaPlayer()
    private val firstTimestamp = System.currentTimeMillis()
    private var previousClassName = ""
    private var previousTimestamp = 0L
    private lateinit var csvLogger: CSVLogger
    private lateinit var soundPlayer: SoundPlayer

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
            YOLOv10Detector = YOLOv10Detector(requireContext(), MODEL_PATH_YOLOv10, LABELS_PATH, this) {
                toast(it)
            }
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        soundPlayer = SoundPlayer(requireContext())

        createCSVFile()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider  = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val rotation = binding.viewFinder.display.rotation

        val cameraSelector = CameraSelector
            .Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview =  Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
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

                if (isFrontCamera) {
                    postScale(
                        -1f,
                        1f,
                        imageProxy.width.toFloat(),
                        imageProxy.height.toFloat()
                    )
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
                matrix, true
            )

            YOLOv10Detector?.detect(rotatedBitmap)
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
        } catch(exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
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
        YOLOv10Detector?.close()
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


    override fun onDetect(boundingBoxes: List<BoundingBoxYOLOv10>, inferenceTime: Long) {
        val currentTimestamp = System.currentTimeMillis()
        requireActivity().runOnUiThread {
            binding.overlay.apply {
                setResults(boundingBoxes)
                invalidate()
            }

            if (boundingBoxes.isNotEmpty()) {
                // TODO: Compute the class with higher priority and importance
                val best = boundingBoxes.maxByOrNull { it.cnf }

                if (best != null) {
                    if (currentTimestamp - previousTimestamp > 9000) {
                        if (acceptableConfidence(best.cnf)) {
                            previousClassName = best.clsName
                            previousTimestamp = currentTimestamp
                            soundPlayer.playSound(best.clsName)
                            toast("Detectado: ${best.clsName} [${best.cnf}]")
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
    private fun logBestDetectedBoundingBoxes(boundingBoxes: List<BoundingBoxYOLOv10>, timestamp: Long, best: BoundingBoxYOLOv10?, inferenceTime: Long) {
        try {
            for (bbox in boundingBoxes) {
                val timestamp = System.currentTimeMillis()
                var isAcceptable = acceptableConfidence(bbox.cnf)
                val sound = if (isAcceptable) "1" else "0"
                val clsName = bbox.clsName

                if (isAcceptable && clsName.startsWith("limite-velocidad-")) {
                    SpeedLimitState.currentSpeedLimit = clsName.substringAfter("limite-velocidad-").toFloat()
                } else if (isAcceptable && clsName.startsWith("zona-escolar")) {
                    SpeedLimitState.currentSpeedLimit = 10f
                }

                csvLogger.logRowData(timestamp, LocationState.latitude, LocationState.longitude, SpeedState.currentSpeed, clsName, bbox.cnf, sound, inferenceTime)
            }
        } catch (e: IOException) {
            Log.e("CSVError", "Error writing to CSV file", e)
        }
    }

    /**
     * Verifica si la confianza de la detección es aceptable.
     */
    private fun acceptableConfidence(cnf: Float): Boolean {
        return cnf >= 0.7
    }
}