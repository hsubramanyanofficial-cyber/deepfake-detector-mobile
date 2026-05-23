package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanEntity)

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteScanById(id: Int)

    @Query("DELETE FROM scan_history")
    suspend fun clearAllScans()
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alert_logs ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Update
    suspend fun updateAlert(alert: AlertEntity)

    @Query("UPDATE alert_logs SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("DELETE FROM alert_logs WHERE id = :id")
    suspend fun deleteAlertById(id: Int)

    @Query("DELETE FROM alert_logs")
    suspend fun clearAllAlerts()
}
