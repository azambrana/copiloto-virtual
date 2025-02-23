package com.copilotovirtual

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.copilotovirtual.databinding.ActivityMainBinding
import com.copilotovirtual.data.model.DistanceState
import com.copilotovirtual.data.model.LocationState
import com.copilotovirtual.data.model.SpeedLimitState
import com.copilotovirtual.data.model.SpeedState
import com.copilotovirtual.utils.GPSManager
import kotlinx.coroutines.*
import com.copilotovirtual.utils.SoundPlayer
import java.lang.Integer.parseInt
import kotlin.math.abs

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
        var speedLimitExceeded = false
        var distanceLimitExceeded = 0f
        coroutineScope.launch {
            while (isLoopRunning) {
                Log.d("ISA Loop", "Timestamp: ${System.currentTimeMillis()}, Speed: ${SpeedState.currentSpeed}, SpeedLimit: ${SpeedLimitState.currentSpeedLimit}, Distance: ${DistanceState.totalDistance}")

                val deltaSpeed = SpeedState.currentSpeed - SpeedLimitState.currentSpeedLimit

                if (!speedLimitExceeded && SpeedLimitState.currentSpeedLimit > 0 && deltaSpeed > 0 && abs(deltaSpeed.toInt()) % 5 == 0) {
                    speedLimitExceeded = true
                    distanceLimitExceeded = DistanceState.totalDistance
                    val message = "Exceso de velocidad: ${SpeedState.currentSpeed} km/h, Límite: ${SpeedLimitState.currentSpeedLimit} km/h"
                    toast(message)
                    Log.d("ISA Loop", message)

                    soundPlayer.playSound("exceso-velocidad")

                    // reset speed limit
                    SpeedLimitState.currentSpeedLimit = 40f
                }

                // mantener el límite dentro de los siguientes 100 metros
                if (speedLimitExceeded && DistanceState.totalDistance - distanceLimitExceeded > 100) {
                    speedLimitExceeded = false
                }

                delay(loopInterval)
            }
        }
    }

    private fun onLocationReceived(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val speed = location.speed * 3.6f // Convert to km/h

        var distance = 0f
        lastLocation?.let {
            distance = it.distanceTo(location)
            totalDistance += distance
        }
        lastLocation = location

        SpeedState.currentSpeed = speed
        DistanceState.totalDistance += distance
        LocationState.latitude = latitude
        LocationState.longitude = longitude

        // findViewById<TextView>(R.id.speedLimit).text = SpeedLimitState.currentSpeedLimit.toInt().toString() + " km/h"
        findViewById<TextView>(R.id.currentSpeed).text = speed.toInt().toString() + " km/h"

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
