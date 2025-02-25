package com.copilotovirtual.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.copilotovirtual.adas.isa.DEFAULT_SPEED_LIMIT
import com.copilotovirtual.adas.isa.SPEED_LIMIT_PREFFIX
import com.copilotovirtual.data.model.TrafficSign

/**
 * ViewModel para manejar la señal de límite de velocidad detectada.
 * Se utiliza para comunicar la señal de límite de velocidad detectada entre los distintos componentes de la aplicación.
 *
 * @author Alvaro Zambrana Sejas
 * @since 0.4
 */
class SpeedLimitViewModel : ViewModel() {
    private val _detectedSpeedSign = MutableLiveData<TrafficSign?>()
    val detectedSpeedSign: LiveData<TrafficSign?> get() = _detectedSpeedSign

    // set default speed limit sign
    init {
        _detectedSpeedSign.value = TrafficSign(SPEED_LIMIT_PREFFIX + DEFAULT_SPEED_LIMIT, 1f, null)
    }

    /**
     * Actualiza la señal de límite de velocidad detectada.
     * @param sign La señal de límite de velocidad detectada.
     */
    fun updateSpeedLimitSign(sign: TrafficSign?) {
        _detectedSpeedSign.postValue(sign)
    }
}
