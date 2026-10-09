package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Complaint
import com.example.model.ComplaintStatus
import com.example.model.Language
import com.example.model.PoliceStation
import com.example.model.SafeTripState
import com.example.model.User
import com.example.ui.theme.Navy900
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SosDangerRed

@Composable
fun HomeScreen(
    user: User,
    activeTrip: SafeTripState?,
    complaints: List<Complaint>,
    nearestPoliceStations: List<PoliceStation> = emptyList(),
    currentLanguage: Language,
    onNavigateToComplaint: () -> Unit,
    onNavigateToRoute: () -> Unit,
    onOpenFaq: () -> Unit,
    onSelectComplaint: (Complaint) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Greeting card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (currentLanguage == Language.ENGLISH)
                            "Namaste, ${user.fullName} 🙏"
                        else
                            "नमस्ते, ${user.fullName} 🙏",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Aadhaar: ${user.maskedAadhaar}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Current Safety Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("safety_status_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (activeTrip != null) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (activeTrip != null) SaffronOrange else SafetyGreen
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (activeTrip != null) SaffronOrange else SafetyGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (activeTrip != null) Icons.Default.Navigation else Icons.Default.CheckCircle,
                            contentDescription = "Status",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (activeTrip != null) {
                                if (currentLanguage == Language.ENGLISH) "Trip Active & Monitored" else "सुरक्षित यात्रा सक्रिय"
                            } else {
                                if (currentLanguage == Language.ENGLISH) "You are Safe" else "आप सुरक्षित हैं"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTrip != null) Color(0xFF9A3412) else Color(0xFF14532D)
                        )

                        Text(
                            text = if (activeTrip != null) {
                                if (currentLanguage == Language.ENGLISH)
                                    "Route to: ${activeTrip.destination} • Telemetry Active"
                                else
                                    "गंतव्य: ${activeTrip.destination} • निगरानी चालू"
                            } else {
                                if (currentLanguage == Language.ENGLISH)
                                    "PCR 112 Ready • 2 Emergency Contacts Linked"
                                else
                                    "पीसीआर 112 तैयार • 2 आपातकालीन संपर्क जुड़े हैं"
                            },
                            fontSize = 13.sp,
                            color = if (activeTrip != null) Color(0xFFB45309) else Color(0xFF166534)
                        )
                    }

                    if (activeTrip != null) {
                        Button(
                            onClick = onNavigateToRoute,
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Nearest Police Station Live Radar Card
        if (nearestPoliceStations.isNotEmpty()) {
            val closestStation = nearestPoliceStations.first()
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nearest_police_station_home_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF3B82F6))
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
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2563EB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalPolice,
                                        contentDescription = "Police",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (currentLanguage == Language.ENGLISH) "Nearest Police Station (Live GPS)" else "निकटतम पुलिस स्टेशन (लाइव)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1E3A8A)
                                    )
                                    Text(
                                        text = if (currentLanguage == Language.ENGLISH) closestStation.nameEn else closestStation.nameHi,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1D4ED8)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDBEAFE)
                            ) {
                                Text(
                                    text = "${closestStation.distanceKm} km away",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E40AF),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (currentLanguage == Language.ENGLISH)
                                "${closestStation.addressEn} • 🚗 ${closestStation.estimatedDriveMin} min PCR response • Women Help Desk"
                            else
                                "${closestStation.addressHi} • 🚗 ${closestStation.estimatedDriveMin} मिनट पुलिस रिस्पांस • महिला हेल्प डेस्क",
                            fontSize = 12.sp,
                            color = Color(0xFF1E40AF)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${closestStation.phoneNumber.replace(" ", "")}"))
                                    context.startActivity(dialIntent)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("call_nearest_station_home_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SosDangerRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLanguage == Language.ENGLISH) "Call Station" else "कॉल करें",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = onNavigateToRoute,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("view_nearest_station_map_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = "View Map", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLanguage == Language.ENGLISH) "View on Map" else "मैप पर देखें",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick-action Cards (File Complaint, Start Safe Trip, FAQ & Helplines)
        item {
            Text(
                text = if (currentLanguage == Language.ENGLISH) "Quick Actions" else "त्वरित सेवाएं",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Action 1: File Complaint
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onNavigateToComplaint)
                        .testTag("action_file_complaint"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddComment,
                                contentDescription = "Complaint",
                                tint = Navy900
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "File Complaint" else "शिकायत दर्ज करें",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Text or voice report" else "लिखित या आवाज़ में",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action 2: Start Safe Trip
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onNavigateToRoute)
                        .testTag("action_start_safe_trip"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFEDD5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = "Safe Route",
                                tint = SaffronOrange
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Safe Route" else "सुरक्षित मार्ग",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Fastest vs Safest" else "निगरानी व नेविगेशन",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Action 3: Safety FAQ & Helplines banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenFaq)
                    .testTag("action_open_faq_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Help,
                                contentDescription = "FAQ",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (currentLanguage == Language.ENGLISH) "Safety FAQ & Help Guide" else "सुरक्षा अक्सर पूछे जाने वाले सवाल",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (currentLanguage == Language.ENGLISH) "Emergency guide, SOS walkthrough & 112 hotline" else "आपातकालीन गाइड, एसओएस व 112 हेल्पलाइन",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open FAQ",
                        tint = Navy900
                    )
                }
            }
        }

        // Emergency Contacts Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("emergency_contacts_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = "Contacts",
                                tint = Navy900,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentLanguage == Language.ENGLISH) "Emergency Contacts" else "आपातकालीन संपर्क",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = "2 Linked",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafetyGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    user.emergencyContacts.forEach { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = contact.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = contact.phone,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            // Call Action button
                            IconButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone.replace(" ", "")}"))
                                    context.startActivity(dialIntent)
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call ${contact.name}",
                                    tint = SafetyGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Complaints List with Status Tags
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentLanguage == Language.ENGLISH) "Recent Complaints" else "हाल की शिकायतें",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${complaints.size} Total",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (complaints.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "No complaints",
                            tint = SafetyGreen,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "No Active Complaints" else "कोई सक्रिय शिकायत नहीं",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (currentLanguage == Language.ENGLISH)
                                "You haven't filed any complaints yet. All clear."
                            else
                                "अभी तक कोई शिकायत दर्ज नहीं की गई है।",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(complaints) { complaint ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectComplaint(complaint) }
                    .testTag("complaint_item_${complaint.id}"),
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
                            text = complaint.id,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        // Status Tag
                        val (statusBg, statusText) = when (complaint.status) {
                            ComplaintStatus.REGISTERED -> Color(0xFFEFF6FF) to Color(0xFF1D4ED8)
                            ComplaintStatus.REVIEWING -> Color(0xFFFFFBEB) to Color(0xFFB45309)
                            ComplaintStatus.ASSIGNED_TO_OFFICER -> Color(0xFFF0FDF4) to Color(0xFF15803D)
                            ComplaintStatus.ACTION_TAKEN -> Color(0xFFECFDF5) to Color(0xFF047857)
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = statusBg
                        ) {
                            Text(
                                text = if (currentLanguage == Language.ENGLISH)
                                    complaint.status.displayNameEn
                                else
                                    complaint.status.displayNameHi,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = complaint.category.displayNameEn,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "📍 ${complaint.location}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    Text(
                        text = complaint.narrative,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "QR Ready",
                                tint = Navy900,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Track & QR Slip",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Navy900
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Details",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }

        // Extra space for bottom nav + floating SOS button
        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}
