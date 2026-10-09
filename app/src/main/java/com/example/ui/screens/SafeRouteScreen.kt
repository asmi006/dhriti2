package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NoCell
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.Language
import com.example.model.PoliceStation
import com.example.model.RouteOption
import com.example.model.RouteType
import com.example.model.SafeTripState
import com.example.repository.SafetyRepository
import com.example.ui.components.OfflineBroadcastDialog
import com.example.ui.components.StationaryCheckDialog
import com.example.ui.theme.Navy900
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SosDangerRed
import java.util.Locale

@Composable
fun SafeRouteScreen(
    repository: SafetyRepository,
    activeTrip: SafeTripState?,
    stationaryCountdownSeconds: Int,
    currentLanguage: Language,
    onTriggerSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedRouteType by remember { mutableStateOf(RouteType.SAFEST) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Live Map & Route, 1: Nearest Police Stations

    val liveRouteDetails by repository.liveRouteDetails.collectAsState()
    val currentLiveLoc by repository.currentLiveLocation.collectAsState()
    val nearestPoliceStations by repository.nearestPoliceStations.collectAsState()
    val isCalculatingRoute by repository.isCalculatingRoute.collectAsState()

    var selectedPoliceStation by remember { mutableStateOf<PoliceStation?>(null) }
    var mapZoomLevel by remember { mutableStateOf(1f) }

    // Pulsing radar animation for live GPS marker
    val infiniteTransition = rememberInfiniteTransition(label = "gps_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasLocationPermission = fineGranted || coarseGranted
        if (hasLocationPermission) {
            repository.locationService.startLiveLocationUpdates()
        }
    }

    DisposableEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
        onDispose {}
    }

    // Default route options if still loading
    val safestRoute = liveRouteDetails?.safestRoute ?: RouteOption(
        type = RouteType.SAFEST,
        titleEn = "Safest Live Route",
        titleHi = "सबसे सुरक्षित लाइव मार्ग",
        timeMinutes = 18,
        distanceKm = 2.4,
        safetyScore = 95,
        crowdIndex = "High Pedestrian Density",
        lightingPercent = 96,
        pastIncidents = "0 incidents reported",
        policeCheckpointsCount = 3,
        tags = listOf("96% Well Lit", "3 Police Booths", "PCR Patrolled")
    )

    val fastestRoute = liveRouteDetails?.fastestRoute ?: RouteOption(
        type = RouteType.FASTEST,
        titleEn = "Fastest Live Route",
        titleHi = "सबसे तेज़ लाइव मार्ग",
        timeMinutes = 12,
        distanceKm = 1.8,
        safetyScore = 74,
        crowdIndex = "Moderate / Mixed",
        lightingPercent = 64,
        pastIncidents = "Secondary Pathway",
        policeCheckpointsCount = 1,
        tags = listOf("Dim Alleyway (250m)", "Saves 6 min")
    )

    val activeSelectedRoute = if (selectedRouteType == RouteType.SAFEST) safestRoute else fastestRoute

    // Dialogs for safety check simulations
    if (activeTrip?.isStationaryAlertActive == true) {
        StationaryCheckDialog(
            countdownSeconds = stationaryCountdownSeconds,
            currentLanguage = currentLanguage,
            onDismissSafe = { repository.dismissStationaryAlert() },
            onTriggerSos = {
                repository.dismissStationaryAlert()
                onTriggerSos()
            }
        )
    }

    if (activeTrip?.isOfflineAlertActive == true) {
        OfflineBroadcastDialog(
            lastKnownLocation = activeTrip.lastKnownLocation,
            currentLanguage = currentLanguage,
            onDismiss = { repository.dismissOfflineIncident() }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ACTIVE TRIP BANNER (If monitoring started)
        if (activeTrip != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_trip_monitoring_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SafetyGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SafetyGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = "Active",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (currentLanguage == Language.ENGLISH) "Safe Trip in Progress" else "सुरक्षित यात्रा जारी है",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF14532D)
                                )
                                Text(
                                    text = "To: ${activeTrip.destination}",
                                    fontSize = 13.sp,
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFC8E6C9)
                        ) {
                            Text(
                                text = "GPS Live",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live GPS Coordinates readout
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "GPS",
                            tint = SafetyGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLiveLoc != null) {
                                String.format(Locale.US, "Live Location: %.4f° N, %.4f° E (±%.1fm)", currentLiveLoc!!.latitude, currentLiveLoc!!.longitude, currentLiveLoc!!.accuracy)
                            } else {
                                "Live Location: 28.6139° N, 77.2090° E (Acquiring Fix)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF14532D)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (currentLanguage == Language.ENGLISH)
                            "Safety Telemetry: Auto-notifies if stationary for 90s or if connection drops."
                        else
                            "सुरक्षा जांच: 90 सेकंड स्थिर रहने पर स्वतः अलार्म बजेगा।",
                        fontSize = 12.sp,
                        color = Color(0xFF1B5E20)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulation Test Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { repository.triggerStationaryCheck() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("simulate_stationary_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = "Stationary", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simulate Stopped", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { repository.simulateOfflineIncident() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("simulate_offline_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.NoCell, contentDescription = "Offline", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simulate Offline", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { repository.endTrip() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("end_trip_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Finish")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "End Trip (Arrived Safely)" else "यात्रा समाप्त करें (सुरक्षित पहुंचे)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Sub-tabs: Live Map & Safe Route vs Nearest Police Stations
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Navy900,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .testTag("safe_route_tab_row")
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Map, contentDescription = "Map", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Live GPS Map" else "लाइव जीपीएस मैप",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                },
                modifier = Modifier.testTag("tab_live_map")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocalPolice, contentDescription = "Police", tint = SosDangerRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Police Stations (${nearestPoliceStations.size})" else "पुलिस स्टेशन (${nearestPoliceStations.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                },
                modifier = Modifier.testTag("tab_police_stations")
            )
        }

        // REAL-TIME GPS TELEMETRY STATUS BAR
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (hasLocationPermission) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (hasLocationPermission) Color(0xFFBBF7D0) else Color(0xFFFECACA)),
            modifier = Modifier.fillMaxWidth().testTag("live_gps_telemetry_bar")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (hasLocationPermission) SafetyGreen else SosDangerRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (currentLiveLoc != null) {
                                String.format(Locale.US, "GPS: %.4f° N, %.4f° E", currentLiveLoc!!.latitude, currentLiveLoc!!.longitude)
                            } else {
                                "GPS: 28.6139° N, 77.2090° E (Active)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (currentLiveLoc != null) {
                                String.format(Locale.US, "Accuracy: ±%.1f m • 24x7 Real-time Radar", currentLiveLoc!!.accuracy)
                            } else {
                                "Searching GNSS Constellation..."
                            },
                            fontSize = 10.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            repository.locationService.startLiveLocationUpdates()
                        },
                        modifier = Modifier.size(32.dp).testTag("refresh_gps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh GPS",
                            tint = Navy900,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (hasLocationPermission) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                    ) {
                        Text(
                            text = if (hasLocationPermission) "3D Fix Active" else "Permission Req",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasLocationPermission) Color(0xFF15803D) else Color(0xFFB91C1C),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // REAL TIME INTERACTIVE MAP CANVAS AREA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .testTag("safe_route_map_container"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B192C))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Street grid lines
                    val streetColor = Color(0xFF1E293B)
                    for (i in 0..6) {
                        val y = h * (i / 6f)
                        drawLine(streetColor, Offset(0f, y), Offset(w, y), strokeWidth = 2f)
                    }
                    for (i in 0..7) {
                        val x = w * (i / 7f)
                        drawLine(streetColor, Offset(x, 0f), Offset(x, h), strokeWidth = 2f)
                    }

                    // User live position coordinates on map
                    val userX = w * 0.22f
                    val userY = h * 0.72f

                    // Destination point (or selected police station point)
                    val targetX = if (selectedPoliceStation != null) w * 0.76f else w * 0.82f
                    val targetY = if (selectedPoliceStation != null) h * 0.35f else h * 0.22f

                    // Safe corridor illumination zone
                    drawCircle(
                        color = Color(0x224ADE80),
                        radius = 110f,
                        center = Offset(w * 0.5f, h * 0.48f)
                    )

                    // Draw Route line (Fastest or Safest or Direct to Police Station)
                    if (selectedPoliceStation != null) {
                        // Direct police dispatch route line
                        val policePath = Path().apply {
                            moveTo(userX, userY)
                            lineTo(w * 0.38f, h * 0.58f)
                            lineTo(w * 0.58f, h * 0.42f)
                            lineTo(targetX, targetY)
                        }
                        drawPath(
                            path = policePath,
                            color = Color(0xFFF59E0B),
                            style = Stroke(width = 8f, cap = StrokeCap.Round)
                        )
                    } else {
                        // Regular safe & fast routes
                        val fastPath = Path().apply {
                            moveTo(userX, userY)
                            lineTo(w * 0.42f, h * 0.65f)
                            lineTo(w * 0.65f, h * 0.35f)
                            lineTo(targetX, targetY)
                        }
                        drawPath(
                            path = fastPath,
                            color = if (selectedRouteType == RouteType.FASTEST) Color(0xFF38BDF8) else Color(0x4438BDF8),
                            style = Stroke(width = if (selectedRouteType == RouteType.FASTEST) 8f else 4f, cap = StrokeCap.Round)
                        )

                        val safePath = Path().apply {
                            moveTo(userX, userY)
                            lineTo(w * 0.30f, h * 0.48f)
                            lineTo(w * 0.54f, h * 0.48f)
                            lineTo(w * 0.72f, h * 0.36f)
                            lineTo(targetX, targetY)
                        }
                        drawPath(
                            path = safePath,
                            color = if (selectedRouteType == RouteType.SAFEST) Color(0xFF22C55E) else Color(0x4422C55E),
                            style = Stroke(width = if (selectedRouteType == RouteType.SAFEST) 9f else 4f, cap = StrokeCap.Round)
                        )
                    }

                    // Render Nearby Police Station Checkpoint Markers on the Map
                    val policePositions = listOf(
                        Triple(w * 0.30f, h * 0.48f, "PS 1"),
                        Triple(w * 0.54f, h * 0.48f, "PS 2"),
                        Triple(w * 0.76f, h * 0.35f, "PS 3"),
                        Triple(w * 0.46f, h * 0.25f, "PS 4")
                    )

                    policePositions.forEach { (px, py, label) ->
                        // Golden halo
                        drawCircle(color = Color(0x33FFB300), radius = 18f, center = Offset(px, py))
                        // Outer ring
                        drawCircle(color = Color(0xFFFFB300), radius = 10f, center = Offset(px, py))
                        // Inner core
                        drawCircle(color = Color(0xFF0F172A), radius = 5f, center = Offset(px, py))
                    }

                    // User Live Beacon with pulsating radar ring
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = pulseAlpha),
                        radius = pulseRadius,
                        center = Offset(userX, userY)
                    )
                    drawCircle(color = Color(0xFF0284C7), radius = 12f, center = Offset(userX, userY))
                    drawCircle(color = Color.White, radius = 5f, center = Offset(userX, userY))

                    // Destination or Police Station Pin
                    drawCircle(color = if (selectedPoliceStation != null) Color(0xFFFFB300) else Color(0xFFEF4444), radius = 15f, center = Offset(targetX, targetY))
                    drawCircle(color = Color.White, radius = 6f, center = Offset(targetX, targetY))
                }

                // Map Legend & Overlays
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(Color(0xDD000000), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF0284C7), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("You (Live GPS)", color = Color.White, fontSize = 9.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFFFB300), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Police Station", color = Color.White, fontSize = 9.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF22C55E), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Safe Corridor", color = Color.White, fontSize = 9.sp)
                    }
                }

                // Map quick controls overlay (Top right)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xCC0F172A),
                        modifier = Modifier.clickable {
                            selectedPoliceStation = null
                            mapZoomLevel = 1f
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Center", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Center GPS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // SELECTED POLICE STATION DETAIL CARD OVERLAY (if selected)
        if (selectedPoliceStation != null) {
            val station = selectedPoliceStation!!
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selected_police_station_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalPolice, contentDescription = "Police", tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (currentLanguage == Language.ENGLISH) station.nameEn else station.nameHi,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF78350F)
                                )
                                Text(
                                    text = "${station.distanceKm} km away • Heading ${station.cardinalDirection}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "24x7 Active",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (currentLanguage == Language.ENGLISH) station.addressEn else station.addressHi,
                        fontSize = 12.sp,
                        color = Color(0xFF92400E)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🚶 ${station.estimatedWalkingMin} min walk",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF78350F)
                        )
                        Text(
                            text = "🚗 ${station.estimatedDriveMin} min PCR response",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF78350F)
                        )
                        Text(
                            text = "🛡️ ${station.activePcrVans} PCR Vans",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF78350F)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${station.phoneNumber.replace(" ", "")}"))
                                context.startActivity(dialIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SosDangerRed),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("call_selected_station_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call Station", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                searchQuery = station.nameEn
                                repository.calculateLiveRoute(station.nameEn)
                                selectedTab = 0
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("navigate_to_station_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = "Navigate", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Navigate Here", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { selectedPoliceStation = null },
                            modifier = Modifier.height(44.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Dismiss", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // CONTENT BASED ON SELECTED TAB
        if (selectedTab == 1) {
            // TAB 1: NEAREST POLICE STATIONS DIRECTORY
            Text(
                text = if (currentLanguage == Language.ENGLISH) "Police Stations Sorted by Proximity to Live Location" else "निकटतम पुलिस स्टेशन (दूरी अनुसार)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            nearestPoliceStations.forEach { station ->
                PoliceStationCard(
                    station = station,
                    currentLanguage = currentLanguage,
                    isSelected = selectedPoliceStation?.id == station.id,
                    onSelect = {
                        selectedPoliceStation = station
                    },
                    onNavigate = {
                        selectedPoliceStation = station
                        searchQuery = station.nameEn
                        repository.calculateLiveRoute(station.nameEn)
                        selectedTab = 0
                    },
                    onCall = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${station.phoneNumber.replace(" ", "")}"))
                        context.startActivity(dialIntent)
                    }
                )
            }
        } else {
            // TAB 0: ROUTE SEARCH & COMPARISON
            // Destination Search Box with Real Route Calculation
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    repository.calculateLiveRoute(it)
                },
                label = { Text("Search Destination / Police Post") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (isCalculatingRoute) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = { repository.calculateLiveRoute(searchQuery) }) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Pin",
                                tint = SaffronOrange
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("route_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Quick suggested real destinations
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Parliament St Police Station",
                    "Mandir Marg Police Post",
                    "Metro Station",
                    "India Gate",
                    "Women CAW Cell",
                    "Central Market"
                ).forEach { dest ->
                    FilterChip(
                        selected = searchQuery.contains(dest),
                        onClick = {
                            searchQuery = dest
                            repository.calculateLiveRoute(dest)
                        },
                        label = { Text(dest, fontSize = 12.sp) }
                    )
                }
            }

            // REAL ROUTE OPTIONS SIDE BY SIDE
            Text(
                text = if (currentLanguage == Language.ENGLISH) "Calculated Live Route Options" else "वास्तविक मार्ग गणना",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // OPTION 1: SAFEST ROUTE
                RouteComparisonCard(
                    option = safestRoute,
                    isSelected = selectedRouteType == RouteType.SAFEST,
                    onSelect = { selectedRouteType = RouteType.SAFEST },
                    currentLanguage = currentLanguage,
                    modifier = Modifier.weight(1f).testTag("select_safest_route")
                )

                // OPTION 2: FASTEST ROUTE
                RouteComparisonCard(
                    option = fastestRoute,
                    isSelected = selectedRouteType == RouteType.FASTEST,
                    onSelect = { selectedRouteType = RouteType.FASTEST },
                    currentLanguage = currentLanguage,
                    modifier = Modifier.weight(1f).testTag("select_fastest_route")
                )
            }

            // Details of selected route
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedRouteType == RouteType.SAFEST) "Safest Corridor Telemetry" else "Fastest Route Telemetry",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${activeSelectedRoute.safetyScore}/100 Safety Score",
                            fontWeight = FontWeight.Bold,
                            color = if (activeSelectedRoute.safetyScore > 80) SafetyGreen else SaffronOrange,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    RouteMetricRow(
                        icon = Icons.Default.Lightbulb,
                        label = "Street Lighting",
                        value = "${activeSelectedRoute.lightingPercent}% Illuminated",
                        tint = if (activeSelectedRoute.lightingPercent > 80) Color(0xFFF59E0B) else Color(0xFF94A3B8)
                    )

                    RouteMetricRow(
                        icon = Icons.Default.People,
                        label = "Foot Traffic / Crowd",
                        value = activeSelectedRoute.crowdIndex,
                        tint = Navy900
                    )

                    RouteMetricRow(
                        icon = Icons.Default.LocalPolice,
                        label = "Police Posts Enroute",
                        value = "${activeSelectedRoute.policeCheckpointsCount} Manning Posts",
                        tint = SafetyGreen
                    )

                    RouteMetricRow(
                        icon = Icons.Default.Shield,
                        label = "Past Incident Record",
                        value = activeSelectedRoute.pastIncidents,
                        tint = if (activeSelectedRoute.pastIncidents.contains("0")) SafetyGreen else SosDangerRed
                    )
                }
            }

            // Quick closest police post banner in route tab
            if (nearestPoliceStations.isNotEmpty()) {
                val closest = nearestPoliceStations.first()
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF6FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth().clickable { selectedPoliceStation = closest }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalPolice, contentDescription = "Police", tint = Color(0xFF1D4ED8), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Nearest Police: ${closest.nameEn}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E3A8A)
                                )
                                Text(
                                    text = "${closest.distanceKm} km away • ${closest.estimatedDriveMin}m PCR response",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1D4ED8)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${closest.phoneNumber.replace(" ", "")}"))
                                context.startActivity(dialIntent)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color(0xFF1D4ED8), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // START TRIP BUTTON
            if (activeTrip == null) {
                Button(
                    onClick = {
                        repository.startTrip(searchQuery.ifBlank { "Safe Point" }, activeSelectedRoute)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_trip_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedRouteType == RouteType.SAFEST) SafetyGreen else Navy900
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Navigation, contentDescription = "Start")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLanguage == Language.ENGLISH)
                            "Start Monitored Trip (${activeSelectedRoute.distanceKm} km • ${activeSelectedRoute.timeMinutes} mins)"
                        else
                            "सुरक्षित यात्रा शुरू करें (${activeSelectedRoute.distanceKm} किमी • ${activeSelectedRoute.timeMinutes} मिनट)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(88.dp))
    }
}

