package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Language
import com.example.ui.theme.Navy900
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.SosDangerRed

data class FaqItem(
    val id: String,
    val category: String,
    val questionEn: String,
    val questionHi: String,
    val answerEn: String,
    val answerHi: String,
    val tipBadgeEn: String? = null,
    val tipBadgeHi: String? = null
)

@Composable
fun FaqDialog(
    currentLanguage: Language,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var expandedItemIds by remember { mutableStateOf(setOf<String>("faq-1", "faq-2")) }

    val allFaqs = remember {
        listOf(
            FaqItem(
                id = "faq-1",
                category = "SOS",
                questionEn = "How does the Emergency SOS Panic Alert work?",
                questionHi = "आपातकालीन एसओएस पैनिक अलर्ट कैसे काम करता है?",
                answerEn = "Long-press the persistent floating SOS button for 5 seconds (or tap it in an emergency). Dhriti initiates a 10-second cancel window with tactile feedback. If uncancelled, it broadcasts your live GPS coordinates, battery level, and emergency link to PCR 112 and your 2 linked emergency contacts.",
                answerHi = "फ्लोटिंग एसओएस बटन को 5 सेकंड तक दबाए रखें। 10 सेकंड की उलटी गिनती शुरू होगी। रद्द न करने पर आपका लाइव जीपीएस स्थान, बैटरी स्तर और ट्रैकिंग लिंक तुरंत पीसीआर 112 और आपके आपातकालीन संपर्कों को भेज दिया जाता है।",
                tipBadgeEn = "10s Safety Buffer",
                tipBadgeHi = "10 सेकंड का सुरक्षा बफर"
            ),
            FaqItem(
                id = "faq-2",
                category = "GPS",
                questionEn = "How does real-time GPS location and police station radar work?",
                questionHi = "लाइव जीपीएस और निकटतम पुलिस स्टेशन रडार कैसे काम करता है?",
                answerEn = "Dhriti continuously fetches high-accuracy GPS telemetry. The Safe Route screen displays real-time coordinates, bearing, and ranks the nearest 24x7 police stations and women help desks by exact distance. You can tap any police station to view its SHO in charge, direct contact number, or navigate directly to it.",
                answerHi = "धृति वास्तविक समय में सटीक जीपीएस उपग्रह सिग्नल प्राप्त करती है। सेफ रूट स्क्रीन पर निकटतम पुलिस चौकियों और महिला हेल्प डेस्क की दूरी, दिशा और संपर्क नंबर प्रदर्शित होते हैं, जहाँ आप एक टैप में कॉल या नेविगेट कर सकती हैं।",
                tipBadgeEn = "Live Radar",
                tipBadgeHi = "लाइव रडार"
            ),
            FaqItem(
                id = "faq-3",
                category = "SOS",
                questionEn = "What happens if I accidentally trigger the SOS?",
                questionHi = "यदि गलती से एसओएस बटन दब जाए तो क्या होगा?",
                answerEn = "You have a 10-second cancellation countdown window. Simply tap the green 'Cancel False Alarm' button. No alert, SMS, or dispatch will be triggered if cancelled during this window.",
                answerHi = "आपके पास 10 सेकंड का समय होता है। 'गलत अलार्म रद्द करें' बटन दबाने पर कोई भी संदेश या अलर्ट पुलिस को नहीं भेजा जाएगा।",
                tipBadgeEn = "Zero False Dispatches",
                tipBadgeHi = "गलत अलर्ट से बचाव"
            ),
            FaqItem(
                id = "faq-4",
                category = "GPS",
                questionEn = "What is the Stationary Vehicle & Inactivity Watchdog?",
                questionHi = "वाहन रुकने या स्थिर रहने की सुरक्षा जांच क्या है?",
                answerEn = "During a monitored Safe Trip, if the GPS detects you have remained stationary for 90 seconds in an unexpected area, Dhriti triggers an automated safety check with a 45-second countdown. If you do not confirm you are safe, SOS is automatically dispatched.",
                answerHi = "यात्रा के दौरान यदि आपका वाहन 90 सेकंड से अधिक समय तक रुकता है, तो ऐप सुरक्षा जांच शुरू करता है। 45 सेकंड में प्रतिक्रिया न मिलने पर स्वचालित रूप से एसओएस भेज दिया जाता है।",
                tipBadgeEn = "Auto Watchdog",
                tipBadgeHi = "स्वचालित निगरानी"
            ),
            FaqItem(
                id = "faq-5",
                category = "Police",
                questionEn = "Can I directly call the nearest police station or women help desk?",
                questionHi = "क्या मैं सीधे निकटतम पुलिस स्टेशन या महिला हेल्प डेस्क को कॉल कर सकती हूँ?",
                answerEn = "Yes! On the Safe Route and Nearest Police Stations screen, every station has a direct phone dialer button for its landline as well as the national 112 emergency hotline. Tapping 'Call Station' immediately launches your phone dialer.",
                answerHi = "हाँ! निकटतम पुलिस स्टेशन सूची में प्रत्येक चौकी का लैंडलाइन नंबर और 112 हेल्पलाइन उपलब्ध है। 'कॉल करें' दबाने पर तुरंत फोन डायलर खुल जाता है।",
                tipBadgeEn = "1-Tap Dialing",
                tipBadgeHi = "एक टैप में कॉल"
            ),
            FaqItem(
                id = "faq-6",
                category = "Complaints",
                questionEn = "How does the AI Voice Complaint & FIR generator work?",
                questionHi = "एआई वॉयस शिकायत और ई-एफआईआर जनरेटर कैसे काम करता है?",
                answerEn = "You can speak naturally in English or Hindi describing an incident. Dhriti uses Gemini AI to extract key incident category, location, timing, and perpetrator descriptions into a structured FIR-ready complaint draft with legal section suggestions.",
                answerHi = "आप हिंदी या अंग्रेजी में बोलकर घटना का विवरण दे सकती हैं। धृति एआई आपकी आवाज़ से स्वतः स्थान, समय और विवरण निकालकर कानूनी प्रारूप में शिकायत तैयार कर देती है।",
                tipBadgeEn = "Gemini Powered",
                tipBadgeHi = "जेमिनी एआई समर्थित"
            ),
            FaqItem(
                id = "faq-7",
                category = "Privacy",
                questionEn = "Is my Aadhaar number stored securely?",
                questionHi = "क्या मेरा आधार नंबर पूरी तरह सुरक्षित है?",
                answerEn = "Yes. Dhriti never stores your full Aadhaar in plaintext. Only the last 4 digits are kept for masked display (XXXX-XXXX-1234), and an irreversible SHA-256 salted cryptographic hash is generated locally for session verification.",
                answerHi = "हाँ। आपका पूरा आधार कभी भी प्लेनटेक्स्ट में सेव नहीं होता। केवल अंतिम 4 अंक प्रदर्शित होते हैं और स्थानीय क्रिप्टोग्राफिक हैश द्वारा सत्यापन किया जाता है।",
                tipBadgeEn = "SHA-256 Salted",
                tipBadgeHi = "क्रिप्टोग्राफिक सुरक्षा"
            ),
            FaqItem(
                id = "faq-8",
                category = "Privacy",
                questionEn = "What is the 5-Minute Inactivity Session Lock?",
                questionHi = "5 मिनट का इनएक्टिविटी सेशन लॉक क्या है?",
                answerEn = "If you step away from the app for 5 minutes, Dhriti locks your session to prevent unauthorized viewing of sensitive complaint details or personal contacts. You can unlock with your secure password.",
                answerHi = "5 मिनट तक ऐप का उपयोग न करने पर स्क्रीन अपने आप लॉक हो जाती है ताकि कोई आपकी निजी शिकायतें या संपर्क न देख सके।",
                tipBadgeEn = "Auto Privacy Lock",
                tipBadgeHi = "ऑटो प्राइवेसी लॉक"
            ),
            FaqItem(
                id = "faq-9",
                category = "SOS",
                questionEn = "Does SOS broadcast work offline without cellular data?",
                questionHi = "क्या बिना इंटरनेट के भी एसओएस काम करता है?",
                answerEn = "Yes! If cellular data is dropped, Dhriti utilizes an offline emergency SMS telemetry protocol. It packages your last known GPS coordinates into an encoded emergency SMS sent directly to your linked contacts.",
                answerHi = "हाँ! इंटरनेट बंद होने पर भी धृति ऑफलाइन एसएमएस प्रोटोकॉल के जरिए आपके अंतिम जीपीएस निर्देशांक संपर्कों को भेज देती है।",
                tipBadgeEn = "Offline Ready",
                tipBadgeHi = "ऑफलाइन सुविधा"
            )
        )
    }

    val categories = listOf(
        "All" to if (currentLanguage == Language.ENGLISH) "All Questions" else "सभी प्रश्न",
        "SOS" to if (currentLanguage == Language.ENGLISH) "🚨 SOS Alert" else "🚨 एसओएस अलर्ट",
        "GPS" to if (currentLanguage == Language.ENGLISH) "🗺️ Live Map & GPS" else "🗺️ लाइव जीपीएस",
        "Police" to if (currentLanguage == Language.ENGLISH) "👮 Police Stations" else "👮 पुलिस सहायता",
        "Complaints" to if (currentLanguage == Language.ENGLISH) "📝 Complaints & FIR" else "📝 शिकायतें",
        "Privacy" to if (currentLanguage == Language.ENGLISH) "🔒 Privacy & Aadhaar" else "🔒 गोपनीयता"
    )

    val filteredFaqs = allFaqs.filter { item ->
        val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
        val matchesQuery = searchQuery.isBlank() ||
                item.questionEn.contains(searchQuery, ignoreCase = true) ||
                item.questionHi.contains(searchQuery, ignoreCase = true) ||
                item.answerEn.contains(searchQuery, ignoreCase = true) ||
                item.answerHi.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("faq_dialog_container"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with Dhriti theme
                Surface(
                    color = Navy900,
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E3E62)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Help,
                                    contentDescription = "FAQ",
                                    tint = SaffronOrange,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (currentLanguage == Language.ENGLISH) "Safety FAQ & Help Guide" else "सुरक्षा अक्सर पूछे जाने वाले सवाल",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (currentLanguage == Language.ENGLISH) "Dhriti Women's Safety Assistant" else "धृति महिला सुरक्षा सहायक",
                                    fontSize = 12.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("faq_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // National Helplines Quick Banner
                Surface(
                    color = Color(0xFFFFF7ED),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Emergency Numbers:" else "आपातकालीन नंबर:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9A3412)
                        )

                        HelplineBadge(
                            number = "112",
                            label = "Police PCR",
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                                context.startActivity(intent)
                            }
                        )

                        HelplineBadge(
                            number = "1091",
                            label = "Women Helpline",
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1091"))
                                context.startActivity(intent)
                            }
                        )

                        HelplineBadge(
                            number = "181",
                            label = "Distress Desk",
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:181"))
                                context.startActivity(intent)
                            }
                        )

                        HelplineBadge(
                            number = "1930",
                            label = "Cyber Cell",
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Search topics (e.g. SOS, GPS, police, privacy)..." else "खोजें (एसओएस, जीपीएस, पुलिस, प्राइवेसी)...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Navy900)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("faq_search_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedCategory == key,
                            onClick = { selectedCategory = key },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // FAQ List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    if (filteredFaqs.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "No results",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (currentLanguage == Language.ENGLISH) "No matching questions found" else "कोई प्रश्न नहीं मिला",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (currentLanguage == Language.ENGLISH) "Try searching for 'SOS', 'police', or 'route'." else "'एसओएस', 'पुलिस' या 'मार्ग' खोजें।",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredFaqs) { faq ->
                            val isExpanded = expandedItemIds.contains(faq.id)
                            val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "rotate")

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedItemIds = if (isExpanded) {
                                            expandedItemIds - faq.id
                                        } else {
                                            expandedItemIds + faq.id
                                        }
                                    }
                                    .testTag("faq_card_${faq.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isExpanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isExpanded) 1.5.dp else 1.dp,
                                    color = if (isExpanded) Navy900 else Color(0xFFE2E8F0)
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (currentLanguage == Language.ENGLISH) faq.questionEn else faq.questionHi,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Icon(
                                            imageVector = Icons.Default.ExpandMore,
                                            contentDescription = "Expand",
                                            tint = Navy900,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .rotate(rotation)
                                        )
                                    }

                                    val tipBadge = if (currentLanguage == Language.ENGLISH) faq.tipBadgeEn else faq.tipBadgeHi
                                    if (tipBadge != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFEFF6FF)
                                        ) {
                                            Text(
                                                text = tipBadge,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E40AF),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    AnimatedVisibility(visible = isExpanded) {
                                        Column {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = if (currentLanguage == Language.ENGLISH) faq.answerEn else faq.answerHi,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }

                // Bottom Close button
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (currentLanguage == Language.ENGLISH) "Got it • Close FAQ" else "समझ गए • बंद करें",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HelplineBadge(
    number: String,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFED7AA))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call $number",
                tint = SosDangerRed,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(text = number, fontSize = 11.sp, fontWeight = FontWeight.Black, color = SosDangerRed)
                Text(text = label, fontSize = 9.sp, color = Color(0xFF78350F))
            }
        }
    }
}
