package com.example.model

data class PoliceStation(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val latitude: Double,
    val longitude: Double,
    val addressEn: String,
    val addressHi: String,
    val phoneNumber: String,
    val emergencyHelpline: String = "112",
    val jurisdiction: String,
    val hasWomenHelpDesk: Boolean = true,
    val activePcrVans: Int = 3,
    val stationInCharge: String,
    val distanceMeters: Float = 0f,
    val distanceKm: Double = 0.0,
    val estimatedWalkingMin: Int = 5,
    val estimatedDriveMin: Int = 2,
    val bearingDegrees: Float = 0f,
    val cardinalDirection: String = "N"
)
