package fr.isen.Isoardi.androidsmartdevice.composable

import android.bluetooth.le.ScanResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.Isoardi.androidsmartdevice.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    modifier: Modifier = Modifier,
    scanResults: List<ScanResult>,
    isScanning: Boolean,
    onScanToggle: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AndroidSmartDevice") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.LightGray,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Lancer le Scan BLE",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.fleche),
                    contentDescription = "Arrow Icon",
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onScanToggle) {
                Text(text = if (isScanning) "Arrêter le scan BLE" else "Lancer le scan BLE")
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isScanning) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                items(scanResults) { result ->
                    val deviceName = result.device.name ?: "Inconnu"
                    val deviceAddress = result.device.address ?: "Adresse inconnue"
                    Text(text = "Appareil : $deviceName\nAdresse: $deviceAddress")
                    Divider(color = Color.LightGray, thickness = 1.dp)
                }
            }
        }
    }
}
