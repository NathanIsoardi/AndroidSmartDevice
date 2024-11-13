package fr.isen.Isoardi.androidsmartdevice

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun getColorForRSSI(rssi: Int): Color {
    return when {
        rssi >= -50 -> Color(0xFF4CAF50) // Vert
        rssi >= -70 -> Color(0xFFFFC107) // Jaune
        else -> Color(0xFFF44336)        // Rouge
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    modifier: Modifier = Modifier,
    scanResults: List<BLEDevice>,
    isScanning: Boolean,
    onScanToggle: () -> Unit,
    onDeviceClick: (BLEDevice) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AndroidSmartDevice") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Button(
                onClick = onScanToggle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isScanning) "Arrêter le scan BLE" else "Lancer le scan BLE")
            }

            if (isScanning) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (scanResults.isEmpty()) {
                Text(
                    text = if (isScanning) "Scanning en cours..." else "Aucun appareil détecté",
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(scanResults) { device ->
                        ListItem(
                            modifier = Modifier
                                .clickable { onDeviceClick(device) }
                                .padding(vertical = 8.dp),
                            headlineContent = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Colonne pour le nom et l'adresse
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = device.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = device.address,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    // Cercle avec le RSSI
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(color = getColorForRSSI(device.rssi))
                                    ) {
                                        Text(
                                            text = "${device.rssi}",
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

