package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.remote.GeminiService
import com.example.data.repository.AlertRepository
import com.example.data.repository.ScanRepository
import com.example.ui.screens.MainDashboard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Dynamic edge-to-edge support
        enableEdgeToEdge()

        // Initialize Room Database
        val database = AppDatabase.getDatabase(this)
        val scanDao = database.scanDao()
        val alertDao = database.alertDao()

        // Repositories & Remotes
        val scanRepository = ScanRepository(scanDao)
        val alertRepository = AlertRepository(alertDao)
        val geminiService = GeminiService()

        // Load MainViewModel with Factory
        val viewModelFactory = MainViewModelFactory(scanRepository, alertRepository, geminiService)
        val viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        // Dynamically request notification permissions on Android 13+ (Safe for notifications)
        requestNotificationPermission()

        setContent {
            MyApplicationTheme {
                MainDashboard(viewModel)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionState = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            if (permissionState != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 101
    }
}

@androidx.compose.runtime.Composable
fun Greeting(name: String, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}
