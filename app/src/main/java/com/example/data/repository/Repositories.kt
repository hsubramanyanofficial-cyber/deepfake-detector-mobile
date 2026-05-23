package com.example.data.repository

import com.example.data.database.AlertDao
import com.example.data.database.AlertEntity
import com.example.data.database.ScanDao
import com.example.data.database.ScanEntity
import kotlinx.coroutines.flow.Flow

class ScanRepository(private val scanDao: ScanDao) {
    val allScans: Flow<List<ScanEntity>> = scanDao.getAllScans()

    suspend fun saveScan(scan: ScanEntity) {
        scanDao.insertScan(scan)
    }

    suspend fun deleteScan(scanEntity: ScanEntity) {
        scanDao.deleteScanById(scanEntity.id)
    }

    suspend fun clearAll() {
        scanDao.clearAllScans()
    }
}

class AlertRepository(private val alertDao: AlertDao) {
    val allAlerts: Flow<List<AlertEntity>> = alertDao.getAllAlerts()

    suspend fun saveAlert(alert: AlertEntity) {
        alertDao.insertAlert(alert)
    }

    suspend fun markAsRead(id: Int) {
        alertDao.markAsRead(id)
    }

    suspend fun deleteAlert(alertId: Int) {
        alertDao.deleteAlertById(alertId)
    }

    suspend fun clearAll() {
        alertDao.clearAllAlerts()
    }

    // Preseed mock realistic deepfake alert database
    suspend fun seedInitialAlertsIfEmpty(currentAlerts: List<AlertEntity>) {
        if (currentAlerts.isEmpty()) {
            val defaultAlerts = listOf(
                AlertEntity(
                    title = "CRITICAL: Urgent Banking Voice Clone Campaign Active",
                    body = "We have identified an active, automated phone scam campaign in multiple regions using high-fidelity AI-cloned voices of immediate family members. Intercept target numbers are selected via scraped public social profiles. Always establish an offline family safe-word to verify identity before initiating urgent funds transfers.",
                    threatLevel = "CRITICAL",
                    source = "Sentinel CERT",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 30 // 30 minutes ago
                ),
                AlertEntity(
                    title = "HIGH: Disinformation Video of Election Officials Circulating",
                    body = "A synthetic video showing regional election officials tampering with physical ballot papers has gained millions of views across platform X. Deepfake analysis from Sentinel AI detects severe visual temporal blinking artifacts and inconsistent background shadow structures, demonstrating 98% manipulation probability.",
                    threatLevel = "HIGH",
                    source = "Global Media Watch",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 120 // 2 hours ago
                ),
                AlertEntity(
                    title = "WARNING: Generative AI Web Scams Advertising Crypto",
                    body = "A series of sponsored posts on Facebook are leveraging deepfaked celebrity video endorsements to trick users into subscribing to fraudulent, malicious crypto investment platforms. Noticeable lip-sync mismatch and flat voice dynamics are present in all verified samples.",
                    threatLevel = "WARNING",
                    source = "Federal Trade Commission",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 240 // 4 hours ago
                ),
                AlertEntity(
                    title = "INFO: App Security Update Released",
                    body = "Sentinel AI App upgraded to integrated real-time scanning powered by Gemini. Added advanced facial temporal jitter analysis models and custom webhook channels for local and global push notification simulation.",
                    threatLevel = "INFO",
                    source = "Sentinel Security",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 1440 // 1 day ago
                )
            )
            for (alert in defaultAlerts) {
                alertDao.insertAlert(alert)
            }
        }
    }
}
