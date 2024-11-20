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
fun LedComposable(
    deviceName: String,
    isConnected: Boolean,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onLedButtonClick: (LEDStateEnum) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contrôle des LEDs") }
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
            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Appareil : $deviceName",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isConnected) {
                Text(
                    text = "Connecté",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Text(
                    text = "Déconnecté",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onConnectClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isConnected
            ) {
                Text(text = "Se connecter")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Boutons pour contrôler les LEDs
            Button(
                onClick = { onLedButtonClick(LEDStateEnum.NONE) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected
            ) {
                Text(text = "Éteindre les LEDs")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onLedButtonClick(LEDStateEnum.LED_1) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected
            ) {
                Text(text = "Allumer LED n°1")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onLedButtonClick(LEDStateEnum.LED_2) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected
            ) {
                Text(text = "Allumer LED n°2")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onLedButtonClick(LEDStateEnum.LED_3) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected
            ) {
                Text(text = "Allumer LED n°3")
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onDisconnectClick,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected
            ) {
                Text(text = "Se déconnecter")
            }
        }
    }
}
