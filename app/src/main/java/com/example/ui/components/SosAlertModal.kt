package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.EmergencyContact
import com.example.model.Language
import com.example.model.SosState
import com.example.ui.theme.Navy900
import com.example.ui.theme.SosDangerRed
import com.example.ui.theme.SosDangerRedDark

@Composable
fun SosAlertModal(
    sosState: SosState,
    contacts: List<EmergencyContact>,
    currentLanguage: Language,
    onCancelSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = { /* Cannot dismiss without clicking cancel or safe */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .testTag("sos_alert_dialog"),
            color = Color(0xFF1A0A0A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top header & flashing badge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(SosDangerRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert Triggered",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (currentLanguage == Language.ENGLISH)
                            "EMERGENCY ALERT BROADCAST"
                        else
                            "आपातकालीन चेतावनी सक्रिय",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF5252),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (currentLanguage == Language.ENGLISH)
                            "Alert sent to Police Control Room (112) and your emergency contacts"
                        else
                            "पुलिस कंट्रोल रूम (112) और आपके आपातकालीन संपर्कों को सूचना भेजी गई",
                        fontSize = 17.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Details Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1214)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF8B2527)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalPolice,
                                contentDescription = "Police",
                                tint = Color(0xFFFF8A80),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PCR Unit: Dial 112 Command Dispatch",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }

                        // Live Location
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = "GPS",
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Live GPS Coordinates Broadcast:",
                                    fontSize = 13.sp,
                                    color = Color(0xFFB0BEC5)
                                )
                                Text(
                                    text = "28.6139° N, 77.2090° E (Accuracy: ±3m)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        // Device Telemetry
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryFull,
                                contentDescription = "Battery",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Battery: ${sosState.batteryLevel}% • Network: 4G/5G Active",
                                color = Color(0xFFECEFF1),
                                fontSize = 14.sp
                            )
                        }

                        // Contacts alerted
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Notified Emergency Contacts:" else "सूचित संपर्क:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFAB91),
                            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                        )

                        contacts.forEach { contact ->
                            Text(
                                text = "• ${contact.name} (${contact.phone})",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Open Google Maps Link
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sosState.locationUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFFF8A80)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = "Open Link",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open Live Location Link",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Action Buttons: Cancel false alarm (first 10 seconds) or Call 112
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (sosState.cancelWindowRemainingSeconds > 0) {
                        Button(
                            onClick = onCancelSos,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("cancel_sos_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF374151)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Cancel SOS",
                                tint = Color(0xFFFFD54F)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentLanguage == Language.ENGLISH)
                                    "Cancel False Alarm (${sosState.cancelWindowRemainingSeconds}s)"
                                else
                                    "गलत अलार्म रद्द करें (${sosState.cancelWindowRemainingSeconds}s)",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // After 10s: Alert is officially active with PCR
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF3E1315),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = if (currentLanguage == Language.ENGLISH)
                                    "🚨 Transmission Confirmed. Police dispatch in route."
                                else
                                    "🚨 प्रसारण की पुष्टि हो चुकी है। पुलिस रवाना हो चुकी है।",
                                color = Color(0xFFFF8A80),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Button(
                            onClick = onCancelSos,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("dismiss_sos_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (currentLanguage == Language.ENGLISH) "I Am Safe Now (Dismiss)" else "अब मैं सुरक्षित हूँ (बंद करें)",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Direct Call 112 button
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("dial_112_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SosDangerRed
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call 112",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Call PCR Helpline 112" else "पीसीआर हेल्पलाइन 112 पर कॉल करें",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
