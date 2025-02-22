package com.copilotovirtual.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copilotovirtual.data.model.TrafficSign
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TrafficSignViewModel : ViewModel() {
    private val _detectedTrafficSign = MutableLiveData<TrafficSign?>() // Nullable to handle no detection
    val detectedTrafficSign: LiveData<TrafficSign?> get() = _detectedTrafficSign

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
