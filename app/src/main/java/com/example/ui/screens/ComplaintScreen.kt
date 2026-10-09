package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Complaint
import com.example.model.ComplaintStatus
import com.example.model.IncidentCategory
import com.example.model.Language
import com.example.repository.SafetyRepository
import com.example.ui.components.PdfReportDialog
import com.example.ui.components.QrCodeView
import com.example.ui.components.VoiceRecorderDialog
import com.example.ui.theme.Navy900
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.SafetyGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintScreen(
    repository: SafetyRepository,
    complaints: List<Complaint>,
    selectedComplaint: Complaint?,
    onClearSelectedComplaint: () -> Unit,
    currentLanguage: Language,
    modifier: Modifier = Modifier
) {
    var screenTab by remember { mutableStateOf(if (selectedComplaint != null) 1 else 0) } // 0: File New, 1: Track
    var activeComplaintToTrack by remember { mutableStateOf<Complaint?>(selectedComplaint ?: complaints.firstOrNull()) }
    var showVoiceRecorder by remember { mutableStateOf(false) }
    var showPdfDialogForComplaint by remember { mutableStateOf<Complaint?>(null) }

    // Form Fields
    var selectedCategory by remember { mutableStateOf(IncidentCategory.HARASSMENT) }
    var locationInput by remember { mutableStateOf("") }
    var approximateTimeInput by remember {
        mutableStateOf(SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date()))
    }
    var perpetratorDescInput by remember { mutableStateOf("") }
    var vehicleDetailsInput by remember { mutableStateOf("") }
    var narrativeInput by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    var aiStatusNotice by remember { mutableStateOf<String?>(null) }
    var isAiStructuring by remember { mutableStateOf(false) }

    if (showVoiceRecorder) {
        VoiceRecorderDialog(
            repository = repository,
            currentLanguage = currentLanguage,
            onDismiss = { showVoiceRecorder = false },
            onTranscriptReady = { transcript, aiResult ->
                narrativeInput = aiResult?.cleanedNarrative ?: transcript
                if (aiResult != null) {
                    selectedCategory = aiResult.category
                    if (aiResult.location.isNotBlank()) locationInput = aiResult.location
                    if (aiResult.approximateTime.isNotBlank()) approximateTimeInput = aiResult.approximateTime
                    if (aiResult.perpetratorDescription.isNotBlank()) perpetratorDescInput = aiResult.perpetratorDescription
                    if (aiResult.vehicleDetails.isNotBlank()) vehicleDetailsInput = aiResult.vehicleDetails
                    aiStatusNotice = "Auto-structured via ${aiResult.usedModelName}"
                }
            }
        )
    }

    if (showPdfDialogForComplaint != null) {
        PdfReportDialog(
            complaint = showPdfDialogForComplaint!!,
            currentLanguage = currentLanguage,
            onDismiss = { showPdfDialogForComplaint = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Header: File Complaint vs Track Existing
        TabRow(
            selectedTabIndex = screenTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Navy900,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = screenTab == 0,
                onClick = { screenTab = 0 },
                text = {
                    Text(
                        text = if (currentLanguage == Language.ENGLISH) "1. File Complaint" else "1. शिकायत दर्ज करें",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                },
                modifier = Modifier.testTag("tab_file_complaint")
            )
            Tab(
                selected = screenTab == 1,
                onClick = { screenTab = 1 },
                text = {
                    Text(
                        text = if (currentLanguage == Language.ENGLISH) "2. Track & QR (${complaints.size})" else "2. ट्रैकिंग स्थिति (${complaints.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                },
                modifier = Modifier.testTag("tab_track_complaint")
            )
        }

        if (screenTab == 0) {
            // FILE NEW COMPLAINT FORM
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Input Options banner: Type or Voice
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Choose Input Method" else "विवरण दर्ज करने का तरीका चुनें",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showVoiceRecorder = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("mic_voice_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Mic",
                                    tint = SaffronOrange
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLanguage == Language.ENGLISH) "Voice Record" else "ध्वनि रिकॉर्ड",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { /* Focus narrative */ },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Type")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLanguage == Language.ENGLISH) "Type Text" else "टाइप करें",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Auto-Structured Report Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security",
                                tint = SafetyGreen
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentLanguage == Language.ENGLISH) "Auto-Structured Incident Fields" else "संरचित घटना विवरण",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Category Selection Chips
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Incident Category:" else "घटना का प्रकार:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IncidentCategory.values().forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = {
                                        Text(
                                            text = if (currentLanguage == Language.ENGLISH) cat.displayNameEn else cat.displayNameHi,
                                            fontSize = 13.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Navy900,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Location with GPS auto-fill
                        OutlinedTextField(
                            value = locationInput,
                            onValueChange = { locationInput = it; formError = null },
                            label = { Text("Incident Location / Landmark") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Location") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    val loc = repository.currentLiveLocation.value
                                    if (loc != null) {
                                        locationInput = String.format(Locale.US, "GPS: %.4f° N, %.4f° E (Accuracy: ±%.1fm)", loc.latitude, loc.longitude, loc.accuracy)
                                    } else {
                                        locationInput = "GPS: 28.6139° N, 77.2090° E (Acquiring Satellites)"
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "Current GPS",
                                        tint = SafetyGreen
                                    )
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("complaint_location_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Approximate Time
                        OutlinedTextField(
                            value = approximateTimeInput,
                            onValueChange = { approximateTimeInput = it },
                            label = { Text("Approximate Time & Date") },
                            leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = "Time") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("complaint_time_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Perpetrator Description
                        OutlinedTextField(
                            value = perpetratorDescInput,
                            onValueChange = { perpetratorDescInput = it },
                            label = { Text("Perpetrator Description (Height, build, clothing)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Perpetrator") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("complaint_perpetrator_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Vehicle Details
                        OutlinedTextField(
                            value = vehicleDetailsInput,
                            onValueChange = { vehicleDetailsInput = it },
                            label = { Text("Vehicle Details (Make, color, registration number)") },
                            leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Vehicle") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("complaint_vehicle_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Narrative
                        OutlinedTextField(
                            value = narrativeInput,
                            onValueChange = { narrativeInput = it; formError = null },
                            label = { Text("Incident Narrative (Detailed Statement)") },
                            placeholder = { Text("Describe sequence of events clearly...") },
                            minLines = 4,
                            maxLines = 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("complaint_narrative_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // AI Auto-Structure Action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (aiStatusNotice != null) {
                                Text(
                                    text = "✨ $aiStatusNotice",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SafetyGreen
                                )
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            OutlinedButton(
                                onClick = {
                                    if (narrativeInput.isNotBlank()) {
                                        scope.launch {
                                            isAiStructuring = true
                                            val aiRes = repository.analyzeIncidentWithAi(narrativeInput)
                                            isAiStructuring = false
                                            selectedCategory = aiRes.category
                                            if (aiRes.location.isNotBlank()) locationInput = aiRes.location
                                            if (aiRes.approximateTime.isNotBlank()) approximateTimeInput = aiRes.approximateTime
                                            if (aiRes.perpetratorDescription.isNotBlank()) perpetratorDescInput = aiRes.perpetratorDescription
                                            if (aiRes.vehicleDetails.isNotBlank()) vehicleDetailsInput = aiRes.vehicleDetails
                                            aiStatusNotice = "Auto-structured via ${aiRes.usedModelName}"
                                        }
                                    } else {
                                        formError = "Please type or dictate narrative statement first"
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isAiStructuring
                            ) {
                                if (isAiStructuring) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Analyzing...", fontSize = 12.sp)
                                } else {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", tint = SaffronOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("AI Auto-Extract", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (formError != null) {
                            Text(
                                text = formError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (narrativeInput.isBlank()) {
                                    formError = "Please describe the incident in the narrative field"
                                } else if (locationInput.isBlank()) {
                                    formError = "Please specify incident location"
                                } else {
                                    val newComplaint = repository.submitComplaint(
                                        category = selectedCategory,
                                        location = locationInput,
                                        approximateTime = approximateTimeInput,
                                        perpetratorDescription = perpetratorDescInput,
                                        vehicleDetails = vehicleDetailsInput,
                                        narrative = narrativeInput
                                    )
                                    // Reset form and switch to Tracking view with this new complaint!
                                    narrativeInput = ""
                                    perpetratorDescInput = ""
                                    vehicleDetailsInput = ""
                                    activeComplaintToTrack = newComplaint
                                    screenTab = 1
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("submit_complaint_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Submit")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentLanguage == Language.ENGLISH)
                                    "Submit & Generate QR Tracking Slip"
                                else
                                    "शिकायत दर्ज करें एवं QR प्राप्त करें",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        } else {
            // TRACKING VIEW & QR CODE
            val complaint = activeComplaintToTrack ?: complaints.firstOrNull()

            if (complaint == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No complaints filed yet. File a new complaint to track.",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Complaint Selector if multiple exist
                    if (complaints.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            complaints.forEach { c ->
                                FilterChip(
                                    selected = c.id == complaint.id,
                                    onClick = { activeComplaintToTrack = c },
                                    label = { Text(c.id, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    }

                    // QR Code & Identification Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "DIGITAL COMPLAINT ACKNOWLEDGMENT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = complaint.id,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Navy900
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Deterministic QR Code view
                            QrCodeView(
                                data = complaint.qrCodePayload,
                                size = 160.dp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Scan or tap to verify with National Women Safety Registry",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Download Report as PDF Button
                            OutlinedButton(
                                onClick = { showPdfDialogForComplaint = complaint },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("open_pdf_dialog_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "PDF",
                                    tint = Navy900
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentLanguage == Language.ENGLISH) "Download Report as PDF" else "रिपोर्ट PDF डाउनलोड करें",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 4-STEP TRACKING STEPPER
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = if (currentLanguage == Language.ENGLISH) "Investigation Tracking Stepper" else "जांच की स्थिति",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            TrackingStepItem(
                                stepNumber = 1,
                                title = "1. Registered",
                                subtitle = "Cryptographically signed and logged with Police Control Room",
                                isCompleted = complaint.status.stepIndex >= 1,
                                isCurrent = complaint.status.stepIndex == 1
                            )

                            TrackingStepItem(
                                stepNumber = 2,
                                title = "2. Reviewing",
                                subtitle = "Duty Officer inspecting statement and incident location coordinates",
                                isCompleted = complaint.status.stepIndex >= 2,
                                isCurrent = complaint.status.stepIndex == 2
                            )

                            TrackingStepItem(
                                stepNumber = 3,
                                title = "3. Assigned to Officer",
                                subtitle = "${complaint.officerName ?: "Inspector in Charge"} (${complaint.badgeNumber ?: "DL-POL"}) • ${complaint.policeStation ?: "Station"}",
                                isCompleted = complaint.status.stepIndex >= 3,
                                isCurrent = complaint.status.stepIndex == 3
                            )

                            TrackingStepItem(
                                stepNumber = 4,
                                title = "4. Action Taken",
                                subtitle = "Perpetrator identified / Area patrol intensified / Case closed",
                                isCompleted = complaint.status.stepIndex >= 4,
                                isCurrent = complaint.status.stepIndex == 4,
                                isLast = true
                            )
                        }
                    }

                    // Structured Summary details
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Incident Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            DetailField(label = "Category", value = complaint.category.displayNameEn)
                            DetailField(label = "Location", value = complaint.location)
                            DetailField(label = "Time", value = complaint.approximateTime)
                            if (complaint.perpetratorDescription.isNotBlank()) {
                                DetailField(label = "Perpetrator", value = complaint.perpetratorDescription)
                            }
                            if (complaint.vehicleDetails.isNotBlank()) {
                                DetailField(label = "Vehicle", value = complaint.vehicleDetails)
                            }
                            DetailField(label = "Narrative", value = complaint.narrative)
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun TrackingStepItem(
    stepNumber: Int,
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLast: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> SafetyGreen
                            isCurrent -> SaffronOrange
                            else -> Color(0xFFCBD5E1)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = "$stepNumber",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(if (isCompleted) SafetyGreen else Color(0xFFCBD5E1))
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isCompleted -> MaterialTheme.colorScheme.onSurface
                    isCurrent -> SaffronOrange
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
