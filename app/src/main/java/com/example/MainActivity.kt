package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Complaint
import com.example.model.Language
import com.example.repository.SafetyRepository
import com.example.ui.components.DhritiHeader
import com.example.ui.components.FaqDialog
import com.example.ui.components.PersistentSosButton
import com.example.ui.components.PrivacyDialog
import com.example.ui.components.SessionLockDialog
import com.example.ui.components.SosAlertModal
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ComplaintScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SafeRouteScreen
import com.example.ui.theme.DhritiTheme
import com.example.ui.theme.Navy900
import com.example.ui.theme.SaffronOrange

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val systemDark = isSystemInDarkTheme()
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            val repository = remember { SafetyRepository(context, coroutineScope) }

            val currentUser by repository.currentUser.collectAsState()
            val isDarkMode by repository.isDarkMode.collectAsState()
            val currentLanguage by repository.currentLanguage.collectAsState()
            val sosState by repository.sosState.collectAsState()
            val activeTrip by repository.activeTrip.collectAsState()
            val stationaryCountdownSeconds by repository.stationaryCountdownSeconds.collectAsState()
            val isSessionLocked by repository.isSessionLocked.collectAsState()
            val complaints by repository.complaints.collectAsState()
            val nearestPoliceStations by repository.nearestPoliceStations.collectAsState()

            var currentTab by remember { mutableIntStateOf(0) } // 0: Home, 1: Complaint, 2: Safe Route
            var selectedComplaintForTracking by remember { mutableStateOf<Complaint?>(null) }
            var showPrivacyDialog by remember { mutableStateOf(false) }
            var showFaqDialog by remember { mutableStateOf(false) }

            // Back handler
            BackHandler(enabled = showFaqDialog || showPrivacyDialog || (currentUser != null && (currentTab != 0 || selectedComplaintForTracking != null))) {
                if (showFaqDialog) {
                    showFaqDialog = false
                } else if (showPrivacyDialog) {
                    showPrivacyDialog = false
                } else if (selectedComplaintForTracking != null) {
                    selectedComplaintForTracking = null
                } else if (currentTab != 0) {
                    currentTab = 0
                }
            }

            DhritiTheme(darkTheme = isDarkMode) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("dhriti_main_scaffold"),
                        topBar = {
                            DhritiHeader(
                                isDarkMode = isDarkMode,
                                currentLanguage = currentLanguage,
                                onToggleTheme = { repository.toggleDarkMode() },
                                onToggleLanguage = {
                                    repository.setLanguage(
                                        if (currentLanguage == Language.ENGLISH) Language.HINDI else Language.ENGLISH
                                    )
                                },
                                onOpenPrivacy = { showPrivacyDialog = true },
                                onOpenFaq = { showFaqDialog = true },
                                onLockSession = { repository.lockSessionNow() },
                                isLoggedIn = currentUser != null
                            )
                        },
                        bottomBar = {
                            if (currentUser != null) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    tonalElevation = 8.dp,
                                    modifier = Modifier.testTag("bottom_nav_bar")
                                ) {
                                    // Tab 0: Home
                                    NavigationBarItem(
                                        selected = currentTab == 0,
                                        onClick = {
                                            currentTab = 0
                                            selectedComplaintForTracking = null
                                            repository.touchUserActivity()
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                                                contentDescription = "Home"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = if (currentLanguage == Language.ENGLISH) "Home" else "मुख्य",
                                                fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Navy900,
                                            selectedTextColor = Navy900,
                                            indicatorColor = Color(0xFFE2E8F0)
                                        ),
                                        modifier = Modifier.testTag("nav_item_home")
                                    )

                                    // Tab 1: Complaint
                                    NavigationBarItem(
                                        selected = currentTab == 1,
                                        onClick = {
                                            currentTab = 1
                                            repository.touchUserActivity()
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentTab == 1) Icons.Default.AddComment else Icons.Outlined.AddComment,
                                                contentDescription = "Complaint"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = if (currentLanguage == Language.ENGLISH) "Complaint" else "शिकायत",
                                                fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Navy900,
                                            selectedTextColor = Navy900,
                                            indicatorColor = Color(0xFFE2E8F0)
                                        ),
                                        modifier = Modifier.testTag("nav_item_complaint")
                                    )

                                    // Tab 2: Safe Route
                                    NavigationBarItem(
                                        selected = currentTab == 2,
                                        onClick = {
                                            currentTab = 2
                                            selectedComplaintForTracking = null
                                            repository.touchUserActivity()
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentTab == 2) Icons.Default.Route else Icons.Outlined.Route,
                                                contentDescription = "Safe Route"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = if (currentLanguage == Language.ENGLISH) "Safe Route" else "सुरक्षित मार्ग",
                                                fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Navy900,
                                            selectedTextColor = Navy900,
                                            indicatorColor = Color(0xFFE2E8F0)
                                        ),
                                        modifier = Modifier.testTag("nav_item_safe_route")
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (currentUser == null) {
                                AuthScreen(
                                    repository = repository,
                                    currentLanguage = currentLanguage
                                )
                            } else {
                                Crossfade(
                                    targetState = currentTab,
                                    animationSpec = tween(150),
                                    label = "screen_crossfade"
                                ) { tab ->
                                    when (tab) {
                                        0 -> HomeScreen(
                                            user = currentUser!!,
                                            activeTrip = activeTrip,
                                            complaints = complaints,
                                            nearestPoliceStations = nearestPoliceStations,
                                            currentLanguage = currentLanguage,
                                            onNavigateToComplaint = {
                                                selectedComplaintForTracking = null
                                                currentTab = 1
                                            },
                                            onNavigateToRoute = { currentTab = 2 },
                                            onOpenFaq = { showFaqDialog = true },
                                            onSelectComplaint = { complaint ->
                                                selectedComplaintForTracking = complaint
                                                currentTab = 1
                                            }
                                        )

                                        1 -> ComplaintScreen(
                                            repository = repository,
                                            complaints = complaints,
                                            selectedComplaint = selectedComplaintForTracking,
                                            onClearSelectedComplaint = { selectedComplaintForTracking = null },
                                            currentLanguage = currentLanguage
                                        )

                                        2 -> SafeRouteScreen(
                                            repository = repository,
                                            activeTrip = activeTrip,
                                            stationaryCountdownSeconds = stationaryCountdownSeconds,
                                            currentLanguage = currentLanguage,
                                            onTriggerSos = { repository.triggerSos() }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Persistent Floating SOS button on all post-login screens at bottom-left (hidden while typing)
                    @OptIn(ExperimentalLayoutApi::class)
                    if (currentUser != null && !WindowInsets.isImeVisible) {
                        PersistentSosButton(
                            onTriggerSos = { repository.triggerSos() },
                            currentLanguage = currentLanguage,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .navigationBarsPadding()
                                .padding(start = 16.dp, bottom = 86.dp)
                        )
                    }

                    // Emergency SOS Alert Modal (Triggered on 5-second long-press or stationary timeout)
                    if (sosState.isActive && currentUser != null) {
                        SosAlertModal(
                            sosState = sosState,
                            contacts = currentUser!!.emergencyContacts,
                            currentLanguage = currentLanguage,
                            onCancelSos = { repository.cancelSos() }
                        )
                    }

                    // 5-minute inactivity session lock modal
                    if (isSessionLocked && currentUser != null) {
                        SessionLockDialog(
                            userEmail = currentUser!!.email,
                            currentLanguage = currentLanguage,
                            onUnlock = { pass -> repository.unlockSession(pass) },
                            onLogout = { repository.logout() }
                        )
                    }

                    // Privacy & Security Information Modal
                    if (showPrivacyDialog) {
                        PrivacyDialog(
                            currentLanguage = currentLanguage,
                            onDismiss = { showPrivacyDialog = false },
                            onDeleteAllData = { repository.deleteAllData() }
                        )
                    }

                    // Safety FAQ & Emergency Help Guide Modal
                    if (showFaqDialog) {
                        FaqDialog(
                            currentLanguage = currentLanguage,
                            onDismiss = { showFaqDialog = false }
                        )
                    }
                }
            }
        }
    }
}
