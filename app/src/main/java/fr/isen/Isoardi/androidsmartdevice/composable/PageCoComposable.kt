package fr.isen.Isoardi.androidsmartdevice

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageCoComposable(
    deviceName: String,
    rssi: Int,
    isConnected: Boolean,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onNavigateToLedControl: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connexion à l'appareil") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = deviceName,
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Puissance du signal : $rssi dBm",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(50.dp))

            if (isConnected) {
                Button(onClick = onDisconnectClick) {
                    Text(text = "Se deconnecter")
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = onNavigateToLedControl) {
                    Text(text = "Controler les LEDs")
                }
            } else {
                Button(onClick = onConnectClick) {
                    Text(text = "Se connecter")
                }
            }
        }
    }
}
