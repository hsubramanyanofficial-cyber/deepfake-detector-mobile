package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "scan_history")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "IMAGE", "AUDIO", "VIDEO", "URL", "TEXT"
    val title: String,
    val input: String,
    val confidenceScore: Int, // 0 - 100% chance of manipulation
    val isManipulated: Boolean,
    val analysisResult: String,
    val timestamp: Long = System.currentTimeMillis()
) : java.io.Serializable

@Entity(tableName = "alert_logs")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val threatLevel: String, // "CRITICAL", "HIGH", "WARNING", "INFO"
    val source: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) : java.io.Serializable
