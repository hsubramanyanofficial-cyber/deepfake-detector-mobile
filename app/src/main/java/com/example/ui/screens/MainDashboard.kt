package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AlertEntity
import com.example.data.database.ScanEntity
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

// --- Custom Theme Color Consts ---
val DeepObsidian = Color(0xFF1C1B1F)     // Master background (slate charcoal) - form high density body bg-[#1C1B1F]
val CardBackground = Color(0xFF2B2930)   // Primary container (Material 3 Surface Container) - bg-[#2B2930]
val ActiveSurface = Color(0xFF313033)    // Row background (Material 3 Surface) - bg-[#313033]
val ElevationSurface = Color(0xFF49454F) // Dense/Highlight container - bg-[#49454F]
val TechTeal = Color(0xFFD0BCFF)         // Master Lavender highlight - text-[#D0BCFF]
val CyberGold = Color(0xFFEADDFF)        // Active pill container background - bg-[#EADDFF]
val DarkPurpleText = Color(0xFF21005D)   // On-Lavender dark text - text-[#21005D]
val CyberOrange = Color(0xFFFFB300)      // High defense warning indicator color
val AlertRed = Color(0xFFF2B8B5)         // Cyber defense critical warning color (M3 standard light red)
val MutedSlate = Color(0xFF938F99)       // Secondary light gray label - text-[#938F99]
val LightGray = Color(0xFFCAC4D0)        // Description gray body text - text-[#CAC4D0]
val GridLineColor = Color(0x13FFFFFF)     // 5% semi-transparent white border line


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentTab = remember { mutableStateOf(0) } // 0 = Scanner, 1 = Alerts, 2 = Intel Resource
    
    // API Key config
    val savedApiKey = remember { mutableStateOf("MY_GEMINI_API_KEY") }
    val showApiKeyField = remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = CardBackground,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(1.dp, GridLineColor, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                NavigationBarItem(
                    selected = currentTab.value == 0,
                    onClick = { currentTab.value = 0 },
                    modifier = Modifier.testTag("nav_tab_scanner"),
                    icon = { Icon(if (currentTab.value == 0) Icons.Filled.Shield else Icons.Outlined.Shield, contentDescription = "Scanner") },
                    label = { Text("Scanner", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkPurpleText,
                        selectedTextColor = TechTeal,
                        indicatorColor = CyberGold,
                        unselectedIconColor = MutedSlate,
                        unselectedTextColor = MutedSlate
                    )
                )
                NavigationBarItem(
                    selected = currentTab.value == 1,
                    onClick = { currentTab.value = 1 },
                    modifier = Modifier.testTag("nav_tab_alerts"),
                    icon = { 
                        // Show a glowing dot if there are unread alerts
                        val alerts by viewModel.allAlerts.collectAsState()
                        val hasUnread = alerts.any { !it.isRead }
                        Box {
                            Icon(if (currentTab.value == 1) Icons.Filled.Notifications else Icons.Outlined.Notifications, contentDescription = "Alerts Feed")
                            if (hasUnread) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(AlertRed, CircleShape)
                                        .align(Alignment.TopEnd)
                                        .border(1.5.dp, CardBackground, CircleShape)
                                )
                            }
                        }
                    },
                    label = { Text("Alerts", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkPurpleText,
                        selectedTextColor = TechTeal,
                        indicatorColor = CyberGold,
                        unselectedIconColor = MutedSlate,
                        unselectedTextColor = MutedSlate
                    )
                )
                NavigationBarItem(
                    selected = currentTab.value == 2,
                    onClick = { currentTab.value = 2 },
                    modifier = Modifier.testTag("nav_tab_intel"),
                    icon = { Icon(if (currentTab.value == 2) Icons.Filled.MenuBook else Icons.Outlined.MenuBook, contentDescription = "Security Intel") },
                    label = { Text("Offline Intel", fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkPurpleText,
                        selectedTextColor = TechTeal,
                        indicatorColor = CyberGold,
                        unselectedIconColor = MutedSlate,
                        unselectedTextColor = MutedSlate
                    )
                )
            }
        },
        containerColor = DeepObsidian
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .drawBehind {
                    // Futuristic technical grid lines background
                    val gridSize = 40.dp.toPx()
                    val paintColor = GridLineColor.copy(alpha = 0.5f)
                    for (x in 0..size.width.toInt() step gridSize.toInt()) {
                        drawLine(
                            color = paintColor,
                            start = Offset(x.toFloat(), 0f),
                            end = Offset(x.toFloat(), size.height),
                            strokeWidth = 1f
                        )
                    }
                    for (y in 0..size.height.toInt() step gridSize.toInt()) {
                        drawLine(
                            color = paintColor,
                            start = Offset(0f, y.toFloat()),
                            end = Offset(size.width, y.toFloat()),
                            strokeWidth = 1f
                        )
                    }
                }
        ) {
            // Header bar
            HeaderView(
                apiKey = savedApiKey.value,
                onApiKeyChanged = { savedApiKey.value = it },
                showKeyField = showApiKeyField.value,
                toggleKeyField = { showApiKeyField.value = !showApiKeyField.value }
            )

            HorizontalDivider(color = GridLineColor, thickness = 1.dp)

            // Dynamic Tab selection
            Box(modifier = Modifier.weight(1f)) {
                when (currentTab.value) {
                    0 -> ScannerScreen(viewModel, savedApiKey.value)
                    1 -> AlertsScreen(viewModel)
                    2 -> OfflineIntelScreen()
                }
            }
        }
    }
}

