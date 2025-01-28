package com.copilotovirtual

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.location.*
import android.location.Location
import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.copilotovirtual.databinding.ActivityMainBinding
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var lastLocation: Location? = null
    private var totalDistance = 0f

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private val firstTimestamp = System.currentTimeMillis()

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize the location request (this is where it was missing)
        locationRequest = LocationRequest.create().apply {
            interval = 5000 // 5 seconds
            fastestInterval = 2000 // 2 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Check and request permissions
        checkPermissions()

        // Initialize location services
        setupLocationClient()

        // Create or get the CSV file for logging
        createCSVFile()

    }


    private fun setStatusBarColor(color: Int) {
        window?.statusBarColor = ContextCompat.getColor(baseContext, color)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
    }

    // Set up the location client
    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 5000 // 5 seconds
            fastestInterval = 2000 // 2 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    // Check location permissions
    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Permissions are granted, start location updates
            startLocationUpdates()
        }
    }

    // Handle the permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }

    // Start receiving location updates
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        }
    }

    // Location callback to handle location updates
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            p0 ?: return
            for (location in p0.locations) {
                logLocationToCSV(location)
            }
        }
    }

    // Create CSV file and write headers if it doesn't exist
    private fun createCSVFile() {
        try {
            val file = getCSVFile()
            if (!file.exists()) {
                file.createNewFile()
                val fileWriter = FileWriter(file, true)
                fileWriter.append("latitud,longitud,velocidad,timestamp,distancia\n")
                fileWriter.flush()
                fileWriter.close()
            }
        } catch (e: IOException) {
            Log.e("CSVError", "Error creating CSV file", e)
        }
    }

    /**
     * Registrar los datos de la ubicación, velocidad, accuracy en un archivo CSV
     * @param location Objeto Location para extrar los datos y almacenarlos en el CSV
     */
    private fun logLocationToCSV(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val speed = location.speed * 3.6f; // km/h
        val accuracy = location.accuracy;
        val timestamp = location.time // timestamp en millisegundos

        var distance = 0f
        lastLocation?.let {
            distance = it.distanceTo(location)
            totalDistance += distance
        }

        lastLocation = location

        try {
            val file = getCSVFile()
            val fileWriter = FileWriter(file, true)

            fileWriter.append("$timestamp,$latitude,$longitude,$speed,$accuracy,$distance\n")
            fileWriter.flush()
            fileWriter.close()

            Log.d("GPS", "Data logged: Timestamp: $timestamp, Lat: $latitude, Lon: $longitude, " +
                    "Speed: $speed, Accuracy: $accuracy, Distance: $distance")

        } catch (e: IOException) {
            Log.e("CSVError", "Error writing to CSV file", e)
        }
    }

    // Get or create the CSV file in external storage (internal storage can be used too)
    private fun getCSVFile(): File {
        val folder = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val timestampString = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date(firstTimestamp))
        return File(folder, "${timestampString}_gps_data.csv")
    }

    // Stop location updates when not needed (e.g., in onPause)
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}