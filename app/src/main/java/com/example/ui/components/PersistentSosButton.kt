package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.ui.theme.SosDangerRed
import com.example.ui.theme.SosDangerRedDark
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PersistentSosButton(
    onTriggerSos: () -> Unit,
    currentLanguage: Language,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isPressing by remember { mutableStateOf(false) }
    var showTapHint by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }
    var pressJob by remember { mutableStateOf<Job?>(null) }

    fun vibrateShort() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(100)
        }
    }

    fun vibrateHeavy() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 100, 300), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(500)
        }
    }

    LaunchedEffect(showTapHint) {
        if (showTapHint) {
            delay(3000)
            showTapHint = false
        }
    }

    Box(
        modifier = modifier
            .testTag("persistent_sos_container"),
        contentAlignment = Alignment.BottomStart
    ) {
        // Floating single-tap hint tooltip
        if (showTapHint) {
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .offset(x = 0.dp, y = (-74).dp)
                    .padding(start = 4.dp)
            ) {
                Text(
                    text = if (currentLanguage == Language.ENGLISH)
                        "⚠️ Press & Hold for 5 sec to trigger SOS"
                    else
                        "⚠️ आपातकाल के लिए 5 सेकंड दबाए रखें",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Circular SOS container with Progress Ring
        Box(
            modifier = Modifier
                .size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background Canvas for Circular Progress Ring
            Canvas(modifier = Modifier.size(72.dp)) {
                // Background Track
                drawArc(
                    color = Color(0x33D32F2F),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
                // Active Filling Arc
                if (progress.value > 0f) {
                    drawArc(
                        color = Color(0xFFFFD54F),
                        startAngle = -90f,
                        sweepAngle = 360f * progress.value,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // The main touch button
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(if (isPressing) SosDangerRedDark else SosDangerRed)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                // Single tap does nothing except show the guidance message
                                vibrateShort()
                                showTapHint = true
                            },
                            onPress = {
                                isPressing = true
                                showTapHint = false
                                vibrateShort()

                                pressJob = coroutineScope.launch {
                                    // Animate from 0 to 1 over 5000ms (5 seconds)
                                    progress.snapTo(0f)
                                    progress.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = 5000,
                                            easing = LinearEasing
                                        )
                                    )
                                    // If animation completed, trigger SOS
                                    if (progress.value >= 1f) {
                                        vibrateHeavy()
                                        onTriggerSos()
                                    }
                                }

                                tryAwaitRelease()

                                // Released before or after completion
                                isPressing = false
                                pressJob?.cancel()
                                coroutineScope.launch {
                                    progress.snapTo(0f)
                                }
                            }
                        )
                    }
                    .testTag("sos_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SOS",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isPressing) "${(progress.value * 5).toInt()}s" else "HOLD 5s",
                        color = Color(0xFFFFEBEE),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
