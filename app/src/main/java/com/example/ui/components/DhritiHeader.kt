package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Language
import com.example.ui.theme.Navy900
import com.example.ui.theme.SaffronOrange

@Composable
fun DhritiHeader(
    isDarkMode: Boolean,
    currentLanguage: Language,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenFaq: () -> Unit,
    onLockSession: () -> Unit,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isDarkMode) Color(0xFF0B192C) else Navy900,
        contentColor = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_dhriti_user_logo_round),
                            contentDescription = "Dhriti Logo",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (currentLanguage == Language.ENGLISH) "Dhriti" else "धृति",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Government Emblem / Tri-color Accent Bar
                            Box(
                                modifier = Modifier
                                    .size(width = 18.dp, height = 4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(SaffronOrange)
                            )
                        }
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Women's Safety Platform" else "महिला सुरक्षा मंच",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Action controls: Language, Theme, FAQ, Privacy, Lock
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Language Switcher (EN / HI)
                    Surface(
                        onClick = onToggleLanguage,
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E3E62),
                        modifier = Modifier
                            .testTag("language_toggle_button")
                            .padding(end = 4.dp)
                    ) {
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "HI | हिंदी" else "EN | Eng",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }

                    // FAQ Help Button
                    IconButton(
                        onClick = onOpenFaq,
                        modifier = Modifier.testTag("faq_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Help,
                            contentDescription = "Women Safety FAQ & Help",
                            tint = SaffronOrange
                        )
                    }

                    // Dark / Light toggle
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Light/Dark Theme",
                            tint = if (isDarkMode) SaffronOrange else Color.White
                        )
                    }

                    // Privacy & Security Info
                    IconButton(
                        onClick = onOpenPrivacy,
                        modifier = Modifier.testTag("privacy_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = "Security and Privacy Information",
                            tint = Color.White
                        )
                    }

                    // Lock Session (if logged in)
                    if (isLoggedIn) {
                        IconButton(
                            onClick = onLockSession,
                            modifier = Modifier.testTag("session_lock_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Current Session",
                                tint = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }

            // Trust & Encryption Banner strip
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security Lock",
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (currentLanguage == Language.ENGLISH)
                            "E2E Encrypted (AES-256-GCM) • Zero Plaintext Storage"
                        else
                            "ई2ई एन्क्रिप्टेड (AES-256) • सुरक्षित सरकारी प्रोटोकॉल",
                        fontSize = 11.sp,
                        color = Color(0xFFA5D6A7),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