@Composable
private fun PoliceStationCard(
    station: PoliceStation,
    currentLanguage: Language,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onNavigate: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("police_station_card_${station.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFFF59E0B) else Color(0xFFE2E8F0)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalPolice,
                            contentDescription = "Police Post",
                            tint = Color(0xFF3730A3),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) station.nameEn else station.nameHi,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = station.jurisdiction,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = "${station.distanceKm} km",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF166534),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (currentLanguage == Language.ENGLISH) station.addressEn else station.addressHi,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (station.hasWomenHelpDesk) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFCE7F3)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFF9D174D), modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Women Help Desk",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9D174D)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "🚶 ${station.estimatedWalkingMin} min walk",
                        fontSize = 10.sp,
                        color = Color(0xFF334155),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "Heading ${station.cardinalDirection}",
                        fontSize = 10.sp,
                        color = Color(0xFF334155),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier.weight(1f).height(40.dp).testTag("call_station_${station.id}"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = SosDangerRed, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 12.sp, color = SosDangerRed, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigate,
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                    modifier = Modifier.weight(1f).height(40.dp).testTag("navigate_station_${station.id}"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = "Navigate", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Route", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RouteComparisonCard(
    option: RouteOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
    currentLanguage: Language,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onSelect),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                if (option.type == RouteType.SAFEST) Color(0xFFEFFDF5) else Color(0xFFF0F9FF)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) {
                if (option.type == RouteType.SAFEST) SafetyGreen else Color(0xFF0284C7)
            } else {
                Color(0xFFCBD5E1)
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (option.type == RouteType.SAFEST) "🛡️ Safest" else "⚡ Fastest",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (option.type == RouteType.SAFEST) SafetyGreen else Color(0xFF0369A1)
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (option.safetyScore > 85) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = "${option.safetyScore}/100",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (option.safetyScore > 85) Color(0xFF15803D) else Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${option.timeMinutes} mins",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${option.distanceKm} km",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (option.type == RouteType.SAFEST) "${option.lightingPercent}% well-lit" else "Saves time",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (option.type == RouteType.SAFEST) SafetyGreen else Color(0xFF0284C7)
            )
        }
    }
}

@Composable
private fun RouteMetricRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
