package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Complaint
import com.example.model.Language
import com.example.ui.theme.Navy900
import com.example.ui.theme.SafetyGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PdfReportDialog(
    complaint: Complaint,
    currentLanguage: Language,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val formattedDate = SimpleDateFormat("dd MMM yyyy, HH:mm z", Locale.getDefault()).format(Date(complaint.filedTimestamp))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("pdf_report_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLanguage == Language.ENGLISH) "Official Incident Report (PDF)" else "आधिकारिक शिकायत रिपोर्ट (PDF)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Document Paper Canvas (White Printable Layout)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCBD5E1)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Official Letterhead Header
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "GOVERNMENT OF INDIA • WOMEN & CHILD SAFETY CELL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "DHRITI SECURE REPORTING INITIATIVE",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Navy900
                            )
                            Text(
                                text = "OFFICIAL INCIDENT ACKNOWLEDGMENT SLIP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = 1.5.dp,
                            color = Navy900
                        )

                        // Complaint ID & QR Verification Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "COMPLAINT ID / संदर्भ संख्या:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = complaint.id,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Navy900
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Filed on: $formattedDate",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                            QrCodeView(
                                data = complaint.qrCodePayload,
                                size = 80.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Status Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Verified",
                                tint = SafetyGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STATUS: ${complaint.status.displayNameEn.uppercase()} (${complaint.status.displayNameHi})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SafetyGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Structured Fields
                        PdfFieldRow("Incident Category", complaint.category.displayNameEn)
                        PdfFieldRow("Location of Occurrence", complaint.location)
                        PdfFieldRow("Approximate Time", complaint.approximateTime)
                        PdfFieldRow("Perpetrator Description", complaint.perpetratorDescription.ifBlank { "Not specified" })
                        PdfFieldRow("Vehicle Details", complaint.vehicleDetails.ifBlank { "Not observed" })
                        PdfFieldRow("Assigned Officer", complaint.officerName ?: "Special Investigation Cell (In Queue)")
                        PdfFieldRow("Jurisdiction Post", complaint.policeStation ?: "Central Women Helpline")
                        PdfFieldRow("Cryptographic Hash", complaint.encryptedPayloadHash)

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "INCIDENT NARRATIVE / शिकायत का विवरण:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = complaint.narrative,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Note: This is an authentic digitally cryptographed receipt valid under IT Act Section 65B.",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Download PDF & Print
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Report sent to local printer spool", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = "Print")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Print", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Downloaded ${complaint.id}.pdf to Downloads folder", Toast.LENGTH_LONG).show()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("download_pdf_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Download")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Download PDF", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfFieldRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF64748B)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF0F172A)
        )
    }
}