// --- Header View ---
@Composable
fun HeaderView(
    apiKey: String,
    onApiKeyChanged: (String) -> Unit,
    showKeyField: Boolean,
    toggleKeyField: () -> Unit
) {
    var keyVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepObsidian.copy(alpha = 0.85f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // High Density Branding Shield Logo (bg-[#D0BCFF] text-[#21005D])
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(TechTeal, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "Shield Logo",
                        tint = DarkPurpleText,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "Sentinel AI",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.25.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Flashing Green active pulse dot
                        val infiniteTransition = rememberInfiniteTransition("HeaderPulse")
                        val alphaPulse by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "HeaderGlow"
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF4ADE80).copy(alpha = alphaPulse), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "REAL-TIME ACTIVE",
                            color = Color(0xFF4ADE80),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // High Density layout top app bar action controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings button (formerly key settings toggle)
                IconButton(
                    onClick = toggleKeyField,
                    modifier = Modifier
                        .background(CardBackground, RoundedCornerShape(8.dp))
                        .border(1.dp, GridLineColor, RoundedCornerShape(8.dp))
                        .testTag("api_key_settings_button")
                ) {
                    Icon(
                        imageVector = if (showKeyField) Icons.Filled.KeyOff else Icons.Filled.Key,
                        contentDescription = "API Settings",
                        tint = if (apiKey != "MY_GEMINI_API_KEY" && apiKey.isNotBlank()) TechTeal else MutedSlate
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showKeyField,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .border(1.dp, GridLineColor, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "GEMINI COGNITIVE SECURE KEY",
                        color = TechTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = onApiKeyChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input_field"),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        placeholder = { Text("Enter Gemini API Key...", color = MutedSlate) },
                        trailingIcon = {
                            IconButton(onClick = { keyVisible = !keyVisible }) {
                                Icon(
                                    imageVector = if (keyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Key Visibility",
                                    tint = MutedSlate
                                )
                            }
                        },
                        visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TechTeal,
                            unfocusedBorderColor = GridLineColor,
                            cursorColor = TechTeal
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "If left empty or set to default placeholder, Sentinel will dynamically activate local cybersecurity heuristic analyzer models. Direct Gemini scanning works offline with cached checks.",
                        color = MutedSlate,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}


// ==========================================
// SCREEN 0: SCANNER SCREEN (REAL-TIME CORE)
// ==========================================
@Composable
fun ScannerScreen(viewModel: MainViewModel, apiKey: String) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val mediaTypes = listOf(
        Pair("IMAGE", Icons.Filled.Image),
        Pair("AUDIO", Icons.Filled.Mic),
        Pair("VIDEO", Icons.Filled.Videocam),
        Pair("URL", Icons.Filled.Link)
    )

    var selectedType by remember { mutableStateOf("IMAGE") }
    var inputFieldVal by remember { mutableStateOf("") }
    var contextFieldVal by remember { mutableStateOf("") }
    val showDetailsSection = remember { mutableStateOf<ScanEntity?>(null) }

    val recentScans by viewModel.allScans.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "REAL-TIME AUTHENTICATION PORTAL",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        // Type Selector Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .border(1.dp, GridLineColor, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                mediaTypes.forEach { (type, icon) ->
                    val isSelected = selectedType == type
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) TechTeal else Color.Transparent)
                            .clickable { selectedType = type }
                            .padding(vertical = 10.dp)
                            .testTag("media_type_select_$type"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = type,
                            tint = if (isSelected) DeepObsidian else MutedSlate,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = type,
                            color = if (isSelected) DeepObsidian else MutedSlate,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // High Density Metrics Grid (bg-[#2B2930] with LED activity bars and offline report indicators)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Security Feed / Frame Analyzer status Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, GridLineColor, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "SECURITY FEED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedSlate,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "30",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "FPS",
                                fontSize = 9.sp,
                                color = Color(0xFF4ADE80),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Mini led activity spectrum row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF4ADE80).copy(alpha = 0.7f), RoundedCornerShape(1.dp)))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF4ADE80).copy(alpha = 0.7f), RoundedCornerShape(1.dp)))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF4ADE80).copy(alpha = 0.7f), RoundedCornerShape(1.dp)))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFFB300).copy(alpha = 0.4f), RoundedCornerShape(1.dp)))
                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(1.dp)))
                        }
                    }
                }

                // Cache Mode Status Card showing offline database size
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, GridLineColor, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "DATABASE STATUS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedSlate,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "READY",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = TechTeal,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = "Active Connection",
                                tint = TechTeal,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(bottom = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${recentScans.size} offline reports",
                            color = MutedSlate,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Action input options card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GridLineColor, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SCAN SPECIFICATIONS",
                        color = TechTeal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputFieldVal,
                        onValueChange = { inputFieldVal = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_input_field"),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        label = {
                            Text(
                                text = when (selectedType) {
                                    "IMAGE" -> "Image URL or File description"
                                    "AUDIO" -> "Voice sample filename / audio URL"
                                    "VIDEO" -> "Video platform link or file identifier"
                                    else -> "Direct Web URL to scan"
                                },
                                color = MutedSlate
                            )
                        },
                        placeholder = { 
                            Text(
                                text = when (selectedType) {
                                    "IMAGE" -> "e.g., source_avatar_john.png"
                                    "AUDIO" -> "e.g., voice_family_transfer_claim.wav"
                                    "VIDEO" -> "e.g., https://tiktok.com/@user/vid123"
                                    else -> "e.g., https://deepfake-investigation.net/post"
                                },
                                color = MutedSlate.copy(alpha = 0.5f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TechTeal,
                            unfocusedBorderColor = GridLineColor,
                            cursorColor = TechTeal
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = contextFieldVal,
                        onValueChange = { contextFieldVal = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_context_field"),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        label = { Text("Crucial Narrative Context (Optional)", color = MutedSlate) },
                        placeholder = { Text("e.g. This voice was claiming from a hostage situation. Or, Celebrity offering free coins.", color = MutedSlate.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TechTeal,
                            unfocusedBorderColor = GridLineColor,
                            cursorColor = TechTeal
                        ),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            if (inputFieldVal.isNotBlank()) {
                                viewModel.analyzeMedia(
                                    context = context,
                                    type = selectedType,
                                    input = inputFieldVal,
                                    additionalInfo = contextFieldVal,
                                    apiKey = apiKey
                                )
                            }
                        },
                        enabled = !viewModel.isScanning && inputFieldVal.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("scan_submit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TechTeal,
                            disabledContainerColor = TechTeal.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (viewModel.isScanning) {
                            CircularProgressIndicator(
                                color = DeepObsidian,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "COMPUTING NEURAL TENSORS...",
                                color = DeepObsidian,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        } else {
                            Icon(Icons.Filled.Security, contentDescription = null, tint = DeepObsidian)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "INITIATE SECURE SCAN",
                                color = DeepObsidian,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Scanning visual effects panel
        if (viewModel.isScanning) {
            item {
                FuturisticRadarCard()
            }
        }

        // Scan state details (Last finished scan or clicked scan)
        val reportToShow = showDetailsSection.value ?: viewModel.lastScannedResult
        if (reportToShow != null && !viewModel.isScanning) {
            item {
                AuthenticationReportCard(
                    scan = reportToShow,
                    onClose = {
                        if (showDetailsSection.value != null) {
                            showDetailsSection.value = null
                        } else {
                            viewModel.clearAllScans() // Clears VM screen state
                        }
                    }
                )
            }
        }

        // Error message if any
        if (viewModel.scanError != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AlertRed, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Warning, contentDescription = "Error", tint = AlertRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = viewModel.scanError ?: "An unexpected anomaly occurred.",
                            color = AlertRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Offline cached scan local history
        if (recentScans.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LOCAL OFFLINE CACHE (${recentScans.size} SCAN RECORDS)",
                        color = MutedSlate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "CLEAR ALL",
                        color = AlertRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clickable { viewModel.clearAllScans() }
                            .testTag("clear_scans_button")
                    )
                }
            }

            items(recentScans, key = { it.id }) { scan ->
                LocalCachedScanRow(
                    scan = scan,
                    isSelected = reportToShow?.id == scan.id,
                    onClick = { showDetailsSection.value = scan },
                    onDelete = { viewModel.deleteScan(scan) }
                )
            }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = "Zero History",
                        tint = MutedSlate.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "OFFLINE DATABASE EMPTY",
                        color = MutedSlate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Initiate an authentication scan above or look at deepfake security alerts.",
                        color = MutedSlate.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Draw a beautiful rotating neural matrix radar screen representing dynamic protection
@Composable
fun FuturisticRadarCard() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TechTeal.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer cyber grid canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2

                    // Draw rings
                    drawCircle(color = TechTeal.copy(alpha = 0.1f), radius = radius, style = Stroke(2f))
                    drawCircle(color = TechTeal.copy(alpha = 0.15f), radius = radius * 0.7f, style = Stroke(1.5f))
                    drawCircle(color = TechTeal.copy(alpha = 0.2f), radius = radius * 0.4f, style = Stroke(1f))

                    // Draw grid axes
                    drawLine(
                        color = TechTeal.copy(alpha = 0.15f),
                        start = Offset(center.x - radius, center.y),
                        end = Offset(center.x + radius, center.y),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = TechTeal.copy(alpha = 0.15f),
                        start = Offset(center.x, center.y - radius),
                        end = Offset(center.x, center.y + radius),
                        strokeWidth = 1f
                    )

                    // Draw rotating trace sweeper
                    val angleRad = Math.toRadians(rotationAngle.toDouble())
                    val endX = (center.x + radius * cos(angleRad)).toFloat()
                    val endY = (center.y + radius * sin(angleRad)).toFloat()
                    drawLine(
                        color = TechTeal,
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                    
                    // Draw small mock tracking pulses/dots
                    val pulsePositions = listOf(
                        Offset(center.x - radius * 0.5f, center.y - radius * 0.3f),
                        Offset(center.x + radius * 0.6f, center.y + radius * 0.2f),
                        Offset(center.x - radius * 0.2f, center.y + radius * 0.5f)
                    )
                    pulsePositions.forEachIndexed { i, pos ->
                        val alpha = if (i == 0) 0.8f else 0.4f
                        val color = if (i == 0) AlertRed else TechTeal
                        drawCircle(
                            color = color.copy(alpha = alpha),
                            radius = 6f,
                            center = pos
                        )
                    }
                }
                
                // Floating shield lock icon in center with breathing glow
                Icon(
                    imageVector = Icons.Filled.LocalActivity,
                    contentDescription = null,
                    tint = TechTeal,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SEARCHING VOICE PHONEME JITTER & FACIAL EDGES",
                color = TechTeal,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
            Text(
                text = "Interrogating raw frames with temporal validation tensors",
                color = MutedSlate,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// Gorgeous detailed assessment findings card with visual meter
@Composable
fun AuthenticationReportCard(scan: ScanEntity, onClose: () -> Unit) {
    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(scan.timestamp))
    val scoreColor = when {
        scan.isManipulated && scan.confidenceScore > 75 -> AlertRed
        scan.isManipulated -> CyberOrange
        scan.confidenceScore > 40 -> CyberGold
        else -> TechTeal
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, scoreColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .testTag("auth_report_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (scan.type) {
                            "IMAGE" -> Icons.Filled.Image
                            "AUDIO" -> Icons.Filled.Mic
                            "VIDEO" -> Icons.Filled.Videocam
                            else -> Icons.Filled.Link
                        },
                        contentDescription = scan.type,
                        tint = scoreColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = scan.title.uppercase(),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close Report", tint = MutedSlate)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gauge/Meter Visual Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = GridLineColor,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(12f, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = scoreColor,
                            startAngle = 135f,
                            sweepAngle = (scan.confidenceScore.toFloat() / 100f) * 270f,
                            useCenter = false,
                            style = Stroke(12f, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${scan.confidenceScore}%",
                            color = scoreColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "RISK",
                            color = MutedSlate,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "INTELLIGENCE VERDICT",
                        fontSize = 10.sp,
                        color = MutedSlate,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (scan.isManipulated) "🔴 SYNTHETIC/DEEPFAKE ALERT" else "🟢 VERIFIED GENUINE",
                        color = if (scan.isManipulated) AlertRed else TechTeal,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Source: ${scan.input}",
                        color = MutedSlate,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = GridLineColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "NEURAL ANALYSIS FEEDBACK:",
                fontSize = 10.sp,
                color = TechTeal,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Text output contains detailed bullet parameters or paragraphs
            Text(
                text = scan.analysisResult,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Justify,
                fontFamily = FontFamily.Default
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Report ID: #${scan.id}",
                    color = MutedSlate.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Checked: $dateStr",
                    color = MutedSlate.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun LocalCachedScanRow(
    scan: ScanEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(scan.timestamp))
    val statusColor = if (scan.isManipulated) AlertRed else TechTeal

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) CardBackground else CardBackground.copy(alpha = 0.4f))
            .border(
                width = 1.dp,
                color = if (isSelected) statusColor.copy(alpha = 0.7f) else GridLineColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("cached_scan_row_${scan.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (scan.type) {
                    "IMAGE" -> Icons.Filled.Image
                    "AUDIO" -> Icons.Filled.Mic
                    "VIDEO" -> Icons.Filled.Videocam
                    else -> Icons.Filled.Link
                },
                contentDescription = scan.type,
                tint = statusColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = scan.input,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row {
                Text(
                    text = "${scan.type} • Risk: ${scan.confidenceScore}%",
                    color = MutedSlate,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "• $dateStr",
                    color = MutedSlate.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete record", tint = AlertRed.copy(0.7f), modifier = Modifier.size(16.dp))
        }
    }
}


// ==========================================
// SCREEN 1: ALERTS FEED (REAL-TIME BROADCASTS)
// ==========================================
@Composable
fun AlertsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val alerts by viewModel.allAlerts.collectAsState()
    val unreadCount = alerts.count { !it.isRead }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SIMULATED PUSH NOTIFICATION CHANNELS",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                if (unreadCount > 0) {
                    Badge(
                        containerColor = AlertRed,
                        modifier = Modifier.testTag("alert_badge_unread_count")
                    ) {
                        Text("$unreadCount UNREAD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                }
            }
        }

        // Live Action Broadcast Simulator Block (to fulfill 'integrates push notifications')
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            width = 1.dp,
                            brush = Brush.linearGradient(listOf(AlertRed.copy(0.6f), TechTeal.copy(0.6f)))
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(AlertRed, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE PUSH SIMULATION MODULE",
                            color = TechTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Trigger real-time push alerts of newly detected media manipulation directly on your physical/emulator device system notification layout! Interrogate dynamic alarms.",
                        color = Color.White.copy(0.85f),
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Button(
                        onClick = { viewModel.triggerSimulatedPushAlert(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("simulate_push_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.NotificationAdd, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("MOCK AUTOMATED THREAT BROADCAST", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INTEL RADAR INCIDENTS FEED",
                    color = MutedSlate,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                if (alerts.isNotEmpty()) {
                    Text(
                        text = "WIPE RADAR",
                        color = AlertRed.copy(0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clickable { viewModel.clearAllAlerts() }
                            .testTag("clear_alerts_button")
                    )
                }
            }
        }

        if (alerts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SECURE STAGE: NO DEEPFAKE ALERTIMSG RECORDED",
                        color = MutedSlate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        } else {
            items(alerts, key = { it.id }) { alert ->
                AlertItemRow(
                    alert = alert,
                    onMarkRead = { viewModel.markAlertAsRead(alert.id) },
                    onDelete = { viewModel.deleteAlert(alert.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AlertItemRow(
    alert: AlertEntity,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val levelColor = when (alert.threatLevel) {
        "CRITICAL" -> AlertRed
        "HIGH" -> CyberOrange
        "WARNING" -> CyberGold
        else -> TechTeal
    }

    val dateStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(alert.timestamp))

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (alert.isRead) CardBackground.copy(alpha = 0.5f) else CardBackground
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (alert.isRead) GridLineColor else levelColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                expanded = !expanded
                if (!alert.isRead) onMarkRead()
            }
            .testTag("alert_row_${alert.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            // High Density left border stripe corresponding to incident severity
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(levelColor)
            )

            Column(modifier = Modifier.padding(14.dp).weight(1f)) {
            // Priority banner Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(levelColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = alert.threatLevel,
                            color = DeepObsidian,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Source: ${alert.source}",
                        color = MutedSlate,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateStr,
                        color = MutedSlate,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (!alert.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(TechTeal, CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = alert.title,
                color = if (alert.isRead) Color.White.copy(0.7f) else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = alert.body,
                color = MutedSlate,
                fontSize = 12.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = GridLineColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Threat vector is live locally.",
                            color = levelColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(containerColor = AlertRed.copy(0.15f)),
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("DISMISS ALERT", color = AlertRed, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

}


// ==========================================
// SCREEN 2: OFFLINE SECURITY INTEL SCREEN
// ==========================================
@Composable
fun OfflineIntelScreen() {
    var selectedCategory by remember { mutableStateOf("VISUAL") } // VISUAL, AUDIO, METADATA
    val categoryTabs = listOf(
        Pair("VISUAL anomalies", "VISUAL"),
        Pair("VOICE synthesis", "AUDIO"),
        Pair("INTELLIGENCE tech", "METADATA")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "OFFLINE COGNITIVE SECURITY ACADEMY",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Inspect raw media patterns offline using structured criteria. No connection required.",
                color = MutedSlate,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Subcategory bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .border(1.dp, GridLineColor, RoundedCornerShape(8.dp))
            ) {
                categoryTabs.forEach { (label, catCode) ->
                    val isSel = selectedCategory == catCode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) TechTeal else Color.Transparent)
                            .clickable { selectedCategory = catCode }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label.uppercase(),
                            color = if (isSel) DeepObsidian else MutedSlate,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Details content under selected category
        when (selectedCategory) {
            "VISUAL" -> {
                item {
                    ThreatVectorGuideCard(
                        title = "Visual Deepfake Relics & Geometry Artifacts",
                        vibe = "Detecting high-risk generative face-swaps, GAN models, & temporal stitching flaws.",
                        checklists = listOf(
                            "Spectacular eye reflection mismatch (Left/Right eyes display asymmetrical light sources).",
                            "Temporal facial jitter boundaries (blur elements framing the chin, hair segments, or neck connection).",
                            "Irregular blinking patterns (Deepfakes commonly display unnaturally slow or synchronized synchronous blinks).",
                            "Asymmetrical eyeglasses or earrings (generative networks fail to replicate delicate parallel geometries accurately).",
                            "Unnatural skin texturing (superficial plastic-smooth cheek contours devoid of normal physical micro-pores)."
                        ),
                        remediation = "Always capture native screens, trigger zoom actions to check edge pixels, or run reverse-image sweeps on separated raw frames."
                    )
                }
            }
            "AUDIO" -> {
                item {
                    ThreatVectorGuideCard(
                        title = "Acoustic Cloning & Voice Synthesis Indicators",
                        vibe = "Decoding state-of-the-art voice generation vectors & corporate phone spoofing scammers.",
                        checklists = listOf(
                            "Absence of high-frequency breathing (Physical human speak includes rapid, distinct inhalations between long sentences).",
                            "Metallic robotic sound cuts (Vocal transitions lack smooth acoustic resonant slides, generating mechanical clicks).",
                            "Uniform voice volume & excitement levels (clone engines struggle to convey fluctuating biological stress profiles).",
                            "Extremely slow processing delay (AI interactive bots manifest distinct 1-2 second pauses to translate vocal inputs)."
                        ),
                        remediation = "Define an immediate personal or family passcode passcode. Hang up and verify through an encrypted offline communications channel."
                    )
                }
            }
            "METADATA" -> {
                item {
                    ThreatVectorGuideCard(
                        title = "Network Verification & Signature Verification Techniques",
                        vibe = "Validating electronic media metadata structures & algorithmic origin signatures.",
                        checklists = listOf(
                            "Complete stripping of Camera EXIF headers (manipulated social media forwards scrub physical camera signatures).",
                            "Compression ratios incongruous with capture dates (re-saved edited files demonstrate excessive JPEG artifact grids).",
                            "Reverse Lookup matches (the exact original image was taken years ago and is attributed to a separate individual).",
                            "C2PA cryptographic provenance checks (missing hardware-secured provenance signatures generated during raw frame creation)."
                        ),
                        remediation = "Validate files in specialized metadata interrogators, check SHA hashing changes, and monitor global Sentinel intelligence updates closely."
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ThreatVectorGuideCard(
    title: String,
    vibe: String,
    checklists: List<String>,
    remediation: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GridLineColor, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title.uppercase(),
                color = TechTeal,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                lineHeight = 19.sp
            )
            Text(
                text = vibe,
                color = MutedSlate,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp),
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = GridLineColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "CRITICAL ARTIFACT INSPECTION CHECKLIST:",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(10.dp))

            checklists.forEach { item ->
                var checked by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { checked = !checked },
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = if (checked) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                        contentDescription = "Check",
                        tint = if (checked) TechTeal else MutedSlate,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = item,
                        color = if (checked) Color.White else Color.White.copy(0.75f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = GridLineColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SECURE ACTION COMPASS:",
                color = CyberGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = remediation,
                color = Color.White.copy(0.9f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
