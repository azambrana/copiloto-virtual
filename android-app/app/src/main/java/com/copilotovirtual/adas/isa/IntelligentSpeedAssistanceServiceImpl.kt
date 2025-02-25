package com.copilotovirtual.adas.isa

import android.content.Context
import com.copilotovirtual.data.model.TrafficSign

const val EXCESO_VELOCIDAD = "exceso-velocidad"

class IntelligentSpeedAssistanceServiceImpl(
    context: Context,
    speedLimitListener: SpeedLimitListener
) : IntelligentSpeedAssistanceService(context, speedLimitListener) {

    private var isa: IntelligentSpeedAssistance

    init {
        isa = IntelligentSpeedAssistanceImpl(context, speedLimitListener)
        isa.start()
        resetSpeedLimit()
    }

    override fun processSpeedLimitTrafficSign(trafficSign: TrafficSign) {
        var detectedSpeedLimitSign: TrafficSign? = null

        if (trafficSign.type.startsWith(SPEED_LIMIT_PREFFIX)) {
            detectedSpeedLimitSign = trafficSign
        }

        if (trafficSign.type.startsWith("zona-escolar")) {
            detectedSpeedLimitSign = TrafficSign(
                type = "limite-velocidad-10",
                confidence = 1f,
                position = null
            )
        }

        if ( detectedSpeedLimitSign != null) {
            speedLimitListener.onSpeedLimitDetected(detectedSpeedLimitSign)
        }
    }
}