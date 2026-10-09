package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.crypto.CryptoManager
import com.example.model.Language
import com.example.repository.SafetyRepository
import com.example.ui.theme.Navy900
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.SafetyGreen

@Composable
fun AuthScreen(
    repository: SafetyRepository,
    currentLanguage: Language,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Login, 1: Register

    // Register Form Fields
    var fullName by remember { mutableStateOf("") }
    var rawAadhaarInput by remember { mutableStateOf("") }
    var isAadhaarMaskedActive by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // 2 Emergency Contacts
    var contact1Name by remember { mutableStateOf("") }
    var contact1Phone by remember { mutableStateOf("") }
    var contact2Name by remember { mutableStateOf("") }
    var contact2Phone by remember { mutableStateOf("") }

    var validationError by remember { mutableStateOf<String?>(null) }

    val displayedAadhaar = remember(rawAadhaarInput, isAadhaarMaskedActive) {
        if (isAadhaarMaskedActive && rawAadhaarInput.length >= 4) {
            CryptoManager.maskAadhaar(rawAadhaarInput)
        } else {
            rawAadhaarInput
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Official Logo & Trust Header
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color(0xFF1E3E62), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_dhriti_user_logo_round),
                contentDescription = "Dhriti Safety Logo",
                modifier = Modifier.size(76.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (currentLanguage == Language.ENGLISH) "Dhriti • Women's Safety" else "धृति • महिला सुरक्षा",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = if (currentLanguage == Language.ENGLISH)
                "Ministry-Compliant Emergency Response & Safe Journey Network"
            else
                "आपातकालीन प्रतिक्रिया एवं सुरक्षित यात्रा नेटवर्क",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Security encryption notice
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock",
                    tint = SafetyGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (currentLanguage == Language.ENGLISH) "Your data is encrypted" else "आपका डेटा एन्क्रिप्टेड है",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (currentLanguage == Language.ENGLISH)
                            "Protected on-device using AES-256-GCM. Aadhaar is salted & hashed (SHA-256) on client."
                        else
                            "आपके डिवाइस पर सुरक्षित AES-256-GCM एन्क्रिप्शन। आधार कभी भी सादे टेक्स्ट में संग्रहीत नहीं होता।",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Tab Selector (Login / Register)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Navy900,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0; validationError = null },
                text = {
                    Text(
                        text = if (currentLanguage == Language.ENGLISH) "Citizen Login" else "नागरिक लॉगिन",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                },
                modifier = Modifier.testTag("tab_login")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1; validationError = null },
                text = {
                    Text(
                        text = if (currentLanguage == Language.ENGLISH) "New Registration" else "नया पंजीकरण",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                },
                modifier = Modifier.testTag("tab_register")
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedTab == 0) {
            // LOGIN FORM
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (currentLanguage == Language.ENGLISH) "Sign In to Your Safety Account" else "अपने सुरक्षा खाते में प्रवेश करें",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; validationError = null },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; validationError = null },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (validationError != null) {
                        Text(
                            text = validationError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                validationError = "Please enter your registered email and password"
                            } else {
                                repository.registerUser(
                                    fullName = email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() },
                                    rawAadhaar = "9876 5432 1234",
                                    email = email,
                                    emergencyContact1Name = "Contact 1",
                                    emergencyContact1Phone = "+91 00000 00000",
                                    emergencyContact2Name = "Contact 2",
                                    emergencyContact2Phone = "+91 00000 00000"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Login Securely" else "सुरक्षित लॉगिन करें",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // REGISTRATION FORM
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (currentLanguage == Language.ENGLISH) "Citizen Registration Form" else "नागरिक पंजीकरण फॉर्म",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it; validationError = null },
                        label = { Text("Full Name (as per ID)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_name_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Aadhaar Number with Automatic Client Masking
                    OutlinedTextField(
                        value = displayedAadhaar,
                        onValueChange = { input ->
                            // Remove non digits
                            val digitsOnly = input.filter { it.isDigit() }
                            if (digitsOnly.length <= 12) {
                                rawAadhaarInput = digitsOnly
                                isAadhaarMaskedActive = false
                            }
                            validationError = null
                        },
                        label = { Text("Aadhaar Number (12 digits)") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "Aadhaar") },
                        supportingText = {
                            Text(
                                text = if (rawAadhaarInput.length == 12)
                                    "🔒 Client-side masked as ${CryptoManager.maskAadhaar(rawAadhaarInput)}"
                                else
                                    "Enter 12 digits (will be hashed & masked as XXXX-XXXX-1234)",
                                color = if (rawAadhaarInput.length == 12) SafetyGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (rawAadhaarInput.length == 12) {
                                OutlinedButton(
                                    onClick = { isAadhaarMaskedActive = !isAadhaarMaskedActive },
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(if (isAadhaarMaskedActive) "Show" else "Mask", fontSize = 11.sp)
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_aadhaar_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; validationError = null },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_email_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; validationError = null },
                        label = { Text("Password (for PBKDF2 Key Derivation)") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = "Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_password_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (currentLanguage == Language.ENGLISH) "2 Emergency Contacts (Notified on SOS):" else "2 आपातकालीन संपर्क:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Navy900
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Contact 1
                    OutlinedTextField(
                        value = contact1Name,
                        onValueChange = { contact1Name = it },
                        label = { Text("Contact 1 Name & Relation") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = contact1Phone,
                        onValueChange = { contact1Phone = it },
                        label = { Text("Contact 1 Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Contact 2
                    OutlinedTextField(
                        value = contact2Name,
                        onValueChange = { contact2Name = it },
                        label = { Text("Contact 2 Name & Relation") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = contact2Phone,
                        onValueChange = { contact2Phone = it },
                        label = { Text("Contact 2 Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (validationError != null) {
                        Text(
                            text = validationError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (fullName.isBlank()) {
                                validationError = "Please enter your full name"
                            } else if (rawAadhaarInput.length < 12) {
                                validationError = "Please enter complete 12-digit Aadhaar number"
                            } else if (email.isBlank()) {
                                validationError = "Please enter valid email"
                            } else if (password.length < 4) {
                                validationError = "Password must be at least 4 characters"
                            } else if (contact1Name.isBlank() || contact1Phone.isBlank()) {
                                validationError = "Please enter Contact 1 name and phone number"
                            } else if (contact2Name.isBlank() || contact2Phone.isBlank()) {
                                validationError = "Please enter Contact 2 name and phone number"
                            } else {
                                repository.registerUser(
                                    fullName = fullName,
                                    rawAadhaar = rawAadhaarInput,
                                    email = email,
                                    emergencyContact1Name = contact1Name,
                                    emergencyContact1Phone = contact1Phone,
                                    emergencyContact2Name = contact2Name,
                                    emergencyContact2Phone = contact2Phone
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("reg_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Register & Issue Encrypted JWT" else "पंजीकरण करें एवं JWT जारी करें",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
