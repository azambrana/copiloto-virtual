package com.copilotovirtual.model

object LocationState {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var totalDistance = 0f
    var firstTimestamp = System.currentTimeMillis()
    var LOCATION_PERMISSION_REQUEST_CODE = 1
}