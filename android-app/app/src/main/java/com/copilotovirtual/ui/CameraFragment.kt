package com.copilotovirtual.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
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
import com.copilotovirtual.model.BoundingBox
import com.copilotovirtual.Constants.LABELS_PATH
import com.copilotovirtual.Constants.MODEL_PATH
import com.copilotovirtual.Detector
import com.copilotovirtual.databinding.FragmentCameraBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraFragment : Fragment(), Detector.DetectorListener {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val isFrontCamera = false
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var detector: Detector? = null
    private lateinit var cameraExecutor: ExecutorService
    private val mediaPlayer = MediaPlayer()
    private val firstTimestamp = System.currentTimeMillis()
    private var previousClassName = ""
    private var previousTimestamp = 0L

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
            detector = Detector(requireContext(), MODEL_PATH, LABELS_PATH, this) {
                toast(it)
            }
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

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

            detector?.detect(rotatedBitmap)
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
        detector?.close()
        cameraExecutor.shutdown()
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
                // TODO: Compute the class with higher priority and importance
                val best = boundingBoxes.maxByOrNull { it.cnf }

                if (best != null) {
                    if (currentTimestamp - previousTimestamp > 9000) {
                        if (shouldSound(best.cnf)) {
                            previousClassName = best.clsName
                            previousTimestamp = currentTimestamp
                            playSound(requireContext(), best.clsName)
                            toast("Detectado: ${best.clsName} [${best.cnf}]")
                        }
                    }
                }
                logDetectedBoundingBoxes(boundingBoxes, currentTimestamp, best, inferenceTime)
            }
        }
    }

    fun playSound(context: Context, clsName: String) {
        val fileName = "${clsName}.wav"
        if (mediaPlayer.isPlaying) {
            return
        }
        try {
            mediaPlayer.reset()
            val afd = context.assets.openFd(fileName)
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mediaPlayer.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mediaPlayer.isLooping = false
        mediaPlayer.start()
        toast("Sonido: $fileName")
    }


    private fun toast(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    // Create CSV file and write headers if it doesn't exist
    private fun createCSVFile() {
        try {
            val file = getCSVFile()
            if (!file.exists()) {
                file.createNewFile()
                val fileWriter = FileWriter(file, true)
                fileWriter.append("timestamp,clase,probabilidad,sound,inferenceTime\n")
                fileWriter.flush()
                fileWriter.close()
            }
        } catch (e: IOException) {
            Log.e("CSVError", "Error creating CSV file", e)
        }
    }

    private fun logDetectedBoundingBoxes(boundingBoxes: List<BoundingBox>, timestamp: Long, best: BoundingBox?, inferenceTime: Long) {
        try {
            val file = getCSVFile()
            val fileWriter = FileWriter(file, true)

            for (bbox in boundingBoxes) {
                val timestamp = System.currentTimeMillis()
                val sound = if (shouldSound(bbox.cnf)) "1" else "0"
                fileWriter.append("${timestamp},${bbox.clsName},${bbox.cnf},${sound},${inferenceTime}\n")
            }

            fileWriter.flush()
            fileWriter.close()
        } catch (e: IOException) {
            Log.e("CSVError", "Error writing to CSV file", e)
        }
    }

    private fun shouldSound(cnf: Float): Boolean {
        return cnf >= 0.7
    }

    private fun getCSVFile(): File {
        val folder = getActivity()?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val timestampString = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date(firstTimestamp))
        return File(folder, "${timestampString}_yolo_data.csv")
    }

}