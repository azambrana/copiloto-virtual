package com.copilotovirtual

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.copilotovirtual.databinding.ActivityMainBinding
import com.copilotovirtual.model.DistanceState
import com.copilotovirtual.model.LocationState
import com.copilotovirtual.model.SpeedLimitState
import com.copilotovirtual.model.SpeedState
import com.copilotovirtual.utils.GPSManager
import kotlinx.coroutines.*
import com.copilotovirtual.utils.SoundPlayer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var gpsManager: GPSManager

    private var lastLocation: Location? = null
    private var totalDistance = 0f
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private val loopInterval = 2000L // 2 segundos
    private var isLoopRunning = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private lateinit var soundPlayer: SoundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setStatusBarColor(R.color.primary)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Initialize utilities
        gpsManager = GPSManager(this, ::onLocationReceived)

        // Check permissions
        if (gpsManager.checkPermissions()) {
            gpsManager.startLocationUpdates()
        } else {
            gpsManager.requestPermissions(this, LOCATION_PERMISSION_REQUEST_CODE)
        }


        soundPlayer = SoundPlayer(baseContext)

        startAsyncLoop()

    }

    private fun toast(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(baseContext, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun startAsyncLoop() {
        isLoopRunning = true
        coroutineScope.launch {
            while (isLoopRunning) {
                // 🔹 Código que se ejecuta cada X segundos
                println("Ejecución en el loop: ${System.currentTimeMillis()}")
                Log.d("Loop", "Ejecución en el loop: ${System.currentTimeMillis()}, Speed: ${SpeedState.currentSpeed}, SpeedLimit: ${SpeedLimitState.currentSpeedLimit}, Distance: ${DistanceState.totalDistance}")

                if (SpeedLimitState.currentSpeedLimit > 0 && SpeedState.currentSpeed > SpeedLimitState.currentSpeedLimit) {
                    toast("Exceso de velocidad: ${SpeedState.currentSpeed} km/h, Límite: ${SpeedLimitState.currentSpeedLimit}")
                    Log.d("Loop", "Exceso de velocidad: ${SpeedState.currentSpeed} km/h, Límite: ${SpeedLimitState.currentSpeedLimit}")
                    soundPlayer.playSound("exceso-velocidad")
                }

                delay(loopInterval)
            }
        }
    }

    private fun setStatusBarColor(color: Int) {
        window?.statusBarColor = ContextCompat.getColor(baseContext, color)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
    }

    private fun onLocationReceived(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val speed = location.speed * 3.6f // Convert to km/h
        val accuracy = location.accuracy
        val timestamp = location.time

        var distance = 0f
        lastLocation?.let {
            distance = it.distanceTo(location)
            totalDistance += distance
        }
        lastLocation = location

        SpeedState.currentSpeed = speed
        DistanceState.totalDistance = DistanceState.totalDistance + totalDistance
        LocationState.latitude = latitude
        LocationState.longitude = longitude

        Log.d("Location", "Lat: $latitude, Lon: $longitude, Speed: $speed, Distance: $distance")
    }

    override fun onPause() {
        super.onPause()
        gpsManager.stopLocationUpdates()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && gpsManager.checkPermissions()) {
            gpsManager.startLocationUpdates()
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isLoopRunning = false // Detiene el loop cuando la actividad se destruye
        coroutineScope.cancel() // Cancela coroutines activas
    }
}
