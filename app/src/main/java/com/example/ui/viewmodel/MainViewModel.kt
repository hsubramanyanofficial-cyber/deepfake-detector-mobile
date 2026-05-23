package com.example.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AlertEntity
import com.example.data.database.ScanEntity
import com.example.data.remote.GeminiService
import com.example.data.repository.AlertRepository
import com.example.data.repository.ScanRepository
import com.example.ui.utils.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val scanRepository: ScanRepository,
    private val alertRepository: AlertRepository,
    private val geminiService: GeminiService
) : ViewModel() {

    // Streams of Database state
    val allScans: StateFlow<List<ScanEntity>> = scanRepository.allScans
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allAlerts: StateFlow<List<AlertEntity>> = alertRepository.allAlerts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI state holder
    var isScanning by mutableStateOf(false)
        private set

    var scanError by mutableStateOf<String?>(null)

    // Last scan done
    var lastScannedResult by mutableStateOf<ScanEntity?>(null)

    init {
        // Automatically seed initial mock alerts if empty
        viewModelScope.launch {
            allAlerts.collectLatest { alerts ->
                alertRepository.seedInitialAlertsIfEmpty(alerts)
            }
        }
    }

    fun analyzeMedia(
        context: Context,
        type: String,
        input: String,
        additionalInfo: String,
        apiKey: String
    ) {
        viewModelScope.launch {
            isScanning = true
            scanError = null
            try {
                // Perform deepfake analysis
                val result = geminiService.analyzeMedia(type, input, additionalInfo, apiKey)
                
                // Formulate the scan database record
                val scanRecord = ScanEntity(
                    type = type,
                    title = when (type) {
                        "IMAGE" -> "Visual Alignment Check"
                        "AUDIO" -> "Acoustic Synthesizer Check"
                        "VIDEO" -> "Temporal Deepfake Check"
                        "URL" -> "Media Origin Signature"
                        else -> "Contextual Threat Report"
                    },
                    input = input.ifBlank { "Unspecified Media Reference" },
                    confidenceScore = result.confidenceScore,
                    isManipulated = result.isManipulated,
                    analysisResult = result.reportText,
                    timestamp = System.currentTimeMillis()
                )

                scanRepository.saveScan(scanRecord)
                lastScannedResult = scanRecord

                // Send immediate dynamic system notification alert if critical manipulation is detected
                if (result.isManipulated && result.confidenceScore > 65) {
                    NotificationHelper.showNotification(
                        context = context,
                        title = "Verification Check Alert",
                        message = "Sentinel detected high probability of synthesis manipulation (${result.confidenceScore}%) on verified media: $input",
                        threatLevel = "HIGH"
                    )
                }

            } catch (e: Exception) {
                scanError = "Authentication check failed: ${e.message}"
            } finally {
                isScanning = false
            }
        }
    }

    fun clearAllScans() {
        viewModelScope.launch {
            scanRepository.clearAll()
            lastScannedResult = null
        }
    }

    fun deleteScan(scan: ScanEntity) {
        viewModelScope.launch {
            scanRepository.deleteScan(scan)
            if (lastScannedResult?.id == scan.id) {
                lastScannedResult = null
            }
        }
    }

    fun deleteAlert(alertId: Int) {
        viewModelScope.launch {
            alertRepository.deleteAlert(alertId)
        }
    }

    fun markAlertAsRead(alertId: Int) {
        viewModelScope.launch {
            alertRepository.markAsRead(alertId)
        }
    }

    fun clearAllAlerts() {
        viewModelScope.launch {
            alertRepository.clearAll()
        }
    }

    // Interactive Trigger to demonstrate live Push Notification / Automated Alerts
    fun triggerSimulatedPushAlert(context: Context) {
        viewModelScope.launch {
            val alertCampaigns = listOf(
                Pair(
                    "CRITICAL: Live CEO Voice Clone Harassment Campaign",
                    "Security alerts identify synthetic audio mimicking chief corporate decision makers circulating today. Scammers order urgent secure database access credentials or bank level wires. Verify internally using encrypted safe-words!"
                ),
                Pair(
                    "HIGH: Social Media Deepfake of Prominent Anchor",
                    "Deepfaked footage of standard network presenters reporting massive local natural emergencies has been identified. Check local city municipal channels before initiating community-wide panic actions."
                ),
                Pair(
                    "WARNING: AI-altered Identity Verification Spoofs",
                    "Vibe templates and biometric KYC checks are leaking face-restoration templates designed to bypass remote identity verification engines. Standard Sentinel shields remain 100% active."
                )
            )

            val select = alertCampaigns.random()
            val newAlert = AlertEntity(
                title = select.first,
                body = select.second,
                threatLevel = if (select.first.contains("CRITICAL")) "CRITICAL" else if (select.first.contains("HIGH")) "HIGH" else "WARNING",
                source = "Sentinel Push Broadcast"
            )

            alertRepository.saveAlert(newAlert)

            // Fire natural system notification
            NotificationHelper.showNotification(
                context = context,
                title = newAlert.title,
                message = newAlert.body,
                threatLevel = newAlert.threatLevel
            )
        }
    }
}

// ViewModel factory helper
class MainViewModelFactory(
    private val scanRepository: ScanRepository,
    private val alertRepository: AlertRepository,
    private val geminiService: GeminiService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(scanRepository, alertRepository, geminiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
