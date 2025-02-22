package com.copilotovirtual.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CurrentSpeedViewModel : ViewModel() {
    private val _currentSpeed = MutableLiveData<Int?>()
    val currentSpeed: LiveData<Int?> get() = _currentSpeed

    fun updateCurrentSpeed(speed: Int?) {
        _currentSpeed.postValue(speed)
    }
}
