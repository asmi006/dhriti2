package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.ai.StructuredIncidentResult
import com.example.model.Language
import com.example.repository.SafetyRepository
import com.example.speech.SpeechRecognitionHelper
import com.example.ui.theme.Navy900
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.SafetyGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun VoiceRecorderDialog(
    repository: SafetyRepository,
    currentLanguage: Language,
    onDismiss: () -> Unit,
    onTranscriptReady: (String, StructuredIncidentResult?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val speechHelper = remember { SpeechRecognitionHelper(context) }

    val isListening by speechHelper.isListening.collectAsState()
    val liveTranscript by speechHelper.liveTranscript.collectAsState()
    val rmsLevel by speechHelper.rmsLevel.collectAsState()
    val errorMessage by speechHelper.errorMessage.collectAsState()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            val locale = if (currentLanguage == Language.HINDI) Locale("hi", "IN") else Locale.ENGLISH
            speechHelper.startListening(locale)
        }
    }

    var isAnalyzingWithAi by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        if (hasAudioPermission) {
            val locale = if (currentLanguage == Language.HINDI) Locale("hi", "IN") else Locale.ENGLISH
            speechHelper.startListening(locale)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        onDispose {
            speechHelper.stopListening()
        }
    }

    LaunchedEffect(isListening) {
        if (isListening) {
            while (isListening) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    // Dynamic wave heights derived from real microphone RMS level
    val baseWave = animateFloatAsState(targetValue = (rmsLevel * 60f).coerceAtLeast(8f), label = "rmsWave").value

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("voice_recorder_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Mic",
                            tint = if (isListening) SaffronOrange else Navy900
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Live Voice Dictation" else "लाइव आवाज़ रिकॉर्डर",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Real Microphone Amplitude Waveform
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (isListening) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(0.4f, 0.7f, 1.0f, 0.6f, 1.2f, 0.8f, 0.5f).forEach { multiplier ->
                                val height = (baseWave * multiplier).coerceIn(8f, 68f)
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .height(height.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(SaffronOrange)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (hasAudioPermission) "Tap Microphone to Speak" else "Grant Microphone Permission",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Timer & Live Mic Toggle Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = String.format("00:%02d", recordingSeconds),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isListening) SaffronOrange else MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = {
                            if (!hasAudioPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                if (isListening) {
                                    speechHelper.stopListening()
                                } else {
                                    val locale = if (currentLanguage == Language.HINDI) Locale("hi", "IN") else Locale.ENGLISH
                                    speechHelper.startListening(locale)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (isListening) Color(0xFFD32F2F) else Navy900)
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Toggle Recording",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Real-Time Transcript Display
                Text(
                    text = if (currentLanguage == Language.ENGLISH) "Live Recognized Transcript:" else "पहचाना गया विवरण:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = liveTranscript.ifBlank {
                            if (isListening) "Listening to microphone... speak now" else "No speech detected yet. Speak clearly into your mic."
                        },
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = if (liveTranscript.isNotBlank()) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ACTION: Analyze with AI & Auto-Structure
                Button(
                    onClick = {
                        val textToProcess = liveTranscript.trim()
                        if (textToProcess.isNotBlank()) {
                            scope.launch {
                                isAnalyzingWithAi = true
                                speechHelper.stopListening()
                                val aiResult = repository.analyzeIncidentWithAi(textToProcess)
                                isAnalyzingWithAi = false
                                onTranscriptReady(textToProcess, aiResult)
                                onDismiss()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("ai_structure_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isAnalyzingWithAi && liveTranscript.isNotBlank()
                ) {
                    if (isAnalyzingWithAi) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Processing with AI Model...", fontSize = 14.sp)
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", tint = SaffronOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "AI Auto-Structure & Apply" else "AI मॉडल से संरचित करें",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
