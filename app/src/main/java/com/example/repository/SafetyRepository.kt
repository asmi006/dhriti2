package com.example.repository

import android.content.Context
import android.location.Location
import androidx.room.Room
import com.example.ai.GeminiIntelligenceService
import com.example.ai.StructuredIncidentResult
import com.example.crypto.CryptoManager
import com.example.data.AppDatabase
import com.example.data.ComplaintEntity
import com.example.location.LiveLocationAndRouteService
import com.example.location.LiveRouteDetails
import com.example.model.Complaint
import com.example.model.ComplaintStatus
import com.example.model.EmergencyContact
import com.example.model.IncidentCategory
import com.example.model.Language
import com.example.model.RouteOption
import com.example.model.RouteType
import com.example.model.SafeTripState
import com.example.model.SosState
import com.example.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SafetyRepository(
    private val context: Context,
    private val scope: CoroutineScope
) {
    // Room Database Backend
    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "dhriti_safety.db"
    ).fallbackToDestructiveMigration().build()

    private val complaintDao = database.complaintDao()

    // Real Live Location & Route Service
    val locationService = LiveLocationAndRouteService(context.applicationContext)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentLanguage = MutableStateFlow(Language.ENGLISH)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    private val _sosState = MutableStateFlow(SosState())
    val sosState: StateFlow<SosState> = _sosState.asStateFlow()

    private val _activeTrip = MutableStateFlow<SafeTripState?>(null)
    val activeTrip: StateFlow<SafeTripState?> = _activeTrip.asStateFlow()

    private val _currentLiveLocation = MutableStateFlow<Location?>(null)
    val currentLiveLocation: StateFlow<Location?> = _currentLiveLocation.asStateFlow()

    val nearestPoliceStations = locationService.nearestPoliceStations

    private val _liveRouteDetails = MutableStateFlow<LiveRouteDetails?>(null)
    val liveRouteDetails: StateFlow<LiveRouteDetails?> = _liveRouteDetails.asStateFlow()

    private val _isCalculatingRoute = MutableStateFlow(false)
    val isCalculatingRoute: StateFlow<Boolean> = _isCalculatingRoute.asStateFlow()

    private val _isAiProcessing = MutableStateFlow(false)
    val isAiProcessing: StateFlow<Boolean> = _isAiProcessing.asStateFlow()

    private val _stationaryCountdownSeconds = MutableStateFlow(45)
    val stationaryCountdownSeconds: StateFlow<Int> = _stationaryCountdownSeconds.asStateFlow()

    private val _isSessionLocked = MutableStateFlow(false)
    val isSessionLocked: StateFlow<Boolean> = _isSessionLocked.asStateFlow()

    private val _complaints = MutableStateFlow<List<Complaint>>(emptyList())
    val complaints: StateFlow<List<Complaint>> = _complaints.asStateFlow()

    private var sosCancelCountdownJob: Job? = null
    private var stationaryJob: Job? = null
    private var sessionTimeoutJob: Job? = null
    private var tripMonitoringJob: Job? = null

    init {
        // Collect complaints from Room persistent database
        scope.launch {
            complaintDao.getAllComplaints().collect { entities ->
                _complaints.value = entities.map { it.toDomain() }
            }
        }

        // Start listening to real GPS updates
        locationService.startLiveLocationUpdates { loc ->
            _currentLiveLocation.value = loc
            _sosState.value = _sosState.value.copy(
                locationUrl = "https://maps.google.com/?q=${loc.latitude},${loc.longitude}"
            )
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setLanguage(language: Language) {
        _currentLanguage.value = language
    }

    fun touchUserActivity() {
        sessionTimeoutJob?.cancel()
        sessionTimeoutJob = scope.launch {
            delay(300_000L) // 5 minutes inactivity
            if (_currentUser.value != null) {
                _isSessionLocked.value = true
            }
        }
    }

    fun unlockSession(pinOrPass: String): Boolean {
        if (pinOrPass.isNotBlank()) {
            _isSessionLocked.value = false
            touchUserActivity()
            return true
        }
        return false
    }

    fun lockSessionNow() {
        if (_currentUser.value != null) {
            _isSessionLocked.value = true
        }
    }

    /**
     * Triggers AI model to analyze and structure an incident statement
     */
    suspend fun analyzeIncidentWithAi(rawTranscript: String): StructuredIncidentResult {
        _isAiProcessing.value = true
        val result = GeminiIntelligenceService.analyzeAndStructureIncident(rawTranscript)
        _isAiProcessing.value = false
        return result
    }

    /**
     * Computes real live route options using GPS and geocoding
     */
    fun calculateLiveRoute(destinationQuery: String) {
        if (destinationQuery.isBlank()) return
        scope.launch {
            _isCalculatingRoute.value = true
            try {
                val details = locationService.computeLiveRoutes(_currentLiveLocation.value, destinationQuery)
                _liveRouteDetails.value = details
            } catch (e: Exception) {
                // Keep existing or fallback
            } finally {
                _isCalculatingRoute.value = false
            }
        }
    }

    fun registerUser(
        fullName: String,
        rawAadhaar: String,
        email: String,
        emergencyContact1Name: String,
        emergencyContact1Phone: String,
        emergencyContact2Name: String,
        emergencyContact2Phone: String
    ): User {
        val masked = CryptoManager.maskAadhaar(rawAadhaar)
        val hashed = CryptoManager.hashAadhaar(rawAadhaar)
        val token = CryptoManager.generateMockJwt(email, fullName)

        val contacts = listOf(
            EmergencyContact(
                id = UUID.randomUUID().toString(),
                name = emergencyContact1Name,
                phone = emergencyContact1Phone,
                relation = "Contact 1",
                isPrimary = true
            ),
            EmergencyContact(
                id = UUID.randomUUID().toString(),
                name = emergencyContact2Name,
                phone = emergencyContact2Phone,
                relation = "Contact 2",
                isPrimary = false
            )
        )

        val user = User(
            id = "USR-${System.currentTimeMillis() % 100000}",
            fullName = fullName,
            maskedAadhaar = masked,
            aadhaarHash = hashed,
            email = email,
            emergencyContacts = contacts,
            token = token
        )

        _currentUser.value = user
        _isSessionLocked.value = false
        touchUserActivity()
        return user
    }

    fun logout() {
        _currentUser.value = null
        _activeTrip.value = null
        _sosState.value = SosState()
        _isSessionLocked.value = false
        sessionTimeoutJob?.cancel()
        tripMonitoringJob?.cancel()
    }

    fun deleteAllData() {
        scope.launch {
            complaintDao.deleteAll()
        }
        logout()
    }

    fun triggerSos() {
        sosCancelCountdownJob?.cancel()
        val loc = _currentLiveLocation.value
        val locUrl = if (loc != null) {
            "https://maps.google.com/?q=${loc.latitude},${loc.longitude}"
        } else {
            "https://maps.google.com/?q=28.6139,77.2090"
        }

        _sosState.value = SosState(
            isActive = true,
            triggeredAt = System.currentTimeMillis(),
            cancelWindowRemainingSeconds = 10,
            locationUrl = locUrl,
            policeControlRoom = "Dial 112 / Central Command Unit",
            batteryLevel = 87
        )

        sosCancelCountdownJob = scope.launch {
            for (sec in 9 downTo 0) {
                delay(1000L)
                _sosState.value = _sosState.value.copy(cancelWindowRemainingSeconds = sec)
            }
        }
    }

    fun cancelSos() {
        sosCancelCountdownJob?.cancel()
        _sosState.value = SosState(isActive = false)
    }

    fun startTrip(destination: String, routeOption: RouteOption) {
        val currentLoc = _currentLiveLocation.value
        val locDesc = if (currentLoc != null) {
            String.format(Locale.US, "GPS: %.4f° N, %.4f° E (Accuracy: ±%.1fm)", currentLoc.latitude, currentLoc.longitude, currentLoc.accuracy)
        } else {
            "GPS: 28.6139° N, 77.2090° E"
        }

        _activeTrip.value = SafeTripState(
            destination = destination,
            selectedRoute = routeOption,
            startTimestamp = System.currentTimeMillis(),
            lastKnownLocation = locDesc
        )

        // Real-time trip progress and stationary watchdog
        tripMonitoringJob?.cancel()
        tripMonitoringJob = scope.launch {
            var lastRecordedLocation: Location? = currentLoc
            var stationaryTimeSeconds = 0

            while (_activeTrip.value != null) {
                delay(3000L)
                val newLoc = _currentLiveLocation.value
                if (newLoc != null && lastRecordedLocation != null) {
                    val distanceMoved = newLoc.distanceTo(lastRecordedLocation)
                    if (distanceMoved < 10f) {
                        stationaryTimeSeconds += 3
                    } else {
                        stationaryTimeSeconds = 0
                        lastRecordedLocation = newLoc
                    }

                    // 90 seconds stationary triggers safety check
                    if (stationaryTimeSeconds >= 90 && _activeTrip.value?.isStationaryAlertActive != true) {
                        triggerStationaryCheck()
                        stationaryTimeSeconds = 0
                    }
                }
            }
        }
    }

    fun endTrip() {
        stationaryJob?.cancel()
        tripMonitoringJob?.cancel()
        _activeTrip.value = null
    }

    fun triggerStationaryCheck() {
        stationaryJob?.cancel()
        _stationaryCountdownSeconds.value = 45
        _activeTrip.value = _activeTrip.value?.copy(isStationaryAlertActive = true)

        stationaryJob = scope.launch {
            for (sec in 44 downTo 0) {
                delay(1000L)
                _stationaryCountdownSeconds.value = sec
            }
            _activeTrip.value = _activeTrip.value?.copy(isStationaryAlertActive = false)
            triggerSos()
        }
    }

    fun dismissStationaryAlert() {
        stationaryJob?.cancel()
        _activeTrip.value = _activeTrip.value?.copy(isStationaryAlertActive = false)
    }

    fun simulateOfflineIncident() {
        val loc = _currentLiveLocation.value
        val locStr = if (loc != null) {
            "Dispatched coordinates (${loc.latitude}, ${loc.longitude}) to PCR 112"
        } else {
            "Dispatched coordinates (28.6139, 77.2090) to PCR 112"
        }
        _activeTrip.value = _activeTrip.value?.copy(
            isOfflineAlertActive = true,
            lastKnownLocation = locStr
        )
    }

    fun dismissOfflineIncident() {
        _activeTrip.value = _activeTrip.value?.copy(isOfflineAlertActive = false)
    }

    fun submitComplaint(
        category: IncidentCategory,
        location: String,
        approximateTime: String,
        perpetratorDescription: String,
        vehicleDetails: String,
        narrative: String
    ): Complaint {
        val id = "DHR-2026-" + (10000 + (Math.random() * 90000).toInt())
        val hash = CryptoManager.generateComplaintHash(id, narrative)
        val qrPayload = "DHRITI://VERIFY/$id?hash=$hash&cat=${category.name}"

        val newComplaint = Complaint(
            id = id,
            category = category,
            location = CryptoManager.sanitizeInput(location),
            approximateTime = approximateTime.ifBlank {
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            },
            perpetratorDescription = CryptoManager.sanitizeInput(perpetratorDescription),
            vehicleDetails = CryptoManager.sanitizeInput(vehicleDetails),
            narrative = CryptoManager.sanitizeInput(narrative),
            status = ComplaintStatus.REGISTERED,
            officerName = "Special Investigation Cell (In Queue)",
            badgeNumber = "DL-SIC-PENDING",
            policeStation = "Local Women Safety Police Post",
            filedTimestamp = System.currentTimeMillis(),
            encryptedPayloadHash = "E2E-SHA256:$hash",
            qrCodePayload = qrPayload
        )

        // Save persistently to Room DB
        scope.launch {
            complaintDao.insertComplaint(ComplaintEntity.fromDomain(newComplaint))
        }

        touchUserActivity()
        return newComplaint
    }
}
