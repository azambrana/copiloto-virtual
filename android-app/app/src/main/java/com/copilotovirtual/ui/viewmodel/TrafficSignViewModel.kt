package com.copilotovirtual.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copilotovirtual.data.model.TrafficSign
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel para manejar la señal de tráfico detectada.
 * Se utiliza para comunicar la señal de tráfico detectada entre los distintos componentes de la aplicación.
 *
 * @author Alvaro Zambrana Sejas
 * @since 0.4
 */
class TrafficSignViewModel : ViewModel() {
    private val _detectedTrafficSign = MutableLiveData<TrafficSign?>() // Nullable to handle no detection
    val detectedTrafficSign: LiveData<TrafficSign?> get() = _detectedTrafficSign

    /**
     * Actualiza la señal de tráfico detectada.
     * @param sign La señal de tráfico detectada.
     */
    fun updateTrafficSign(sign: TrafficSign?) {
        _detectedTrafficSign.postValue(sign) // Use postValue() to trigger UI updates

        // Automatically hide the traffic sign after 5 seconds
        if (sign != null) {
            viewModelScope.launch {
                delay(5000) // Wait for 5 seconds
                _detectedTrafficSign.postValue(null) // Hide the traffic sign
            }
        }
    }
}
