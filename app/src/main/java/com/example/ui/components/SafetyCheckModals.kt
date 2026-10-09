package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoCell
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Language
import com.example.ui.theme.Navy900
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SosDangerRed

/**
 * 45-second countdown alert when user is stationary for 90s mid-trip
 */
@Composable
fun StationaryCheckDialog(
    countdownSeconds: Int,
    currentLanguage: Language,
    onDismissSafe: () -> Unit,
    onTriggerSos: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissSafe,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("stationary_check_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { countdownSeconds / 45f },
                        modifier = Modifier.size(90.dp),
                        color = SaffronOrange,
                        strokeWidth = 6.dp,
                        trackColor = Color(0x33FF7722),
                    )
                    Text(
                        text = "${countdownSeconds}s",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = SaffronOrange
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (currentLanguage == Language.ENGLISH) "Are you safe?" else "क्या आप सुरक्षित हैं?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (currentLanguage == Language.ENGLISH)
                        "You have been stationary for 90 seconds. If not dismissed in $countdownSeconds seconds, an emergency alert will be dispatched to Police (112)."
                    else
                        "आप 90 सेकंड से एक ही जगह पर हैं। यदि $countdownSeconds सेकंड में पुष्टि नहीं हुई तो पुलिस (112) को चेतावनी भेजी जाएगी।",
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Safe Dismissal
                Button(
                    onClick = onDismissSafe,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("dismiss_stationary_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Safe")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLanguage == Language.ENGLISH) "I Am Safe (Dismiss)" else "मैं सुरक्षित हूँ (बंद करें)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Emergency Trigger Immediate
                Button(
                    onClick = onTriggerSos,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("trigger_sos_from_stationary_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SosDangerRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = "SOS")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLanguage == Language.ENGLISH) "Emergency: Trigger SOS Now" else "आपातकाल: तुरंत SOS भेजें",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Offline mid-trip broadcast simulator modal
 */
@Composable
fun OfflineBroadcastDialog(
    lastKnownLocation: String,
    currentLanguage: Language,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("offline_broadcast_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF201309))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(SaffronOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NoCell,
                        contentDescription = "Offline Broadcast",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (currentLanguage == Language.ENGLISH) "LAST KNOWN LOCATION BROADCAST" else "अंतिम ज्ञात स्थान प्रसारण",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFCC80),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (currentLanguage == Language.ENGLISH)
                        "Network connectivity was lost during your active trip. The server automatically dispatched your last verified GPS beacon to PCR 112 and your emergency contacts."
                    else
                        "यात्रा के दौरान नेटवर्क संपर्क टूट गया था। अंतिम सत्यापित जीपीएस स्थान आपातकालीन संपर्कों और पुलिस को भेज दिया गया है।",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFFF5E0D0),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF381F0E),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📍 $lastKnownLocation",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFD54F),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (currentLanguage == Language.ENGLISH) "Acknowledge & Continue" else "समझ लिया",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Privacy & Data Governance Dialog
 */
@Composable
fun PrivacyDialog(
    currentLanguage: Language,
    onDismiss: () -> Unit,
    onDeleteAllData: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("privacy_settings_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = Navy900,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Privacy & Security" else "गोपनीयता और सुरक्षा",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Security guarantees
                SecurityPillar(
                    title = "End-to-End Encryption (AES-256-GCM)",
                    desc = "All incident narratives, live coordinates, and personal contacts are encrypted on your device using PBKDF2 derived keys."
                )

                SecurityPillar(
                    title = "Zero Plaintext Aadhaar Storage",
                    desc = "Aadhaar numbers are instantly masked (XXXX-XXXX-1234) and salted with client-side SHA-256 hashing. Raw numbers never enter storage."
                )

                SecurityPillar(
                    title = "Memory-Only Session JWT",
                    desc = "Auth tokens are stored exclusively in RAM and expire after inactivity. No plain session cookies or localStorage."
                )

                SecurityPillar(
                    title = "Permission Transparency",
                    desc = "GPS, microphone, and vibrator are accessed strictly on-demand during active trips, voice filing, and SOS triggers."
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!showDeleteConfirm) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("delete_data_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SosDangerRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Delete")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Delete All My Data" else "मेरा सारा डेटा मिटाएं",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "⚠️ Confirm Permanent Wipe",
                                fontWeight = FontWeight.Bold,
                                color = SosDangerRed,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "This will immediately wipe your encrypted profile, token, and complaint history from device memory.",
                                fontSize = 13.sp,
                                color = Color(0xFF5D1010),
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showDeleteConfirm = false },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = {
                                        onDeleteAllData()
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = SosDangerRed)
                                ) {
                                    Text("Wipe Data", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityPillar(title: String, desc: String) {
    Row(modifier = Modifier.padding(bottom = 12.dp)) {
        Icon(
            imageVector = Icons.Default.Key,
            contentDescription = "Key",
            tint = SafetyGreen,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 5-minute inactivity session lock modal
 */
@Composable
fun SessionLockDialog(
    userEmail: String,
    currentLanguage: Language,
    onUnlock: (String) -> Boolean,
    onLogout: () -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = { /* Cannot dismiss without unlock or logout */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("session_lock_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Navy900),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (currentLanguage == Language.ENGLISH) "Session Locked" else "सत्र लॉक किया गया",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (currentLanguage == Language.ENGLISH)
                        "Auto-locked after inactivity for your safety. Enter your PIN or password to resume."
                    else
                        "सुरक्षा के लिए स्वतः लॉक किया गया। पुनः प्रारंभ करने के लिए पासवर्ड दर्ज करें।",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                )

                Text(
                    text = userEmail,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = null
                    },
                    label = { Text("Password / PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("session_unlock_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = SosDangerRed,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (passwordInput.isNotBlank()) {
                            val success = onUnlock(passwordInput)
                            if (!success) {
                                errorMessage = "Please enter valid password or PIN"
                            }
                        } else {
                            errorMessage = "Please enter your password / PIN"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("unlock_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Unlock Session", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Log Out")
                }
            }
        }
    }
}
