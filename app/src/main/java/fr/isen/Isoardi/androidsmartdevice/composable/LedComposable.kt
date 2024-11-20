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
    onLedButtonClick: (LedStateEnum) -> Unit,
    onDisconnectClick: () -> Unit
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

            // Boutons pour contrôler les LEDs
            Button(
                onClick = { onLedButtonClick(LedStateEnum.NONE) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected
            ) {
                Text(text = "0 LEDs")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onLedButtonClick(LedStateEnum.Led_1) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected
            ) {
                Text(text = "LED n°1")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onLedButtonClick(LedStateEnum.Led_2) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected
            ) {
                Text(text = "LED n°2")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onLedButtonClick(LedStateEnum.Led_3) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected
            ) {
                Text(text = "LED n°3")
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onDisconnectClick,
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                enabled = isConnected
            ) {
                Text(text = "Se déconnecter")
            }
        }
    }
}
