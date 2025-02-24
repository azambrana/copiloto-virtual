package com.copilotovirtual.adas.isa

import com.copilotovirtual.data.model.TrafficSign

class IntelligentSpeedAssistanceImpl(speedLimitListener: SpeedLimitListener) :
    IntelligentSpeedAssistance(speedLimitListener) {
    override fun processSpeedLimit(detectedSign: TrafficSign?, currentSpeed: Float) {
        TODO("Not yet implemented")
    }

    override fun getLastSpeedLimit(): Int? {
        TODO("Not yet implemented")
    }

    override fun resetSpeedLimit() {
        TODO("Not yet implemented")
    }
}