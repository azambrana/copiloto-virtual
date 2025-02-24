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

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private var isLoopRunning = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main)


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
    }

    private fun toast(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(baseContext, message, Toast.LENGTH_LONG).show()
        }
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
