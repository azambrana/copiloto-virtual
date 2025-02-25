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
    private val _detectedTrafficSign = MutableLiveData<TrafficSign?>()
    val detectedTrafficSign: LiveData<TrafficSign?> get() = _detectedTrafficSign

    /**
     * Actualiza la señal de tráfico detectada.
     * @param sign La señal de tráfico detectada.
     */
    fun updateTrafficSign(sign: TrafficSign?) {
        _detectedTrafficSign.postValue(sign)

        // Ocultar la señal de tráfico después de 5 segundos
        if (sign != null) {
            viewModelScope.launch {
                delay(5000)
                _detectedTrafficSign.postValue(null)
            }
        }
    }
}
