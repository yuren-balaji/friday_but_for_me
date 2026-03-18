package com.example.first_app_0_0_1.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.first_app_0_0_1.export.DataExportManager
import com.example.first_app_0_0_1.import.DataImportManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val exportManager = DataExportManager(context)
    val importManager = DataImportManager(context)

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            coroutineScope.launch {
                exportManager.exportDataToJson()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                importManager.importDataFromJson(it)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = { 
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                 when (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    PackageManager.PERMISSION_GRANTED -> {
                        coroutineScope.launch {
                            exportManager.exportDataToJson()
                        }
                    }
                    else -> {
                        exportLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            } else {
                coroutineScope.launch {
                    exportManager.exportDataToJson()
                }
            }
        }) {
            Text("Export Data")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { 
            importLauncher.launch("application/json")
        }) {
            Text("Import Data")
        }
    }
}
