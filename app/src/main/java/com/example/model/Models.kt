package com.example.model

enum class Language {
    ENGLISH, HINDI
}

data class EmergencyContact(
    val id: String,
    val name: String,
    val phone: String,
    val relation: String,
    val isPrimary: Boolean = false
)

data class User(
    val id: String,
    val fullName: String,
    val maskedAadhaar: String, // E.g. "XXXX-XXXX-1234"
    val aadhaarHash: String,   // SHA-256 hash with client salt
    val email: String,
    val emergencyContacts: List<EmergencyContact>,
    val token: String,
    val tokenIssuedAt: Long = System.currentTimeMillis()
)

enum class IncidentCategory(val displayNameEn: String, val displayNameHi: String) {
    HARASSMENT("Harassment", "उत्पीड़न"),
    STALKING("Stalking", "पीछा करना"),
    PHYSICAL_ASSAULT("Physical Assault", "शारीरिक हमला"),
    THEFT("Theft", "चोरी"),
    CYBER_HARASSMENT("Cyber Harassment", "साइबर उत्पीड़न"),
    EVE_TEASING("Eve Teasing / Catcalling", "फब्तियां कसना")
}

enum class ComplaintStatus(val displayNameEn: String, val displayNameHi: String, val stepIndex: Int) {
    REGISTERED("Registered", "दर्ज की गई", 1),
    REVIEWING("Reviewing", "समीक्षा जारी", 2),
    ASSIGNED_TO_OFFICER("Assigned to Officer", "अधिकारी को सौंपा गया", 3),
    ACTION_TAKEN("Action Taken", "कार्रवाई पूरी हुई", 4)
}

data class Complaint(
    val id: String, // e.g. "DHR-2026-84920"
    val category: IncidentCategory,
    val location: String,
    val approximateTime: String,
    val perpetratorDescription: String,
    val vehicleDetails: String,
    val narrative: String,
    val status: ComplaintStatus,
    val officerName: String? = null,
    val badgeNumber: String? = null,
    val policeStation: String? = null,
    val filedTimestamp: Long = System.currentTimeMillis(),
    val encryptedPayloadHash: String,
    val qrCodePayload: String
)

enum class RouteType {
    FASTEST, SAFEST
}

data class RouteOption(
    val type: RouteType,
    val titleEn: String,
    val titleHi: String,
    val timeMinutes: Int,
    val distanceKm: Double,
    val safetyScore: Int, // e.g. 94 / 100
    val crowdIndex: String, // "High Crowd" / "Moderate"
    val lightingPercent: Int, // 95%
    val pastIncidents: String, // "0 reported this month"
    val policeCheckpointsCount: Int,
    val tags: List<String>
)

data class SafeTripState(
    val destination: String,
    val selectedRoute: RouteOption,
    val startTimestamp: Long,
    val isStationaryAlertActive: Boolean = false,
    val isOfflineAlertActive: Boolean = false,
    val lastKnownLocation: String = "Connaught Place / Inner Circle (28.6315° N, 77.2167° E)",
    val isCompleted: Boolean = false
)

data class SosState(
    val isActive: Boolean = false,
    val triggeredAt: Long = 0L,
    val cancelWindowRemainingSeconds: Int = 10,
    val locationUrl: String = "https://maps.google.com/?q=28.6139,77.2090",
    val policeControlRoom: String = "Dial 112 / Central Command Unit",
    val batteryLevel: Int = 87
)
