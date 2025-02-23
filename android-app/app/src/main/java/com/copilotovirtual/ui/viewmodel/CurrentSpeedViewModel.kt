package com.copilotovirtual.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel para almacenar la velocidad actual.
 * Se utiliza para comunicar la velocidad actual entre los distintos componentes de la aplicación.
 *
 * @author Alvaro Zambrana Sejas
 * @since 0.4
 */
class CurrentSpeedViewModel : ViewModel() {
    private val _currentSpeed = MutableLiveData<Int?>()
    val currentSpeed: LiveData<Int?> get() = _currentSpeed

    /**
     * Actualiza la velocidad actual.
     * @param speed La velocidad actual.
     */
    fun updateCurrentSpeed(speed: Int?) {
        _currentSpeed.postValue(speed)
    }
}
