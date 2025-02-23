package com.copilotovirtual.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

/**
 * Clase que se encarga de manejar la ubicación del dispositivo.
 * @param context Contexto de la aplicación
 * @param onLocationUpdate Función que se ejecutará cuando se actualice la ubicación
 *
 * @author Alvaro Zambrana Sejas
 * @version 0.4
 */
class GPSManager(
    private val context: Context,
    private val onLocationUpdate: (Location) -> Unit
) {
    /**
     * Cliente de ubicación fusionada de Google Play Services
     */
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Solicitud de ubicación
     */
    private val locationRequest: LocationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, // Priority level
        3000 // Interval in milliseconds (5s)
    ).apply {
        setMinUpdateIntervalMillis(2000) // Fastest interval (2s)
        setWaitForAccurateLocation(false) // Avoid extra wait time for high accuracy
    }.build()

    /**
     * Callback para recibir actualizaciones de ubicación
     */
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult ?: return
            for (location in locationResult.locations) {
                onLocationUpdate(location) // Pass location to MainActivity
            }
        }
    }

    /**
     * Verifica si la aplicación tiene permiso de ubicación.
     * @return true si la aplicación tiene permiso de ubicación, false en caso contrario
     */
    fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Solicita permiso de ubicación.
     * @param activity Actividad que solicita el permiso
     * @param requestCode Código de solicitud
     */
    fun requestPermissions(activity: android.app.Activity, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode)
    }

    /**
     * Inicia las actualizaciones de ubicación.
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (checkPermissions()) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Detiene las actualizaciones de ubicación.
     */
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
