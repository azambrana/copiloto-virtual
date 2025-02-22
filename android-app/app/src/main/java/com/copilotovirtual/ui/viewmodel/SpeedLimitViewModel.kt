package com.copilotovirtual.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.copilotovirtual.data.model.TrafficSign

class SpeedLimitViewModel : ViewModel() {
    private val _detectedSpeedSign = MutableLiveData<TrafficSign?>()
    val detectedSpeedSign: LiveData<TrafficSign?> get() = _detectedSpeedSign

    fun updateSpeedLimitSign(sign: TrafficSign?) {
        _detectedSpeedSign.postValue(sign)
    }
}
